
package com.luzbuild.studio;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Presentation component for real build events.
 * The build adapter must call setProgress only when it receives measurable progress.
 * It intentionally does not animate or invent percentages by itself.
 */
final class BuildProgressPanel extends LinearLayout {
    private final ProgressBar bar;
    private final TextView status;
    private final TextView percent;

    BuildProgressPanel(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setLayoutDirection(LAYOUT_DIRECTION_RTL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(Ui.dp(context, 2), Ui.dp(context, 10), Ui.dp(context, 2), Ui.dp(context, 2));

        LinearLayout line = new LinearLayout(context);
        line.setOrientation(HORIZONTAL);
        line.setLayoutDirection(LAYOUT_DIRECTION_RTL);
        line.setGravity(Gravity.CENTER_VERTICAL);

        status = Ui.text(context, "بانتظار محرك البناء المحلي", 11, Ui.MUTED, false);
        line.addView(status, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

        percent = Ui.text(context, "—", 12, Ui.GREEN, true);
        line.addView(percent, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(line, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.GREEN));
        bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.PANEL2));

        LayoutParams bp = new LayoutParams(LayoutParams.MATCH_PARENT, Ui.dp(context, 6));
        bp.topMargin = Ui.dp(context, 8);
        addView(bar, bp);
    }

    void reset(String message) {
        bar.setIndeterminate(false);
        bar.setProgress(0);
        status.setText(message);
        percent.setText("—");
    }

    void showPreparing(String message) {
        bar.setIndeterminate(true);
        status.setText(message);
        percent.setText("...");
    }

    void setProgress(int value, String message) {
        int safe = Math.max(0, Math.min(100, value));
        bar.setIndeterminate(false);
        bar.setProgress(safe);
        status.setText(message);
        percent.setText(safe + "%");
    }

    void showBlocked(String message) {
        bar.setIndeterminate(false);
        bar.setProgress(0);
        status.setText(message);
        percent.setText("لم يبدأ");
    }

    void showFinished(boolean success, String message) {
        bar.setIndeterminate(false);
        bar.setProgress(success ? 100 : bar.getProgress());
        status.setText(message);
        percent.setText(success ? "100%" : "فشل");
    }
}
