package fr.fab.optimem;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import java.util.Locale;

/** Native French TTS. No network permission, no services in the background. */
public final class SpeechGuide {
    private final Context context;
    private final SharedPreferences prefs;
    private TextToSpeech tts;
    private boolean ready;
    private boolean released;
    private String pending;
    private boolean pendingManual;

    public SpeechGuide(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("optimem_speech", Context.MODE_PRIVATE);
        try {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (released) return;
                if (status == TextToSpeech.SUCCESS && tts != null) {
                    int language = tts.setLanguage(Locale.FRANCE);
                    ready = language != TextToSpeech.LANG_MISSING_DATA
                            && language != TextToSpeech.LANG_NOT_SUPPORTED;
                    if (ready) tts.setSpeechRate(0.87f);
                }
                if (ready && pending != null) {
                    String words = pending;
                    pending = null;
                    say(words, pendingManual);
                } else if (!ready && pendingManual) missingVoice();
            });
        } catch (RuntimeException e) {
            ready = false;
        }
    }
    public boolean automaticEnabled() {
        return prefs.getBoolean("auto_voice", true);
    }
    public void setAutomaticEnabled(boolean value) {
        prefs.edit().putBoolean("auto_voice", value).apply();
        if (!value) stop();
    }
    private boolean screenReaderActive() {
        AccessibilityManager manager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return manager != null && manager.isEnabled() && manager.isTouchExplorationEnabled();
    }
    public void onPageOpened(String words) {
        speakIfAutomatic(words);
    }
    public void speakIfAutomatic(String words) {
        if (automaticEnabled() && !screenReaderActive()) say(words, false);
    }
    public void speak(String words) {
        say(words, true);
    }
    private void say(String words, boolean manual) {
        if (released || words == null || words.trim().isEmpty()) return;
        if (tts == null || !ready) {
            pending = words;
            pendingManual = manual;
            return;
        }
        try {
            int status = tts.speak(words, TextToSpeech.QUEUE_FLUSH, null, "optimem_tts");
            if (status == TextToSpeech.ERROR && manual) missingVoice();
        } catch (RuntimeException e) {
            if (manual) missingVoice();
        }
    }
    private void missingVoice() {
        pending = null;
        pendingManual = false;
        Toast.makeText(context,
                "Voix française indisponible. Vérifie Paramètres Android > Accessibilité > Synthèse vocale.",
                Toast.LENGTH_LONG).show();
    }
    public void stop() {
        pending = null;
        pendingManual = false;
        try { if (tts != null) tts.stop(); } catch (RuntimeException ignored) { }
    }
    public void shutdown() {
        released = true;
        stop();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }
}
