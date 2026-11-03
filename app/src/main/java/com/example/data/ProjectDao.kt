package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProject(id: Long = 1L): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectSync(id: Long = 1L): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)
}
