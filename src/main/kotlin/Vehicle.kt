
import Depot.depot
import GlobalVariables.maxCapacity
import GlobalVariables.penalty
import kotlin.math.abs

class Vehicle(val id: Int) {
    val list = arrayListOf<Customer>()
    val coordinatesArray = Array(2) { mutableListOf<Int>() }
    val idsList = arrayListOf<Int>()
    val timeList = arrayListOf<Double>()
    var violation = 0.0
        private set
    var currentCapacity = 0
    var currentTime = 0.0
    var effectiveness = 0.0
        private set
        get() {
            return if (changed) {
                changed = false
                field = calcEffectiveness()
                field
            } else {
                field
            }
        }
    private var changed = true
    val last
        get() = list.last()

    constructor(id: Int, list: ArrayList<Customer>) : this(id) {
        list.forEach {
            this.list.add(it)
        }
        syncAll()
    }

    fun clear() {
        list.clear()
        coordinatesArray[0].clear()
        coordinatesArray[1].clear()
        idsList.clear()
        timeList.clear()
        effectiveness = 0.0
        violation = 0.0
        currentCapacity = 0
    }

    fun copy() : Vehicle = Vehicle(id, list)

    fun add(customer: Customer) {
        if (list.isNotEmpty()) {
            currentTime += list.last().calcDistance(customer)
            customer.arrivalTime = currentTime
            currentTime += customer.getDelay()
        }
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
        timeList.add(index, value.arrivalTime + value.getDelay())
    }

    private fun deleteAt(index: Int) {
        currentCapacity -= list[index].demand
        list.removeAt(index)
        coordinatesArray[0].removeAt(index)
        coordinatesArray[1].removeAt(index)
        idsList.removeAt(index)
        timeList.removeAt(index)
    }

    private fun calcViolation() : Double {
        violation = 0.0
        list.forEach {
            violation += it.getViolation()
        }
        violation += (currentCapacity - maxCapacity).coerceAtLeast(0)
        return violation
    }

    private fun sync(index: Int) {
        coordinatesArray[0][index] = list[index].x
        coordinatesArray[1][index] = list[index].y
        idsList[index] = list[index].id
        timeList[index] = list[index].arrivalTime + list[index].getDelay()
    }

    private fun syncAll() {
        coordinatesArray[0].clear()
        coordinatesArray[1].clear()
        timeList.clear()
        idsList.clear()
        list.forEach {
            coordinatesArray[0].add(it.x)
            coordinatesArray[1].add(it.y)
            idsList.add(it.id)
            timeList.add(it.arrivalTime + it.getDelay())
        }
    }

    private fun calcEffectiveness() : Double {
        var effectiveness = 0.0
        list.forEachIndexed { i, customer ->
            if (i != list.lastIndex) {
                effectiveness += customer.calcDistance(list[i + 1])
            }
        }
        return effectiveness + calcViolation() * penalty
    }

    fun doubleOpt() : Double {
        var effect = 0.0
        list.forEachIndexed { i, _ ->
            for (j in i..list.lastIndex) {
                val currentEffect = effectiveness - doubleOptEffectiveness(i, j)
                if (currentEffect > effect) {
                    list[i].isMuted = true
                    doubleOpt(i, j)
                    effect = currentEffect
                }
            }
        }
        return effect
    }

    fun insertOpt() : Double {
        var effect = 0.0
        list.forEachIndexed { i, _ ->
            for (j in i..list.lastIndex) {
                val currentEffect = effectiveness - insertOptEffectiveness(i, j)
                if (currentEffect > effect) {
                    list[i].isMuted = true
                    insertOpt(i, j)
                    effect = currentEffect
                }
            }
        }
        return effect
    }

    fun swap(path: Vehicle) : Double {
        var effect = 0.0
        list.forEachIndexed { i, _ ->
            for (j in path.list.indices) {
                val currentEffect = path.effectiveness + effectiveness - swapEffectiveness(path, i, j)
                if (currentEffect > effect) {
                    list[i].isMuted = true
                    swap(path, i, j)
                    effect = currentEffect
                }
            }
        }
        return effect
    }

