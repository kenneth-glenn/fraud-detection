package org.fiverty.frauddetection.controller;

import org.fiverty.frauddetection.model.dto.TransactionRequestDto;
import org.fiverty.frauddetection.model.dto.TransactionResponseDto;
import org.fiverty.frauddetection.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Endpoint to evaluate a transaction and determine potential fraud signals.
     *
     * @param transaction the transaction to be scored
     * @return a ResponseEntity containing the transaction response DTO
     */
    @PostMapping("/score-transaction")
    public ResponseEntity<TransactionResponseDto> evaluateTransaction(@RequestBody TransactionRequestDto transaction) {
            return ResponseEntity.ok(fraudDetectionService.scoreTransaction(transaction));
    }

}