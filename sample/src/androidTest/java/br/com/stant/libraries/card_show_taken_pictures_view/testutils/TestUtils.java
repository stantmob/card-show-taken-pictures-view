package br.com.stant.libraries.card_show_taken_pictures_view.testutils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static java.lang.Thread.sleep;

/**
 * Created by root on 26/08/16.
 */
public class TestUtils {

    public static void waitEspresso(long milles) {
        try {
            sleep(milles);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void checkIfIdIsDisplayed(int id){
        onView(withId(id)).check(matches(isDisplayed()));
    }

    public static void findAndClick(int id){
        onView(withId(id)).perform(click());
    }

}
