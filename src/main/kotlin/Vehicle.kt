
import Depot.depot
import GlobalVariables.maxCapacity
import GlobalVariables.penalty

class Vehicle(val id: Int) {
    val list = arrayListOf<Customer>()
    val coordinatesArray
        get() = syncCoordinates()
    val idsList
        get() = syncIds()
    val timeList
        get() = syncTime()
    var violation = 0.0
        private set
    var currentCapacity = 0
    var currentTime = 0.0
    var time = 0.0
        get() = calcTime()
        private set

    val last
        get() = list.last()
    val bestIndexes = arrayOf(-1, -1)

    constructor(id: Int, list: ArrayList<Customer>) : this(id) {
        list.forEach {
            this.add(it)
        }
    }

    private fun syncCoordinates() : Array<MutableList<Int>>{
        val coordinates = arrayOf(mutableListOf<Int>(), mutableListOf())
        list.forEach {
            coordinates[0].add(it.x)
            coordinates[1].add(it.y)
        }
        return coordinates
    }

    private fun syncIds() : ArrayList<Int> {
        val ids = arrayListOf<Int>()
        list.forEach {
            ids.add(it.id)
        }
        return ids
    }

    private fun syncTime() : ArrayList<Double> {
        val times = arrayListOf<Double>()
        recalculateCurrentTime()
        list.forEachIndexed { i, it ->
            if (it.demand == 0 && i == 0) {
                times.add(it.ts + it.getDelay())
            } else {
                times.add(it.arrivalTime + it.getDelay())
            }
        }
        return times
    }

    private fun syncAll() {
        coordinatesArray[0].clear()
        coordinatesArray[1].clear()
        idsList.clear()
        timeList.clear()

        list.forEach {
            coordinatesArray[0].add(it.x)
            coordinatesArray[1].add(it.y)
            idsList.add(it.id)
        }
        recalculateCurrentTime()
    }

    fun clear() {
        list.clear()
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
    }

    private fun insertInto(index: Int, value: Customer) {
        currentCapacity += value.demand
        list.add(index, value)
        recalculateCurrentTime()
    }

    private fun recalculateCurrentTime() {
        currentTime = 0.0
        list[0].arrivalTime = currentTime
        currentTime += list[0].getTimeSpent()
        list.forEachIndexed { i, customer ->
            if (i != 0) {
                currentTime += list[i - 1].calcDistance(customer)
                customer.arrivalTime = currentTime
                currentTime += customer.getTimeSpent()
            }
        }
    }

    private fun deleteAt(index: Int) {
        currentCapacity -= list[index].demand
        list.removeAt(index)
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

    fun improvePosition() : Double {
        var lastTime = time
        val defaultTime = lastTime
        var i = 1
        while (i < list.lastIndex) {
            val vehicle = list[i]
            deleteAt(i)
            var improved = false
            for (j in 1 until list.lastIndex) {
                insertInto(j, vehicle)
                if (time < lastTime) {
                    lastTime = time
                    improved = true
                    break
                }
                deleteAt(j)
            }
            if (!improved) {
                insertInto(i, vehicle)
                i++
            }
        }
        return defaultTime - lastTime
    }

    private fun swap(i: Int, j: Int) {
        list[i] = list[j].also { list[j] = list[i] }
    }

    fun bestMove(path: Vehicle) : Double {
        var effect = 0.0
        bestIndexes[0] = -1
        bestIndexes[1] = -1
        for (i in 1 until list.lastIndex) {
            for (j in 1 until path.list.size) {
                val currentEffect = moveEffectiveness(path, i, j)
                if ((effect == 0.0 || currentEffect > effect) && currentEffect != 0.0) {
                    bestIndexes[0] = i
                    bestIndexes[1] = j
                    effect = currentEffect
                }
            }
        }
        return effect
    }

    private fun moveEffectiveness(path: Vehicle, indexFrom: Int, indexTo: Int) : Double {
        val oldTime = path.time + time
        if (move(path, indexFrom, indexTo)) {
            val newTime = path.time + time
            path.move(this, indexTo, indexFrom)
            return oldTime - newTime
        }
        return 0.0
    }

    fun move(path: Vehicle, indexFrom: Int, indexTo: Int) : Boolean {
        if (!list[indexFrom].isMuted) {
            if (indexFrom in 1 until list.lastIndex) {
                if (indexTo in 1..path.list.lastIndex) {
                    currentCapacity -= list[indexFrom].demand

                    path.insertInto(indexTo, list[indexFrom])
                    deleteAt(indexFrom)
                    return true
                }
            }
        }
        return false
    }

    fun isAvailable() : Boolean {
        list.forEachIndexed { i, customer ->
            if (i in 1 until list.lastIndex) {
                if (!customer.isMuted) {
                    return true
                }
            }
        }
        return false
    }

    fun isEmpty() = list.size <= 2

    fun isNotEmpty() = !isEmpty()
}
