# Backend API Responses

Tài liệu này tổng hợp **response body** của các API hiện có trong backend, dựa trên source code ở:

- [./src/main/java/hospital/web/rest/AuthResource.java](./src/main/java/hospital/web/rest/AuthResource.java)
- [./src/main/java/hospital/web/rest/AuthenticateController.java](./src/main/java/hospital/web/rest/AuthenticateController.java)
- [./src/main/java/hospital/web/rest/AccountResource.java](./src/main/java/hospital/web/rest/AccountResource.java)
- [./src/main/java/hospital/web/rest/DoctorResource.java](./src/main/java/hospital/web/rest/DoctorResource.java)
- [./src/main/java/hospital/web/rest/HospitalResource.java](./src/main/java/hospital/web/rest/HospitalResource.java)
- [./src/main/java/hospital/web/rest/AppointmentResource.java](./src/main/java/hospital/web/rest/AppointmentResource.java)
- [./src/main/java/hospital/web/rest/PaymentResource.java](./src/main/java/hospital/web/rest/PaymentResource.java)
- [./src/main/java/hospital/web/rest/ReviewResource.java](./src/main/java/hospital/web/rest/ReviewResource.java)
- [./src/main/java/hospital/web/rest/MedicalRecordResource.java](./src/main/java/hospital/web/rest/MedicalRecordResource.java)
- [./src/main/java/hospital/web/rest/SpecialtyResource.java](./src/main/java/hospital/web/rest/SpecialtyResource.java)
- [./src/main/java/hospital/web/rest/UserProfileResource.java](./src/main/java/hospital/web/rest/UserProfileResource.java)
- [./src/main/java/hospital/web/rest/AdminResource.java](./src/main/java/hospital/web/rest/AdminResource.java)
- [./src/main/java/hospital/web/rest/UserResource.java](./src/main/java/hospital/web/rest/UserResource.java)
- [./src/main/java/hospital/web/rest/AuthorityResource.java](./src/main/java/hospital/web/rest/AuthorityResource.java)

> Ghi chú: nhiều endpoint trả về `Map<String, Object>`, nên schema có thể linh hoạt theo dữ liệu thực tế.

---

## 1) Auth APIs

### `POST /api/auth/register`

**Response `201 Created`**

```json
{
  "id": "1",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "token": "jwt-token",
  "expiresIn": 86400,
  "createdAt": "2026-04-28T08:00:00Z"
}
```

**Error `400`**

```json
{ "error": "Passwords do not match", "code": "PASSWORD_MISMATCH" }
```

```json
{
  "error": "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character",
  "code": "WEAK_PASSWORD"
}
```

```json
{ "error": "Email already exists", "code": "DUPLICATE_EMAIL" }
```

---

### `POST /api/auth/login`

**Response `200 OK`**

```json
{
  "id": "1",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "token": "jwt-token",
  "expiresIn": 86400
}
```

**Error `401`**

```json
{ "error": "Invalid credentials", "code": "INVALID_CREDENTIALS" }
```

---

### `POST /api/auth/logout`

**Response `200 OK`**

```json
{
  "message": "Logged out successfully"
}
```

---

### `POST /api/auth/refresh`

**Response `200 OK`**

```json
{
  "id": "1",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "token": "new-jwt-token",
  "expiresIn": 86400
}
```

**Error `401`**

```json
{ "error": "Unauthorized", "code": "UNAUTHORIZED" }
```

---

## 2) JWT auth kiểu JHipster

### `POST /api/authenticate`

**Response `200 OK`**

```json
{
  "id_token": "jwt-token"
}
```

### `GET /api/authenticate`

- **`204 No Content`** nếu đã đăng nhập
- **`401 Unauthorized`** nếu chưa đăng nhập

---

## 3) Account APIs

### `POST /api/register`

**Response `201 Created`**

- Không có body

---

### `GET /api/activate?key=...`

**Response**

- Không có body

---

### `GET /api/account`

**Response `200 OK`** trả về `AdminUserDTO`

