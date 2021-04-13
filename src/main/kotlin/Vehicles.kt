import GlobalVariables.vehiclesNumber

object Vehicles {
    val bestSolution = arrayListOf<Vehicle>()
    var bestEffectiveness = 0.0
    val vehicleArray = arrayListOf<Vehicle>()

    fun init(depot: Depot) {
        for (i in 0 until vehiclesNumber) {
            val vehicle = Vehicle()
            vehicle.add(depot)
            vehicle.add(depot)
            vehicleArray.add(vehicle)
        }
    }

    fun initialSolution() {

    }
}
