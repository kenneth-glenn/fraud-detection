package org.fiverty.frauddetection.model.dto;

import lombok.Builder;
import lombok.Data;
import org.fiverty.frauddetection.model.FraudSignal;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TransactionResponseDto {
    private String customerName;
    private String ipAddress;
    private Location location;
    private PaymentDetails paymentDetails;
    private TransactionDetails transactionDetails;
    private List<FraudSignal> fraudSignals;

    @Data
    @Builder
    public static class Location {
        private String city;
        private String state;
    }

    @Data
    @Builder
    public static class PaymentDetails {
        private String cardLast4;
        private String nameOnCard;
        private BigDecimal purchaseAmount;
    }

    @Data
    @Builder
    public static class TransactionDetails {
        private String merchantName;
        private Location merchantLocation;
        private Integer purchasedItemCount;
    }
}
