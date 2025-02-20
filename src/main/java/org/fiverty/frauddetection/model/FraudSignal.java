package org.fiverty.frauddetection.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "fraud_signal")
public class FraudSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signal_id")
    private Long signalId;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SignalType signalType;

    @Column(nullable = false)
    private Boolean potentialFraud;

    @ElementCollection
//    @CollectionTable(name = "fraud_signal", joinColumns = @JoinColumn(name = "signal_id"))
    @Column(name = "details")
    private List<String> details;

    public enum SignalType {
        LOCATION, IP_ADDRESS, TRANSACTION, CARD_DETAILS
    }
}