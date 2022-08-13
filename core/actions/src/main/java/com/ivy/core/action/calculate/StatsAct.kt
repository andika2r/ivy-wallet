package com.ivy.core.action.calculate

import arrow.core.nonEmptyListOf
import com.ivy.core.action.currency.exchange.ExchangeAct
import com.ivy.core.functions.transaction.foldTransactions
import com.ivy.data.CurrencyCode
import com.ivy.data.transaction.Transaction
import com.ivy.data.transaction.TransactionType
import com.ivy.frp.action.FPAction
import com.ivy.frp.asParamTo
import com.ivy.frp.thenInvokeAfter
import javax.inject.Inject

class StatsAct @Inject constructor(
    private val exchangeAct: ExchangeAct
) : FPAction<StatsAct.Input, Stats>() {
    data class Input(
        val trns: List<Transaction>,
        val outputCurrency: CurrencyCode
    )

    override suspend fun Input.compose(): suspend () -> Stats = {
        val res = foldTransactions(
            transactions = trns,
            valueFunctions = nonEmptyListOf(
                ::income,
                ::expense,
                ::incomeCount,
                ::expenseCount,
            ),
            arg = outputCurrency
        )

        val income = res[0]
        val expense = res[1]

        Stats(
            balance = income - expense,
            income = income,
            expense = expense,
            incomesCount = res[2].toInt(),
            expensesCount = res[3].toInt()
        )
    }

    private suspend fun income(
        trn: Transaction, arg: CurrencyCode
    ): Double = when (trn.type) {
        TransactionType.Income -> trnAmountInCurrency(trn, arg)
        else -> 0.0
    }

    private suspend fun expense(
        trn: Transaction, arg: CurrencyCode
    ): Double = when (trn.type) {
        TransactionType.Expense -> trnAmountInCurrency(trn, arg)
        else -> 0.0
    }

    private suspend fun incomeCount(
        trn: Transaction, arg: CurrencyCode
    ): Double = when (trn.type) {
        TransactionType.Income -> 1.0
        else -> 0.0
    }

    private suspend fun expenseCount(
        trn: Transaction, arg: CurrencyCode
    ): Double = when (trn.type) {
        TransactionType.Expense -> 1.0
        else -> 0.0
    }


    private suspend fun trnAmountInCurrency(
        trn: Transaction,
        outputCurrency: CurrencyCode
    ): Double =
        ExchangeAct.Input(
            from = trn.account.currency,
            to = outputCurrency,
            amount = trn.amount
        ) asParamTo exchangeAct thenInvokeAfter {
            it.orNull() ?: 0.0
        }
}