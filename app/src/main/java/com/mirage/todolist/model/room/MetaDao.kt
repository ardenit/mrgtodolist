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
        WHERE meta_name = 'data_version'
    """)
    fun getDataVersion(): List<UUID>

    @Query("""
        INSERT OR REPLACE
        INTO meta(meta_name, meta_value)
        VALUES('data_version', :newVersion)
        """)
    fun setDataVersion(newVersion: UUID)

    @Query("""
        SELECT meta_value
        FROM meta
        WHERE meta_name = 'data_version'
    """)
    fun getLiveDataVersion(): LiveData<UUID>

    @Query("SELECT * FROM meta")
    fun getAllMeta(): List<MetaEntity>
}