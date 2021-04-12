class Path {
    val list = arrayListOf<Customer>()
    val idsArray = Array(2) { mutableListOf<Int>() }
    val timeList = arrayListOf<Int>()
    var violation = 0
    private set

    fun clear() {
        list.clear()
        idsArray[0].clear()
        idsArray[1].clear()
        timeList.clear()
        violation = 0
    }

    fun syncStep() {
        val l = list.last()
        idsArray[0].add(l.x)
        idsArray[1].add(l.y)
        timeList.add(l.arrivalTime + l.getDelay())
        violation += l.getViolation()
    }

    fun syncAll() {
        idsArray[0].clear()
        idsArray[1].clear()
        list.forEach {
            idsArray[0].add(it.x)
            idsArray[1].add(it.y)
            timeList.add(it.arrivalTime + it.getDelay())
            violation += it.getViolation()
        }
    }
}
