import Constants.PENALTY_START_VALUE
import Depot.depot
import GlobalVariables.maxCapacity
import GlobalVariables.penalty
import GlobalVariables.vehiclesNumber
import java.awt.Color

object Vehicles {
    var bestSolution = arrayListOf<Vehicle>()
    var bestTime = 0.0
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
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime <= 300000) {
            bestMove()
            updateData()
        }
    }

    private fun bestMove() {
        var bestEffect = 0.0
        lateinit var bestVehicle1: Vehicle
        lateinit var bestVehicle2: Vehicle
        var index1 = -1
        var index2 = -1

        val availableVehicles = vehicles.copy()
        availableVehicles.removeIf { it.isEmpty() }
        val emptyVehicle = vehicles.firstOrNull { it.isEmpty() }
        if (emptyVehicle != null) {
            availableVehicles.add(emptyVehicle)
        }

        for (vehicle1 in availableVehicles) {
            if (vehicle1.isNotEmpty()) {
                for (vehicle2 in availableVehicles) {
                    if (vehicle1.id != vehicle2.id) {
                        val effect = vehicle1.bestMove(vehicle2)
                        if ((bestEffect == 0.0 || effect > bestEffect) && effect != 0.0) {
                            bestEffect = effect
                            bestVehicle1 = vehicle1
                            bestVehicle2 = vehicle2
                            index1 = vehicle1.bestIndexes[0]
                            index2 = vehicle1.bestIndexes[1]
                        }
                    }
                }
            }
        }

        bestVehicle1.apply {
            val customer = list[index1]
            move(bestVehicle2, index1, index2)
            customer.isMuted = true
        }
    }

    private fun showBest() {
        val bestCoordinates = Array(2) { mutableListOf<Int>() }
        bestSolution.forEach {
            bestCoordinates[0].addAll(it.coordinatesArray[0])
            bestCoordinates[1].addAll(it.coordinatesArray[1])
        }
        Chart.updateData("Best", bestCoordinates)
    }

    private fun init() {
        for (i in 0 until vehiclesNumber) {
            val vehicle = Vehicle(i)
            vehicle.add(depot)
            vehicles.add(vehicle)
            Chart.addSeries("$i", vehicle.coordinatesArray)
        }
        Chart.addSeries("Best", vehicles[0].coordinatesArray, Color.BLUE)
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
        vehicles[i].add(depot)
    }

    private fun updateData() {
        val effectiveness = calcTime()
        val usedVehicles = countUsedVehicles()
        val violation = calcViolation()

        Customers.updateData()
        updateBestValues(effectiveness)
        showData(effectiveness, usedVehicles, violation)
        penaltyChange(violation)
    }

    private fun penaltyChange(violation: Double) {
        if (violation > 0) {
            if (penalty * 2 < Long.MAX_VALUE && penalty * 2 > 0) {
                penalty *= 2
            } else {
                penalty = Long.MAX_VALUE
            }
        } else {
            penalty = PENALTY_START_VALUE
        }
    }

    private fun updateBestValues(currentEffectiveness: Double) {
        if (bestTime == 0.0 || bestTime > currentEffectiveness) {
            bestUsedVehicles = countUsedVehicles()
            bestTime = currentEffectiveness
            bestSolution = vehicles.copy()
            DataManager.writeData(bestSolution, bestTime)
            showBest()
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

    private fun calcTime() : Double {
        var time = 0.0
        vehicles.forEach {
            if (it.isNotEmpty()) {
                time += it.time
            }
        }
        return time
    }

    private fun countUsedVehicles() = vehicles.count { it.isNotEmpty() }

    private fun showData(currentEffectiveness: Double, usedVehicles: Int, violation: Double) {
        visualize()
        println("Best effectiveness{$bestUsedVehicles}: $bestTime " +
                "Current effectiveness{$usedVehicles}: $currentEffectiveness " +
                "Violation: $violation Penalty: $penalty " +
                "Muted customers: ${Customers.mutedSize()}")
    }

    private fun visualize() {
        vehicles.forEachIndexed { i, vehicle ->
            Chart.updateData("$i", vehicle.coordinatesArray)
        }
    }
}