```json
{
  "id": 1,
  "login": "user@example.com",
  "firstName": "Nguyễn Văn",
  "lastName": "A",
  "email": "user@example.com",
  "imageUrl": "/img/avatar.png",
  "activated": true,
  "langKey": "en",
  "createdBy": "admin",
  "createdDate": "2026-04-28T08:00:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-04-28T08:10:00Z",
  "authorities": ["ROLE_USER"]
}
```

---

### `POST /api/account`

**Response**

- Không có body

---

### `POST /api/account/change-password`

**Response**

- Không có body

---

### `POST /api/account/reset-password/init`

**Response**

- Không có body

---

### `POST /api/account/reset-password/finish`

**Response**

- Không có body

---

## 4) Public user API

### `GET /api/users`

**Response `200 OK`**

Body là `List<UserDTO>`:

```json
[
  {
    "id": 1,
    "login": "user@example.com"
  }
]
```

> API này còn trả thêm **pagination headers** trong response header.

---

## 5) Profile APIs

### `GET /api/users/me`

**Response `200 OK`**

```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0123456789",
  "avatar": "/uploads/avatars/1/avatar.png",
  "dateOfBirth": "1998-01-01",
  "gender": "male",
  "address": "Hà Nội",
  "healthInsurance": "BH123456",
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

### `PUT /api/users/me`

**Response `200 OK`**

```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "message": "Thông tin cá nhân đã được cập nhật"
}
```

---

### `PUT /api/users/me/password`

**Response `200 OK`**

```json
{
  "message": "Mật khẩu đã được cập nhật"
}
```

**Error `400`**

```json
{ "error": "Invalid password", "code": "INVALID_PASSWORD" }
```

```json
{ "error": "Password confirmation does not match", "code": "PASSWORD_MISMATCH" }
```

```json
{
  "error": "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character",
  "code": "WEAK_PASSWORD"
}
```

---

### `PUT /api/users/me/avatar`

**Response `200 OK`**

```json
{
  "id": 1,
  "avatar": "/uploads/avatars/1/my-avatar.jpg",
  "message": "Ảnh đại diện đã được cập nhật"
}
```

---

## 6) Doctor APIs

### `GET /api/doctors`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "fullName": "Dr. Nguyễn Văn A",
      "specialty": "Cardiology",
      "hospital": {
        "id": 1,
        "name": "Bệnh viện Trung ương"
      },
      "bio": "Chuyên gia tim mạch...",
      "avatar": "/avatars/doctor-1.jpg",
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
    "total": 100,
    "totalPages": 10
  }
}
```

---

### `GET /api/doctors/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "fullName": "Dr. Nguyễn Văn A",
  "email": "doctor@example.com",
  "specialty": "Cardiology",
  "bio": "Tốt nghiệp...",
  "avatar": "/avatars/doctor-1.jpg",
  "experience": 15,
  "license": "LIC-123456",
  "price": 300000,
  "rating": 4.8,
  "reviewCount": 125,
  "hospital": {
    "id": 1,
    "name": "Bệnh viện Trung ương",
    "address": "Số 1, Phố Đông, Hà Nội",
    "phone": "0123456789"
  },
  "availableTime": [
    {
      "date": "2026-05-01",
      "slots": ["08:00", "08:30", "09:00"]
    }
  ],
  "reviews": [
    {
      "id": 1,
      "userId": 1,
      "userName": "Nguyễn Văn B",
      "rating": 5,
      "comment": "Bác sĩ rất tận tâm",
      "createdAt": "2026-04-28T08:00:00Z"
    }
  ]
}
```

---

## 7) Hospital APIs

### `GET /api/hospitals`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "name": "Bệnh viện Trung ương",
      "address": "Hà Nội",
      "phone": "0123456789",
      "email": "contact@hospital.com",
      "avatar": "/images/hospital.png",
      "rating": 4.7,
      "reviewCount": 120,
      "description": "Bệnh viện đa khoa...",
      "doctorCount": 25,
      "serviceCount": 3
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 20,
    "totalPages": 2
  }
}
```

---

