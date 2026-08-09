# FOCAL Backend API

이 문서는 현재 Controller와 DTO를 기준으로 작성한 API 요약 명세입니다.

## 공통 규칙

### Base URL

```text
http://localhost:8080
```

### 인증

로그인 성공 시 Access Token은 응답 본문으로, Refresh Token은 `HttpOnly; SameSite=Strict; Path=/` 쿠키로 전달됩니다.

```http
Authorization: Bearer <access-token>
```

다음 API는 Access Token 없이 호출할 수 있습니다.

- `POST /users/signup`
- `POST /users/login`
- `POST /users/token/refresh` — Refresh Token 쿠키 사용
- `POST /images/users`
- `POST /images/posts`

### 공통 응답

```json
{
  "message": "request_success",
  "data": {}
}
```

검증·인증·도메인 오류도 같은 구조를 사용하며 `data`는 `null`입니다.

```json
{
  "message": "error description",
  "data": null
}
```

| 상태 | 의미 |
| --- | --- |
| `400 Bad Request` | 요청 검증 실패, 잘못된 인자, 현재 비밀번호 불일치 |
| `401 Unauthorized` | 인증 정보 누락·만료, 로그인 실패 |
| `403 Forbidden` | 작성자 등 필요한 권한 없음 |
| `404 Not Found` | 리소스 없음 또는 유효하지 않은 리소스 상태 |
| `409 Conflict` | 중복 이메일 |
| `500 Internal Server Error` | 처리되지 않은 서버 오류 |

## 회원·인증 API

### 회원가입

```http
POST /users/signup
Content-Type: application/json
```

```json
{
  "nickname": "focal",
  "email": "focal@example.com",
  "password": "password",
  "profileImg": "https://example.com/profile.jpg"
}
```

- `nickname`: 필수, 최대 10자
- `email`: 필수, 이메일 형식
- `password`: 필수
- `profileImg`: 선택

성공: `201 Created`

```json
{
  "message": "request_success",
  "data": {
    "id": 1,
    "nickname": "focal"
  }
}
```

### 로그인

```http
POST /users/login
Content-Type: application/json
```

```json
{
  "email": "focal@example.com",
  "password": "password"
}
```

성공: `200 OK`와 Refresh Token 쿠키

```json
{
  "message": "request_success",
  "data": {
    "id": 1,
    "nickname": "focal",
    "email": "focal@example.com",
    "profileImg": "https://example.com/profile.jpg",
    "token": {
      "accessToken": "eyJ...",
      "expiresIn": 3600
    }
  }
}
```

### Access Token 재발급

```http
POST /users/token/refresh
Cookie: refreshToken=<refresh-token>
```

성공 응답의 `data`는 `accessToken`, `expiresIn`을 포함합니다. Refresh Token이 회전되면 새 쿠키도 함께 설정됩니다.

### 내 정보 조회

```http
GET /users/me
Authorization: Bearer <access-token>
```

```json
{
  "message": "request_success",
  "data": {
    "id": 1,
    "nickname": "focal",
    "email": "focal@example.com",
    "profileImg": "https://example.com/profile.jpg"
  }
}
```

### 내 정보 수정

```http
PATCH /users/me
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "nickname": "new-name",
  "profileImg": "https://example.com/new-profile.jpg"
}
```

- `nickname`: null 불가, 최대 10자
- `profileImg`: 선택, 값이 있으면 URL 형식

### 비밀번호 변경

```http
PATCH /users/me/password
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "currentPassword": "old-password",
  "newPassword": "new-password"
}
```

### 로그아웃

```http
POST /users/logout
Authorization: Bearer <access-token>
Cookie: refreshToken=<refresh-token>
```

저장된 Refresh Token을 제거합니다. 성공: `200 OK`.

### 회원 탈퇴

```http
DELETE /users/me
Authorization: Bearer <access-token>
Cookie: refreshToken=<refresh-token>
```

회원을 소프트 삭제하고 Refresh Token을 제거합니다. 성공: `204 No Content`.

## 게시글 API

### 공통 장소 요청

게시글 작성·수정의 `location`은 선택 필드입니다.

```json
{
  "providerPlaceId": "kakao-place-id",
  "placeName": "장소 이름",
  "roadAddressName": "도로명 주소",
  "latitude": 37.566500,
  "longitude": 126.978000
}
```

