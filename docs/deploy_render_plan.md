# 🚀 PLAN CHUYÊN SÂU: Deploy BE lên Render (Docker + CI/CD từ số 0 đến URL thật)

> **Mục tiêu cuối:** Có 1 URL public (VD: `https://mini-ecommerce-api.onrender.com`) chạy Spring Boot BE thật, kèm Swagger UI public để demo, và hiểu đủ sâu Docker + CI/CD để trả lời phỏng vấn tự tin — không phải chỉ "làm theo được".
>
> **Thời gian dự kiến:** Giai đoạn 0 giờ đã sâu hơn nhiều (không còn 30-45 phút) — ước tính thật ~3-4 tiếng để vừa đọc vừa gõ tay thực hành. Tổng cả plan ~10-12 tiếng làm việc thật sự. Với ~6+ tiếng/ngày, hợp lý là **Giai đoạn 0 làm trọn hôm nay** (đáng giá hơn là làm vội), phần deploy thật (Giai đoạn 1-5) dời sang ngày mai — không sao cả, hiểu chắc Docker quan trọng hơn có URL sớm 1 ngày.
>
> **Quyết định kiến trúc đã chốt (Hướng A):** Giữ nguyên code MinIO SDK (S3-compatible) → chỉ đổi endpoint sang **Cloudflare R2** (free 10GB, không tính phí egress) ở môi trường production. Không sửa `StorageService`/`MinioConfig` logic, chỉ đổi biến môi trường.
>
> ## ⚠️ 2 RỦI RO THẬT ĐÃ XÁC MINH — bắt buộc xử lý, không phải tuỳ chọn
>
> **Rủi ro 1 — Email KHÔNG gửi được trên Render free tier.** Từ 26/09/2025, Render chặn hẳn outbound traffic tới toàn bộ port SMTP (25, 465, 587) trên free web service. `JavaMailSender` hiện tại của anh dùng SMTP → sẽ timeout im lặng trên production, dù local vẫn chạy tốt. **Bắt buộc sửa ở 1.4 bên dưới** (chuyển sang gửi mail qua HTTP API thay vì SMTP).
>
> **Rủi ro 2 — VNPay IPN có thể bị miss do cold-start.** Free service ngủ sau 15 phút không traffic, thức dậy mất 30-60s. Nếu VNPay gọi webhook đúng lúc service đang ngủ, request có thể timeout trước khi app kịp thức. **Xử lý ở 3D bên dưới** (giữ service "thức" bằng health-check ping định kỳ).

---

## 🗺️ Bản đồ 6 Giai đoạn

```
GIAI ĐOẠN 0 — Docker chuyên sâu: khái niệm + cài đặt + lệnh + tự viết   [~3-4 tiếng]
GIAI ĐOẠN 1 — Dựng hạ tầng Production (Postgres, Redis, R2 trên Render/Cloudflare)  [~1-1.5h]
GIAI ĐOẠN 2 — Chuẩn bị code cho Production (config, security, CORS)        [~1.5-2h]
GIAI ĐOẠN 3 — Deploy lần đầu lên Render (thủ công, để hiểu cơ chế)         [~1.5-3h]
GIAI ĐOẠN 4 — CI/CD thật: tự động deploy khi push code                    [~1h]
GIAI ĐOẠN 5 — Smoke Test + cập nhật Doc với link demo thật                [~1h]
```

**Nguyên tắc xuyên suốt:** Mỗi Giai đoạn có mục "Tự kiểm tra hiểu" — trả lời được thì mới qua bước sau. Không cần trả lời hoàn hảo, chỉ cần giải thích được bằng lời của mình, không phải học thuộc.

---

# GIAI ĐOẠN 0 — Docker chuyên sâu (từ hiểu → tự viết được, không chỉ đọc lại)

*Anh nói thật là chưa vững Docker — vậy Giai đoạn này đổi mục tiêu: không chỉ "ôn lại file có sẵn" nữa, mà đủ để tự cài, tự gõ lệnh, tự viết được 1 Dockerfile/Compose từ đầu nếu cần. Khái niệm đi nhanh, phần lệnh + thực hành đi chậm và kỹ — đúng thứ hay bị hỏi khi phỏng vấn thực chiến ("anh gõ lệnh gì để...", không phải "định nghĩa Docker là gì").*

**Sơ đồ: Bức tranh tổng thể Docker hoạt động thế nào**
```
  Dockerfile (công thức nấu ăn — dạng text, anh viết)
        │  docker build
        ▼
  Docker Image (món ăn đã nấu xong, đóng gói sẵn — tĩnh, chỉ đọc)
        │  docker run
        ▼
  Docker Container (đĩa ăn đang được dọn ra bàn — 1 instance đang chạy,
                     có thể tạo nhiều container từ CÙNG 1 image)
        │
        ▼
  Docker Registry (VD: Docker Hub, GHCR) = "kho lưu công thức đã nấu sẵn",
  dùng docker push/pull để đẩy lên / tải image có sẵn về, không cần build lại
```

**So sánh nhanh Container vs Máy ảo (VM) — câu hỏi phỏng vấn kinh điển:**
VM ảo hoá cả phần cứng, mỗi VM có hệ điều hành (kernel) riêng → nặng, khởi động chậm (phút). Container chia sẻ chung kernel của máy host, chỉ đóng gói riêng phần ứng dụng + thư viện cần thiết → nhẹ, khởi động nhanh (giây). Đây là lý do 1 máy chạy được hàng chục container nhưng chỉ vài VM.

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| Docker tổng quan (get started) | `docker get started overview` | https://docs.docker.com/get-started/docker-overview/ |
| Docker CLI đầy đủ | `docker cli reference` | https://docs.docker.com/reference/cli/docker/ |
| Dockerfile reference đầy đủ | `dockerfile reference instructions` | https://docs.docker.com/reference/dockerfile/ |
| Compose file reference đầy đủ | `docker compose file reference` | https://docs.docker.com/reference/compose-file/ |
| Docker Desktop cài macOS | `docker desktop install mac` | https://docs.docker.com/desktop/setup/install/mac-install/ |

**🔴 Lỗi thường gặp khi mới cài/dùng Docker (tra nhanh khi gặp):**
| Lỗi (từ khoá để nhớ) | Nguyên nhân thường gặp |
|---|---|
| `Cannot connect to the Docker daemon` | Docker Desktop chưa mở/chưa khởi động xong — mở app, đợi icon cá voi hết loading |
| `port is already allocated` | Có container/app khác đang chiếm port 8080/5432 — `docker ps` để kiểm tra và stop |
| `no space left on device` | Image/container cũ tích tụ lâu ngày — chạy `docker system prune` |
| Container "Exited (1)" ngay sau start | Xem `docker logs <container>` — thường do thiếu biến môi trường bắt buộc |
| `permission denied while trying to connect to the Docker daemon socket` | Trên Linux thường do user chưa thuộc group `docker`; trên macOS hiếm gặp nếu dùng Docker Desktop bình thường |

## 0.0. Docker Daemon & Docker Desktop — Docker thật sự chạy ở đâu

**Khái niệm cốt lõi (nhanh):** Khi gõ lệnh `docker`, câu lệnh đó là **CLI client** — nó gửi yêu cầu tới **Docker Daemon** (`dockerd`), một tiến trình chạy nền chịu trách nhiệm thật sự build image, chạy container, quản lý network/volume. Trên macOS, Docker Desktop chính là ứng dụng chạy Docker Daemon bên trong 1 máy ảo Linux nhẹ (vì kernel Linux mới chạy container thật, macOS không có sẵn) — đây là lý do phải mở Docker Desktop lên (icon cá voi ở thanh menu) trước khi gõ bất kỳ lệnh `docker` nào, nếu chưa mở sẽ báo lỗi `Cannot connect to the Docker daemon`.

