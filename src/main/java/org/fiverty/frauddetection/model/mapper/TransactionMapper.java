package org.fiverty.frauddetection.model.mapper;

import org.fiverty.frauddetection.model.dto.TransactionRequestDto;
import org.fiverty.frauddetection.model.Transaction;

import java.time.Instant;

public class TransactionMapper {

    private TransactionMapper() {
        // Private constructor to prevent instantiation SonarQube Rule: java:S1118
    }

    public static Transaction mapToEntity(TransactionRequestDto requestDto) {
        Transaction transaction = new Transaction();

        // Map flat fields
        transaction.setCustomerName(requestDto.getCustomerName());
        transaction.setIpAddress(requestDto.getIpAddress());

        // Map nested location fields
        TransactionRequestDto.LocationDto location = requestDto.getLocation();
        if (location != null) {
            transaction.setCustomerCity(location.getCity());
            transaction.setCustomerState(location.getState());
        }

        // Map payment details
        TransactionRequestDto.PaymentDetailsDto paymentDetails = requestDto.getPaymentDetails();
        if (paymentDetails != null) {
            transaction.setCardLast4(paymentDetails.getCardLast4());
            transaction.setNameOnCard(paymentDetails.getNameOnCard());
            transaction.setPurchaseAmount(paymentDetails.getPurchaseAmount());
        }

        // Map transaction details
        TransactionRequestDto.TransactionDetailsDto transactionDetails = requestDto.getTransactionDetails();
        if (transactionDetails != null) {
            transaction.setMerchantName(transactionDetails.getMerchantName());

            TransactionRequestDto.TransactionDetailsDto.MerchantLocationDto merchantLocation = transactionDetails.getMerchantLocation();
            if (merchantLocation != null) {
                transaction.setMerchantCity(merchantLocation.getCity());
                transaction.setMerchantState(merchantLocation.getState());
            }

            transaction.setPurchasedItemCount(transactionDetails.getPurchasedItemCount());
        }

        // Set additional mandatory fields
        transaction.setValidFrom(Instant.now().minusSeconds(3600)); // Set current timestamp (or modify as needed)
        transaction.setValidTo(Instant.now().plusSeconds(3600)); // Example: 1 hour validity
        transaction.setIsCurrent(true); // Default value for "isCurrent", assuming new transactions are "current"

        return transaction;
    }


}