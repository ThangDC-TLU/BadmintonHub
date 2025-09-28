# 🏸 BadmintonHub E-Commerce Microservices

BadmintonHub là dự án học tập xây dựng nền tảng thương mại điện tử bán đồ cầu lông, ứng dụng kiến trúc **microservices** hiện đại để tối ưu hóa hiệu năng, khả năng mở rộng và trải nghiệm vận hành thực tế.

---

## 🚀 Tổng quan kiến trúc & công nghệ nổi bật

- **Kiến trúc Microservices:**  
  Hệ thống chia thành nhiều service độc lập (Auth, Product, Cart, Order,...), giao tiếp qua API Gateway, dễ mở rộng và bảo trì.

- **Redis lưu trữ giỏ hàng:**  
  Giỏ hàng của từng người dùng được lưu trên Redis – giúp thao tác thêm/xóa/sửa nhanh chóng, giảm tải cho database truyền thống, hỗ trợ trải nghiệm realtime.

- **Kafka cho truyền sự kiện:**  
  Sử dụng Apache Kafka để truyền event giữa các service (ví dụ: đặt hàng, thanh toán) theo mô hình pub/sub, tăng khả năng mở rộng và đảm bảo tính nhất quán dữ liệu.

- **Swagger tập trung tại GatewayService:**  
  Toàn bộ tài liệu API và giao diện test được tập trung tại gatewayService, đảm bảo kiểm thử đúng luồng nghiệp vụ tổng thể của hệ thống, thuận tiện cho phát triển và kiểm thử.

- **Thanh toán Paypal:**  
  Tích hợp cổng thanh toán quốc tế Paypal, tăng độ tin cậy và mở rộng đối tượng khách hàng.

- **Giám sát hệ thống với OpenTelemetry + SigNoz:**  
  Thu thập trace, metrics và logs từ các microservices, gửi về SigNoz để giám sát hiệu năng, phát hiện lỗi, chủ động vận hành.

- **Circuit Breaker & Load Balancer:**  
  Sử dụng Resilience4j để tự động bảo vệ khi một service lỗi, đồng thời cân bằng tải qua Eureka giúp hệ thống luôn sẵn sàng phục vụ.

- **Refresh Config:**  
  Tích hợp Spring Cloud Config Server, cho phép thay đổi cấu hình động mà không cần restart service, thuận tiện cho vận hành thực tế.

---

## 📚 Stack công nghệ

- **Backend:** Java (Spring Boot, Spring Cloud)
- **Template:** FreeMarker
- **Cache:** Redis
- **Messaging/Event:** Apache Kafka
- **Tracing & Monitoring:** OpenTelemetry + SigNoz
- **API Docs & Test:** Swagger UI tại GatewayService
- **Payment:** Paypal SDK
- **Config Server:** Spring Cloud Config
- **Circuit Breaker & Load Balancer:** Resilience4j, Eureka

---

## 🛠️ Hướng dẫn cài đặt & chạy thử

```bash
# 1. Clone mã nguồn
git clone https://github.com/ThangDC-TLU/BadmintonHub.git

# 2. Khởi động các service phụ trợ (Redis, Kafka, SigNoz, Config Server...)

# 3. Clone các file config
https://github.com/ThangDC-TLU/badmintonHub-config.git

# 4. Build & chạy các microservice
./mvnw spring-boot:run

# 5. Truy cập Swagger UI tại GatewayService để test toàn bộ các luồng API
http://localhost:{gateway-port}/swagger-ui.html
```

## 🙋‍♂️ Tác giả

- **ThangDC-TLU**
- [LinkedIn](#) | [GitHub](https://github.com/ThangDC-TLU)

---
