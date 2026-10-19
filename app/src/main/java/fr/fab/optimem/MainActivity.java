package fr.fab.optimem;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Manual, offline memory measurement aid. Does not terminate other processes. */
public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(11, 19, 36);
    private static final int WHITE = Color.rgb(246, 250, 254);
    private static final int MUTED = Color.rgb(197, 216, 229);
    private static final int TEAL = Color.rgb(113, 219, 183);
    private static final long GIB = 1024L * 1024 * 1024;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Long> history = new ArrayList<>();
    private TextView live, pressure, latest, comparison, ramPlusTest, appChosen;
    private MemoryChart chart;
    private ActivityManager activityManager;
    private LocalJournal journal;
    private android.content.SharedPreferences prefs;
    private boolean preparedThisSession = false;
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            sample();
            handler.postDelayed(this, 4000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        journal = new LocalJournal(this);
        prefs = getSharedPreferences("optimem", MODE_PRIVATE);
        drawLayout();
        journal.append("APP_START version=0.1.0 sdk=" + android.os.Build.VERSION.SDK_INT +
                " appareil=" + android.os.Build.MANUFACTURER + "/" + android.os.Build.MODEL);
    }
    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(poll);
        handler.post(poll);
    }
    @Override protected void onPause() {
        handler.removeCallbacks(poll);
        super.onPause();
    }
    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    private LinearLayout column() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }
    private TextView text(LinearLayout parent, String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setPadding(0, dp(9), 0, dp(9));
        parent.addView(view);
        return view;
    }
    private Button button(LinearLayout parent, String name, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setTextColor(BG);
        b.setBackgroundTintList(ColorStateList.valueOf(TEAL));
        b.setMinHeight(dp(58));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), 0, dp(6));
        parent.addView(b, params);
        b.setOnClickListener(click);
        return b;
    }
    private void heading(LinearLayout parent, String content) {
        text(parent, content, 23, WHITE).setContentDescription(content);
    }
    private void drawLayout() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout body = column();
        body.setPadding(dp(20), dp(12), dp(20), dp(36));
        scroll.addView(body);
        setContentView(scroll);

        text(body, "🧹 OptiMem", 31, TEAL);
        text(body, "Diagnostic honnête de mémoire • hors ligne • Android 16", 17, MUTED);
        heading(body, "État actuel de la mémoire");
        live = text(body, "Lecture en cours…", 22, WHITE);
        pressure = text(body, "Pression mémoire : calcul en cours", 18, MUTED);
        latest = text(body, "", 16, MUTED);
        chart = new MemoryChart(this);
        body.addView(chart, new LinearLayout.LayoutParams(-1, dp(175)));
        text(body, "Courbe des mesures disponibles pendant l'observation (une mesure toutes les 4 secondes, 8 minutes maximum). L'échelle s'adapte aux valeurs réellement observées.", 16, MUTED);

        heading(body, "Préparation IA");
        button(body, "🚀 Préparer le téléphone pour une IA lourde", v -> prepareAI());
        button(body, "Ouvrir les paramètres Samsung / maintenance", v -> openPhoneSettings());
        button(body, "Mesurer après les actions manuelles", v -> measureAfter());
        comparison = text(body, "Aucune comparaison enregistrée.", 19, WHITE);
        button(body, "Choisir une application IA à lancer", v -> chooseLaunchApp());
        appChosen = text(body, "Application choisie : aucune", 17, MUTED);
        updateChosen();
        button(body, "Lancer l'application choisie", v -> launchChosen());

        heading(body, "Applications récemment actives");
        text(body, "L'accès d'utilisation est facultatif. Une application récemment affichée n'est pas nécessairement encore en mémoire. RAM individuelle : non disponible.", 17, MUTED);
        button(body, "Voir l'activité récente et les paramètres", v -> showUsage());

        heading(body, "RAM physique / RAM Plus");
        text(body, "La RAM physique est la mémoire accessible au noyau. RAM Plus utilise une partie du stockage comme mémoire virtuelle : ce n'est pas de la RAM physique et cela n'augmente pas forcément la vitesse d'une grosse IA locale. La valeur RAM Plus ne peut pas être lue ni modifiée par OptiMem.", 17, WHITE);
        text(body, "Pour comparer : Paramètres → Maintenance de l'appareil → Mémoire → RAM Plus (intitulés variables selon One UI). Choisir manuellement 4 ou 8 Go, redémarrer si demandé, puis ouvrir OptiMem et enregistrer un essai. Éviter d'autres changements simultanés.", 17, MUTED);
        button(body, "Noter un essai avec RAM Plus réglée à 4 Go", v -> recordRamPlus(4));
        button(body, "Noter un essai avec RAM Plus réglée à 8 Go", v -> recordRamPlus(8));
        ramPlusTest = text(body, "", 16, MUTED);
        updateRamPlus();

        heading(body, "Nettoyage sûr et journal");
        button(body, "Nettoyer seulement le cache d'OptiMem", v -> cleanOwnCache());
        text(body, "Ne touche jamais aux fichiers, modèles IA, sauvegardes et caches des autres applications.", 17, MUTED);
        button(body, "Copier le journal de diagnostic", v -> copyJournal());
        button(body, "Actualiser la mesure", v -> sample());
        text(body, "Les mesures dépendent de l'instant où elles sont prises. Une hausse de RAM disponible ne prouve pas à elle seule qu'une action donnée en est la cause.", 16, MUTED);
        updateComparisonFromPreferences();
    }
    private ActivityManager.MemoryInfo snapshot() {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        return info;
    }
    private static String gb(long b) {
        return String.format(Locale.FRANCE, "%.2f Go", b / (double) GIB);
    }
    private static String moment(long ms) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,
                Locale.FRANCE).format(new Date(ms));
    }
    private static String state(ActivityManager.MemoryInfo m) {
        return "disponible=" + gb(m.availMem) + " totalAccessible=" + gb(m.totalMem)
                + " threshold=" + gb(m.threshold) + " lowMemory=" + m.lowMemory;
    }
    private void sample() {
        try {
            ActivityManager.MemoryInfo m = snapshot();
            long occupied = Math.max(0, m.totalMem - m.availMem);
            live.setText("Disponible : " + gb(m.availMem) +
                    "\nRAM totale accessible : " + gb(m.totalMem) +
                    "\nOccupée / non disponible (estimation) : " + gb(occupied));
            String indicator = m.lowMemory ? "Oui : pression mémoire signalée par Android"
                    : (m.availMem <= m.threshold ? "Proche du seuil Android" : "Pas d'alerte lowMemory Android");
            pressure.setText("Mémoire faible Android : " + (m.lowMemory ? "OUI" : "NON")
                    + "\nSeuil d'alerte : " + gb(m.threshold)
                    + "\nÉtat : " + indicator);
            latest.setText("Dernière mesure : " + moment(System.currentTimeMillis()) +
                    "\nLa quantité exactement récupérable et la RAM des autres processus sont indisponibles.");
            history.add(m.availMem);
            if (history.size() > 120) history.remove(0);
            chart.setSamples(history);
        } catch (RuntimeException e) {
            latest.setText("Lecture mémoire indisponible : " + e.getClass().getSimpleName());
            journal.append("ERREUR mesure " + e.getClass().getSimpleName());
        }
    }
    private void prepareAI() {
        try {
            ActivityManager.MemoryInfo m = snapshot();
            prefs.edit().putLong("before", m.availMem).putLong("beforeTotal", m.totalMem)
                    .putLong("beforeThreshold", m.threshold).putBoolean("beforeLow", m.lowMemory)
                    .putLong("beforeTime", System.currentTimeMillis()).apply();
            preparedThisSession = true;
            journal.append("PREPARATION_DEBUT mesure AVANT " + state(m));
            updateComparisonFromPreferences();
            String msg = "Mesure de référence enregistrée : " + gb(m.availMem) + " disponibles."
                    + "\n\n1. Sauvegarder votre travail dans les applications ouvertes."
                    + "\n2. Utiliser manuellement les réglages Samsung Maintenance de l'appareil, si utile."
                    + "\n3. Ne pas fermer une application de communication, d'accessibilité ou un service important."
                    + "\n4. Revenir ici et choisir « Mesurer après les actions manuelles »."
                    + "\n5. Choisir puis lancer FxExtractor ou une autre IA."
                    + "\n\nAucune application tierce n'a été fermée par OptiMem.";
            new AlertDialog.Builder(this).setTitle("Préparation IA — manuelle").setMessage(msg)
                    .setPositiveButton("Ouvrir les paramètres", (d, w) -> openPhoneSettings())
                    .setNegativeButton("Rester dans OptiMem", null).show();
        } catch (RuntimeException e) {
            journal.append("ERREUR preparation " + e.getClass().getSimpleName());
            toast("Impossible de mesurer la RAM.");
        }
    }
    private void measureAfter() {
        if (!prefs.contains("before")) {
            toast("Commencer par « Préparer le téléphone pour une IA lourde ».");
            return;
        }
        try {
            ActivityManager.MemoryInfo after = snapshot();
            long before = prefs.getLong("before", 0);
            long delta = after.availMem - before;
            prefs.edit().putLong("after", after.availMem).putLong("afterTime", System.currentTimeMillis())
                    .putBoolean("afterLow", after.lowMemory).apply();
            journal.append("MESURE_APRES " + state(after) + " variation_disponible_octets=" + delta
                    + " actionExterneManuelle=non_verifiable");
            updateComparisonFromPreferences();
            toast("Mesure après enregistrée.");
        } catch (RuntimeException e) {
            journal.append("ERREUR mesure_apres " + e.getClass().getSimpleName());
            toast("Nouvelle mesure impossible.");
        }
    }
    private void updateComparisonFromPreferences() {
        if (comparison == null) return;
        if (!prefs.contains("before")) {
            comparison.setText("Aucune comparaison enregistrée.");
            return;
        }
        long before = prefs.getLong("before", 0);
        String data = "Avant : " + gb(before) + " disponibles (" +
                moment(prefs.getLong("beforeTime", 0)) + ").";
        if (prefs.contains("after")) {
            long after = prefs.getLong("after", 0);
            data += "\nAprès : " + gb(after) + " disponibles (" +
                    moment(prefs.getLong("afterTime", 0)) + ")."
                    + "\nVariation observée : " +
                    (after >= before ? "+" : "−") + gb(Math.abs(after - before)) +
                    ". Cette variation n'est pas une quantité « nettoyée » certifiée.";
        } else {
            data += "\nAprès : pas encore mesuré.";
        }
        comparison.setText(data);
    }
    private void openPhoneSettings() {
        journal.append("ACTION ouverture paramètres généraux Android (aucun nettoyage automatique)");
        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        catch (RuntimeException e) {
            journal.append("ECHEC paramètres généraux " + e.getClass().getSimpleName());
            toast("Paramètres indisponibles.");
        }
    }
    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        return appOps != null && appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    private void openUsageAccessSettings() {
        journal.append("ACTION accès réglages autorisation données utilisation");
        try { startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)); }
        catch (RuntimeException e) {
            journal.append("ECHEC réglage permission usage " + e.getClass().getSimpleName());
            toast("Le réglage d'autorisation est indisponible.");
        }
    }
    private String appName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return String.valueOf(pm.getApplicationLabel(info));
        } catch (PackageManager.NameNotFoundException | SecurityException e) {
            return packageName;
        }
    }
    private void showUsage() {
        if (!hasUsageAccess()) {
            journal.append("PERMISSION usage access refusée/non accordée ; aucune donnée lue");
            new AlertDialog.Builder(this).setTitle("Accès facultatif")
                    .setMessage("Sans autorisation d'accès aux données d'utilisation, OptiMem ne peut pas présenter la liste récente. Cette autorisation ne permet pas de connaître la RAM des autres applications ni de les arrêter.")
                    .setPositiveButton("Autoriser manuellement", (d,w) -> openUsageAccessSettings())
                    .setNegativeButton("Annuler", null).show();
            return;
        }
        try {
            UsageStatsManager manager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            UsageEvents events = manager == null ? null : manager.queryEvents(now - 6L * 3600 * 1000, now);
            Map<String, Long> seen = new HashMap<>();
            if (events != null) {
                UsageEvents.Event event = new UsageEvents.Event();
                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                            event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        String pkg = event.getPackageName();
                        if (pkg != null && !pkg.equals(getPackageName())) {
                            long previous = seen.containsKey(pkg) ? seen.get(pkg) : 0;
                            if (event.getTimeStamp() > previous) seen.put(pkg, event.getTimeStamp());
                        }
                    }
                }
            }
            List<Map.Entry<String, Long>> list = new ArrayList<>(seen.entrySet());
            list.sort((a,b) -> Long.compare(b.getValue(), a.getValue()));
            if (list.size() > 20) list = new ArrayList<>(list.subList(0, 20));
            journal.append("ACTIVITE autorisée packagesRécent=" + list.size() +
                    " fenêtre=6h statut_processus=non_observable ram_tiers=non_observable");
            final Map<Integer, String> ids = new HashMap<>();
            final String[] selected = {null};
            LinearLayout panel = column();
            panel.setPadding(dp(15), dp(6), dp(15), dp(6));
            TextView note = text(panel, "Activité au premier plan dans les 6 dernières heures, pas une liste de processus exécutés. Consommation RAM de chaque application : non disponible. Sélectionner une application pour accéder à ses réglages.", 16, Color.BLACK);
            if (list.isEmpty()) text(panel, "Aucune activité disponible dans cette fenêtre.", 17, Color.BLACK);
            RadioGroup group = new RadioGroup(this);
            panel.addView(group);
            for (Map.Entry<String, Long> item : list) {
                RadioButton rb = new RadioButton(this);
                int id = View.generateViewId();
                rb.setId(id);
                ids.put(id, item.getKey());
                rb.setText(appName(item.getKey()) + "\n" + item.getKey() +
                        "\nDernier affichage : " + moment(item.getValue()) +
                        "\nProcessus : non confirmé · RAM : non mesurable");
                rb.setTextColor(Color.BLACK);
                rb.setTextSize(16);
                rb.setPadding(dp(5), dp(8), dp(5), dp(8));
                rb.setMinHeight(dp(67));
                group.addView(rb);
            }
            group.setOnCheckedChangeListener((g,checkedId)->selected[0] = ids.get(checkedId));
            ScrollView scroll = new ScrollView(this);
            scroll.addView(panel);
            new AlertDialog.Builder(this).setTitle("Activité récente — informative")
                    .setView(scroll)
                    .setPositiveButton("Réglages de l'application choisie", (dialog,which) -> {
                        if (selected[0] != null) openAppSettings(selected[0]);
                        else toast("Aucune application sélectionnée.");
                    })
                    .setNegativeButton("Fermer", null).show();
        } catch (SecurityException denied) {
            journal.append("PERMISSION REFUSÉE consultation activité: SecurityException");
            toast("Android refuse l'accès aux données d'utilisation.");
        } catch (RuntimeException e) {
            journal.append("ERREUR activité " + e.getClass().getSimpleName());
            toast("Activité récente indisponible.");
        }
    }
    private void openAppSettings(String pkg) {
        journal.append("ACTION ouverture paramètres de l'application " + pkg + " ; aucune fermeture automatisée");
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + pkg));
            startActivity(i);
        } catch (RuntimeException e) {
            journal.append("ECHEC réglages de " + pkg + " : " + e.getClass().getSimpleName());
            toast("Les paramètres de cette application sont indisponibles.");
        }
    }
    private void chooseLaunchApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager pm = getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
            Map<String, String> byPackage = new LinkedHashMap<>();
            for (ResolveInfo r : apps) {
                if (r.activityInfo != null && !getPackageName().equals(r.activityInfo.packageName)) {
                    String pkg = r.activityInfo.packageName;
                    byPackage.put(pkg, String.valueOf(r.loadLabel(pm)));
                }
            }
            List<Map.Entry<String, String>> items = new ArrayList<>(byPackage.entrySet());
            items.sort((a,b)->a.getValue().compareToIgnoreCase(b.getValue()));
            if (items.isEmpty()) { toast("Aucune autre application lançable visible."); return; }
            String[] labels = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                labels[i] = items.get(i).getValue() + " — " + items.get(i).getKey();
            }
            new AlertDialog.Builder(this).setTitle("Choisir FxExtractor ou une autre IA")
                    .setItems(labels, (d,index)->{
                        String pkg = items.get(index).getKey();
                        prefs.edit().putString("launchPkg",pkg).apply();
                        journal.append("CHOIX application à lancer : " + pkg + " ; non lancée");
                        updateChosen();
                    }).setNegativeButton("Annuler", null).show();
        } catch (RuntimeException e) {
            journal.append("ERREUR liste lanceurs " + e.getClass().getSimpleName());
            toast("Impossible de lister les applications lançables.");
        }
    }
    private void updateChosen() {
        if (appChosen == null) return;
        String pkg = prefs.getString("launchPkg", "");
        appChosen.setText(pkg.isEmpty() ? "Application choisie : aucune" :
                "Application choisie : " + appName(pkg) + " (" + pkg + ")");
    }
    private void launchChosen() {
        String pkg = prefs.getString("launchPkg", "");
        if (pkg.isEmpty()) { toast("Choisir d'abord une application."); return; }
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent == null) {
                Intent query = new Intent(Intent.ACTION_MAIN);
                query.addCategory(Intent.CATEGORY_LAUNCHER);
                for (ResolveInfo info : getPackageManager().queryIntentActivities(query, 0)) {
                    if (info.activityInfo != null && pkg.equals(info.activityInfo.packageName)) {
                        intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                                .setClassName(pkg, info.activityInfo.name);
                        break;
                    }
                }
            }
            if (intent == null) {
                journal.append("ECHEC lancement " + pkg + " : intention introuvable");
                toast("Application non lançable. Vérifier installation.");
                return;
            }
            journal.append("ACTION lancement application " + pkg);
            startActivity(intent);
        } catch (RuntimeException e) {
            journal.append("ECHEC lancement " + pkg + " " + e.getClass().getSimpleName());
            toast("Le lancement a été refusé par Android.");
        }
    }
    private void recordRamPlus(int gbConfig) {
        try {
            ActivityManager.MemoryInfo m = snapshot();
            long time = System.currentTimeMillis();
            prefs.edit().putLong("ramPlus"+gbConfig, m.availMem)
                    .putLong("ramPlusTime"+gbConfig, time).apply();
            journal.append("ESSAI_RAM_PLUS " + gbConfig + "Go VALEUR_DECLARÉE_MANUELLEMENT "
                    + state(m) + " date=" + moment(time));
            updateRamPlus();
            toast("Essai annoté : " + gbConfig + " Go déclarés par l'utilisateur.");
        } catch (RuntimeException e) {
            journal.append("ERREUR essai RAM Plus " + e.getClass().getSimpleName());
            toast("Mesure impossible.");
        }
    }
    private void updateRamPlus() {
        if (ramPlusTest == null) return;
        boolean four = prefs.contains("ramPlus4"), eight = prefs.contains("ramPlus8");
        String msg = "Essais RAM Plus (réglages déclarés manuellement ; pas de lecture de la configuration système) :";
        msg += four ? "\n4 Go : " + gb(prefs.getLong("ramPlus4",0)) +
                " disponibles le " + moment(prefs.getLong("ramPlusTime4",0)) : "\n4 Go : non enregistré";
        msg += eight ? "\n8 Go : " + gb(prefs.getLong("ramPlus8",0)) +
                " disponibles le " + moment(prefs.getLong("ramPlusTime8",0)) : "\n8 Go : non enregistré";
        if (four && eight) {
            long delta = prefs.getLong("ramPlus8",0) - prefs.getLong("ramPlus4",0);
            msg += "\nDifférence observée 8 Go vs 4 Go : " +
                    (delta >= 0 ? "+" : "−") + gb(Math.abs(delta)) +
                    ". Ce n'est pas une preuve de gain de performance.";
        }
        ramPlusTest.setText(msg);
    }
    private long deleteOwnCacheTree(File root, File file) {
        try {
            String anchor = root.getCanonicalPath() + File.separator;
            String path = file.getCanonicalPath();
            if (!path.startsWith(anchor) || java.nio.file.Files.isSymbolicLink(file.toPath())) return 0;
            if (file.isDirectory()) {
                long total = 0;
                File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) total += deleteOwnCacheTree(root, child);
                }
                file.delete();
                return total;
            } else {
                long len = file.length();
                return file.delete() ? len : 0;
            }
        } catch (IOException | SecurityException e) {
            return 0;
        }
    }
    private void cleanOwnCache() {
        File root = getCacheDir();
        long bytes = 0;
        File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) bytes += deleteOwnCacheTree(root, child);
        }
        journal.append("CACHE_PROPRE nettoyage_dossier_cacheDir_uniquement_octets_supprimés=" + bytes);
        new AlertDialog.Builder(this).setTitle("Cache OptiMem seulement")
                .setMessage("Fichiers temporaires propres à OptiMem supprimés (taille des fichiers effacés) : "
                        + String.format(Locale.FRANCE,"%.1f Ko",bytes / 1024.0)
                        + ".\nAucun cache d'une autre application ni fichier de modèle IA n'a été touché.")
                .setPositiveButton("OK",null).show();
    }
    private void copyJournal() {
        try {
            String log = journal.read();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("Journal OptiMem", log));
                toast("Journal local copié dans le presse-papiers.");
            } else {
                toast("Presse-papiers indisponible.");
            }
        } catch (IOException | RuntimeException e) {
            toast("Impossible de copier le journal.");
        }
    }
    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); }
}
