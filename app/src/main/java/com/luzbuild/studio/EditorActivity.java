
package com.luzbuild.studio;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class EditorActivity extends Activity {
    private File file;
    private CodeEditorView editor;
    private TextView status, dirtyLabel;
    private final Deque<String> undo = new ArrayDeque<>();
    private final Deque<String> redo = new ArrayDeque<>();
    private boolean suppressHistory = false;
    private String beforeChange = "";
    private boolean dirty = false;
    private static final int HISTORY_LIMIT = 100;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("file");
        file = path == null ? null : new File(path);
        if (file == null || !file.isFile()) { finish(); return; }
        render(); load();
    }

    private void render() {
        LinearLayout page = Ui.column(this); page.setBackgroundColor(Ui.BG); Ui.pad(page, this, 10);
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout titles = Ui.column(this);
        titles.addView(Ui.text(this, file.getName(), 15, Ui.TEXT, true));
        dirtyLabel = Ui.text(this, "محرر كود • غير محفوظ", 11, Ui.MUTED, false); titles.addView(dirtyLabel);
        top.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        android.widget.Button save = Ui.button(this, "حفظ والعودة", true); top.addView(save, new LinearLayout.LayoutParams(-2, -2));
        save.setOnClickListener(v -> saveAndReturn());
        page.addView(top, matchWidth());

        HorizontalScrollView toolScroll = new HorizontalScrollView(this); toolScroll.setHorizontalScrollBarEnabled(false); toolScroll.setFillViewport(false);
        LinearLayout tools = new LinearLayout(this); tools.setOrientation(LinearLayout.HORIZONTAL); tools.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        addTool(tools, "تراجع", v -> undo()); addTool(tools, "إعادة", v -> redo()); addTool(tools, "بحث", v -> showSearch(false));
        addTool(tools, "استبدال", v -> showSearch(true)); addTool(tools, "تعليق", v -> toggleComment()); addTool(tools, "مسافة", v -> indent());
        addTool(tools, "إلغاء مسافة", v -> dedent()); addTool(tools, "انتقل لسطر", v -> goToLine()); addTool(tools, "تحديد الكل", v -> editor.selectAll());
        toolScroll.addView(tools);
        LinearLayout.LayoutParams tp = matchWidth(); tp.topMargin = Ui.dp(this, 9); page.addView(toolScroll, tp);

        editor = new CodeEditorView(this);
        editor.setTextSize(13.5f);
        editor.setTextColor(Ui.TEXT);
        editor.setHintTextColor(Ui.MUTED);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setBackground(Ui.shape(0xFF0B1218, 15, this));
        editor.setPadding(Ui.dp(this, 58), Ui.dp(this, 15), Ui.dp(this, 14), Ui.dp(this, 18));
        editor.setTextDirection(View.TEXT_DIRECTION_LTR);
        editor.setGravity(Gravity.TOP | Gravity.LEFT);
        editor.setHorizontallyScrolling(true);
        editor.setSingleLine(false);
        editor.setIncludeFontPadding(false);
        editor.setLineSpacing(Ui.dp(this, 2), 1.0f);
        editor.setSelection(0);
        editor.setHint("ابدأ كتابة الكود هنا...");
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setFillViewport(true); scroll.addView(editor);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, 0, 1); ep.topMargin = Ui.dp(this, 9); ep.bottomMargin = Ui.dp(this, 7); page.addView(scroll, ep);

        LinearLayout statusBar = Ui.card(this, Ui.PANEL, 10); statusBar.setOrientation(LinearLayout.HORIZONTAL); statusBar.setGravity(Gravity.CENTER_VERTICAL);
        status = Ui.text(this, "السطر 1  •  العمود 1  •  " + CodeSyntaxHighlighter.languageName(file.getName()), 11, Ui.MUTED, false);
        statusBar.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
        statusBar.addView(Ui.pill(this, "UTF-8", Ui.GREEN, 0xFF12342D));
        LinearLayout.LayoutParams sb = matchWidth(); page.addView(statusBar, sb);
        setContentView(page);

        editor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { beforeChange = s.toString(); }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { dirty = true; updateDirty(); editor.invalidate(); updateStatus(); }
            @Override public void afterTextChanged(Editable e) {
                if (!suppressHistory && !beforeChange.equals(e.toString())) {
                    undo.push(beforeChange); while (undo.size() > HISTORY_LIMIT) undo.removeLast(); redo.clear();
                }
                CodeSyntaxHighlighter.apply(e, file.getName());
                editor.invalidate();
            }
        });
        editor.setSelectionListener(this::updateStatus);
    }

    private void load() {
        try {
            if (file.length() > 5L * 1024 * 1024) throw new Exception("الملف أكبر من الحد المسموح");
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String text = new String(bytes, StandardCharsets.UTF_8);
            suppressHistory = true; editor.setText(text); suppressHistory = false;
            undo.clear(); redo.clear(); dirty = false; updateDirty(); updateStatus();
            CodeSyntaxHighlighter.apply(editor.getText(), file.getName()); editor.invalidate();
        } catch (Exception e) { Toast.makeText(this, "تعذر فتح الملف: " + e.getMessage(), Toast.LENGTH_LONG).show(); finish(); }
    }

    private void saveAndReturn() {
        File temp = new File(file.getParentFile(), file.getName() + ".luztmp");
        File backup = new File(file.getParentFile(), file.getName() + ".luzbak");
        try {
            File parent = file.getParentFile();
            if (parent == null || !parent.exists()) throw new Exception("مجلد الملف غير موجود");
            try (FileOutputStream out = new FileOutputStream(temp)) {
                out.write(editor.getText().toString().getBytes(StandardCharsets.UTF_8));
                out.getFD().sync();
            }
            if (backup.exists() && !backup.delete()) throw new Exception("تعذر تنظيف نسخة الحفظ السابقة");
            if (file.exists() && !file.renameTo(backup)) throw new Exception("تعذر إنشاء نسخة أمان قبل الحفظ");
            if (!temp.renameTo(file)) {
                if (backup.exists()) backup.renameTo(file);
                throw new Exception("تعذر تثبيت الملف المحفوظ؛ تمت محاولة استعادة النسخة السابقة");
            }
            if (backup.exists()) backup.delete();
            dirty = false; updateDirty();
            Toast.makeText(this, "تم حفظ الملف والعودة للمشروع", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void undo() {
        if (undo.isEmpty()) { toast("لا توجد خطوة للتراجع"); return; }
        String current = editor.getText().toString(); redo.push(current); String previous = undo.pop(); setEditorText(previous);
    }
    private void redo() {
        if (redo.isEmpty()) { toast("لا توجد خطوة للإعادة"); return; }
        String current = editor.getText().toString(); undo.push(current); String next = redo.pop(); setEditorText(next);
    }
    private void setEditorText(String value) { suppressHistory = true; editor.setText(value); editor.setSelection(editor.length()); suppressHistory = false; dirty = true; updateDirty(); CodeSyntaxHighlighter.apply(editor.getText(), file.getName()); updateStatus(); }

    private void showSearch(boolean replaceMode) {
        LinearLayout box = Ui.column(this); Ui.pad(box, this, 8);
        EditText query = Ui.input(this, "النص المطلوب البحث عنه"); box.addView(query, matchWidth());
        EditText replacement = Ui.input(this, "النص البديل");
        if (replaceMode) { LinearLayout.LayoutParams p = matchWidth(); p.topMargin = Ui.dp(this, 8); box.addView(replacement, p); }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(replaceMode ? "البحث والاستبدال" : "بحث داخل الملف").setView(box)
            .setNegativeButton("إغلاق", null).setNeutralButton("التالي", null).setPositiveButton(replaceMode ? "استبدال الكل" : "بحث", null).create();
        dialog.setOnShowListener(v -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(w -> findNext(query.getText().toString()));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w -> {
                String q = query.getText().toString(); if (q.isEmpty()) { toast("اكتب كلمة البحث أولاً"); return; }
                if (replaceMode) replaceAll(q, replacement.getText().toString()); else findNext(q);
            });
        }); dialog.show();
    }

    private void findNext(String needle) {
        if (needle == null || needle.isEmpty()) { toast("اكتب كلمة البحث"); return; }
        String text = editor.getText().toString(); int start = Math.max(editor.getSelectionEnd(), 0); int at = text.indexOf(needle, start);
        if (at < 0) at = text.indexOf(needle);
        if (at < 0) { toast("لم يتم العثور على النص"); return; }
        editor.requestFocus(); editor.setSelection(at, at + needle.length());
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
    }

    private void replaceAll(String needle, String replacement) {
        if (needle.isEmpty()) { toast("اكتب كلمة البحث"); return; }
        String original = editor.getText().toString(); String changed = original.replace(needle, replacement);
        if (original.equals(changed)) { toast("لم يتم العثور على النص"); return; }
        setEditorText(changed); toast("تم استبدال جميع النتائج");
    }

    private void toggleComment() {
        String linePrefix = CodeSyntaxHighlighter.commentPrefix(file.getName());
        int start = editor.getSelectionStart(), end = editor.getSelectionEnd();
        String all = editor.getText().toString();
        int lineStart = all.lastIndexOf('\n', Math.max(0, start - 1)) + 1;
        int lineEnd = all.indexOf('\n', end); if (lineEnd < 0) lineEnd = all.length();
        String block = all.substring(lineStart, lineEnd);
        if (linePrefix.equals("<!--")) {
            boolean remove = block.trim().startsWith("<!--") && block.trim().endsWith("-->");
            String replacement = remove ? block.replaceFirst("^([\\s]*)<!--\\s?", "$1").replaceFirst("\\s?-->$", "") : "<!-- " + block + " -->";
            editor.getText().replace(lineStart, lineEnd, replacement);
            editor.setSelection(Math.min(lineStart + replacement.length(), editor.length()));
            return;
        }
        String[] lines = block.split("\\n", -1); boolean remove = true;
        for (String line : lines) if (!line.trim().isEmpty() && !line.trim().startsWith(linePrefix)) { remove = false; break; }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (remove) { int p = line.indexOf(linePrefix); if (p >= 0) line = line.substring(0, p) + line.substring(p + linePrefix.length()); }
            else if (!line.trim().isEmpty()) line = linePrefix + " " + line;
            out.append(line); if (i < lines.length - 1) out.append('\n');
        }
        editor.getText().replace(lineStart, lineEnd, out.toString()); editor.setSelection(Math.min(lineStart + out.length(), editor.length()));
    }

    private void indent() {
        String all = editor.getText().toString();
        int selectionStart = Math.max(0, editor.getSelectionStart());
        int selectionEnd = Math.max(selectionStart, editor.getSelectionEnd());
        if (selectionStart == selectionEnd) {
            editor.getText().insert(selectionStart, "    ");
            return;
        }
        int blockStart = all.lastIndexOf('\n', Math.max(0, selectionStart - 1)) + 1;
        int blockEnd = all.indexOf('\n', selectionEnd);
        if (blockEnd < 0) blockEnd = all.length();
        String block = all.substring(blockStart, blockEnd);
        String[] lines = block.split("\\n", -1);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) { out.append("    ").append(lines[i]); if (i < lines.length - 1) out.append('\n'); }
        editor.getText().replace(blockStart, blockEnd, out.toString());
        editor.setSelection(Math.min(selectionStart + 4, editor.length()), Math.min(selectionEnd + (lines.length * 4), editor.length()));
    }

    private void dedent() {
        String all = editor.getText().toString();
        int selectionStart = Math.max(0, editor.getSelectionStart());
        int selectionEnd = Math.max(selectionStart, editor.getSelectionEnd());
        int blockStart = all.lastIndexOf('\n', Math.max(0, selectionStart - 1)) + 1;
        int blockEnd = selectionStart == selectionEnd ? all.indexOf('\n', selectionEnd) : all.indexOf('\n', selectionEnd);
        if (blockEnd < 0) blockEnd = all.length();
        String block = all.substring(blockStart, blockEnd);
        String[] lines = block.split("\\n", -1);
        StringBuilder out = new StringBuilder();
        int removedFirst = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]; int remove = 0;
            while (remove < 4 && remove < line.length() && line.charAt(remove) == ' ') remove++;
            if (i == 0) removedFirst = remove;
            out.append(line.substring(remove)); if (i < lines.length - 1) out.append('\n');
        }
        if (removedFirst == 0 && lines.length == 1) { toast("لا توجد مسافة بادئة لإزالتها"); return; }
        editor.getText().replace(blockStart, blockEnd, out.toString());
        int newStart = Math.max(blockStart, selectionStart - removedFirst);
        int newEnd = Math.max(newStart, selectionEnd - removedFirst);
        editor.setSelection(Math.min(newStart, editor.length()), Math.min(newEnd, editor.length()));
    }

    private void goToLine() {
        EditText input = Ui.input(this, "رقم السطر");
        new AlertDialog.Builder(this).setTitle("الانتقال إلى سطر").setView(input).setNegativeButton("إلغاء", null)
            .setPositiveButton("انتقال", (d, w) -> { try { int target = Math.max(1, Integer.parseInt(input.getText().toString())); int offset = 0; String s = editor.getText().toString(); for (int i = 1; i < target && offset < s.length(); i++) { int n=s.indexOf('\n', offset); if(n<0){offset=s.length();break;} offset=n+1; } editor.requestFocus(); editor.setSelection(Math.min(offset, editor.length())); } catch(Exception e) { toast("أدخل رقم سطر صحيحاً"); } }).show();
    }

    private void updateDirty() { if (dirtyLabel != null) dirtyLabel.setText(dirty ? "● تعديلات غير محفوظة" : "✓ جميع التعديلات محفوظة"); }
    private void updateStatus() {
        if (status == null || editor == null) return;
        int pos = Math.max(0, editor.getSelectionStart()); String s = editor.getText().toString();
        int line = 1, last = -1; for (int i = 0; i < Math.min(pos, s.length()); i++) if (s.charAt(i) == '\n') { line++; last = i; }
        int col = pos - last;
        status.setText("السطر " + line + "  •  العمود " + col + "  •  " + CodeSyntaxHighlighter.languageName(file.getName()));
    }
    private void addTool(LinearLayout parent, String name, View.OnClickListener click) {
        android.widget.Button b = Ui.button(this, name, false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, Ui.dp(this, 43)); p.setMargins(0, 0, Ui.dp(this, 6), 0); parent.addView(b, p); b.setOnClickListener(click);
    }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_SHORT).show(); }
    private LinearLayout.LayoutParams matchWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    @Override public void onBackPressed() {
        if (!dirty) { super.onBackPressed(); return; }
        new AlertDialog.Builder(this).setTitle("تعديلات غير محفوظة").setMessage("هل تريد حفظ التغييرات والعودة إلى المشروع؟")
            .setNegativeButton("متابعة التحرير", null).setNeutralButton("خروج دون حفظ", (d, w) -> finish())
            .setPositiveButton("حفظ والعودة", (d, w) -> saveAndReturn()).show();
    }
}
