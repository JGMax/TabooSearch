import Constants.MAX_MEMORY_DURATION
import Constants.MIN_MEMORY_DURATION
import Constants.PENALTY_INCREASE
import Constants.PENALTY_START_VALUE
import Constants.VISUALIZE
import GlobalVariables.maxCapacity
import GlobalVariables.penalty
import GlobalVariables.vehiclesNumber
import java.awt.Color
import kotlin.random.Random
import kotlin.system.exitProcess

object Vehicles {
    var bestSolution = arrayListOf<Vehicle>()
    var bestTime = 0.0
    var bestUsedVehicles = 0
    private val vehicles = arrayListOf<Vehicle>()
    private val currentTime = Array(2) { mutableListOf(0.0) }
    private val currentTimeWithViolation = Array(2) { mutableListOf(0.0) }
    private var step = 0.0

    fun run() {
        init()
        initialSolution()
        updateData()
        search()
        println("End")
    }

    private fun search() {
        while (true) {
            bestMove()
            updateData()
        }
    }

    private fun updateTimeData() {
        if (VISUALIZE) {
            step += 1
            if (step > 500) {
                if (currentTimeWithViolation[0].isNotEmpty() && step - currentTimeWithViolation[0][0] > 500) {
                    currentTimeWithViolation[0].removeAt(0)
                    currentTimeWithViolation[1].removeAt(0)
                }
                currentTime[0].removeAt(0)
                currentTime[1].removeAt(0)
            }
            if (calcViolation() > 0) {
                currentTimeWithViolation[0].add(step)
                currentTimeWithViolation[1].add(calcTime())
            }
            currentTime[0].add(step)
            currentTime[1].add(calcTime())
        }
    }

    private fun bestMove() {
        var bestEffect = 0.0
        lateinit var bestVehicle1: Vehicle
        lateinit var bestVehicle2: Vehicle
        var index1 = -1
        var index2 = -1

        val availableVehicles = arrayListOf<Vehicle>()
        vehicles.forEach {
            if (it.isNotEmpty()) {
                availableVehicles.add(it)
            }
        }

        val emptyVehicle = vehicles.firstOrNull { it.isEmpty() }
        if (emptyVehicle != null) {
            availableVehicles.add(emptyVehicle)
        }

        for (vehicle1 in availableVehicles) {
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
        bestVehicle1.apply {
            val customer = list[index1]
            move(bestVehicle2, index1, index2)
            customer.isMuted = true
            customer.maxMutedSteps = Random.nextInt(MIN_MEMORY_DURATION, MAX_MEMORY_DURATION)
            improvePosition()
            bestVehicle2.improvePosition()
        }
    }

    private fun showBest() {
        if (VISUALIZE) {
            val bestCoordinates = Array(2) { mutableListOf<Int>() }
            bestSolution.forEach {
                bestCoordinates[0].addAll(it.coordinatesArray[0])
                bestCoordinates[1].addAll(it.coordinatesArray[1])
            }
            Chart.updateData("Best", bestCoordinates)
        }
    }

    private fun init() {
        for (i in 0 until vehiclesNumber) {
            val vehicle = Vehicle(i)
            vehicle.add(Depot.clone())
            vehicles.add(vehicle)
        }
        visualizationInit()
    }

    private fun visualizationInit() {
        if (VISUALIZE) {
            vehicles.forEach {
                Chart.addSeries("${it.id}", it.coordinatesArray)
            }
            Chart.addSeries("Best", vehicles[0].coordinatesArray, Color.BLUE)

            TimeChangingChart.addSeries("currentTime", currentTime, Color.BLUE)
            TimeChangingChart.addSeries("with violation", currentTimeWithViolation, Color.RED)
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
                i++
            }
        }
        closePath()
    }

    private fun closePath() {
        vehicles.forEach {
            it.add(Depot.clone())
        }
    }

    private fun updateData() {
        val effectiveness = calcTime()
        val usedVehicles = countUsedVehicles()
        val violation = calcViolation()

        Customers.updateData()
        updateBestValues(effectiveness, violation)
        updateTimeData()
        penaltyChange(violation)
        showData(effectiveness, usedVehicles, violation)
        if (bestTime < 828) {
            bestSolution.forEach {
                println("${it.id} Time: ${it.time} Violation: ${it.violation}")
            }
            exitProcess(0)
        }
    }

    private fun penaltyChange(violation: Double) {
        if (violation > 0) {
            if (penalty * PENALTY_INCREASE < Long.MAX_VALUE && penalty * PENALTY_INCREASE > 0) {
                penalty *= PENALTY_INCREASE
            } else {
                penalty = Long.MAX_VALUE
            }
        } else {
            penalty = PENALTY_START_VALUE
        }
    }

    private fun updateBestValues(currentEffectiveness: Double, violation: Double) {
        if ((bestTime == 0.0 || bestTime > currentEffectiveness)) {
            bestUsedVehicles = countUsedVehicles()
            bestTime = currentEffectiveness
            bestSolution = vehicles.copy()
            if (violation == 0.0) {
                DataManager.writeData(bestSolution, bestTime)
            }
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
        println("Best time{$bestUsedVehicles}: $bestTime " +
                "Current time{$usedVehicles}: $currentEffectiveness " +
                "Violation: $violation Penalty: $penalty " +
                "Muted: ${Customers.mutedSize()}")
    }

    private fun visualize() {
        if (VISUALIZE) {
            vehicles.forEachIndexed { i, vehicle ->
                Chart.updateData("$i", vehicle.coordinatesArray)
            }
            if (currentTime.isNotEmpty()) {
                TimeChangingChart.updateData("currentTime", currentTime)
            }
            if (currentTimeWithViolation.isNotEmpty()) {
                TimeChangingChart.updateData("with violation", currentTimeWithViolation)
            }
        }
    }
}
