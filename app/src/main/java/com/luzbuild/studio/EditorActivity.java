package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class EditorActivity extends Activity {

    private File file;
    private EditText editor;
    private boolean changed = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        String path = getIntent().getStringExtra("file");

        file = path == null ? null : new File(path);

        if (file == null || !file.isFile()) {
            finish();
            return;
        }

        render();
        load();
    }

    private void render() {

        LinearLayout page = Ui.column(this);

        page.setBackgroundColor(Ui.BG);
        Ui.pad(page, this, 12);

        LinearLayout top = new LinearLayout(this);

        top.setOrientation(
            LinearLayout.HORIZONTAL
        );

        top.setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        LinearLayout title = Ui.column(this);

        title.addView(
            Ui.text(this, file.getName(),
                16, Ui.TEXT, true)
        );

        title.addView(
            Ui.text(this, "محرر نصوص بسيط",
                11, Ui.MUTED, false)
        );

        top.addView(
            title,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        android.widget.Button save =
            Ui.button(this, "حفظ", true);

        top.addView(
            save,
            new LinearLayout.LayoutParams(-2, -2)
        );

        save.setOnClickListener(v -> save());

        page.addView(
            top,
            new LinearLayout.LayoutParams(-1, -2)
        );

        editor = new EditText(this);

        editor.setGravity(
            android.view.Gravity.TOP |
            android.view.Gravity.LEFT
        );

        editor.setTextColor(0xFFE4EEF2);
        editor.setHintTextColor(Ui.MUTED);
        editor.setTextSize(13);

        editor.setTypeface(
            android.graphics.Typeface.MONOSPACE
        );

        editor.setInputType(
            InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_FLAG_MULTI_LINE |
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(true);

        editor.setBackground(
            Ui.shape(0xFF0D151C, 12, this)
        );

        editor.setPadding(
            Ui.dp(this, 12),
            Ui.dp(this, 12),
            Ui.dp(this, 12),
            Ui.dp(this, 12)
        );

        ScrollView scroll = new ScrollView(this);

        scroll.addView(editor);

        LinearLayout.LayoutParams ep =
            new LinearLayout.LayoutParams(-1, 0, 1);

        ep.topMargin = Ui.dp(this, 12);

        page.addView(scroll, ep);

        editor.addTextChangedListener(
            new android.text.TextWatcher() {

                public void beforeTextChanged(
                    CharSequence s,
                    int st,
                    int c,
                    int a
                ) {}

                public void onTextChanged(
                    CharSequence s,
                    int st,
                    int b,
                    int c
                ) {
                    changed = true;
                }

                public void afterTextChanged(
                    android.text.Editable e
                ) {}
            }
        );

        setContentView(page);
    }

    private void load() {

        try {

            editor.setText(
                new String(
                    Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8
                )
            );

            changed = false;

        } catch (Exception e) {

            Toast.makeText(
                this,
                "تعذر فتح الملف: " + e.getMessage(),
                Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    private void save() {

        try {

            Files.write(
                file.toPath(),
                editor.getText()
                    .toString()
                    .getBytes(StandardCharsets.UTF_8)
            );

            changed = false;

            Toast.makeText(
                this,
                "تم حفظ التعديلات",
                Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                this,
                "فشل الحفظ: " + e.getMessage(),
                Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onBackPressed() {

        if (!changed) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("توجد تعديلات غير محفوظة")
            .setMessage("هل تريد حفظها قبل الخروج؟")

            .setPositiveButton("حفظ", (d, w) -> {

                save();

                if (!changed) {
                    finish();
                }
            })

            .setNegativeButton(
                "خروج دون حفظ",
                (d, w) -> finish()
            )

            .setNeutralButton(
                "متابعة التحرير",
                null
            )

            .show();
    }
}
