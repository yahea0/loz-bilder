
package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class ProjectActivity extends Activity {
    private File project, current;
    private LinearLayout list;
    private EditText search;
    private TextView breadcrumb;
    private BuildProgressPanel buildProgress;
    private BuildProfileDetector.Profile analysis;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("project");
        project = path == null ? null : new File(path);
        if (project == null || !project.isDirectory()) { finish(); return; }
        current = project;
        analysis = BuildProfileDetector.detect(project);
        render();
    }

    private void render() {
        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(Ui.BG);
        Ui.pad(page, this, 14);

        LinearLayout header = Ui.row(this, View.LAYOUT_DIRECTION_RTL);
        LinearLayout names = Ui.column(this);
        names.addView(Ui.text(this, project.getName(), 21, Ui.TEXT, true), matchWidth());
        names.addView(Ui.text(this, "مستكشف المشروع", 12, Ui.MUTED, false), matchWidth());
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView more = Ui.iconButton(this, "⋮");
        LinearLayout.LayoutParams moreP = new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46));
        moreP.setMargins(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
        header.addView(more, moreP);
        more.setOnClickListener(v -> projectMenu(more));
        page.addView(header, matchWidth());

        LinearLayout buildCard = Ui.card(this, Ui.PANEL, 18);
        LinearLayout buildHead = Ui.row(this, View.LAYOUT_DIRECTION_RTL);
        LinearLayout buildInfo = Ui.column(this);
        buildInfo.addView(Ui.text(this, "خطة البناء المختلط", 16, Ui.TEXT, true), matchWidth());
        buildInfo.addView(Ui.text(this, analysis.outputLabel + " • " + analysis.summary, 11, Ui.MUTED, false), matchWidth());
        buildHead.addView(buildInfo, new LinearLayout.LayoutParams(0, -2, 1));
        android.widget.Button inspect = Ui.button(this, "تحليل", true);
        LinearLayout.LayoutParams inspectP = new LinearLayout.LayoutParams(-2, Ui.dp(this, 46));
        inspectP.setMargins(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
        buildHead.addView(inspect, inspectP);
        buildCard.addView(buildHead, matchWidth());
        buildProgress = new BuildProgressPanel(this);
        buildCard.addView(buildProgress, matchWidth());
        TextView note = Ui.text(this,
            "يعرض التحليل كل اللغات والمكوّنات المكتشفة. تنفيذ بناء APK محلياً يحتاج تثبيت محرك Gradle وسلاسل الأدوات المطلوبة؛ لا نعرض تقدماً وهمياً.",
            11, Ui.MUTED, false);
        LinearLayout.LayoutParams noteP = matchWidth(); noteP.topMargin = Ui.dp(this, 6);
        buildCard.addView(note, noteP);
        LinearLayout.LayoutParams buildP = matchWidth(); buildP.setMargins(0, Ui.dp(this, 12), 0, 0);
        page.addView(buildCard, buildP);
        inspect.setOnClickListener(v -> showBuildAnalysis());

        LinearLayout actions = Ui.row(this, View.LAYOUT_DIRECTION_RTL);
        android.widget.Button newFile = Ui.button(this, "+ ملف", false);
        android.widget.Button newFolder = Ui.button(this, "+ مجلد", false);
        android.widget.Button export = Ui.button(this, "تصدير ZIP", false);
        actions.addView(newFile, Ui.spacedWidth(this, 4));
        actions.addView(newFolder, Ui.spacedWidth(this, 4));
        actions.addView(export, Ui.spacedWidth(this, 4));
        LinearLayout.LayoutParams ap = matchWidth(); ap.topMargin = Ui.dp(this, 12);
        page.addView(actions, ap);
        newFile.setOnClickListener(v -> createItem(false));
        newFolder.setOnClickListener(v -> createItem(true));
        export.setOnClickListener(v -> exportProject());

        search = Ui.input(this, "ابحث في أسماء الملفات والمجلدات...");
        LinearLayout.LayoutParams sp = matchWidth(); sp.topMargin = Ui.dp(this, 11);
        page.addView(search, sp);
        breadcrumb = Ui.text(this, "", 12, Ui.GREEN, false);
        LinearLayout.LayoutParams bp = matchWidth(); bp.topMargin = Ui.dp(this, 11);
        page.addView(breadcrumb, bp);

        list = Ui.column(this);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1);
        lp.topMargin = Ui.dp(this, 9);
        page.addView(scroll, lp);
        setContentView(page);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        refresh();
    }

    private void projectMenu(View anchor) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
        menu.getMenu().add("إعادة تحليل المشروع");
        menu.getMenu().add("إعادة تسمية المشروع");
        menu.getMenu().add("حذف المشروع نهائياً");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();
            if (s.equals("إعادة تحليل المشروع")) { analysis = BuildProfileDetector.detect(project); showBuildAnalysis(); }
            else if (s.equals("إعادة تسمية المشروع")) renameProject();
            else confirmDeleteProject();
            return true;
        }); menu.show();
    }

    private void confirmDeleteProject() {
        new AlertDialog.Builder(this).setTitle("حذف المشروع نهائياً؟")
            .setMessage("سيتم حذف «" + project.getName() + "» وكل الملفات والمجلدات داخله. لا يمكن التراجع.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف نهائياً", (d, w) -> {
                ProjectStore.deleteRecursively(project);
                Toast.makeText(this, "تم حذف المشروع", Toast.LENGTH_SHORT).show();
                finish();
            }).show();
    }

    private void renameProject() {
        EditText input = Ui.input(this, "اسم المشروع الجديد"); input.setText(project.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية المشروع").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                File target = new File(project.getParentFile(), ProjectStore.safeName(input.getText().toString()));
                if (target.exists()) { toast("يوجد مشروع بهذا الاسم"); return; }
                if (!project.renameTo(target)) { toast("تعذرت إعادة التسمية"); return; }
                project = target; current = target; analysis = BuildProfileDetector.detect(project); refresh();
            }).show();
    }

    private void showBuildAnalysis() {
        analysis = BuildProfileDetector.detect(project);
        buildProgress.showBlocked("لم يبدأ البناء: محرك التنفيذ المحلي غير مثبّت بعد");
        LinearLayout box = Ui.column(this);
        box.addView(Ui.text(this, "نوع المخرجات: " + analysis.outputLabel, 15, Ui.TEXT, true), matchWidth());
        addDialogLine(box, "اللغات المكتشفة", analysis.languagesText);
        addDialogLine(box, "المكوّنات", analysis.componentsText);
        addDialogLine(box, "بيئة البناء الرئيسية", analysis.toolchain);
        addDialogLine(box, "أمر البناء", analysis.command);
        addDialogLine(box, "التكامل المطلوب", analysis.integrationNotes);
        addDialogLine(box, "تنبيهات", analysis.warnings);
        new AlertDialog.Builder(this).setTitle("تحليل المشروع متعدد اللغات")
            .setView(box).setPositiveButton("حسناً", null).show();
    }

    private void addDialogLine(LinearLayout box, String title, String value) {
        LinearLayout section = Ui.card(this, 0xFF1D303B, 12);
        section.addView(Ui.text(this, title, 12, Ui.GREEN, true), matchWidth());
        TextView body = Ui.text(this, (value == null || value.isEmpty()) ? "غير مكتشف" : value, 12, Ui.TEXT, false);
        body.setTextIsSelectable(true);
        LinearLayout.LayoutParams bp = matchWidth(); bp.topMargin = Ui.dp(this, 4);
        section.addView(body, bp);
        LinearLayout.LayoutParams sp = matchWidth(); sp.setMargins(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        box.addView(section, sp);
    }

    private void refresh() {
        if (list == null || current == null) return;
        list.removeAllViews();
        String relative = project.toURI().relativize(current.toURI()).getPath();
        breadcrumb.setText("المشروع  /  " + (relative.isEmpty() ? "الجذر" : relative.replace("/", "  /  ")));
        if (!current.equals(project)) {
            android.widget.Button parent = Ui.button(this, "↑  المجلد الأعلى", false);
            LinearLayout.LayoutParams p = matchWidth(); p.setMargins(0, 0, 0, Ui.dp(this, 9));
            list.addView(parent, p);
            parent.setOnClickListener(v -> { current = current.getParentFile(); refresh(); });
        }

        File[] files = current.listFiles();
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        String q = search == null ? "" : search.getText().toString().trim().toLowerCase();
        int shown = 0;
        for (File f : files) {
            if (!q.isEmpty() && !f.getName().toLowerCase().contains(q)) continue;
            shown++;
            LinearLayout row = Ui.card(this, Ui.PANEL, 15);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            GlassFileIconView icon = new GlassFileIconView(this, f.getName(), f.isDirectory());
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(Ui.dp(this, 49), Ui.dp(this, 49));
            ip.setMargins(0, 0, Ui.dp(this, 8), 0);
            row.addView(icon, ip);

            LinearLayout details = Ui.column(this);
            details.addView(Ui.text(this, f.getName(), 14, Ui.TEXT, true), matchWidth());
            details.addView(Ui.text(this, f.isDirectory() ? "مجلد" : ProjectStore.humanSize(f.length()), 11, Ui.MUTED, false), matchWidth());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(0, -2, 1);
            dp.setMargins(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
            row.addView(details, dp);

            TextView more = Ui.iconButton(this, "⋮");
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 42));
            mp.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
            row.addView(more, mp);

            LinearLayout.LayoutParams rp = matchWidth(); rp.setMargins(0, 0, 0, Ui.dp(this, 8));
            list.addView(row, rp);
            row.setOnClickListener(v -> { if (f.isDirectory()) { current = f; refresh(); } else openFile(f); });
            more.setOnClickListener(v -> itemMenu(more, f));
            row.setOnLongClickListener(v -> { itemMenu(more, f); return true; });
        }
        if (shown == 0) list.addView(Ui.text(this, q.isEmpty() ? "هذا المجلد فارغ." : "لا توجد نتائج مطابقة.", 13, Ui.MUTED, false), matchWidth());
    }

    private void itemMenu(View anchor, File f) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(this, anchor);
        if (f.isFile()) menu.getMenu().add("فتح وتعديل");
        menu.getMenu().add("إعادة تسمية"); menu.getMenu().add("حذف");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();
            if (s.equals("فتح وتعديل")) openFile(f);
            else if (s.equals("إعادة تسمية")) rename(f);
            else confirmDelete(f);
            return true;
        }); menu.show();
    }

    private void createItem(boolean directory) {
        EditText input = Ui.input(this, directory ? "اسم المجلد" : "مثلاً Main.java");
        new AlertDialog.Builder(this).setTitle(directory ? "إنشاء مجلد" : "إنشاء ملف").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("إنشاء", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) { toast("اسم غير صالح"); return; }
                File target = new File(current, name);
                if (target.exists()) { toast("هذا الاسم موجود بالفعل"); return; }
                boolean ok;
                if (directory) ok = target.mkdirs();
                else { try { ok = target.createNewFile(); } catch (Exception e) { ok = false; } }
                if (!ok) { toast("تعذر إنشاء العنصر"); return; }
                refresh(); if (!directory) openFile(target);
            }).show();
    }

    private void openFile(File f) {
        if (f.length() > 5L * 1024 * 1024) { toast("الملف أكبر من 5MB؛ المحرر النصي لا يفتحه حفاظاً على أداء الهاتف."); return; }
        Intent i = new Intent(this, EditorActivity.class);
        i.putExtra("file", f.getAbsolutePath());
        startActivity(i);
    }

    private void rename(File f) {
        EditText input = Ui.input(this, "الاسم الجديد"); input.setText(f.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) { toast("اسم غير صالح"); return; }
                File target = new File(f.getParentFile(), name);
                if (target.exists()) { toast("الاسم موجود بالفعل"); return; }
                if (!f.renameTo(target)) toast("تعذرت إعادة التسمية");
                refresh();
            }).show();
    }

    private void confirmDelete(File f) {
        new AlertDialog.Builder(this).setTitle("حذف العنصر؟")
            .setMessage("سيتم حذف «" + f.getName() + "» وكل محتوياته إن كان مجلداً.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف", (d, w) -> { ProjectStore.deleteRecursively(f); refresh(); }).show();
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
                try { ProjectStore.exportZip(this, project, data.getData()); runOnUiThread(() -> toast("تم تصدير المشروع")); }
                catch (Exception e) { runOnUiThread(() -> toast("فشل التصدير: " + e.getMessage())); }
            }).start();
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private LinearLayout.LayoutParams matchWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    @Override public void onBackPressed() { if (!current.equals(project)) { current = current.getParentFile(); refresh(); } else super.onBackPressed(); }
}
