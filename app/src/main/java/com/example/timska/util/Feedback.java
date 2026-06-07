package com.example.timska.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/** Small haptic + audio cues confirming a tap to the user. */
public final class Feedback {

    private Feedback() {
    }

    public static void success(Context context) {
        vibrate(context, 120);
        beep(ToneGenerator.TONE_PROP_BEEP);
    }

    public static void error(Context context) {
        vibrate(context, 300);
        beep(ToneGenerator.TONE_PROP_NACK);
    }

    private static void vibrate(Context context, long ms) {
        Vibrator vibrator = getVibrator(context);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private static Vibrator getVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager =
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager == null ? null : manager.getDefaultVibrator();
        }
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private static void beep(int toneType) {
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
            tone.startTone(toneType, 200);
            new Handler(Looper.getMainLooper()).postDelayed(tone::release, 300);
        } catch (RuntimeException ignored) {
            // ToneGenerator can throw if the audio resource is unavailable; ignore.
        }
    }
}
