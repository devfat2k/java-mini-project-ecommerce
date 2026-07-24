# ============================================================
# STAGE 1: BUILDER — nặng, chỉ tồn tại tạm thời lúc build,
# không bị đẩy vào image cuối cùng deploy thật
# ============================================================

# Base image: bản Linux Alpine (siêu nhẹ) đã cài sẵn JDK 21
# Dùng JDK (không phải JRE) vì bước này cần COMPILE code -> bắt buộc có javac
FROM eclipse-temurin:21-jdk-alpine AS builder

# Đặt thư mục làm việc bên trong image là /app
# Mọi lệnh COPY/RUN phía sau đều chạy tương đối từ thư mục này
WORKDIR /app

# --- Nhóm lệnh tải dependency Maven TRƯỚC, tách riêng khỏi source code ---
# Lý do: pom.xml rất ít khi đổi, còn code Java thay đổi liên tục.
# Nếu gộp chung (COPY . . rồi mới build), Docker không phân biệt được
# cái gì đổi cái gì không -> hễ code đổi là build lại từ đầu, kể cả
# bước tải dependency vốn không cần tải lại. Tách riêng ra để dùng CACHE.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Cấp quyền thực thi cho file mvnw (Maven wrapper)
# Vì quá trình COPY từ máy host sang container không luôn giữ nguyên
# quyền thực thi -> phải chủ động chmod lại, tránh lỗi "Permission denied"
RUN chmod +x mvnw

# Tải toàn bộ dependency Maven về, dựa vào nội dung pom.xml
# Layer này chỉ bị invalidate (build lại) khi pom.xml thay đổi
# (VD thêm dependency mới), KHÔNG bị ảnh hưởng khi sửa code business logic
RUN ./mvnw dependency:go-offline -B

# --- Copy source code SAU, vì đây là phần thay đổi thường xuyên nhất ---
# . đầu = thư mục src trên máy host, ./src = đích trong /app của image
COPY src ./src

# Build ra file .jar thật sự
# -DskipTests: bỏ qua chạy unit test lúc build, để build nhanh hơn
# (test được chạy riêng ở bước CI, không phải lúc đóng gói image)
RUN ./mvnw clean package -DskipTests


# ============================================================
# STAGE 2: RUNTIME — nhẹ, đây mới là image THẬT SỰ deploy lên server
# ============================================================

# Bắt đầu 1 image HOÀN TOÀN MỚI, sạch, không kế thừa gì từ stage builder
# Dùng JRE (không phải JDK) vì giờ chỉ cần CHẠY .jar, không cần compile
# gì nữa -> không cần Maven, không cần javac -> image nhẹ hơn nhiều
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy DUY NHẤT 1 file .jar đã build xong từ stage "builder" sang đây.
# Mọi thứ khác ở stage 1 (source code gốc, cache Maven ~/.m2, pom.xml,
# JDK...) bị BỎ LẠI HOÀN TOÀN, không lọt vào image cuối cùng.
# --from=builder: chỉ định lấy từ stage đã đặt tên "builder" ở trên
COPY --from=builder /app/target/*.jar app.jar

# Chỉ mang tính khai báo/tài liệu - cổng mà app SẼ dùng bên trong container.
# Không tự động mở port ra ngoài - việc "mở" thật là do -p hoặc "ports:"
# trong docker-compose.yml quyết định. Khớp đúng port Tomcat thật (8085).
EXPOSE 8085

# Lệnh chạy khi container khởi động - chạy file .jar vừa copy vào
ENTRYPOINT ["java", "-jar", "app.jar"]