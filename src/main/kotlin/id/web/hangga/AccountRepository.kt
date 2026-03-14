package id.web.hangga

object AccountRepository {

    val account1 = Account("19000123", "Hangga", 1000)
    val account2 = Account("19000234", "John", 1000)
    val account3 = Account("19000345", "Alice", 2000)

    val accounts = mapOf(
        account1.number to account1,
        account2.number to account2,
        account3.number to account3
    )

    fun find(number: String) = accounts[number]
}