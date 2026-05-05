# Spring Boot Backend API Specification

## Healthcare Appointment Booking System

Tài liệu này mô tả tất cả API endpoints cần thiết để backend Spring Boot implement.

---

## 📋 Authentication Endpoints

### 1. POST /api/auth/register

**Đăng ký tài khoản mới**

**Request:**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "phoneNumber": "0123456789",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response (201):**

```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Error (400):**

```json
{
  "error": "Email already exists",
  "code": "DUPLICATE_EMAIL"
}
```

---

### 2. POST /api/auth/login

**Đăng nhập**

**Request:**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200):**

```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

**Error (401):**

```json
{
  "error": "Invalid credentials",
  "code": "INVALID_CREDENTIALS"
}
```

---

### 3. POST /api/auth/logout

**Đăng xuất**

**Headers:**

```
Authorization: Bearer {token}
```

**Response (200):**

```json
{
  "message": "Logged out successfully"
}
```

---

### 4. POST /api/auth/refresh

**Refresh JWT Token**

**Request:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

---

## 👨‍⚕️ Doctor Endpoints

### 5. GET /api/doctors

**Lấy danh sách bác sĩ (có phân trang, lọc, sắp xếp)**

**Query Parameters:**

```
GET /api/doctors?page=1&limit=10&specialty=cardiology&minRating=4&maxPrice=500000&search=Nguyễn&sortBy=rating&sortOrder=desc
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "doctor-1",
      "fullName": "Dr. Nguyễn Văn A",
      "specialty": "cardiology",
      "hospital": {
        "id": "hospital-1",
        "name": "Bệnh viện Trung Ương"
      },
      "bio": "Chuyên gia tim mạch...",
      "avatar": "https://api.example.com/avatars/doctor-1.jpg",
      "experience": 15,
      "price": 300000,
      "rating": 4.8,
      "reviewCount": 125,
      "isAvailable": true,
      "availableSlots": 12
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 150,
    "totalPages": 15
  }
}
```

---

### 6. GET /api/doctors/{id}

**Lấy thông tin chi tiết bác sĩ**

**Response (200):**

```json
{
  "id": "doctor-1",
  "fullName": "Dr. Nguyễn Văn A",
  "email": "doctor@example.com",
  "specialty": "cardiology",
  "bio": "Tốt nghiệp Đại học Y Hà Nội năm 2008...",
  "avatar": "https://api.example.com/avatars/doctor-1.jpg",
  "experience": 15,
  "license": "LIC-123456",
  "price": 300000,
  "rating": 4.8,
  "reviewCount": 125,
  "hospital": {
    "id": "hospital-1",
    "name": "Bệnh viện Trung Ương",
    "address": "Số 1, Phố Đông, Hà Nội",
    "phone": "0123456789"
  },
  "availableTime": [
    {
      "date": "2024-01-20",
      "slots": ["08:00", "08:30", "09:00", "09:30", "10:00", "14:00", "14:30", "15:00", "15:30", "16:00"]
    }
  ],
  "reviews": [
    {
      "id": "review-1",
      "userId": "user-123",
      "userName": "Nguyễn Văn B",
      "rating": 5,
      "comment": "Bác sĩ rất tháo vát và chu đáo",
      "createdAt": "2024-01-10T10:30:00Z"
    }
  ]
}
```

---

### 7. GET /api/specialties

**Lấy danh sách chuyên khoa**

**Response (200):**

```json
{
  "data": [
    {
      "id": "specialty-1",
      "name": "Cardiology",
      "vietnamName": "Tim Mạch",
      "icon": "heart",
      "doctorCount": 25,
      "description": "Chuyên khoa về tim mạch"
    }
  ]
}
```

---

## 🏥 Hospital Endpoints

### 8. GET /api/hospitals

**Lấy danh sách bệnh viện**

**Query Parameters:**

```
GET /api/hospitals?page=1&limit=10&search=Trung Ương&sortBy=rating
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "hospital-1",
      "name": "Bệnh viện Trung Ương",
      "address": "Số 1, Phố Đông, Hà Nội",
      "phone": "0123456789",
      "email": "contact@hospital.vn",
      "avatar": "https://api.example.com/hospitals/hospital-1.jpg",
      "rating": 4.7,
      "reviewCount": 320,
      "description": "Bệnh viện hàng đầu...",
      "doctorCount": 45,
      "serviceCount": 12
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 45,
    "totalPages": 5
  }
}
```

---

### 9. GET /api/hospitals/{id}

