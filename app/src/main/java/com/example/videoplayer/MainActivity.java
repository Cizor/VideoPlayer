package com.example.videoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView maxTime;
    private TextView currPosition;
    private Button pauseButton;
    private Button startButton;

    private SeekBar seekBar;
    private SeekBar volumeSeekBar;
    private MediaPlayer mediaPlayer;
    private Handler threadHandler = new Handler();
    private int duration;

    private AudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button forwardButton;
        Button rewindButton;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        this.maxTime = findViewById(R.id.textView2);
        this.currPosition = findViewById(R.id.textView3);
        this.pauseButton = findViewById(R.id.button3);
        this.startButton = findViewById(R.id.button2);
        forwardButton = findViewById(R.id.button4);
        rewindButton = findViewById(R.id.button);
        this.pauseButton.setEnabled(false);
        this.volumeSeekBar = findViewById(R.id.volumeSeekBar);
        this.volumeSeekBar.setClickable(false);
        this.volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        this.seekBar = findViewById(R.id.seekBar);
        this.seekBar.setClickable(false);

        int songId = this.getResources().getIdentifier("perfect", "raw", this.getPackageName());
        Uri uri = Uri.parse("https://ia802508.us.archive.org/5/items/testmp3testfile/mpthreetest.mp3");
        this.mediaPlayer = MediaPlayer.create(this, songId);
        this.duration = this.mediaPlayer.getDuration();
        this.seekBar.setMax(duration);
        forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int LONG_TIME = 10000;
                if (currentPosition+LONG_TIME < duration){
                    mediaPlayer.seekTo(currentPosition+LONG_TIME);
                } else {
                    mediaPlayer.seekTo(duration);
                }
                return true;
            }
        });

        rewindButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int LONG_TIME = 10000;
                if (currentPosition - LONG_TIME > 0){
                    mediaPlayer.seekTo(currentPosition - LONG_TIME);
                } else {
                    mediaPlayer.seekTo(0);
                }
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                if (seekBar.getProgress() == seekBar.getMax()){
                    mediaPlayer.seekTo(0);
                }
            }
        });
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
                mediaPlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private String msToString(int millisecs){
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long)millisecs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long)millisecs);
        return minutes+":"+seconds;
    }

    public void doStart(View v){
        int currentPosition = this.mediaPlayer.getCurrentPosition();
        if (currentPosition == 0){
            String maxTimeString = this.msToString(this.duration);
            this.maxTime.setText(maxTimeString);
        } else if(currentPosition == duration){
            this.mediaPlayer.reset();
        }
        this.mediaPlayer.start();
        UpdateSeekBarThread updateSeekBar = new UpdateSeekBarThread();
        threadHandler.postDelayed(updateSeekBar, 50);
        this.pauseButton.setEnabled(true);
        this.startButton.setEnabled(true);
    }

    class UpdateSeekBarThread implements Runnable{
        @Override
        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            String currPosStr = msToString(currentPosition);
            currPosition.setText(currPosStr);
            seekBar.setProgress(currentPosition);
            threadHandler.postDelayed(this, 50);
        }
    }

    public void doPause(View v){
        this.mediaPlayer.pause();
        this.pauseButton.setEnabled(false);
        this.startButton.setEnabled(true);
    }

    public void doRewind(View v){
        int currentPosition = mediaPlayer.getCurrentPosition();
        int SUBTRACT_TIME = 5000;
        if (currentPosition - SUBTRACT_TIME > 0){
            this.mediaPlayer.seekTo(currentPosition - SUBTRACT_TIME);
        }else {
            mediaPlayer.seekTo(0);
        }
    }

    public void doFastForward(View v){
        int currentPosition = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();
        int ADD_TIME = 5000;
        if (currentPosition + ADD_TIME < duration){
            this.mediaPlayer.seekTo(currentPosition+ADD_TIME);
        } else {
            mediaPlayer.seekTo(duration);
        }
    }
}
