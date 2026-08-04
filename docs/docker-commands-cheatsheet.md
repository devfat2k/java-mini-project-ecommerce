# 🐳 Docker Cheatsheet — Mini Ecommerce Project

> Tra nhanh khi cần build/chạy/debug project. Không cần nhớ hết — chỉ cần biết chỗ nào để tra.

---

## 🚀 Việc thường làm nhất (đọc phần này trước)

| Muốn làm gì | Lệnh |
|---|---|
| Code/debug hàng ngày (chạy app qua IntelliJ) | `docker compose up db redis minio` |
| Test đóng gói toàn bộ, y hệt production | `docker compose up --build` |
| Dừng hết, giữ lại data | `docker compose down` |
| Xem app có lỗi gì không | `docker compose logs -f app` |

---

## 1. Kiểm tra hệ thống

| Lệnh | Công dụng |
|---|---|
| `docker --version` | Xem version Docker CLI đang cài |
| `docker compose version` | Xem version Compose plugin |
| `docker info` | Xem thông tin Docker Daemon — thấy `Server: ...` nghĩa là daemon đang sống |
| `docker run hello-world` | Test nhanh: daemon có chạy được container không |

---

## 2. Build Image

| Lệnh | Công dụng |
|---|---|
| `docker build -t <ten>:<tag> .` | Build 1 image từ Dockerfile ở thư mục hiện tại. VD: `docker build -t mini-ecommerce:v1 .` |
| `docker compose build` | Build lại image của service khai báo trong `docker-compose.yml`, KHÔNG chạy |
| `docker compose up --build` | Build lại + chạy luôn toàn bộ service — dùng khi vừa sửa Dockerfile hoặc sửa code |
| `docker images` | Liệt kê toàn bộ image đang có trên máy, xem tên/size |
| `docker rmi <ten_image>` | Xoá 1 image (phải xoá hết container đang dùng nó trước) |
| `docker tag <cu> <moi>` | Gắn thêm 1 tên/tag khác cho cùng 1 image |

---

## 3. Chạy Container / Compose

| Lệnh | Công dụng |
|---|---|
| `docker run -d --name <ten> -p <host>:<container> --env-file .env <image>` | Chạy 1 container đơn lẻ, chạy nền (`-d`), nạp biến môi trường từ file `../../../../../../../../../Downloads/.env` |
| `docker compose up` | Chạy toàn bộ service theo `docker-compose.yml` (dùng image đã build sẵn, không build lại) |
| `docker compose up -d` | Chạy nền, không chiếm Terminal |
| `docker compose up <ten_service1> <ten_service2>` | Chỉ chạy đúng những service chỉ định. VD: `docker compose up db redis minio` (bỏ qua `app` vì chạy `app` bằng IntelliJ) |
| `docker compose down` | Dừng + xoá container của project (data trong volume vẫn giữ) |
| `docker compose down -v` | Dừng + xoá luôn cả volume — **MẤT DATA DB, cẩn thận, chỉ dùng khi chắc chắn muốn làm sạch từ đầu** |

---

## 4. Xem trạng thái

| Lệnh | Công dụng |
|---|---|
| `docker ps` | Liệt kê container **đang chạy** |
| `docker ps -a` | Liệt kê **toàn bộ** container, kể cả đã dừng (Exited) |
| `docker compose ps` | Trạng thái các service thuộc project compose hiện tại |
| `docker network ls` | Xem các network hiện có — kiểm tra network riêng Compose đã tạo (VD `mini-ecommerce_default`) |

---

## 5. Debug (dùng khi có lỗi)

