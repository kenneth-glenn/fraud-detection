package org.fiverty.frauddetection.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "transaction_current")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @Column(name="customer_name", nullable = false)
    private String customerName;

    @Column(name="ip_address", nullable = false)
    private String ipAddress;

    @Column(name="customer_city", nullable = false)
    private String customerCity;

    @Column(name="customer_state", nullable = false, length = 2)
    private String customerState;

    @Column(name="card_last4")
    private String cardLast4;

    @Column(name="name_on_card", nullable = false)
    private String nameOnCard;

    @Column(name="purchase_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchaseAmount;

    @Column(name="merchant_name",nullable = false)
    private String merchantName;

    @Column(name="merchant_city",nullable = false)
    private String merchantCity;

    @Column(name="merchant_state",nullable = false, length = 2)
    private String merchantState;

    @Column(name="purchased_item_count",nullable = false)
    private Integer purchasedItemCount;

    @Column(name="valid_from",nullable = false)
    private Instant validFrom;

    @Column(name="valid_to",nullable = false)
    private Instant validTo;

    @Column(name="is_current",nullable = false)
    private Boolean isCurrent;
}
