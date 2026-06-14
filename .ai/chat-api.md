# Chat API Documentation

This document describes the API endpoints and websocket events used for the real-time chat functionality.

## Data Models

### Message
```json
{
  "id": "uuid",
  "senderId": "uuid",
  "receiverId": "uuid", 
  "content": "string",
  "createdAt": "timestamp",
  "status": "SENT | DELIVERED | READ"
}
```

### MessageAttachment
```json
{
  "id": "uuid",
  "messageId": "uuid",
  "fileUrl": "string",
  "fileType": "IMAGE | DOCUMENT | VIDEO",
  "fileName": "string",
  "fileSize": "number"
}
```

## REST API Endpoints

### 1. Get Chat History
Retrieves previous messages between the current user and another user.

**Endpoint:** `GET /api/v1/chat/messages/{receiverId}`

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Number of messages per page (default: 20)

**Response:**
```json
{
  "status": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "...",
        "senderId": "...",
        "receiverId": "...",
        "content": "Hello",
        "createdAt": "2023-10-25T10:00:00Z",
        "status": "READ"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 50,
    "totalPages": 3,
    "last": false
  }
}
```

### 2. Mark Messages as Read
Marks all unread messages from a specific user as read.

**Endpoint:** `PUT /api/v1/chat/messages/{senderId}/read`

**Response:**
```json
{
  "status": 200,
  "message": "Messages marked as read",
  "data": null
}
```

### 3. Get User Conversations
Retrieves all conversations for the current user.

**Endpoint:** `GET /api/v1/chat/conversations`

**Response:**
```json
{
  "status": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "type": "PRIVATE",
      "otherUserId": 5,
      "otherUserName": "John Doe",
      "otherUserAvatar": "https://...",
      "lastMessage": "See you soon!",
      "lastMessageTime": "2023-10-25T10:00:00Z",
      "unreadCount": 3
    }
  ]
}
```

### 4. Upload Attachment
Uploads a file to be attached to a message.

**Endpoint:** `POST /api/v1/chat/attachments`

**Headers:**
- `Content-Type: multipart/form-data`

**Body:**
- `file`: (File) The file to upload

**Response:**
```json
{
  "status": 200,
  "message": "Upload successful",
  "data": {
    "fileUrl": "https://...",
    "fileName": "document.pdf",
    "fileType": "DOCUMENT",
    "fileSize": 1024500
  }
}
```

## WebSocket Connection

The chat uses STOMP over WebSockets for real-time communication.

**Connection Endpoint:** `/ws`
**Authentication:** Pass the JWT token in the connection headers or query string (depending on your setup).

### Subscriptions (Listen to events)

**1. Personal Messages Queue**
Subscribe to this queue to receive new messages directed to the current user.
- **Destination:** `/user/queue/messages`
- **Payload:** ChatMessageResponse Object

**2. Message Status Updates**
Subscribe to this queue to know when your sent messages are read by the recipient.
- **Destination:** `/user/queue/status`
- **Payload:**
```json
{
  "messageIds": ["id1", "id2"],
  "status": "READ"
}
```

### Publishing (Send events)

**1. Send a Message**
- **Destination:** `/app/chat.send`
- **Payload:**
```json
{
  "receiverId": 5,
  "content": "Hello there!",
  "messageType": "TEXT",
  "attachments": [
     {
       "fileUrl": "https://...",
       "fileType": "IMAGE",
       "fileName": "photo.jpg",
       "fileSize": 2048000
     }
  ]
}
```

**2. Send Typing Indicator (Optional)**
- **Destination:** `/app/chat.typing`
- **Payload:**
```json
{
  "receiverId": 5,
  "isTyping": true
}
```

## Implementation Examples

### Frontend JavaScript (WebSocket Client)