**Lấy chi tiết bệnh viện**

**Response (200):**

```json
{
  "id": "hospital-1",
  "name": "Bệnh viện Trung Ương",
  "address": "Số 1, Phố Đông, Hà Nội",
  "phone": "0123456789",
  "email": "contact@hospital.vn",
  "avatar": "https://api.example.com/hospitals/hospital-1.jpg",
  "rating": 4.7,
  "reviewCount": 320,
  "description": "Bệnh viện hàng đầu...",
  "services": ["Khám tổng quát", "Chụp X-quang", "Siêu âm"],
  "doctors": [
    {
      "id": "doctor-1",
      "fullName": "Dr. Nguyễn Văn A",
      "specialty": "cardiology",
      "avatar": "https://api.example.com/avatars/doctor-1.jpg",
      "rating": 4.8
    }
  ]
}
```

---

## 📅 Appointment Endpoints

### 10. POST /api/appointments

**Đặt lịch khám mới**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "doctorId": "doctor-1",
  "hospitalId": "hospital-1",
  "appointmentDate": "2024-01-20",
  "appointmentTime": "09:00",
  "reason": "Khám định kỳ",
  "notes": "Có tiền sử bệnh tim"
}
```

**Response (201):**

```json
{
  "id": "appointment-123",
  "doctorId": "doctor-1",
  "doctorName": "Dr. Nguyễn Văn A",
  "hospitalId": "hospital-1",
  "hospitalName": "Bệnh viện Trung Ương",
  "appointmentDate": "2024-01-20",
  "appointmentTime": "09:00",
  "status": "PENDING",
  "reason": "Khám định kỳ",
  "notes": "Có tiền sử bệnh tim",
  "price": 300000,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### 11. GET /api/appointments

**Lấy danh sách lịch khám của người dùng**

**Headers:**

```
Authorization: Bearer {token}
```

**Query Parameters:**

```
GET /api/appointments?status=PENDING&sortBy=date&sortOrder=asc
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "appointment-123",
      "doctorId": "doctor-1",
      "doctorName": "Dr. Nguyễn Văn A",
      "hospitalId": "hospital-1",
      "hospitalName": "Bệnh viện Trung Ương",
      "appointmentDate": "2024-01-20",
      "appointmentTime": "09:00",
      "status": "PENDING",
      "reason": "Khám định kỳ",
      "price": 300000,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 5,
    "totalPages": 1
  }
}
```

---

### 12. GET /api/appointments/{id}

**Lấy chi tiết một lịch khám**

**Headers:**

```
Authorization: Bearer {token}
```

**Response (200):**

```json
{
  "id": "appointment-123",
  "doctorId": "doctor-1",
  "doctorName": "Dr. Nguyễn Văn A",
  "doctorPhone": "0987654321",
  "hospitalId": "hospital-1",
  "hospitalName": "Bệnh viện Trung Ương",
  "hospitalAddress": "Số 1, Phố Đông, Hà Nội",
  "appointmentDate": "2024-01-20",
  "appointmentTime": "09:00",
  "status": "PENDING",
  "reason": "Khám định kỳ",
  "notes": "Có tiền sử bệnh tim",
  "price": 300000,
  "paymentStatus": "UNPAID",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### 13. PUT /api/appointments/{id}

**Chỉnh sửa lịch khám (thay đổi ngày/giờ)**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "appointmentDate": "2024-01-22",
  "appointmentTime": "14:00"
}
```

**Response (200):**

```json
{
  "id": "appointment-123",
  "appointmentDate": "2024-01-22",
  "appointmentTime": "14:00",
  "status": "PENDING",
  "message": "Lịch khám đã được cập nhật"
}
```

---

### 14. DELETE /api/appointments/{id}

**Hủy lịch khám**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "reason": "Không có thời gian"
}
```

**Response (200):**

```json
{
  "id": "appointment-123",
  "status": "CANCELLED",
  "message": "Lịch khám đã được hủy"
}
```

---

### 15. GET /api/appointments/{doctorId}/available-slots

**Lấy khung giờ còn trống của bác sĩ**

**Query Parameters:**

```
GET /api/appointments/doctor-1/available-slots?startDate=2024-01-20&endDate=2024-01-31
```

**Response (200):**

```json
{
  "doctorId": "doctor-1",
  "doctorName": "Dr. Nguyễn Văn A",
  "availableSlots": [
    {
      "date": "2024-01-20",
      "slots": ["08:00", "08:30", "09:00", "09:30", "10:00", "14:00", "14:30", "15:00"]
    },
    {
      "date": "2024-01-21",
      "slots": ["08:00", "08:30", "09:00", "10:00", "14:00", "14:30", "15:00", "15:30"]
    }
  ]
}
```

---

## 👤 User Profile Endpoints

### 16. GET /api/users/me

**Lấy thông tin cá nhân của người dùng hiện tại**

**Headers:**

```
Authorization: Bearer {token}
```

**Response (200):**

```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0123456789",
  "avatar": "https://api.example.com/avatars/user-123.jpg",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Đường Lê Lợi, Hà Nội",
  "healthInsurance": "123456789",
  "createdAt": "2024-01-01T10:30:00Z"
}
```

---

### 17. PUT /api/users/me

**Cập nhật thông tin cá nhân**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0987654321",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Đường Lê Lợi, Hà Nội",
  "healthInsurance": "123456789"
}
```

