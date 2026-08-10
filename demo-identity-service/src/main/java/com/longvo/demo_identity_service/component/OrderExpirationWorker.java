package com.longvo.demo_identity_service.component;

import com.longvo.demo_identity_service.dto.DraftOrderCreatedEvent;
import com.longvo.demo_identity_service.service.OrderExpirationRedisService;
import com.longvo.demo_identity_service.service.OrderService;
import com.longvo.demo_identity_service.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationWorker {

    private final OrderExpirationRedisService expirationRedisService;
    private final OrderService orderService;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 3000)
    public void processExpiredOrders() {

        long now = System.currentTimeMillis();

        Set<String> expiredOrders =
                expirationRedisService.getExpiredOrders(now, BATCH_SIZE);

        List<Long> expiredOrderIds = expiredOrders.stream()
                .map(Long::valueOf)
                .toList();

        if (expiredOrders == null || expiredOrders.isEmpty()) {
            return;
        }

            try {

                log.info("Processing expired order:");

                orderService.cancelExpiredOrder(expiredOrderIds);
                for (String orderId : expiredOrders) {
                    expirationRedisService.removeOrder(orderId);
                }


            } catch (Exception e) {

                log.error("Failed processing order", e);

            }

    }
}
