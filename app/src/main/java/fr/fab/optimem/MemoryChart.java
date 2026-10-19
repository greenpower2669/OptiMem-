package fr.fab.optimem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lightweight foreground-only chart. No background observer or native library. */
public final class MemoryChart extends View {
    private final List<Long> samples = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public MemoryChart(Context context) {
        super(context);
        setMinimumHeight(Math.round(getResources().getDisplayMetrics().density * 164));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Courbe mémoire : aucune mesure encore disponible");
    }
    public void setSamples(List<Long> values) {
        samples.clear();
        samples.addAll(values);
        if (samples.isEmpty()) {
            setContentDescription("Courbe mémoire : aucune mesure");
        } else {
            long min = Long.MAX_VALUE, max = 0;
            for (long item : samples) { min = Math.min(min, item); max = Math.max(max, item); }
            setContentDescription(String.format(Locale.FRANCE,
                    "Courbe de %d observations. Mémoire disponible min %.2f et max %.2f gigaoctets.",
                    samples.size(), min / 1073741824.0, max / 1073741824.0));
        }
        invalidate();
    }
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int h = Math.round(getResources().getDisplayMetrics().density * 175);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float left = 14, top = 16, right = getWidth() - 14, bottom = getHeight() - 18;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(20, 42, 60));
        c.drawRoundRect(left, top, right, bottom, 12, 12, paint);
        paint.setStrokeWidth(1.4f);
        paint.setColor(Color.rgb(108, 127, 143));
        for (int i = 1; i < 4; i++) {
            float y = top + i * (bottom - top) / 4;
            c.drawLine(left, y, right, y, paint);
        }
        if (samples.size() < 2) { return; }
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (long s : samples) { min = Math.min(s, min); max = Math.max(s, max); }
        double range = Math.max(128 * 1024 * 1024.0, (max - min) * 1.5);
        double center = (max + min) / 2.0;
        double lo = center - range / 2.0;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(113, 219, 183));
        paint.setStrokeWidth(3.4f);
        float prevX = 0, prevY = 0;
        for (int i = 0; i < samples.size(); i++) {
            float x = left + (right - left) * i / (samples.size() - 1f);
            double frac = (samples.get(i) - lo) / range;
            float y = (float) (bottom - Math.max(0.0, Math.min(1.0, frac)) * (bottom - top));
            if (i > 0) { c.drawLine(prevX, prevY, x, y, paint); }
            prevX = x; prevY = y;
        }
        paint.setStyle(Paint.Style.FILL);
    }
}
