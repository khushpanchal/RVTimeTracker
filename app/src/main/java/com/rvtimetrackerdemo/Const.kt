package com.rvtimetrackerdemo

import java.util.UUID

object Const {

    val initialItems = listOf(
        Item("John", "Smith", UUID.randomUUID().toString()),
        Item("John", "Smith", UUID.randomUUID().toString()),
        Item("Jane", "Johnson", UUID.randomUUID().toString()),
        Item("Alice", "Brown", UUID.randomUUID().toString()),
        Item("David", "Martinez", UUID.randomUUID().toString()),
        Item("Michael", "Garcia", UUID.randomUUID().toString()),
        Item("Olivia", "Davis", UUID.randomUUID().toString()),
        Item("Sophia", "Lee", UUID.randomUUID().toString()),
        Item("Daniel", "Wilson", UUID.randomUUID().toString()),
        Item("Eva", "Lopez", UUID.randomUUID().toString())
    )

    private fun generateRandomName(): String {
        val names = arrayOf(
            "Michael", "Olivia", "David", "Sophia", "Daniel", "Eva", "Messi",
            "Sam", "Reece", "Liam", "Jason", "Gus", "Mark", "Chris", "Ben"
        )
        val randomIndex = (names.indices).random()
        return names[randomIndex]
    }

    private fun generateRandomSurname(): String {
        val surnames = arrayOf(
            "Lee", "Garcia", "Martinez", "Davis", "Anderson", "Wilson", "Lopez",
            "Green", "Stokes", "Root", "Wakes", "Wood", "Atkinson", "Willey", "Curran"
        )
        val randomIndex = (surnames.indices).random()
        return surnames[randomIndex]
    }

    fun generateUniqueItem(): Item {
        val name = generateRandomName()
        val surname = generateRandomSurname()
        return Item(name, surname, UUID.randomUUID().toString())
    }

}