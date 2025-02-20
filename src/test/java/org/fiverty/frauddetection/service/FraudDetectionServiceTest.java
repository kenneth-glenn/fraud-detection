package org.fiverty.frauddetection.service;

import org.fiverty.frauddetection.model.FraudSignal;
import org.fiverty.frauddetection.model.Transaction;
import org.fiverty.frauddetection.model.dto.TransactionRequestDto;
import org.fiverty.frauddetection.model.dto.TransactionResponseDto;
import org.fiverty.frauddetection.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    public FraudDetectionServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void scoreTransaction_ValidTransaction_NoFraudSignals() {
        TransactionRequestDto transaction = createValidTransactionRequestDto();
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(transaction);
//        mappedTransaction.setTransactionId(new UUID(1,1));
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertFalse(response.getFraudSignals().stream().allMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }


    @Test
    void scoreTransaction_InvalidTransaction_ThrowsException() {
        TransactionRequestDto invalidTransaction = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fraudDetectionService.scoreTransaction(invalidTransaction));

        assertEquals("Transaction cannot be null or empty", exception.getMessage());
    }

    @Test
    void scoreTransaction_FraudulentIpAddress_FraudSignalRaised() {
        TransactionRequestDto transaction = createValidTransactionRequestDto();
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(transaction);

        mappedTransaction.setIpAddress("10.0.0.1");
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    void scoreTransaction_PrivateIpAddress_FraudSignalRaised() {
        TransactionRequestDto transaction = createValidTransactionRequestDto();
        transaction.setIpAddress("192.168.0.10");
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(transaction);
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));
        assertEquals(1, response.getFraudSignals().stream()
                .filter(signal -> signal.getDetails().contains("IP Address is in a private range and may use a VPN to mask its origin")).count());

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    void scoreTransaction_CitiesMatchStatesDiffer_NoFraudSignal() {
        TransactionRequestDto transaction = createValidTransactionRequestDto();
        transaction.setLocation(new TransactionRequestDto.LocationDto("Springfield", "IL"));
        transaction.getTransactionDetails().getMerchantLocation().setCity("Springfield");
        transaction.getTransactionDetails().getMerchantLocation().setState("MO");
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(transaction);
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertTrue(response.getFraudSignals().stream()
                .anyMatch(FraudSignal::getPotentialFraud));
    }

    @Test
    void scoreTransaction_NameMismatch_FraudSignalRaised() {
        TransactionRequestDto validTransactionRequestDto = createValidTransactionRequestDto();
        validTransactionRequestDto.setTransactionDetails(new TransactionRequestDto.TransactionDetailsDto("Merchant Name", new TransactionRequestDto.TransactionDetailsDto.MerchantLocationDto("Chicago", "IL"), 0));
        validTransactionRequestDto.setPaymentDetails(new TransactionRequestDto.PaymentDetailsDto("1234", "Mismatched Name", new BigDecimal("100.00")));
        validTransactionRequestDto.setCustomerName("James Earl Jones");
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(validTransactionRequestDto);

        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(validTransactionRequestDto);
        verify(transactionRepository, times(1)).saveAndFlush(argThat(transaction ->
                transaction.getCustomerName().equals(mappedTransaction.getCustomerName()) &&
                        transaction.getNameOnCard().equals(mappedTransaction.getNameOnCard()) &&
                        transaction.getCardLast4().equals(mappedTransaction.getCardLast4())
        ));


        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

    }

    @Test
    void scoreTransaction_InvalidState_FraudSignalRaised() {
        TransactionRequestDto transaction = createValidTransactionRequestDto();
        transaction.setLocation(new TransactionRequestDto.LocationDto("Springfield", "XX"));
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(transaction);
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    void scoreTransaction_NoPurchasedItems_FraudSignalRaised() {

        TransactionRequestDto validTransactionRequestDto = createValidTransactionRequestDto();
        validTransactionRequestDto.setTransactionDetails(new TransactionRequestDto.TransactionDetailsDto("Merchant Name", new TransactionRequestDto.TransactionDetailsDto.MerchantLocationDto("Chicago", "IL"), 0));
        validTransactionRequestDto.setPaymentDetails(new TransactionRequestDto.PaymentDetailsDto("1234", "James", new BigDecimal("100.00")));
        Transaction mappedTransaction = fraudDetectionService.mapTransactionRequestToTransaction(validTransactionRequestDto);
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(mappedTransaction);

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(validTransactionRequestDto);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    private TransactionRequestDto createValidTransactionRequestDto() {
        return TransactionRequestDto.builder()
                .customerName("John Doe")
                .ipAddress("11.168.1.1")
                .location(new TransactionRequestDto.LocationDto("Springfield", "IL"))
                .paymentDetails(new TransactionRequestDto.PaymentDetailsDto("1234", "John Doe", new BigDecimal("100.00")))
                .transactionDetails(
                        new TransactionRequestDto.TransactionDetailsDto(
                                "Merchant Name",
                                new TransactionRequestDto.TransactionDetailsDto.MerchantLocationDto("Chicago", "IL"),
                                1
                        )
                )
                .build();
    }

}