package com.app.palate.order;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.app.palate.auth.Account;
import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemStatus;
import com.app.palate.orderItem.OrderItem;
import com.app.palate.orderItem.OrderItemDTO;
import com.app.palate.payment.MonnifyInvoiceResponse;
import com.app.palate.payment.MonnifyService;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.utils.EntityResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateOrder {
    private final EntityResolver entityResolver;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MonnifyService monnifyService;

    public OrderResponseDTO createOrder(OrderRequestDTO request, OrderEvents orderEvents) {
        validateRequest(request);

        RestaurantTable table = entityResolver.resolveRestaurantTable(request.getTableId());
        Account waiter = entityResolver.resolveEmployee(request.getWaiterId());
        Account cashier = entityResolver.resolveEmployee(request.getCashierId());
        Customer customer = resolveCustomer(request, customerRepository);

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTable(table);
        order.setWaiter(waiter);
        order.setCustomer(customer);
        order.setCashier(cashier);
        order.setInvoiceNumber(generateInvoiceNumber());

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;
        int totalQuantity = 0;

        for (OrderItemDTO itemDTO : request.getItems()) {
            Long menuItemId = itemDTO.getMenuItemId();
            if (menuItemId == null)
                throw new BadRequestException("Menu item Id is required");

            MenuItem menuItem = entityResolver.resolveMenuItem(menuItemId);
            if (MenuItemStatus.UNAVAILABLE.equals(menuItem.getStatus())) {
                throw new BadRequestException("Sorry, " + menuItem.getName() + " is unavailable");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setTakeOut(itemDTO.isTakeOut());

            items.add(orderItem);
            total += menuItem.getPrice() * itemDTO.getQuantity();
            totalQuantity += itemDTO.getQuantity();
        }

        order.setItems(items);
        order.setTotal(total);
        order.setQuantity(totalQuantity);

        Order savedOrder = orderRepository.save(order);

        // Generate virtual account for this order
        try {
            String customerName = customer.getTitle() + " " + customer.getName();
            MonnifyInvoiceResponse invoice = monnifyService.createOrderInvoice(
                savedOrder.getId(),
                savedOrder.getTotal(),
                customerName  // actual customer name
            );
            savedOrder.setVirtualAccountNumber(invoice.accountNumber());
            savedOrder.setVirtualBankName(invoice.bankName());
            savedOrder.setMonnifyReference(invoice.invoiceReference());
            orderRepository.save(savedOrder);
            log.info("Virtual account created for order {}: {} - {}",
                savedOrder.getId(), invoice.bankName(), invoice.accountNumber());
        } catch (Exception e) {
            log.error("Failed to generate virtual account for order {}", savedOrder.getId(), e);
        }

        OrderResponseDTO response = OrderResponseDTO.mapToResponse(savedOrder);
        orderEvents.broadcastCreated(response);
        return response;
    }

    /* ======================= VALIDATION ======================= */
    private static void validateRequest(OrderRequestDTO request) {
        if (request.getTableId() == null)
            throw new BadRequestException("Table Id is required");
        if (request.getWaiterId() == null)
            throw new BadRequestException("Waiter Id is required");
        if (request.getItems() == null || request.getItems().isEmpty())
            throw new BadRequestException("Order items are required");
        if (request.getCashierId() == null)
            throw new BadRequestException("Cashier ID is required");
    }

    /* ======================= CUSTOMER ======================= */
    private static Customer resolveCustomer(
            OrderRequestDTO request,
            CustomerRepository customerRepository) {

        if (request.getCustomerId() != null) {
            Customer existing = customerRepository.findById(request.getCustomerId()).orElse(null);
            if (existing != null) {
                if (request.getCustomerName() != null && !request.getCustomerName().isBlank())
                    existing.setName(request.getCustomerName());
                if (request.getCustomerTitle() != null && !request.getCustomerTitle().isBlank())
                    existing.setTitle(request.getCustomerTitle());
                if (request.getCustomerPhoneNumber() != null && !request.getCustomerPhoneNumber().isBlank())
                    existing.setPhoneNumber(request.getCustomerPhoneNumber());
                return customerRepository.save(existing);
            }
        }

        if (request.getCustomerName() == null || request.getCustomerName().isBlank() ||
                request.getCustomerTitle() == null || request.getCustomerTitle().isBlank()) {
            throw new BadRequestException("Customer name and title are required");
        }

        String phone = request.getCustomerPhoneNumber();
        if (phone != null && !phone.isBlank()) {
            Customer byPhone = customerRepository.findByPhoneNumber(phone);
            if (byPhone != null) {
                byPhone.setName(request.getCustomerName());
                byPhone.setTitle(request.getCustomerTitle());
                return customerRepository.save(byPhone);
            }
        }

        Customer newCustomer = new Customer();
        newCustomer.setName(request.getCustomerName());
        newCustomer.setTitle(request.getCustomerTitle());
        if (phone != null && !phone.isBlank()) {
            newCustomer.setPhoneNumber(phone);
        }
        return customerRepository.save(newCustomer);
    }

    private static String generateInvoiceNumber() {
        return "ORD-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}