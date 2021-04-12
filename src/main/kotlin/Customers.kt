object Customers {
    private val customers = arrayListOf<Customer>()
    val muted = arrayListOf<Customer>()
    val available = arrayListOf<Customer>()

    fun addCustomer(customer: Customer) {
        customers.add(customer)
    }

    fun updateData() {
        addStep()
        updateLists()
    }

    private fun addStep() {
        muted.forEach {
            it.increaseMutedStep()
        }
    }

    private fun updateLists() {
        muted.clear()
        available.clear()
        customers.forEach {
            if (it.isMuted) {
                muted.add(it)
            } else {
                available.add(it)
            }
        }
    }
}
