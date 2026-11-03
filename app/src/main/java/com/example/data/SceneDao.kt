package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY sceneIndex ASC")
    fun getScenesForProject(projectId: Long = 1L): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY sceneIndex ASC")
    suspend fun getScenesSync(projectId: Long = 1L): List<SceneEntity>

    @Query("SELECT * FROM scenes WHERE id = :sceneId LIMIT 1")
    suspend fun getSceneById(sceneId: Long): SceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<SceneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: SceneEntity): Long

    @Update
    suspend fun updateScene(scene: SceneEntity)

    @Query("DELETE FROM scenes WHERE id = :sceneId")
    suspend fun deleteSceneById(sceneId: Long)

    @Query("DELETE FROM scenes WHERE projectId = :projectId")
    suspend fun deleteAllScenesForProject(projectId: Long = 1L)
}
