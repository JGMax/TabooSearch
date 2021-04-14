import GlobalVariables.maxCapacity
import GlobalVariables.vehiclesNumber
import java.io.File

object DataManager {
    lateinit var inputFile: String
    lateinit var outputFile: String

    fun readData(delimiters:String = ",", lines2Miss: Int = 1) {
        val lines = File(inputFile).readLines()
        lines.forEachIndexed { i, s ->
            val dataList = s.split(delimiters).map {
                if (it.isNotEmpty()) {
                    it.toDouble().toInt()
                } else { -1 }
            }.toMutableList()
            dataList.removeAll { it == -1 }
            when (i) {
                0 -> {
                    vehiclesNumber = dataList[1]
                    maxCapacity = dataList[2]
                }
                1 -> {
                    val customer = Customer(dataList)
                    Depot.set(customer)
                }
                else -> {
                    val customer = Customer(dataList)
                    Customers.addCustomer(customer)
                }
            }
        }
        Customers.updateData()
    }

    fun writeData(vehicles: ArrayList<Vehicle>, effectiveness: Double = -1.0) {
        File(outputFile).printWriter().use {
            if (effectiveness != -1.0) {
                it.println("Effectiveness: $effectiveness")
            }
            vehicles.forEach { v ->
                if (v.isNotEmpty()) {
                    v.idsList.forEachIndexed { i, id -> it.print("$id ${v.timeList[i].round(2)} ") }
                    it.println()
                }
            }
        }
    }
}