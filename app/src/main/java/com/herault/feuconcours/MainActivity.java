package com.herault.feuconcours;

import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // Maximun sound stream.
    private static final int MAX_STREAMS = 5;
     SoundPool soundPool;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private float volume;

    private int soundIdKlaxon1;
    private int soundIdKlaxon2;
    private int soundIdKlaxon3;

    int[] colorFeu = new int [] { Color.RED,Color.RED, Color.GREEN, Color.YELLOW , Color.RED,Color.GREEN, Color.YELLOW , Color.RED };
    String[] sequenceAB = new String[] {"AB","AB", "AB", "AB" , "CD","CD","CD","CD"};
    String [] sequenceCD = new String[] {"CD","CD","CD","CD","AB", "AB", "AB" ,"AB" };
    long [] waitingTime   = new long[]  {0,10, 90, 30 , 10, 90, 30 , 0};
    int [] offsetTime  = {0,0,30,0,0,30,0,0};
    int []  klaxon = new int[]  {0,2, 1, 0 , 2,1,0,3};

    private boolean live = false ; // Timer status
    long targetTime = 0 ;
    int step =0 ;                 // index of step
    boolean sequence =false ;
    boolean klaxonOn ;

    Handler timerHandler = new Handler(Looper.getMainLooper());

    long startTime = 0;
    TextView timerTextView ;
    TextView serieTextView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timerTextView);
        serieTextView = findViewById(R.id.serialTextView);
        timerTextView.setOnTouchListener(startStop);

        // sound gestion

        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volume Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volume (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);



            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
        });
        // Load sound file (Klaxonn.mp3) into SoundPool.
        this.soundIdKlaxon1 = this.soundPool.load(this, R.raw.klaxonn1,1);
        this.soundIdKlaxon2 = this.soundPool.load(this, R.raw.klaxonn2,1);
        this.soundIdKlaxon3 = this.soundPool.load(this, R.raw.klaxonn3,1);

    }

    private final View.OnTouchListener startStop = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (!live) {
                    step = 0;
                    live = true;
                    startTime =  System.currentTimeMillis();
                    klaxonOn = true ;
                    v.setKeepScreenOn(true);
                    timerHandler.postDelayed(timerRunnable, 1000);
                } else {
                    step = 0;
                    live = false;
                    klaxonOn = false ;
                    sequence = !sequence;
                    v.setKeepScreenOn(false);
                    timerHandler.removeCallbacks(timerRunnable);
                }
            }

            return true;
        }

    };



 // Timer
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if ( live ) {

                if ((step+1) == sequenceAB.length) {
                    live = false;
                    timerHandler.removeCallbacks(timerRunnable);
                    timerTextView.setKeepScreenOn(false);
                    targetTime = 0;
                    sequence = !sequence;
                }
                else
                {
                    targetTime = waitingTime[step]*1000;  // target Time in millisecond
                    long millis = System.currentTimeMillis() - startTime;
                    targetTime -=  millis;                            // temps restant
                    if (targetTime <= 0) {                                  // Init new step

                        startTime = System.currentTimeMillis();
 //                       millis = System.currentTimeMillis() - startTime;
                        step += 1;
                        klaxonOn = true;
                        targetTime = waitingTime[step]*1000;
                    }
                    int seconds = (int) (targetTime / 1000 + ((targetTime % 1000 ==0) ? 0 : 1));
                    seconds += offsetTime [step];
                    int minutes = seconds / 60;
                    int timeSeconds= seconds ;
                    seconds = seconds % 60;


         //           timerTextView.setText(String.format("%d:%02d", minutes, seconds));
                    timerTextView.setText(String.format("%d", timeSeconds));
                    timerTextView.setBackgroundColor(colorFeu[step]);
                    serieTextView.setBackgroundColor(colorFeu[step]);
                    if (colorFeu[step]==  Color.YELLOW)
                    {
                        timerTextView.setTextColor(Color.BLACK);
                        serieTextView.setTextColor(Color.BLACK);
                    }
                    else
                    {
                        timerTextView.setTextColor(Color.WHITE);
                        serieTextView.setTextColor(Color.WHITE);
                    }

                    if(sequence)
                        serieTextView.setText(sequenceCD[step]);
                    else
                        serieTextView.setText(sequenceAB[step]);
                    if(klaxonOn) {
                        switch (klaxon[step]) {
                            case 0:
                                break;
                            case 1:
                                soundPool.play(soundIdKlaxon1, volume, volume, 1, 0, 1f);
                                break;
                            case 2:
                                 soundPool.play(soundIdKlaxon2, volume, volume, 1, 0, 1f);
                                break;
                            case 3:
                                  soundPool.play(soundIdKlaxon3, volume, volume, 1, 0, 1f);
                                break;

                        }
                        klaxonOn = false;
                    }
                    timerHandler.postDelayed(this, 1000);

                }
            }
            else {
                timerTextView.setText("00");


                targetTime = 0 ;
            }
        }
    };

  @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);

    }
    }



