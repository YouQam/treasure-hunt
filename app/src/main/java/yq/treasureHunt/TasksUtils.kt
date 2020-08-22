package yq.treasureHunt

import com.google.android.gms.maps.model.LatLng


/**
 * Stores latitude and longitude information along with an id.
 */
data class TaskDataObject(val id: String, val latLong: LatLng)

internal object TasksConstants {
    val TASK_DATA = arrayOf(
        TaskDataObject(
            "task1",
            LatLng(51.9756784, 7.5777154)
        ),

        TaskDataObject(
            "task2",
            LatLng(51.9761918, 7.5764577)
        )
    )

    val NUM_TASKS = TASK_DATA.size
    const val TARGET_DISTANCE_METERS = 100
}
