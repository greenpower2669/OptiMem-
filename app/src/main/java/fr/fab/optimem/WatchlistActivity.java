package fr.fab.optimem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A persistent manual checklist: no automatic killing/uninstall/hibernation. */
public final class WatchlistActivity extends Activity {
    private static final int BG = Color.rgb(11, 19, 36);
    private static final int WHITE = Color.rgb(246, 250, 254);
    private static final int MUTED = Color.rgb(197, 216, 229);
    private static final int TEAL = Color.rgb(113, 219, 183);
    private static final int EXPORT = 410, IMPORT = 411;
    private WatchlistStore store;
    private LocalJournal journal;
    private SpeechGuide speech;
    private Button automaticVoiceButton;
    private LinearLayout listContainer;
    private TextView summary;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new WatchlistStore(this);
        journal = new LocalJournal(this);
        speech = new SpeechGuide(this);
        setup();
        speech.onPageOpened("Voici ton carnet personnel OptiMem. " + spokenListSummary()
                + " Pour entendre les conseils, utilise le gros bouton Lire mon carnet.");
    }
    @Override protected void onPause() {
        speech.stop();
        super.onPause();
    }
    @Override protected void onDestroy() {
        speech.shutdown();
        super.onDestroy();
    }
    @Override protected void onResume() {
        super.onResume();
        if (listContainer != null) refreshList();
    }
    private int dp(int x) { return Math.round(x * getResources().getDisplayMetrics().density); }
    private LinearLayout vertical() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        return result;
    }
    private TextView text(LinearLayout box, String value, int size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(0, dp(7), 0, dp(7));
        box.addView(v);
        return v;
    }
    private Button button(LinearLayout layout, String name, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setTextColor(BG);
        b.setMinHeight(dp(56));
        b.setBackgroundTintList(ColorStateList.valueOf(TEAL));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(5), 0, dp(5));
        layout.addView(b, params);
        b.setOnClickListener(listener);
        return b;
    }
    private void setup() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = vertical();
        root.setPadding(dp(18), dp(14), dp(18), dp(42));
        scroll.addView(root);
        setContentView(scroll);

        text(root, "📋 Mon carnet d'applications", 27, TEAL);
        text(root, "Coche les applications à examiner : elles resteront dans ton carnet après la fermeture ou le redémarrage d'OptiMem. Aucune application n'est arrêtée.", 17, MUTED);
        button(root, "🔊 Lire mon carnet et les conseils", v ->
                speech.speak(spokenListSummary() + " " + spokenGuideSummary()));
        button(root, "🔊 Lire la liste des applications mémorisées", v ->
                speech.speak(spokenListContents()));
        button(root, "⏹ Arrêter la lecture", v -> speech.stop());
        automaticVoiceButton = button(root, "Lecture automatique : " +
                (speech.automaticEnabled() ? "activée" : "désactivée"), v -> {
            boolean enabled = !speech.automaticEnabled();
            speech.setAutomaticEnabled(enabled);
            automaticVoiceButton.setText("Lecture automatique : " +
                    (enabled ? "activée" : "désactivée"));
            speech.speak(enabled ? "Lecture automatique activée." :
                    "Lecture automatique désactivée. Les boutons de lecture restent disponibles.");
        });
        summary = text(root, "Chargement…", 19, WHITE);
        button(root, "➕ Choisir des applications à surveiller", v -> chooseInstalledApps());
        button(root, "Guide : limiter, mettre en veille ou désinstaller", v -> guide(null));
        button(root, "Ouvrir la liste des applications Android", v -> openGeneralAppSettings());
        button(root, "Sauvegarder mon carnet dans un fichier", v -> exportList());
        button(root, "Restaurer mon carnet depuis un fichier", v -> importList());
        text(root, "Important : la sauvegarde est manuelle. Avant de désinstaller OptiMem pour installer une APK signée autrement, sauvegarde ce carnet ; une désinstallation efface ses données locales.", 16, MUTED);
        listContainer = vertical();
        root.addView(listContainer);
        refreshList();
    }
    private String spokenListSummary() {
        List<WatchlistStore.Entry> records = store.entries();
        int done = 0;
        for (WatchlistStore.Entry row : records) if (row.reviewed) done++;
        return "Ton carnet contient " + records.size() + " applications, dont " +
                done + " déjà examinées et " + (records.size() - done) + " à vérifier.";
    }
    private String spokenListContents() {
        List<WatchlistStore.Entry> records = store.entries();
        if (records.isEmpty()) return "Le carnet est vide. Ajoute d'abord les applications que tu souhaites examiner.";
        StringBuilder result = new StringBuilder(spokenListSummary());
        int limit = Math.min(10, records.size());
        for (int i = 0; i < limit; i++) {
            WatchlistStore.Entry app = records.get(i);
            result.append(" Numéro ").append(i + 1).append(". ")
                    .append(app.label).append(app.reviewed ? ", examinée." : ", à examiner.");
            if (!app.note.trim().isEmpty()) result.append(" Remarque : ").append(app.note);
        }
        if (records.size() > limit) result.append(" Il reste ")
                .append(records.size() - limit)
                .append(" fiches. Écoute-les individuellement grâce à Lire cette fiche.");
        return result.toString();
    }
    private String spokenGuideSummary() {
        return "Pour limiter une application, ouvre ses paramètres Android et cherche Batterie, "
                + "utilisation en arrière-plan ou restriction. Les applications facultatives peuvent "
                + "être mises en veille dans les réglages Samsung. "
                + "Attention aux appels, aux notifications, aux alarmes, aux sauvegardes et "
                + "aux services d'accessibilité. Ne désinstalle jamais sans vérifier les données "
                + "à conserver. OptiMem ne ferme aucune application automatiquement.";
    }
    private String noteForSpeech(String pkg) {
        for (WatchlistStore.Entry entry : store.entries())
            if (entry.packageName.equals(pkg)) return entry.note;
        return "";
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private void refreshList() {
        if (listContainer == null) return;
        List<WatchlistStore.Entry> all = store.entries();
        int done = 0;
        for (WatchlistStore.Entry row : all) if (row.reviewed) done++;
        summary.setText(all.size() + " applications mémorisées • " +
                (all.size() - done) + " à examiner • " + done + " déjà examinées");
        listContainer.removeAllViews();
        if (all.isEmpty()) {
            text(listContainer, "Aucune application sélectionnée. Utilise « Choisir des applications à surveiller » pour commencer.", 18, WHITE);
            return;
        }
        for (WatchlistStore.Entry row : all) addCard(row);
    }
    private void addCard(WatchlistStore.Entry row) {
        LinearLayout card = vertical();
        card.setPadding(dp(12), dp(14), dp(12), dp(14));
        card.setBackgroundColor(Color.rgb(24, 45, 62));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, dp(8));
        listContainer.addView(card, params);

        text(card, row.label, 21, WHITE);
        text(card, row.packageName, 14, MUTED);
        button(card, "🔊 Lire cette fiche : " + row.label, v -> {
            String note = noteForSpeech(row.packageName);
            speech.speak(row.label + ". " + (row.reviewed ? "Déjà vérifiée." : "Encore à examiner.") +
                    (note.isEmpty() ? " Aucune note enregistrée." : " Remarque : " + note) +
                    " Tu peux enregistrer une remarque, la déclarer vérifiée ou ouvrir ses paramètres.");
        });
        if (row.packageName.equals(getPackageName())) {
            text(card, "OptiMem lui-même : ne désinstalle pas cet outil pour optimiser la RAM.", 16, MUTED);
        }
        boolean system = isSystemApp(row.packageName);
        if (system) text(card, "Attention : composant système ou préinstallé. Ne pas désactiver sans vérifier son rôle.", 16, TEAL);
        CheckBox tracked = new CheckBox(this);
        tracked.setText("☑ À conserver dans mon carnet");
        tracked.setTextSize(18);
        tracked.setTextColor(WHITE);
        tracked.setMinHeight(dp(48));
        tracked.setChecked(true);
        card.addView(tracked);
        tracked.setOnCheckedChangeListener((b, checked) -> {
            if (!checked) {
                new AlertDialog.Builder(this).setTitle("Retirer du carnet ?")
                        .setMessage("Tu ne modifieras pas l'application Android. Seule sa fiche OptiMem et ses notes seront retirées. Tu peux annuler.")
                        .setPositiveButton("Retirer la fiche", (d, w) -> {
                            if (store.setTracked(row.packageName, row.label, false)) {
                                journal.append("CARNET fiche retirée " + row.packageName);
                                refreshList();
                            } else toast("Échec de l'enregistrement");
                        })
                        .setNegativeButton("Garder", (d, w) -> tracked.setChecked(true))
                        .setOnCancelListener(d -> tracked.setChecked(true)).show();
            }
        });

        CheckBox reviewed = new CheckBox(this);
        reviewed.setText("J'ai vérifié cette application");
        reviewed.setChecked(row.reviewed);
        reviewed.setTextColor(WHITE);
        reviewed.setTextSize(18);
        reviewed.setMinHeight(dp(48));
        card.addView(reviewed);

        text(card, "Ma remarque (raison du doute, action envisagée, résultat constaté) :", 16, MUTED);
        EditText note = new EditText(this);
        note.setText(row.note);
        note.setTextColor(WHITE);
        note.setHintTextColor(MUTED);
        note.setHint("Ex. : peu utile, vérifier la batterie avant de la limiter");
        note.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        note.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1500)});
        note.setTextSize(17);
        note.setMinLines(2);
        note.setMaxLines(6);
        note.setContentDescription("Remarque personnelle pour " + row.label);
        card.addView(note);
        reviewed.setOnCheckedChangeListener((check, value) -> {
            if (store.update(row.packageName, note.getText().toString(), value)) {
                journal.append("CARNET état examiné enregistré " + row.packageName + " = " + value);
                speech.speakIfAutomatic(row.label + (value ? " vérifiée." : " remise à examiner."));
                updateCount();
            } else {
                reviewed.setOnCheckedChangeListener(null);
                reviewed.setChecked(!value);
                toast("Échec de sauvegarde du statut.");
            }
        });

        button(card, "Enregistrer note et statut de " + row.label, v -> {
            if (store.update(row.packageName, note.getText().toString(), reviewed.isChecked())) {
                journal.append("CARNET note/statut sauvegardés " + row.packageName + " examiné=" + reviewed.isChecked());
                speech.speakIfAutomatic("Remarque et statut enregistrés pour " + row.label + ".");
                toast("Fiche enregistrée.");
                refreshList();
            } else {
                journal.append("ERREUR enregistrement fiche " + row.packageName);
                toast("La fiche n'a pas pu être enregistrée.");
            }
        });
        button(card, "Comment agir sur " + row.label + " ?", v -> guide(row.packageName));
        button(card, "Ouvrir les paramètres Android de " + row.label, v -> openAppSettings(row.packageName));
    }
    private boolean isSystemApp(String pkg) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(pkg, 0);
            return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException | SecurityException e) {
            return false; // Unknown is not proof of a non-system package.
        }
    }
    private void chooseInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found;
        try { found = pm.queryIntentActivities(launcher, 0); }
        catch (RuntimeException e) { journal.append("ERREUR catalogue launcher " + e.getClass().getSimpleName()); toast("Catalogue indisponible."); return; }
        Map<String, String> unique = new HashMap<>();
        for (ResolveInfo item : found) {
            if (item.activityInfo != null && !getPackageName().equals(item.activityInfo.packageName)) {
                String pkg = item.activityInfo.packageName;
                unique.put(pkg, String.valueOf(item.loadLabel(pm)));
            }
        }
        List<Map.Entry<String, String>> apps = new ArrayList<>(unique.entrySet());
        apps.sort((a,b) -> a.getValue().compareToIgnoreCase(b.getValue()));
        LinearLayout dialogContent = vertical();
        dialogContent.setPadding(dp(14), dp(8), dp(14), dp(8));
        TextView intro = text(dialogContent, "Coche les applications à conserver dans le carnet. Le choix est enregistré immédiatement. La liste contient les applications dont Android autorise l'affichage ; elle ne garantit pas un inventaire complet.", 16, MUTED);
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(MUTED);
        search.setTextSize(19);
        search.setHint("Rechercher une application");
        search.setContentDescription("Filtrer les applications visibles");
        dialogContent.addView(search);
        ScrollView innerScroll = new ScrollView(this);
        LinearLayout choices = vertical();
        innerScroll.addView(choices);
        dialogContent.addView(innerScroll, new LinearLayout.LayoutParams(-1, dp(350)));
        Runnable populate = () -> {
            String term = search.getText().toString().toLowerCase(Locale.ROOT).trim();
            choices.removeAllViews();
            int count = 0;
            for (Map.Entry<String, String> app : apps) {
                if (!app.getValue().toLowerCase(Locale.ROOT).contains(term) &&
                        !app.getKey().toLowerCase(Locale.ROOT).contains(term)) continue;
                if (count >= 65) break;
                count++;
                String pkg = app.getKey(), label = app.getValue();
                CheckBox cb = new CheckBox(this);
                cb.setText(label + "\n" + pkg);
                cb.setTextSize(17);
                cb.setTextColor(WHITE);
                cb.setMinHeight(dp(60));
                cb.setChecked(store.contains(pkg));
                choices.addView(cb);
                cb.setOnCheckedChangeListener((button, isChecked) -> {
                    if (store.setTracked(pkg, label, isChecked)) {
                        journal.append("CARNET " + (isChecked ? "sélection" : "déselection") + " " + pkg);
                        speech.speakIfAutomatic(label + (isChecked ? " ajoutée au carnet." : " retirée du carnet."));
                        updateCount();
                    } else {
                        cb.setOnCheckedChangeListener(null);
                        cb.setChecked(!isChecked);
                        cb.setOnCheckedChangeListener((v1, v2) -> {
                            if (store.setTracked(pkg, label, v2)) updateCount();
                        });
                        toast("Impossible d'enregistrer ce choix.");
                    }
                });
            }
            if (count == 0) text(choices, "Aucune application correspondante dans le catalogue visible.", 16, WHITE);
            if (count >= 65) text(choices, "Affichage limité aux 65 premiers résultats : précise ta recherche pour voir les autres.", 16, MUTED);
        };
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { populate.run(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        populate.run();
        speech.speakIfAutomatic("Sélectionne les applications à surveiller. Les cases sont sauvegardées immédiatement. Tu peux filtrer la liste avec le champ Recherche.");
        new AlertDialog.Builder(this).setTitle("Choisir les applications à surveiller")
                .setView(dialogContent)
                .setPositiveButton("Terminé", (d,w) -> refreshList()).show();
        journal.append("CARNET catalogue launcher ouvert totalVisible=" + apps.size());
    }
    private void updateCount() {
        int all = store.entries().size(), done=0;
        for (WatchlistStore.Entry entry : store.entries()) if (entry.reviewed) done++;
        summary.setText(all + " applications mémorisées • " + (all - done) + " à examiner • " + done + " déjà examinées");
    }
    private void guide(String pkg) {
        String msg = "1. Pour examiner : ouvre les paramètres Android de l'application et consulte Batterie / Utilisation en arrière-plan, selon les menus disponibles.\n\n"
                + "2. Pour limiter : sur Samsung, Paramètres → Batterie ou Maintenance de l'appareil → Batterie → Limites d'utilisation en arrière-plan → Applications en veille ou veille profonde → Ajouter. Les intitulés dépendent de One UI.\n\n"
                + "3. Pour la désinstaller : Paramètres → Applications → application → Désinstaller, si Android propose l'option. Vérifie sauvegardes et données avant. Une application système peut ne pas être désinstallable.\n\n"
                + "4. « Forcer l'arrêt » est temporaire : Android ou toi-même pourrez relancer l'application. Une restriction de batterie peut retarder des alertes ou notifications.\n\n"
                + "IMPORTANT : ne touche pas aux services d'accessibilité, appels, messagerie, sécurité, réveils, sauvegardes ou fonctions critiques. OptiMem ne change aucun de ces réglages à ta place. Aucun gain de RAM garanti.";
        speech.speakIfAutomatic(spokenGuideSummary());
        new AlertDialog.Builder(this).setTitle("Guide manuel et réversible")
                .setMessage(msg)
                .setPositiveButton(pkg == null ? "Réglages des applications" : "Paramètres de cette application",
                        (d, w) -> { if (pkg == null) openGeneralAppSettings(); else openAppSettings(pkg); })
                .setNeutralButton("Réglages généraux — Batterie", (d, w) -> openBatterySettings())
                .setNegativeButton("Fermer", null).show();
    }
    private void openAppSettings(String pkg) {
        journal.append("CARNET demande d'ouverture détails Android " + pkg + " ; aucune modification système");
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg)));
        } catch (RuntimeException e) {
            journal.append("ECHEC ouverture application settings " + pkg + " " + e.getClass().getSimpleName());
            toast("Détails indisponibles, ouverture des paramètres généraux.");
            openGeneralAppSettings();
        }
    }
    private void openGeneralAppSettings() {
        journal.append("CARNET ouverture réglages liste applications");
        try { startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)); }
        catch (RuntimeException e) { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (RuntimeException ignored) { toast("Réglages indisponibles."); } }
    }
    private void openBatterySettings() {
        journal.append("CARNET ouverture réglages batterie généraux");
        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        catch (RuntimeException e) { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (RuntimeException ignored) { toast("Réglages indisponibles."); } }
    }
    private void exportList() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "OptiMem-carnet-applications.json");
        try { startActivityForResult(intent, EXPORT); }
        catch (RuntimeException e) { toast("Choix d'un fichier indisponible."); journal.append("ERREUR ouverture export SAF"); }
    }
    private void importList() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try { startActivityForResult(intent, IMPORT); }
        catch (RuntimeException e) { toast("Choix d'un fichier indisponible."); journal.append("ERREUR ouverture import SAF"); }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == EXPORT) {
            try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                if (out == null) throw new IOException("Aucun accès écriture");
                out.write(store.exportJson().getBytes(StandardCharsets.UTF_8));
                out.flush();
                journal.append("CARNET sauvegarde manuelle via sélecteur Android, fiches=" + store.entries().size());
                speech.speakIfAutomatic("Carnet sauvegardé dans le fichier choisi.");
                toast("Carnet sauvegardé.");
            } catch (IOException | JSONException | RuntimeException e) {
                journal.append("ECHEC sauvegarde carnet " + e.getClass().getSimpleName());
                toast("Échec de la sauvegarde : vérifier le fichier choisi.");
            }
        } else if (requestCode == IMPORT) {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) throw new IOException("Aucun accès lecture");
                ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) {
                    if (dataBytes.size() + n > 524288) throw new IOException("Fichier trop volumineux");
                    dataBytes.write(buf, 0, n);
                }
                String payload = new String(dataBytes.toByteArray(), StandardCharsets.UTF_8);
                int count = WatchlistStore.inspectImport(payload);
                new AlertDialog.Builder(this).setTitle("Restaurer mon carnet")
                        .setMessage("Le fichier contient " + count + " fiches. Les fiches nouvelles seront ajoutées. Les notes et statuts déjà présents dans OptiMem seront conservés, sans écrasement. Continuer ?")
                        .setPositiveButton("Importer", (d, w) -> {
                            try {
                                int added = store.mergeImported(payload);
                                journal.append("CARNET restauration manuelle fichier, nouvelles_fiches=" + added);
                                refreshList();
                                speech.speakIfAutomatic(added + " nouvelles fiches importées.");
                                toast(added + " nouvelles fiches importées.");
                            } catch (JSONException e) {
                                journal.append("ECHEC restauration carnet format ou stockage");
                                toast("Importation impossible.");
                            }
                        }).setNegativeButton("Annuler", null).show();
            } catch (IOException | JSONException | RuntimeException e) {
                journal.append("ECHEC lecture carnet import " + e.getClass().getSimpleName());
                toast("Fichier incompatible ou illisible.");
            }
        }
    }
}
