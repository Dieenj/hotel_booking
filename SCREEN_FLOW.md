# Hotel Booking Platform — Tài liệu Luồng Màn Hình

---

## Tổng quan hệ thống

Hệ thống có **2 actor chính:**

| Actor | Mô tả |
|-------|-------|
| **Guest** | Người dùng cuối — tìm kiếm, đặt phòng, quản lý booking |
| **Admin** | Quản trị viên — quản lý khách sạn, phòng, booking |

---

## PHẦN 1 — LUỒNG GUEST

### 1.1 Luồng tổng quan

```
                        ┌─────────────────────────────────────────────┐
                        │                  GUEST FLOW                  │
                        └─────────────────────────────────────────────┘

[Chưa đăng nhập]
        │
        ▼
  ┌──────────┐
  │   Home   │──── Search ────► Search Results ────► Hotel Detail ────► Room Detail
  └──────────┘                                                               │
        │                                                                    │
        │                                                               [Chưa login?]
        │                                                                    │
   [Register]                                                           ┌───▼────┐
        │                                                               │  Login │
        ▼                                                               └───┬────┘
  ┌──────────┐                                                              │
  │ Register │                                                              ▼
  └──────────┘                                                      ┌─────────────┐
        │                                                            │ Booking Form│
        └──────────────────── Login ──────────────────────────────► └──────┬──────┘
                                │                                          │
                                ▼                                          ▼
                          ┌──────────┐                              ┌─────────────┐
                          │  Home    │                              │   Payment   │
                          │(logged)  │                              └──────┬──────┘
                          └──────────┘                                     │
                                │                                          ▼
                          [My Profile]                           ┌──────────────────┐
                                │                                │  Confirmation    │
                          ┌─────▼──────┐                        └──────────────────┘
                          │  Profile   │
                          └─────┬──────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
            ┌─────────────┐       ┌──────────────────┐
            │ My Bookings │       │  Edit Profile    │
            └──────┬──────┘       └──────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
┌──────────────┐    ┌──────────────────┐
│ Booking      │    │  Write Review    │
│ Detail       │    │  (sau checkout)  │
└──────┬───────┘    └──────────────────┘
       │
  [Upcoming?]
       │
       ▼
  Cancel Booking
```

---

### 1.2 Mô tả từng màn hình — Guest

---

#### S01 — Home (Trang chủ)

**Mục đích:** Điểm vào chính của ứng dụng, nơi user bắt đầu tìm kiếm.

**Thành phần chính:**
- Hero banner với tagline và Search Bar nổi bật
- Search Bar gồm: Destination (text), Check-in / Check-out (date picker), Guests (counter), nút Search
- Section "Featured Hotels" — danh sách 6 khách sạn nổi bật dạng card
- Section "Popular Destinations" — grid ảnh các thành phố

**Hành động:**
- Nhập thông tin tìm kiếm → nhấn Search → chuyển sang Search Results
- Click vào Hotel Card → chuyển sang Hotel Detail
- Click Login / Register trên navbar

---

#### S02 — Search Results (Kết quả tìm kiếm)

**Mục đích:** Hiển thị danh sách khách sạn phù hợp với tiêu chí tìm kiếm.

**Thành phần chính:**
- Search Bar ở trên (có thể chỉnh sửa lại tham số)
- Sidebar Filter:
  - Price Range (slider min-max)
  - Star Rating (1-5 sao, checkbox)
  - Amenities (Wifi, Pool, Parking, Gym... checkbox)
- Danh sách Hotel Card (horizontal layout):
  - Ảnh thumbnail
  - Tên, địa điểm, rating, số review
  - Giá từ (per night)
  - Tag tiện nghi nổi bật
- Sort bar: Giá tăng dần / Giá giảm dần / Rating cao nhất
- Pagination

**Hành động:**
- Thay đổi filter → danh sách cập nhật
- Click Hotel Card → chuyển sang Hotel Detail

---

#### S03 — Hotel Detail (Chi tiết khách sạn)

**Mục đích:** Hiển thị toàn bộ thông tin khách sạn để user quyết định đặt phòng.

**Thành phần chính:**
- Image Gallery (1 ảnh lớn + thumbnail nhỏ, click để xem full)
- Thông tin cơ bản: Tên, địa chỉ, rating tổng, số review
- Tabs điều hướng: Overview / Rooms / Reviews
- **Tab Overview:**
  - Mô tả khách sạn (paragraph)
  - Danh sách tiện nghi (icon + label grid)
  - Bản đồ vị trí (static image hoặc Google Maps embed)
