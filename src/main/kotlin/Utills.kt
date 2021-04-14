import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Customer.calcDistance(c2: Customer) =
    sqrt((x - c2.x).toDouble().pow(2)
            + (y - c2.y).toDouble().pow(2))

fun Customer.nearestCustomer(array: ArrayList<Customer>) : Customer {
    var minDistance = calcDistance(array[0])
    var minCustomer: Customer = array[0]
    array.forEach {
        val distance = calcDistance(it)
        if (distance < minDistance) {
            minDistance = distance
            minCustomer = it
        }
    }
    return minCustomer
}

fun <T> ArrayList<T>.copy() : ArrayList<T> {
    val arr = ArrayList<T>()
    forEach {
        arr.add(it)
    }
    return arr
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
}

