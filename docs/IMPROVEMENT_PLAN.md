# Kế hoạch cải thiện — StayEase Hotel Booking

Tài liệu này chia các vấn đề phát hiện khi review repo thành từng phase. Mỗi phase là một commit
(hoặc nhóm commit) độc lập, build + test phải xanh sau mỗi phase.

**Nguyên tắc chung**

- Backend là *nguồn chuẩn* cho hợp đồng API (URL, quyền truy cập đã khai báo trong `SecurityConfig`,
  Swagger). Frontend được sửa cho khớp backend, không đổi URL backend.
- Không đổi schema theo cách phá dữ liệu cũ (vẫn dùng `ddl-auto: update`; chỉ thêm, không xoá cột).
- Mỗi lỗi logic được sửa phải có test tái hiện.

**Baseline (trước khi sửa)**

| Kiểm tra | Kết quả |
|---|---|
| `mvn test` (backend) | ✅ pass — nhưng chỉ có 1 test `contextLoads` |
| `pnpm build` (frontend) | ✅ pass |
| `pnpm lint` (frontend) | ❌ 18 errors, 8 warnings |

---

## Phase 0 — Khôi phục hợp đồng API Frontend ↔ Backend  🔴 (ưu tiên cao nhất)

Hiện tại frontend gọi sai URL và đọc sai field ở hầu hết các màn hình → luồng chính gần như không chạy.

### 0.1 Sai URL

| Chức năng | Frontend đang gọi | Backend thật |
|---|---|---|
| Cập nhật profile | `PUT /auth/me` | `PUT /auth/profile` |
| Thanh toán | `POST /bookings/{id}/confirm-payment` | `POST /bookings/{id}/pay` |
| Booking của tôi | `GET /bookings/my` | `GET /bookings` |
| Admin: tất cả booking | `GET /bookings` (⚠ trả về booking của chính admin) | `GET /admin/bookings` |
| Admin: tạo/sửa/xoá hotel | `/hotels`, `/hotels/{id}` | `/admin/hotels`, `/admin/hotels/{id}` |
| Admin: room type | `/hotels/{id}/room-types`, `/room-types/{id}` | `/admin/hotels/{id}/room-types`, `/admin/room-types/{id}` |
| Review theo hotel | `GET /reviews/hotel/{id}` | `GET /hotels/{id}/reviews` |

### 0.2 Sai shape DTO (`frontend/src/types/index.ts`)

| Type | Frontend | Backend |
|---|---|---|
| `AuthResponse` | `{ token, user: UserProfile }` | phẳng `{ id, email, fullName, phone, role, token }` → map lại trong `api/auth.ts` |
| `HotelResponse` | `totalReviews` | `reviewCount` |
| `RoomTypeResponse/Request` | `areaM2`, `basePricePerNight` | `area`, `basePrice` |
| `BookingResponse` | lồng `user`, `roomType` | phẳng `roomTypeId, roomTypeName, hotelId, hotelName, hotelAddress, nights` |
| `ReviewResponse` | `userFullName` | `userName` |
| `ApiResponse` lỗi | đọc `message` | backend trả `error` |

### Việc làm
1. Sửa URL trong `api/*.ts`.
2. Sửa `types/index.ts` cho khớp DTO backend; `authApi.login/register` map về `{ token, user }` để
   `AuthContext` không phải đổi.
3. Thêm helper `getErrorMessage(err)` trong `lib/utils.ts` (đọc `error` → `message` → fallback), thay cho
   các đoạn cast lặp lại ở các page.
4. Sửa các page dùng field cũ (`booking.roomType.hotelName` → `booking.hotelName`, …).

**Kiểm chứng:** `pnpm build` (tsc bắt hết field sai).

---

## Phase 1 — Lỗi logic nghiệp vụ đặt phòng  🔴