**Tự kiểm tra hiểu:** Vì sao trên macOS phải "mở Docker Desktop" trước, còn trên server Linux thật thì không cần app nào cả, chỉ cần cài `dockerd` chạy nền? *(Gợi ý: Linux có kernel hỗ trợ container natively; macOS/Windows phải chạy 1 máy ảo Linux ẩn bên trong Docker Desktop để giả lập môi trường đó.)*

## 0.1. Cài đặt & xác nhận Docker chạy đúng

**Việc cần làm (macOS, đúng theo máy anh đang dùng):**
1. Nếu chưa cài: tải Docker Desktop tại `docker.com/products/docker-desktop` (bản cho Mac, chọn đúng chip Apple Silicon hoặc Intel).
2. Mở Docker Desktop, đợi icon cá voi hết "đang khởi động".
3. Mở Terminal, gõ lần lượt để xác nhận:
```bash
docker --version          # xem version CLI
docker compose version    # xem version Compose plugin
docker info                # xem thông tin daemon đang chạy (Server: ... nghĩa là daemon sống)
docker run hello-world     # test chạy container đầu tiên, tải image test, in ra thông báo thành công
```

**Tự kiểm tra hiểu:** `docker run hello-world` thật ra vừa làm 2 việc gì liên tiếp? *(Gợi ý: (1) không thấy image `hello-world` ở local → tự động `pull` về từ Docker Hub, (2) sau đó mới `run` tạo container từ image đó — 2 bước gộp làm 1 lệnh.)*

## 0.2. Các lệnh Docker dùng trong thực tế hàng ngày

**Nguyên tắc học phần này:** đừng học thuộc — mở Terminal, tự gõ thử từng lệnh trên chính project `mini-ecommerce` ngay khi đọc, nhớ bằng tay nhanh hơn nhớ bằng mắt.

**Nhóm 1 — Quản lý Image:**
| Lệnh | Tác dụng |
|---|---|
| `docker build -t mini-ecommerce:v1 .` | Build image từ Dockerfile ở thư mục hiện tại, đặt tên+tag `mini-ecommerce:v1` |
| `docker images` | Liệt kê toàn bộ image đang có ở local |
| `docker rmi <image_id>` | Xoá 1 image (phải xoá hết container dùng nó trước) |
| `docker tag mini-ecommerce:v1 mini-ecommerce:latest` | Gắn thêm 1 tag khác cho cùng 1 image |

**Nhóm 2 — Quản lý Container:**
| Lệnh | Tác dụng |
|---|---|
| `docker run -d -p 8080:8080 --name api mini-ecommerce:v1` | Chạy container nền (`-d`), map port máy:port container, đặt tên `api` |
| `docker ps` | Liệt kê container **đang chạy** |
| `docker ps -a` | Liệt kê **toàn bộ** container kể cả đã dừng |
| `docker stop api` | Dừng container (gửi tín hiệu graceful shutdown) |
| `docker rm api` | Xoá container đã dừng |
| `docker restart api` | Khởi động lại |

**Nhóm 3 — Debug (dùng nhiều nhất khi có lỗi):**
| Lệnh | Tác dụng |
|---|---|
| `docker logs api` | Xem log output của container (giống xem console log app) |
| `docker logs -f api` | Xem log real-time, giống `tail -f` |
| `docker exec -it api sh` | "Chui vào" bên trong container đang chạy, mở shell tương tác để tự kiểm tra file/env bên trong |
| `docker inspect api` | Xem chi tiết cấu hình container (IP, mount, env...) dạng JSON |

**Nhóm 4 — Dọn dẹp (tránh đầy ổ đĩa):**
| Lệnh | Tác dụng |
|---|---|
| `docker system prune` | Xoá container đã dừng, network/image không dùng tới |
| `docker system prune -a` | Xoá luôn cả image không có container nào tham chiếu tới (mạnh tay hơn) |
| `docker volume prune` | Xoá volume không còn container nào dùng |

**Nhóm 5 — Docker Compose (thay vì gõ `docker run` dài dòng cho nhiều service):**
| Lệnh | Tác dụng |
|---|---|
| `docker compose up` | Đọc `docker-compose.yml`, tạo & chạy toàn bộ service khai báo trong đó |
| `docker compose up -d` | Chạy nền |
| `docker compose up --build` | Build lại image trước khi chạy (dùng khi vừa sửa Dockerfile/code) |
| `docker compose down` | Dừng & xoá toàn bộ container của project (giữ lại volume mặc định) |
| `docker compose down -v` | Dừng & xoá luôn cả volume (mất data DB local — cẩn thận) |
| `docker compose logs -f app` | Xem log riêng 1 service tên `app` trong file compose |
| `docker compose ps` | Liệt kê service đang chạy thuộc project compose này |

**Tự kiểm tra hiểu:** Khác nhau giữa `docker stop` và `docker kill`? Giữa `docker compose down` và `docker compose down -v`? *(Gợi ý: `stop` gửi tín hiệu để app tự dọn dẹp rồi thoát, `kill` ngắt ngay lập tức không cho kịp dọn; `-v` xoá luôn volume — nếu Postgres đang chạy trong đó, mất sạch data.)*

## 0.3. Docker Image sâu hơn — Layer, Cache, Tag

**Khái niệm cốt lõi:** Mỗi dòng `RUN`/`COPY`/`ADD` trong Dockerfile tạo ra **1 layer** riêng, được cache lại. Lần build sau, nếu 1 layer không đổi (nội dung dòng lệnh + file liên quan giống hệt lần trước), Docker **tái sử dụng cache**, không chạy lại — đây là lý do thứ tự các dòng trong Dockerfile ảnh hưởng tốc độ build: nên đặt dòng **ít thay đổi nhất lên trước** (VD: `COPY pom.xml` rồi `RUN mvn dependency:go-offline` TRƯỚC khi `COPY` toàn bộ source code — vì `pom.xml` ít đổi hơn source code, tách riêng để cache layer tải dependency, không phải tải lại mỗi lần sửa 1 dòng code).

**Tag** không phải "phiên bản" theo nghĩa cố định — chỉ là 1 cái tên trỏ tới 1 image cụ thể, có thể trỏ lại. `latest` là tag mặc định nếu không ghi rõ, **không có nghĩa là "mới nhất" tự động** — chỉ là quy ước, ai đó phải chủ động build và tag `latest` thì nó mới đúng là bản mới nhất.

**Tự kiểm tra hiểu:** Nếu Dockerfile của anh viết `COPY . .` (copy toàn bộ source) ngay dòng đầu, trước cả bước tải dependency Maven, điều gì xảy ra mỗi lần chỉ sửa 1 dòng code rồi build lại? *(Gợi ý: layer `COPY . .` bị invalidate → mọi layer sau nó (kể cả tải dependency) đều phải chạy lại từ đầu, dù dependency không hề đổi — build chậm không cần thiết. Đây chính là lý do Dockerfile chuẩn tách `COPY pom.xml` + tải dependency ra riêng, trước khi `COPY` source.)*

## 0.4. Dockerfile — viết từ số 0, rồi soi lại file thật của anh

**Các instruction cốt lõi cần nhớ:**
| Instruction | Tác dụng |
|---|---|
| `FROM <image>` | Chọn base image làm nền |
| `WORKDIR /app` | Đặt thư mục làm việc bên trong container, các lệnh sau chạy tương đối từ đây |
| `COPY <src> <dest>` | Copy file từ máy host vào image |
| `RUN <command>` | Chạy lệnh **lúc build** (VD: cài dependency, compile) — kết quả được "đóng băng" vào image |
| `ENV KEY=value` | Đặt biến môi trường **cố định trong image**, ai chạy container cũng thấy (khác `ARG`) |
| `ARG KEY=value` | Biến chỉ tồn tại **lúc build**, không có trong image cuối cùng, không truy cập được lúc container chạy |
| `EXPOSE 8080` | Chỉ mang tính khai báo/tài liệu — cổng container **sẽ dùng**, không tự động mở port ra ngoài (phải dùng `-p` lúc `run` mới thật sự mở) |
| `CMD [...]` | Lệnh mặc định chạy khi container start — **có thể bị override** khi `docker run image <lệnh khác>` |
| `ENTRYPOINT [...]` | Lệnh chính chạy khi container start — **khó override** hơn, thường dùng khi muốn container luôn chạy đúng 1 chương trình cố định |

