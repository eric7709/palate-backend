# Palate Restaurant Management System - Complete Data Flow Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture & Stack](#architecture--stack)
3. [Core Entity Relationships](#core-entity-relationships)
4. [Authentication & Security Flow](#authentication--security-flow)
5. [Order Management Data Flow](#order-management-data-flow)
6. [Real-time Communication (WebSocket)](#real-time-communication-websocket)
7. [Payment Integration (Monnify)](#payment-integration-monnify)
8. [Analytics & Reporting](#analytics--reporting)
9. [Table & Room Management](#table--room-management)
10. [Menu Item & Category Management](#menu-item--category-management)
11. [Customer Management](#customer-management)
12. [Notifications & Alerts](#notifications--alerts)
13. [Scheduled Tasks](#scheduled-tasks)
14. [Data Persistence & Repositories](#data-persistence--repositories)
15. [Request/Response Flow Examples](#requestresponse-flow-examples)

---

## System Overview

Palate is a comprehensive **Spring Boot 3.2.5-based Restaurant Management System** that handles orders, payments, table allocations, staff management, and real-time analytics. The system provides APIs for multiple roles (Admins, Managers, Waiters, Cashiers, Customers) with different permission levels and uses WebSocket for real-time communication.

### Key Features:
- Order creation and management with status tracking
- Real-time order updates via WebSocket (STOMP)
- Payment processing through Monnify payment gateway
- Dynamic analytics and sales reporting
- Table and room management with QR codes
- Staff role-based access control
- Order timeouts and automatic notifications
- Customer order history tracking

---

## Architecture & Stack

### Technology Stack:
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 21
- **ORM:** Spring Data JPA + Hibernate
- **Databases:** PostgreSQL (Production), H2 (Development/Testing)
- **Authentication:** JWT (JSON Web Tokens) with role-based authorization
- **Real-time:** Spring WebSocket with STOMP protocol + SockJS
- **Payment:** Monnify API integration
- **Security:** Spring Security + BCrypt password encoding
- **QR Generation:** ZXing library
- **Validation:** Jakarta Validation API
- **Build:** Maven
- **Utilities:** Lombok, MapStruct

### Architecture Pattern:
```
Controller Layer (REST API) 
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL/H2)
    ↓
WebSocket/Events (Real-time Updates)
```

---

## Core Entity Relationships

### ER Diagram (Text Representation):

```
                          ┌─────────────────┐
                          │    Account      │
                          │   (Employees)   │
                          └────────┬────────┘
                                   │
                   ┌───────────────┼───────────────┐
                   │               │               │
                   ▼               ▼               ▼
            ┌────────────┐  ┌────────────┐  ┌─────────────┐
            │   Orders   │  │   Tables   │  │    Rooms    │
            └────────────┘  └────────────┘  └─────────────┘
                   │               │
                   ▼               ▼
            ┌────────────┐  ┌──────────────────┐
            │ OrderItems │  │ TableAllocations │
            └────────────┘  └──────────────────┘
                   │
                   ▼
            ┌────────────┐
            │  MenuItems │
            └────────────┘
                   │
                   ▼
            ┌──────────────┐
            │  Categories  │
            └──────────────┘

            ┌────────────┐
            │ Customers  │
            └────────────┘
                   │
                   ├─────────(orders)─────────► Orders
                   │
                   └─────(notifications)──────► Notifications

            ┌─────────────────┐
            │ Notifications   │
            └─────────────────┘
                   │
                   ▼
            ┌────────────┐
            │  Accounts  │
            └────────────┘
```

### Entity Descriptions:

#### **Account (Employees/Staff)**
- Represents system users (Waiters, Cashiers, Admins, Managers)
- Fields: firstName, lastName, email, phoneNumber, password (hashed), role, status
- Relationships:
  - One-to-Many: Orders (as waiter or cashier)
  - One-to-Many: RestaurantTables (assigned as waiter/cashier)
  - One-to-Many: Rooms (assigned as cashier)
  - One-to-Many: TableAllocations (as waiter or cashier)
  - One-to-Many: Notifications (receives)
- Constraints: email and phoneNumber are unique

#### **Customer**
- Represents customer profiles
- Fields: name, title, phoneNumber, email
- Relationships:
  - One-to-Many: Orders
  - One-to-Many: Notifications (can receive order updates)
- Key: Can be created and tracked for loyalty/analytics

#### **Order**
- Central entity in the system
- Fields: total, quantity, status (PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED), invoiceNumber, note, paidAmount, isUnderpaid, remainingBalance, virtualAccountNumber, monnifyReference, virtualBankName
- Relationships:
  - Many-to-One: Account (waiter_id) - who takes the order
  - Many-to-One: Account (cashier_id) - who processes payment
  - Many-to-One: RestaurantTable - which table the order is for
  - Many-to-One: Room - which room/delivery
  - Many-to-One: Customer - who ordered
  - One-to-Many: OrderItems (cascade delete on removal)
- Constraints: status and invoiceNumber are not nullable
- Indexes: idx_orders_status_created (optimizes filtering by status and date)

#### **OrderItem**
- Line items within an order
- Fields: quantity, price, takeOut (boolean)
- Relationships:
  - Many-to-One: Order (parent, cascade delete)
  - Many-to-One: MenuItem (what item was ordered)
- Key: Bridges orders and menu items

#### **MenuItem**
- Products available for ordering
- Fields: name, description, price, status (AVAILABLE, UNAVAILABLE), imageUrl
- Relationships:
  - Many-to-One: Category (classified by)
  - One-to-Many: OrderItems (inverse)
- Constraints: name, description, price are required; status defaults to AVAILABLE

#### **Category**
- Groups menu items
- Fields: name, status (ACTIVE by default), description
- Relationships:
  - One-to-Many: MenuItems (inverse)
- Key: Organizes the menu structure

#### **RestaurantTable**
- Physical tables in the restaurant
- Fields: tableName, tableNumber (unique), capacity, qrCode, status (AVAILABLE, OCCUPIED, RESERVED)
- Relationships:
  - Many-to-One: Account (waiter_id) - assigned waiter
  - Many-to-One: Account (cashier_id) - assigned cashier
  - One-to-Many: TableAllocations
  - One-to-Many: Orders (inverse)
- Key: QR code enables customers to scan and view menu

#### **Room**
- Delivery/Private dining rooms or areas
- Fields: roomNumber (unique), floor, qrCode, status (AVAILABLE by default)
- Relationships:
  - Many-to-One: Account (cashier_id) - assigned cashier
  - One-to-Many: Orders (inverse)
- Key: Alternative to tables for delivery or private dining

#### **TableAllocation**
- Tracks staff assignment to tables for a shift
- Fields: cashierAllocatedAt, cashierDeallocatedAt, waiterAllocatedAt, waiterDeallocatedAt (timestamps)
- Relationships:
  - Many-to-One: RestaurantTable
  - Many-to-One: Account (cashier)
  - Many-to-One: Account (waiter)
- Key: Enables tracking who was responsible for which table during which time

#### **Notification**
- System alerts sent to staff
- Fields: title, type, message, isRead, readAt (timestamp)
- Relationships:
  - Many-to-One: Account (recipient)
- Key: Used for order timeouts, alerts, and status updates

---

## Authentication & Security Flow

### JWT Token Generation Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    Login Request                              │
│         POST /api/palate/auth/login                           │
│         Body: { email, password }                             │
└──────────────────┬───────────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ AuthService.login()  │
        └──────────┬───────────┘
                   │
        ┌──────────▼────────────────────────────────────┐
        │ 1. Find Account by email                      │
        │ 2. Verify password with BCrypt               │
        │ 3. Check if account status is ACTIVE         │
        └──────────┬──────────────────────────────────┘
                   │
        ┌──────────▼──────────────────────────────┐
        │ JwtUtil.generateAccessToken(account)   │
        │                                          │
        │ Claims:                                 │
        │ - subject: email                        │
        │ - firstName: account.firstName          │
        │ - role: account.role.name()             │
        │ - id: account.id                        │
        │ - issuedAt: now                         │
        │ - expiration: now + 15 minutes          │
        │ - signature: HS256 with ACCESS_SECRET   │
        └──────────┬──────────────────────────────┘
                   │
        ┌──────────▼──────────────────────────────┐
        │ JwtUtil.generateRefreshToken(account)  │
        │                                          │
        │ Claims:                                 │
        │ - subject: email                        │
        │ - id: account.id                        │
        │ - issuedAt: now                         │
        │ - expiration: now + 7 days              │
        │ - signature: HS256 with REFRESH_SECRET  │
        └──────────┬──────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ AuthResponseDTO                      │
        │ {                                    │
        │   accessToken: "jwt...",             │
        │   refreshToken: "jwt...",            │
        │   expiresIn: 900,                    │
        │   email: email,                      │
        │   role: account.role                 │
        │ }                                    │
        └──────────┬───────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ HTTP 200 Response with Tokens        │
        └──────────────────────────────────────┘
```

### Request Authorization Flow

```
┌──────────────────────────────────────────────────────────────┐
│                HTTP Request                                   │
│         Authorization: Bearer <access_token>                  │
└──────────────────┬───────────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ Security Filter Chain                │
        │ (JwtAuthenticationFilter)            │
        │ - Extract token from header          │
        └──────────┬──────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ JwtUtil.getEmail(token)              │
        │ JwtUtil.validateToken(token)        │
        │                                      │
        │ Verify:                              │
        │ - Signature (using ACCESS_SECRET)   │
        │ - Expiration                         │
        │ - Claims format                      │
        └──────────┬──────────────────────────┘
                   │
        ┌──────────▼──────────────────────────────────────┐
        │ Load Account from DB by email                   │
        │ Build Authentication from claims:              │
        │ - email (principal)                             │
        │ - role (authority)                              │
        │ - status                                        │
        └──────────┬──────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ Security Context is set              │
        │ Continue to Controller               │
        └──────────┬──────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ Authorization Check                  │
        │ (based on @RequestMapping role)      │
        │ - ADMIN, MANAGER, WAITER, CASHIER    │
        └──────────┬──────────────────────────┘
                   │
        ┌──────────▴──────────────┐
        │                         │
        │ YES                     NO
        ▼                         ▼
    ┌───────┐            ┌──────────────┐
    │Process│            │403 Forbidden │
    │Request│            └──────────────┘
    └───────┘
```

### Token Refresh Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    Refresh Request                            │
│         POST /api/palate/auth/refresh                         │
│         Body: { refreshToken: "jwt..." }                      │
└──────────────────┬───────────────────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ AuthService.refresh()                │
        │ Extract email from refresh token    │
        └──────────┬──────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ Find Account by email                │
        │ Validate account status              │
        └──────────┬──────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ Generate new Access Token            │
        │ (15 min expiration)                  │
        └──────────┬──────────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ AccessTokenResponse                  │
        │ {                                    │
        │   accessToken: "new_jwt...",         │
        │   expiresIn: 900                     │
        │ }                                    │
        └──────────────────────────────────────┘
```

### Security Configuration Details

**Public Endpoints** (No authentication required):
- POST `/api/palate/auth/login`
- POST `/api/palate/auth/refresh`
- GET `/api/palate/health`
- GET `/api/palate/menu-items/**`
- GET `/api/palate/categories/**`
- POST `/api/palate/orders` (customer can create their own orders)
- GET `/api/palate/orders/customer/**` (customer views own orders)
- GET `/api/palate/rooms/qrcode/**` (QR code scanning)
- GET `/api/palate/tables/qrcode/**` (QR code scanning)
- POST `/api/palate/customers` (customer self-registration)
- All WebSocket endpoints `/api/palate/ws/**`
- All webhook endpoints `/webhooks/**`

**Authenticated Endpoints** (Must be logged in):
- GET `/api/palate/orders` (all orders)
- GET `/api/palate/orders/{id}` (specific order)
- PATCH `/api/palate/orders/{id}` (update order status) - requires ADMIN/WAITER/CASHIER

**Role-Based Endpoints**:
- **ADMIN**: Full access to categories, menu items, accounts, tables, rooms
- **MANAGER**: View orders and customer reports
- **CASHIER**: Process payments, view table/room status
- **WAITER**: Take orders, view table assignments
- **CUSTOMER**: Place orders, view own orders

---

## Order Management Data Flow

### Complete Order Lifecycle

```
CUSTOMER INITIATES ORDER
│
├─ POST /api/palate/orders
│  Body: OrderRequestDTO {
│    tableId, roomId, customerId, waiterId, cashierId,
│    items: [{menuItemId, quantity, takeOut}, ...],
│    note: "Special requests"
│  }
│
▼
ORDER VALIDATION & CREATION
│
├─ CreateOrder.createOrder()
│  ├─ Validate request (non-null, valid items)
│  ├─ Resolve entities:
│  │  ├─ RestaurantTable (if tableId provided)
│  │  ├─ Room (if roomId provided)
│  │  ├─ Account (waiter, cashier) - verify roles
│  │  └─ Customer (create if new)
│  │
│  ├─ Create Order entity:
│  │  ├─ Set status = PENDING
│  │  ├─ Set invoiceNumber = "INV-{date}-{timestamp}"
│  │  ├─ Set table, room, cashier, waiter references
│  │  └─ Set customer reference
│  │
│  ├─ Create OrderItems for each item in request:
│  │  ├─ Resolve MenuItem
│  │  ├─ Verify MenuItem.status != UNAVAILABLE
│  │  ├─ Get MenuItem.price
│  │  ├─ Calculate: subtotal = price * quantity
│  │  └─ Accumulate: order.total += subtotal
│  │
│  ├─ Persist Order + OrderItems to DB (transaction)
│  │
│  ├─ Generate Payment Invoice (if order.total > 0):
│  │  ├─ Call MonnifyService.createOrderInvoice(
│  │  │     orderId, order.total, customer.name)
│  │  └─ On success:
│  │     ├─ Store virtualAccountNumber
│  │     ├─ Store virtualBankName
│  │     ├─ Store monnifyReference
│  │     └─ Update order in DB
│  │
│  └─ Broadcast to WebSocket
│     ├─ /topic/orders/created (all subscribers)
│     └─ /topic/orders/customer/{customerId} (customer-specific)
│
▼
ORDER RECEIVED (Status: PENDING)
│
├─ Kitchen/Staff receive WebSocket notification
│  ├─ OrderResponseDTO {
│  │  id, invoiceNumber, total, quantity, status,
│  │  table, room, waiter, cashier, items: [...]
│  │ }
│  └─ Display in kitchen display system (KDS)
│
▼
WAITER/CASHIER UPDATES ORDER STATUS
│
├─ PATCH /api/palate/orders/{id}
│  Body: UpdateOrderStatusDTO { status }
│
├─ OrderService.updateOrder()
│  ├─ Fetch Order by ID
│  ├─ Update status:
│  │  ├─ PENDING → CONFIRMED (kitchen acknowledges)
│  │  ├─ CONFIRMED → PREPARING (kitchen starts)
│  │  ├─ PREPARING → READY (food is ready)
│  │  ├─ READY → COMPLETED (customer receives)
│  │  └─ Any → CANCELLED (if needed)
│  │
│  ├─ Save Order to DB
│  │
│  └─ Broadcast to WebSocket
│     ├─ /topic/orders/updated
│     └─ /topic/orders/customer/{customerId}
│
▼
CUSTOMER RECEIVES UPDATES (WebSocket)
│
├─ Client subscribes to /topic/orders/customer/{customerId}
├─ Receives real-time status changes
└─ Updates UI: "Your order is being prepared"

┌─────────────────────────────────────────┐
│ PAYMENT PROCESSING                      │
│ (if order.status == READY/COMPLETED)    │
└─────────────────────────────────────────┘
│
├─ Cashier initiates payment
│  └─ GET /api/palate/orders/{id}
│     Returns virtualAccountNumber, monnifyReference
│
├─ Customer pays via Monnify:
│  ├─ Scan QR code or enter account number
│  ├─ Transfer funds to virtualAccountNumber
│  └─ Monnify processes transaction
│
├─ Monnify sends webhook to /webhooks/monnify
│  └─ MonnifyWebhookController processes callback:
│     ├─ Verify payment receipt
│     ├─ Update Order:
│     │  ├─ paidAmount = amount received
│     │  ├─ isUnderpaid = (paidAmount < order.total)
│     │  ├─ remainingBalance = order.total - paidAmount
│     │  └─ status = COMPLETED or flag for partial payment
│     └─ Broadcast payment confirmation
│
▼
ORDER COMPLETION
│
├─ Order.status = COMPLETED
├─ paidAmount is recorded
├─ remainingBalance (if any) is tracked
└─ Order data feeds into Analytics

┌─────────────────────────────────────────┐
│ ANALYTICS & REPORTING                   │
│ (All completed orders)                  │
└─────────────────────────────────────────┘
│
├─ Aggregated in AnalyticsRepository:
│  ├─ Total revenue (sum of paidAmount)
│  ├─ Total orders (count by date range)
│  ├─ Average order value (total / count)
│  ├─ Cancelled count & cancellation rate
│  ├─ Top items/categories by sales
│  ├─ Top waiters/cashiers by revenue
│  ├─ Top tables/rooms by revenue
│  ├─ Take-out vs dine-in split
│  └─ Time-series revenue data
│
└─ Exposed via GET /api/palate/analytics
```

### Order Filtering & Retrieval

```
GET /api/palate/orders?status=READY&waiterId=5&page=0&size=20

Query Parameters:
├─ status: OrderStatus enum
├─ waiterId: Long (filter by waiter)
├─ cashierId: Long (filter by cashier)
├─ tableId: Long (filter by table)
├─ roomId: Long (filter by room)
├─ search: String (search invoiceNumber, customer name)
├─ minTotal: Double (price range filter)
├─ maxTotal: Double (price range filter)
├─ startDate: LocalDate (created date range)
├─ endDate: LocalDate (created date range)
├─ page: int (0-indexed)
├─ size: int (results per page)
├─ sortBy: String (createdAt, updatedAt, total, quantity)
└─ sortDirection: String (asc, desc)

OrderService.getAllOrders(OrderFilterDTO filter)
├─ Build OrderSpecification with all filters
├─ Convert date range to Instant (UTC)
├─ Create Pageable with sort
├─ Execute JPA query with Specification
└─ Return OrderPageResponse {
   page: Page<OrderResponseDTO>,
   statusCounts: {
     PENDING: long,
     CONFIRMED: long,
     PREPARING: long,
     READY: long,
     COMPLETED: long,
     CANCELLED: long
   }
}
```

---

## Real-time Communication (WebSocket)

### WebSocket Architecture

```
┌────────────────────────────────────────────────────────────┐
│              WebSocket Configuration                        │
│                (WebSocketConfig.java)                       │
└─────────────────────┬──────────────────────────────────────┘
                      │
        ┌─────────────┴──────────────┐
        │                            │
        ▼                            ▼
   STOMP Endpoint              Message Broker
   /api/palate/ws              SimpleBroker (/topic)
   + SockJS fallback           - /topic/orders/created
   (for older browsers)        - /topic/orders/updated
                               - /topic/orders/customer/{id}
                               - /topic/notifications/{id}
                               - /topic/tables/status
                               - /topic/analytics/update
```

### WebSocket Connection Flow

```
CLIENT INITIATES CONNECTION
│
├─ JavaScript:
│  const client = new SockJS('/api/palate/ws');
│  const stompClient = Stomp.over(client);
│  stompClient.connect({}, onConnect);
│
▼
SERVER ACCEPTS STOMP HANDSHAKE
│
├─ WebSocketConfig registers endpoint
├─ SockJS creates HTTP fallback sessions
└─ STOMP protocol negotiated

CLIENT SUBSCRIBES TO TOPICS
│
├─ stompClient.subscribe(
│    '/topic/orders/customer/123',
│    onOrderUpdate
│  );
│
├─ stompClient.subscribe(
│    '/topic/orders/updated',
│    onAnyOrderUpdate
│  );
│
└─ stompClient.subscribe(
     '/topic/notifications/5',
     onNotification
   );
│
▼
EVENTS ARE BROADCAST FROM SERVER
│
├─ OrderEvents.broadcastCreated(orderDto)
│  ├─ messagingTemplate.convertAndSend(
│  │    '/topic/orders/created', orderDto)
│  └─ messagingTemplate.convertAndSend(
│     '/topic/orders/customer/{customerId}', orderDto)
│
├─ OrderEvents.broadcastUpdated(orderDto)
│  ├─ messagingTemplate.convertAndSend(
│  │    '/topic/orders/updated', orderDto)
│  └─ messagingTemplate.convertAndSend(
│     '/topic/orders/customer/{customerId}', orderDto)
│
└─ NotificationService.sendToAccount()
   └─ messagingTemplate.convertAndSend(
      '/topic/notifications/{accountId}', notificationDto)
│
▼
CLIENT RECEIVES UPDATES
│
├─ onOrderUpdate(message) callback
│  ├─ Parse OrderResponseDTO from message.body
│  ├─ Update local order state
│  └─ Re-render UI (Order status, items, total)
│
└─ onNotification(message) callback
   ├─ Parse Notification from message.body
   ├─ Display toast/alert
   └─ Mark notification as read (async API call)
```

### Order Event Topics

| Topic | Triggered By | Subscribers | Payload |
|-------|--------------|-------------|---------|
| `/topic/orders/created` | OrderService.createOrder() | Kitchen, Admins | OrderResponseDTO |
| `/topic/orders/updated` | OrderService.updateOrder() | All staff, Customer | OrderResponseDTO |
| `/topic/orders/customer/{id}` | Create/Update Order | Customer, Waiter | OrderResponseDTO |
| `/topic/notifications/{id}` | NotificationService | Account recipient | Notification |
| `/topic/tables/status` | Table allocation | Staff | TableAllocationResponseDTO |
| `/topic/analytics/update` | Scheduled task | Dashboard viewers | AnalyticsSummaryByDTO |

---

## Payment Integration (Monnify)

### Monnify Payment Flow

```
ORDER CREATION TRIGGERS INVOICE
│
├─ CreateOrder.createOrder()
│  └─ order.total = sum of all OrderItems
│
▼
INVOICE CREATION (if total > 0)
│
├─ CreateOrder calls:
│  MonnifyService.createOrderInvoice(orderId, total, customerName)
│
▼
AUTHENTICATE WITH MONNIFY
│
├─ MonnifyService.getAuthToken()
│  ├─ Credentials = apiKey + ":" + secretKey
│  ├─ Encode: Base64.encode(credentials)
│  ├─ POST https://sandbox.monnify.com/api/v1/auth/login
│  │  Header: Authorization: Basic <encoded>
│  ├─ Response: { responseBody: { accessToken } }
│  └─ Cache accessToken (reuse for subsequent calls)
│
▼
CREATE INVOICE
│
├─ POST https://sandbox.monnify.com/api/v1/invoice/create
│  ├─ Header: Authorization: Bearer <accessToken>
│  ├─ Body: {
│  │   amount: 50000,
│  │   currencyCode: "NGN",
│  │   contractCode: "<from config>",
│  │   invoiceReference: "ORDER-{orderId}-{timestamp}",
│  │   description: "Payment for Order #{orderId}",
│  │   customerName: customer.name,
│  │   customerEmail: "order{orderId}@palate.com",
│  │   expiryDate: now + 24 hours
│  │ }
│  │
│  └─ Response: {
│     responseBody: {
│       accountNumber: "XXXXXXXXXX",
│       bankName: "MONNIFY VIRTUAL BANK",
│       [...]
│     }
│  }
│
▼
STORE VIRTUAL ACCOUNT DETAILS
│
├─ MonnifyInvoiceResponse returned:
│  ├─ accountNumber: "XXXXXXXXXX"
│  ├─ bankName: "MONNIFY VIRTUAL BANK"
│  ├─ amountNaira: 50000
│  ├─ orderId: 1
│  └─ invoiceReference: "ORDER-1-1693132800000"
│
├─ CreateOrder updates Order entity:
│  ├─ order.virtualAccountNumber = "XXXXXXXXXX"
│  ├─ order.virtualBankName = "MONNIFY VIRTUAL BANK"
│  ├─ order.monnifyReference = "ORDER-1-1693132800000"
│  └─ save to DB
│
▼
RETURN TO CLIENT
│
├─ OrderResponseDTO includes:
│  ├─ virtualAccountNumber (display to customer)
│  ├─ virtualBankName (display to customer)
│  └─ invoiceReference (link to invoice)
│
▼
CUSTOMER PAYS (Manual/Mobile App)
│
├─ Customer transfers exact amount to virtualAccountNumber
├─ Monnify processes transaction
└─ Payment is received in merchant account

┌─────────────────────────────────────────────────────────────┐
│ MONNIFY WEBHOOK CALLBACK                                    │
│ POST /webhooks/monnify                                      │
│ (Async notification of payment)                             │
└─────────────────────────────────────────────────────────────┘
│
├─ Monnify sends:
│  {
│    "eventType": "SUCCESSFUL_INVOICE_PAYMENT",
│    "eventData": {
│      "invoiceReference": "ORDER-1-1693132800000",
│      "amountPaid": 50000,
│      "paymentDate": "2023-08-27T15:30:00Z"
│    }
│  }
│
▼
WEBHOOK HANDLER
│
├─ MonnifyWebhookController.handleInvoicePayment()
│  ├─ Parse invoiceReference to get orderId
│  ├─ Fetch Order from DB
│  ├─ Update Order:
│  │  ├─ paidAmount = amountPaid
│  │  ├─ isUnderpaid = (paidAmount < order.total)
│  │  ├─ remainingBalance = order.total - paidAmount
│  │  └─ paymentStatus = COMPLETED or PARTIAL
│  │
│  └─ Save Order to DB
│
├─ Broadcast WebSocket update
│  └─ OrderResponseDTO sent to customer/staff
│
├─ Create Notification
│  └─ "Payment received: NGN {amountPaid}"
│
└─ Return 200 OK to Monnify
```

### Monnify Configuration

```yaml
# application-local.yml
monnify:
  api:
    key: ${MONNIFY_API_KEY}
  secret:
    key: ${MONNIFY_SECRET_KEY}
  contract:
    code: ${MONNIFY_CONTRACT_CODE}
  # Sandbox URL: https://sandbox.monnify.com/api/v1/
  # Production URL: https://api.monnify.com/api/v1/
```

---

## Analytics & Reporting

### Analytics Data Aggregation

```
AnalyticsService.getSummary(LocalDate from, LocalDate to, int limit)
│
├─ Convert dates to UTC Instant range
│  └─ Start: from.atStartOfDay(UTC)
│  └─ End: to.plusDays(1).atStartOfDay(UTC)
│
▼
QUERY EXECUTED VIA AnalyticsRepository
│
├─ PRIMARY KPIs:
│  ├─ totalRevenue()
│  │  └─ SELECT SUM(orders.total) FROM orders
│  │     WHERE createdAt BETWEEN start AND end
│  │     AND status = 'COMPLETED'
│  │
│  ├─ totalOrderCount()
│  │  └─ SELECT COUNT(*) FROM orders
│  │     WHERE createdAt BETWEEN start AND end
│  │
│  ├─ cancelledOrderCount()
│  │  └─ SELECT COUNT(*) FROM orders
│  │     WHERE status = 'CANCELLED'
│  │     AND createdAt BETWEEN start AND end
│  │
│  ├─ averageOrderValue()
│  │  └─ SELECT AVG(orders.total) FROM orders
│  │     WHERE status = 'COMPLETED'
│  │
│  ├─ averageItemsPerOrder()
│  │  └─ SELECT AVG(orders.quantity) FROM orders
│  │     WHERE createdAt BETWEEN start AND end
│  │
│  └─ cancellationRate
│     └─ cancelled / totalOrders * 100
│
├─ CUSTOMER INSIGHTS:
│  ├─ newVsReturningCustomers()
│  │  └─ SELECT
│  │     COUNT(CASE WHEN orders.customer_id.count=1 THEN 1)
│  │       AS new_customers,
│  │     COUNT(CASE WHEN orders.customer_id.count>1 THEN 1)
│  │       AS returning_customers
│  │     FROM orders JOIN customers ...
│  │
│  └─ takeOutVsDineIn()
│     └─ SELECT
│        orderItems.takeOut,
│        COUNT(*) AS count,
│        SUM(orders.total) AS revenue
│        FROM orderItems JOIN orders ...
│        GROUP BY takeOut
│
├─ TOP PERFORMERS (By Revenue):
│  ├─ topWaitersBySales()
│  │  └─ SELECT waiter_id, name, SUM(total), COUNT(*)
│  │     FROM orders JOIN accounts ...
│  │     GROUP BY waiter_id ORDER BY SUM(total) DESC
│  │     LIMIT limit
│  │
│  ├─ topCashiersBySales()
│  ├─ topTablesBySales()
│  ├─ topRoomsBySales()
│  ├─ topCategoriesBySales()
│  │  └─ SELECT category_id, name, SUM(orderItems.price*quantity),
│  │     COUNT(*) FROM orderItems JOIN menuItems JOIN categories ...
│  │     GROUP BY category_id ORDER BY SUM DESC
│  │
│  └─ topMenuItemsBySales()
│     └─ SELECT menuItem_id, name, category, SUM(price*quantity),
│        COUNT(*) FROM orderItems JOIN menuItems ...
│        GROUP BY menuItem_id ORDER BY SUM DESC
│
├─ TOP PERFORMERS (By Volume):
│  ├─ topWaitersByCount()
│  ├─ topCashiersByCount()
│  ├─ topTablesByCount()
│  ├─ topRoomsByCount()
│  ├─ topCategoriesByCount()
│  └─ topMenuItemsByCount()
│  └─ Similar queries, ORDER BY COUNT(*) DESC
│
├─ LEAST PERFORMERS:
│  ├─ leastWaitersBySales() - ORDER BY SUM(total) ASC
│  ├─ leastTablesBySales()
│  └─ leastMenuItemsBySales()
│  └─ Inverse of top performers
│
└─ TIME-SERIES DATA:
   ├─ salesByDay()
   │  └─ SELECT DAYNAME(createdAt), COUNT(*), SUM(total)
   │     FROM orders GROUP BY DAYNAME ORDER BY ...
   │
   ├─ salesByHour()
   │  └─ SELECT HOUR(createdAt), COUNT(*), SUM(total)
   │     FROM orders GROUP BY HOUR ORDER BY ...
   │
   └─ revenueOverTime()
      └─ SELECT DATE(createdAt), COUNT(*), SUM(total)
         FROM orders GROUP BY DATE ORDER BY DATE DESC

▼
BUILD RESPONSE DTO
│
├─ AnalyticsSummaryByDTO {
│  ├─ totalRevenue, totalOrders, averageOrderValue
│  ├─ averageItemsPerOrder, cancelledOrders, cancellationRate
│  ├─ newCustomers, returningCustomers
│  ├─ topWaitersBySales: List<AccountSalesDTO>
│  ├─ topCashiersBySales: List<AccountSalesDTO>
│  ├─ topTablesBySales: List<TableSalesDTO>
│  ├─ topRoomsBySales: List<RoomSalesDTO>
│  ├─ topCategoriesBySales: List<CategorySalesDTO>
│  ├─ topMenuItemsBySales: List<MenuItemSalesDTO>
│  ├─ topCustomersBySales: List<CustomerSalesDTO>
│  ├─ [leastXxx fields with same structures]
│  ├─ salesByDay: List<DaySalesDTO>
│  ├─ salesByHour: List<HourSalesDTO>
│  ├─ revenueOverTime: List<DailyRevenueDTO>
│  ├─ takeOutCount, takeOutRevenue
│  ├─ dineInCount, dineInRevenue
│  └─ [... many more fields]
│ }
│
▼
RETURN TO CLIENT
│
├─ GET /api/palate/analytics?from=2023-08-01&to=2023-08-31&limit=10
│  └─ HTTP 200 + AnalyticsSummaryByDTO JSON
│
└─ Client visualizes:
   ├─ Dashboard KPIs
   ├─ Top products/staff charts
   ├─ Revenue trends
   └─ Customer analysis
```

---

## Table & Room Management

### Table Management Flow

```
ADMIN CREATES TABLE
│
├─ POST /api/palate/tables
│  Body: {
│    tableName: "Table 1",
│    tableNumber: 1,
│    capacity: 4,
│    status: "AVAILABLE"
│  }
│
▼
CREATE TABLE WITH QR CODE
│
├─ TableService.createTable()
│  ├─ Validate unique tableName and tableNumber
│  ├─ Generate QR code containing:
│  │  ├─ URL: /api/palate/tables/qrcode/{tableId}
│  │  └─ Encodes table info for mobile scanning
│  ├─ ZXing library generates QR image
│  ├─ Store QR as Base64 or file path
│  └─ Persist Table entity
│
▼
TABLE ALLOCATED TO STAFF
│
├─ POST /api/palate/table-allocations
│  Body: {
│    tableId: 1,
│    waiterId: 5,
│    cashierId: 3
│  }
│
├─ TableAllocationService.allocate()
│  ├─ Create TableAllocation entity
│  ├─ Set waiter + waiterAllocatedAt = now
│  ├─ Set cashier + cashierAllocatedAt = now
│  ├─ Update RestaurantTable.status = OCCUPIED
│  └─ Persist to DB
│
└─ Broadcast WebSocket:
   └─ /topic/tables/status updated
```

### Room Management Flow

```
SIMILAR TO TABLES
│
├─ POST /api/palate/rooms
│  Body: {
│    roomNumber: "101",
│    floor: 1,
│    status: "AVAILABLE"
│  }
│
├─ RoomService.createRoom()
│  ├─ Generate QR code for room access
│  ├─ Store room details
│  └─ Set default status = AVAILABLE
│
└─ Used for:
   ├─ Delivery orders
   ├─ Private dining
   └─ Banquet reservations
```

---

## Menu Item & Category Management

### Menu Item Lifecycle

```
CATEGORY CREATION (Admin only)
│
├─ POST /api/palate/categories
│  Body: { name: "Appetizers", description: "...", status: "ACTIVE" }
│
└─ CategoryService.createCategory()
   ├─ Validate unique category name
   ├─ Set status = ACTIVE (default)
   └─ Persist Category entity

MENU ITEM CREATION (Admin/Cashier)
│
├─ POST /api/palate/menu-items
│  Body: {
│    name: "Caesar Salad",
│    description: "Fresh romaine lettuce...",
│    price: 2500.00,
│    categoryId: 1,
│    imageUrl: "https://...",
│    status: "AVAILABLE"
│  }
│
├─ MenuItemService.createMenuItem()
│  ├─ Validate required fields
│  ├─ Resolve Category by ID
│  ├─ Set status = AVAILABLE
│  └─ Persist MenuItem entity
│
└─ Available for ordering via:
   ├─ GET /api/palate/menu-items (all active)
   ├─ GET /api/palate/menu-items/{id} (single)
   └─ GET /api/palate/categories/{id}/menu-items

MENU ITEM UNAVAILABILITY
│
├─ POST /api/palate/menu-items/unavailable
│  Body: [{ menuItemId: 1, isUnavailable: true }, ...]
│
└─ MenuItemService.setUnavailable()
   ├─ Set MenuItem.status = UNAVAILABLE
   ├─ Prevents new orders from selecting it
   ├─ Existing orders still reference it
   └─ Returns to AVAILABLE when stock replenishes

MENU ITEM UPDATES (Admin/Cashier)
│
├─ PUT /api/palate/menu-items/{id}
│  Body: { name, description, price, categoryId, ... }
│
└─ MenuItemService.updateMenuItem()
   ├─ Fetch existing MenuItem
   ├─ Update fields
   ├─ Re-validate constraints
   └─ Persist changes
   └─ Does NOT affect existing orders

MENU ITEM DELETION (Admin only)
│
├─ DELETE /api/palate/menu-items/{id}
│
└─ MenuItemService.deleteMenuItem()
   ├─ Check for active orders referencing this item
   ├─ If orders exist → soft delete (status = DELETED)
   ├─ If no orders → hard delete
   └─ Returns HTTP 204 No Content
```

---

## Customer Management

### Customer Data Flow

```
CUSTOMER SELF-REGISTRATION (Public)
│
├─ POST /api/palate/customers
│  Body: {
│    name: "John Doe",
│    title: "Mr",
│    phoneNumber: "08012345678",
│    email: "john@example.com"
│  }
│
├─ CustomerService.createCustomer()
│  ├─ Validate non-null fields
│  ├─ Check phone/email uniqueness (optional)
│  ├─ Create Customer entity
│  └─ Persist to DB
│
└─ HTTP 201 + CustomerDTO
   └─ Includes customerId for future orders

CUSTOMER LOOKUP DURING ORDER CREATION
│
├─ OrderRequestDTO { customerId, customerDetails }
│
├─ CreateOrder.resolveCustomer()
│  ├─ IF customerId provided:
│  │  └─ Fetch Customer by ID
│  │
│  ├─ ELSE IF email/phone provided:
│  │  ├─ Search existing Customer
│  │  └─ Create new if not found
│  │
│  └─ Return Customer entity
│
└─ Customer linked to Order entity

RETRIEVE CUSTOMER ORDERS (Admin/Manager)
│
├─ GET /api/palate/customers/{id}
│  └─ Returns Customer details + order history
│
└─ OrderService.fetchCustomerOrdersToday(customerId)
   ├─ Query orders WHERE customer_id = customerId
   └─ Filter by createdAt = TODAY (Lagos timezone)
```

---

## Notifications & Alerts

### Notification System

```
NOTIFICATION TRIGGERS
│
├─ Order Status Changes:
│  ├─ When Order.status updated
│  └─ NotificationService sends to waiter/cashier
│
├─ Order Timeout:
│  ├─ OrderTimeoutScheduler detects stale orders
│  │  (pending for > 10 minutes)
│  ├─ Creates "STALE_ORDER" notification
│  └─ Sends to all ADMIN accounts
│
├─ Payment Confirmation:
│  ├─ Monnify webhook triggers
│  ├─ Creates "PAYMENT_RECEIVED" notification
│  └─ Sends to cashier/order creator
│
└─ Custom Notifications:
   ├─ Manual creation by staff
   ├─ System alerts (inventory, etc)
   └─ Scheduled announcements

NOTIFICATION CREATION & DELIVERY
│
├─ NotificationService.sendToAccount(account, title, type, message)
│  ├─ Create Notification entity:
│  │  ├─ title: "Order Not Responded To"
│  │  ├─ type: "STALE_ORDER"
│  │  ├─ message: "Order #INV-001..."
│  │  ├─ account_id: recipient
│  │  ├─ isRead: false
│  │  └─ readAt: null
│  │
│  ├─ Persist Notification to DB
│  │
│  └─ Broadcast via WebSocket:
│     └─ messagingTemplate.convertAndSend(
│        '/topic/notifications/{accountId}',
│        NotificationDTO)
│
▼
CLIENT RECEIVES NOTIFICATION
│
├─ Subscribe to: /topic/notifications/{accountId}
├─ Display toast/alert with title + message
├─ Optionally link to related entity (Order, etc)
└─ Mark as read via API:
   └─ PATCH /api/palate/notifications/{id}
      └─ NotificationService.markAsRead(id)
         ├─ Set isRead = true
         ├─ Set readAt = now
         └─ Persist to DB

NOTIFICATION RETRIEVAL
│
└─ GET /api/palate/notifications?unreadOnly=true
   ├─ Fetch Notification entities for authenticated user
   └─ Return NotificationDTO list (ordered by createdAt DESC)
```

---

## Scheduled Tasks

### OrderTimeoutScheduler

```
@Scheduled(fixedRate = 60000) // Every 60 seconds
OrderTimeoutScheduler.checkStaleOrders()
│
├─ Instant cutoff = now() - 10 minutes
│
├─ Query orders:
│  └─ SELECT * FROM orders
│     WHERE status = 'PENDING'
│     AND createdAt < cutoff
│     AND hasBeenNotified = false
│
├─ For each stale order:
│  ├─ Fetch all ADMIN accounts
│  ├─ Create Notification:
│  │  ├─ title: "Order Not Responded To"
│  │  ├─ type: "STALE_ORDER"
│  │  ├─ message: "Order #{invoiceNumber} pending 10+ min"
│  │  └─ recipient: each admin
│  │
│  ├─ Send via NotificationService
│  ├─ Log warning message
│  └─ Mark order as notified (optional)
│
└─ Repeat every 60 seconds
```

### PingScheduler

```
Used for application health checks
└─ Sends periodic heartbeat
   └─ Useful for load balancers / uptime monitoring
```

---

## Data Persistence & Repositories

### JPA Repository Pattern

```
Repository Hierarchy:
├─ JpaRepository<T, ID> (Spring Data JPA)
│  └─ Provides CRUD operations (save, findById, delete, etc)
│
├─ Custom Queries:
│  ├─ @Query("SELECT ...") native SQL
│  ├─ Specification<T> dynamic queries (Criteria API)
│  └─ Method naming conventions (findBy...)
│
└─ Example: OrderRepository
   ├─ extends JpaRepository<Order, Long>
   ├─ extends JpaSpecificationExecutor<Order>
   │
   ├─ Custom methods:
   │  ├─ findByStatus(OrderStatus)
   │  ├─ findByWaiter_Id(Long waiterId)
   │  ├─ countByStatusNotIn(List<OrderStatus>)
   │  └─ findStaleOrders(Instant cutoff)
   │
   └─ Specification usage:
      └─ orderRepository.findAll(
         OrderSpecification.filter(...), pageable)
```

### Key Repositories

| Repository | Entity | Key Methods |
|------------|--------|-------------|
| OrderRepository | Order | findByStatus, findByWaiter_Id, findStaleOrders |
| OrderItemRepository | OrderItem | findByOrder_Id |
| MenuItemRepository | MenuItem | findByCategory_Id, findByStatus |
| CategoryRepository | Category | findByStatus |
| CustomerRepository | Customer | findByEmail, findByPhoneNumber |
| AccountRepository | Account | findByEmail, findByRole |
| RestaurantTableRepository | RestaurantTable | findByStatus, findByWaiter_Id |
| RoomRepository | Room | findByStatus |
| TableAllocationRepository | TableAllocation | findByTable_Id |
| NotificationRepository | Notification | findByAccount_Id, findByIsRead |
| AnalyticsRepository | N/A (custom queries) | totalRevenue, topWaitersBySales, etc |

### Transaction Management

```
@Transactional
public Order createOrder(OrderRequestDTO request) {
  ├─ Entities resolved & validated (within transaction)
  ├─ Order entity created & saved
  ├─ OrderItems created & associated
  ├─ MonnifyService.createOrderInvoice() called
  │  (HTTP call - should not fail transaction)
  ├─ OrderEvents.broadcastCreated() called
  │  (WebSocket - asynchronous)
  │
  └─ IF exception occurs:
     └─ ROLLBACK: Order, OrderItems unsaved
     └─ Invoice creation may succeed but Order fails
        → Monnify cleanup needed in webhook
}

@Transactional(readOnly = true)
public Order getOrderById(Long id) {
  └─ No writes allowed, optimized for reading
}
```

---

## Request/Response Flow Examples

### Example 1: Create Order with Payment

```
REQUEST:
POST /api/palate/orders
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "tableId": 1,
  "customerId": 5,
  "waiterId": 3,
  "cashierId": 2,
  "items": [
    {"menuItemId": 10, "quantity": 2, "takeOut": false},
    {"menuItemId": 15, "quantity": 1, "takeOut": false}
  ],
  "note": "No onions on burger"
}

PROCESSING:
1. JwtAuthenticationFilter validates token
2. OrderController.createOrder() invoked
3. OrderService.createOrder() → CreateOrder.createOrder()
4. Resolve: Table(1), Customer(5), Waiter(3), Cashier(2)
5. Fetch MenuItems(10, 15) and verify status
6. Calculate totals:
   - Item 1: 2,500 * 2 = 5,000
   - Item 2: 3,500 * 1 = 3,500
   - Total: 8,500 NGN
7. Create Order entity (status=PENDING)
8. Create 2 OrderItem entities
9. Save Order + OrderItems (transaction)
10. MonnifyService.createOrderInvoice(1, 8500, "Customer Name")
    - Auth with Monnify
    - Create invoice for 8,500 NGN
    - Receive: accountNumber, bankName, reference
11. Update Order with virtual account details
12. OrderEvents.broadcastCreated() → WebSocket
13. Return OrderResponseDTO

RESPONSE:
HTTP 201 Created
Content-Type: application/json

{
  "id": 1,
  "invoiceNumber": "INV-2023-08-27-001",
  "total": 8500.0,
  "quantity": 3,
  "status": "PENDING",
  "paidAmount": null,
  "isUnderpaid": false,
  "remainingBalance": 8500.0,
  "virtualAccountNumber": "0123456789",
  "virtualBankName": "MONNIFY VIRTUAL BANK",
  "monnifyReference": "ORDER-1-1693132800000",
  "table": {"id": 1, "tableName": "Table 1", ...},
  "waiter": {"id": 3, "firstName": "John", ...},
  "cashier": {"id": 2, "firstName": "Jane", ...},
  "customer": {"id": 5, "name": "Customer Name", ...},
  "items": [
    {
      "id": 1,
      "menuItem": {"id": 10, "name": "Burger", "price": 2500},
      "quantity": 2,
      "takeOut": false,
      "price": 2500
    },
    {
      "id": 2,
      "menuItem": {"id": 15, "name": "Fries", "price": 3500},
      "quantity": 1,
      "takeOut": false,
      "price": 3500
    }
  ],
  "createdAt": "2023-08-27T15:20:00Z",
  "updatedAt": "2023-08-27T15:20:05Z"
}

WEBSOCKET BROADCAST (All Subscribers):
Topic: /topic/orders/created
Topic: /topic/orders/customer/5

Payload: Same OrderResponseDTO as above
```

### Example 2: Update Order Status

```
REQUEST:
PATCH /api/palate/orders/1
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "status": "READY"
}

PROCESSING:
1. JwtAuthenticationFilter validates token
2. Check authorization: requires ADMIN/WAITER/CASHIER
3. OrderService.updateOrder(1, UpdateOrderStatusDTO)
4. Fetch Order(1) from DB
5. Order.status = PENDING → READY
6. Save Order to DB
7. OrderResponseDTO mapped from updated Order
8. OrderEvents.broadcastUpdated()
   - Broadcast to /topic/orders/updated
   - Broadcast to /topic/orders/customer/5

RESPONSE:
HTTP 200 OK

{
  "id": 1,
  "invoiceNumber": "INV-2023-08-27-001",
  "status": "READY",
  "total": 8500.0,
  [... other fields ...]
  "updatedAt": "2023-08-27T15:25:00Z"
}

WEBSOCKET SUBSCRIBERS RECEIVE:
- Kitchen display system updates order to "Ready"
- Waiter app updates order status
- Customer app shows "Your order is ready!"
```

### Example 3: Get Analytics

```
REQUEST:
GET /api/palate/analytics?from=2023-08-01&to=2023-08-31&limit=5
Authorization: Bearer eyJhbGc...

PROCESSING:
1. JwtAuthenticationFilter validates token
2. Check authorization: requires ADMIN/MANAGER
3. Convert dates to UTC Instant:
   - from = 2023-08-01T00:00:00Z
   - to = 2023-09-01T00:00:00Z
4. AnalyticsService.getSummary()
   - Execute 20+ SQL queries on Orders table
   - Calculate aggregates (SUM, COUNT, AVG)
   - Order results by revenue/count (LIMIT 5)
5. Map results to AnalyticsSummaryByDTO
6. Return JSON response

RESPONSE:
HTTP 200 OK

{
  "totalRevenue": 185500.0,
  "totalOrders": 42,
  "averageOrderValue": 4416.67,
  "averageItemsPerOrder": 2.8,
  "cancelledOrders": 3,
  "cancellationRate": 7.14,
  "newCustomers": 8,
  "returningCustomers": 34,
  "topWaitersBySales": [
    {"id": 3, "name": "John Doe", "totalSales": 45000, "orderCount": 12},
    {"id": 4, "name": "Jane Smith", "totalSales": 38500, "orderCount": 10},
    ...
  ],
  "topMenuItemsBySales": [
    {"id": 10, "name": "Burger", "categoryName": "Main Courses", "totalSales": 50000, "totalQuantity": 50},
    {"id": 15, "name": "Fries", "categoryName": "Sides", "totalSales": 35000, "totalQuantity": 70},
    ...
  ],
  "salesByDay": [
    {"day": "Monday", "orderCount": 6, "totalRevenue": 26500},
    {"day": "Tuesday", "orderCount": 8, "totalRevenue": 35600},
    ...
  ],
  "revenueOverTime": [
    {"date": "2023-08-31", "orderCount": 2, "totalRevenue": 18500},
    {"date": "2023-08-30", "orderCount": 3, "totalRevenue": 24000},
    ...
  ],
  [... many more fields ...]
}
```

### Example 4: Payment Webhook

```
REQUEST (From Monnify):
POST /webhooks/monnify
Content-Type: application/json

{
  "eventType": "SUCCESSFUL_INVOICE_PAYMENT",
  "eventData": {
    "invoiceReference": "ORDER-1-1693132800000",
    "amountPaid": 8500,
    "paymentDate": "2023-08-27T15:30:00Z",
    "transactionId": "MONNIFY-TXN-123456"
  }
}

PROCESSING:
1. MonnifyWebhookController.handleInvoicePayment()
2. Parse invoiceReference → orderId = 1
3. Fetch Order(1) from DB
4. Update Order:
   - paidAmount = 8500
   - isUnderpaid = false (8500 >= 8500)
   - remainingBalance = 0
5. Save Order to DB
6. Log payment receipt
7. OrderEvents.broadcastUpdated()
   - Notify customer: "Payment received!"
8. NotificationService.sendToAccount()
   - Notify cashier: "Payment confirmed"
9. Return 200 OK

RESPONSE:
HTTP 200 OK

{
  "status": "SUCCESS",
  "message": "Payment processed"
}

SIDE EFFECTS:
- Order paidAmount updated in DB
- WebSocket: Order status broadcast
- Notification: Sent to staff/customer
- Analytics: Revenue updated for reporting
```

---

## Data Validation & Error Handling

### Validation Layers

```
1. REQUEST VALIDATION:
   ├─ @NotNull, @NotBlank annotations on DTO fields
   ├─ Custom validation via ValidationUtils
   ├─ Jakarta Validation API
   └─ MethodArgumentNotValidException → 400 Bad Request

2. BUSINESS LOGIC VALIDATION:
   ├─ Entity existence checks
   ├─ Status transition validation
   ├─ Authorization checks
   └─ BadRequestException → 400 Bad Request

3. DATABASE CONSTRAINTS:
   ├─ NOT NULL constraints
   ├─ UNIQUE constraints
   ├─ FOREIGN KEY constraints
   └─ DataIntegrityViolationException → 409 Conflict

4. CUSTOM EXCEPTIONS:
   ├─ BadRequestException (400)
   ├─ NotFoundException (404)
   ├─ UnauthorizedException (401)
   ├─ ForbiddenException (403)
   └─ InternalServerException (500)
```

### Error Response Format

```json
{
  "status": 400,
  "message": "Order not found",
  "timestamp": "2023-08-27T15:30:00Z",
  "path": "/api/palate/orders/999",
  "error": "BadRequestException"
}
```

---

## Performance Considerations

### Database Indexes

```
orders:
├─ PRIMARY KEY: id
├─ UNIQUE: invoiceNumber
├─ INDEX: idx_orders_status_created (status, createdAt)
│  → Optimizes filtering by status + date range
├─ FOREIGN KEY: waiter_id, cashier_id, table_id, room_id
│
menuItems:
├─ FOREIGN KEY: category_id
│
orderItems:
├─ FOREIGN KEY: order_id (cascade delete)
├─ FOREIGN KEY: menu_item_id
│
accounts:
├─ UNIQUE: email, phoneNumber
│
customers:
├─ UNIQUE: email (optional)
└─ UNIQUE: phoneNumber (optional)
```

### Query Optimization

```
1. LAZY LOADING:
   ├─ Order.items → @OneToMany(lazy = true)
   ├─ Loaded only when accessed
   └─ Reduces memory for large result sets

2. PAGINATION:
   ├─ getAllOrders() uses Pageable
   ├─ Default: page=0, size=20
   ├─ Prevents OOM on large queries
   └─ Returns OrderPageResponse with statusCounts

3. PROJECTIONS:
   ├─ OrderSummaryProjection
   ├─ Returns only needed fields
   └─ Reduces database load

4. BATCH OPERATIONS:
   ├─ Cascade operations on Order
   ├─ OrderItems saved with Order
   └─ Single transaction for consistency
```

### Caching Strategy

```
JWT Token Caching:
├─ Access Token: 15 minutes (short-lived)
├─ Refresh Token: 7 days (long-lived)
└─ Clients must refresh before expiry

Monnify Token Caching:
├─ getAuthToken() caches accessToken
├─ Reused for subsequent invoice requests
└─ Reduces API calls to Monnify
```

---

## Deployment & Environment Configuration

### Environment-Specific Profiles

```
application.yaml (active profile: local)
├─ Base configuration

application-local.yml (Development)
├─ H2 in-memory database
├─ Logging level: DEBUG
├─ CORS: Open for development
├─ Monnify: Sandbox mode

application-prod.yml (Production)
├─ PostgreSQL remote database
├─ Logging level: INFO
├─ CORS: Restricted origins
├─ Monnify: Production API
├─ Security: Strict validation
└─ TLS/HTTPS enforced
```

### Secrets Management

```
JWT Secrets:
├─ jwt.access-secret (Access token signing key)
└─ jwt.refresh-secret (Refresh token signing key)

Monnify Credentials:
├─ monnify.api.key
├─ monnify.secret.key
└─ monnify.contract.code

Database Credentials:
├─ spring.datasource.url
├─ spring.datasource.username
└─ spring.datasource.password

→ All stored as environment variables
  (NOT in version control)
```

---

## System Flow Diagram (High-Level)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PALATE SYSTEM                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  CLIENT LAYER (Web/Mobile Apps)                                    │
│  ├─ REST API calls (HTTP)                                          │
│  ├─ WebSocket subscriptions (STOMP)                                │
│  └─ QR code scanning                                               │
│         │                                                           │
│         ▼                                                           │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │ SPRING BOOT APPLICATION LAYER                              │   │
│  ├────────────────────────────────────────────────────────────┤   │
│  │                                                            │   │
│  │ ┌─ REST Controllers ──────────────────────────────────┐   │   │
│  │ │ OrderController, CustomerController, Analytics... │   │   │
│  │ └─────────────────┬────────────────────────────────────┘   │   │
│  │                   │                                        │   │
│  │ ┌─ Security Layer ▼───────────────────────────────────┐   │   │
│  │ │ JwtAuthenticationFilter, SecurityConfig             │   │   │
│  │ └─────────────────┬────────────────────────────────────┘   │   │
│  │                   │                                        │   │
│  │ ┌─ Service Layer ▼───────────────────────────────────┐   │   │
│  │ │ OrderService, CustomerService, AnalyticsService    │   │   │
│  │ │ MonnifyService, NotificationService                │   │   │
│  │ └─────────────────┬────────────────────────────────────┘   │   │
│  │                   │                                        │   │
│  │ ┌─ WebSocket Layer ▼─────────────────────────────────┐   │   │
│  │ │ OrderEvents, NotificationService (async)           │   │   │
│  │ └─────────────────┬────────────────────────────────────┘   │   │
│  │                   │                                        │   │
│  │ ┌─ Repository Layer ▼───────────────────────────────┐   │   │
│  │ │ JPA Repositories, Specifications, Custom Queries   │   │   │
│  │ └─────────────────┬────────────────────────────────────┘   │   │
│  │                   │                                        │   │
│  └───────────────────┼────────────────────────────────────────┘   │
│                      │                                             │
│         ┌────────────┼────────────┐                                │
│         │            │            │                                │
│         ▼            ▼            ▼                                │
│    ┌─────────┐  ┌─────────┐  ┌──────────────┐                    │
│    │PostgreSQL│ │  H2     │  │Monnify API   │                    │
│    │Database  │ │(dev/test)│  │(Payment)     │                    │
│    └─────────┘  └─────────┘  └──────────────┘                    │
│         │            │                                             │
│         └────────────┼────────────────────────────────────────┐   │
│                      │                                        │   │
│  ┌──────────────────▼─────────────────────────────────────┐   │   │
│  │ SCHEDULED TASKS (Async)                                │   │   │
│  │ ├─ OrderTimeoutScheduler (every 60 sec)              │   │   │
│  │ ├─ PingScheduler                                     │   │   │
│  │ └─ Notification creation & delivery                 │   │   │
│  └──────────────────────────────────────────────────────┘   │   │
│                                                             │   │
└─────────────────────────────────────────────────────────────┘   │
```

---

## Conclusion

The Palate Restaurant Management System is a comprehensive, multi-layered Spring Boot application that efficiently handles:

1. **Order Management**: From creation through payment with real-time status tracking
2. **Staff Coordination**: Role-based access, table/room allocations, and assignments
3. **Payment Processing**: Integration with Monnify for secure payment handling
4. **Real-time Updates**: WebSocket-based instant notifications to all stakeholders
5. **Analytics & Reporting**: Deep insights into sales, performance, and customer behavior
6. **Security**: JWT-based authentication with role-based authorization
7. **Scalability**: Lazy loading, pagination, and database indexing for performance

The data flows seamlessly through well-defined layers, ensuring data consistency, transaction safety, and optimal performance across the entire system.

