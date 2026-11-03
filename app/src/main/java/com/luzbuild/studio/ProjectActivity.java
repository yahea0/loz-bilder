package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class ProjectActivity extends Activity {

    private File project;
    private File current;
    private LinearLayout list;
    private EditText search;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        String path = getIntent().getStringExtra("project");

        project = path == null ? null : new File(path);

        if (project == null || !project.isDirectory()) {
            finish();
            return;
        }

        current = project;
        render();
    }

    private void render() {

        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(Ui.BG);
        Ui.pad(page, this, 14);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout names = Ui.column(this);

        names.addView(
            Ui.text(this, project.getName(),
                21, Ui.TEXT, true)
        );

        names.addView(
            Ui.text(this, "مستكشف الملفات",
                12, Ui.MUTED, false)
        );

        header.addView(
            names,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        android.widget.Button back =
            Ui.button(this, "رجوع", false);

        header.addView(
            back,
            new LinearLayout.LayoutParams(-2, -2)
        );

        back.setOnClickListener(v -> finish());

        page.addView(
            header,
            new LinearLayout.LayoutParams(-1, -2)
        );

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        android.widget.Button build =
            Ui.button(this, "بناء APK", true);

        android.widget.Button export =
            Ui.button(this, "تصدير ZIP", false);

        actions.addView(
            build,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        LinearLayout.LayoutParams ep =
            new LinearLayout.LayoutParams(0, -2, 1);

        ep.setMargins(Ui.dp(this, 7), 0, 0, 0);

        actions.addView(export, ep);

        LinearLayout.LayoutParams ap =
            new LinearLayout.LayoutParams(-1, -2);

        ap.topMargin = Ui.dp(this, 14);

        page.addView(actions, ap);

        build.setOnClickListener(v -> showBuildInfo());
        export.setOnClickListener(v -> exportProject());

        LinearLayout createActions = new LinearLayout(this);

        createActions.setOrientation(
            LinearLayout.HORIZONTAL
        );

        createActions.setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        android.widget.Button newFile =
            Ui.button(this, "+ ملف", false);

        android.widget.Button newFolder =
            Ui.button(this, "+ مجلد", false);

        createActions.addView(
            newFile,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        LinearLayout.LayoutParams nfp =
            new LinearLayout.LayoutParams(0, -2, 1);

        nfp.setMargins(Ui.dp(this, 7), 0, 0, 0);

        createActions.addView(newFolder, nfp);

        LinearLayout.LayoutParams cap =
            new LinearLayout.LayoutParams(-1, -2);

        cap.topMargin = Ui.dp(this, 8);

        page.addView(createActions, cap);

        newFile.setOnClickListener(
            v -> createItem(false)
        );

        newFolder.setOnClickListener(
            v -> createItem(true)
        );

        search = new EditText(this);

        search.setSingleLine(true);
        search.setTextColor(Ui.TEXT);
        search.setHintTextColor(Ui.MUTED);
        search.setHint("ابحث عن ملف أو مجلد...");

        search.setBackground(
            Ui.shape(Ui.PANEL, 12, this)
        );

        search.setPadding(
            Ui.dp(this, 12),
            0,
            Ui.dp(this, 12),
            0
        );

        LinearLayout.LayoutParams sp =
            new LinearLayout.LayoutParams(
                -1,
                Ui.dp(this, 48)
            );

        sp.topMargin = Ui.dp(this, 12);

        page.addView(search, sp);

        search.addTextChangedListener(
            new TextWatcher() {

                public void beforeTextChanged(
                    CharSequence s,
                    int st,
                    int c,
                    int a
                ) {}

                public void onTextChanged(
                    CharSequence s,
                    int st,
                    int before,
                    int count
                ) {
                    refresh();
                }

                public void afterTextChanged(
                    Editable e
                ) {}
            }
        );

        list = Ui.column(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(list);

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1, 0, 1);

        lp.topMargin = Ui.dp(this, 10);

        page.addView(scroll, lp);

        setContentView(page);

        refresh();
    }

    private void refresh() {

        if (list == null || current == null) return;

        list.removeAllViews();

        if (!current.equals(project)) {

            android.widget.Button parent =
                Ui.button(this, "⬆  المجلد الأعلى", false);

            LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(-1, -2);

            pp.bottomMargin = Ui.dp(this, 8);

            list.addView(parent, pp);

            parent.setOnClickListener(v -> {
                current = current.getParentFile();
                refresh();
            });
        }

        File[] files = current.listFiles();

        if (files == null) return;

        Arrays.sort(
            files,
            Comparator.comparing(File::isFile)
                .thenComparing(
                    File::getName,
                    String.CASE_INSENSITIVE_ORDER
                )
        );

        String q = search == null
            ? ""
            : search.getText().toString()
                .trim().toLowerCase();

        for (File f : files) {

            if (!q.isEmpty() &&
                !f.getName().toLowerCase().contains(q)) {
                continue;
            }

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
            );

            row.setBackground(
                Ui.shape(Ui.PANEL, 12, this)
            );

            Ui.pad(row, this, 12);

            String icon = f.isDirectory()
                ? "📁"
                : fileIcon(f.getName());

            LinearLayout details = Ui.column(this);

            details.addView(
                Ui.text(this,
                    icon + "  " + f.getName(),
                    14, Ui.TEXT, true)
            );

            details.addView(
                Ui.text(this,
                    f.isDirectory()
                        ? "مجلد"
                        : humanSize(f.length()),
                    11, Ui.MUTED, false)
            );

            row.addView(
                details,
                new LinearLayout.LayoutParams(0, -2, 1)
            );

            list.addView(row, rowParams());

            row.setOnClickListener(v -> {

                if (f.isDirectory()) {
                    current = f;
                    refresh();
                } else {
                    openFile(f);
                }
            });

            row.setOnLongClickListener(v -> {

                new AlertDialog.Builder(this)
                    .setTitle(f.getName())
                    .setItems(
                        new String[]{
                            "إعادة تسمية",
                            "حذف"
                        },
                        (d, which) -> {
                            if (which == 0) {
                                rename(f);
                            } else {
                                confirmDelete(f);
                            }
                        }
                    )
                    .show();

                return true;
            });
        }

        if (list.getChildCount() == 0) {

            list.addView(
                Ui.text(this,
                    "لا توجد عناصر في هذا المجلد.",
                    14, Ui.MUTED, false)
            );
        }
    }

    private void createItem(boolean directory) {

        EditText input = new EditText(this);

        input.setSingleLine(true);

        input.setHint(
            directory
                ? "اسم المجلد"
                : "مثلاً NewClass.java"
        );

        new AlertDialog.Builder(this)
            .setTitle(
                directory
                    ? "إنشاء مجلد"
                    : "إنشاء ملف"
            )
            .setView(input)
            .setPositiveButton("إنشاء", (d, w) -> {

                String name = input.getText()
                    .toString()
                    .trim();

                if (name.isEmpty() ||
                    name.equals(".") ||
                    name.equals("..") ||
                    name.contains("/") ||
                    name.contains("\\")) {

                    Toast.makeText(
                        this,
                        "اسم غير صالح",
                        Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                File target = new File(current, name);

                if (target.exists()) {

                    Toast.makeText(
                        this,
                        "هذا الاسم موجود بالفعل",
                        Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean ok = directory
                    ? target.mkdirs()
                    : createEmptyFile(target);

                if (!ok) {

                    Toast.makeText(
                        this,
                        "تعذر إنشاء العنصر",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                refresh();

                if (!directory && target.isFile()) {
                    openFile(target);
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private boolean createEmptyFile(File f) {

        try {
            return f.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }

    private void openFile(File f) {

        if (f.length() > 2 * 1024 * 1024) {

            Toast.makeText(
                this,
                "الملف أكبر من 2MB؛ فتحه في المحرر النصي غير مناسب حالياً.",
                Toast.LENGTH_LONG
            ).show();

            return;
        }

        Intent i = new Intent(
            this,
            EditorActivity.class
        );

        i.putExtra(
            "file",
            f.getAbsolutePath()
        );

        startActivity(i);
    }

    private void rename(File f) {

        EditText input = new EditText(this);

        input.setSingleLine(true);
        input.setText(f.getName());

        new AlertDialog.Builder(this)
            .setTitle("إعادة تسمية")
            .setView(input)
            .setPositiveButton("حفظ", (d, w) -> {

                String name = input.getText()
                    .toString()
                    .trim();

                if (name.isEmpty() ||
                    name.contains("/")) {
                    return;
                }

                File target = new File(
                    f.getParentFile(),
                    name
                );

                if (target.exists()) {

                    Toast.makeText(
                        this,
                        "الاسم موجود بالفعل",
                        Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (!f.renameTo(target)) {

                    Toast.makeText(
                        this,
                        "تعذرت إعادة التسمية",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                refresh();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void confirmDelete(File f) {

        new AlertDialog.Builder(this)
            .setTitle("حذف " + f.getName() + "؟")
            .setMessage("لا يمكن التراجع عن الحذف.")
            .setPositiveButton("حذف", (d, w) -> {

                ProjectStore.deleteRecursively(f);
                refresh();

            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showBuildInfo() {

        new AlertDialog.Builder(this)
            .setTitle("محرك البناء المحلي")
            .setMessage(
                "هذه النسخة تحفظ المشاريع وتسمح بتعديل ملفاتها. " +
                "زر بناء APK سيتصل بمحرك مستقل داخل التطبيق.\n\n" +

                "المطلوب للمحرك: بيئة Linux ARM64 + PRoot + " +
                "JDK + Gradle + Android SDK/Build Tools. " +
                "تُنزّل مرة واحدة داخل التطبيق ثم تُعاد " +
                "الاستفادة من Gradle cache في كل بناء.\n\n" +

                "لم يتم تفعيل البناء بعد، لذلك لن نعرض نجاحاً وهمياً."
            )
            .setPositiveButton("حسناً", null)
            .show();
    }

    private void exportProject() {

        Intent i = new Intent(
            Intent.ACTION_CREATE_DOCUMENT
        );

        i.addCategory(
            Intent.CATEGORY_OPENABLE
        );

        i.setType("application/zip");

        i.putExtra(
            Intent.EXTRA_TITLE,
            project.getName() + ".zip"
        );

        startActivityForResult(i, 33);
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        );

        if (requestCode == 33 &&
            resultCode == RESULT_OK &&
            data != null &&
            data.getData() != null) {

            new Thread(() -> {

                try {

                    ProjectStore.exportZip(
                        this,
                        project,
                        data.getData()
                    );

                    runOnUiThread(() ->
                        Toast.makeText(
                            this,
                            "تم تصدير المشروع",
                            Toast.LENGTH_LONG
                        ).show()
                    );

                } catch (Exception e) {

                    runOnUiThread(() ->
                        Toast.makeText(
                            this,
                            "فشل التصدير: " + e.getMessage(),
                            Toast.LENGTH_LONG
                        ).show()
                    );
                }

            }).start();
        }
    }

    private String fileIcon(String n) {

        String x = n.toLowerCase();

        if (x.endsWith(".java")) return "☕";
        if (x.endsWith(".kt")) return "🟣";
        if (x.endsWith(".xml")) return "🧩";

        if (x.endsWith(".gradle") ||
            x.endsWith(".kts")) {
            return "⚙";
        }

        if (x.endsWith(".png") ||
            x.endsWith(".jpg") ||
            x.endsWith(".webp")) {
            return "🖼";
        }

        return "📄";
    }

    private String humanSize(long n) {

        if (n < 1024) return n + " B";

        if (n < 1024 * 1024) {
            return String.format("%.1f KB", n / 1024f);
        }

        return String.format("%.1f MB", n / 1048576f);
    }

    private LinearLayout.LayoutParams rowParams() {

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);

        p.bottomMargin = Ui.dp(this, 7);

        return p;
    }
}
