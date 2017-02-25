package com.example.mathalarm.Alarms.MathAlarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mathalarm.R;
import java.io.IOException;

public class MathAlarmService extends Service {
    private static final int ALARM_TIMEOUT_MILLISECONDS = 5 * 60 * 1000;

    private int alarmComplexityLevel,selectedDeepSleepMusic;
    private String alarmMessageText;

    private String musicResourceID;
    private MediaPlayer mediaPlayer;
    private int initialCallState;
    private TelephonyManager telephonyManager;

    private static final int NOTIFICATION_ID = 2;
    private AlarmNotifications alarmNotifications = new AlarmNotifications();

    private static final int KILLER_HANDLE_SERVICE_SILENT = 1;
    private static final int KILLER_HANDLE_DEEP_SLEEP_MUSIC = 2;

    private boolean handlerMessageSent;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // snooze alarm if alarm played for 5 minutes without answer
            if (msg.what == KILLER_HANDLE_SERVICE_SILENT) {
                Log.i(MainMathAlarm.TAG, "handleMessage, silentKiller msg");
                //start receiver with action Snooze
                Intent snoozeIntent = new Intent(MainMathAlarm.ALARM_SNOOZE_ACTION);
                sendBroadcast(snoozeIntent);
                stopSelf();
            }
            //stop playing deepSleepMusic and start playing alarmMusic
            else if(msg.what == KILLER_HANDLE_DEEP_SLEEP_MUSIC) {
                Log.i(MainMathAlarm.TAG, "handleMessage deepSleep msg");
                //stop playing deepSleepMusic
                AlarmStopPlayingMusic();
                EnableServiceHandlerKiller(KILLER_HANDLE_SERVICE_SILENT);
                //start playing alarmMusic
                AlarmStartPlayingMusic("alarmMusic",musicResourceID);
                handlerMessageSent=false;
            }
        }
    };
    //Phone state listener for situation where alarm triggered in time of call
    // so we will save and snooze our alarm
    private PhoneStateListener phoneStateListener = new PhoneStateListener()
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if(state != TelephonyManager.CALL_STATE_IDLE && state !=initialCallState) {
                Log.i(MainMathAlarm.TAG, "phoneStateListener true (snooze alarm)");
                //start receiver with action Snooze
                Intent snoozeIntent = new Intent(MainMathAlarm.ALARM_SNOOZE_ACTION);
                startActivity(snoozeIntent);
                stopSelf();
            }
        }
    };
    @Override
    public void onCreate() {
        Log.i(MainMathAlarm.TAG, "MathAlarmService " + " onCreate");
        super.onCreate();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Registers a listener object to receive notification of changes in specified telephony states.
        // @PhoneStateListener.LISTEN_CALL_STATE The telephony state(s) of interest to the listener
        telephonyManager.listen(
                phoneStateListener,
                phoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(MainMathAlarm.TAG, "MathAlarmService " + " onStartCommand");
        alarmComplexityLevel = intent.getExtras().getInt("alarmComplexityLevel", 0);

        Boolean alarmCondition = intent.getExtras().getBoolean("alarmCondition", false);

        String defaultAlarmMessageText = "\"" + "Good morning sir." + "\"";
        alarmMessageText = intent.getExtras().getString("alarmMessageText", defaultAlarmMessageText);

        int selectedMusic = intent.getExtras().getInt("selectedMusic", 0);
        String[] musicList = getResources().getStringArray(R.array.music_list);
        musicResourceID = getPackageName() + "/raw/" + musicList[selectedMusic];
         selectedDeepSleepMusic = intent.getIntExtra("selectedDeepSleepMusic",0);

        if (alarmCondition) {
            Start_DisplayAlarmActivity();
            //selectedDeepSleepMusic ==1 (ON)
            if(selectedDeepSleepMusic==1) {
                EnableServiceHandlerKiller(KILLER_HANDLE_DEEP_SLEEP_MUSIC);
                AlarmStartPlayingMusic("deepSleepMusic",musicResourceID);
                handlerMessageSent = true;
            }
            //selectedDeepSleepMusic ==0 (OFF)
            else if(selectedDeepSleepMusic==0) {
                EnableServiceHandlerKiller(KILLER_HANDLE_SERVICE_SILENT);
                AlarmStartPlayingMusic("alarmMusic",musicResourceID);
                handlerMessageSent = false;
            }
        }//if condition false
        else {
            Toast.makeText(this,"alarmCondition" +" false",Toast.LENGTH_LONG).show();
        }
        startForeground(NOTIFICATION_ID,alarmNotifications.NewNotification(this));
        initialCallState = telephonyManager.getCallState();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MainMathAlarm.TAG, "MathAlarmService " + " onDestroy");
            //stop listen for incoming calls
            //To unregister a listener, pass the listener object and set the events argument to LISTEN_NONE (0).
            telephonyManager.listen(phoneStateListener,0);

        if(handlerMessageSent)
        DisableServiceHandlerKiller(KILLER_HANDLE_DEEP_SLEEP_MUSIC);
        else
            DisableServiceHandlerKiller(KILLER_HANDLE_SERVICE_SILENT);

        AlarmStopPlayingMusic();
        alarmNotifications.CancelNotification(this,NOTIFICATION_ID);
    }

    /**
     * @param alarmMusicType (deepSleepMusic) (alarmMusic)
     */
    private void AlarmStartPlayingMusic(String alarmMusicType, String musicResourceID) {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (alarmMusicType){
            case "alarmMusic":
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //FLAG_SHOW_UI Show a toast containing the current volume.
                if (!audioManager.isVolumeFixed())
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_SHOW_UI);
            } else
                /*other method to increase volume for API < LOLLIPOP 21*/
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_SHOW_UI);
                break;

            case "deepSleepMusic":
                musicResourceID = getPackageName() + "/raw/" + R.raw.free;
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)/2, AudioManager.FLAG_SHOW_UI);
                break;
        }


        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.i(MainMathAlarm.TAG,"MathAlarmService Error occurred while playing audio.");
                    mp.stop();
                    mp.release();
                    mediaPlayer = null;
                    return true;
                }});
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            //setDataSource must be placed in try/catch
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + musicResourceID));
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.i(MainMathAlarm.TAG, "MathAlarmService " + " AlarmStartPlayingMusic isPlaying() =" + musicResourceID);

            Log.i(MainMathAlarm.TAG, "MathAlarmService " + " AlarmStartPlayingMusic isPlaying() =" + mediaPlayer.isPlaying());
        } catch (IOException e) {
            Log.i(MainMathAlarm.TAG, "MathAlarmService " + " AlarmStartPlayingMusic error" + e.getMessage());
        }
    }

    private void AlarmStopPlayingMusic() {
        if (mediaPlayer.isPlaying()) {
                try {
                    mediaPlayer.stop();
                    Log.i(MainMathAlarm.TAG, "MathAlarmService " + "AlarmStopPlayingMusic " + "isPlaying=" + mediaPlayer.isPlaying());
                    mediaPlayer.release();
                    mediaPlayer = null;
                } catch (Exception e) {
                    Log.i(MainMathAlarm.TAG, "MathAlarmService " + "AlarmStopPlayingMusic error " + e.getMessage());
                }
        } else
            Log.i(MainMathAlarm.TAG, "MathAlarmService " + "AlarmStopPlayingMusic isPlaying()=" + mediaPlayer.isPlaying());
    }

    private void Start_DisplayAlarmActivity() {
        Intent dialogIntent = new Intent(this, DisplayAlarmActivity.class);
        dialogIntent.putExtra("AlarmCreatedKey", true)
                .putExtra("alarmMessageText", alarmMessageText)
                .putExtra("alarmComplexityLevel", alarmComplexityLevel)
                //If set, this activity will become the start of a new task on this history stack.
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    private void EnableServiceHandlerKiller(int type) {
        switch (type){
            case KILLER_HANDLE_SERVICE_SILENT:
                handler.sendMessageDelayed(handler.obtainMessage(KILLER_HANDLE_SERVICE_SILENT),
                        ALARM_TIMEOUT_MILLISECONDS);
                Log.i(MainMathAlarm.TAG, "MathAlarmService " +" EnableServiceHandlerKiller"+ " KILLER_HANDLE_SERVICE_SILENT");
                break;
            case KILLER_HANDLE_DEEP_SLEEP_MUSIC:
                handler.sendMessageDelayed(handler.obtainMessage(KILLER_HANDLE_DEEP_SLEEP_MUSIC),
                        ALARM_TIMEOUT_MILLISECONDS);
                Log.i(MainMathAlarm.TAG, "MathAlarmService "+" EnableServiceHandlerKiller"+ " KILLER_HANDLE_DEEP_SLEEP_MUSIC");
                break;
        }
    }

    private void DisableServiceHandlerKiller(int type) {
        switch (type) {
            case KILLER_HANDLE_SERVICE_SILENT:
                handler.removeMessages(KILLER_HANDLE_SERVICE_SILENT);
                Log.i(MainMathAlarm.TAG, "MathAlarmService " + " DisableServiceHandlerKiller " + "KILLER_HANDLE_SERVICE_SILENT");
                break;
            case KILLER_HANDLE_DEEP_SLEEP_MUSIC:
                handler.removeMessages(KILLER_HANDLE_DEEP_SLEEP_MUSIC);
                Log.i(MainMathAlarm.TAG, "MathAlarmService " + " DisableServiceHandlerKiller " + "KILLER_HANDLE_DEEP_SLEEP_MUSIC");
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}