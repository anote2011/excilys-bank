/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.bank.service.impl;

import static com.excilys.ebi.bank.model.entity.Operation.newOperation;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.excilys.ebi.bank.dao.AccountDao;
import com.excilys.ebi.bank.dao.CardDao;
import com.excilys.ebi.bank.dao.OperationDao;
import com.excilys.ebi.bank.dao.OperationStatusRefDao;
import com.excilys.ebi.bank.dao.OperationTypeRefDao;
import com.excilys.ebi.bank.dao.UserDao;
import com.excilys.ebi.bank.model.IConstants;
import com.excilys.ebi.bank.model.YearMonth;
import com.excilys.ebi.bank.model.entity.Account;
import com.excilys.ebi.bank.model.entity.Card;
import com.excilys.ebi.bank.model.entity.Operation;
import com.excilys.ebi.bank.model.entity.User;
import com.excilys.ebi.bank.model.entity.ref.OperationSign;
import com.excilys.ebi.bank.model.entity.ref.OperationStatus;
import com.excilys.ebi.bank.model.entity.ref.OperationStatusRef;
import com.excilys.ebi.bank.model.entity.ref.OperationType;
import com.excilys.ebi.bank.model.entity.ref.OperationTypeRef;
import com.excilys.ebi.bank.service.BankService;
import com.excilys.ebi.bank.service.UnsufficientBalanceException;
import com.google.common.base.Function;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;

@Service
@Transactional(readOnly = true)
public class BankServiceImpl implements BankService {

	private static final int PAGE_SIZE = 20;

	@Autowired
	private UserDao userDao;

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private OperationDao operationDao;

	@Autowired
	private CardDao cardDao;

	@Autowired
	private OperationStatusRefDao operationStatusDao;

	@Autowired
	private OperationTypeRefDao operationTypeDao;

	@Override
	@Cacheable(cacheName = IConstants.Cache.ENTITY_CACHE, keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator"))
	public Integer findAccountIdByNumber(String accountNumber) {
		return accountDao.findByNumber(accountNumber).getId();
	}

	@Override
	@Cacheable(cacheName = IConstants.Cache.ENTITY_CACHE, keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator"))
	public Integer findCardIdByNumber(String cardNumber) {
		return cardDao.findByNumber(cardNumber).getId();
	}

	@Override
	public List<Account> findAccountsByUser(User user) {
		return accountDao.findByUsersOrderByNumberAsc(user);
	}

	@Override
	public List<Account> findAccountsByUserFetchCardsOrderByNumberAsc(User user) {
		return accountDao.findByUserFetchCardsOrderByNumberAsc(user);
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'read')")
	public Account findAccountByNumberFetchCardsOrderByNumberAsc(String accountNumber) {
		return accountDao.findByNumberFetchCardsOrderByNumberAsc(accountNumber);
	}

	@Override
	public Page<Operation> findNonCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth, int page) {

		Pageable pageable = new PageRequest(page, PAGE_SIZE);
		return operationDao.findNonCardByAccountIdAndYearMonth(accountId, yearMonth, pageable);
	}

	@Override
	public Map<Card, BigDecimal[]> sumResolvedCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth) {

		Collection<Card> cards = cardDao.findByAccountIdOrderByNumberAsc(accountId);

		Map<Card, BigDecimal[]> sums = newHashMap();
		for (Card card : cards) {
			sums.put(card, new BigDecimal[] { ZERO, ZERO });
		}

		Map<Integer, Operation> creditSumsIndexedByCardId = uniqueIndex(
				operationDao.sumResolvedAmountByAccountIdAndYearMonthAndSignGroupByCard(accountId, yearMonth, OperationSign.CREDIT), new Function<Operation, Integer>() {
					@Override
					public Integer apply(Operation input) {
						return input.getCard().getId();
					}
				});

		Map<Integer, Operation> debitSumsIndexedByCardId = uniqueIndex(
				operationDao.sumResolvedAmountByAccountIdAndYearMonthAndSignGroupByCard(accountId, yearMonth, OperationSign.DEBIT), new Function<Operation, Integer>() {
					@Override
					public Integer apply(Operation input) {
						return input.getCard().getId();
					}
				});

		for (Entry<Card, BigDecimal[]> entry : sums.entrySet()) {
			Operation creditSum = creditSumsIndexedByCardId.get(entry.getKey().getId());
			if (creditSum != null) {
				entry.getValue()[0] = creditSum.getAmount();
			}
			Operation debitSum = debitSumsIndexedByCardId.get(entry.getKey().getId());
			if (debitSum != null) {
				entry.getValue()[1] = debitSum.getAmount();
			}
		}

		return sums;
	}

	@Override
	public BigDecimal sumResolvedAmountByAccountIdAndYearMonthAndSign(Integer accountId, YearMonth yearMonth, OperationSign sign) {

		return operationDao.sumResolvedAmountByAccountIdAndYearMonthAndSign(accountId, yearMonth, sign);
	}

