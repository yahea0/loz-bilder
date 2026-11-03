
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

    private Ui() {}

    static GradientDrawable shape(int color, int radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    static TextView text(Context c, String s, int size, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    static TextView pill(Context c, String label, int textColor, int background) {
        TextView t = text(c, label, 10, textColor, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
        t.setBackground(shape(background, 30, c));
        return t;
    }

    static TextView iconButton(Context c, String label) {
        TextView t = text(c, label, 21, TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(shape(PANEL2, 12, c));
        return t;
    }

    static LinearLayout card(Context c, int color, int radius) {
        LinearLayout l = column(c);
        l.setBackground(shape(color, radius, c));
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
        e.setBackground(shape(PANEL2, 12, c));
        e.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        return e;
    }

    static Button button(Context c, String label, boolean primary) {
        Button b = new Button(c);
        b.setText(label); b.setTextSize(13); b.setAllCaps(false);
        b.setTextColor(primary ? BG : TEXT);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(primary ? GREEN : PANEL2, 14, c));
        b.setMinHeight(dp(c, 45));
        b.setMinimumHeight(dp(c, 45));
        b.setPadding(dp(c, 12), dp(c, 7), dp(c, 12), dp(c, 7));
        return b;
    }

    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    static void pad(View v, Context c, int all) {
        int p = dp(c, all); v.setPadding(p, p, p, p);
    }
}
