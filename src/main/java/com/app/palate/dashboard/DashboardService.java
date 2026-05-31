package com.app.palate.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardOrderRepository    orderRepository;
    private final DashboardCustomerRepository customerRepository;

    public DashboardStats getStats(Instant from, Instant to) {
        // Shift back by the same duration to get the comparison window
        // e.g. Nov 1–30 → Oct 2–Nov 1 (same length, directly before)
        Duration period  = Duration.between(from, to);
        Instant  prevFrom = from.minus(period);
        Instant  prevTo   = from;

        // Revenue
        double currRevenue = orderRepository.sumRevenueBetween(from, to);
        double prevRevenue = orderRepository.sumRevenueBetween(prevFrom, prevTo);

        // Orders
        long currOrders = orderRepository.countOrdersBetween(from, to);
        long prevOrders = orderRepository.countOrdersBetween(prevFrom, prevTo);

        // Customers
        long currCustomers = customerRepository.countCustomersBetween(from, to);
        long prevCustomers = customerRepository.countCustomersBetween(prevFrom, prevTo);

        // Avg order value
        double currAvg = currOrders == 0 ? 0 : currRevenue / currOrders;
        double prevAvg = prevOrders == 0 ? 0 : prevRevenue / prevOrders;

        return new DashboardStats(
            currRevenue,   growth(prevRevenue,   currRevenue),
            currOrders,    growth(prevOrders,    currOrders),
            currCustomers, growth(prevCustomers, currCustomers),
            currAvg,       growth(prevAvg,       currAvg)
        );
    }

    private double growth(double prev, double curr) {
        if (prev == 0) return 0;
        return ((curr - prev) / prev) * 100.0;
    }

    private double growth(long prev, long curr) {
        return growth((double) prev, (double) curr);
    }
}