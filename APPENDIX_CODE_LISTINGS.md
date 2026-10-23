# Appendix: Important Backend Code Listings

This appendix presents ten important code listings from the Palate restaurant-management backend. The listings cover security, authentication, ordering, payment, notifications, and monitoring.

## 1. Security and Role-Based Access Control

**Source:** `src/main/java/com/app/palate/security/SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/palate/auth/login").permitAll()
                    .requestMatchers("/api/palate/auth/refresh").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/palate/orders").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/palate/orders/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/palate/orders/**")
                    .hasAnyRole("ADMIN", "WAITER", "CASHIER")
                    .requestMatchers(HttpMethod.DELETE, "/api/palate/customers/**")
                    .hasRole("ADMIN")
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**Functionality comment:** Configures the API security boundary. It declares public endpoints, requires authentication for protected endpoints, restricts operations by staff role, and installs JWT authentication before normal request authorization.

## 2. JWT Request Authentication Filter

**Source:** `src/main/java/com/app/palate/security/JwtAuthenticationFilter.java`

```java
String token = extractToken(request);

if (token != null) {
    try {
        if (jwtUtil.isValid(token)) {
            String email = jwtUtil.getEmail(token);
            String role = jwtUtil.getRole(token);
            var authority = new SimpleGrantedAuthority(role);
            var authentication = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception e) {
        SecurityContextHolder.clearContext();
    }
}

filterChain.doFilter(request, response);
```

**Functionality comment:** Extracts and validates a Bearer JWT on each request, then stores the user email and role in Spring Security’s context for downstream authorization.

## 3. Access and Refresh Token Generation

**Source:** `src/main/java/com/app/palate/security/JwtUtil.java`

```java
public String generateAccessToken(Account account) {
    return Jwts.builder()
            .subject(account.getEmail())
            .claim("role", account.getRole().name())
            .claim("id", account.getId())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()
                    + ACCESS_EXPIRATION))
            .signWith(accessTokenSecret)
            .compact();
}

public String generateRefreshToken(Account account) {
    return Jwts.builder()
            .subject(account.getEmail())
            .claim("id", account.getId())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()
                    + REFRESH_EXPIRATION))
            .signWith(refreshTokenSecret)
            .compact();
}
```

**Functionality comment:** Creates signed tokens with separate lifetimes. The access token contains role information for authorization, while the refresh token supports obtaining a new access token after expiry.

## 4. Login and Password Verification

**Source:** `src/main/java/com/app/palate/auth/AuthService.java`

```java
public AuthResponseDTO login(LoginRequestDTO request) {
    ValidationUtils.requireNonNull(request, "Request body");
    ValidationUtils.requireNonBlank(request.email(), "Email");
    ValidationUtils.requireNonBlank(request.password(), "Password");

    Account account = accountRepository
            .findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), account.getPassword())) {
        throw new BadRequestException("Invalid credentials");
    }

    return new AuthResponseDTO(jwtUtil.generateAccessToken(account),
            jwtUtil.generateRefreshToken(account), accountMapper.mapToDto(account));
}
```

**Functionality comment:** Validates login data, normalizes the email, compares the submitted password with its BCrypt hash, and returns tokens only after successful authentication.

## 5. Order Validation and Total Calculation

**Source:** `src/main/java/com/app/palate/order/services/CreateOrder.java`

```java
private static void validateRequest(OrderRequestDTO request) {
    boolean hasTable = request.getTableId() != null;
    boolean hasRoom = request.getRoomId() != null;

    if (!hasTable && !hasRoom) {
        throw new BadRequestException("Order must belong to either a table or a room");
    }
    if (hasTable && hasRoom) {
        throw new BadRequestException("Order cannot belong to both a table and a room");
    }
    if (hasTable && request.getWaiterId() == null) {
        throw new BadRequestException("Waiter Id is required");
    }
    if (request.getCashierId() == null || request.getItems() == null
            || request.getItems().isEmpty()) {
        throw new BadRequestException("Cashier and order items are required");
    }
}

