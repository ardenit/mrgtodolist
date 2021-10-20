package com.mirage.todolist.model.sync

import java.util.*

data class LockFileData(
    val lockStartMillis: Long,
    val lockOwner: UUID?,
    val dataVersion: UUID?
)