package com.example.wallet.service;

import com.example.wallet.dto.TransactionCreateRequest;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.TransactionUpdateRequest;
import com.example.wallet.exception.InsufficientFundsException;
import com.example.wallet.exception.InvalidTransferException;
import com.example.wallet.exception.TransactionNotFoundException;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.User;
import com.example.wallet.model.enums.TransactionType;
import com.example.wallet.model.enums.UserRole;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.TransactionSpecifications;
import com.example.wallet.security.AppUserDetails;
import com.example.wallet.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(4);

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public BigDecimal currentBalanceForCurrentUser() {
        AppUserDetails current = SecurityUtils.requireCurrentUser();
        return computeBalance(current.getUserId());
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        AppUserDetails current = SecurityUtils.requireCurrentUser();
        User owner = userService.getById(current.getUserId());
        BigDecimal amount = request.getAmount().setScale(4, java.math.RoundingMode.HALF_UP);

        if (request.getType() == TransactionType.TRANSFER) {
            return createTransfer(owner, amount, request.getDescription().trim(), request.getCounterpartyEmail());
        }
        if (request.getType() == TransactionType.DEBIT && computeBalance(owner.getId()).compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        if (request.getCounterpartyEmail() != null && !request.getCounterpartyEmail().isBlank()) {
            throw new InvalidTransferException("counterpartyEmail is only allowed for TRANSFER transactions");
        }

        Transaction tx = Transaction.builder()
                .user(owner)
                .amount(amount)
                .type(request.getType())
                .description(request.getDescription().trim())
                .build();
        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved, balanceAfterForTransaction(owner.getId(), saved.getId()));
    }

    private TransactionResponse createTransfer(User sender, BigDecimal amount, String description, String counterpartyEmail) {
        if (!StringUtils.hasText(counterpartyEmail)) {
            throw new InvalidTransferException("counterpartyEmail is required for TRANSFER");
        }
        String email = counterpartyEmail.trim().toLowerCase();
        if (sender.getEmail().equalsIgnoreCase(email)) {
            throw new InvalidTransferException("Cannot transfer to yourself");
        }
        if (computeBalance(sender.getId()).compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        User receiver = userService.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidTransferException("No user registered with email: " + email));

        String groupId = UUID.randomUUID().toString();
        Transaction out = Transaction.builder()
                .user(sender)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .description(description)
                .counterpartyEmail(receiver.getEmail())
                .transferGroupId(groupId)
                .build();
        Transaction in = Transaction.builder()
                .user(receiver)
                .amount(amount)
                .type(TransactionType.CREDIT)
                .description("Transfer from " + sender.getEmail() + " — " + description)
                .counterpartyEmail(sender.getEmail())
                .transferGroupId(groupId)
                .build();
        Transaction savedOut = transactionRepository.save(out);
        transactionRepository.save(in);
        return toResponse(savedOut, balanceAfterForTransaction(sender.getId(), savedOut.getId()));
    }

    private BigDecimal balanceAfterForTransaction(Long userId, Long transactionId) {
        Map<Long, BigDecimal> map = buildBalanceAfterMap(userId);
        return map.getOrDefault(transactionId, ZERO);
    }

    private Map<Long, BigDecimal> buildBalanceAfterMap(Long userId) {
        List<Transaction> asc = transactionRepository.findByUser_IdOrderByCreatedAtAsc(userId);
        BigDecimal running = ZERO;
        Map<Long, BigDecimal> byId = new HashMap<>();
        for (Transaction t : asc) {
            running = running.add(signedImpact(t));
            byId.put(t.getId(), running);
        }
        return byId;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(TransactionType type, Instant from, Instant to) {
        AppUserDetails current = SecurityUtils.requireCurrentUser();
        Specification<Transaction> spec = Specification.where(TransactionSpecifications.typeEquals(type))
                .and(TransactionSpecifications.createdAtFrom(from))
                .and(TransactionSpecifications.createdAtTo(to));
        if (current.getRole() != UserRole.ADMIN) {
            spec = spec.and(TransactionSpecifications.forUserId(current.getUserId()));
        }
        List<Transaction> list =
                transactionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        Set<Long> userIds = list.stream()
                .map(t -> t.getUser().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Map<Long, BigDecimal>> balanceMaps = new HashMap<>();
        for (Long uid : userIds) {
            balanceMaps.put(uid, buildBalanceAfterMap(uid));
        }
        return list.stream()
                .map(t -> toResponse(
                        t,
                        balanceMaps
                                .getOrDefault(t.getUser().getId(), Map.of())
                                .getOrDefault(t.getId(), ZERO)))
                .toList();
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionUpdateRequest request) {
        Transaction tx = transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
        assertCanModify(SecurityUtils.requireCurrentUser(), tx);
        if (tx.getTransferGroupId() != null) {
            throw new InvalidTransferException(
                    "Transfer entries cannot be edited. Delete the transfer (removes both sides) and create a new one.");
        }
        if (request.getType() == TransactionType.TRANSFER) {
            throw new InvalidTransferException("Cannot change a transaction to TRANSFER via update. Use create instead.");
        }

        BigDecimal newAmount = request.getAmount().setScale(4, java.math.RoundingMode.HALF_UP);
        Long userId = tx.getUser().getId();

        assertUpdateKeepsNonNegativeHistory(tx.getId(), userId, newAmount, request.getType());

        tx.setAmount(newAmount);
        tx.setType(request.getType());
        tx.setDescription(request.getDescription().trim());
        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved, balanceAfterForTransaction(userId, saved.getId()));
    }

    private void assertUpdateKeepsNonNegativeHistory(
            Long excludeId, Long userId, BigDecimal newAmount, TransactionType newType) {
        List<Transaction> asc = transactionRepository.findByUser_IdOrderByCreatedAtAsc(userId);
        BigDecimal running = ZERO;
        for (Transaction t : asc) {
            BigDecimal impact = t.getId().equals(excludeId)
                    ? signedImpact(newAmount, newType)
                    : signedImpact(t);
            running = running.add(impact);
            if (running.compareTo(ZERO) < 0) {
                throw new InsufficientFundsException();
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        Transaction tx = transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
        AppUserDetails user = SecurityUtils.requireCurrentUser();
        if (tx.getTransferGroupId() != null) {
            List<Transaction> group = transactionRepository.findByTransferGroupId(tx.getTransferGroupId());
            for (Transaction t : group) {
                assertCanModify(user, t);
            }
            transactionRepository.deleteAll(group);
            return;
        }
        assertCanModify(user, tx);
        transactionRepository.delete(tx);
    }

    private BigDecimal computeBalance(Long userId) {
        List<Transaction> asc = transactionRepository.findByUser_IdOrderByCreatedAtAsc(userId);
        BigDecimal sum = ZERO;
        for (Transaction t : asc) {
            sum = sum.add(signedImpact(t));
        }
        return sum;
    }

    private BigDecimal signedImpact(Transaction t) {
        return signedImpact(t.getAmount(), t.getType());
    }

    private BigDecimal signedImpact(BigDecimal amount, TransactionType type) {
        return switch (type) {
            case CREDIT -> amount;
            case DEBIT -> amount.negate();
            case TRANSFER -> amount.negate();
        };
    }

    private void assertCanModify(AppUserDetails user, Transaction tx) {
        if (user.getRole() != UserRole.ADMIN && !tx.getUser().getId().equals(user.getUserId())) {
            throw new AccessDeniedException("You can only modify your own transactions");
        }
    }

    private TransactionResponse toResponse(Transaction tx, BigDecimal balanceAfter) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getUser().getId(),
                tx.getCreatedAt(),
                balanceAfter,
                tx.getCounterpartyEmail(),
                tx.getTransferGroupId());
    }
}
