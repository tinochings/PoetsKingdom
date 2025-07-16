package com.wendorochena.poetskingdom.utils.parallelism.images.task

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

interface TaskSupervisor{
    val taskSupervisorModel : TaskSupervisorModel
    val coroutineScope : CoroutineScope
    val dispatcher : CoroutineDispatcher

    /**
     *
     */
    fun updateTotalImagesProcessed()

    /**
     *
     */
    suspend fun observeTaskSupervisorModel(dispatcherToRunOn : CoroutineDispatcher,onModelObserved : (TaskSupervisorModel) -> Unit)
}