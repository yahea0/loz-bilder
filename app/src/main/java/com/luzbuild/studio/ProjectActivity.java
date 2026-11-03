
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
    private File project, current;
    private LinearLayout list;
    private EditText search;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("project");
        project = path == null ? null : new File(path);
        if (project == null || !project.isDirectory()) { finish(); return; }
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
        names.addView(Ui.text(this, project.getName(), 21, Ui.TEXT, true));
        names.addView(Ui.text(this, "مستكشف الملفات", 12, Ui.MUTED, false));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        android.widget.Button back = Ui.button(this, "رجوع", false);
        header.addView(back, new LinearLayout.LayoutParams(-2, -2));
        back.setOnClickListener(v -> finish());
        page.addView(header, matchWidth());

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        android.widget.Button build = Ui.button(this, "البناء", true);
        android.widget.Button export = Ui.button(this, "تصدير ZIP", false);
        actions.addView(build, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(0, -2, 1);
        xp.setMargins(Ui.dp(this, 7), 0, 0, 0);
        actions.addView(export, xp);
        LinearLayout.LayoutParams ap = matchWidth(); ap.topMargin = Ui.dp(this, 13);
        page.addView(actions, ap);
        build.setOnClickListener(v -> showBuildInfo());
        export.setOnClickListener(v -> exportProject());

        LinearLayout newActions = new LinearLayout(this);
        newActions.setOrientation(LinearLayout.HORIZONTAL);
        newActions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        android.widget.Button newFile = Ui.button(this, "+ ملف", false);
        android.widget.Button newFolder = Ui.button(this, "+ مجلد", false);
        newActions.addView(newFile, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams nfp = new LinearLayout.LayoutParams(0, -2, 1);
        nfp.setMargins(Ui.dp(this, 7), 0, 0, 0);
        newActions.addView(newFolder, nfp);
        LinearLayout.LayoutParams np = matchWidth(); np.topMargin = Ui.dp(this, 8);
        page.addView(newActions, np);
        newFile.setOnClickListener(v -> createItem(false));
        newFolder.setOnClickListener(v -> createItem(true));

        search = Ui.input(this, "بحث في أسماء الملفات...");
        LinearLayout.LayoutParams sp = matchWidth(); sp.topMargin = Ui.dp(this, 11);
        page.addView(search, sp);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable e) {}
        });

        list = Ui.column(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1);
        lp.topMargin = Ui.dp(this, 10);
        page.addView(scroll, lp);
        setContentView(page);
        refresh();
    }

    private void refresh() {
        if (list == null || current == null) return;
        list.removeAllViews();
        if (!current.equals(project)) {
            android.widget.Button parent = Ui.button(this, "↑ المجلد الأعلى", false);
            LinearLayout.LayoutParams pp = matchWidth(); pp.bottomMargin = Ui.dp(this, 8);
            list.addView(parent, pp);
            parent.setOnClickListener(v -> { current = current.getParentFile(); refresh(); });
        }

        File[] files = current.listFiles();
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        String q = search == null ? "" : search.getText().toString().trim().toLowerCase();

        for (File f : files) {
            if (!q.isEmpty() && !f.getName().toLowerCase().contains(q)) continue;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.shape(Ui.PANEL, 12, this));
            Ui.pad(row, this, 12);

            String icon = f.isDirectory() ? "📁" : fileIcon(f.getName());
            LinearLayout details = Ui.column(this);
            details.addView(Ui.text(this, icon + "  " + f.getName(), 14, Ui.TEXT, true));
            details.addView(Ui.text(this, f.isDirectory() ? "مجلد" : ProjectStore.humanSize(f.length()), 11, Ui.MUTED, false));
            row.addView(details, new LinearLayout.LayoutParams(0, -2, 1));
            list.addView(row, rowParams());

            row.setOnClickListener(v -> {
                if (f.isDirectory()) { current = f; refresh(); }
                else openFile(f);
            });
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this).setTitle(f.getName())
                    .setItems(new String[]{"إعادة تسمية", "حذف"}, (d, which) -> {
                        if (which == 0) rename(f); else confirmDelete(f);
                    }).show();
                return true;
            });
        }
        if (list.getChildCount() == 0) list.addView(Ui.text(this, "لا توجد عناصر في هذا المجلد.", 14, Ui.MUTED, false));
    }

    private void createItem(boolean directory) {
        EditText input = Ui.input(this, directory ? "اسم المجلد" : "اسم الملف");
        new AlertDialog.Builder(this).setTitle(directory ? "إنشاء مجلد" : "إنشاء ملف").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("إنشاء", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) {
                    toast("اسم غير صالح"); return;
                }
                File target = new File(current, name);
                if (target.exists()) { toast("هذا الاسم موجود بالفعل"); return; }
                boolean ok = directory ? target.mkdirs() : createEmptyFile(target);
                if (!ok) { toast("تعذر إنشاء العنصر"); return; }
                refresh();
                if (!directory) openFile(target);
            }).show();
    }

    private boolean createEmptyFile(File f) {
        try { return f.createNewFile(); } catch (Exception e) { return false; }
    }

    private void openFile(File f) {
        if (f.length() > 5L * 1024 * 1024) {
            toast("الملف أكبر من 5MB؛ لا يمكن فتحه في المحرر الحالي.");
            return;
        }
        Intent i = new Intent(this, EditorActivity.class);
        i.putExtra("file", f.getAbsolutePath());
        startActivity(i);
    }

    private void rename(File f) {
        EditText input = Ui.input(this, "الاسم الجديد");
        input.setText(f.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) {
                    toast("اسم غير صالح"); return;
                }
                File target = new File(f.getParentFile(), name);
                if (target.exists()) { toast("الاسم موجود بالفعل"); return; }
                if (!f.renameTo(target)) toast("تعذرت إعادة التسمية");
                refresh();
            }).show();
    }

    private void confirmDelete(File f) {
        new AlertDialog.Builder(this).setTitle("حذف العنصر؟")
            .setMessage("سيتم حذف «" + f.getName() + "» نهائياً.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف", (d, w) -> { ProjectStore.deleteRecursively(f); refresh(); })
            .show();
    }

    private void showBuildInfo() {
        BuildProfileDetector.Profile profile = BuildProfileDetector.detect(project);
        LinearLayout box = Ui.column(this);
        box.addView(Ui.text(this, "نوع المشروع: " + profile.name, 16, Ui.TEXT, true));
        box.addView(Ui.text(this, "الأدوات المطلوبة: " + profile.tools, 13, Ui.MUTED, false));
        box.addView(Ui.text(this, profile.note, 12, Ui.ORANGE, false));
        new AlertDialog.Builder(this).setTitle("تحليل المشروع").setView(box).setPositiveButton("حسناً", null).show();
    }

    private void exportProject() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, project.getName() + ".zip");
        startActivityForResult(i, 33);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            new Thread(() -> {
                try {
                    ProjectStore.exportZip(this, project, data.getData());
                    runOnUiThread(() -> toast("تم تصدير المشروع"));
                } catch (Exception e) { runOnUiThread(() -> toast("فشل التصدير: " + e.getMessage())); }
            }).start();
        }
    }

    private String fileIcon(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".java")) return "☕";
        if (n.endsWith(".kt") || n.endsWith(".kts")) return "🟣";
        if (n.endsWith(".xml") || n.endsWith(".html")) return "🧩";
        if (n.endsWith(".gradle") || n.endsWith(".properties")) return "⚙";
        if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".webp") || n.endsWith(".svg")) return "🖼";
        if (n.endsWith(".py")) return "🐍";
        if (n.endsWith(".js") || n.endsWith(".ts")) return "🟨";
        return "📄";
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams p = matchWidth();
        p.bottomMargin = Ui.dp(this, 7);
        return p;
    }
    private LinearLayout.LayoutParams matchWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
