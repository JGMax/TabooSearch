open class Customer(val x: Int,
                    val y: Int,
                    val demand: Int,
                    private val ts: Int, val te: Int,
                    private val service: Int) {

    fun getTimeSpent(arrivalTime: Int) =
        arrivalTime + service + if (ts > arrivalTime) {
            ts - arrivalTime
        } else {
            0
        }

    fun getViolation(arrivalTime: Int) = if (te < arrivalTime) {
            arrivalTime - te
        } else {
            0
        }
}