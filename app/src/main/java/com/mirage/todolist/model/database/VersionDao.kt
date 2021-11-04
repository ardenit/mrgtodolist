package com.mirage.todolist.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface VersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersion(version: VersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllVersions(versions: List<VersionEntity>)

    @Query("""
        SELECT *
        FROM versions
    """)
    fun getAllVersionsFlow(): Flow<List<VersionEntity>>

    @Query("""
        SELECT data_version
        FROM versions
        WHERE account_name = :email
    """)
    fun getDataVersion(email: String): List<UUID>

    @Query("""
        INSERT OR REPLACE
        INTO versions(account_name, data_version, must_be_processed)
        VALUES (:email, :newVersion, :mustBeProcessed)
        """)
    fun setDataVersion(email: String, newVersion: UUID, mustBeProcessed: Boolean)

    @Query("""
        UPDATE versions
        SET must_be_processed = :mustBeProcessed
        WHERE account_name = :email
        """)
    fun setMustBeProcessed(email: String, mustBeProcessed: Boolean)

    @Query("""
        SELECT must_be_processed
        FROM versions
        WHERE account_name = :email
    """)
    fun getMustBeProcessed(email: String): Boolean

    @Query("""
        SELECT data_version
        FROM versions
        WHERE account_name = :email
    """)
    fun getDataVersionFlow(email: String): Flow<UUID>

    @Query("""
        DELETE FROM versions
        WHERE 1
    """)
    fun clear()

    @Query("SELECT * FROM versions")
    fun getAllVersions(): List<VersionEntity>
}

/** Updates database version without signaling for sync processing */
fun VersionDao.updateVersion(email: String) {
    if (email.isNotBlank()) setDataVersion(email, UUID.randomUUID(), false)
}