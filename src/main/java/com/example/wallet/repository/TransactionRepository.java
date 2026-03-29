package com.example.wallet.repository;

import com.example.wallet.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUser_IdOrderByCreatedAtAsc(Long userId);

    List<Transaction> findByTransferGroupId(String transferGroupId);
}
