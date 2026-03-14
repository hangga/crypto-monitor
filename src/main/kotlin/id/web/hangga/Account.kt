package id.web.hangga

class Account(
    val number: String,
    val name: String,
    var balance: Int
) {

    private fun deposit(amount: Int) {
        balance += amount
    }

    private fun withdraw(amount: Int) {
        balance -= amount
    }

    fun transfer(to: Account, amount: Int) {
        println("${this.name}(${this.number}) tries to transfer $amount to ${to.name}(${to.number})")

        synchronized(this) {
            Thread.sleep(100)

            if (balance >= amount) {
                withdraw(amount)

                synchronized(to) {
                    to.deposit(amount)
                }
            }
        }
    }
}