package com.creditcard.repository;

import com.creditcard.model.CreditCard;
import com.creditcard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByCardNumber(String cardNumber);
    List<CreditCard> findByUser(User user);
    List<CreditCard> findByStatus(CreditCard.CardStatus status);
    boolean existsByCardNumber(String cardNumber);
}
