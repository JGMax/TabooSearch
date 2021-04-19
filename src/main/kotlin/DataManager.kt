
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

    fun validData(file: String) {
        val lines = File(file).readLines()

        var sum = 0.0
        lines.forEachIndexed { index, s ->
            val vehicle = Vehicle(index)
            s.split(" ").forEachIndexed { i, num ->
                if (i % 2 == 0) {
                    if (num.toInt() == 0) {
                        vehicle.list.add(Depot.clone())
                    } else {
                        val c = Customers.customers.find { it.id == num.toInt() }
                        if (c != null) {
                            vehicle.add(c)
                        }
                    }
                }
            }
            vehicle.list.forEach {
                print("${it.id} ")
            }
            println()
            vehicle.list.forEach {
                print("${it.getViolation()} ")
            }
            println()
            println("{${vehicle.id}}Violation ${vehicle.violation}")
            println("{${vehicle.id}}Time ${vehicle.time}")
            sum += vehicle.time
        }
        println("Sum of times: $sum")
    }

}