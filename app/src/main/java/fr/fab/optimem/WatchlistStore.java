package fr.fab.optimem;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Local-only, explicit watchlist. No package intervention, telemetry or network. */
public final class WatchlistStore {
    public static final class Entry {
        public final String packageName;
        public String label, note;
        public boolean reviewed;
        public final long addedAt;
        Entry(String pkg, String label, String note, boolean reviewed, long addedAt) {
            this.packageName = pkg;
            this.label = label;
            this.note = note;
            this.reviewed = reviewed;
            this.addedAt = addedAt;
        }
    }
    private static final String PREFS = "optimem_watchlist";
    private static final String KEY = "records_json";
    private static final int MAX_APPS = 300;
    private final SharedPreferences prefs;

    public WatchlistStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    private static boolean validPackage(String name) {
        return name != null && name.length() <= 240 &&
                name.matches("[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+");
    }
    private static String clean(String data, int limit) {
        if (data == null) return "";
        return data.length() > limit ? data.substring(0, limit) : data;
    }
    private synchronized JSONObject load() {
        try {
            String raw = prefs.getString(KEY, "");
            if (!raw.isEmpty()) {
                JSONObject parsed = new JSONObject(raw);
                if (parsed.optInt("schema", -1) == 1 && parsed.optJSONArray("apps") != null) {
                    return parsed;
                }
            }
        } catch (JSONException | ClassCastException ignored) {
            // Do not overwrite bad or unsupported records implicitly.
        }
        JSONObject fresh = new JSONObject();
        try { fresh.put("schema", 1); fresh.put("apps", new JSONArray()); }
        catch (JSONException ignored) { }
        return fresh;
    }
    private synchronized boolean save(JSONObject data) {
        return prefs.edit().putString(KEY, data.toString()).commit();
    }
    public synchronized List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        JSONArray arr = load().optJSONArray("apps");
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            String pkg = o.optString("package", "");
            if (!validPackage(pkg)) continue;
            out.add(new Entry(pkg, o.optString("label", pkg),
                    o.optString("note", ""), o.optBoolean("reviewed", false),
                    o.optLong("addedAt", 0)));
        }
        out.sort((a, b) -> Long.compare(b.addedAt, a.addedAt));
        return out;
    }
    public synchronized boolean contains(String pkg) {
        return find(load().optJSONArray("apps"), pkg) >= 0;
    }
    private static int find(JSONArray arr, String pkg) {
        if (arr != null) for (int i = 0; i < arr.length(); i++) {
            JSONObject row = arr.optJSONObject(i);
            if (row != null && pkg.equals(row.optString("package", ""))) return i;
        }
        return -1;
    }
    public synchronized boolean setTracked(String pkg, String label, boolean yes) {
        if (!validPackage(pkg)) return false;
        JSONObject obj = load();
        JSONArray old = obj.optJSONArray("apps");
        if (old == null) old = new JSONArray();
        int index = find(old, pkg);
        if (yes && index < 0 && old.length() >= MAX_APPS) return false;
        JSONArray next = new JSONArray();
        for (int i = 0; i < old.length(); i++) {
            if (i != index || yes) next.put(old.opt(i));
        }
        if (yes && index < 0) {
            JSONObject entry = new JSONObject();
            try {
                entry.put("package", pkg);
                entry.put("label", clean(label, 150));
                entry.put("note", "");
                entry.put("reviewed", false);
                entry.put("addedAt", System.currentTimeMillis());
                next.put(entry);
            } catch (JSONException ignored) { return false; }
        }
        try { obj.put("apps", next); } catch (JSONException ignored) { return false; }
        return save(obj);
    }
    public synchronized boolean update(String pkg, String note, boolean reviewed) {
        JSONObject root = load();
        JSONArray arr = root.optJSONArray("apps");
        int index = find(arr, pkg);
        if (index < 0) return false;
        try {
            JSONObject o = arr.getJSONObject(index);
            o.put("note", clean(note, 1500));
            o.put("reviewed", reviewed);
            o.put("reviewedAt", reviewed ? System.currentTimeMillis() : 0);
            return save(root);
        } catch (JSONException ignored) { return false; }
    }
    public synchronized String exportJson() throws JSONException {
        return load().toString(2);
    }
    public static int inspectImport(String json) throws JSONException {
        if (json == null || json.length() > 524288) throw new JSONException("Fichier trop grand");
        JSONObject root = new JSONObject(json);
        if (root.optInt("schema", -1) != 1) throw new JSONException("Version de fichier incompatible");
        JSONArray apps = root.optJSONArray("apps");
        if (apps == null || apps.length() > MAX_APPS) throw new JSONException("Liste manquante ou trop longue");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.optJSONObject(i);
            if (app == null || !validPackage(app.optString("package", ""))) {
                throw new JSONException("Identifiant d'application invalide à la ligne " + (i + 1));
            }
        }
        return apps.length();
    }
    /** Merge without overwriting existing user notes and reviewed flags. */
    public synchronized int mergeImported(String json) throws JSONException {
        inspectImport(json);
        JSONObject root = load();
        JSONArray target = root.optJSONArray("apps");
        JSONArray incoming = new JSONObject(json).getJSONArray("apps");
        int added = 0;
        for (int i = 0; i < incoming.length(); i++) {
            JSONObject item = incoming.getJSONObject(i);
            String pkg = item.getString("package");
            if (find(target, pkg) >= 0) continue;
            if (target.length() >= MAX_APPS) break;
            JSONObject entry = new JSONObject();
            entry.put("package", pkg);
            entry.put("label", clean(item.optString("label", pkg), 150));
            entry.put("note", clean(item.optString("note", ""), 1500));
            entry.put("reviewed", item.optBoolean("reviewed", false));
            entry.put("addedAt", item.optLong("addedAt", System.currentTimeMillis()));
            target.put(entry);
            added++;
        }
        if (added > 0 && !save(root)) throw new JSONException("Impossible d'enregistrer la liste");
        return added;
    }
}
