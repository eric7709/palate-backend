package com.app.palate.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardOrderRepository orderRepository;
    private static final ZoneOffset WAT = ZoneOffset.of("+01:00");

    public DashboardDTO getDashboardData() {
        Instant now = Instant.now();
        Instant startOfDay = LocalDate.now(WAT).atStartOfDay().toInstant(WAT);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);
        Instant start30DaysAgo = now.minus(30, ChronoUnit.DAYS);

        return new DashboardDTO(
                getPaidOrdersVolumeByHour(startOfDay, endOfDay),
                getAvgOrderValueByTable(start30DaysAgo, now),
                orderRepository.getTotalOrderVolume(startOfDay, endOfDay),
                getPeakHourToday(startOfDay, endOfDay));
    }

    private List<OrderHourDTO> getPaidOrdersVolumeByHour(Instant start, Instant end) {
        List<Object[]> results = orderRepository.getPaidOrdersVolumeByHour(start, end);
        List<OrderHourDTO> hourly = new ArrayList<>();

        for (int i = 0; i < 24; i++)
            hourly.add(new OrderHourDTO(String.format("%02d:00", i), 0L));

        for (Object[] row : results) {
            int hour = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            if (hour >= 0 && hour < 24) {
                hourly.set(hour, new OrderHourDTO(String.format("%02d:00", hour), count));
            }
        }
        return hourly;
    }

    private List<TableAvgDTO> getAvgOrderValueByTable(Instant start, Instant end) {
        return orderRepository.getAvgOrderValueByTable(start, end).stream()
                .map(row -> new TableAvgDTO((String) row[0], ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());
    }

    private PeakHourDTO getPeakHourToday(Instant start, Instant end) {
        List<Object[]> results = orderRepository.getPeakOrderHour(start, end);
        if (results.isEmpty())
            return new PeakHourDTO("N/A", 0L);

        Object[] top = results.get(0);
        return new PeakHourDTO(String.format("%02d:00", ((Number) top[0]).intValue()), ((Number) top[1]).longValue());
    }

  
  
}