    fun move(path: Vehicle) : Double {
        var effect = 0.0
        list.forEachIndexed { i, _ ->
            for (j in path.list.indices) {
                val currentEffect = path.effectiveness + effectiveness - moveEffectiveness(path, i, j)
                if (currentEffect > effect) {
                    list[i].isMuted = true
                    move(path, i, j)
                    effect = currentEffect
                }
            }
        }
        return effect
    }

    private fun doubleOptEffectiveness(edgeAfterIndex1: Int, edgeAfterIndex2: Int) : Double {
        if (edgeAfterIndex1 in 0 until list.lastIndex
            && edgeAfterIndex2 in 0 until list.lastIndex
            && abs(edgeAfterIndex1 - edgeAfterIndex2) in 2 until list.size) {
            val newPath = copy()
            newPath.doubleOpt(edgeAfterIndex1, edgeAfterIndex2)
            return newPath.effectiveness
        }
        return effectiveness
    }

    private fun insertOptEffectiveness(index: Int, afterIndexInsert: Int) : Double {
        if (index in 1 until list.lastIndex && afterIndexInsert in 0 until list.lastIndex) {
            val newPath = copy()
            newPath.insertOpt(index, afterIndexInsert)
            return newPath.effectiveness
        }
        return effectiveness
    }

    private fun swapEffectiveness(path: Vehicle, myIndex: Int, pathIndex: Int) : Double {
        if (myIndex in 1 until list.lastIndex && pathIndex in 1 until path.list.lastIndex) {
            val newMyPath = copy()
            val newPath = path.copy()
            newMyPath.swap(newPath, myIndex, pathIndex)
            return newPath.effectiveness + newMyPath.effectiveness
        }
        return effectiveness + path.effectiveness
    }

    private fun moveEffectiveness(path: Vehicle, indexFrom: Int, indexTo: Int) : Double {
        if (indexFrom in 1 until list.lastIndex && indexTo in 1 until path.list.lastIndex) {
            val newMyPath = copy()
            val newPath = path.copy()
            newMyPath.move(newPath, indexFrom, indexTo)
            return newPath.effectiveness + newMyPath.effectiveness
        }
        return effectiveness + path.effectiveness
    }

    private fun doubleOpt(edgeAfterIndex1: Int, edgeAfterIndex2: Int) {
        if (edgeAfterIndex1 in 0 until list.lastIndex
            && edgeAfterIndex2 in 0 until list.lastIndex
            && abs(edgeAfterIndex1 - edgeAfterIndex2) in 2 until list.size) {
            changed = true
            var maxId = edgeAfterIndex1.coerceAtLeast(edgeAfterIndex2)
            var minId = edgeAfterIndex1.coerceAtMost(edgeAfterIndex2) + 1

            while (maxId > minId) {
                list[minId] = list[maxId].also { list[maxId] = list[minId] }
                maxId--
                minId++
            }
        }
    }

    private fun insertOpt(index: Int, afterIndexInsert: Int) {
        if (index in 1 until list.lastIndex && afterIndexInsert in 0 until list.lastIndex) {
            changed = true
            insertInto(afterIndexInsert + 1, list[index])
            deleteAt(index)
        }
    }

    private fun swap(path: Vehicle, myIndex: Int, pathIndex: Int) {
        if (myIndex in 1 until list.lastIndex && pathIndex in 1 until path.list.lastIndex) {
            changed = true

            currentCapacity -= list[myIndex].demand
            currentCapacity += path.list[pathIndex].demand

            path.currentCapacity -= path.list[pathIndex].demand
            path.currentCapacity += list[myIndex].demand

            list[myIndex] = path.list[pathIndex].also { path.list[pathIndex] = list[myIndex] }
            sync(myIndex)
            path.sync(pathIndex)
        }
    }

    private fun move(path: Vehicle, indexFrom: Int, indexTo: Int) {
        if (indexFrom in 1 until list.lastIndex) {
            if (path.isEmpty()) {
                changed = true

                currentCapacity -= list[indexFrom].demand

                path.add(list[indexFrom])
                path.add(depot)
                deleteAt(indexFrom)
            } else if (indexTo in 1 until path.list.lastIndex) {
                changed = true

                currentCapacity -= list[indexFrom].demand

                path.insertInto(indexTo, list[indexFrom])
                deleteAt(indexFrom)
            }
        }
    }

    fun isEmpty() = effectiveness == 0.0

    fun isNotEmpty() = !isEmpty()
}
