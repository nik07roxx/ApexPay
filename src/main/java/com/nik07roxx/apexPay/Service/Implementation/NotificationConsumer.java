package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "apexpay-transactions", groupId = "apexpay-txn-group")
    public void listenTransactionEvents(TransactionResponse recordPayload) {
        log.info("📩 Kafka Event Triggered! Generating notification receipt context...");
        log.info("Processing ledger ID: {}", recordPayload.transactionReference());
        log.info("Source Route Account: {} | Transferred Amount: {}",
                recordPayload.sourceAccount(), recordPayload.sourceAmount());

        // FUTURE TRACK: This is where we will hook up JavaMailSender to fire actual emails!
        log.info("Receipt dispatch logging processed successfully. Sending email.");

        // Same email, so deposit or withdraw, one email needed
        if(recordPayload.sourceEmail().equals(recordPayload.targetEmail()))
        {
            String subject = "Apex Pay: Account Transaction Update";
            String body = String.format(
                    "Dear Customer,\n\n" +
                            "Your account has been successfully processed for a %s.\n\n" +
                            "Transaction Details:\n" +
                            "▪ Reference ID: %s\n" +
                            "▪ Type: %s\n" +
                            "▪ Amount: %s %s\n" +
                            "▪ Description: %s\n" +
                            "▪ Date & Time: %s\n\n" +
                            "Thank you for choosing Apex Pay!\n\n" +
                            "Best regards,\n" +
                            "Apex Pay Support Team",
                    recordPayload.transactionType().toString().toLowerCase(),
                    recordPayload.transactionReference(),
                    recordPayload.transactionType(),
                    recordPayload.targetAmount(),
                    recordPayload.targetCurrency(),
                    recordPayload.description() != null ? recordPayload.description() : "N/A",
                    recordPayload.timestamp()
            );

            emailService.sendEmail(recordPayload.targetEmail(), subject, body);
        }
        else // Different emails, transfer, two emails needed for both parties
        {
            // 1. Send Debit Notification to the Sender (Source Email)
            String senderSubject = "Apex Pay: Account Debited Alert";
            String senderBody = String.format(
                    "Dear Customer,\n\n" +
                            "Your account (%s) has been debited for a fund transfer.\n\n" +
                            "Transaction Details:\n" +
                            "▪ Reference ID: %s\n" +
                            "▪ Sent To Account: %s\n" +
                            "▪ Debited Amount: %s %s\n" +
                            "▪ Date & Time: %s\n\n" +
                            "If this was not initiated by you, please contact support immediately.\n\n" +
                            "Best regards,\n" +
                            "Apex Pay Support Team",
                    recordPayload.sourceAccount(),
                    recordPayload.transactionReference(),
                    recordPayload.targetAccount(),
                    recordPayload.sourceAmount(),
                    recordPayload.sourceCurrency(),
                    recordPayload.timestamp()
            );
            emailService.sendEmail(recordPayload.sourceEmail(), senderSubject, senderBody);

            // 2. Send Credit Notification to the Receiver (Target Email)
            String receiverSubject = "Apex Pay: Account Credited Alert";
            String receiverBody = String.format(
                    "Dear Customer,\n\n" +
                            "Good news! Your account (%s) has been credited with funds.\n\n" +
                            "Transaction Details:\n" +
                            "▪ Reference ID: %s\n" +
                            "▪ Received From Account: %s\n" +
                            "▪ Credited Amount: %s %s\n" +
                            "▪ Description: %s\n" +
                            "▪ Date & Time: %s\n\n" +
                            "Log in to your Apex Pay app to view your updated balance.\n\n" +
                            "Best regards,\n" +
                            "Apex Pay Support Team",
                    recordPayload.targetAccount(),
                    recordPayload.transactionReference(),
                    recordPayload.sourceAccount(),
                    recordPayload.targetAmount(),
                    recordPayload.targetCurrency(),
                    recordPayload.description() != null ? recordPayload.description() : "N/A",
                    recordPayload.timestamp()
            );
            emailService.sendEmail(recordPayload.targetEmail(), receiverSubject, receiverBody);
        }
    }
}
