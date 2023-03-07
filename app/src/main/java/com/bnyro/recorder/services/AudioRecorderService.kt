package com.bnyro.recorder.services

import android.media.MediaRecorder
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.AudioChannels
import com.bnyro.recorder.enums.AudioDeviceSource
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.StorageHelper

class AudioRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.recording_audio)

    override fun start() {
        val audioFormat = AudioFormat.getCurrent()

        recorder = PlayerHelper.newRecorder(this).apply {
            Preferences.prefs.getInt(Preferences.audioDeviceSourceKey, AudioDeviceSource.DEFAULT.value).let {
                    setAudioSource(it)
            }

            Preferences.prefs.getInt(Preferences.audioSampleRateKey, -1).takeIf { it > 0 }?.let {
                setAudioSamplingRate(it)
                setAudioEncodingBitRate(it * 32 * 2)
            }

            Preferences.prefs.getInt(Preferences.audioBitrateKey, -1).takeIf { it > 0 }?.let {
                setAudioEncodingBitRate(it)
            }
            Preferences.prefs.getInt(Preferences.audioChannelsKey, AudioChannels.MONO.value).takeIf { it > AudioChannels.MONO.value }?.let {
                    setAudioChannels(it)
            }

            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)

            outputFile = StorageHelper.getOutputFile(
                this@AudioRecorderService,
                audioFormat.extension
            )
            fileDescriptor = contentResolver.openFileDescriptor(outputFile!!.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()
        }

        super.start()
    }
}
