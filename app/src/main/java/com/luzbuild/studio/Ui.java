
package com.luzbuild.studio;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int BG = 0xFF0D151C;
    static final int PANEL = 0xFF172630;
    static final int PANEL2 = 0xFF223744;
    static final int TEXT = 0xFFF1F6F8;
    static final int MUTED = 0xFF9FB2BD;
    static final int GREEN = 0xFF69D5B4;
    static final int ORANGE = 0xFFFFC078;
    static final int BLUE = 0xFF82AAFF;
    static final int GLASS_STROKE = 0x557FE8D0;

    private Ui() {}

    static GradientDrawable shape(int color, int radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    static GradientDrawable glassShape(Context c, int top, int bottom, int radiusDp, int stroke) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{top, bottom}
        );
        d.setCornerRadius(dp(c, radiusDp));
        d.setStroke(dp(c, 1), stroke);
        return d;
    }

    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static TextView text(Context c, String s, int size, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    static TextView pill(Context c, String label, int textColor, int background) {
        TextView t = text(c, label, 10, textColor, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
        t.setBackground(glassShape(c, background, background, 30, 0x447FE8D0));
        return t;
    }

    static TextView iconButton(Context c, String label) {
        TextView t = text(c, label, 21, TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(glassShape(c, 0xFF294454, 0xFF172C38, 14, 0x6688DCCB));
        t.setElevation(dp(c, 2));
        return t;
    }

    static LinearLayout card(Context c, int color, int radius) {
        LinearLayout l = column(c);
        l.setBackground(glassShape(c, lighten(color, 7), color, radius, 0x223E7180));
        l.setElevation(dp(c, 1));
        pad(l, c, 14);
        return l;
    }

    static EditText input(Context c, String hint) {
        EditText e = new EditText(c);
        e.setSingleLine(true);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setTextSize(14);
        e.setHint(hint);
        e.setPadding(dp(c, 13), dp(c, 4), dp(c, 13), dp(c, 4));
        e.setBackground(glassShape(c, 0xFF253D4A, 0xFF1C303B, 13, 0x445B8D91));
        e.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        return e;
    }

    static Button button(Context c, String label, boolean primary) {
        Button b = new Button(c);
        b.setText(label);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setTextColor(primary ? 0xFF0B211D : TEXT);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(primary
            ? glassShape(c, 0xFF86E6CF, GREEN, 15, 0x99B5FFEB)
            : glassShape(c, 0xFF294453, PANEL2, 15, 0x446D99A7));
        b.setMinHeight(dp(c, 47));
        b.setMinimumHeight(dp(c, 47));
        b.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
        b.setElevation(dp(c, 2));
        return b;
    }

    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    static LinearLayout row(Context c, int direction) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutDirection(direction);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    static LinearLayout.LayoutParams spacedWidth(Context c, int gapDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        int g = dp(c, gapDp);
        p.setMargins(g, 0, g, 0);
        return p;
    }

    static void pad(View v, Context c, int all) {
        int p = dp(c, all);
        v.setPadding(p, p, p, p);
    }

    private static int lighten(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, ((color >>> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >>> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