### `GET /api/hospitals/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "name": "Bệnh viện Trung ương",
  "address": "Hà Nội",
  "phone": "0123456789",
  "email": "contact@hospital.com",
  "avatar": "/images/hospital.png",
  "rating": 4.7,
  "reviewCount": 120,
  "description": "Bệnh viện đa khoa...",
  "doctorCount": 25,
  "serviceCount": 3,
  "services": ["Khám tổng quát", "Chụp X-quang", "Siêu âm"],
  "doctors": [
    {
      "id": 1,
      "fullName": "Dr. Nguyễn Văn A",
      "specialty": "Cardiology",
      "avatar": "/avatars/doctor-1.jpg",
      "rating": 4.8
    }
  ]
}
```

---

## 8) Specialty API

### `GET /api/specialties`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "name": "Cardiology",
      "vietnamName": "Tim mạch",
      "icon": "heart",
      "doctorCount": 25,
      "description": "Chuyên khoa về tim mạch"
    }
  ]
}
```

---

## 9) Appointment APIs

### `POST /api/appointments`

**Response `201 Created`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "doctorPhone": "0123456789",
  "hospitalId": 1,
  "hospitalName": "Bệnh viện Trung ương",
  "hospitalAddress": "Hà Nội",
  "appointmentDate": "2026-05-01",
  "appointmentTime": "08:30:00",
  "status": "PENDING",
  "reason": "Khám tim mạch",
  "notes": "Ghi chú",
  "price": 300000,
  "paymentStatus": "UNPAID",
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

### `GET /api/appointments`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "doctorId": 2,
      "doctorName": "Dr. Nguyễn Văn A",
      "doctorPhone": "0123456789",
      "hospitalId": 1,
      "hospitalName": "Bệnh viện Trung ương",
      "hospitalAddress": "Hà Nội",
      "appointmentDate": "2026-05-01",
      "appointmentTime": "08:30:00",
      "status": "PENDING",
      "reason": "Khám tim mạch",
      "notes": "Ghi chú",
      "price": 300000,
      "paymentStatus": "UNPAID",
      "createdAt": "2026-04-28T08:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

### `GET /api/appointments/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "doctorPhone": "0123456789",
  "hospitalId": 1,
  "hospitalName": "Bệnh viện Trung ương",
  "hospitalAddress": "Hà Nội",
  "appointmentDate": "2026-05-01",
  "appointmentTime": "08:30:00",
  "status": "PENDING",
  "reason": "Khám tim mạch",
  "notes": "Ghi chú",
  "price": 300000,
  "paymentStatus": "UNPAID",
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

