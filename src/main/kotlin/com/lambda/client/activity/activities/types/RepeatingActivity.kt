package com.lambda.client.activity.activities.types

import com.lambda.client.activity.Activity
import com.lambda.client.event.SafeClientEvent

interface RepeatingActivity {
    val maximumRepeats: Int
    var repeated: Int

    companion object {
        fun SafeClientEvent.checkRepeat(activity: Activity) {
            if (activity !is RepeatingActivity) return

            with(activity) {
                if (repeated++ >= maximumRepeats && maximumRepeats != 0) return

                activityStatus = Activity.ActivityStatus.UNINITIALIZED
                owner.subActivities.add(activity)
//                LambdaMod.LOG.info("Looping $name [$currentLoops/${if (maxLoops == 0) "∞" else maxLoops}] ")
            }
        }
    }
}