- `providerPlaceId`, `placeName`, `latitude`, `longitude`: 장소를 보낼 경우 필수
- `placeName`: 최대 100자
- `roadAddressName`: 최대 255자
- `latitude`: -90 이상 90 이하
- `longitude`: -180 이상 180 이하

### 게시글 작성

```http
POST /posts
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "title": "서울의 여름",
  "content": "오늘의 사진입니다.",
  "contentImg": "https://example.com/post.jpg",
  "tags": ["서울", "여름"],
  "location": {
    "providerPlaceId": "place-1",
    "placeName": "서울광장",
    "roadAddressName": "서울 중구 세종대로 110",
    "latitude": 37.566500,
    "longitude": 126.978000
  }
}
```

- `title`: 필수, 최대 26자
- `content`: 필수
- `contentImg`: 필수, URL 형식
- `tags`: 선택, 최대 5개, 각 태그 최대 20자
- `location`: 선택

성공: `201 Created`.

### 게시글 상세 조회

```http
GET /posts/{postId}
Authorization: Bearer <access-token>
```

응답에는 제목·본문·이미지·작성자, 집계 수치, `isMine`, `isLiked`, 태그와 장소가 포함됩니다. 상세 조회 시 조회수가 증가합니다.

### 게시글 목록 조회

```http
GET /posts?filter=RECENT
Authorization: Bearer <access-token>
```

| `filter` | 설명 |
| --- | --- |
| `RECENT` | 최신순 최대 10건, 기본값 |
| `POPULAR` | 좋아요순 최대 10건 |
| `MINE` | 로그인 사용자의 게시글 최신순 |

응답의 각 게시글에는 로그인 사용자의 `isLiked`, 태그 목록과 장소 정보가 포함됩니다.

### 해시태그·기간 검색

```http
GET /posts/search?tag=서울&startDate=2026-08-01&endDate=2026-08-09
Authorization: Bearer <access-token>
```

- `tag`: 필수
- `startDate`, `endDate`: `yyyy-MM-dd`
- 시작일 00:00 이상, 종료일 다음 날 00:00 미만의 게시글을 최신순으로 최대 10건 반환

### 게시글 수정

```http
PATCH /posts/{postId}
Authorization: Bearer <access-token>
Content-Type: application/json
```

요청 구조는 게시글 작성과 동일합니다. 단, `contentImg`는 선택입니다. 작성자만 수정할 수 있으며 태그와 장소도 함께 갱신합니다.

### 게시글 삭제

```http
DELETE /posts/{postId}
Authorization: Bearer <access-token>
```

작성자만 삭제할 수 있습니다. 성공: `204 No Content`.

## 댓글 API

### 댓글 작성

```http
POST /posts/{postId}/comments
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "comment": "좋은 사진이에요."
}
```

`comment`는 필수입니다. 성공: `201 Created`.

### 댓글 목록 조회

```http
GET /posts/{postId}/comments
Authorization: Bearer <access-token>
```

각 댓글에는 작성자 ID·닉네임·프로필, 생성 시각, 내용과 `isMine`이 포함됩니다.

### 댓글 수정

```http
PATCH /comments/{commentId}
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "comment": "수정한 댓글입니다."
}
```

작성자만 수정할 수 있습니다. 성공: `200 OK`.

### 댓글 삭제

```http
DELETE /comments/{commentId}
Authorization: Bearer <access-token>
```

작성자만 삭제할 수 있습니다. 성공: `204 No Content`.

## 좋아요 API

### 좋아요 등록·복구

```http
POST /posts/{postId}/like
Authorization: Bearer <access-token>
```

```json
{
  "message": "request_success",
  "data": {
    "likeId": 1,
    "isLiked": true,
    "likeCount": 12
  }
}
```

이전에 취소한 좋아요가 있으면 기존 행을 복구합니다.

### 좋아요 취소

```http
DELETE /posts/{postId}/like
Authorization: Bearer <access-token>
```

Like 행을 소프트 삭제하고 갱신된 좋아요 수를 반환합니다.

## 이미지 API

`multipart/form-data`의 `file` 필드로 이미지를 업로드합니다. 애플리케이션 전체 요청 크기 제한은 15MB입니다.

```http
POST /images/users
Content-Type: multipart/form-data
```

```http
POST /images/posts
Content-Type: multipart/form-data
```

성공: `201 Created`.

```json
{
  "message": "request_success",
  "data": {
    "imageUrl": "http://localhost:8080/images/posts/generated-file-name.jpg"
  }
}
```
