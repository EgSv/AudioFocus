package ru.startandroid.develop.audiofocus

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import java.io.IOException

const val LOG_TAG = "myLogs"

class MainActivity : AppCompatActivity(), OnCompletionListener {

    private var audioManager: AudioManager? = null
    private var afListenerMusic: AFListener? = null
    private var afListenerSound: AFListener? = null

    private var mpMusic: MediaPlayer? = null
    private var mpSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun onClickMusic(v: View?) {
        mpMusic = MediaPlayer()
        try {
            mpMusic!!.setDataSource("mnt/sdcard/Music/music.mp3")
            mpMusic!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mpMusic!!.setOnCompletionListener(this)
        afListenerMusic = AFListener(mpMusic!!, "Music")
        val requestResult: Int = audioManager!!.requestAudioFocus(afListenerMusic,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
        Log.d(LOG_TAG, "Music request focus, result: $requestResult")
        mpMusic!!.start()
    }

    fun onClickSound(v: View) {
        var duration: Int = AudioManager.AUDIOFOCUS_GAIN
        when(v.id) {
            R.id.btnPlaySoundG -> duration = AudioManager.AUDIOFOCUS_GAIN
            R.id.btnPlaySoundGT -> duration = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            R.id.btnPlaySoundGTD -> duration = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        }

        mpSound = MediaPlayer.create(this, R.raw.explosion)
        mpSound!!.setOnCompletionListener(this)

        afListenerSound = AFListener(mpSound!!, "Sound")
        val requestResult: Int = audioManager!!.requestAudioFocus(afListenerSound, AudioManager.STREAM_MUSIC, duration)
        Log.d(LOG_TAG, "Sound request focus, result: $requestResult")
        mpSound!!.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp == mpMusic) {
            Log.d(LOG_TAG, "Music: abandon focus")
            audioManager!!.abandonAudioFocus(afListenerMusic)
        } else if (mp == mpSound) {
            Log.d(LOG_TAG, "Sound: abandon focus")
            audioManager!!.abandonAudioFocus(afListenerSound)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mpMusic != null) {
            mpMusic!!.release()
        }
        if (mpSound != null) {
            mpSound!!.release()
        }
        if (afListenerMusic != null) {
            audioManager!!.abandonAudioFocus(afListenerMusic)
        }
        if (afListenerSound != null) {
            audioManager!!.abandonAudioFocus(afListenerSound)
        }
    }

    internal inner class AFListener(mp: MediaPlayer, label: String): OnAudioFocusChangeListener {
        private var label = ""
        private var mp: MediaPlayer

        init {
            this.label = label
            this.mp = mp
        }

        override fun onAudioFocusChange(focusChange: Int) {
            var event: String = ""
            when(focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    event = "AUDIOFOCUS_LOSS"
                    mp.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    event = "AUDIOFOCUS_LOSS_TRANSIENT"
                    mp.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    event = "AUDIO_LOSS_TRANSIENT_CAN_DUCK"
                    mp.setVolume(0.5F, 0.5F)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    event = "AUDIOFOCUS_GAIN"
                    if (!mp.isPlaying) {
                        mp.start()
                        mp.setVolume(1.0f, 1.0f)
                    }
                }
            }
            Log.d(LOG_TAG, "$label onAudioFocusChange: $event")
        }
    }
}