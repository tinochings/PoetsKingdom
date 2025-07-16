package com.wendorochena.poetskingdom.utils.parallelism.images.tasksImplementation

import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisor
import com.wendorochena.poetskingdom.utils.parallelism.images.task.TaskSupervisorModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.math.round

class TaskSupervisorImpl(
    override val taskSupervisorModel: TaskSupervisorModel,
    override val coroutineScope: CoroutineScope,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TaskSupervisor {
    private var taskSupervisorModelEditable = taskSupervisorModel.copy()
    private val modelChannel = Channel<TaskSupervisorModel>()


    override fun updateTotalImagesProcessed() {
            coroutineScope.launch(dispatcher) {
                if (taskSupervisorModelEditable.totalImages > 0) {
                    val current = taskSupervisorModelEditable.currentImagesProcessed + 1
                    val progress =
                        current.toFloat() / taskSupervisorModelEditable.totalImages
                    val percentage = round(progress * 100).toInt()

                    taskSupervisorModelEditable = taskSupervisorModelEditable.copy(
                        progress = progress,
                        currentImagesProcessed = current,
                        percentage = percentage
                    )
                    modelChannel.send(taskSupervisorModelEditable)
                    if (taskSupervisorModelEditable.currentImagesProcessed == taskSupervisorModelEditable.totalImages)
                        modelChannel.close()
                }
            }
    }

    override suspend fun observeTaskSupervisorModel(
        dispatcherToRunOn: CoroutineDispatcher,
        onModelObserved: (TaskSupervisorModel) -> Unit
    ){
        coroutineScope.launch(dispatcherToRunOn) {
            modelChannel.consumeEach {
                onModelObserved.invoke(it)
            }
        }
    }
}