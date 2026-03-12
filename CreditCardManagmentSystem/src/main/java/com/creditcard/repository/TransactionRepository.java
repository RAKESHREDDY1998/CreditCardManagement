package com.creditcard.repository;

import com.creditcard.model.CreditCard;
import com.creditcard.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCreditCard(CreditCard creditCard);

    List<Transaction> findByCreditCardOrderByTransactionDateDesc(CreditCard creditCard);

    @Query("SELECT t FROM Transaction t WHERE t.creditCard = :card " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByCardAndDateRange(
        @Param("card") CreditCard card,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.creditCard = :card " +
           "AND t.status = 'APPROVED' ORDER BY t.transactionDate DESC")
    List<Transaction> findApprovedTransactionsByCard(@Param("card") CreditCard card);
}
