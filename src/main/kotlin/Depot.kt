object Depot {
    lateinit var depot: Customer

    fun set(customer: Customer) {
        depot = customer
    }

    fun set(id: Int, x: Int, y: Int, demand: Int,
            ts: Int, te: Int, service: Int) {
        depot = Customer(id, x, y, demand, ts, te, service)
    }
}
