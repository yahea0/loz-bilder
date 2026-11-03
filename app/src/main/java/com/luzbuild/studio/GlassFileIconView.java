
package com.luzbuild.studio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Small, scalable glass-style icon for folders and source files. */
final class GlassFileIconView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final String label;
    private final boolean folder;
    private final int accent;

    GlassFileIconView(Context context, String fileName, boolean isFolder) {
        super(context);
        folder = isFolder;
        label = isFolder ? "DIR" : extensionLabel(fileName);
        accent = isFolder ? 0xFF65D8BE : colorFor(fileName);
        int size = Ui.dp(context, 48);
        setLayoutParams(new android.widget.LinearLayout.LayoutParams(size, size));
        setContentDescription(isFolder ? "مجلد" : "ملف " + label);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float d = getResources().getDisplayMetrics().density;
        float w = getWidth();
        float h = getHeight();
        float pad = 3.5f * d;
        bounds.set(pad, pad, w - pad, h - pad);

        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(3f * d, 0, 2f * d, 0x55000000);
        paint.setShader(new LinearGradient(0, pad, 0, h - pad,
            withAlpha(accent, 0xB8), withAlpha(accent, 0x35), Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 13f * d, 13f * d, paint);
        paint.clearShadowLayer();
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f * d);
        paint.setColor(withAlpha(0xFFEFFFFF, 0x99));
        canvas.drawRoundRect(bounds, 13f * d, 13f * d, paint);
        paint.setStyle(Paint.Style.FILL);

        if (folder) drawFolder(canvas, d); else drawFile(canvas, d);

        paint.setShader(new LinearGradient(0, pad, 0, h * 0.58f,
            0x55FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 13f * d, 13f * d, paint);
        paint.setShader(null);
    }

    private void drawFolder(Canvas canvas, float d) {
        Path p = new Path();
        p.moveTo(12*d, 16*d);
        p.lineTo(20*d, 16*d);
        p.lineTo(24*d, 20*d);
        p.lineTo(36*d, 20*d);
        p.quadTo(40*d, 20*d, 40*d, 24*d);
        p.lineTo(40*d, 34*d);
        p.quadTo(40*d, 38*d, 36*d, 38*d);
        p.lineTo(12*d, 38*d);
        p.quadTo(8*d, 38*d, 8*d, 34*d);
        p.lineTo(8*d, 20*d);
        p.quadTo(8*d, 16*d, 12*d, 16*d);
        p.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 15*d, 0, 39*d,
            0xFFE8FFF8, 0xFF9DE9D6, Shader.TileMode.CLAMP));
        canvas.drawPath(p, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f*d);
        paint.setColor(0xCCFFFFFF);
        canvas.drawPath(p, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x5573BFAE);
        canvas.drawRoundRect(11*d, 25*d, 37*d, 34*d, 3*d, 3*d, paint);
    }

    private void drawFile(Canvas canvas, float d) {
        Path p = new Path();
        p.moveTo(14*d, 8*d);
        p.lineTo(28*d, 8*d);
        p.lineTo(37*d, 17*d);
        p.lineTo(37*d, 38*d);
        p.quadTo(37*d, 41*d, 34*d, 41*d);
        p.lineTo(14*d, 41*d);
        p.quadTo(11*d, 41*d, 11*d, 38*d);
        p.lineTo(11*d, 11*d);
        p.quadTo(11*d, 8*d, 14*d, 8*d);
        p.close();
        paint.setShader(new LinearGradient(0, 8*d, 0, 41*d,
            0xFFF4FBFF, 0xFFBDD9E9, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(p, paint);
        paint.setShader(null);
        Path fold = new Path();
        fold.moveTo(28*d, 8*d); fold.lineTo(28*d, 17*d); fold.lineTo(37*d, 17*d); fold.close();
        paint.setColor(withAlpha(accent, 0xFF));
        canvas.drawPath(fold, paint);
        paint.setColor(0xFF6A8290);
        paint.setStrokeWidth(1.3f*d);
        canvas.drawLine(16*d, 24*d, 32*d, 24*d, paint);
        canvas.drawLine(16*d, 29*d, 32*d, 29*d, paint);
        canvas.drawLine(16*d, 34*d, 27*d, 34*d, paint);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private static String extensionLabel(String name) {
        if (name == null) return "FILE";
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "FILE";
        String ext = name.substring(dot + 1).toUpperCase();
        return ext.length() > 4 ? ext.substring(0, 4) : ext;
    }

    private static int colorFor(String name) {
        String n = name == null ? "" : name.toLowerCase();
        if (n.endsWith(".java") || n.endsWith(".kt")) return 0xFF7DAEFF;
        if (n.endsWith(".js") || n.endsWith(".ts") || n.endsWith(".jsx") || n.endsWith(".tsx")) return 0xFFFFD166;
        if (n.endsWith(".py")) return 0xFF8BC7F0;
        if (n.endsWith(".xml") || n.endsWith(".html")) return 0xFFFFA46A;
        if (n.endsWith(".json") || n.endsWith(".yaml") || n.endsWith(".yml")) return 0xFFB89CFF;
        if (n.endsWith(".c") || n.endsWith(".cpp") || n.endsWith(".h") || n.endsWith(".hpp")) return 0xFF7DE0B3;
        if (n.endsWith(".md")) return 0xFFB4C4D0;
        return 0xFF9CC8D8;
    }
}