**Response (200):**

```json
{
  "id": "user-123",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "message": "Thông tin cá nhân đã được cập nhật"
}
```

---

### 18. PUT /api/users/me/password

**Đổi mật khẩu**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**Response (200):**

```json
{
  "message": "Mật khẩu đã được cập nhật"
}
```

---

### 19. PUT /api/users/me/avatar

**Cập nhật ảnh đại diện (FormData)**

**Headers:**

```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request:**

```
file: <image file>
```

**Response (200):**

```json
{
  "id": "user-123",
  "avatar": "https://api.example.com/avatars/user-123.jpg",
  "message": "Ảnh đại diện đã được cập nhật"
}
```

---

## 📋 Medical Records Endpoints

### 20. GET /api/medical-records

**Lấy hồ sơ y tế của người dùng**

**Headers:**

```
Authorization: Bearer {token}
```

**Query Parameters:**

```
GET /api/medical-records?limit=10&page=1
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "record-1",
      "appointmentId": "appointment-123",
      "doctorName": "Dr. Nguyễn Văn A",
      "visitDate": "2024-01-15",
      "diagnosis": "Cao huyết áp mức 1",
      "treatment": "Uống thuốc amlodipine 5mg hàng ngày",
      "notes": "Cần theo dõi huyết áp định kỳ",
      "attachments": [
        {
          "id": "file-1",
          "name": "ekg-result.pdf",
          "url": "https://api.example.com/files/ekg-result.pdf"
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 3,
    "totalPages": 1
  }
}
```

---

### 21. POST /api/medical-records

**Tạo hồ sơ y tế (từ admin/doctor)**

**Headers:**

```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request:**

```json
{
  "userId": "user-123",
  "appointmentId": "appointment-123",
  "diagnosis": "Cao huyết áp mức 1",
  "treatment": "Uống thuốc amlodipine 5mg hàng ngày",
  "notes": "Cần theo dõi huyết áp định kỳ"
}
```

**Response (201):**

```json
{
  "id": "record-1",
  "userId": "user-123",
  "appointmentId": "appointment-123",
  "diagnosis": "Cao huyết áp mức 1",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## ⭐ Review Endpoints

### 22. POST /api/reviews

**Viết đánh giá bác sĩ**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "doctorId": "doctor-1",
  "appointmentId": "appointment-123",
  "rating": 5,
  "comment": "Bác sĩ rất chuyên môn và tận tình"
}
```

**Response (201):**

```json
{
  "id": "review-1",
  "doctorId": "doctor-1",
  "userId": "user-123",
  "rating": 5,
  "comment": "Bác sĩ rất chuyên môn và tận tình",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### 23. GET /api/reviews

**Lấy đánh giá của người dùng**

**Headers:**

```
Authorization: Bearer {token}
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "review-1",
      "doctorId": "doctor-1",
      "doctorName": "Dr. Nguyễn Văn A",
      "rating": 5,
      "comment": "Bác sĩ rất chuyên môn",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

---

### 24. PUT /api/reviews/{id}