```javascript
// Connect to WebSocket
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/ws',
    reconnectDelay: 5000,
    connectHeaders: {
        Authorization: 'Bearer ' + localStorage.getItem('token')
    }
});

stompClient.onConnect = function() {
    console.log('Connected!');
    
    // Subscribe to receive messages
    stompClient.subscribe('/user/queue/messages', function(message) {
        const chatMessage = JSON.parse(message.body);
        console.log('Received message:', chatMessage);
        displayMessage(chatMessage);
    });
    
    // Subscribe to status updates
    stompClient.subscribe('/user/queue/status', function(message) {
        const status = JSON.parse(message.body);
        markMessagesAsRead(status);
    });
};

stompClient.onError = function(error) {
    console.error('WebSocket error:', error);
};

stompClient.activate();

// Send a message
function sendMessage(receiverId, content) {
    const message = {
        receiverId: receiverId,
        content: content,
        messageType: 'TEXT',
        attachments: []
    };
    
    stompClient.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(message),
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('token')
        }
    });
}

// Disconnect
function disconnect() {
    if (stompClient !== null) {
        stompClient.deactivate();
    }
}
```

### REST API Usage (Fetch Example)

```javascript
// Get conversations
async function getConversations() {
    const response = await fetch('/api/v1/chat/conversations', {
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('token')
        }
    });
    return await response.json();
}

// Get message history
async function getMessageHistory(receiverId, page = 0, size = 20) {
    const response = await fetch(
        `/api/v1/chat/messages/${receiverId}?page=${page}&size=${size}`,
        {
            headers: {
                Authorization: 'Bearer ' + localStorage.getItem('token')
            }
        }
    );
    return await response.json();
}

// Mark as read
async function markAsRead(senderId) {
    const response = await fetch(`/api/v1/chat/messages/${senderId}/read`, {
        method: 'PUT',
        headers: {
            Authorization: 'Bearer ' + localStorage.getItem('token')
        }
    });
    return await response.json();
}
```

### Backend Usage (Java)

```java
// Inject ChatService
@Autowired
private ChatService chatService;

// Get user conversations
String token = "Bearer eyJ...";
List<ConversationResponse> conversations = chatService.getUserConversations(token);

// Get message history
String token = "Bearer eyJ...";
Page<ChatMessageResponse> history = chatService.getMessageHistory(
    token, 
    receiverId, 
    0,  // page
    20  // size
);

// Mark as read
chatService.markMessagesAsRead(token, senderId);

// Send message via service
ChatMessageRequest request = new ChatMessageRequest();
request.setReceiverId(5);
request.setContent("Hello!");
request.setMessageType("TEXT");

ChatMessageResponse response = chatService.sendMessage(token, request);
```

## File Structure

```
src/main/java/com/g_wuy/swp391/voltera/
├── config/
│   └── WebSocketConfig.java          # WebSocket STOMP configuration
├── controller/
│   └── ChatController.java           # REST endpoints + WebSocket handlers
├── service/
│   └── ChatService.java              # Business logic for chat operations
├── repository/
│   ├── MessageRepository.java        # Message data access
│   ├── MessageAttachmentRepository.java
│   ├── ConversationRepository.java
│   └── ConversationParticipantRepository.java
├── entity/
│   ├── Message.java                  # Message database entity
│   ├── MessageAttachment.java
│   ├── Conversation.java
│   └── ConversationParticipant.java
├── model/
│   ├── request/
│   │   └── ChatMessageRequest.java
│   └── response/
│       ├── ChatMessageResponse.java
│       └── ConversationResponse.java
```

## Error Handling

All errors return with appropriate HTTP status codes:
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User or resource not found
- `500 Internal Server Error` - Server-side errors

Example error response:
```json
{
  "status": 404,
  "message": "Receiver not found",
  "data": null
}
```

## Security Notes

- JWT token is required for all endpoints and WebSocket connections
- Token is extracted from `Authorization: Bearer <token>` header
- WebSocket connections use STOMP protocol with header-based authentication
- CORS is enabled for `*` origins (configure as needed for production)
- All user IDs are validated against JWT claims to prevent unauthorized access
- Messages are associated with authenticated users only

## Dependencies Added to pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

## Testing the Chat API

### 1. Using Postman (REST endpoints)
- Set Authorization header: `Bearer <your_jwt_token>`
- GET `/api/v1/chat/conversations` - List all conversations
- GET `/api/v1/chat/messages/{receiverId}?page=0&size=20` - Get message history
- PUT `/api/v1/chat/messages/{senderId}/read` - Mark messages as read

### 2. Using WebSocket (STOMP)
- Connect to `ws://localhost:8080/ws` with STOMP client
- Subscribe to `/user/queue/messages`
- Send message to `/app/chat.send` with JSON payload
- Listen for incoming messages on subscribed queues
