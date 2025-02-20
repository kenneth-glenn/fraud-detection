package org.fiverty.frauddetection.service;

import org.fiverty.frauddetection.model.FraudSignal;
import org.fiverty.frauddetection.model.Transaction;
import org.fiverty.frauddetection.model.dto.TransactionResponseDto;
import org.fiverty.frauddetection.repository.FraudSignalRepository;
import org.fiverty.frauddetection.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudSignalRepository fraudSignalRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    public FraudDetectionServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void scoreTransaction_ValidTransaction_NoFraudSignals() {
        Transaction transaction = createValidTransaction();
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(fraudSignalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).save(transaction);
        verify(fraudSignalRepository, times(1)).saveAll(anyList());
    }

    @Test
    void scoreTransaction_InvalidTransaction_ThrowsException() {
        Transaction invalidTransaction = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fraudDetectionService.scoreTransaction(invalidTransaction));

        assertEquals("Transaction cannot be null or empty", exception.getMessage());
        verifyNoInteractions(fraudSignalRepository);
    }

    @Test
    void scoreTransaction_FraudulentIpAddress_FraudSignalRaised() {
        Transaction transaction = createValidTransaction();
        transaction.setIpAddress("10.0.0.1");
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(fraudSignalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).save(transaction);
        verify(fraudSignalRepository, times(1)).saveAll(anyList());
    }

    @Test
    void scoreTransaction_NameMismatch_FraudSignalRaised() {
        Transaction transaction = createValidTransaction();
        transaction.setNameOnCard("Mismatch Name");
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(fraudSignalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).save(transaction);
        verify(fraudSignalRepository, times(1)).saveAll(anyList());
    }

    @Test
    void scoreTransaction_InvalidState_FraudSignalRaised() {
        Transaction transaction = createValidTransaction();
        transaction.setCustomerState("XX");
        transaction.setMerchantState("YY");
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(fraudSignalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).save(transaction);
        verify(fraudSignalRepository, times(1)).saveAll(anyList());
    }

    @Test
    void scoreTransaction_NoPurchasedItems_FraudSignalRaised() {
        Transaction transaction = createValidTransaction();
        transaction.setPurchasedItemCount(0);
        transaction.setPurchaseAmount(new BigDecimal("100.00"));
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(fraudSignalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDto response = fraudDetectionService.scoreTransaction(transaction);

        assertNotNull(response);
        assertNotNull(response.getFraudSignals());
        assertEquals(4, response.getFraudSignals().size());
        assertTrue(response.getFraudSignals().stream().anyMatch(FraudSignal::getPotentialFraud));

        verify(transactionRepository, times(1)).save(transaction);
        verify(fraudSignalRepository, times(1)).saveAll(anyList());
    }

    private Transaction createValidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setCustomerName("John Doe");
        transaction.setIpAddress("192.168.1.1");
        transaction.setCustomerCity("Springfield");
        transaction.setCustomerState("IL");
        transaction.setCardLast4("1234");
        transaction.setNameOnCard("John Doe");
        transaction.setPurchaseAmount(new BigDecimal("100.00"));
        transaction.setMerchantName("Shopify Merchant");
        transaction.setMerchantCity("Chicago");
        transaction.setMerchantState("IL");
        transaction.setPurchasedItemCount(1);
        transaction.setValidFrom(Instant.now());
        transaction.setIsCurrent(true);
        return transaction;
    }
}