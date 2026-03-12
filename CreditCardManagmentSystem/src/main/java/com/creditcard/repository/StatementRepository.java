package com.creditcard.repository;

import com.creditcard.model.CreditCard;
import com.creditcard.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {
    List<Statement> findByCreditCard(CreditCard creditCard);
    List<Statement> findByCreditCardOrderByStatementDateDesc(CreditCard creditCard);
    Optional<Statement> findByCreditCardAndStatementDate(CreditCard card, LocalDate date);
    List<Statement> findByStatus(Statement.StatementStatus status);
}
