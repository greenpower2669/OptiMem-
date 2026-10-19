package fr.fab.optimem;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** User-owned local diagnostic journal, bounded and never uploaded. */
public final class LocalJournal {
    private final File file;
    public LocalJournal(Context ctx) { this.file = new File(ctx.getFilesDir(), "optimem-journal.txt"); }
    public synchronized void append(String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.FRANCE).format(new Date());
        String line = time + " | " + message.replace('\n', ' ') + "\n";
        try {
            if (file.exists() && file.length() > 131072) {
                String old = read();
                String tail = old.substring(Math.max(0, old.length() - 65536));
                try (FileOutputStream out = new FileOutputStream(file, false)) {
                    out.write(("[ARCHIVE LOCALE LIMITÉE : lignes anciennes retirées]\n" + tail).getBytes(StandardCharsets.UTF_8));
                }
            }
            try (FileOutputStream out = new FileOutputStream(file, true)) {
                out.write(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            // Deliberately never send diagnostics to a remote service.
        }
    }
    public synchronized String read() throws IOException {
        if (!file.exists()) return "Aucune entrée de journal.";
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
