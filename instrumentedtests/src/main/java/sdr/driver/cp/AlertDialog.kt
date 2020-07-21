package sdr.driver.cp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId

object AlertDialog {

    enum class Button(private val id: Int) {
        Positive(android.R.id.button1),
        Negative(android.R.id.button2),
        Neutral(android.R.id.button3);

        fun click() {
            onView(withId(id)).perform(ViewActions.click())
        }
    }
}
