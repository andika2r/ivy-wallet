package com.ivy.core.domain.api.action

import com.ivy.core.data.Transaction
import com.ivy.core.data.sync.SyncState

class WriteTransactionAct {
    // TODO: Implement
}

sealed interface ModifyTransaction {
    data class Save(
        val transaction: Transaction,
        val sync: SyncState
    ) : ModifyTransaction

}