- **Tab Rooms:**
  - Danh sách Room Type Card:
    - Ảnh phòng
    - Tên loại phòng (Standard / Deluxe / Suite)
    - Sức chứa (số người)
    - Diện tích
    - Tiện nghi riêng
    - Giá / đêm
    - Số phòng còn lại (ví dụ: "Only 3 left!")
    - Nút "Book Now"
- **Tab Reviews:**
  - Rating breakdown (cleanliness / service / location / value)
  - Danh sách review card: avatar, tên, ngày, comment, sao

**Hành động:**
- Click "Book Now" → chuyển sang Booking Form (nếu chưa login thì redirect sang Login trước)
- Click thumbnail → mở gallery fullscreen

---

#### S04 — Login

**Mục đích:** Xác thực người dùng bằng email + password.

**Thành phần chính:**
- Logo + tiêu đề "Welcome back"
- Input Email
- Input Password (có toggle show/hide)
- Nút "Sign In"
- Link "Forgot Password?" (có thể để static cho intern)
- Divider "or"
- Link "Create an account"

**Hành động:**
- Submit form → validate → gọi API login → lưu JWT → redirect về trang trước đó hoặc Home
- Click "Create an account" → chuyển sang Register

---

#### S05 — Register

**Mục đích:** Tạo tài khoản mới cho Guest.

**Thành phần chính:**
- Input Full Name
- Input Email
- Input Phone (optional)
- Input Password
- Input Confirm Password
- Checkbox đồng ý điều khoản
- Nút "Create Account"
- Link "Already have an account? Sign in"

**Hành động:**
- Submit → validate client-side → gọi API register → auto login → redirect Home

---

#### S06 — Booking Form (Form đặt phòng)

**Mục đích:** Thu thập thông tin cần thiết để hoàn tất booking.

**Thành phần chính:**
- **Cột trái — Guest Information:**
  - Full Name (pre-filled từ profile)
  - Email (pre-filled)
  - Phone
  - Special Requests (textarea)
- **Cột phải — Booking Summary:**
  - Ảnh + tên khách sạn
  - Tên loại phòng
  - Check-in / Check-out date
  - Số đêm
  - Số khách
  - Giá / đêm × số đêm
  - Thuế / phí (nếu có)
  - **Tổng cộng** (highlighted)
  - Nút "Proceed to Payment"

**Hành động:**
- Nhấn "Proceed to Payment" → validate → chuyển sang Payment

---

#### S07 — Payment (Thanh toán)

**Mục đích:** Xử lý thanh toán (mock cho intern).

**Thành phần chính:**
- Tiêu đề "Secure Payment"
- Booking summary compact (hotel, dates, total)
- **Payment Form:**
  - Card Number (format: 1234 5678 9012 3456)
  - Cardholder Name
  - Expiry Date (MM/YY)
  - CVV
  - Radio chọn loại thẻ: Visa / Mastercard
- Nút "Pay Now — [tổng tiền]"
- Badge "Secured by SSL"

**Lưu ý cho intern:** Đây là mock payment — không cần tích hợp cổng thanh toán thật. Submit form → gọi API tạo booking với status CONFIRMED.

**Hành động:**
- Submit → gọi API → chuyển sang Confirmation

---

#### S08 — Booking Confirmation (Xác nhận đặt phòng)

**Mục đích:** Thông báo đặt phòng thành công và cung cấp thông tin booking.

