package com.mirage.todolist.model.googledrive

import java.util.*

/**
 * Data stored as JSON within lock acquisition file in Google Drive Application Folder
 */
data class LockFileData(
    val lockStartMillis: Long,
    val lockOwner: UUID?,
    val dataVersion: UUID?
)