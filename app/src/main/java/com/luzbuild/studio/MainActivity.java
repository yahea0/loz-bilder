package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {

    private static final int PICK_ZIP = 10;
    private static final int PICK_FOLDER = 11;

    private String pendingName = "ImportedProject";
    private LinearLayout projectsBox;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

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

        content.addView(
            Ui.text(this, "لوز بيلدر", 29, Ui.TEXT, true),
            matchWrap()
        );

        content.addView(
            Ui.text(this, "استوديو مشاريع أندرويد من هاتفك",
                14, Ui.MUTED, false),
            matchWrap()
        );

        View gap = new View(this);
        content.addView(
            gap,
            new LinearLayout.LayoutParams(
                1, Ui.dp(this, 20)
            )
        );

        LinearLayout hero = Ui.column(this);
        hero.setBackground(
            Ui.shape(Ui.PANEL, 20, this)
        );
        Ui.pad(hero, this, 16);

        hero.addView(
            Ui.text(this, "مشاريعك في مكان واحد",
                19, Ui.TEXT, true),
            matchWrap()
        );

        hero.addView(
            Ui.text(this,
                "استورد المشروع مرة واحدة، ثم عدّل ملفاته محلياً واحتفظ بنسخة العمل على الهاتف.",
                14, Ui.MUTED, false),
            matchWrap()
        );

        content.addView(hero, matchWidth());

        View gap2 = new View(this);
        content.addView(
            gap2,
            new LinearLayout.LayoutParams(
                1, Ui.dp(this, 14)
            )
        );

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(
            LinearLayout.HORIZONTAL
        );
        actions.setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        android.widget.Button zip =
            Ui.button(this, "استيراد ZIP", true);

        android.widget.Button folder =
            Ui.button(this, "استيراد مجلد", false);

        actions.addView(
            zip,
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        LinearLayout.LayoutParams fp =
            new LinearLayout.LayoutParams(0, -2, 1);

        fp.setMargins(Ui.dp(this, 8), 0, 0, 0);

        actions.addView(folder, fp);

        content.addView(actions, matchWidth());

        android.widget.Button create =
            Ui.button(this, "+ مشروع جديد", false);

        LinearLayout.LayoutParams cp = matchWidth();
        cp.topMargin = Ui.dp(this, 8);

        content.addView(create, cp);

        View gap3 = new View(this);

        content.addView(
            gap3,
            new LinearLayout.LayoutParams(
                1, Ui.dp(this, 24)
            )
        );

        content.addView(
            Ui.text(this, "مشاريعي", 19, Ui.TEXT, true),
            matchWrap()
        );

        projectsBox = Ui.column(this);

        LinearLayout.LayoutParams pp = matchWidth();
        pp.topMargin = Ui.dp(this, 10);

        content.addView(projectsBox, pp);

        View spacer = new View(this);

        content.addView(
            spacer,
            new LinearLayout.LayoutParams(
                1, Ui.dp(this, 24)
            )
        );

        LinearLayout note = Ui.column(this);

        note.setBackground(
            Ui.shape(0xFF332B20, 16, this)
        );

        Ui.pad(note, this, 14);

        note.addView(
            Ui.text(this, "محرك البناء المحلي",
                16, Ui.ORANGE, true),
            matchWrap()
        );

        note.addView(
            Ui.text(this,
                "هذه نسخة تأسيسية لإدارة الملفات. محرك Gradle وJDK وAndroid SDK المستقل سيُضاف كحزمة أدوات تُثبّت من داخل التطبيق مرة واحدة، ثم تعمل محلياً.",
                13, 0xFFFFDDB4, false),
            matchWrap()
        );

        content.addView(note, matchWidth());

        scroll.addView(content);

        page.addView(
            scroll,
            new LinearLayout.LayoutParams(-1, 0, 1)
        );

        setContentView(page);

        zip.setOnClickListener(v -> pickZip());

        folder.setOnClickListener(
            v -> askNameThenPickFolder()
        );

        create.setOnClickListener(
            v -> createProject()
        );

        refreshProjects();
    }

    private void refreshProjects() {

        if (projectsBox == null) return;

        projectsBox.removeAllViews();

        for (File f : ProjectStore.projects(this)) {

            LinearLayout card = new LinearLayout(this);

            card.setGravity(
                android.view.Gravity.CENTER_VERTICAL
            );

            card.setOrientation(
                LinearLayout.HORIZONTAL
            );

            card.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
            );

            card.setBackground(
                Ui.shape(Ui.PANEL, 16, this)
            );

            Ui.pad(card, this, 14);

            LinearLayout info = Ui.column(this);

            info.addView(
                Ui.text(this, f.getName(),
                    16, Ui.TEXT, true),
                matchWrap()
            );

            info.addView(
                Ui.text(this,
                    countFiles(f) + " ملفاً ومجلداً",
                    12, Ui.MUTED, false),
                matchWrap()
            );

            card.addView(
                info,
                new LinearLayout.LayoutParams(0, -2, 1)
            );

            android.widget.Button open =
                Ui.button(this, "فتح", true);

            card.addView(
                open,
                new LinearLayout.LayoutParams(-2, -2)
            );

            open.setOnClickListener(
                v -> openProject(f)
            );

            card.setOnClickListener(
                v -> openProject(f)
            );

            LinearLayout.LayoutParams p = matchWidth();
            p.bottomMargin = Ui.dp(this, 9);

            projectsBox.addView(card, p);
        }

        if (projectsBox.getChildCount() == 0) {

            projectsBox.addView(
                Ui.text(this,
                    "لا توجد مشاريع بعد. ابدأ باستيراد ZIP أو إنشاء مشروع جديد.",
                    14, Ui.MUTED, false),
                matchWrap()
            );
        }
    }

    private int countFiles(File f) {

        File[] children = f.listFiles();

        if (children == null) return 0;

        int n = children.length;

        for (File child : children) {
            if (child.isDirectory()) {
                n += countFiles(child);
            }
        }

        return n;
    }

    private void createProject() {

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("مثلاً MyAndroidApp");

        new AlertDialog.Builder(this)
            .setTitle("إنشاء مشروع")
            .setView(input)
            .setPositiveButton("إنشاء", (d, w) -> {

                try {

                    File f = ProjectStore.create(
                        this,
                        input.getText().toString()
                    );

                    refreshProjects();
                    openProject(f);

                } catch (Exception e) {
                    toast(e.getMessage());
                }

            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void askNameThenPickFolder() {

        EditText input = new EditText(this);

        input.setSingleLine(true);
        input.setText("ImportedProject");

        new AlertDialog.Builder(this)
            .setTitle("اسم المشروع المستورد")
            .setView(input)
            .setPositiveButton("اختيار المجلد",
                (d, w) -> {

                    pendingName =
                        input.getText().toString();

                    Intent i = new Intent(
                        Intent.ACTION_OPEN_DOCUMENT_TREE
                    );

                    startActivityForResult(
                        i,
                        PICK_FOLDER
                    );
                })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void pickZip() {

        Intent i = new Intent(
            Intent.ACTION_OPEN_DOCUMENT
        );

        i.setType("application/zip");

        i.addCategory(
            Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(i, PICK_ZIP);
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

        if (resultCode != RESULT_OK ||
            data == null ||
            data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {

            if (requestCode == PICK_ZIP) {

                String name =
                    uri.getLastPathSegment() == null
                    ? "ImportedProject"
                    : uri.getLastPathSegment()
                        .replaceAll("(?i)\\.zip$", "");

                ProjectStore.importZip(
                    this,
                    uri,
                    name
                );

            } else if (requestCode == PICK_FOLDER) {

                getContentResolver()
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                ProjectStore.importTree(
                    this,
                    uri,
                    pendingName
                );
            }

            refreshProjects();

            toast("تم استيراد المشروع بنجاح");

        } catch (Exception e) {

            toast(
                "فشل الاستيراد: " + e.getMessage()
            );
        }
    }

    private void openProject(File f) {

        Intent i = new Intent(
            this,
            ProjectActivity.class
        );

        i.putExtra(
            "project",
            f.getAbsolutePath()
        );

        startActivity(i);
    }

    private void toast(String s) {

        Toast.makeText(
            this,
            s == null ? "حدث خطأ" : s,
            Toast.LENGTH_LONG
        ).show();
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (projectsBox != null) {
            refreshProjects();
        }
    }
}
