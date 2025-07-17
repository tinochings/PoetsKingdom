package com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation

import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisor
import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisorModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class NoOpTaskSupervisor(
    override val taskSupervisorModel: TaskSupervisorModel = TaskSupervisorModel(),
    override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
    override val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) : TaskSupervisor {


    override fun updateTotalImagesProcessed() {
        /* no-op */
    }

    override suspend fun observeTaskSupervisorModel(
        onModelObserved: (TaskSupervisorModel) -> Unit
    ) {
        /* no-op */
    }
}