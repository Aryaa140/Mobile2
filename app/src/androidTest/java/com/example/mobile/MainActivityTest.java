package com.example.mobile;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        ViewInteraction editText = onView(
                allOf(withId(R.id.username), withText("Masukkan Username"),
                        withParent(withParent(withId(R.id.cardLogin))),
                        isDisplayed()));
        editText.check(matches(withText("Arya")));

        ViewInteraction button = onView(
                allOf(withId(R.id.btnLogin), withText("Masuk"),
                        withParent(withParent(withId(R.id.cardLogin))),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.password), withText("Masukkan Password"),
                        withParent(withParent(withId(R.id.cardLogin))),
                        isDisplayed()));
        editText2.check(matches(withText("12121212")));

        ViewInteraction textView = onView(
                allOf(withId(R.id.BuatAkun), withText("Belum Punya Akun?"),
                        withParent(withParent(withId(R.id.cardLogin))),
                        isDisplayed()));
        textView.check(matches(withText("Belum Punya Akun?")));
    }
}
