
package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.PopupMenu;

import java.io.File;

public class MainActivity extends Activity {
    private static final int PICK_ZIP = 10;
    private static final int PICK_FOLDER = 11;
    private String pendingName = "ImportedProject";
    private LinearLayout projectsBox;
    private EditText projectSearch;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        render();
    }

    private void render() {
        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(Ui.BG);
        Ui.pad(page, this, 18);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = Ui.column(this);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout brand = Ui.column(this);
        brand.addView(Ui.text(this, "لوز بيلدر", 31, Ui.TEXT, true));
        brand.addView(Ui.text(this, "استوديو تطوير وبناء المشاريع من هاتفك", 13, Ui.MUTED, false));
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = Ui.pill(this, "نسخة 0.2", Ui.GREEN, 0xFF0C2824);
        top.addView(version, new LinearLayout.LayoutParams(-2, -2));
        content.addView(top, matchWidth());
        addGap(content, 20);

        LinearLayout hero = Ui.card(this, Ui.PANEL, 22);
        hero.addView(Ui.text(this, "مساحة عملك البرمجية", 20, Ui.TEXT, true));
        addGap(hero, 7);
        hero.addView(Ui.text(this,
            "استورد مشاريعك، رتّب ملفاتك، وابحث وعدّل الكود في محرر يدعم ألوان الصياغة وترقيم الأسطر.",
            14, Ui.MUTED, false));
        content.addView(hero, matchWidth());
        addGap(content, 14);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        android.widget.Button zip = Ui.button(this, "استيراد ZIP", true);
        android.widget.Button folder = Ui.button(this, "استيراد مجلد", false);
        actions.addView(zip, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, -2, 1);
        fp.setMargins(Ui.dp(this, 8), 0, 0, 0);
        actions.addView(folder, fp);
        content.addView(actions, matchWidth());

        android.widget.Button create = Ui.button(this, "+ إنشاء مشروع جديد", false);
        LinearLayout.LayoutParams cp = matchWidth(); cp.topMargin = Ui.dp(this, 8);
        content.addView(create, cp);
        addGap(content, 22);

        LinearLayout section = new LinearLayout(this);
        section.setGravity(Gravity.CENTER_VERTICAL);
        section.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        section.addView(Ui.text(this, "مشاريعي", 20, Ui.TEXT, true), new LinearLayout.LayoutParams(0, -2, 1));
        section.addView(Ui.pill(this, "محفوظة على الهاتف", Ui.MUTED, Ui.PANEL2));
        content.addView(section, matchWidth());

        projectSearch = Ui.input(this, "ابحث باسم المشروع...");
        LinearLayout.LayoutParams q = matchWidth(); q.topMargin = Ui.dp(this, 10);
        content.addView(projectSearch, q);
        projectsBox = Ui.column(this);
        LinearLayout.LayoutParams pp = matchWidth(); pp.topMargin = Ui.dp(this, 12);
        content.addView(projectsBox, pp);

        addGap(content, 18);
        LinearLayout engine = Ui.card(this, 0xFF2B261F, 18);
        engine.addView(Ui.text(this, "محرك البناء متعدد البيئات", 16, Ui.ORANGE, true));
        addGap(engine, 6);
        engine.addView(Ui.text(this,
            "المحرر وإدارة المشاريع جاهزان. تشغيل البناء المحلي يحتاج تثبيت بيئات اللغة وأدواتها (مثل JDK وAndroid SDK أو Node أو Python أو C/C++). لن يعرض التطبيق نسبة نجاح وهمية قبل تشغيل محرك حقيقي.",
            13, 0xFFFFE0BD, false));
        content.addView(engine, matchWidth());
        addGap(content, 24);

        scroll.addView(content);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);

        zip.setOnClickListener(v -> pickZip());
        folder.setOnClickListener(v -> askNameThenPickFolder());
        create.setOnClickListener(v -> createProject());
        projectSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { refreshProjects(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        refreshProjects();
    }

    private void refreshProjects() {
        if (projectsBox == null) return;
        projectsBox.removeAllViews();
        String query = projectSearch == null ? "" : projectSearch.getText().toString().trim().toLowerCase();
        int shown = 0;
        for (File f : ProjectStore.projects(this)) {
            if (!query.isEmpty() && !f.getName().toLowerCase().contains(query)) continue;
            shown++;
            LinearLayout card = Ui.card(this, Ui.PANEL, 17);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            LinearLayout info = Ui.column(this);
            info.addView(Ui.text(this, f.getName(), 17, Ui.TEXT, true));
            info.addView(Ui.text(this, countFiles(f) + " عنصر • " + ProjectStore.humanSize(ProjectStore.folderSize(f)), 12, Ui.MUTED, false));
            card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            android.widget.Button open = Ui.button(this, "فتح المشروع", true);
            card.addView(open, new LinearLayout.LayoutParams(-2, -2));
            TextView menu = Ui.iconButton(this, "⋮");
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 46));
            mp.setMargins(Ui.dp(this, 5), 0, 0, 0);
            card.addView(menu, mp);
            open.setOnClickListener(v -> openProject(f));
            info.setOnClickListener(v -> openProject(f));
            menu.setOnClickListener(v -> showProjectMenu(menu, f));
            card.setOnClickListener(v -> openProject(f));
            LinearLayout.LayoutParams row = matchWidth(); row.bottomMargin = Ui.dp(this, 9);
            projectsBox.addView(card, row);
        }
        if (shown == 0) {
            String message = query.isEmpty() ? "لا توجد مشاريع بعد. استورد مشروعاً أو أنشئ واحداً للبدء." : "لا توجد مشاريع تطابق البحث.";
            LinearLayout empty = Ui.card(this, Ui.PANEL, 15);
            empty.addView(Ui.text(this, message, 13, Ui.MUTED, false));
            projectsBox.addView(empty, matchWidth());
        }
    }

    private void showProjectMenu(View anchor, File project) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("فتح المشروع");
        menu.getMenu().add("إعادة تسمية");
        menu.getMenu().add("حذف المشروع نهائياً");
        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("فتح المشروع")) openProject(project);
            else if (title.equals("إعادة تسمية")) renameProject(project);
            else confirmDeleteProject(project);
            return true;
        });
        menu.show();
    }

    private void confirmDeleteProject(File project) {
        new AlertDialog.Builder(this)
            .setTitle("حذف المشروع نهائياً؟")
            .setMessage("سيُحذف المشروع «" + project.getName() + "» وكل ملفاته من مساحة لوز بيلدر على الهاتف. لا يمكن التراجع عن هذا الإجراء.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف نهائياً", (d, w) -> {
                ProjectStore.deleteRecursively(project);
                refreshProjects();
                Toast.makeText(this, "تم حذف المشروع", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private void renameProject(File project) {
        EditText input = Ui.input(this, "اسم المشروع الجديد");
        input.setText(project.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية المشروع").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = ProjectStore.safeName(input.getText().toString());
                File target = new File(project.getParentFile(), name);
                if (target.exists()) { toast("يوجد مشروع بهذا الاسم بالفعل"); return; }
                if (!project.renameTo(target)) toast("تعذرت إعادة التسمية");
                refreshProjects();
            }).show();
    }

    private int countFiles(File f) {
        File[] children = f.listFiles(); if (children == null) return 0;
        int count = children.length;
        for (File child : children) if (child.isDirectory()) count += countFiles(child);
        return count;
    }

    private void createProject() {
        EditText input = Ui.input(this, "اسم المشروع");
        new AlertDialog.Builder(this).setTitle("إنشاء مشروع جديد").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("إنشاء", (d, w) -> {
                try { File f = ProjectStore.create(this, input.getText().toString()); refreshProjects(); openProject(f); }
                catch (Exception e) { toast(e.getMessage()); }
            }).show();
    }

    private void askNameThenPickFolder() {
        EditText input = Ui.input(this, "اسم المشروع المستورد"); input.setText("ImportedProject");
        new AlertDialog.Builder(this).setTitle("استيراد مجلد مشروع").setView(input)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("اختيار المجلد", (d, w) -> {
                pendingName = input.getText().toString();
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(i, PICK_FOLDER);
            }).show();
    }

    private void pickZip() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/zip"); i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK_ZIP);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (requestCode == PICK_ZIP) {
                String path = uri.getLastPathSegment();
                String name = path == null ? "ImportedProject" : path.replaceAll("(?i)\\.zip$", "");
                ProjectStore.importZip(this, uri, name);
            } else if (requestCode == PICK_FOLDER) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ProjectStore.importTree(this, uri, pendingName);
            }
            refreshProjects(); toast("تم استيراد المشروع بنجاح");
        } catch (Exception e) { toast("فشل الاستيراد: " + e.getMessage()); }
    }

    private void openProject(File f) {
        Intent i = new Intent(this, ProjectActivity.class);
        i.putExtra("project", f.getAbsolutePath()); startActivity(i);
    }
    private void toast(String s) { Toast.makeText(this, s == null ? "حدث خطأ" : s, Toast.LENGTH_LONG).show(); }
    private void addGap(LinearLayout parent, int dp) { View v = new View(this); parent.addView(v, new LinearLayout.LayoutParams(1, Ui.dp(this, dp))); }
    private LinearLayout.LayoutParams matchWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    @Override protected void onResume() { super.onResume(); if (projectsBox != null) refreshProjects(); }
}
