
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
import android.widget.PopupMenu;
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
    private TextView breadcrumb, detected;
    private BuildProgressPanel buildProgress;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("project");
        project = path == null ? null : new File(path);
        if (project == null || !project.isDirectory()) { finish(); return; }
        current = project; render();
    }

    private void render() {
        LinearLayout page = Ui.column(this); page.setBackgroundColor(Ui.BG); Ui.pad(page, this, 14);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout names = Ui.column(this);
        names.addView(Ui.text(this, project.getName(), 21, Ui.TEXT, true));
        names.addView(Ui.text(this, "مستكشف المشروع", 12, Ui.MUTED, false));
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView more = Ui.iconButton(this, "⋮"); header.addView(more, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        more.setOnClickListener(v -> projectMenu(more));
        page.addView(header, matchWidth());

        LinearLayout buildCard = Ui.card(this, Ui.PANEL, 17);
        LinearLayout buildHead = new LinearLayout(this); buildHead.setGravity(Gravity.CENTER_VERTICAL); buildHead.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout buildInfo = Ui.column(this);
        buildInfo.addView(Ui.text(this, "البناء والتجميع", 16, Ui.TEXT, true));
        detected = Ui.text(this, BuildProfileDetector.detect(project).label + " • أدوات البناء غير مثبّتة داخل التطبيق", 11, Ui.MUTED, false);
        buildInfo.addView(detected);
        buildHead.addView(buildInfo, new LinearLayout.LayoutParams(0, -2, 1));
        android.widget.Button build = Ui.button(this, "بناء", true); buildHead.addView(build, new LinearLayout.LayoutParams(-2, -2));
        buildCard.addView(buildHead, matchWidth());
        buildProgress = new BuildProgressPanel(this);
        buildCard.addView(buildProgress, matchWidth());
        TextView buildHint = Ui.text(this, "ستتحرك النسبة عند وصول أحداث تقدم حقيقية من أداة البناء. لا تُختلق نسبة عند عدم توفرها.", 11, Ui.MUTED, false);
        LinearLayout.LayoutParams bh = matchWidth(); bh.topMargin = Ui.dp(this, 5); buildCard.addView(buildHint, bh);
        LinearLayout.LayoutParams bc = matchWidth(); bc.topMargin = Ui.dp(this, 12); page.addView(buildCard, bc);
        build.setOnClickListener(v -> showBuildRequirements());

        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); actions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        android.widget.Button newFile = Ui.button(this, "+ ملف", false);
        android.widget.Button newFolder = Ui.button(this, "+ مجلد", false);
        android.widget.Button export = Ui.button(this, "تصدير ZIP", false);
        actions.addView(newFile, new LinearLayout.LayoutParams(0, -2, 1));
        addSmall(actions, newFolder); addSmall(actions, export);
        LinearLayout.LayoutParams ap = matchWidth(); ap.topMargin = Ui.dp(this, 9); page.addView(actions, ap);
        newFile.setOnClickListener(v -> createItem(false)); newFolder.setOnClickListener(v -> createItem(true)); export.setOnClickListener(v -> exportProject());

        search = Ui.input(this, "ابحث في أسماء الملفات والمجلدات...");
        LinearLayout.LayoutParams sp = matchWidth(); sp.topMargin = Ui.dp(this, 10); page.addView(search, sp);
        breadcrumb = Ui.text(this, "", 12, Ui.GREEN, false);
        LinearLayout.LayoutParams bp = matchWidth(); bp.topMargin = Ui.dp(this, 11); page.addView(breadcrumb, bp);
        list = Ui.column(this); ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1); lp.topMargin = Ui.dp(this, 8); page.addView(scroll, lp);
        setContentView(page);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        refresh();
    }

    private void projectMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("العودة إلى المشاريع");
        menu.getMenu().add("إعادة تسمية المشروع");
        menu.getMenu().add("حذف المشروع نهائياً");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();
            if (s.equals("العودة إلى المشاريع")) finish();
            else if (s.equals("إعادة تسمية المشروع")) renameProject();
            else confirmDeleteProject();
            return true;
        }); menu.show();
    }

    private void confirmDeleteProject() {
        new AlertDialog.Builder(this).setTitle("حذف المشروع نهائياً؟")
            .setMessage("سيتم حذف «" + project.getName() + "» مع جميع ملفاته ومجلداته من الهاتف. لا يمكن التراجع عن الحذف.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف نهائياً", (d, w) -> { ProjectStore.deleteRecursively(project); Toast.makeText(this, "تم حذف المشروع", Toast.LENGTH_SHORT).show(); finish(); }).show();
    }

    private void renameProject() {
        EditText input = Ui.input(this, "اسم المشروع الجديد"); input.setText(project.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية المشروع").setView(input).setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                File target = new File(project.getParentFile(), ProjectStore.safeName(input.getText().toString()));
                if (target.exists()) { toast("يوجد مشروع بهذا الاسم"); return; }
                if (!project.renameTo(target)) { toast("تعذرت إعادة التسمية"); return; }
                project = target; current = target; refresh();
                Toast.makeText(this, "تمت إعادة التسمية", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private void showBuildRequirements() {
        BuildProfileDetector.Profile p = BuildProfileDetector.detect(project);
        buildProgress.showBlocked("لم يبدأ البناء: محرك التنفيذ المحلي غير مدمج بعد");
        StringBuilder message = new StringBuilder();
        message.append("نوع المشروع المكتشف: ").append(p.label).append("\n\n");
        message.append("أداة البناء المطلوبة: ").append(p.toolchain).append("\n");
        message.append("أمر البناء المقترح: ").append(p.command).append("\n\n");
        message.append("محرك التنفيذ المحلي غير مدمج في هذه النسخة بعد؛ لذلك لا يمكن بدء تجميع فعلي أو إظهار نسبة حقيقية الآن.\n\n");
        message.append("سيكون المحرك قابلاً لإضافة بيئات مستقلة: Android/Gradle، Java/Maven، Node.js، Python، C/C++، Rust، Go، .NET وغيرها. كل بيئة تحتاج مترجمها واعتمادياتها الخاصة.");
        new AlertDialog.Builder(this).setTitle("تشخيص بيئة البناء").setMessage(message.toString()).setPositiveButton("حسناً", null).show();
    }

    private void refresh() {
        if (list == null || current == null) return;
        list.removeAllViews();
        String relative = project.toURI().relativize(current.toURI()).getPath();
        breadcrumb.setText("المشروع  /  " + (relative.isEmpty() ? "الجذر" : relative.replace("/", "  /  ")));
        if (!current.equals(project)) {
            android.widget.Button parent = Ui.button(this, "↑  المجلد الأعلى", false);
            LinearLayout.LayoutParams p = matchWidth(); p.bottomMargin = Ui.dp(this, 7); list.addView(parent, p);
            parent.setOnClickListener(v -> { current = current.getParentFile(); refresh(); });
        }
        File[] files = current.listFiles(); if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        String q = search == null ? "" : search.getText().toString().trim().toLowerCase();
        int shown = 0;
        for (File f : files) {
            if (!q.isEmpty() && !f.getName().toLowerCase().contains(q)) continue;
            shown++;
            LinearLayout row = Ui.card(this, Ui.PANEL, 13); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            TextView icon = Ui.text(this, f.isDirectory() ? "▣" : fileIcon(f.getName()), 20, f.isDirectory() ? Ui.GREEN : Ui.BLUE, true);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(Ui.dp(this, 34), Ui.dp(this, 38)); row.addView(icon, ip);
            LinearLayout details = Ui.column(this);
            details.addView(Ui.text(this, f.getName(), 14, Ui.TEXT, true));
            details.addView(Ui.text(this, f.isDirectory() ? "مجلد" : ProjectStore.humanSize(f.length()), 11, Ui.MUTED, false));
            row.addView(details, new LinearLayout.LayoutParams(0, -2, 1));
            TextView more = Ui.iconButton(this, "⋮"); row.addView(more, new LinearLayout.LayoutParams(Ui.dp(this, 38), Ui.dp(this, 40)));
            LinearLayout.LayoutParams rp = matchWidth(); rp.bottomMargin = Ui.dp(this, 6); list.addView(row, rp);
            row.setOnClickListener(v -> { if (f.isDirectory()) { current = f; refresh(); } else openFile(f); });
            more.setOnClickListener(v -> itemMenu(more, f));
            row.setOnLongClickListener(v -> { itemMenu(more, f); return true; });
        }
        if (shown == 0) list.addView(Ui.text(this, q.isEmpty() ? "هذا المجلد فارغ." : "لا توجد نتائج مطابقة.", 13, Ui.MUTED, false));
    }

    private void itemMenu(View anchor, File f) {
        PopupMenu menu = new PopupMenu(this, anchor);
        if (f.isFile()) menu.getMenu().add("فتح وتعديل");
        menu.getMenu().add("إعادة تسمية"); menu.getMenu().add("حذف");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();
            if (s.equals("فتح وتعديل")) openFile(f); else if (s.equals("إعادة تسمية")) rename(f); else confirmDelete(f);
            return true;
        }); menu.show();
    }

    private void createItem(boolean directory) {
        EditText input = Ui.input(this, directory ? "اسم المجلد" : "مثلاً Main.java");
        new AlertDialog.Builder(this).setTitle(directory ? "إنشاء مجلد" : "إنشاء ملف").setView(input)
            .setNegativeButton("إلغاء", null).setPositiveButton("إنشاء", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")) { toast("اسم غير صالح"); return; }
                File target = new File(current, name);
                if (target.exists()) { toast("الاسم موجود بالفعل"); return; }
                boolean ok;
                if (directory) ok = target.mkdirs(); else { try { ok = target.createNewFile(); } catch (Exception e) { ok = false; } }
                if (!ok) { toast("تعذر إنشاء العنصر"); return; }
                refresh(); if (!directory) openFile(target);
            }).show();
    }

    private void openFile(File f) {
        if (f.length() > 5L * 1024 * 1024) { toast("الملف أكبر من 5MB؛ المحرر النصي لا يفتحه حفاظاً على أداء الهاتف."); return; }
        Intent i = new Intent(this, EditorActivity.class); i.putExtra("file", f.getAbsolutePath()); startActivity(i);
    }

    private void rename(File f) {
        EditText input = Ui.input(this, "الاسم الجديد"); input.setText(f.getName());
        new AlertDialog.Builder(this).setTitle("إعادة تسمية").setView(input).setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.contains("/") || name.contains("\\")) { toast("اسم غير صالح"); return; }
                File target = new File(f.getParentFile(), name);
                if (target.exists()) { toast("الاسم موجود بالفعل"); return; }
                if (!f.renameTo(target)) toast("تعذرت إعادة التسمية");
                refresh();
            }).show();
    }

    private void confirmDelete(File f) {
        new AlertDialog.Builder(this).setTitle("حذف العنصر؟").setMessage("سيتم حذف «" + f.getName() + "» وكل ما بداخله إن كان مجلداً.")
            .setNegativeButton("إلغاء", null).setPositiveButton("حذف", (d, w) -> { ProjectStore.deleteRecursively(f); refresh(); }).show();
    }

    private void exportProject() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, project.getName() + ".zip"); startActivityForResult(i, 33);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            new Thread(() -> { try { ProjectStore.exportZip(this, project, data.getData()); runOnUiThread(() -> toast("تم تصدير المشروع")); }
                catch (Exception e) { runOnUiThread(() -> toast("فشل التصدير: " + e.getMessage())); } }).start();
        }
    }
    private void addSmall(LinearLayout parent, View child) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1); p.setMargins(Ui.dp(this, 6), 0, 0, 0); parent.addView(child, p); }
    private String fileIcon(String n) { String x=n.toLowerCase(); if(x.endsWith(".java"))return "J"; if(x.endsWith(".kt"))return "K"; if(x.endsWith(".xml"))return "<>"; if(x.endsWith(".gradle")||x.endsWith(".kts"))return "G"; if(x.endsWith(".py"))return "Py"; if(x.endsWith(".js")||x.endsWith(".ts"))return "JS"; if(x.endsWith(".json"))return "{}"; if(x.endsWith(".md"))return "M"; return "·"; }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private LinearLayout.LayoutParams matchWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    @Override public void onBackPressed() { if (!current.equals(project)) { current = current.getParentFile(); refresh(); } else super.onBackPressed(); }
}