for (OrderItemDTO itemDTO : request.getItems()) {
    MenuItem menuItem = entityResolver.resolveMenuItem(itemDTO.getMenuItemId());
    if (MenuItemStatus.UNAVAILABLE.equals(menuItem.getStatus())) {
        throw new BadRequestException("Sorry, " + menuItem.getName() + " is unavailable");
    }
    total += menuItem.getPrice() * itemDTO.getQuantity();
    totalQuantity += itemDTO.getQuantity();
}
```

**Functionality comment:** Enforces order rules, rejects unavailable menu items, and calculates the total from current menu prices and requested quantities before saving the order.

## 6. Order Status Updates and Live Events

**Source:** `src/main/java/com/app/palate/order/OrderService.java`

```java
public Order updateOrder(Long id, UpdateOrderStatusDTO request) {
    ValidationUtils.requireNonNull(id, "Order ID");
    ValidationUtils.requireNonNull(request, "Request body");

    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Order not found"));
    if (request.status() != null) {
        order.setStatus(request.status());
    }

    Order saved = orderRepository.save(order);
    orderEvents.broadcastUpdated(OrderResponseDTO.mapToResponse(saved));
    return saved;
}
```

**Functionality comment:** Centralizes order status changes, persists the new state, and broadcasts the update so staff and customers receive it in real time.

## 7. Dynamic Order Filtering and Pagination

**Source:** `src/main/java/com/app/palate/order/OrderService.java`

```java
String sortBy = filter.sortBy();
if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
    sortBy = "createdAt";
}

Sort sort = "asc".equalsIgnoreCase(filter.direction())
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
Pageable pageable = PageRequest.of(filter.page() != null ? filter.page() : 0,
        filter.size() != null ? filter.size() : 30, sort);

Specification<Order> specification = OrderSpecification.filter(
        filter.status(), filter.waiterId(), filter.cashierId(), filter.tableId(),
        filter.roomId(), filter.minTotal(), filter.maxTotal(), startInstant,
        endInstant, filter.search());
Page<Order> orders = orderRepository.findAll(specification, pageable);
```

**Functionality comment:** Builds a paginated, sorted, and dynamically filtered order query. The allowed sort-field list prevents clients from selecting arbitrary database fields.

## 8. Payment Webhook Reconciliation

**Source:** `src/main/java/com/app/palate/payment/MonnifyWebhookController.java`

```java
if (!"SUCCESSFUL_TRANSACTION".equals(eventType)) {
    return ResponseEntity.ok().build();
}

String invoiceReference = product.get("reference").asText();
Order order = orderRepository.findByMonnifyReference(invoiceReference).orElse(null);
if (order == null) {
    return ResponseEntity.ok().build();
}

double amountPaid = eventData.get("amountPaid").asDouble();
double expectedAmount = Boolean.TRUE.equals(order.getIsUnderpaid())
        && order.getRemainingBalance() != null
        ? order.getRemainingBalance() : order.getTotal();
double previouslyPaid = order.getPaidAmount() != null ? order.getPaidAmount() : 0;
order.setPaidAmount(previouslyPaid + amountPaid);

if (amountPaid == expectedAmount) handleExactPayment(order);
else if (amountPaid < expectedAmount) handleUnderpayment(order, amountPaid, expectedAmount);
else handleOverpayment(order, amountPaid, expectedAmount);
```

**Functionality comment:** Matches a successful Monnify transaction to an order, accumulates payments, and applies separate business rules for exact payment, underpayment, and overpayment.

## 9. Secure QR Code Generation

**Source:** `src/main/java/com/app/palate/service/QrCodeService.java`

```java
private static final String CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
private static final int TOKEN_LENGTH = 7;
private final SecureRandom random = new SecureRandom();

public String generateRandomQrCode() {
    StringBuilder token = new StringBuilder(TOKEN_LENGTH);
    for (int i = 0; i < TOKEN_LENGTH; i++) {
        int randomIndex = random.nextInt(CHAR_POOL.length());
        token.append(CHAR_POOL.charAt(randomIndex));
    }
    return token.toString();
}
```

**Functionality comment:** Generates compact seven-character QR lookup values using `SecureRandom`, making identifiers difficult to guess while keeping them practical to print and scan.

## 10. Scheduled Detection of Stale Orders

**Source:** `src/main/java/com/app/palate/order/helpers/OrderTimeoutScheduler.java`

```java
@Scheduled(fixedRate = 60000)
public void checkStaleOrders() {
    Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);
    List<Order> staleOrders = orderRepository.findStaleOrders(cutoff);
    if (staleOrders.isEmpty()) return;

    List<Account> admins = accountRepository.findByRole(Role.ROLE_ADMIN);
    for (Order order : staleOrders) {
        String message = "Order " + order.getInvoiceNumber()
                + " has been pending for over 10 minutes.";
        for (Account admin : admins) {
            notificationService.sendToAccount(admin, "Order Not Responded To",
                    "STALE_ORDER", message);
        }
    }
}
```

**Functionality comment:** Runs every minute, identifies orders pending for more than ten minutes, and alerts administrators so delayed orders can be handled promptly.