**Cập nhật đánh giá**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "rating": 4,
  "comment": "Cập nhật đánh giá"
}
```

**Response (200):**

```json
{
  "id": "review-1",
  "rating": 4,
  "comment": "Cập nhật đánh giá",
  "updatedAt": "2024-01-16T10:30:00Z"
}
```

---

### 25. DELETE /api/reviews/{id}

**Xóa đánh giá**

**Headers:**

```
Authorization: Bearer {token}
```

**Response (200):**

```json
{
  "message": "Đánh giá đã được xóa"
}
```

---

## 💳 Payment Endpoints

### 26. POST /api/payments/process

**Xử lý thanh toán**

**Headers:**

```
Authorization: Bearer {token}
```

**Request:**

```json
{
  "appointmentId": "appointment-123",
  "amount": 300000,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "cardHolder": "NGUYEN VAN A",
  "expiryDate": "12/25",
  "cvv": "123"
}
```

**Response (200):**

```json
{
  "id": "payment-1",
  "appointmentId": "appointment-123",
  "amount": 300000,
  "paymentMethod": "CREDIT_CARD",
  "status": "SUCCESS",
  "transactionId": "TXN123456789",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Alternative Methods:**

- `BANK_TRANSFER` - Chuyển khoản ngân hàng
- `MOMO` - Ví MoMo
- `ZALOPAY` - ZaloPay
- `DIRECT_PAYMENT` - Thanh toán trực tiếp tại bệnh viện

---

### 27. GET /api/payments

**Lấy lịch sử thanh toán**

**Headers:**

```
Authorization: Bearer {token}
```

**Query Parameters:**

```
GET /api/payments?status=SUCCESS&sortBy=date&sortOrder=desc
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "payment-1",
      "appointmentId": "appointment-123",
      "doctorName": "Dr. Nguyễn Văn A",
      "amount": 300000,
      "paymentMethod": "CREDIT_CARD",
      "status": "SUCCESS",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 8,
    "totalPages": 1
  }
}
```

---

## 🛡️ Admin Endpoints

### 28. GET /api/admin/dashboard

**Lấy thống kê dashboard**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Response (200):**

```json
{
  "statistics": {
    "totalUsers": 1250,
    "totalDoctors": 85,
    "totalAppointments": 3456,
    "totalRevenue": 1234567890,
    "monthlyAppointments": 456,
    "monthlyRevenue": 123456789,
    "appointmentStatuses": {
      "PENDING": 45,
      "CONFIRMED": 320,
      "COMPLETED": 2890,
      "CANCELLED": 201
    }
  }
}
```

---

### 29. GET /api/admin/users

**Lấy danh sách người dùng (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Query Parameters:**

```
GET /api/admin/users?page=1&limit=20&search=Nguyễn&sortBy=createdAt
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "user-123",
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "appointments": 5,
      "createdAt": "2024-01-01T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1250,
    "totalPages": 63
  }
}
```

---

### 30. DELETE /api/admin/users/{id}

**Xóa người dùng (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Response (200):**

```json
{
  "id": "user-123",
  "message": "Người dùng đã được xóa"
}
```

---

### 31. GET /api/admin/doctors

**Lấy danh sách bác sĩ (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Query Parameters:**

```
GET /api/admin/doctors?page=1&limit=20&search=Nguyễn
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "doctor-1",
      "fullName": "Dr. Nguyễn Văn A",
      "email": "doctor@example.com",
      "specialty": "cardiology",
      "hospital": "Bệnh viện Trung Ương",
      "appointments": 120,
      "rating": 4.8
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 85,
    "totalPages": 5
  }
}
```

---

### 32. DELETE /api/admin/doctors/{id}

**Xóa bác sĩ (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Response (200):**

```json
{
  "id": "doctor-1",
  "message": "Bác sĩ đã được xóa"
}
```

---

### 33. GET /api/admin/appointments

**Lấy tất cả lịch khám (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Query Parameters:**

```
GET /api/admin/appointments?page=1&limit=20&status=PENDING&sortBy=date
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "appointment-123",
      "userId": "user-123",
      "userName": "Nguyễn Văn A",
      "doctorId": "doctor-1",
      "doctorName": "Dr. Nguyễn Văn A",
      "appointmentDate": "2024-01-20",
      "appointmentTime": "09:00",
      "status": "PENDING",
      "price": 300000
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 3456,
    "totalPages": 173
  }
}
```

---

### 34. PUT /api/admin/appointments/{id}

**Cập nhật trạng thái lịch khám (Admin)**

**Headers:**

```
Authorization: Bearer {token}
Role: ADMIN
```

**Request:**

```json
{
  "status": "CONFIRMED"
}
```

**Response (200):**

```json
{
  "id": "appointment-123",
  "status": "CONFIRMED",
  "message": "Trạng thái lịch khám đã được cập nhật"
}
```

---

## 🔒 Error Responses

Tất cả endpoints sử dụng error response format sau:

**400 - Bad Request:**

```json
{
  "error": "Invalid input",
  "code": "INVALID_INPUT",
  "details": [
    {
      "field": "email",
      "message": "Email is invalid"
    }
  ]
}
```

**401 - Unauthorized:**

```json
{
  "error": "Unauthorized",
  "code": "UNAUTHORIZED",
  "message": "Token is invalid or expired"
}
```

**403 - Forbidden:**

```json
{
  "error": "Forbidden",
  "code": "FORBIDDEN",
  "message": "You do not have permission to access this resource"
}
```

**404 - Not Found:**

```json
{
  "error": "Not Found",
  "code": "NOT_FOUND",
  "message": "Resource not found"
}
```

**500 - Server Error:**

```json
{
  "error": "Internal Server Error",
  "code": "INTERNAL_ERROR",
  "message": "Something went wrong"
}
```

---

## 🔐 Security Requirements

1. **JWT Token:**

   - Implement JWT for authentication
   - Token expiration: 24 hours
   - Refresh token mechanism
   - Store token in HTTP-only cookies (recommended)

2. **CORS:**

   - Allow requests from `http://localhost:3000` (dev)
   - Allow requests from your production domain
   - Include credentials in requests

3. **Password Security:**

   - Hash passwords using bcrypt with salt rounds ≥ 10
   - Minimum password length: 8 characters
   - Require mix of uppercase, lowercase, numbers, special characters

4. **Rate Limiting:**

   - Implement rate limiting (e.g., 100 requests per minute per IP)
   - Stricter limits for authentication endpoints (e.g., 5 login attempts per minute)

5. **Input Validation:**

   - Validate all input on backend
   - Sanitize inputs to prevent SQL injection
   - Validate file uploads (size, type, virus scan)

6. **Data Protection:**
   - Use HTTPS only
   - Encrypt sensitive data in database
   - Implement row-level security for user data

---

## 📝 Database Schema Requirements

### Users Table

```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20),
  avatar_url VARCHAR(500),
  date_of_birth DATE,
  gender ENUM('MALE', 'FEMALE', 'OTHER'),
  address VARCHAR(500),
  health_insurance VARCHAR(100),
  role ENUM('USER', 'ADMIN', 'DOCTOR') DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Doctors Table

```sql
CREATE TABLE doctors (
  id VARCHAR(36) PRIMARY KEY,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  phone_number VARCHAR(20),
  specialty VARCHAR(100) NOT NULL,
  hospital_id VARCHAR(36),
  bio TEXT,
  avatar_url VARCHAR(500),
  experience INT,
  license VARCHAR(100) UNIQUE,
  price DECIMAL(10, 2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);
```

### Hospitals Table

```sql
CREATE TABLE hospitals (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(500),
  phone VARCHAR(20),
  email VARCHAR(255),
  avatar_url VARCHAR(500),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Appointments Table

```sql
CREATE TABLE appointments (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  doctor_id VARCHAR(36) NOT NULL,
  hospital_id VARCHAR(36) NOT NULL,
  appointment_date DATE NOT NULL,
  appointment_time TIME NOT NULL,
  status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
  reason VARCHAR(500),
  notes TEXT,
  price DECIMAL(10, 2),
  payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);
```

### Reviews Table

```sql
CREATE TABLE reviews (
  id VARCHAR(36) PRIMARY KEY,
  doctor_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  appointment_id VARCHAR(36),
  rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
```

### Medical Records Table

```sql
CREATE TABLE medical_records (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  appointment_id VARCHAR(36),
  doctor_id VARCHAR(36),
  diagnosis TEXT,
  treatment TEXT,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (appointment_id) REFERENCES appointments(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
```

### Payments Table

```sql
CREATE TABLE payments (
  id VARCHAR(36) PRIMARY KEY,
  appointment_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  payment_method VARCHAR(50),
  status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
  transaction_id VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointments(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🚀 Frontend Configuration

Cập nhật `.env.local` trong Next.js project:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_NAME=Healthcare Appointment Booking
```

---

## 📞 Integration Checklist

- [ ] Implement all 34 endpoints
- [ ] Set up JWT authentication
- [ ] Create database schema
- [ ] Implement input validation
- [ ] Set up CORS for localhost:3000
- [ ] Implement error handling
- [ ] Add logging/monitoring
- [ ] Test all endpoints with Postman
- [ ] Deploy to production
- [ ] Update NEXT_PUBLIC_API_URL for production

---

Tài liệu này cung cấp tất cả thông tin cần thiết để xây dựng Spring Boot backend. Frontend đã hoàn toàn sẵn sàng để tích hợp!