| Lệnh | Công dụng |
|---|---|
| `docker logs <ten_container>` | Xem toàn bộ log của 1 container |
| `docker logs -f <ten_container>` | Xem log real-time, giống `tail -f` |
| `docker compose logs -f <ten_service>` | Xem log real-time của riêng 1 service trong compose (VD `app`, `db`) |
| `docker exec -it <ten_container> sh` | Chui vào bên trong container đang chạy, mở shell tương tác |
| `docker exec -it <ten_container> env` | Xem toàn bộ biến môi trường thực sự đang inject vào container |
| `docker inspect <ten_container>` | Xem cấu hình chi tiết (IP, mount, env, network...) dạng JSON |

---

## 6. Dừng / Xoá

| Lệnh | Công dụng |
|---|---|
| `docker stop <ten>` | Dừng container — gửi tín hiệu graceful (SIGTERM), cho tiến trình bên trong kịp dọn dẹp trước khi thoát |
| `docker kill <ten>` | Dừng ngay lập tức, không cho kịp dọn dẹp — chỉ dùng khi `stop` bị treo |
| `docker rm <ten>` | Xoá container đã dừng |
| `docker rm -f <ten>` | Xoá container luôn, kể cả đang chạy (force) |
| `docker restart <ten>` | Khởi động lại 1 container |

---

## 7. Dọn dẹp (tránh đầy ổ đĩa)

| Lệnh | Công dụng |
|---|---|
| `docker system prune` | Xoá container đã dừng + network/image không còn container nào dùng |
| `docker system prune -a` | Mạnh tay hơn — xoá cả image không có container nào tham chiếu tới |
| `docker volume prune` | Xoá volume không còn container nào dùng |

---

## 🔴 Lỗi hay gặp — tra nhanh

| Lỗi | Nguyên nhân / hướng fix |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop chưa mở/chưa khởi động xong |
| `port is already allocated` | Port đang bị chiếm bởi container/app khác — `docker ps` kiểm tra rồi `docker stop` |
| `no space left on device` | Image/container cũ tích tụ — chạy `docker system prune` |
| Container "Exited (1)" ngay sau start | `docker logs <ten>` xem lý do — thường thiếu biến môi trường bắt buộc |
| `error getting credentials - docker-credential-desktop` | PATH thiếu đường dẫn tới Docker Desktop — restart Docker Desktop, mở Terminal mới |
| Build lại vẫn mất thời gian dù chỉ sửa 1 dòng code | Dockerfile đang `COPY . .` trước khi build — tách riêng `COPY pom.xml` + tải dependency ra trước `COPY src` |
| `Connection refused` khi app gọi DB/Redis trong container khác | Đang dùng `localhost` — phải đổi thành **tên service** trong `docker-compose.yml` (VD `db`, `redis`) |
| `Could not resolve placeholder 'XXX'` | Thiếu biến môi trường — kiểm tra `environment:` trong compose hoặc `--env-file` |
| Container tên bị trùng khi chạy lại | `docker rm -f <ten>` xoá container cũ (dù đã Exited, tên vẫn bị giữ) |
| `../../../../../../../../../Downloads/.env` không tự được đọc dù có COPY vào image | Spring Boot / Docker không tự parse file `../../../../../../../../../Downloads/.env` — phải dùng `--env-file` (docker run) hoặc để Compose tự đọc `../../../../../../../../../Downloads/.env` cùng thư mục |

---

## 📌 Ghi chú riêng cho project này

- **Local code/debug:** chạy `db`/`redis`/`minio` qua Docker, chạy `app` qua IntelliJ để debug — dùng `localhost` trong `../../../../../../../../../Downloads/.env`.
- **Test đóng gói:** `docker compose up --build` chạy cả 4 service, `app` gọi các service khác bằng tên (`db`, `redis`, `minio`), không phải `localhost`.
- **Tag ảnh:** hiện dùng `latest` — ổn cho local. Khi vào Giai đoạn CI/CD (deploy thật), nên đổi sang tag theo version hoặc git commit hash (VD `mini-ecommerce:v1.2.0` hoặc `mini-ecommerce:a1b2c3d`) để tránh nhầm lẫn "latest" không tự đồng bộ giữa các máy/môi trường.