| # | Vấn đề | Cách sửa |
|---|---|---|
| 1.1 | Không có chỗ nào set `COMPLETED` → **không ai review được** | `BookingLifecycleService.completeFinishedBookings(today)`: `CONFIRMED`/`CHECKED_IN` có `checkOut <= today` → `COMPLETED`. Chạy định kỳ bằng `@Scheduled` (mỗi giờ). |
| 1.2 | Booking `PENDING` giữ phòng vĩnh viễn | `expirePendingBookings(cutoff)`: `PENDING` tạo trước `now - app.booking.pending-ttl` (mặc định 30 phút) → `CANCELLED` + trả phòng. Chạy mỗi phút. |
| 1.3 | Vẫn đặt được phòng của hotel đã xoá | `findActiveRoomTypeById` kiểm tra thêm `hotel.active`; `deleteHotel` vô hiệu hoá luôn các room type. |
| 1.4 | Race condition khi tạo `room_inventory` mới (2 request cùng lúc → vi phạm unique → 500); đọc mỗi ngày 2 lần | `reserve`/`release`/cập nhật `totalRooms` đều khoá dòng `room_types` (`PESSIMISTIC_WRITE`) → tuần tự hoá theo room type, chạy được cả Postgres lẫn H2. Load inventory cả khoảng ngày bằng **1 query**, tạo các ngày còn thiếu, `saveAll`. |
| 1.5 | Chuyển trạng thái booking không khoá → pay & auto-expire có thể chạy chồng | `BookingRepository.findByIdForUpdate` (`PESSIMISTIC_WRITE`) cho mọi thao tác đổi status. |
| 1.6 | Mã booking `BK-yyyyMMdd-NNNN` chỉ ~9000 mã/ngày → trùng → 500 | 6 ký tự `[A-Z0-9]` từ `SecureRandom` (~2,1 tỷ tổ hợp/ngày) + kiểm tra `existsByBookingCode`, thử lại tối đa 5 lần. |
| 1.7 | Đổi `totalRooms` không cập nhật `room_inventory` đã có | Khi `totalRooms` đổi: cập nhật các ngày từ hôm nay trở đi (`available = total - booked`); từ chối nếu `total < booked` ở ngày nào đó. |
| 1.8 | Ngày chưa có inventory trả về `Integer.MAX_VALUE` | Dùng `roomType.totalRooms`. |
| 1.9 | Doanh thu dashboard chỉ tính `CONFIRMED` (sau 1.1 sẽ mất doanh thu `COMPLETED`) | Tính `CONFIRMED`, `CHECKED_IN`, `COMPLETED`. |
| 1.10 | Logic huỷ/xác nhận lặp giữa user và admin | Gom về `doCancel(booking)` / `doConfirm(booking)`. |

**Test:** integration test (MockMvc + H2) cho: đặt hết phòng → lỗi; huỷ trả phòng; hotel bị xoá
không đặt được; expire PENDING; complete → review được; đổi `totalRooms` thấp hơn số đã đặt → lỗi.

---

## Phase 2 — Bảo mật & độ bền API  🟠

