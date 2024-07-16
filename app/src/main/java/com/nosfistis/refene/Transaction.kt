package com.nosfistis.refene

/**
 * Created by Mike on 1/5/2017.
 */
class Transaction {
    var id: Long = 0
    var price: Float
    private var person: Person? = null
    var description: String? = null

    constructor(price: Float, person: Person?) {
        this.price = price
        this.person = person
    }

    constructor(id: Long, price: Float, description: String?) {
        this.id = id
        this.price = price
        this.description = description
    }

    fun getPerson(): Person? {
        return person
    }

    fun setPerson(person: Person?) {
        this.person = person
    }
}
