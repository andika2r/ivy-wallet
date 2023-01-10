package com.ivy.core.domain.action.account

import com.ivy.common.time.provider.TimeProvider
import com.ivy.core.domain.action.Action
import com.ivy.core.persistence.dao.account.AccountDao
import com.ivy.data.account.Account
import javax.inject.Inject

class AccountsAct @Inject constructor(
    private val accountDao: AccountDao,
    private val timeProvider: TimeProvider,
) : Action<Unit, List<Account>>() {
    override suspend fun Unit.willDo(): List<Account> =
        accountDao.findAllSnapshot()
            .map { acc -> toDomain(acc = acc, timeProvider = timeProvider) }
}