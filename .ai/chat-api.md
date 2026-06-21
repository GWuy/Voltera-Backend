# Chat Service API Documentation

Tài liệu này hướng dẫn cách sử dụng Chat API cho hệ thống Voltera, bao gồm các REST API và kết nối WebSocket (STOMP).

## 1. Tổng quan WebSocket

Hệ thống sử dụng STOMP qua WebSocket để truyền tải tin nhắn thời gian thực.

- **WebSocket Endpoint:** `/ws`
- **Giao thức:** STOMP (với SockJS hỗ trợ fallback)
- **Xác thực:** Gửi JWT Token qua header `Authorization` khi kết nối (CONNECT frame).
  - Header: `Authorization: Bearer <your_jwt_token>`

### 1.1 Destinations (Lộ trình tin nhắn)

- **Subscribe (Nhận tin nhắn):** `/user/queue/messages`
- **Send (Gửi tin nhắn):** `/app/chat.send`

---

## 2. REST API Reference

Các API cơ bản để quản lý cuộc trò chuyện và lịch sử tin nhắn.
**Base URL:** `/api/v1/chat`

### 2.1 Lấy danh sách cuộc trò chuyện
Trả về danh sách các cuộc trò chuyện của người dùng hiện tại kèm theo tin nhắn cuối cùng và số tin nhắn chưa đọc.

- **Endpoint:** `GET /conversations`
- **Headers:** `Authorization: Bearer <token>`
- **Phản hồi mẫu (200 OK):**
```json
[
  {
    "id": 1,
    "type": "PRIVATE",
    "otherUserId": 5,
    "otherUserName": "Nguyễn Văn A",
    "otherUserAvatar": "https://example.com/avatar.png",
    "lastMessage": "Chào bạn, xe đã sạc xong chưa?",
    "lastMessageTime": "2024-03-20T10:30:00Z",
    "unreadCount": 2
  }
]
```

### 2.2 Lấy lịch sử tin nhắn
Lấy danh sách tin nhắn giữa người dùng hiện tại và một người dùng khác. Hỗ trợ phân trang.

- **Endpoint:** `GET /messages/{receiverId}`
- **Query Params:**
  - `page` (mặc định: 0)
  - `size` (mặc định: 20)
- **Headers:** `Authorization: Bearer <token>`
- **Phản hồi mẫu (200 OK):**
```json
{
  "content": [
    {
      "id": 105,
      "conversationId": 1,
      "senderId": 5,
      "senderName": "Nguyễn Văn A",
      "senderAvatar": "...",
      "content": "Chào bạn!",
      "messageType": "TEXT",
      "createdAt": "2024-03-20T10:30:00Z",
      "attachments": []
    }
  ],
  "pageable": { ... },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2.3 Đánh dấu tin nhắn đã đọc
Đánh dấu tất cả tin nhắn từ một người gửi cụ thể là đã được người dùng hiện tại đọc.

- **Endpoint:** `PUT /messages/{senderId}/read`
- **Headers:** `Authorization: Bearer <token>`
- **Phản hồi:** `200 OK`

---

## 3. WebSocket Real-time Flow

### 3.1 Gửi tin nhắn
Gửi một tin nhắn mới tới đích `/app/chat.send`.

**Payload:**
```json
{
  "receiverId": 5,
  "content": "Chào bạn, tôi muốn hỏi về trạm sạc.",
  "messageType": "TEXT",
  "attachments": [
    {
      "fileUrl": "...",
      "fileType": "IMAGE"
    }
  ]
}
```

### 3.2 Nhận tin nhắn
Khi có tin nhắn mới, Server sẽ gửi dữ liệu tới `/user/queue/messages` của cả người gửi và người nhận.

**Cấu trúc dữ liệu nhận được:**
Giống như đối tượng `ChatMessageResponse` trong API lịch sử tin nhắn.

---

## 4. Ví dụ Code Client (JavaScript - StompJS)

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

const headers = {
    'Authorization': 'Bearer ' + token
};

stompClient.connect(headers, function (frame) {
    console.log('Connected: ' + frame);

    // 1. Đăng ký nhận tin nhắn
    stompClient.subscribe('/user/queue/messages', function (message) {
        const chatMessage = JSON.parse(message.body);
        console.log('Tin nhắn mới:', chatMessage);
        // Hiển thị tin nhắn lên UI
    });

    // 2. Gửi tin nhắn
    const sendMessage = (receiverId, text) => {
        const payload = {
            receiverId: receiverId,
            content: text,
            messageType: 'TEXT'
        };
        stompClient.send("/app/chat.send", headers, JSON.stringify(payload));
    };
});
```

## 5. Lưu ý
- Đảm bảo Token JWT còn hạn khi kết nối WebSocket.
- Hệ thống tự động tạo cuộc trò chuyện (Conversation) nếu chưa tồn tại giữa 2 người dùng khi gửi tin nhắn lần đầu.
- `unreadCount` được tính dựa trên `lastReadMessageId` lưu trong bảng `conversation_participant`.
