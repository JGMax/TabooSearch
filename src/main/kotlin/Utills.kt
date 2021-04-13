import kotlin.math.pow
import kotlin.math.sqrt

fun Customer.calcDistance(c2: Customer) =
    sqrt((x - c2.x).toDouble().pow(2)
            + (y - c2.y).toDouble().pow(2))

fun Customer.nearestCustomer(array: ArrayList<Customer>) : Customer {
    //todo find nearest customer
    return array[0]
}
