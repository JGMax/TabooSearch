import kotlin.math.pow
import kotlin.math.sqrt

fun Customer.calcDistance(c2: Customer) =
    sqrt((x - c2.x).toDouble().pow(2)
            + (y - c2.y).toDouble().pow(2))

fun Customer.nearestCustomer(array: ArrayList<Customer>) : Customer {
    var maxDistance = calcDistance(array[0])
    var maxCustomer: Customer = array[0]
    array.forEach {
        val distance = calcDistance(it)
        if (distance > maxDistance) {
            maxDistance = distance
            maxCustomer = it
        }
    }
    return maxCustomer
}

fun <T> ArrayList<T>.copy() : ArrayList<T> {
    val arr = ArrayList<T>(size)
    arr.forEachIndexed { i, _ ->
        arr[i] = get(i)
    }
    return arr
}