### `PUT /api/appointments/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "doctorPhone": "0123456789",
  "hospitalId": 1,
  "hospitalName": "Bệnh viện Trung ương",
  "hospitalAddress": "Hà Nội",
  "appointmentDate": "2026-05-02",
  "appointmentTime": "09:00:00",
  "status": "PENDING",
  "reason": "Khám tim mạch",
  "notes": "Ghi chú",
  "price": 300000,
  "paymentStatus": "UNPAID",
  "createdAt": "2026-04-28T08:00:00Z",
  "message": "Lịch khám đã được cập nhật"
}
```

---

### `DELETE /api/appointments/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "doctorPhone": "0123456789",
  "hospitalId": 1,
  "hospitalName": "Bệnh viện Trung ương",
  "hospitalAddress": "Hà Nội",
  "appointmentDate": "2026-05-01",
  "appointmentTime": "08:30:00",
  "status": "CANCELLED",
  "reason": "Khám tim mạch",
  "notes": "Ghi chú\nCancel reason: Bận",
  "price": 300000,
  "paymentStatus": "UNPAID",
  "createdAt": "2026-04-28T08:00:00Z",
  "message": "Lịch khám đã được hủy"
}
```

---

### `GET /api/appointments/{doctorId}/available-slots?startDate=...&endDate=...`

**Response `200 OK`**

```json
{
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "availableSlots": [
    {
      "date": "2026-05-01",
      "slots": ["08:00", "08:30", "09:00"]
    },
    {
      "date": "2026-05-02",
      "slots": ["08:00", "09:30"]
    }
  ]
}
```

---

## 10) Payment APIs

### `POST /api/payments/process`

**Response `200 OK`**

```json
{
  "id": 1,
  "appointmentId": 1,
  "doctorName": "Dr. Nguyễn Văn A",
  "amount": 300000,
  "paymentMethod": "CARD",
  "status": "SUCCESS",
  "transactionId": "TXN1A2B3C4D5E6F",
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

### `GET /api/payments`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "appointmentId": 1,
      "doctorName": "Dr. Nguyễn Văn A",
      "amount": 300000,
      "paymentMethod": "CARD",
      "status": "SUCCESS",
      "transactionId": "TXN1A2B3C4D5E6F",
      "createdAt": "2026-04-28T08:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

## 11) Review APIs

### `POST /api/reviews`

**Response `201 Created`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "userId": 1,
  "rating": 5,
  "comment": "Bác sĩ rất tốt",
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

### `GET /api/reviews`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "doctorId": 2,
      "doctorName": "Dr. Nguyễn Văn A",
      "userId": 1,
      "rating": 5,
      "comment": "Bác sĩ rất tốt",
      "createdAt": "2026-04-28T08:00:00Z"
    }
  ]
}
```

---

### `PUT /api/reviews/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "doctorId": 2,
  "doctorName": "Dr. Nguyễn Văn A",
  "userId": 1,
  "rating": 4,
  "comment": "Cập nhật review",
  "createdAt": "2026-04-28T08:00:00Z",
  "updatedAt": "2026-04-28T08:10:00Z"
}
```

---

### `DELETE /api/reviews/{id}`

**Response `200 OK`**

```json
{
  "message": "Đánh giá đã được xóa"
}
```

---

## 12) Medical record APIs

### `GET /api/medical-records`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "appointmentId": 1,
      "doctorName": "Dr. Nguyễn Văn A",
      "visitDate": "2026-05-01",
      "diagnosis": "Viêm họng",
      "treatment": "Uống thuốc 5 ngày",
      "notes": "Tái khám sau 1 tuần",
      "attachments": []
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

---

### `POST /api/medical-records`

**Response `201 Created`**

```json
{
  "id": 1,
  "appointmentId": 1,
  "doctorName": "Dr. Nguyễn Văn A",
  "visitDate": "2026-05-01",
  "diagnosis": "Viêm họng",
  "treatment": "Uống thuốc 5 ngày",
  "notes": "Tái khám sau 1 tuần",
  "attachments": [],
  "createdAt": "2026-04-28T08:00:00Z"
}
```

---

## 13) Admin APIs

### `GET /api/admin/dashboard`

**Response `200 OK`**

```json
{
  "statistics": {
    "totalUsers": 100,
    "totalDoctors": 20,
    "totalAppointments": 350,
    "totalRevenue": 150000000,
    "monthlyAppointments": 350,
    "monthlyRevenue": 150000000,
    "appointmentStatuses": {
      "PENDING": 10,
      "CONFIRMED": 20,
      "COMPLETED": 300,
      "CANCELLED": 20
    }
  }
}
```

---

### `GET /api/admin/doctors`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "fullName": "Dr. Nguyễn Văn A",
      "email": "doctor@example.com",
      "specialty": "Cardiology",
      "hospital": "Bệnh viện Trung ương",
      "appointments": 50,
      "rating": 4.8
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

### `DELETE /api/admin/doctors/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "message": "Bác sĩ đã được xóa"
}
```

---

### `GET /api/admin/appointments`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "userId": 1,
      "userName": "Nguyễn Văn B",
      "doctorId": 2,
      "doctorName": "Dr. Nguyễn Văn A",
      "appointmentDate": "2026-05-01",
      "appointmentTime": "08:30:00",
      "status": "PENDING",
      "price": 300000
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

### `PUT /api/admin/appointments/{id}`

**Response `200 OK`**

```json
{
  "id": 1,
  "status": "CONFIRMED",
  "message": "Trạng thái lịch khám đã được cập nhật"
}
```

---

### `GET /api/admin/users`

**Response `200 OK`**

```json
{
  "data": [
    {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "createdAt": "2026-04-28T08:00:00Z",
      "appointments": 2
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

### `GET /api/admin/users/{login}`

**Response `200 OK`**

```json
{
  "id": 1,
  "login": "user@example.com",
  "firstName": "Nguyễn Văn",
  "lastName": "A",
  "email": "user@example.com",
  "imageUrl": "/img/avatar.png",
  "activated": true,
  "langKey": "en",
  "createdBy": "admin",
  "createdDate": "2026-04-28T08:00:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-04-28T08:10:00Z",
  "authorities": ["ROLE_USER"]
}
```

---

### `POST /api/admin/users`

**Response `201 Created`**

Trả về **entity `User`**:

```json
{
  "id": 1,
  "login": "user@example.com",
  "firstName": "Nguyễn Văn",
  "lastName": "A",
  "email": "user@example.com",
  "activated": true,
  "langKey": "en",
  "imageUrl": "/img/avatar.png",
  "phoneNumber": "0123456789",
  "dateOfBirth": "1998-01-01",
  "gender": "male",
  "address": "Hà Nội",
  "healthInsurance": "BH123456",
  "createdBy": "admin",
  "createdDate": "2026-04-28T08:00:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-04-28T08:00:00Z"
}
```

> `password`, `activationKey`, `resetKey` không được trả ra ngoài.

---

### `PUT /api/admin/users` hoặc `PUT /api/admin/users/{login}`

**Response `200 OK`**

```json
{
  "id": 1,
  "login": "user@example.com",
  "firstName": "Nguyễn Văn",
  "lastName": "A",
  "email": "user@example.com",
  "imageUrl": "/img/avatar.png",
  "activated": true,
  "langKey": "en",
  "createdBy": "admin",
  "createdDate": "2026-04-28T08:00:00Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-04-28T08:10:00Z",
  "authorities": ["ROLE_USER"]
}
```

---

### `DELETE /api/admin/users/{id}`

**Response `200 OK`**

```json
{
  "id": "1",
  "message": "Người dùng đã được xóa"
}
```

---

## 14) Authority APIs

### `POST /api/authorities`

**Response `201 Created`**

```json
{
  "name": "ROLE_ADMIN"
}
```

---

### `GET /api/authorities`

**Response `200 OK`**

```json
[{ "name": "ROLE_ADMIN" }, { "name": "ROLE_USER" }]
```

---

### `GET /api/authorities/{id}`

**Response `200 OK`**

```json
{
  "name": "ROLE_ADMIN"
}
```

---

### `DELETE /api/authorities/{id}`

**Response `204 No Content`**

- Không có body

---

## 15) Response lỗi chung

Handler lỗi chung nằm ở [./src/main/java/hospital/web/rest/errors/ApiExceptionHandler.java](./src/main/java/hospital/web/rest/errors/ApiExceptionHandler.java).

### Validation error

```json
{
  "error": "Invalid input",
  "code": "INVALID_INPUT",
  "details": [
    {
      "field": "email",
      "message": "must not be blank"
    }
  ]
}
```

### Bad credentials

```json
{
  "error": "Invalid credentials",
  "code": "INVALID_CREDENTIALS"
}
```

### Forbidden

```json
{
  "error": "Forbidden",
  "code": "FORBIDDEN"
}
```

### Unauthorized

```json
{
  "error": "Unauthorized",
  "code": "UNAUTHORIZED"
}
```

### Invalid input

```json
{
  "error": "Amount must be greater than 0",
  "code": "INVALID_INPUT"
}
```

### Not found

```json
{
  "error": "Doctor not found",
  "code": "NOT_FOUND"
}
```

### Internal error

```json
{
  "error": "Something went wrong",
  "code": "INTERNAL_ERROR"
}
```

---

## 16) Ghi chú nhanh

- Các endpoint trả `PageResponseDTO` luôn có dạng:

```json
{
  "data": [],
  "pagination": {
    "page": 1,
    "limit": 10,
    "total": 0,
    "totalPages": 0
  }
}
```

- Nhiều endpoint dùng `Map<String, Object>` nên response có thể thay đổi nhẹ theo dữ liệu thực tế.
- Một số API như `POST /api/register`, `POST /api/account`, `POST /api/account/change-password` **không có body response**.
