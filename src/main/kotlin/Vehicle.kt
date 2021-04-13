import GlobalVariables.penalty
import kotlin.math.abs

class Vehicle() {
    val list = arrayListOf<Customer>()
    val idsArray = Array(2) { mutableListOf<Int>() }
    val timeList = arrayListOf<Int>()
    var currentCapacity = 0
    var effectiveness = 0.0
        get() {
            return if (changed) {
                changed = false
                calcEffectiveness()
            } else {
                field
            }
        }
    private set
    private var changed = true

    constructor(list: ArrayList<Customer>) : this() {
        list.forEach {
            this.list.add(it)
        }
        syncAll()
    }

    fun clear() {
        list.clear()
        idsArray[0].clear()
        idsArray[1].clear()
        timeList.clear()
        effectiveness = 0.0
        currentCapacity = 0
    }

    fun copy() : Vehicle = Vehicle(list)

    fun add(customer: Customer) {
        currentCapacity += customer.demand
        if (list.size < 2) {
            list.add(customer)
            idsArray[0].add(customer.x)
            idsArray[1].add(customer.y)
            timeList.add(customer.arrivalTime + customer.getDelay())
        } else {
            list.add(list.lastIndex, customer)
            idsArray[0].add(list.lastIndex, customer.x)
            idsArray[1].add(list.lastIndex, customer.y)
            timeList.add(list.lastIndex, customer.arrivalTime + customer.getDelay())
        }
    }

    private fun insertInto(index: Int, value: Customer) {
        currentCapacity += value.demand
        list.add(index, value)
        idsArray[0].add(index, value.x)
        idsArray[1].add(index, value.y)
        timeList.add(index, value.arrivalTime + value.getDelay())
    }

    private fun deleteAt(index: Int) {
        currentCapacity -= list[index].demand
        list.removeAt(index)
        idsArray[0].removeAt(index)
        idsArray[1].removeAt(index)
        timeList.removeAt(index)
    }

    private fun calcViolation() : Int {
        var violation = 0
        list.forEach {
            violation += it.getViolation()
        }
        //todo capacity violation
        return violation
    }

    private fun sync(index: Int) {
        idsArray[0][index] = list[index].x
        idsArray[1][index] = list[index].y

        timeList[index] = list[index].arrivalTime + list[index].getDelay()
    }

    private fun syncAll() {
        idsArray[0].clear()
        idsArray[1].clear()
        list.forEach {
            idsArray[0].add(it.x)
            idsArray[1].add(it.y)
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

    fun doubleOptEffectiveness(edgeAfterIndex1: Int, edgeAfterIndex2: Int) : Double {
        if (edgeAfterIndex1 in 0 until list.lastIndex
            && edgeAfterIndex2 in 0 until list.lastIndex
            && abs(edgeAfterIndex1 - edgeAfterIndex2) in 2 until list.size) {
            val newPath = Vehicle(list)
            newPath.doubleOpt(edgeAfterIndex1, edgeAfterIndex2)
            return newPath.effectiveness
        }
        return 0.0
    }

    fun insertOptEffectiveness(index: Int, afterIndexInsert: Int) : Double {
        if (index in 1 until list.lastIndex && afterIndexInsert in 0 until list.lastIndex) {
            val newPath = Vehicle(list)
            newPath.insertOpt(index, afterIndexInsert)
            return newPath.effectiveness
        }
        return 0.0
    }

    fun swapEffectiveness(path: Vehicle, myIndex: Int, pathIndex: Int) : Double {
        if (myIndex in 1 until list.lastIndex && pathIndex in 1 until path.list.lastIndex) {
            val newMyPath = Vehicle(list)
            val newPath = Vehicle(path.list)
            newMyPath.swap(newPath, myIndex, pathIndex)
            return newPath.effectiveness + newMyPath.effectiveness
        }
        return 0.0
    }

    fun moveEffectiveness(path: Vehicle, indexFrom: Int, indexTo: Int) : Double {
        if (indexFrom in 1 until list.lastIndex && indexTo in 1 until path.list.lastIndex) {
            val newMyPath = Vehicle(list)
            val newPath = Vehicle(path.list)
            newMyPath.move(newPath, indexFrom, indexTo)
            return newPath.effectiveness + newMyPath.effectiveness
        }
        return 0.0
    }

    fun doubleOpt(edgeAfterIndex1: Int, edgeAfterIndex2: Int) {
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

    fun insertOpt(index: Int, afterIndexInsert: Int) {
        if (index in 1 until list.lastIndex && afterIndexInsert in 0 until list.lastIndex) {
            changed = true
            insertInto(afterIndexInsert + 1, list[index])
            deleteAt(index)
        }
    }

    fun swap(path: Vehicle, myIndex: Int, pathIndex: Int) {
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

    fun move(path: Vehicle, indexFrom: Int, indexTo: Int) {
        if (indexFrom in 1 until list.lastIndex && indexTo in 1 until path.list.lastIndex) {
            changed = true

            currentCapacity -= list[indexFrom].demand

            path.insertInto(indexTo, list[indexFrom])
            deleteAt(indexFrom)
        }
    }

    fun isEmpty() = effectiveness == 0.0
}
