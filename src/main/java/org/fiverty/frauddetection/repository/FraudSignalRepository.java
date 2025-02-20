package org.fiverty.frauddetection.repository;

import org.fiverty.frauddetection.model.FraudSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudSignalRepository extends JpaRepository<FraudSignal, Long> {
}