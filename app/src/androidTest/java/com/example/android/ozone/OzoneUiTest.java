package com.example.android.ozone;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.ozone.ui.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
// Check Bake App for more examples
@RunWith(AndroidJUnit4.class)
public class OzoneUiTest {

    @Rule
    public ActivityTestRule<MainActivity> mTestRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void checkIfCorrectFragmentIsDisplayed(){

        onView(withId(R.id.favourite)).perform(click()).check(matches(isDisplayed()));
    }
}
