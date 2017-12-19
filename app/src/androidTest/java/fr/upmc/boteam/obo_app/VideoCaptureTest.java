package fr.upmc.boteam.obo_app;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideoCaptureTest {

    @Rule
    public ActivityTestRule<VideoCapture> mActivityTestRule = new ActivityTestRule<>(VideoCapture.class);

    @Test
    public void mainActivityTest() throws InterruptedException {
        onView(withId(R.id.sv_camera)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.sv_camera)).perform(click());
    }
}