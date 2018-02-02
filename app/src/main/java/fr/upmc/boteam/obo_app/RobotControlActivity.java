package fr.upmc.boteam.obo_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * This activity simulate robot remote control by reacting to the web browser's actions.
 */
public class RobotControlActivity extends AppCompatActivity {

    /** Useful console log tag. */
    private final String LOG_TAG = "RobotRemoteActivity";

    /** Used to assure that we control the correct robot */
    private String myUser;
    private String myRobot;

    protected Button btnReset;
    protected Button btnForwards;
    protected Button btnTurnLeft;
    protected Button btnStop;
    protected Button btnTurnRight;
    protected Button btnBackwards;

    protected ToggleButton tglRCMode;
    protected ToggleButton tglMode;
    protected ToggleButton tglLeds;

    protected SeekBar skBarAngVel;
    protected SeekBar skBarTime;
    protected SeekBar skBarAngle;
    protected SeekBar skBarPan;
    protected SeekBar skBarTilt;
    protected SeekBar skBarPeriod;

    /** Singleton connection to server */
    private Socket mSocket;
    {
        try {
            // Change this to your localhost ip address example : 192.168.42.187
            mSocket = IO.socket("http://" + MainActivity.SERVER_ADDRESS + ":3000/");


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);

        // get the user and the robot ids
        Intent intent = getIntent();
        myUser = intent.getStringExtra(Menu.EXTRA_USER);
        myRobot = intent.getStringExtra(Menu.EXTRA_ROBOT);

        Log.i(LOG_TAG, "myUser = " + myUser);
        Log.i(LOG_TAG, "myRobot = " + myRobot);

        // Buttons
        this.btnReset = findViewById(R.id.btnResetPanTilt);
        this.btnForwards = findViewById(R.id.btnForward);
        this.btnTurnLeft =  findViewById(R.id.btnTurnLeft);
        this.btnStop = findViewById(R.id.btnStop);
        this.btnTurnRight = findViewById(R.id.btnTurnRight);
        this.btnBackwards = findViewById(R.id.btnBarckward);

        // Toggle Buttons
        this.tglRCMode = findViewById(R.id.tglRCMode);
        this.tglMode = findViewById(R.id.tglMode);
        this.tglLeds = findViewById(R.id.tglLeds);

        // Seek Bars
        this.skBarAngVel = findViewById(R.id.skBarAngVel);
        this.skBarTime = findViewById(R.id.skBarTime);
        this.skBarAngle = findViewById(R.id.skBarAngle);
        this.skBarPan = findViewById(R.id.skBarPan);
        this.skBarTilt = findViewById(R.id.skBarTilt);
        this.skBarPeriod = findViewById(R.id.skBarStatusPeriod);

        btnForwards.setOnClickListener(clickButton);
        btnBackwards.setOnClickListener(clickButton);
        btnTurnLeft.setOnClickListener(clickButton);
        btnTurnRight.setOnClickListener(clickButton);
        btnStop.setOnClickListener(clickButton);
        btnReset.setOnClickListener(clickButton);

        tglRCMode.setChecked(false);
        tglMode.setChecked(false);
        tglLeds.setChecked(false);

        mSocket.connect();
        //sendTextualData();
        handleEvents();
    }

