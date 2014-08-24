package de.damdi.hiittimer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.media.MediaPlayer;
import android.widget.ToggleButton;
import android.view.WindowManager;
import android.view.MenuItem;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.io.Console;
import java.lang.String;
import java.util.Formatter;

import de.damdi.hiittimer.R;
import android.support.v7.app.ActionBarActivity;

public class TimerActivity extends ActionBarActivity {

    public class HIITCountDownTimer extends CountDownTimer {
        public HIITCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onTick(long l) {
            int minutes = ((int)l/MILLIS_IN_SEC) / SECS_IN_MIN;
            int seconds = ((int)l/MILLIS_IN_SEC) - (minutes * SECS_IN_MIN);

            savedTime = (int)l/MILLIS_IN_SEC;

            updateDisplayedTimer(minutes, seconds);
        }

        @Override
        public void onFinish() {
            if(isWorkoutOver()) {
                endWorkout();
            }
            else {
                nextPeriod();
            }
        }
    }

    private EditText timerDisplay;
    private EditText curRoundText;
    private Switch timerSwitch;
    private View mainView;
    private ToggleButton bellButton;

    private HIITCountDownTimer mainTimer;
    private int savedTime;

    private int curRound = 0;
    private int workRoundLen;
    private int restRoundLen;
    private int delayLen;
    private int totalRounds;
    private boolean workPeriod = true;
    private boolean soundEnabled = true;

    private Context con;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener mediaPrepListener;
    private PowerManager powerManager;