**Thành phần chính:**
- Icon check-circle lớn màu xanh
- Tiêu đề "Booking Confirmed!"
- Booking ID (ví dụ: #BK-20240718-0042)
- Chi tiết booking:
  - Khách sạn, địa chỉ
  - Loại phòng
  - Check-in / Check-out
  - Số khách
  - Tổng tiền đã thanh toán
- Thông báo "Confirmation email sent to [email]"
- 2 nút: "View My Bookings" | "Back to Home"

---

#### S09 — My Bookings (Lịch sử đặt phòng)

**Mục đích:** Xem toàn bộ lịch sử booking của user.

**Thành phần chính:**
- Tabs lọc: All / Upcoming / Completed / Cancelled
- Danh sách Booking Card:
  - Ảnh khách sạn (thumbnail nhỏ)
  - Tên khách sạn, loại phòng
  - Check-in → Check-out
  - Booking ID
  - Tổng tiền
  - Badge trạng thái (Pending / Confirmed / Completed / Cancelled)
  - Nút "View Detail"
  - Nút "Cancel" (chỉ hiện nếu Upcoming + còn trong thời hạn hủy)
  - Nút "Write Review" (chỉ hiện nếu Completed + chưa review)

---

#### S10 — Booking Detail (Chi tiết booking)

**Mục đích:** Xem chi tiết đầy đủ một booking cụ thể.

**Thành phần chính:**
- Booking ID + Badge trạng thái
- Thông tin khách sạn (tên, địa chỉ, ảnh)
- Thông tin phòng (loại, tiện nghi)
- Check-in / Check-out / Số đêm / Số khách
- Thông tin khách đặt (tên, email, phone)
- Special Requests
- Bảng giá chi tiết
- Timeline trạng thái: Booked → Confirmed → Check-in → Check-out
- Nút "Cancel Booking" (nếu còn được hủy) → mở Modal xác nhận

---

#### S11 — Write Review (Viết đánh giá)

**Mục đích:** Cho phép guest đánh giá sau khi hoàn thành kỳ lưu trú.

**Thành phần chính:**
- Thông tin khách sạn (tên, ảnh nhỏ)
- Rating tổng (click vào sao 1-5)
- Rating theo tiêu chí:
  - Cleanliness ★
  - Service ★
  - Location ★
  - Value for Money ★
- Textarea "Share your experience"
- Nút "Submit Review"

---

#### S12 — Profile (Trang cá nhân)

**Mục đích:** Xem và chỉnh sửa thông tin tài khoản.

**Thành phần chính:**
- Avatar (click để upload ảnh)
- Full Name, Email (readonly), Phone
- Nút "Edit Profile"
- Nút "Change Password"
- Nút "Sign Out"

---

## PHẦN 2 — LUỒNG ADMIN

### 2.1 Luồng tổng quan

```
                    ┌─────────────────────────────────────┐
                    │              ADMIN FLOW              │
                    └─────────────────────────────────────┘

[Admin Login]
      │
      ▼
┌──────────────┐
│  Dashboard   │
└──────┬───────┘
       │
  ┌────┴────────────────────────────────────────┐
  │                                             │
  ▼                                             ▼
[Hotel Management]                      [Booking Management]
  │                                             │
  ├── Hotel List                                ├── Booking List
  │     ├── Add Hotel                           │     └── Booking Detail
  │     ├── Edit Hotel                          │           ├── Confirm
  │     └── Delete Hotel                        │           └── Cancel
  │
  └── Room Type Management (per hotel)
        ├── Room Type List
        ├── Add Room Type
        ├── Edit Room Type
        └── Delete Room Type
```

---

### 2.2 Mô tả từng màn hình — Admin

---

#### A01 — Admin Login

**Mục đích:** Đăng nhập riêng cho admin (cùng endpoint, khác role).

**Thành phần chính:**
- Badge "Admin Portal"
- Input Email + Password
- Nút "Sign In"

**Hành động:** Submit → xác thực role ADMIN → redirect Dashboard. Nếu role là GUEST → báo lỗi "Access denied".

---

#### A02 — Dashboard

**Mục đích:** Tổng quan tình hình hoạt động của hệ thống.

**Thành phần chính:**
- **Stat Cards (row):**
  - Total Revenue (tháng này)
  - Total Bookings (tháng này)
  - Occupancy Rate (%)
  - Total Hotels
- **Chart:** Doanh thu theo tuần/tháng (bar chart hoặc line chart)
- **Table: Recent Bookings** — 10 booking mới nhất (ID, Guest, Hotel, Dates, Status, Amount)
- **Table: Top Hotels** — theo số booking hoặc doanh thu

---

#### A03 — Hotel List (Danh sách khách sạn)

**Mục đích:** Xem và quản lý tất cả khách sạn trong hệ thống.

**Thành phần chính:**
- Nút "Add Hotel" (primary, góc phải)
- Bộ lọc: Search by name, filter by status
- Table:
  - Ảnh (thumbnail)
  - Tên khách sạn
  - Địa chỉ / Thành phố
  - Rating trung bình
  - Số Room Types
  - Status (Active / Inactive)
  - Actions: Edit | Delete
- Pagination

**Hành động:**
- Click "Add Hotel" → chuyển sang Hotel Form (create mode)
- Click "Edit" → chuyển sang Hotel Form (edit mode)
- Click "Delete" → mở Modal xác nhận

---

#### A04 — Hotel Form (Thêm / Sửa khách sạn)

**Mục đích:** Tạo mới hoặc chỉnh sửa thông tin khách sạn.

**Thành phần chính:**
- Breadcrumb: Hotels > Add Hotel (hoặc Edit)
- Input: Hotel Name
- Input: Address, City, Country
- Textarea: Description
- Input: Star Rating (1-5, select)
- Upload ảnh (multiple images, preview)
- Checkbox grid: Amenities (Wifi, Pool, Parking, Restaurant, Gym, Spa...)
- Input: Google Maps URL hoặc Lat/Lng (optional)
- Nút "Save" | "Cancel"

---

#### A05 — Room Type List (Danh sách loại phòng của một khách sạn)

**Mục đích:** Quản lý các loại phòng thuộc một khách sạn cụ thể.

**Thành phần chính:**
- Breadcrumb: Hotels > [Hotel Name] > Room Types
- Thông tin hotel compact (tên, địa chỉ)
- Nút "Add Room Type"
- Table:
  - Ảnh phòng
  - Tên loại phòng
  - Sức chứa (người)
  - Diện tích (m²)
  - Giá cơ bản / đêm
  - Tổng số phòng
  - Status
  - Actions: Edit | Delete

---

#### A06 — Room Type Form (Thêm / Sửa loại phòng)

**Mục đích:** Tạo mới hoặc chỉnh sửa loại phòng.

**Thành phần chính:**
- Input: Room Type Name (Standard / Deluxe / Suite / custom)
- Textarea: Description
- Input: Max Capacity (số người)
- Input: Area (m²)
- Input: Base Price (VNĐ / đêm)
- Input: Total Rooms (tổng số phòng loại này)
- Upload ảnh (multiple)
- Checkbox grid: In-room Amenities (TV, AC, Bathtub, Balcony, Mini Bar...)
- Nút "Save" | "Cancel"

---

#### A07 — Booking List (Danh sách đặt phòng)

**Mục đích:** Xem và quản lý tất cả booking trong hệ thống.

**Thành phần chính:**
- Filter bar:
  - Search by Booking ID hoặc Guest name
  - Filter by Status (All / Pending / Confirmed / Completed / Cancelled)
  - Filter by Date range
- Table:
  - Booking ID
  - Guest Name
  - Hotel & Room Type
  - Check-in / Check-out
  - Nights
  - Total Amount
  - Status badge
  - Actions: View Detail
- Export CSV (optional)
- Pagination

---

#### A08 — Booking Detail — Admin View

**Mục đích:** Xem chi tiết và xử lý booking.

**Thành phần chính:**
- Booking ID + Status badge
- Guest info: Tên, Email, Phone
- Hotel info: Tên, địa chỉ
- Room Type, Check-in, Check-out, Số đêm, Số khách
- Special Requests
- Payment info: Tổng tiền, phương thức thanh toán, thời gian thanh toán
- **Admin Actions:**
  - Nút "Confirm" (nếu status = Pending)
  - Nút "Cancel" (nếu status = Pending hoặc Confirmed)
- Ghi chú admin (textarea — optional)

---

## PHẦN 3 — MODAL & OVERLAY

| Modal | Trigger | Nội dung |
|-------|---------|----------|
| Confirm Cancel Booking | Guest click "Cancel" | "Are you sure?" + note chính sách hủy + 2 nút Yes/No |
| Confirm Delete Hotel | Admin click "Delete" | "This will delete all room types. Confirm?" + 2 nút |
| Image Gallery | Click ảnh trong Hotel Detail | Fullscreen gallery với prev/next |
| Confirm Payment | Guest click "Pay Now" | Summary + confirm |

---

## PHẦN 4 — TRẠNG THÁI BOOKING

```
[PENDING] ──── Admin Confirm ────► [CONFIRMED]
                                         │
                                    Check-in date
                                         │
                                         ▼
                                   [CHECKED_IN]
                                         │
                                   Check-out date
                                         │
                                         ▼
                                   [COMPLETED] ──── Guest Review ────► [REVIEWED]

[PENDING] ──── Cancel (Guest/Admin) ────► [CANCELLED]
[CONFIRMED] ── Cancel (Guest/Admin) ────► [CANCELLED]
```

---

## PHẦN 5 — NAVIGATION STRUCTURE

### Guest Navbar
```
Logo | [Search Bar] | My Bookings | [Avatar dropdown: Profile / Sign Out]
                                  | (chưa login): Sign In | Register
```

### Admin Sidebar
```
📊 Dashboard
🏨 Hotels
   └── [danh sách hotel expand]
       └── Room Types
📋 Bookings
⭐ Reviews (optional)
👤 Profile
```

---

## Tóm tắt số lượng màn hình

| Nhóm | Số màn hình |
|------|-------------|
| Guest — Auth | 2 |
| Guest — Search & Hotel | 3 |
| Guest — Booking Flow | 3 |
| Guest — Profile & History | 4 |
| Admin | 7 |
| **Tổng** | **19** |
