import Constants.SHORT_MEMORY_DURATION

open class Customer(val id: Int,
                    val x: Int,
                    val y: Int,
                    val demand: Int,
                    private val ts: Int, private val te: Int,
                    private val service: Int) {
    var isMuted = false
    private var mutedSteps = 0
    var arrivalTime = 0.0

    constructor(data: List<Int>) :
            this(data[0]
                , data[1]
                , data[2]
                , data[3]
                , data[4]
                , data[5]
                , data[6])

    fun getDelay() = if (ts > arrivalTime) {
        ts - arrivalTime
    } else {
        0.0
    }

    fun getTimeSpent() =
        service + getDelay()

    fun getViolation() = if (te < arrivalTime) {
            arrivalTime - te
        } else {
            0.0
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
