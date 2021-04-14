
fun main() {
    /*Constants.SHORT_MEMORY_DURATION = memory.toInt()
    Constants.PENALTY_START_VALUE = penalty.toLong()*/
    DataManager.inputFile = "src/main/resources/input.txt"
    DataManager.outputFile = "src/main/resources/output.txt"
    DataManager.readData(" ")
    Vehicles.run()
}
