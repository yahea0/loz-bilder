package com.luzbuild.studio;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {

    static final int BG = 0xFF101820;
    static final int PANEL = 0xFF192630;
    static final int PANEL2 = 0xFF213541;
    static final int TEXT = 0xFFF1F6F8;
    static final int MUTED = 0xFF9FB2BD;
    static final int GREEN = 0xFF69D5B4;
    static final int ORANGE = 0xFFFFC078;

    private Ui() {}

    static GradientDrawable shape(
        int color,
        int radiusDp,
        Context c
    ) {

        GradientDrawable d =
            new GradientDrawable();

        d.setColor(color);

        d.setCornerRadius(
            dp(c, radiusDp)
        );

        return d;
    }

    static int dp(Context c, int v) {

        return (int) (
            v *
            c.getResources()
                .getDisplayMetrics()
                .density
            + 0.5f
        );
    }

    static TextView text(
        Context c,
        String s,
        int size,
        int color,
        boolean bold
    ) {

        TextView t = new TextView(c);

        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);

        t.setGravity(
            Gravity.RIGHT |
            Gravity.CENTER_VERTICAL
        );

        t.setTextDirection(
            View.TEXT_DIRECTION_FIRST_STRONG_RTL
        );

        if (bold) {
            t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
            );
        }

        return t;
    }

    static Button button(
        Context c,
        String label,
        boolean primary
    ) {

        Button b = new Button(c);

        b.setText(label);
        b.setTextSize(14);
        b.setAllCaps(false);

        b.setTextColor(
            primary ? BG : TEXT
        );

        b.setBackground(
            shape(
                primary ? GREEN : PANEL2,
                14,
                c
            )
        );

        b.setMinHeight(
            dp(c, 48)
        );

        b.setPadding(
            dp(c, 14),
            dp(c, 8),
            dp(c, 14),
            dp(c, 8)
        );

        return b;
    }

    static LinearLayout column(Context c) {

        LinearLayout l = new LinearLayout(c);

        l.setOrientation(
            LinearLayout.VERTICAL
        );

        l.setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        return l;
    }

    static void pad(
        View v,
        Context c,
        int all
    ) {

        int p = dp(c, all);

        v.setPadding(p, p, p, p);
    }
}