    /**
     * Show toast message on button click event.
     */
    private View.OnClickListener clickButton = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view instanceof Button) {
                Button bt = (Button) view;
                Toast.makeText(getApplicationContext(), bt.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    // ***
    /*public void sendTextualData() {
        JSONObject textualData = new JSONObject();

        textualData.put("gaps", txtGaps.getText());
        textualData.put("falls", txtFalls.getText());
        textualData.put("irs", txtIRs.getText());
        textualData.put("pan", txtPan.getText());
        textualData.put("tilt", txtTilt.getText());
        textualData.put("left", txtLeft.getText());
        textualData.put("right", txtRight.getText());
        textualData.put("battery", txtBattery.getText());

        mSocket.emit("set_data", textualData);
    }*/

    /**
     * Initialise events receiver.
     */
    public void handleEvents() {
        mSocket.on("get_reset", onReset);
        mSocket.on("get_forward", onForwards);
        mSocket.on("get_left", onTurnLeft);
        mSocket.on("get_stop", onStop);
        mSocket.on("get_right", onTurnRight);
        mSocket.on("get_backward", onBackwards);
        mSocket.on("get_rcMode", onRcMode);
        mSocket.on("get_mode", onMode);
        mSocket.on("get_leds", onLeds);
        mSocket.on("get_angVel", onAngVel);
        mSocket.on("get_time", onTime);
        mSocket.on("get_angle", onAngle);
        mSocket.on("get_pan", onPan);
        mSocket.on("get_tilt", onTilt);
        mSocket.on("get_period", onPeriod);
    }

    /**
     * Release events receiver.
     */
    public void releaseEvents() {
        mSocket.off("get_reset", onReset);
        mSocket.off("get_forward", onForwards);
        mSocket.off("get_left", onTurnLeft);
        mSocket.off("get_stop", onStop);
        mSocket.off("get_right", onTurnRight);
        mSocket.off("get_backward", onBackwards);
        mSocket.off("get_rcMode", onRcMode);
        mSocket.off("get_mode", onMode);
        mSocket.off("get_leds", onLeds);
        mSocket.off("get_angVel", onAngVel);
        mSocket.off("get_time", onTime);
        mSocket.off("get_angle", onAngle);
        mSocket.off("get_pan", onPan);
        mSocket.off("get_tilt", onTilt);
        mSocket.off("get_period", onPeriod);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseEvents();
    }

    /**
     * Pan & Tilt reset.
     */
    public Emitter.Listener onReset = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onReset] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnReset.performClick();
                    }
                }
            });
        }
    };

    /**
     * Move forward.
     */
    public Emitter.Listener onForwards = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onForwards] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnForwards.performClick();
                    }
                }
            });
        }
    };

    /**
     * Turn left.
     */
    public Emitter.Listener onTurnLeft = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onTurnLeft] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnTurnLeft.performClick();
                    }
                }
            });
        }
    };

    /**
     * Stop all movement.
     */
    public Emitter.Listener onStop = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onStop] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnStop.performClick();
                    }
                }
            });
        }
    };

    /**
     * Turn right.
     */
    public Emitter.Listener onTurnRight = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onTurnRight] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnTurnRight.performClick();
                    }
                }
            });
        }
    };

    /**
     * Move backward.
     */
    public Emitter.Listener onBackwards = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onBackwards] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        btnBackwards.performClick();
                    }
                }
            });
        }
    };

    /**
     * Control mode.
     */
    public Emitter.Listener onRcMode = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onRcMode] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        tglRCMode.setChecked((Boolean) data.opt("IS_CHECKED"));
                    }
                }
            });
        }
    };

    /**
     * Secure & Un-secure movement mode.
     */
    public Emitter.Listener onMode = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onMode] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        tglMode.setChecked((Boolean) data.opt("IS_CHECKED"));
                    }
                }
            });
        }
    };

    /**
     * IR led toggle mode.
     */
    public Emitter.Listener onLeds = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onLeds] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        tglLeds.setChecked((Boolean) data.opt("IS_CHECKED"));
                    }
                }
            });
        }
    };

    /**
     * The angular velocity bar has been changed, update the value in the label.
     */
    public Emitter.Listener onAngVel = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onAngVel] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarAngVel.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };

    /**
     * The time bar has been changed, set the mode to USE TIME and update the value in the label.
     */
    public Emitter.Listener onTime = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onTime] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarTime.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };

    /**
     * the angle bar has been changed, set the mode to USE ANGLE and update the value in the label.
     */
    public Emitter.Listener onAngle = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onAngle] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarAngle.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };

    /**
     * Pan management.
     */
    public Emitter.Listener onPan = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onPan] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarPan.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };

    /**
     * Tilt management.
     */
    public Emitter.Listener onTilt = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onTilt] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarTilt.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };

    /**
     * Status period.
     */
    public Emitter.Listener onPeriod = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOG_TAG, "[onPeriod] " + data);
                    if (data.opt("USER").equals(myUser) && data.opt("ROBOT").equals(myRobot)) {
                        String progress = (String) data.opt("PROGRESS");
                        skBarPeriod.setProgress(Integer.parseInt(progress));
                    }
                }
            });
        }
    };
}
