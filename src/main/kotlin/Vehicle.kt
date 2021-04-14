
import Depot.depot
import GlobalVariables.maxCapacity
import GlobalVariables.penalty

class Vehicle(val id: Int) {
    val list = arrayListOf<Customer>()
    val coordinatesArray = Array(2) { mutableListOf<Int>() }
    val idsList = arrayListOf<Int>()
    val timeList = arrayListOf<Double>()
    var violation = 0.0
        private set
    var currentCapacity = 0
    var currentTime = 0.0
    var time = 0.0
        private set
        get() = calcTime()

    val last
        get() = list.last()
    val bestIndexes = arrayOf(-1, -1)

    constructor(id: Int, list: ArrayList<Customer>) : this(id) {
        list.forEach {
            this.add(it)
        }
    }

    fun clear() {
        list.clear()
        coordinatesArray[0].clear()
        coordinatesArray[1].clear()
        idsList.clear()
        timeList.clear()
        time = 0.0
        violation = 0.0
        currentCapacity = 0
    }

    fun copy() : Vehicle = Vehicle(id, list)

    fun add(customer: Customer) {
        if (list.isNotEmpty()) {
            currentTime += list.last().calcDistance(customer)
        }
        customer.arrivalTime = currentTime
        currentTime += customer.getTimeSpent()

        currentCapacity += customer.demand
        list.add(customer)
        coordinatesArray[0].add(customer.x)
        coordinatesArray[1].add(customer.y)
        idsList.add(customer.id)
        timeList.add(customer.arrivalTime + customer.getDelay())
    }

    private fun insertInto(index: Int, value: Customer) {
        currentCapacity += value.demand
        list.add(index, value)
        coordinatesArray[0].add(index, value.x)
        coordinatesArray[1].add(index, value.y)
        idsList.add(index, value.id)
        recalculateCurrentTime()
    }

    private fun recalculateCurrentTime() {
        currentTime = 0.0
        timeList.clear()
        list[0].arrivalTime = currentTime
        timeList.add(list[0].arrivalTime + list[0].getDelay())
        currentTime += list[0].getTimeSpent()
        list.forEachIndexed { i, customer ->
            if (i != 0) {
                currentTime += list[i - 1].calcDistance(customer)
                customer.arrivalTime = currentTime
                currentTime += customer.getTimeSpent()
                timeList.add(customer.arrivalTime + customer.getDelay())
            }
        }
    }

    private fun deleteAt(index: Int) {
        currentCapacity -= list[index].demand
        list.removeAt(index)
        coordinatesArray[0].removeAt(index)
        coordinatesArray[1].removeAt(index)
        idsList.removeAt(index)
        timeList.removeAt(index)
        recalculateCurrentTime()
    }

    private fun calcViolation() : Double {
        violation = 0.0
        list.forEach {
            violation += it.getViolation()
        }
        violation += (currentCapacity - maxCapacity).coerceAtLeast(0)
        return violation
    }

    private fun calcTime() : Double {
        recalculateCurrentTime()
        var time = 0.0
        list.forEachIndexed { i, customer ->
            if (i != list.lastIndex) {
                time += customer.calcDistance(list[i + 1])
            }
        }
        return time + calcViolation() * penalty
    }

    fun bestMove(path: Vehicle) : Double {
        var effect: Double? = null
        bestIndexes[0] = -1
        bestIndexes[1] = -1
        list.forEachIndexed { i, _ ->
            val myTime = time
            for (j in path.list.indices) {
                val currentEffect = path.time + myTime - moveEffectiveness(path, i, j)
                if ((effect == null || currentEffect > effect as Double) && currentEffect != 0.0) {
                    bestIndexes[0] = i
                    bestIndexes[1] = j
                    effect = currentEffect
                }
            }
        }
        return effect ?: 0.0
    }

    private fun moveEffectiveness(path: Vehicle, indexFrom: Int, indexTo: Int) : Double {
        val newMyPath = copy()
        val newPath = path.copy()
        newMyPath.move(newPath, indexFrom, indexTo)
        return newPath.time + newMyPath.time
    }

    fun move(path: Vehicle, indexFrom: Int, indexTo: Int) {
        if (!list[indexFrom].isMuted) {
            if (indexFrom in 1 until list.lastIndex) {
                if (path.isEmpty()) {
                    currentCapacity -= list[indexFrom].demand

                    path.add(list[indexFrom])
                    path.add(depot)
                    deleteAt(indexFrom)
                } else if (indexTo in 1..path.list.lastIndex) {
                    currentCapacity -= list[indexFrom].demand

                    path.insertInto(indexTo, list[indexFrom])
                    deleteAt(indexFrom)
                    if (isEmpty()) {
                        deleteAt(list.lastIndex)
                    }
                }
            }
        }
    }

    fun isEmpty() = time == 0.0

    fun isNotEmpty() = !isEmpty()
}
