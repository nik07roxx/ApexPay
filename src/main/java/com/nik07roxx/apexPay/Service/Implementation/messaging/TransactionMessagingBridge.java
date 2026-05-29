package com.nik07roxx.apexPay.Service.Implementation.messaging;

import com.nik07roxx.apexPay.DTO.Transactions.TransactionCompleteEvent;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionMessagingBridge {

    private final KafkaTemplate<String, TransactionResponse> kafkaTemplate;

    // This listener WILL NOT run until the calling DB transaction hits a successful COMMIT state!
    @Async // Separate thread to publish to Kafka
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCommit(TransactionCompleteEvent event) {
        TransactionResponse response = event.transactionResponse();
        log.info("Database transaction committed successfully! Now routing to Kafka broker safely.");

        kafkaTemplate.send("apexpay-transactions", response.transactionReference(), response);
    }
}