**Việc cần làm:** Mở lại `Dockerfile` thật của project, đọc từng dòng, tự hỏi: dòng nào thuộc stage builder, dòng nào thuộc stage runtime, `COPY --from=builder` đang copy cái gì từ đâu sang đâu, thứ tự các dòng đã tối ưu cache đúng theo nguyên tắc 0.3 chưa.

**Khái niệm Multi-stage build (đã có sẵn trong file anh, chỉ cần hiểu lại):** dùng 2 (hoặc nhiều) `FROM` trong 1 Dockerfile. Stage đầu (`builder`) có đầy đủ Maven + JDK để compile code — nặng (~500MB+). Stage cuối chỉ copy **file `.jar` đã build xong** sang một base image nhẹ hơn nhiều (`eclipse-temurin:21-jre-alpine`, chỉ có JRE để *chạy*, không có Maven/JDK để *build*). Image cuối cùng đẩy lên server nhỏ hơn nhiều lần.

```
 STAGE 1 "builder" (nặng, chỉ tồn tại lúc build)
 ┌──────────────────────────────┐
 │ FROM maven:3.9-eclipse-temurin│
 │  COPY pom.xml + source        │
 │  RUN mvn package  →  app.jar  │
 └───────────────┬────────────────┘
                 │ COPY --from=builder (chỉ lấy app.jar)
                 ▼
 STAGE 2 "runtime" (nhẹ, đây mới là image thật deploy)
 ┌──────────────────────────────┐
 │ FROM eclipse-temurin:21-jre-alpine │
 │  COPY app.jar                 │
 │  ENTRYPOINT java -jar app.jar │
 └──────────────────────────────┘
```

**Tự kiểm tra hiểu:** (1) Nếu bỏ multi-stage, chỉ dùng 1 `FROM maven:...` từ đầu đến cuối, điều gì tệ đi? (2) `ENV` và `ARG` khác nhau chỗ nào, JWT secret nên dùng cái nào? *(Gợi ý 1: image nặng, lộ source/Maven cache. Gợi ý 2: secret KHÔNG nên nằm trong `ENV`/`ARG` của Dockerfile — cả 2 đều bị "đóng băng" vào image hoặc build history, ai có image là xem được; secret phải truyền lúc `docker run -e` hoặc qua Render Environment Variables, không viết cứng trong Dockerfile.)*

## 0.5. Docker Compose — viết từ số 0, rồi soi lại file thật của anh

**Cấu trúc YAML cốt lõi:**

```yaml
services:
  app: # tên service, dùng làm hostname để service khác gọi tới
    build: ../../../../../../../..                    # build từ Dockerfile ở thư mục hiện tại
    ports:
      - "8080:8080"             # map port máy_host:port_container
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      db:
        condition: service_healthy   # chờ db khoẻ mới start app
  db:
    image: postgres:16
    environment:
      - POSTGRES_PASSWORD=secret
    volumes:
      - pgdata:/var/lib/postgresql/data   # data sống ngoài container, không mất khi container xoá
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres" ]
      interval: 5s
      retries: 5
volumes:
  pgdata:                        # khai báo named volume, Docker tự quản lý nơi lưu thật trên máy host
```

**Khái niệm cốt lõi — Networking & Healthcheck:** Mỗi service trong `docker-compose.yml` chạy trong network riêng do Compose tự tạo — các container gọi nhau bằng **tên service** (VD: Spring Boot gọi Postgres qua `db:5432` chứ không phải `localhost:5432`, vì `localhost` bên trong container là chính nó). `depends_on: condition: service_healthy` đảm bảo Spring Boot chỉ khởi động **sau khi** Postgres thật sự sẵn sàng nhận connection, không chỉ là container đã "start" (start ≠ ready — Postgres cần vài giây để init).

```
 docker compose up
        │
        ▼
   [db] Postgres start ──► healthcheck pg_isready?
        │                        │ chưa OK → chờ, retry
        │                        │ OK
        ▼                        ▼
   [redis] start            depends_on: service_healthy
        │                        │
        └────────────┬───────────┘
                      ▼
              [app] Spring Boot start
              (giờ mới chắc chắn DB/Redis sẵn sàng)
```

**Việc cần làm:** Mở lại `docker-compose.yml` thật, đối chiếu từng phần với cấu trúc mẫu trên — xác định đâu là network name ngầm định, `healthcheck` của Postgres đang test bằng lệnh gì, volume nào đang giữ data Postgres sống qua các lần restart.

**Tự kiểm tra hiểu:** (1) Nếu bỏ `healthcheck` + `depends_on: condition: service_healthy`, chỉ để `depends_on: db` (không kèm điều kiện), điều gì có thể xảy ra khi `docker compose up` lần đầu trên máy chậm? (2) Volume và Bind Mount khác nhau chỗ nào? *(Gợi ý 1: Spring Boot start trước khi Postgres sẵn sàng → connection refused → app crash. Gợi ý 2: named volume do Docker tự quản lý vị trí lưu trên host, portable hơn; bind mount trỏ thẳng tới 1 đường dẫn cụ thể trên máy host do mình chỉ định — hay dùng khi dev cần hot-reload code.)*

## 0.6. Mở lại `.github/workflows/ci.yml` — hiểu CI đang làm gì

**Khái niệm cốt lõi:** CI (Continuous Integration) hiện tại của anh = mỗi lần push/PR vào `main`, GitHub tự dựng 1 máy ảo sạch, chạy `mvn clean compile` + test. Mục đích: phát hiện lỗi *trước khi* merge, không phải để deploy — CI chỉ "kiểm tra", CD (Continuous Deployment) mới là "đẩy lên server". Anh hiện có CI, chưa có CD — Giai đoạn 4 sẽ thêm phần CD.

**Tự kiểm tra hiểu:** Nếu 1 người bạn khác trong team push code lỗi (không compile được) thẳng vào `main` mà không qua PR, CI hiện tại có ngăn được việc đó merge vào không? *(Gợi ý: không — CI chỉ *báo đỏ*, việc *chặn merge* cần thêm Branch Protection Rule trên GitHub, sẽ nói ở 4C.)*

## 0.7. Bài tập thực hành bắt buộc — gõ tay, không copy-paste

**Việc cần làm (đừng bỏ qua, đây mới là phần biến "biết" thành "thành thạo"):**
1. `docker build -t mini-ecommerce:test .` — tự build image từ Dockerfile thật của project, đo thời gian.
2. Sửa 1 dòng comment vô hại trong code, build lại lần 2 — quan sát layer nào "CACHED", layer nào chạy lại (đúng lý thuyết 0.3).
3. `docker compose up -d` toàn bộ project, `docker compose ps` xem service nào healthy.
4. `docker exec -it <tên container app> sh` chui vào container đang chạy, gõ `env` xem biến môi trường thật sự được inject vào, `exit` để ra.
5. `docker compose logs -f app` xem log real-time, thử gọi 1 API bằng Postman, quan sát log xuất hiện ngay.
6. `docker compose down`, `docker system prune` dọn sạch, build lại từ đầu 1 lần nữa để chắc chắn không phụ thuộc "may mắn còn cache".

