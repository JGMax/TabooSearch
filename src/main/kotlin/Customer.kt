import Constants.SHORT_MEMORY_DURATION

open class Customer(val x: Int,
                    val y: Int,
                    val demand: Int,
                    private val ts: Int, private val te: Int,
                    private val service: Int) {
    var isMuted = false
    private var mutedSteps = 0

    fun getTimeSpent(arrivalTime: Int) =
        service + if (ts > arrivalTime) {
            ts - arrivalTime
        } else {
            0
        }

    fun getViolation(arrivalTime: Int) = if (te < arrivalTime) {
            arrivalTime - te
        } else {
            0
        }

    fun increaseMutedStep() {
        if (isMuted) {
            mutedSteps++
            if (mutedSteps > SHORT_MEMORY_DURATION) {
                isMuted = false
                mutedSteps = 0
            }
        }
    }
}
