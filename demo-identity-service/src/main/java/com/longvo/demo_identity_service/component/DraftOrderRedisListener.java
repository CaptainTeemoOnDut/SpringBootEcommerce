package com.longvo.demo_identity_service.component;

import com.longvo.demo_identity_service.dto.DraftOrderCreatedEvent;
import com.longvo.demo_identity_service.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
public class DraftOrderRedisListener {

    @Autowired
    private RedisCacheService redisCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDraftOrderCreated(DraftOrderCreatedEvent event) {

        redisCacheService.setAsync(
                event.getRedisKey(),
                event.getOrderGroupRedis(),
                Duration.ofMinutes(15)
        );
    }
}
