package com.excilys.ebi.bank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.excilys.ebi.bank.model.YearMonth;
import com.excilys.ebi.bank.model.entity.Account;
import com.excilys.ebi.bank.model.entity.Card;
import com.excilys.ebi.bank.model.entity.Operation;
import com.excilys.ebi.bank.model.entity.User;
import com.excilys.ebi.bank.model.entity.ref.OperationSign;

public interface BankService {

	Integer findAccountIdByNumber(String accountNumber);

	Integer findCardIdByNumber(String cardNumber);

	List<Account> findAccountsByUser(User user);

	List<Account> findAccountsByUserFetchCardsOrderByNumberAsc(User user);

	Account findAccountByNumberFetchCardsOrderByNumberAsc(String accountNumber);

	Page<Operation> findNonCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth, int page);

	Map<Card, BigDecimal[]> sumResolvedCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth);

	BigDecimal sumResolvedAmountByAccountIdAndYearMonthAndSign(Integer accountId, YearMonth yearMonth, OperationSign sign);

	Page<Operation> findResolvedCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth, int page);

	BigDecimal sumResolvedCardAmountByAccountIdAndYearMonthAndSign(Integer accountId, YearMonth yearMonth, OperationSign sign);

	Page<Operation> findResolvedCardOperationsByCardIdAndYearMonth(Integer cardId, YearMonth yearMonth, int page);

	BigDecimal sumResolvedCardAmountByCardIdAndYearMonthAndSign(Integer cardId, YearMonth yearMonth, OperationSign sign);

	Page<Operation> findPendingCardOperationsByAccountId(Integer accountId, int page);

	BigDecimal sumPendingCardAmountByAccountIdAndSign(Integer accountId, OperationSign sign);

	Page<Operation> findPendingCardOperationsByCardId(Integer cardId, int page);

	BigDecimal sumPendingCardAmountByCardIdAndSign(Integer cardId, OperationSign sign);

	boolean isClientOfAccountByAccountIdAndUserLogin(int id, String login);

	Page<Operation> findTransferOperationsByAccountId(Integer accountId, int page);

	void performTransfer(Integer debitedAccountId, Integer creditedAccountId, BigDecimal amount) throws UnsufficientBalanceException;

	long countUsers();

	long countAccounts();

	long countOperations();
}
