package org.fiverty.frauddetection.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    private String customerName;

    private String ipAddress;

    private LocationDto location;

    private PaymentDetailsDto paymentDetails;

    private TransactionDetailsDto transactionDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private String city;
        private String state;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailsDto {
        private String cardLast4;
        private String nameOnCard;
        private BigDecimal purchaseAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetailsDto {
        private String merchantName;
        private MerchantLocationDto merchantLocation;
        private Integer purchasedItemCount;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MerchantLocationDto {
            private String city;
            private String state;
        }
    }

}