| # | Vấn đề | Cách sửa |
|---|---|---|
| 2.1 | `/api/auth/**` permitAll → `/auth/me`, `/auth/profile` không token → NPE → 500 | Chỉ permit `POST /api/auth/register`, `POST /api/auth/login`. |
| 2.2 | Chưa xác thực trả 403 (không phải 401) → frontend không tự logout khi token hết hạn | `authenticationEntryPoint` trả 401 JSON, `accessDeniedHandler` trả 403 JSON (cùng format `ApiResponse`). |
| 2.3 | `/api/availability` public, không giới hạn khoảng ngày, 1 query/ngày | Validate `checkOut > checkIn`, tối đa 30 đêm (dùng chung với booking); chỉ permit `GET`. |
| 2.4 | `orElseThrow()` không tham số khắp nơi → `NoSuchElementException` → 500 | `UserRepository.getByEmailOrThrow` ném `ResourceNotFoundException`. |
| 2.5 | Thiếu handler → lỗi người dùng thành 500 | Thêm handler: `AuthenticationException` (vd. tài khoản bị khoá) → 401, `DataIntegrityViolationException` → 409, thiếu/sai kiểu tham số, body JSON hỏng → 400. |
| 2.6 | Validate input | `guestEmail` thêm `@Email`; `checkIn` dùng `@FutureOrPresent` (cho đặt ngày hôm nay); giới hạn độ dài chuỗi. |
| 2.7 | `size` phân trang không giới hạn | Helper `Pagination.of(page, size, sort)` kẹp `page ≥ 0`, `1 ≤ size ≤ 100`. |
| 2.8 | JWT | Cache `SecretKey`, kiểm tra secret ≥ 32 byte khi khởi động (fail fast), log debug khi token không hợp lệ thay vì nuốt im lặng. |
| 2.9 | `JWT_SECRET` hardcode trong `docker-compose.yml` | Đọc từ biến môi trường / `.env` (có `.env.example`). |

**Test:** `/auth/me` không token → 401; user thường gọi `/admin/**` → 403; availability > 30 đêm → 400.

---

## Phase 3 — Hiệu năng  🟡

| # | Vấn đề | Cách sửa |
|---|---|---|
| 3.1 | `searchHotels`: 2 query (AVG + COUNT) cho **mỗi** hotel | 1 query gộp `GROUP BY hotel.id` cho cả trang. |
| 3.2 | N+1 khi map `Booking → roomType → hotel` và `@ElementCollection` EAGER | `hibernate.default_batch_fetch_size: 50` + `@EntityGraph` cho các query danh sách booking. |
| 3.3 | `findAllByFilters` vừa `ORDER BY` trong JPQL vừa sort trong `Pageable` | Bỏ `ORDER BY` trong JPQL. |
| 3.4 | `getAvailableRooms` 1 query/ngày | Đã xử lý ở Phase 1 (1 query cho cả khoảng). |

---

## Phase 4 — Test, CI, chất lượng frontend  🟢

1. Test integration cho các phase trên (đã viết song song từng phase).
2. Sửa 18 lỗi `pnpm lint`:
   - `components/ui/*`: các file shadcn export cả hằng số → tắt rule `react-refresh/only-export-components` riêng cho `components/ui/**` (chuẩn của shadcn).
   - `AuthContext`: tách `useAuth` khỏi file component hoặc khởi tạo state đúng cách, bỏ `setState` đồng bộ trong effect.
   - Các page dùng hàm trước khi khai báo trong `useEffect` → dời khai báo / dùng `useCallback`.
3. GitHub Actions `ci.yml`: backend `mvn -B verify`, frontend `pnpm install --frozen-lockfile && pnpm lint && pnpm build`.

---

## Phase 5 — Backlog (đề xuất, chưa làm trong đợt này)

Các mục cần quyết định sản phẩm hoặc thay đổi lớn — để bạn chọn:

- **Flyway/Liquibase** thay `ddl-auto: update` (cần chụp schema hiện tại trên Supabase làm baseline).
- **Tìm kiếm theo ngày/số khách** (README có ghi nhưng API chưa hỗ trợ).
- **Refresh token** (`refresh-expiration` đã cấu hình nhưng chưa dùng).
- **Màn hình admin quản lý room type** (API client có nhưng chưa có page nào dùng).
- Gửi email xác nhận (`spring-boot-starter-mail` đã có nhưng chưa dùng).
- Xử lý booking tương lai khi xoá hotel (hiện tại vẫn giữ nguyên, admin tự huỷ).
- Code-splitting frontend (bundle 834 kB), thêm service frontend vào `docker-compose`.
- Tắt `spring.jpa.open-in-view` sau khi thêm `@Transactional(readOnly = true)` cho các hàm đọc.
