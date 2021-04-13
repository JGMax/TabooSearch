import Constants.PENALTY_START_VALUE
import Depot.depot
import GlobalVariables.maxCapacity
import GlobalVariables.penalty
import GlobalVariables.vehiclesNumber

object Vehicles {
    var bestSolution = arrayListOf<Vehicle>()
    var bestEffectiveness = 0.0
    var bestUsedVehicles = 0
    private val vehicles = arrayListOf<Vehicle>()

    fun run() {
        init()
        initialSolution()
        updateData()
        search()
        println("End")
    }

    private fun search() {
        var improved = true
        while (improved) {
            //improved = false
            val seq = arrayListOf(0, 1, 2, 3)
            seq.shuffle()
            seq@for (i in seq) {
                when (i) {
                    0 -> {
                        for (vehicle in vehicles) {
                            if(vehicle.isNotEmpty()) {
                                val effect = vehicle.doubleOpt()
                                if (effect > 0) {
                                    println("doubleOpt $effect")
                                    //improved = true
                                    break@seq
                                }
                            }
                        }
                    }
                    1 -> {
                        for (vehicle in vehicles) {
                            if(vehicle.isNotEmpty()) {
                                val effect = vehicle.insertOpt()
                                if (effect > 0) {
                                    println("insertOpt $effect")
                                    //improved = true
                                    break@seq
                                }
                            }
                        }
                    }
                    2 -> {
                        for (j in 0 until vehicles.lastIndex) {
                            if(vehicles[j].isNotEmpty()) {
                                for (k in j + 1..vehicles.lastIndex) {
                                    if (vehicles[k].isNotEmpty()) {
                                        val effect = vehicles[j].swap(vehicles[k])
                                        if (effect > 0) {
                                            println("swap $effect")
                                            //improved = true
                                            break@seq
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        for (vehicle in vehicles) {
                            if(vehicle.isNotEmpty()) {
                                for (vehicle1 in vehicles) {
                                    if (vehicle.id != vehicle1.id) {
                                        val effect = vehicle.move(vehicle1)
                                        // todo получить лучший эффект из всех доступных
                                        if (effect > 0) {
                                            println("move $effect")
                                            //improved = true
                                            break@seq
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            updateData()
            Customers.updateData()
        }
    }

    private fun init() {
        for (i in 0 until vehiclesNumber) {
            val vehicle = Vehicle(i)
            vehicle.add(depot)
            vehicles.add(vehicle)
            Chart.addSeries("$i", vehicle.coordinatesArray)
        }
    }

    private fun initialSolution() {
        var i = 0

        while (Customers.available.isNotEmpty()) {
            val customer = vehicles[i].last.nearestCustomer(Customers.available)
            if (vehicles[i].currentCapacity + customer.demand <= maxCapacity) {
                vehicles[i].add(customer)
                Customers.deleteAvailable(customer)
            } else {
                vehicles[i].add(depot)
                i++
            }
        }
    }

    private fun updateData() {
        val effectiveness = calcEffectiveness()
        val usedVehicles = countUsedVehicles()
        val violation = calcViolation()

        updateBestValues(effectiveness)
        showData(effectiveness, usedVehicles, violation)
        penaltyChange(violation)
    }

    private fun penaltyChange(violation: Double) {
        if (violation > 0) {
            penalty *= penalty
        } else {
            penalty = PENALTY_START_VALUE
        }
    }

    private fun updateBestValues(currentEffectiveness: Double) {
        if (bestEffectiveness == 0.0 || bestEffectiveness > currentEffectiveness) {
            bestUsedVehicles = countUsedVehicles()
            bestEffectiveness = currentEffectiveness
            bestSolution = vehicles.copy()
        }
    }

    private fun calcViolation() : Double {
        var violation = 0.0
        vehicles.forEach {
            if (it.isNotEmpty()) {
                violation += it.violation
            }
        }
        return violation
    }

    private fun calcEffectiveness() : Double {
        var effectiveness = 0.0
        vehicles.forEach {
            if (it.isNotEmpty()) {
                effectiveness += it.effectiveness
            }
        }
        return effectiveness
    }

    private fun countUsedVehicles() = vehicles.count { it.isNotEmpty() }

    private fun showData(currentEffectiveness: Double, usedVehicles: Int, violation: Double) {
        visualize()
        println("Best effectiveness{$bestUsedVehicles}: $bestEffectiveness " +
                "Current effectiveness{$usedVehicles}: $currentEffectiveness " +
                "Violation: $violation Penalty: $penalty")
    }

    private fun visualize() {
        vehicles.forEachIndexed { i, vehicle ->
            Chart.updateData("$i", vehicle.coordinatesArray)
        }
    }
}
