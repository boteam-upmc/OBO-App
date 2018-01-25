package fr.upmc.boteam.obo_app;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.upmc.boteam.obo_app.services.ServerService;

import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SHOW_HELLO_WORLD;
import static fr.upmc.boteam.obo_app.services.ServerService.ACTION_SHOW_YOLO;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_HELLO;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_WORLD;
import static fr.upmc.boteam.obo_app.services.ServerService.EXTRA_YOLO;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ServerServiceTest {

    private Context appContext;

    @Before
    public void prepare() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void useAppContext() throws Exception {
        assertEquals("fr.upmc.boteam.obo_app", appContext.getPackageName());
    }

    @Test
    public void testActionShowHelloWorld() throws Exception {
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SHOW_HELLO_WORLD);
        intent.putExtra(EXTRA_HELLO, "hello");
        intent.putExtra(EXTRA_WORLD, "world");
        appContext.startService(intent);
    }

    @Test
    public void testActionShowYolo() throws Exception {
        Intent intent = new Intent(appContext, ServerService.class);
        intent.setAction(ACTION_SHOW_YOLO);
        intent.putExtra(EXTRA_YOLO, "yolo");
        appContext.startService(intent);
    }
}