**✅ Output Giai đoạn 0:** Tự giải thích được toàn bộ khái niệm 0.0-0.6 bằng lời, VÀ tự gõ tay được các lệnh ở 0.2/0.7 mà không cần mở lại bảng tra — đây là tiêu chuẩn "thành thạo" thật, không phải chỉ đọc hiểu.

---

# GIAI ĐOẠN 1 — Dựng hạ tầng Production

*Local anh đang chạy Postgres/Redis/MinIO bằng Docker trên máy. Production cần 3 dịch vụ này chạy ở đâu đó luôn online, không phải trên máy cá nhân.*

**Sơ đồ: Local (Docker trên máy) → Production (dịch vụ managed trên Cloud)**
```
 LOCAL (docker-compose, chỉ chạy khi anh mở máy)
 ┌─────────┐  ┌─────────┐  ┌─────────┐
 │ Postgres│  │  Redis  │  │  MinIO  │
 └─────────┘  └─────────┘  └─────────┘

                    │  đổi sang
                    ▼

 PRODUCTION (luôn online, không phụ thuộc máy cá nhân)
 ┌───────────────┐ ┌────────────────┐ ┌──────────────┐ ┌────────────┐
 │ Render Postgres│ │ Render Key Value│ │ Cloudflare R2│ │ Brevo (mail)│
 └───────────────┘ └────────────────┘ └──────────────┘ └────────────┘
        (1.1)            (1.2)             (1.3)            (1.4)
```

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| Render Postgres | `render postgresql free tier setup` | https://render.com/docs/databases |
| Render Key Value (Redis) | `render key value redis setup` | https://render.com/docs/key-value |
| Cloudflare R2 S3-compatible | `cloudflare r2 s3 api compatibility` | https://developers.cloudflare.com/r2/api/s3/api/ |
| Brevo Email API | `brevo transactional email api java` | https://developers.brevo.com/reference/sendtransacemail |

**🔴 Lỗi thường gặp ở Giai đoạn này:**
| Lỗi (từ khoá để nhớ) | Nguyên nhân thường gặp |
|---|---|
| Connect DB local (DBeaver) bị timeout | Dùng nhầm Internal URL thay vì External URL (Internal chỉ hoạt động trong network Render) |
| R2 trả `403 Forbidden` khi upload | API Token chưa cấp đúng quyền Object Read & Write cho đúng bucket |
| Brevo trả `401 Unauthorized` | Chưa xác thực Single Sender, hoặc dùng nhầm SMTP key thay vì API key |

## 1.1. Postgres — Render Managed Database (Free)

**Lưu ý quan trọng đã xác nhận:** Free Postgres trên Render giới hạn ~1GB, **hết hạn và bị xoá sau 30 ngày** không có thời gian ân hạn. Với dự án demo/portfolio thì chấp nhận được, nhưng **nhớ ghi lịch** — tới ngày 25-28 phải tạo DB mới + chạy lại Flyway migration, hoặc nâng cấp trả phí nếu cần ổn định lâu dài hơn cho phỏng vấn.

**Việc cần làm:**
1. Tạo tài khoản Render (dùng GitHub login cho tiện, không cần thẻ tín dụng ở free tier).
2. Tạo **New PostgreSQL** trên dashboard → đặt tên, chọn region gần nhất (Singapore nếu có, giảm latency).
3. Lưu lại **Internal Database URL** (dùng khi Web Service và DB cùng ở Render, nhanh hơn) và **External Database URL** (dùng để test connect từ máy local qua DBeaver, kiểm tra DB sống).

**Tự kiểm tra hiểu:** Vì sao nên dùng Internal Database URL cho Web Service thay vì External? *(Gợi ý: Internal URL đi qua network nội bộ của Render, không qua Internet public → nhanh hơn, và không tính vào băng thông public.)*

## 1.2. Redis — Render Key Value (Free)

**Lưu ý:** Free tier chỉ 25MB — đủ cho cache Product/Category TTL 30 phút của anh vì data không nhiều, không cần lo.

**Việc cần làm:** Tạo **New Key Value** trên Render dashboard, lưu lại connection URL.

## 1.3. Object Storage — Cloudflare R2 (thay thế MinIO ở production)

**Khái niệm cốt lõi:** R2 là dịch vụ **S3-compatible** — nghĩa là cùng một bộ API (`PutObject`, `GetObject`, presigned URL...) mà MinIO SDK của anh đang dùng. Vì code anh viết theo chuẩn S3 API (không viết riêng cho MinIO), nên **không cần sửa `StorageService`/`FileValidationUtil`** — chỉ đổi 4 biến môi trường: endpoint, access key, secret key, bucket name.

**Việc cần làm:**
1. Tạo tài khoản Cloudflare (free), vào mục **R2 Object Storage**.
2. Tạo 1 bucket (VD: `mini-ecommerce-storage`).
3. Tạo **R2 API Token** (Account API Token, quyền Read & Write cho bucket này) → nhận được `Access Key ID`, `Secret Access Key`, và `Endpoint URL` (dạng `https://<account_id>.r2.cloudflarestorage.com`).
4. Ghi lại toàn bộ, sẽ nhập vào Environment Variables ở Giai đoạn 2.

**Tự kiểm tra hiểu:** Nếu sau này muốn đổi từ R2 sang AWS S3 thật, cần sửa bao nhiêu dòng code Java? *(Gợi ý: gần như 0 dòng logic — chỉ đổi lại 4 biến môi trường, đây chính là giá trị của việc code theo chuẩn interface/S3-compatible ngay từ đầu.)*

## 1.4. Email — chuyển từ SMTP sang HTTP API (bắt buộc, vì Rủi ro 1)

**Khái niệm cốt lõi:** SMTP là giao thức mở kết nối TCP trực tiếp tới port 25/465/587 — hạ tầng cloud chặn vì đây là con đường phổ biến để spam bot lạm dụng. HTTP API (như Brevo, SendGrid) gửi email bằng cách gọi `POST` tới 1 endpoint HTTPS qua port 443 — port này không bao giờ bị chặn vì đó chính là port web bình thường. Bản chất vẫn là "gửi email", chỉ đổi *cách vận chuyển*.

**Việc cần làm:**
1. Tạo tài khoản **Brevo** (free 300 email/ngày, đủ dư cho demo) → xác thực 1 email gửi đi (Single Sender Verification).
2. Lấy **API Key** từ Brevo dashboard.
3. Sửa `EmailSenderUtil`: thay vì dùng `JavaMailSender.send(MimeMessage)`, gọi `RestTemplate`/`WebClient` `POST` tới endpoint API của Brevo (`https://api.brevo.com/v3/smtp/email`) với JSON body chứa `sender`, `to`, `htmlContent`. Interface `EmailService`/`EmailServiceImpl` giữ nguyên — chỉ đổi bên trong `EmailSenderUtil` là implementation detail, đúng tinh thần tách lớp anh đã làm.
4. Giữ code JavaMailSender cũ lại cho profile `dev` (local vẫn dùng SMTP bình thường, không cần đổi gì) — dùng `@Profile` hoặc if-else theo `SPRING_PROFILES_ACTIVE` để chọn implementation, hoặc đơn giản nhất: 2 bean khác nhau tuỳ profile.

**Tự kiểm tra hiểu:** Vì sao đây là ví dụ tốt để nói trong phỏng vấn về "thiết kế chống thay đổi hạ tầng"? *(Gợi ý: vì `EmailService` là interface, `ProductService`/`OrderService` gọi qua interface đó — khi đổi từ SMTP sang HTTP API, không có class nghiệp vụ nào khác trong hệ thống bị ảnh hưởng, đúng nguyên lý Dependency Inversion.)*

**✅ Output Giai đoạn 1:** Có trong tay: Postgres URL, Redis URL, R2 Access Key + Secret + Endpoint + Bucket name, Brevo API Key. Tất cả ghi vào 1 file tạm (KHÔNG commit vào Git) để dùng ở bước sau.

---

