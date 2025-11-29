package me.tomasan7.jecnaapi.util

import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.CanteenClient
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate


internal class CanteenTest {
    @Test
    fun testPutOnExchange() {
        val client = CanteenClient()
        runBlocking {
            client.login("vegh", "Fkcw5428")

            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val nextWeekMonday = thisMonday.plusWeeks(1)

            val mondayMenu = client.getDayMenu(nextWeekMonday)

            val firstMeal = mondayMenu.items.firstOrNull()
                ?: error("No meals for Monday $nextWeekMonday")

            val success = client.putOnExchange(firstMeal)
            println("Put on exchange success: $success")
        }
    }

    @Test
    fun testPutAwayFromExchange() {
        val client = CanteenClient()
        runBlocking {
            client.login("vegh", "Fkcw5428")

            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val nextWeekMonday = thisMonday.plusWeeks(1)

            val mondayMenu = client.getDayMenu(nextWeekMonday)

            // Find a meal that is in exchange
            val mealInExchange = mondayMenu.items.firstOrNull()  ?: error("No meals for Monday $nextWeekMonday")

            val success = client.putAwayFromExchange(mealInExchange)
            println("Put away from exchange success: $success")
        }
    }
    @Test
    fun testCorrectMenuItemProperties() {
        val client = CanteenClient()
        runBlocking {
            client.login("vegh", "Fkcw5428")

            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val nextWeekMonday = thisMonday.plusWeeks(1)

            val mondayMenu = client.getDayMenu(nextWeekMonday)

            for (item in mondayMenu.items) {
                println(item.isInExchange)
                println(item.isOrdered)
            }

            println("All menu items have correct properties.")
        }
    }

    @Test
    fun testOrderSecondMealOnTuesday() {
        val client = CanteenClient()
        runBlocking {
            client.login("vegh", "Fkcw5428")

            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val nextWeekTuesday = thisMonday.plusWeeks(1).with(DayOfWeek.TUESDAY)

            val tuesdayMenu = client.getDayMenu(nextWeekTuesday)

            if (tuesdayMenu.items.size < 2) {
                error("Not enough meals on Tuesday $nextWeekTuesday to perform the test.")
            }

            val secondMeal = tuesdayMenu.items[1]

            val orderSuccess = client.order(secondMeal)
            println("Order second meal success: $orderSuccess")
        }
    }
}
