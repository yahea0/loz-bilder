
package com.luzbuild.studio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class CodeEditorView extends EditText {
    public interface SelectionListener {
        void onSelectionChanged();
    }

    private final Paint gutterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private SelectionListener selectionListener;
    private final Rect rect = new Rect();

    public CodeEditorView(Context c) {
        super(c);
        init();
    }

    public CodeEditorView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    public CodeEditorView(Context c, AttributeSet a, int style) {
        super(c, a, style);
        init();
    }

    private void init() {
        setTextDirection(View.TEXT_DIRECTION_LTR);
        setIncludeFontPadding(false);
        setHorizontallyScrolling(true);

        gutterPaint.setColor(0xFF101B24);

        numberPaint.setColor(0xFF718795);
        numberPaint.setTextSize(getTextSize() * 0.88f);
        numberPaint.setTypeface(Typeface.MONOSPACE);
        numberPaint.setTextAlign(Paint.Align.RIGHT);

        activePaint.setColor(0x223ED6B1);

        dividerPaint.setColor(0xFF243743);
        dividerPaint.setStrokeWidth(getResources().getDisplayMetrics().density);
    }

    public void setSelectionListener(SelectionListener l) {
        selectionListener = l;
    }

    @Override protected void onDraw(Canvas canvas) {
        float density = getResources().getDisplayMetrics().density;
        float gutter = 49f * density;

        canvas.drawRect(
            0,
            getScrollY(),
            gutter,
            getScrollY() + getHeight(),
            gutterPaint
        );

        android.text.Layout layout = getLayout();

        if (layout != null) {
            int first = layout.getLineForVertical(getScrollY());
            int last = layout.getLineForVertical(getScrollY() + getHeight());

            int activeLine = getSelectionStart() >= 0
                ? layout.getLineForOffset(getSelectionStart())
                : -1;

            for (int line = first; line <= last && line < layout.getLineCount(); line++) {
                int top = layout.getLineTop(line) + getExtendedPaddingTop();
                int bottom = layout.getLineBottom(line) + getExtendedPaddingTop();

                if (line == activeLine) {
                    canvas.drawRect(
                        gutter + getScrollX(),
                        top,
                        getWidth() + getScrollX(),
                        bottom,
                        activePaint
                    );
                }

                float baseline = layout.getLineBaseline(line) + getExtendedPaddingTop();
                canvas.drawText(
                    String.valueOf(line + 1),
                    gutter - 10f * density,
                    baseline,
                    numberPaint
                );
            }
        }

        canvas.drawLine(
            gutter,
            getScrollY(),
            gutter,
            getScrollY() + getHeight(),
            dividerPaint
        );

        super.onDraw(canvas);
    }

    @Override public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
            event.getAction() == android.view.KeyEvent.ACTION_DOWN) {

            int cursor = Math.max(0, getSelectionStart());
            android.text.Editable editable = getText();
            String all = editable.toString();

            int lineStart = all.lastIndexOf('\n', Math.max(0, cursor - 1)) + 1;
            int i = lineStart;

            while (i < all.length() &&
                (all.charAt(i) == ' ' || all.charAt(i) == '\t')) {
                i++;
            }

            String indent = all.substring(lineStart, i);

            boolean handled = super.onKeyDown(keyCode, event);

            if (handled && !indent.isEmpty()) {
                post(() -> getText().insert(Math.max(0, getSelectionStart()), indent));
            }

            return handled;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        invalidate();

        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }
}