    private PowerManager.WakeLock wakeLock;
    private SharedPreferences sharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private final int MILLIS_IN_SEC = 1000;
    private final int SECS_IN_MIN = 60;
    private final int ON_TICK_INTERVAL_SEC = 1;
    private final String DEFAULT_WORK_ROUND_LEN = "00:30";
    private final String DEFAULT_REST_ROUND_LEN = "00:00";
    private final String DEFAULT_DELAY_LEN = "00:05";
    private final int DEFAULT_TOTAL_ROUNDS = 12;
    private final String RES_PREFIX = "android.resource://de.damdi.hiittimer/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        timerDisplay = (EditText)findViewById(R.id.main_timer);
        curRoundText = (EditText)findViewById(R.id.round_info);
        timerSwitch = (Switch)findViewById(R.id.timer_switch);
        mainView = findViewById(R.id.timer_view);
        bellButton = (ToggleButton)findViewById(R.id.bell_button);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("work_time")) {
                    workRoundLen = TimePreference.getTime(sharedPreferences.getString(key, DEFAULT_WORK_ROUND_LEN));
                }
                else if(key.equals("rest_time")) {
                    restRoundLen = TimePreference.getTime(sharedPreferences.getString(key, DEFAULT_REST_ROUND_LEN));
                }
                else if(key.equals("num_rounds")) {
                    totalRounds = Integer.getInteger(sharedPreferences.getString(key, ""), DEFAULT_TOTAL_ROUNDS);
                }

                resetWorkout();
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(prefListener);

        workRoundLen = TimePreference.getTime(sharedPref.getString("work_time", DEFAULT_WORK_ROUND_LEN));
        restRoundLen = TimePreference.getTime(sharedPref.getString("rest_time", DEFAULT_REST_ROUND_LEN));
        delayLen = TimePreference.getTime(sharedPref.getString("delay_time", DEFAULT_DELAY_LEN));
        totalRounds = Integer.getInteger(sharedPref.getString("num_rounds", ""), DEFAULT_TOTAL_ROUNDS);

        resetWorkout();

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "HIIT Screen Lock");
/*
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mediaPlayer = null;
            }
        });
*/
    }

    @Override
    public void onStart() {
        super.onStart();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    public void onStop() {
        super.onStop();

        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timer, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//       int itemId = item.getItemId();
//		if (itemId == R.id.timely) {
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			//	intent.setClassName("ch.bitspin.timely", "com.android.settings.Settings"); 
//			startActivity(getPackageManager().getLaunchIntentForPackage("ch.bitspin.timely"));
//			startActivity(intent);
 //        //     break;
 //        //default:
 //        //    break;
//		}
//
//        return true;
//    } 

    public void toggleTimerClick(View view) {
        if(((Switch)view).isChecked()) {
            mainTimer = new HIITCountDownTimer(savedTime * MILLIS_IN_SEC, ON_TICK_INTERVAL_SEC * MILLIS_IN_SEC);
            mainTimer.start();
            if(curRound == 0) {
                curRound = 1;
                updateDisplayedRoundInfo();
            }
            if(workPeriod) {
                playRawAudio(R.raw.startround);
                mainView.setBackgroundColor(Color.GREEN);
            }
            else {
                playRawAudio(R.raw.endround);
                mainView.setBackgroundColor(Color.RED);
            }
        }
        else {
            mainTimer.cancel();
            mainView.setBackgroundColor(Color.WHITE);
        }
    }

    public void resetButtonClick(View view) {
        if(timerSwitch.isChecked()) {
            timerSwitch.toggle();
        }

        resetWorkout();
    }

    public void screenCheckClick(View view) {
        if(((CheckBox)view).isChecked()) {
            wakeLock.acquire();
        }
        else {
            wakeLock.release();
        }
    }

    public void updateDisplayedTimer(int minutes, int seconds) {
        timerDisplay.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
    }

    public void updateDisplayedTimer(int seconds) {
        int minutes = seconds / SECS_IN_MIN;
        seconds = (seconds - (minutes * SECS_IN_MIN));
        timerDisplay.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
    }

    public void updateDisplayedRoundInfo() {
//      curRoundText.setText(R.string.cur_round_start_txt + Integer.toString(curRound) + R.string.cur_round_start_del + Integer.toString(totalRounds));
        String roundInfo = "Round: " + Integer.toString(curRound) + " / " + Integer.toString(totalRounds);
        curRoundText.setText(roundInfo);
    }

    public void nextPeriod() {
        if(workPeriod) {
            nextRestPeriod();
        }
        else {
            curRound++;
            updateDisplayedRoundInfo();
            nextWorkPeriod();
        }
    }

    public void nextWorkPeriod() {
        workPeriod = true;
        if(bellButton.isChecked()) {
            playRawAudio(R.raw.startround);
        }
        mainView.setBackgroundColor(Color.GREEN);

        mainTimer = new HIITCountDownTimer(workRoundLen * MILLIS_IN_SEC, ON_TICK_INTERVAL_SEC * MILLIS_IN_SEC);
        mainTimer.start();
    }

    public void nextRestPeriod() {
        workPeriod = false;
        if(bellButton.isChecked()) {
            playRawAudio(R.raw.endround);
        }
        mainView.setBackgroundColor(Color.RED);

        mainTimer = new HIITCountDownTimer(restRoundLen * MILLIS_IN_SEC , ON_TICK_INTERVAL_SEC * MILLIS_IN_SEC);
        mainTimer.start();
    }

    public boolean isWorkoutOver() {
        return (curRound == totalRounds);
    }

    public void endWorkout() {
        if(bellButton.isChecked()) {
            playRawAudio(R.raw.endworkout);
        }
        resetWorkout();
    }

    public void resetWorkout() {

        if(mainTimer != null) {
            mainTimer.cancel();
        }

        savedTime = workRoundLen;
        curRound = 0;

        mainView.setBackgroundColor(Color.WHITE);
        updateDisplayedTimer(savedTime);
        updateDisplayedRoundInfo();

    }

    public void playRawAudio(int audioId) {
        try {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();

            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(RES_PREFIX + audioId));
            mediaPlayer.prepareAsync();
        }
        catch (Exception e) {
            if(e.getMessage() != null) {
                Log.e("Error playing audio: ",e.getMessage());
            }
        }
    }
}