# GIAI ĐOẠN 2 — Chuẩn bị code cho Production

**Sơ đồ: Config chảy từ đâu tới đâu khi container chạy trên Render**
```
 Render Dashboard → Environment Variables
        │  (Render inject vào container lúc start, không phải lúc build)
        ▼
 application.yaml  đọc  ${SPRING_DATASOURCE_URL}, ${JWT_SECRET_KEY}, ...
        │
        ▼
 Spring Boot ApplicationContext khởi tạo Bean với giá trị thật
        │
        ▼
 App chạy đúng với Postgres/Redis/R2/Brevo production
```

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| Spring Profiles | `spring boot profiles application-{profile}.yaml` | https://docs.spring.io/spring-boot/reference/features/profiles.html |
| Externalized Config | `spring boot externalized configuration environment variables` | https://docs.spring.io/spring-boot/reference/features/external-config.html |
| Flyway auto-migrate | `flyway spring boot auto migrate on startup` | https://docs.spring.io/spring-boot/how-to/data-initialization.html |
| Spring Security @PreAuthorize | `spring security method security preauthorize` | https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html |

**🔴 Lỗi thường gặp ở Giai đoạn này:**
| Lỗi (từ khoá để nhớ) | Nguyên nhân thường gặp |
|---|---|
| App chạy local bị lỗi sau khi thêm `${VAR}` | Chưa set default value `${VAR:defaultValue}` — local thiếu biến môi trường đó |
| CORS lỗi `blocked by CORS policy` khi test | Domain gọi vào chưa nằm trong danh sách allowed-origin |
| Flyway báo `checksum mismatch` | Có ai đó sửa tay file migration cũ đã chạy rồi — không bao giờ sửa migration đã apply, chỉ tạo migration mới |

## 2A. Spring Profiles — tách config `dev` và `prod`

**Khái niệm cốt lõi:** Spring Profiles cho phép có nhiều bộ config (`application-dev.yaml`, `application-prod.yaml`) và chọn bộ nào chạy bằng biến môi trường `SPRING_PROFILES_ACTIVE`. Production **không** dùng file `.env` local nữa — mọi secret đọc thẳng từ Environment Variables mà Render inject vào container lúc chạy.

**Việc cần làm:** Tạo `application-prod.yaml` (hoặc dùng chung 1 file với placeholder `${VAR_NAME}` — cách nào cũng được, tuỳ anh đã tổ chức config thế nào). Đảm bảo mọi giá trị nhạy cảm (DB URL, JWT secret, R2 keys, mail password, VNPay keys) đều là `${TEN_BIEN_MOI_TRUONG}`, không hardcode.

**Tự kiểm tra hiểu:** Vì sao không thể chỉ dùng đúng 1 file `application.yaml` với giá trị cố định rồi build 2 image khác nhau cho dev/prod? *(Gợi ý: được, nhưng tốn công build lại image mỗi lần đổi môi trường; dùng biến môi trường cho phép **cùng 1 image** chạy ở nhiều môi trường khác nhau chỉ bằng cách đổi config lúc chạy — đúng triết lý container "build once, run anywhere".)*

## 2B. Flyway — xác nhận chạy migration tự động khi khởi động

**Việc cần làm:** Kiểm tra `spring.flyway.enabled=true` đang bật, và test tâm lý: lần đầu container chạy trên Render, Flyway sẽ tự chạy hết `V1__...` đến `V8__...` lên DB Render trống — không cần anh chạy tay.

## 2C. CORS — cho phép domain thật gọi vào (chuẩn bị trước cho Giai đoạn 9 - FE)

