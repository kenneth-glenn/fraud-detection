package org.fiverty.frauddetection;

import org.fiverty.frauddetection.controller.FraudDetectionController;
import org.fiverty.frauddetection.service.FraudDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FrauddetectionApplicationTests {

    @Autowired
    private FraudDetectionController controller;

    @Autowired
    private FraudDetectionService service;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(service).isNotNull();
    }

}
