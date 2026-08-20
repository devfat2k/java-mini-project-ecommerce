# ============================================================
# STAGE 1: BUILDER — nặng, chỉ tồn tại tạm thời lúc build,
# không bị đẩy vào image cuối cùng deploy thật
# ============================================================

# Base image: bản Linux Alpine (siêu nhẹ) đã cài sẵn JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# --- Tải dependency Maven TRƯỚC để tận dụng Docker layer cache ---
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Cấp quyền thực thi cho Maven wrapper
RUN chmod +x mvnw

# Tải toàn bộ dependencies về trước
RUN ./mvnw dependency:go-offline -B

# --- Copy source code sau ---
COPY src ./src

# Build file .jar (bỏ qua unit test lúc packaging image)
RUN ./mvnw clean package -DskipTests


# ============================================================
# STAGE 2: RUNTIME — siêu nhẹ, dùng JRE 21 Alpine
# ============================================================

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy artifact duy nhất từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Khai báo port Tomcat
EXPOSE 8085

# JVM Options tối ưu cho Container Environment:
# - XX:MaxRAMPercentage: Tự động dùng tối đa 75% RAM được cấp cho container
# - XX:+UseContainerSupport: Tự nhận diện CPU/RAM limits từ Docker
# - Djava.security.egd: Tăng tốc độ khởi tạo SecureRandom
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]