package com.mirage.todolist.model.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface MetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeta(meta: MetaEntity)

    @Query("""
        SELECT meta_value
        FROM meta
        WHERE meta_key = ${MetaEntity.DATA_VERSION_KEY}
    """)
    fun getDataVersion(): List<UUID>

    @Query("""
        INSERT OR REPLACE
        INTO meta(meta_key, meta_value)
        VALUES (${MetaEntity.MUST_BE_PROCESSED_KEY}, :mustBeProcessed), (${MetaEntity.DATA_VERSION_KEY}, :newVersion)
        """)
    fun setDataVersion(newVersion: UUID, mustBeProcessed: Boolean)

    @Query("""
        INSERT OR REPLACE
        INTO meta(meta_key, meta_value)
        VALUES (${MetaEntity.MUST_BE_PROCESSED_KEY}, :mustBeProcessed)
        """)
    fun setMustBeProcessed(mustBeProcessed: Boolean)

    @Query("""
        SELECT meta_value
        FROM meta
        WHERE meta_key = ${MetaEntity.MUST_BE_PROCESSED_KEY}
    """)
    fun getMustBeProcessed(): Boolean

    @Query("""
        SELECT meta_value
        FROM meta
        WHERE meta_key = ${MetaEntity.DATA_VERSION_KEY}
    """)
    fun getLiveDataVersion(): LiveData<UUID>

    @Query("SELECT * FROM meta")
    fun getAllMeta(): List<MetaEntity>
}

/** Updates database version without signaling for sync processing */
fun MetaDao.updateVersion() {
    setDataVersion(UUID.randomUUID(), false)
}