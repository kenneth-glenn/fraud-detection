package org.fiverty.frauddetection.service;

import jakarta.transaction.Transactional;
import org.fiverty.frauddetection.model.FraudSignal;
import org.fiverty.frauddetection.model.Transaction;
import org.fiverty.frauddetection.model.dto.TransactionRequestDto;
import org.fiverty.frauddetection.model.dto.TransactionResponseDto;
import org.fiverty.frauddetection.model.mapper.TransactionMapper;
import org.fiverty.frauddetection.repository.FraudSignalRepository;
import org.fiverty.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    private final TransactionRepository transactionRepository;

    // These items should be stored in a database to allow for more flexibility.
    private static final String CARD_DETAILS_FRAUD_UNEXPECTED = "Card details do not look fraudulent";
    private static final String CARD_DETAILS_NAME_MISMATCH = "Name on card does not match the customer's name";
    private static final String CARD_DETAILS_NULL_OR_EMPTY = "Transaction, customer name, or name on card cannot be null";
    private static final String CARD_DETAILS_UNKNOWN_STATE = "Card details unknown";
    private static final String CUSTOMER_AND_MERCHANT_LOCATION_DETAILS_MISSING = "Customer and merchant city/state cannot be null";
    private static final String CUSTOMER_AND_MERCHANT_LOCATIONS_MATCH = "Customer and merchant locations match";
    private static final String FRAUD_SIGNAL_NULL_OR_EMPTY = "Fraud Signal cannot be null";
    private static final String IP_ADDRESS_NOT_FRAUDULENT_MALICIOUS = "IP Address is not known to be fraudulent or malicious";
    private static final String IP_ADDRESS_SUSPICIOUS_RANGE = "IP Address is in a private range and may use a VPN to mask its origin";
    private static final String LOCATIONS_CUSTOMER_AND_MERCHANT_DIFFER = "Customer and merchant locations differ";
    private static final String LOCATIONS_CUSTOMER_AND_MERCHANT_SAME_STATE = "Customer and merchant are in the same state";
    private static final String LOCATIONS_INVALID_STATE = "Invalid customer or merchant state abbreviation";
    private static final String POTENTIAL_FRAUD_RISK = "Potential risk of fraudulent activity";
    private static final String SIGNAL_DETAILS_NULL_OR_EMPTY = "Signal details cannot be null or empty";
    private static final String TRANSACTION_DETAILS_FRAUD_UNEXPECTED = "Transaction details do not look fraudulent";
    private static final String TRANSACTION_DETAILS_NULL_OR_EMPTY = "Transaction cannot be null or empty";
    private static final String TRANSACTION_DETAILS_PURCHASE_ITEM_COUNT = "Purchased item count is less than 1 while purchase amount is positive";

    // Fraud Signals, should be stored in DB
    private static final boolean FRAUD_RISK = true;
    private static final boolean NO_FRAUD_RISK = false;


    public FraudDetectionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Processes a transaction request by saving the transaction, generating fraud signals,
     * and returning a response DTO containing the transaction details and fraud signals.
     *
     * @param transactionRequestDto the transaction request containing all necessary information
     *                              to process the transaction
     * @return a TransactionResponseDto containing details of the saved transaction and
     *         the associated fraud signals
     * @throws IllegalArgumentException if the input transaction request is invalid
     */
    @Transactional
    public TransactionResponseDto scoreTransaction(TransactionRequestDto transactionRequestDto) throws IllegalArgumentException {
        if (transactionRequestDto == null) {
            throw new IllegalArgumentException(TRANSACTION_DETAILS_NULL_OR_EMPTY);
        }

        Transaction unsavedTransaction = mapTransactionRequestToTransaction(transactionRequestDto);
        if (unsavedTransaction == null) {
            throw new IllegalStateException("Mapping transaction request to entity resulted in null");
        }

        logger.info("Mapped transaction request to Transaction entity: {}", unsavedTransaction);

        Transaction savedTransaction = transactionRepository.saveAndFlush(unsavedTransaction);
        logger.info("Transaction saved with ID: {}", savedTransaction.getTransactionId());

        List<FraudSignal> signals = generateFraudSignals(savedTransaction);
        logger.info("Generated {} fraud signals for transaction ID: {}", signals.size(), savedTransaction.getTransactionId());

        return toTransactionResponseDto(savedTransaction, signals);

    }

    /**
     * Maps a TransactionRequestDto object to a Transaction entity.
     *
     * @param transactionRequestDto the data transfer object containing transaction details
     *                               to be mapped to a Transaction entity
     * @return the mapped Transaction entity
     */
    Transaction mapTransactionRequestToTransaction(TransactionRequestDto transactionRequestDto) {
        return TransactionMapper.mapToEntity(transactionRequestDto);
    }


    /**
     * Converts a Transaction and a list of FraudSignal objects into a TransactionResponseDto.
     *
     * @param transaction the transaction object containing details of the transaction
     * @param signals     the list of fraud signals associated with the transaction
     * @return a TransactionResponseDto constructed using the provided transaction and fraud signals
     * @throws IllegalArgumentException if the transaction or signals are null
     */
    private TransactionResponseDto toTransactionResponseDto(Transaction transaction, List<FraudSignal> signals) throws IllegalArgumentException {
        if (transaction == null) {
            throw new IllegalArgumentException(TRANSACTION_DETAILS_NULL_OR_EMPTY);
        }
        if (signals == null) {
            throw new IllegalArgumentException(FRAUD_SIGNAL_NULL_OR_EMPTY);
        }

        TransactionResponseDto.Location customerLocation = TransactionResponseDto.Location.builder()
                .city(transaction.getCustomerCity())
                .state(transaction.getCustomerState())
                .build();
        TransactionResponseDto.Location merchantLocation = TransactionResponseDto.Location.builder()
                .city(transaction.getMerchantCity())
                .state(transaction.getMerchantState())
                .build();

        return TransactionResponseDto.builder()
                .customerName(transaction.getCustomerName())
                .ipAddress(transaction.getIpAddress())
                .location(customerLocation)
                .paymentDetails(TransactionResponseDto.PaymentDetails.builder()
                        .cardLast4(transaction.getCardLast4())
                        .nameOnCard(transaction.getNameOnCard())
                        .purchaseAmount(transaction.getPurchaseAmount())
                        .build())
                .transactionDetails(TransactionResponseDto.TransactionDetails.builder()
                        .merchantName(transaction.getMerchantName())
                        .merchantLocation(merchantLocation)
                        .purchasedItemCount(transaction.getPurchasedItemCount())
                        .build())
                .fraudSignals(signals)
                .build();
    }

    /**
     * Generates a list of fraud signals based on the provided transaction data.
     *
     * @param transaction the transaction object containing details to evaluate for potential fraud
     * @return a list of FraudSignal objects representing potential fraud indicators
     */
    private List<FraudSignal> generateFraudSignals(Transaction transaction) throws IllegalArgumentException {
        List<FraudSignal> signals = new ArrayList<>();
        signals.add(checkLocation(transaction));
        logger.info("Checking location details for transaction ID: {} location details {}", transaction.getTransactionId(), signals.get(0).getDetails());
        signals.add(checkIpAddress(transaction));
        logger.info("Checking IP details for transaction ID: {} location details {}", transaction.getTransactionId(), signals.get(1).getDetails());
        signals.add(checkTransactionDetails(transaction));
        logger.info("Checking Transaction details for transaction ID: {} location details {}", transaction.getTransactionId(), signals.get(2).getDetails());
        signals.add(checkCardDetails(transaction));
        logger.info("Checking Card details for transaction ID: {} location details {}", transaction.getTransactionId(), signals.get(3).getDetails());
        return signals;
    }

    /**
     * Checks the location details of a given transaction and determines potential fraud signals
     * based on the location data, such as city and state consistency.
     *
     * @param transaction the transaction object containing details to evaluate for potential fraud.
     * @return a FraudSignal object containing the results of the location-based fraud evaluation.
     * @throws IllegalArgumentException if the transaction is null or any required location details are missing.
     */
    private FraudSignal checkLocation(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(TRANSACTION_DETAILS_NULL_OR_EMPTY);
        }
        if (transaction.getCustomerCity() == null || transaction.getMerchantCity() == null ||
                transaction.getCustomerState() == null || transaction.getMerchantState() == null) {
            throw new IllegalArgumentException();
        }

        FraudSignal signal = new FraudSignal();
        signal.setTransaction(transaction);
        signal.setSignalType(FraudSignal.SignalType.LOCATION);
        signal.setDetails(List.of("Location Details"));

        logger.info("Checking location details for transaction ID: {}", transaction.getTransactionId());
        updateSignalDetailsBasedOnLocation(signal, isSameCity(transaction), isSameState(transaction));
        logger.info("Signal details {}",signal.getDetails());

        return signal;
    }

    /**
     * Checks if the customer and merchant are in the same city.
     *
     * @param transaction the transaction to compare customer and merchant city
     * @return true if the customer and merchant are in the same city, ignoring case; false otherwise
     * @throws IllegalArgumentException if transaction, customer city, or merchant city is null
     */
    private boolean isSameCity(Transaction transaction) {
        if (transaction == null || transaction.getCustomerCity() == null || transaction.getMerchantCity() == null) {
            throw new IllegalArgumentException(CUSTOMER_AND_MERCHANT_LOCATION_DETAILS_MISSING);
        }
        return transaction.getCustomerCity().equalsIgnoreCase(transaction.getMerchantCity());
    }

    /**
     * Checks if the customer and merchant are in the same state.
     *
     * @param transaction the transaction to compare customer and merchant state
     * @return true if the customer and merchant are in the same state, ignoring case; false otherwise
     * @throws IllegalArgumentException if transaction, customer state, or merchant state is null
     */
    private boolean isSameState(Transaction transaction) {
        if (transaction == null || transaction.getCustomerState() == null || transaction.getMerchantState() == null) {
            throw new IllegalArgumentException(CUSTOMER_AND_MERCHANT_LOCATION_DETAILS_MISSING);
        }
        return transaction.getCustomerState().equalsIgnoreCase(transaction.getMerchantState());
    }

    /**
     * Updates the details of a fraud signal based on the transaction location comparison
     * between the customer's location and the merchant's location.
     *
     * @param signal      the {@link FraudSignal} object containing the transaction data to evaluate
     * @param isSameCity  a boolean indicating whether the customer and merchant are in the same city
     * @param isSameState a boolean indicating whether the customer and merchant are in the same state
     * @throws IllegalArgumentException if the {@code signal} is null or the transaction within it is null
     */
    private void updateSignalDetailsBasedOnLocation(FraudSignal signal,
                                                    boolean isSameCity,
                                                    boolean isSameState) {
        if (signal == null) {
            throw new IllegalArgumentException(FRAUD_SIGNAL_NULL_OR_EMPTY);
        }
        if (signal.getTransaction() == null) {
            throw new IllegalArgumentException(TRANSACTION_DETAILS_NULL_OR_EMPTY);
        }

        if (!areStatesValid(signal)) {
            markAsPotentialFraud(signal, List.of(LOCATIONS_INVALID_STATE, POTENTIAL_FRAUD_RISK));
            return;
        }

        logger.info("isSameCity: {}, isSameState: {}", isSameCity, isSameState);

        if (isSameCity && isSameState) {
            markAsNoFraud(signal, List.of(CUSTOMER_AND_MERCHANT_LOCATIONS_MATCH));
        } else if (isSameState) {
            markAsNoFraud(signal, List.of(LOCATIONS_CUSTOMER_AND_MERCHANT_SAME_STATE));
        } else {
            markAsPotentialFraud(signal, List.of(
                    LOCATIONS_CUSTOMER_AND_MERCHANT_DIFFER,
                    POTENTIAL_FRAUD_RISK
            ));
        }
    }

    /**
     * Determines if the states associated with the customer and merchant in the given fraud signal are valid US states.
     *
     * @param signal the fraud signal containing transaction details, including customer and merchant states
     * @return true if both the customer state and merchant state are valid US states, false otherwise
     */
    private boolean areStatesValid(FraudSignal signal) {
        return isValidUSState(signal.getTransaction().getCustomerState()) &&
                isValidUSState(signal.getTransaction().getMerchantState());
    }

    /**
     * Marks the given fraud signal with the specified fraud risk and associates additional details.
     *
     * @param signal    the FraudSignal object to update
     * @param fraudRisk the fraud risk level to set (e.g., FRAUD_RISK or NO_FRAUD_RISK)
     * @param details   a list of strings providing additional details about the signal
     * @throws IllegalArgumentException if the signal or details are null or empty
     */
    private void markFraudSignal(FraudSignal signal, boolean fraudRisk, List<String> details) throws IllegalArgumentException {
        if (signal == null) {
            throw new IllegalArgumentException(FRAUD_SIGNAL_NULL_OR_EMPTY);
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException(SIGNAL_DETAILS_NULL_OR_EMPTY);
        }
        logger.info("Setting fraud risk {} with details {}", fraudRisk, details);
        signal.setPotentialFraud(fraudRisk);
        signal.setDetails(details);
    }


    /**
     * Marks the provided fraud signal as a potential fraud by associating it with the fraud risk category
     * and including additional details if available.
     *
     * @param signal  the fraud signal object that is being marked as potential fraud
     * @param details a list of details or reasons supporting the fraud classification
     * @throws IllegalArgumentException if the signal or details are null
     */
    private void markAsPotentialFraud(FraudSignal signal, List<String> details) throws IllegalArgumentException {
        if (signal == null) {
            throw new IllegalArgumentException("FraudSignal cannot be null");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Details cannot be null or empty");
        }
        markFraudSignal(signal, FRAUD_RISK, details);
    }

    /**
     * Marks the given fraud signal as no fraud detected and updates the details.
     *
     * @param signal  The fraud signal to update.
     * @param details A list of details providing context or reason for marking the signal as no fraud.
     * @throws IllegalArgumentException if the signal or details are null or empty
     */
    private void markAsNoFraud(FraudSignal signal, List<String> details) throws IllegalArgumentException {
        if (signal == null) {
            throw new IllegalArgumentException("FraudSignal cannot be null");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Details cannot be null or empty");
        }
        markFraudSignal(signal, NO_FRAUD_RISK, details);
    }


    /**
     * Checks whether the provided string corresponds to a valid US state abbreviation.
     *
     * @param state the state abbreviation to validate. It should be a two-letter
     *              state code (e.g., "CA" for California) and is case-insensitive.
     * @return true if the provided string is a valid US state abbreviation,
     * false otherwise.
     */
    private static boolean isValidUSState(String state) {
        return List.of(
                "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID",
                "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS",
                "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK",
                "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV",
                "WI", "WY"
        ).contains(state.toUpperCase());
    }


    /**
     * Checks the IP address of a given transaction for fraud potential.
     * which could be used to mask a fraudulent action.
     * Service can be enhanced with location tools like IP2Location.io. Allows for IPv4 and IPv6 lookups.
     *
     * @param transaction the transaction for which the IP address is checked
     * @return a FraudSignal object indicating the result of the IP address check
     * @throws IllegalArgumentException if the transaction or its IP address is null
     */
    private FraudSignal checkIpAddress(Transaction transaction) throws IllegalArgumentException {
        if (transaction.getIpAddress() == null) {
            throw new IllegalArgumentException("Transaction or IP address cannot be null");
        }

        logger.info("Checking IP address for transaction ID: {}", transaction.getTransactionId());

        String ipAddress = transaction.getIpAddress();

        FraudSignal signal = new FraudSignal();
        signal.setTransaction(transaction);
        signal.setSignalType(FraudSignal.SignalType.IP_ADDRESS);

        if (isPrivateIp(ipAddress)) {
            logger.info("IP address {} is private", ipAddress);
            signal.setPotentialFraud(true);
            signal.setDetails(List.of(IP_ADDRESS_SUSPICIOUS_RANGE));
        } else {
            signal.setPotentialFraud(false);
            signal.setDetails(List.of(IP_ADDRESS_NOT_FRAUDULENT_MALICIOUS));
        }
        return signal;
    }

    /**
     * Checks whether the given IP address falls within private IP ranges.
     * Supports only IPv4 CIDR ranges 10.x.x.x, 192.168.x.x, and 172.16.x.x to 172.31.x.x.
     *
     * @param ipAddress the IP address to verify
     * @return true if the IP address falls in a private range; false otherwise
     * @throws IllegalArgumentException if the provided IP address is null or empty
     */
    private boolean isPrivateIp(String ipAddress) throws IllegalArgumentException {
        logger.info("Checking if IP address is private: {}", ipAddress);
        if (ipAddress == null || ipAddress.isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        return ipAddress.startsWith("10.") || ipAddress.startsWith("192.168.")
                || (ipAddress.startsWith("172.") && isInRange(ipAddress, 16, 31));
    }

    /**
     * Checks if the second octet of the provided IP address falls within the specified range.
     *
     * @param ipAddress The IP address in dotted-decimal notation.
     * @param lower     The lower bound of the range (inclusive).
     * @param upper     The upper bound of the range (inclusive).
     * @return true if the second octet of the IP address is within the range [lower, upper]; false otherwise
     * or if the input is invalid.
     * @throws NumberFormatException          if the IP address contains a non-numeric second octet.
     * @throws ArrayIndexOutOfBoundsException if the IP address is improperly formatted.
     */
    private boolean isInRange(String ipAddress, int lower, int upper) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        logger.info("Checking if IP address is in range: {}-{}", lower, upper);
        try {
            int secondOctet = Integer.parseInt(ipAddress.split("\\.")[1]);
            return secondOctet >= lower && secondOctet <= upper;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
     * Analyzes the details of a given transaction to detect potential fraud risks, based on
     * purchased item count and purchase amount, and returns a fraud signal object.
     *
     * @param transaction the transaction object containing details such as purchased item count
     *                    and purchase amount
     * @return FraudSignal object indicating the fraud risk assessment. It contains either a
     * fraud risk or no fraud risk based on the transaction details.
     * @throws IllegalArgumentException if the transaction is null or contains invalid details.
     */
    private FraudSignal checkTransactionDetails(Transaction transaction) throws IllegalArgumentException {
        if (transaction == null) {
            throw new IllegalArgumentException(TRANSACTION_DETAILS_NULL_OR_EMPTY);
        }
        logger.info("Checking transaction details for transaction ID: {}", transaction.getTransactionId());

        FraudSignal signal = new FraudSignal();
        signal.setTransaction(transaction);

        if (transaction.getPurchasedItemCount() < 1 && transaction.getPurchaseAmount().compareTo(BigDecimal.ZERO) > 0) {
            markFraudSignal(signal, FRAUD_RISK, List.of(TRANSACTION_DETAILS_PURCHASE_ITEM_COUNT, POTENTIAL_FRAUD_RISK));
        } else {
            markFraudSignal(signal, NO_FRAUD_RISK, List.of(TRANSACTION_DETAILS_FRAUD_UNEXPECTED));
        }

        return signal;
    }

    /**
     * Checks the card details from a transaction to determine potential fraud signals.
     *
     * @param transaction the transaction containing details such as customer name and name on the card
     * @return a FraudSignal object indicating whether the card details suggest potential fraud,
     * along with the signal type and fraud details
     * @throws IllegalArgumentException if the transaction or required transaction fields are null
     */
    private FraudSignal checkCardDetails(Transaction transaction) throws IllegalArgumentException {
        if (transaction == null || transaction.getCustomerName() == null || transaction.getNameOnCard() == null) {
            throw new IllegalArgumentException(CARD_DETAILS_NULL_OR_EMPTY);
        }

        logger.info("Checking card details for transaction ID: {}", transaction.getTransactionId());

        FraudSignal signal = new FraudSignal();
        signal.setTransaction(transaction);
        signal.setSignalType(FraudSignal.SignalType.CARD_DETAILS);
        signal.setDetails(List.of(CARD_DETAILS_UNKNOWN_STATE));

        if (!transaction.getCustomerName().equalsIgnoreCase(transaction.getNameOnCard())) {
            signal.setPotentialFraud(true);
            signal.setDetails(List.of(CARD_DETAILS_NAME_MISMATCH));
        } else {
            signal.setPotentialFraud(false);
            signal.setDetails(List.of(CARD_DETAILS_FRAUD_UNEXPECTED));
        }

        return signal;
    }
}