	@Override
	public Page<Operation> findResolvedCardOperationsByAccountIdAndYearMonth(Integer accountId, YearMonth yearMonth, int page) {

		Pageable pageable = new PageRequest(page, PAGE_SIZE);
		return operationDao.findCardOperationsByAccountIdAndYearMonthAndStatus(accountId, yearMonth, OperationStatus.RESOLVED, pageable);
	}

	@Override
	public BigDecimal sumResolvedCardAmountByAccountIdAndYearMonthAndSign(Integer accountId, YearMonth yearMonth, OperationSign sign) {
		return operationDao.sumCardAmountByAccountIdAndYearMonthAndSignAndStatus(accountId, yearMonth, sign, OperationStatus.RESOLVED);
	}

	@Override
	public Page<Operation> findResolvedCardOperationsByCardIdAndYearMonth(Integer cardId, YearMonth yearMonth, int page) {

		Pageable pageable = new PageRequest(page, PAGE_SIZE);
		return operationDao.findCardOperationsByCardIdAndYearMonthAndStatus(cardId, yearMonth, OperationStatus.RESOLVED, pageable);
	}

	@Override
	public BigDecimal sumResolvedCardAmountByCardIdAndYearMonthAndSign(Integer cardId, YearMonth yearMonth, OperationSign sign) {
		return operationDao.sumCardAmountByCardIdAndYearMonthAndSignAndStatus(cardId, yearMonth, sign, OperationStatus.RESOLVED);
	}

	@Override
	public Page<Operation> findPendingCardOperationsByAccountId(Integer accountId, int page) {

		Pageable pageable = new PageRequest(page, PAGE_SIZE);
		return operationDao.findCardOperationsByAccountIdAndYearMonthAndStatus(accountId, null, OperationStatus.PENDING, pageable);
	}

	@Override
	public BigDecimal sumPendingCardAmountByAccountIdAndSign(Integer accountId, OperationSign sign) {

		return operationDao.sumCardAmountByAccountIdAndYearMonthAndSignAndStatus(accountId, null, sign, OperationStatus.PENDING);
	}

	@Override
	public Page<Operation> findPendingCardOperationsByCardId(Integer cardId, int page) {

		Pageable pageable = new PageRequest(page, PAGE_SIZE);
		return operationDao.findCardOperationsByCardIdAndYearMonthAndStatus(cardId, null, OperationStatus.PENDING, pageable);
	}

	@Override
	public BigDecimal sumPendingCardAmountByCardIdAndSign(Integer cardId, OperationSign sign) {

		return operationDao.sumCardAmountByCardIdAndYearMonthAndSignAndStatus(cardId, null, sign, OperationStatus.PENDING);
	}

	@Override
	public boolean isClientOfAccountByAccountIdAndUserLogin(int id, String login) {
		long count = accountDao.countAccountsByIdAndUserLogin(id, login);
		Assert.isTrue(count <= 1);
		return count > 0;

	}

	@Override
	public Page<Operation> findTransferOperationsByAccountId(Integer accountId, int page) {
		return operationDao.findTransferByAccountId(accountId, null);
	}

	@Override
	@Transactional(readOnly = false)
	public void performTransfer(Integer debitedAccountId, Integer creditedAccountId, BigDecimal amount) throws UnsufficientBalanceException {

		Assert.notNull(debitedAccountId, "debitedAccountId is required");
		Assert.notNull(creditedAccountId, "creditedAccountId is required");
		Assert.notNull(amount, "amount is required");
		Assert.isTrue(amount.compareTo(BigDecimal.valueOf(10L)) >= 0, "amount must be >= 10");
		Assert.isTrue(!debitedAccountId.equals(creditedAccountId), "accounts must be different");

		Account debitedAccount = accountDao.findOne(debitedAccountId);
		Assert.notNull(debitedAccount, "unknown account");

		if (debitedAccount.getBalance().compareTo(amount) < 0) {
			throw new UnsufficientBalanceException();
		}

		Account creditedAccount = accountDao.findOne(creditedAccountId);
		Assert.notNull(creditedAccount, "unknown account");

		debitedAccount.setBalance(debitedAccount.getBalance().subtract(amount));
		creditedAccount.setBalance(creditedAccount.getBalance().add(amount));

		DateTime now = new DateTime();
		OperationStatusRef status = operationStatusDao.findOne(OperationStatus.RESOLVED);
		OperationTypeRef type = operationTypeDao.findOne(OperationType.TRANSFER);

		Operation debitOperation = newOperation().withName("transfert -" + amount).withAccount(debitedAccount).withAmount(amount.negate()).withDate(now).withStatus(status)
				.withType(type).build();
		Operation creditOperation = newOperation().withName("transfert +" + amount).withAccount(creditedAccount).withAmount(amount).withDate(now).withStatus(status).withType(type)
				.build();

		operationDao.save(debitOperation);
		operationDao.save(creditOperation);
	}

	@Override
	public long countUsers() {
		return userDao.count();
	}

	@Override
	public long countAccounts() {
		return accountDao.count();
	}

	@Override
	public long countOperations() {
		return operationDao.count();
	}
}
