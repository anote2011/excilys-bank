package com.excilys.ebi.bank.dao.impl;

import static com.excilys.ebi.bank.model.entity.QUser.user;

import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.excilys.ebi.bank.dao.UserDaoCustom;
import com.excilys.ebi.bank.model.entity.User;

@Repository
public class UserDaoImpl extends QueryDslRepositorySupport implements UserDaoCustom {

	@Override
	public User findByLoginFetchRoles(String login) {
		return from(user).where(user.login.eq(login)).innerJoin(user.roles).fetch().uniqueResult(user);
	}
}