**Việc cần làm:** Đưa CORS allowed-origin ra biến môi trường thay vì hardcode `localhost:8085` (đúng mục TODO #7 anh đã ghi trong `PROJECT_OVERVIEW.md`). Hiện tại có thể tạm để `*` hoặc domain Render sinh ra, sau này khi có FE thật thì đổi lại — không cần rebuild code, chỉ đổi biến môi trường.

## 2D. Bảo mật `TestUploadController` — BẮT BUỘC trước khi public

**Vì sao bắt buộc:** Đây là endpoint test upload không có kiểm soát nghiêm ngặt — public ra Internet mà không khoá là lỗ hổng thật (ai cũng upload file được vào bucket của anh). 2 lựa chọn nhanh:
- Xoá hẳn class này khỏi codebase (nếu không còn cần).
- Hoặc thêm `@PreAuthorize("hasRole('ADMIN')")` + đảm bảo Security Filter Chain áp dụng đúng cho path này.

**Tự kiểm tra hiểu:** Vì sao 1 endpoint "chỉ để test" lại nguy hiểm hơn khi deploy public so với khi chạy local? *(Gợi ý: local chỉ mình anh truy cập được `localhost`; deploy public thì bất kỳ ai trên Internet cũng gọi được URL đó, kể cả bot quét lỗ hổng tự động.)*

**✅ Output Giai đoạn 2:** Code đã sẵn sàng chạy với config từ biến môi trường, không còn hardcode, không còn endpoint hở.

---

# GIAI ĐOẠN 3 — Deploy lần đầu lên Render

*Đây là bước "thực chiến" — nhiều khả năng sẽ gặp lỗi lần đầu, đó là bình thường, không phải dấu hiệu làm sai.*

**Sơ đồ: Flow deploy lần đầu trên Render**
```
 GitHub repo (main branch, có Dockerfile)
        │  Render "New Web Service" connect
        ▼
 Render đọc Dockerfile → build image (giống hệt Giai đoạn 0 đã hiểu)
        │
        ▼
 Container start → đọc PORT + Environment Variables (3B)
        │
        ▼
   ┌──────────┐        ┌──────────────┐
   │ Thành công│───►   │ URL public sống │
   └──────────┘        └──────────────┘
   ┌──────────┐
   │  Fail     │──► đọc Logs → tra bảng lỗi 3C bên dưới
   └──────────┘
```

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| Deploy Docker Web Service | `render deploy docker web service` | https://render.com/docs/deploy-an-app |
| Environment Variables trên Render | `render environment variables secrets` | https://render.com/docs/environment-variables |
| Render Postgres SSL connection | `render postgres sslmode require jdbc` | https://render.com/docs/postgresql-connecting |

## 3A. Tạo Web Service trên Render

**Việc cần làm:**
1. Dashboard Render → **New Web Service** → Connect GitHub repo (`java-mini-project-ecommerce`).
2. Chọn **Runtime: Docker** (Render tự detect `Dockerfile` ở root repo — không cần buildpack).
3. Chọn region, chọn instance type **Free**.

## 3B. Khai báo Environment Variables trên Render Dashboard

**Việc cần làm:** Nhập toàn bộ danh sách đã gom ở Giai đoạn 1 + 2 vào mục **Environment** của Web Service:

| Biến | Giá trị lấy từ |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` / `SPRING_DATASOURCE_URL` | Internal Postgres URL (1.1) |
| `SPRING_DATASOURCE_USERNAME/PASSWORD` | Từ Render Postgres |
| `SPRING_REDIS_HOST/PORT/PASSWORD` | Từ Render Key Value (1.2) |
| `R2_ENDPOINT/ACCESS_KEY/SECRET_KEY/BUCKET` | Từ Cloudflare R2 (1.3) |
| `JWT_SECRET_KEY` | Tạo mới, KHÔNG dùng lại secret local |
| `MAIL_USERNAME/PASSWORD` | SMTP hiện có |
| `VNPAY_*` | Sandbox keys hiện có |
| `CORS_ALLOWED_ORIGINS` | Tạm để domain Render sinh ra |

## 3C. Deploy & Đọc Log khi lỗi

**Việc cần làm:** Bấm **Create Web Service**, Render tự build Docker image từ Dockerfile và deploy. Theo dõi tab **Logs** trực tiếp.

**3 lỗi thường gặp nhất khi mới deploy lần đầu — chuẩn bị tâm lý trước:**

1. **Port binding sai:** Spring Boot mặc định port `8080`, nhưng Render yêu cầu app lắng nghe đúng port mà Render inject qua biến `PORT`. → Sửa `server.port=${PORT:8080}` trong config.
2. **Database connection timeout:** Thường do dùng nhầm External URL thay vì Internal URL, hoặc SSL mode chưa đúng (Render Postgres yêu cầu `sslmode=require` trên connection string).
3. **Build fail vì thiếu `mvnw` permission:** Nếu Dockerfile dùng `./mvnw`, đôi khi thiếu quyền execute — thêm `RUN chmod +x mvnw` vào Dockerfile nếu gặp lỗi `Permission denied`.

**Tự kiểm tra hiểu:** Nếu log báo `Connection refused` tới database ngay khi container vừa start xong, hướng debug đầu tiên nên nghĩ tới là gì? *(Gợi ý: kiểm tra đúng thứ tự — biến môi trường DB URL có đúng không → Internal hay External URL → SSL mode → rồi mới nghi ngờ tới code.)*

## 3D. Giữ service "thức" — xử lý Rủi ro 2 (VNPay IPN cold-start)

**Khái niệm cốt lõi:** Free service ngủ khi không có traffic trong 15 phút. Webhook (IPN) từ VNPay là traffic đến **không đoán trước được thời điểm** — nếu app đang ngủ, VNPay gọi vào đúng lúc app cold-start, tuỳ vào timeout mà VNPay cấu hình, request có thể fail trước khi app kịp trả response.

**Việc cần làm (chọn 1 trong 2, cách 1 đơn giản hơn cho demo):**
- **Cách 1 — Uptime ping:** Dùng dịch vụ free như UptimeRobot hoặc cron-job.org, cấu hình gọi `GET /actuator/health` mỗi 10 phút → giữ service không bao giờ ngủ quá 15 phút. Đơn giản, miễn phí, đủ cho mục đích demo/phỏng vấn.
- **Cách 2 — Chấp nhận giới hạn, ghi rõ trong docs:** Khi demo trực tiếp cho nhà tuyển dụng, chủ động mở app trước vài phút (gọi thử 1 request) để chắc chắn app đã "thức" trước khi test luồng Payment.

**Tự kiểm tra hiểu:** Nếu đây là hệ thống thật (không phải demo), "giữ service thức bằng ping" có phải là giải pháp đúng đắn lâu dài không? *(Gợi ý: không — đó là cách né tránh vấn đề của free tier, không phải kiến trúc chuẩn. Hệ thống thật nên dùng always-on instance trả phí, hoặc thiết kế idempotent + retry ở phía gọi VNPay tra soát (query API) thay vì phụ thuộc hoàn toàn vào IPN một lần. Biết phân biệt "giải pháp cho demo" và "giải pháp cho production thật" chính là điều phỏng vấn hay hỏi.)*

**✅ Output Giai đoạn 3:** Có URL dạng `https://xxx.onrender.com`, gọi `GET /actuator/health` hoặc bất kỳ public endpoint nào trả về response thật (không phải lỗi 502/503), và đã cấu hình uptime ping.

---

# GIAI ĐOẠN 4 — CI/CD thật: tự động deploy khi push code

**Sơ đồ: Toàn bộ pipeline CI/CD sau khi xong Giai đoạn 4**
```
 Dev push code / mở PR vào main
        │
        ▼
 ┌─────────────────────────────────────┐
 │ GitHub Actions (CI)                  │
 │  1. PMD check (4D) — fail nhanh nếu  │
 │     import/biến thừa                 │
 │  2. mvn compile + test               │
 └───────────────┬───────────────────────┘
                  │ tất cả PASS
                  ▼
 Branch Protection (4C) cho phép merge vào main
                  │
                  ▼
 Render Auto-Deploy (4B) phát hiện main có commit mới
                  │
                  ▼
 Build lại Docker image → Deploy → URL production cập nhật
```

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| Render Auto-Deploy | `render auto deploy github` | https://render.com/docs/deploys |
| GitHub Branch Protection | `github branch protection require status checks` | https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches |
| Maven PMD Plugin | `maven pmd plugin configuration` | https://maven.apache.org/plugins/maven-pmd-plugin/ |
| PMD Java Rules | `pmd java ruleset unused imports` | https://docs.pmd-code.org/latest/pmd_rules_java.html |

**🔴 Lỗi thường gặp ở Giai đoạn này:**
| Lỗi (từ khoá để nhớ) | Nguyên nhân thường gặp |
|---|---|
| PMD báo lỗi nhưng không rõ dòng nào | Chạy `./mvnw pmd:pmd` (không phải `pmd:check`) để sinh report HTML chi tiết ở `target/site/pmd.html` |
| Render không tự deploy dù đã push | Auto-Deploy đang tắt, hoặc push nhầm branch không phải branch đã cấu hình |
| Branch Protection chặn cả chính mình merge | Bình thường — cần PR pass CI rồi mới merge được, kể cả chủ repo (đúng ý đồ bảo vệ) |

## 4A. Phân biệt rõ CI đã có vs CD sắp thêm

Nhắc lại từ 0.3: CI hiện tại = test code khi push/PR. CD = sau khi CI pass, **tự động** đẩy code mới lên server production, không cần vào Render bấm tay mỗi lần.

## 4B. Bật Auto-Deploy trên Render (cách đơn giản nhất)

**Việc cần làm:** Trong Web Service Settings → **Auto-Deploy: On**, chọn branch `main`. Từ giờ mỗi lần push/merge vào `main`, Render tự pull code mới, build lại Docker image, deploy — đây chính là CD, không cần viết thêm YAML.

## 4C. Nâng cấp: chỉ auto-deploy khi CI pass (Branch Protection)

**Khái niệm cốt lõi:** Mặc định Render deploy ngay khi có push, kể cả code không compile được (Render tự build lại nên sẽ tự fail ở bước build, nhưng vẫn "cố" deploy). Cách chuẩn hơn: bật **GitHub Branch Protection Rule** cho `main` → yêu cầu "Require status checks to pass before merging" trỏ tới CI workflow hiện có → không ai (kể cả anh) merge được PR nếu CI đỏ → main luôn ở trạng thái deploy được.

**Tự kiểm tra hiểu:** Vì sao "Render tự fail khi build lỗi" chưa đủ tốt bằng "chặn merge từ trước"? *(Gợi ý: nếu lỗi ở bước test logic — chứ không phải lỗi compile — Render vẫn build & deploy "thành công" một phiên bản có bug logic lên production; Branch Protection chặn từ gốc, trước khi merge, không phải chặn ở bước deploy.)*

## 4D. Code Quality Gate — chặn import/biến thừa TRƯỚC khi cho build

**Cần hiểu đúng 1 điều trước tiên:** trình biên dịch Java (`javac`) mặc định **không** báo lỗi hay warning cho import không dùng hoặc biến khai báo rồi bỏ đó — đó là tính năng của IDE (IntelliJ tự gạch xám), không phải của trình biên dịch. Nghĩa là hiện tại, dù IntelliJ có gạch xám cảnh báo trên máy anh, CI trên GitHub Actions **hoàn toàn không biết** những cảnh báo đó tồn tại — code vẫn build xanh bình thường. Đây chính là lý do cần thêm 1 công cụ riêng — không phải `javac` làm được việc này.

**Công cụ dùng:** **PMD** (`maven-pmd-plugin`) — chuyên phát hiện: import không dùng, biến local khai báo rồi không dùng, field private không dùng, catch block rỗng, method quá phức tạp, và nhiều pattern code xấu khác. Chọn PMD vì tích hợp Maven rất gọn, không cần server riêng (khác SonarQube cần host).

**Việc cần làm:**

1. Thêm plugin vào `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.26.0</version>
    <configuration>
        <rulesets>
            <ruleset>category/java/bestpractices.xml</ruleset>
            <ruleset>category/java/codestyle.xml</ruleset>
        </rulesets>
        <failOnViolation>true</failOnViolation>
        <printFailingErrors>true</printFailingErrors>
    </configuration>
</plugin>
```
   *(Ruleset `bestpractices.xml` chứa `UnusedLocalVariable`, `UnusedPrivateField`, `UnusedPrivateMethod`; ruleset `codestyle.xml` chứa `UnusedImports`.)*

2. Test local trước: chạy `./mvnw pmd:check` — lần đầu chạy chắc chắn sẽ báo khá nhiều violation (vì code cũ chưa từng bị soi qua công cụ này). Đừng hoảng — đọc từng lỗi, sửa dần, đây cũng là dịp dọn lại code cũ.

3. Thêm bước vào `.github/workflows/ci.yml`, đặt **trước** bước test (fail nhanh, đỡ tốn thời gian chạy test nếu code đã bẩn):
```yaml
- name: Check code quality (PMD)
  run: ./mvnw pmd:check
```

**Tự kiểm tra hiểu:** Vì sao nên đặt bước PMD check *trước* bước chạy test, thay vì sau? *(Gợi ý: nguyên tắc "fail fast" — PMD check chạy trong vài giây, test suite có thể chạy vài phút; nếu code có lỗi cơ bản như import thừa, không cần tốn thời gian chờ hết test mới biết fail, biết sớm để sửa sớm.)*

**Lưu ý quan trọng:** Đây là "cổng" tự động — nhưng thói quen tốt nhất vẫn là chạy `./mvnw pmd:check` **trên máy local trước khi push**, không đợi CI báo đỏ mới biết. CI là lưới an toàn cuối cùng, không phải bước kiểm tra đầu tiên.

**✅ Output Giai đoạn 4:** Push code mới vào `main` → tự thấy Render redeploy trong vài phút, không cần thao tác tay, VÀ code không đạt chuẩn (import/biến thừa) sẽ bị chặn ngay ở bước PMD check, không lọt được tới bước build/deploy.



---

# GIAI ĐOẠN 5 — Smoke Test + Cập nhật Doc

**Sơ đồ: Luồng Smoke Test theo đúng thứ tự nghiệp vụ**
```
 Register → Login (nhận JWT)
     │
     ▼
 GET /products (lần 1: MISS cache) → GET /products (lần 2: HIT cache, nhanh hơn)
     │
     ▼
 Upload ảnh sản phẩm → ảnh lưu R2 → GET /products/{id} thấy imageUrl thật
     │
     ▼
 Tạo Order → stock giảm đúng (kiểm tra @Version còn hoạt động trên DB thật)
     │
     ▼
 Cancel Order → stock hoàn lại đúng
     │
     ▼
 Nếu tất cả PASS → cập nhật README (5B) → xong
```

## 5A. Smoke Test — kiểm tra nhanh các luồng chính trên URL thật

**Việc cần làm (dùng Postman/Swagger UI public):**
- [ ] Register → Login → nhận JWT
- [ ] Gọi `GET /products` (có cache Redis) — gọi lần 2 xem có nhanh hơn không (dấu hiệu cache HIT)
- [ ] Tạo Order → xem stock trừ đúng (Optimistic Locking còn hoạt động đúng trên DB production)
- [ ] Upload ảnh sản phẩm → xác nhận ảnh lưu vào R2 thật (mở link ảnh trả về, thấy hiển thị được)
- [ ] Đổi trạng thái Order → Cancel → xác nhận hoàn stock

## 5B. Cập nhật README với link demo

**Việc cần làm:** Thêm vào đầu README: badge trạng thái CI, link URL demo, link Swagger UI public, lưu ý "free tier có thể cold-start 30-60s ở request đầu tiên sau thời gian không dùng" (để tránh recruiter tưởng app bị lỗi khi load chậm lần đầu).

**✅ Output cuối cùng:** 1 URL sống, public, đầy đủ luồng chạy được, doc rõ ràng, và anh giải thích được toàn bộ pipeline CI/CD + kiến trúc production khi được hỏi trong phỏng vấn.

---

---

# GIAI ĐOẠN 6 — Đào sâu để THÀNH THẠO thật (Track A — không gấp, không nhét vào 1-2 ngày)

*Đây là câu trả lời thẳng cho phần thứ 2 anh hỏi. Nói thật: Giai đoạn 0→5 ở trên cho anh **1 lần trải nghiệm deploy thành công** — đủ để có URL, đủ để kể một câu chuyện trong CV. Nhưng "thành thạo" theo đúng nghĩa nhà tuyển dụng kỳ vọng — tự tin trả lời khi bị hỏi xoáy, tự debug được tình huống lạ chưa từng gặp — **không đến từ 1 lần làm theo checklist**, mà từ việc hiểu *tại sao* đủ sâu để tự suy luận ra tình huống mới. Cái đó cần thời gian, đúng tinh thần Track A anh đã tự đặt ra trước đây (không deadline, tự giải thích được mới qua bước tiếp). Đừng cố nhồi phần này vào cùng 1-2 ngày với Giai đoạn 0-5 — sẽ chỉ thuộc lòng thao tác, không phải hiểu.*

**Sơ đồ: Pipeline CI/CD "chuẩn" (sau khi hoàn thành 6B) so với hiện tại**
```
 HIỆN TẠI (sau Giai đoạn 4):
 GitHub → CI test/PMD → Render tự build lại từ source → Deploy

 SAU KHI LÀM 6B (chuẩn build-once-run-anywhere):
 GitHub → CI test/PMD → Build Docker image → Push lên GHCR (artifact CỐ ĐỊNH)
                                                   │
                                                   ▼
                                    Render pull đúng image đó → Deploy
                          (image chạy ở Render y hệt image đã test ở CI,
                           không build lại lần nữa → đúng nghĩa "artifact bất biến")
```

**📎 Tài liệu tham khảo & Keyword tra cứu nhanh:**
| Chủ đề | Keyword tra cứu | Doc chính thức |
|---|---|---|
| GitHub Container Registry | `github actions build push ghcr.io docker` | https://docs.github.com/en/actions/publishing-packages/publishing-docker-images |
| Docker networking sâu | `docker bridge vs host network mode` | https://docs.docker.com/engine/network/ |
| Docker resource limits | `docker compose deploy resources limits` | https://docs.docker.com/compose/compose-file/deploy/ |
| CI vs CD khái niệm | `continuous integration vs continuous delivery vs deployment` | https://docs.github.com/en/actions/about-github-actions/about-continuous-integration |

## 6A. Docker — các khái niệm CV ghi "biết Docker" cần trả lời được

| Chủ đề | Câu hỏi phỏng vấn điển hình |
|---|---|
| Image Layers & Caching | "Vì sao thứ tự các dòng `COPY`/`RUN` trong Dockerfile lại ảnh hưởng tốc độ build?" |
| `CMD` vs `ENTRYPOINT` | "2 cái này khác nhau thế nào, khi nào dùng cái nào?" |
| Volume vs Bind Mount | "Data trong Postgres container mất khi nào, làm sao tránh?" |
| Networking mode (bridge/host) | "Container gọi nhau bằng gì nếu không cùng docker-compose?" |
| Resource limits (`--memory`, `--cpus`) | "Nếu 1 container ăn hết RAM server, container khác bị ảnh hưởng thế nào, làm sao ngăn?" |
| Image size optimization | "Ngoài multi-stage, còn cách nào giảm size image? (Alpine base, xoá cache Maven trong cùng layer, .dockerignore)" |

**Cách luyện:** Không đọc lý thuyết suông — với mỗi câu, tự vào project hiện tại, thử nghiệm thật (VD: đổi thứ tự COPY rồi đo lại thời gian build 2 lần, xem cache có hit không) rồi tự viết lại câu trả lời bằng lời của mình.

## 6B. CI/CD — khoảng trống thật trong pipeline hiện tại của anh

**Sự thật cần biết:** CI hiện tại của anh (`ci.yml`) chỉ `mvn compile` + test — **chưa hề build và push Docker image lên registry nào cả**. Khi Render deploy, Render tự build lại image từ source, không dùng image mà CI đã kiểm tra. Đây là khoảng trống thật, không phải lỗi — nhưng nếu bị hỏi phỏng vấn "pipeline CI/CD của bạn build image ở đâu, push lên đâu, Render pull từ đâu?", câu trả lời hiện tại sẽ là "Render tự build lại" — chưa phải pipeline chuẩn CI/CD đúng nghĩa (build once, deploy same artifact everywhere).

**Việc nên làm khi có thời gian (không gấp):**
- Thêm bước vào `ci.yml`: build Docker image, push lên **GitHub Container Registry (GHCR)** — free, tích hợp sẵn với GitHub Actions, không cần thêm tài khoản.
- Đổi Render Web Service từ "build từ Dockerfile" sang "deploy từ image có sẵn trên GHCR" — lúc này CI thật sự là nơi tạo ra artifact, CD chỉ là "lấy đúng artifact đó chạy" — đúng chuẩn "build once, run anywhere" đã nói ở 2A.

**Câu hỏi phỏng vấn điển hình khác cần chuẩn bị:**
- "Continuous Integration, Continuous Delivery, Continuous Deployment khác nhau thế nào?" (Delivery = sẵn sàng deploy nhưng cần người bấm tay; Deployment = tự động hoàn toàn — pipeline của anh sau khi làm xong Giai đoạn 4 là Deployment.)
- "Secrets trong GitHub Actions được quản lý thế nào, có bị lộ trong log không?" (GitHub Secrets, tự động mask trong log.)
- "Nếu deploy lên production bị lỗi, rollback thế nào?" (Render giữ lịch sử deploy, có thể rollback về version trước qua dashboard hoặc redeploy commit cũ — nên tự thử 1 lần để biết thao tác thật, không chỉ biết lý thuyết.)

**✅ Output Giai đoạn 6:** Không có deadline — làm dần trong các tuần tiếp theo, mỗi lần 1-2 chủ đề, luôn kèm "tự làm thử trên chính project" trước khi coi là hiểu.

---

# 📚 PHỤ LỤC — Bảng tra cứu nhanh toàn bộ (gộp lại từ mọi Giai đoạn)

*Mục đích: khi gặp lỗi hoặc cần nhớ lại 1 tính năng, quét bảng này trước khi phải đọc lại từ đầu.*

## Tra theo LỖI (Lỗi → Giai đoạn → Nguyên nhân)

| Lỗi gặp phải | Ở Giai đoạn | Nguyên nhân / hướng fix nhanh |
|---|---|---|
| `port is already allocated` | 0 | Container/app khác đang chiếm port, `docker ps` để kiểm tra |
| `no space left on device` | 0 | `docker system prune` dọn image/container cũ |
| Container "Exited (1)" | 0 | `docker logs <container>` — thường thiếu biến môi trường |
| DBeaver connect timeout | 1 | Nhầm Internal URL thay vì External URL |
| R2 `403 Forbidden` | 1 | API Token thiếu quyền Read/Write đúng bucket |
| Brevo `401 Unauthorized` | 1 | Chưa xác thực Single Sender / nhầm SMTP key với API key |
| App local lỗi sau khi thêm `${VAR}` | 2 | Thiếu default value `${VAR:default}` |
| `blocked by CORS policy` | 2 | Domain gọi vào chưa nằm trong allowed-origin |
| Flyway `checksum mismatch` | 2 | Đã sửa tay migration cũ — không bao giờ sửa, chỉ tạo migration mới |
| App không bind đúng port trên Render | 3 | Thiếu `server.port=${PORT:8080}` |
| `Connection refused` tới DB khi vừa start | 3 | Sai Internal/External URL, hoặc thiếu `sslmode=require` |
| Build fail `Permission denied` với `mvnw` | 3 | Thiếu `RUN chmod +x mvnw` trong Dockerfile |
| Email không gửi được dù local OK | 3 (Rủi ro 1) | Render free chặn SMTP port 25/465/587 — phải dùng Brevo HTTP API (1.4) |
| VNPay không nhận được callback | 3 (Rủi ro 2) | Service ngủ do cold-start — cấu hình uptime ping (3D) |
| PMD báo lỗi không rõ dòng nào | 4 | Chạy `./mvnw pmd:pmd` để có report HTML chi tiết |
| Render không tự deploy dù đã push | 4 | Auto-Deploy đang tắt hoặc push nhầm branch |

## Tra theo TÍNH NĂNG (Feature → Doc/Code liên quan)

| Tính năng | Nằm ở Giai đoạn | Doc/Code cần nhớ |
|---|---|---|
| Docker Daemon & Docker Desktop | 0.0 | Icon cá voi phải chạy trước khi gõ lệnh `docker` |
| Cài đặt & lệnh Docker hàng ngày | 0.1-0.2 | `docker build/run/ps/logs/exec/compose...` |
| Image Layer & Cache | 0.3 | Thứ tự `COPY`/`RUN` ảnh hưởng tốc độ build |
| Multi-stage Docker build | 0.4 | `Dockerfile` — 2 stage builder/runtime |
| Compose healthcheck & thứ tự start | 0.5 | `docker-compose.yml` — `depends_on: condition: service_healthy` |
| CI compile + test | 0.6 | `.github/workflows/ci.yml` |
| DB production | 1.1 | Render PostgreSQL — nhớ hạn 30 ngày |
| Cache production | 1.2 | Render Key Value 25MB |
| Object Storage production | 1.3 | Cloudflare R2, S3-compatible, đổi env không đổi code |
| Email production | 1.4 | Brevo HTTP API thay `JavaMailSender`, sửa `EmailSenderUtil` |
| Config theo môi trường | 2A | Spring Profiles `SPRING_PROFILES_ACTIVE` |
| Auto-migrate DB | 2B | Flyway `V1__` → `V8__` |
| CORS domain thật | 2C | Biến môi trường `CORS_ALLOWED_ORIGINS` |
| Khoá endpoint test | 2D | `@PreAuthorize` hoặc xoá `TestUploadController` |
| Deploy Docker lên Render | 3A-3C | Render Web Service, Runtime: Docker |
| Giữ service thức | 3D | UptimeRobot/cron-job.org ping `/actuator/health` |
| Auto-deploy khi push | 4B | Render Auto-Deploy: On |
| Chặn merge code lỗi | 4C | GitHub Branch Protection Rule |
| Chặn code thừa (import/biến) | 4D | `maven-pmd-plugin`, `./mvnw pmd:check` |
| Build image cố định + đẩy registry | 6B | GitHub Container Registry (GHCR) |

---

## 📌 Việc nên làm NGAY trong hôm nay (nếu bắt đầu từ đầu)

1. Giai đoạn 0 (30-45 phút) — ôn lại, không được bỏ qua dù đã có sẵn, vì đây là phần giúp anh *trả lời được phỏng vấn*, không chỉ *có URL chạy*.
2. Giai đoạn 1 — tạo 3 tài khoản/dịch vụ, gom đủ credentials.
3. Giai đoạn 2 — sửa code, commit, push (CI hiện có sẽ tự chạy, xác nhận vẫn xanh).

Nếu vướng ở bước nào cụ thể, quay lại nói đúng mã (VD: "tôi đang vướng ở 1.3", "tôi đang vướng ở 3C lỗi port binding") để đi sâu đúng chỗ, không cần lặp lại toàn bộ context.
