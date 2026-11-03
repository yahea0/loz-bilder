package com.luzbuild.studio;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class ProjectStore {

    private ProjectStore() {}

    static File root(Context c) {

        File f = new File(
            c.getFilesDir(),
            "projects"
        );

        if (!f.exists()) {
            f.mkdirs();
        }

        return f;
    }

    static File create(Context c, String name)
        throws IOException {

        String safe = safeName(name);

        File f = new File(root(c), safe);

        if (!f.exists() && !f.mkdirs()) {
            throw new IOException(
                "تعذر إنشاء مجلد المشروع"
            );
        }

        new File(
            f,
            "README_PROJECT.txt"
        ).createNewFile();

        return f;
    }

    static String safeName(String value) {

        String s = value == null
            ? "Project"
            : value.trim()
                .replaceAll(
                    "[^A-Za-z0-9_ -]",
                    "_"
                );

        if (s.isEmpty()) {
            s = "Project";
        }

        return s.length() > 48
            ? s.substring(0, 48)
            : s;
    }

    private static String safeChildName(String value) {

        if (value == null ||
            value.isEmpty() ||
            value.equals(".") ||
            value.equals("..")) {
            return "unnamed";
        }

        return value
            .replace("/", "_")
            .replace("\\", "_");
    }

    static List<File> projects(Context c) {

        File[] fs = root(c).listFiles(
            File::isDirectory
        );

        List<File> out = new ArrayList<>();

        if (fs != null) {
            for (File f : fs) {
                out.add(f);
            }
        }

        out.sort(
            Comparator.comparing(
                File::getName,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return out;
    }

    static void importZip(
        Context c,
        Uri uri,
        String suggestedName
    ) throws IOException {

        File dest = new File(
            root(c),
            safeName(suggestedName)
        );

        int n = 2;

        while (dest.exists()) {

            dest = new File(
                root(c),
                safeName(suggestedName) + "_" + n++
            );
        }

        if (!dest.mkdirs()) {
            throw new IOException(
                "تعذر إنشاء مجلد المشروع"
            );
        }

        InputStream opened =
            c.getContentResolver()
                .openInputStream(uri);

        if (opened == null) {
            throw new IOException(
                "تعذر قراءة الملف"
            );
        }

        try (
            InputStream raw = opened;
            ZipInputStream zin =
                new ZipInputStream(
                    new BufferedInputStream(raw)
                )
        ) {

            ZipEntry e;

            byte[] buffer = new byte[32 * 1024];

            long totalBytes = 0;
            int entryCount = 0;

            String canonicalRoot =
                dest.getCanonicalPath()
                    + File.separator;

            while ((e = zin.getNextEntry()) != null) {

                if (++entryCount > 100000) {
                    throw new IOException(
                        "ملف ZIP يحتوي على عدد عناصر أكبر من المسموح"
                    );
                }

                File out = new File(dest, e.getName());

                String canonical =
                    out.getCanonicalPath();

                if (!canonical.equals(
                        dest.getCanonicalPath()
                    ) &&
                    !canonical.startsWith(canonicalRoot)) {

                    throw new IOException(
                        "ملف ZIP يحتوي على مسار غير آمن"
                    );
                }

                if (e.isDirectory()) {

                    out.mkdirs();

                } else {

                    File parent = out.getParentFile();

                    if (parent != null) {
                        parent.mkdirs();
                    }

                    try (
                        OutputStream os =
                            new BufferedOutputStream(
                                new FileOutputStream(out)
                            )
                    ) {

                        int r;

                        while ((r = zin.read(buffer)) != -1) {

                            totalBytes += r;

                            if (totalBytes >
                                2L * 1024 * 1024 * 1024) {

                                throw new IOException(
                                    "حجم المشروع بعد فك الضغط يتجاوز 2GB"
                                );
                            }

                            os.write(buffer, 0, r);
                        }
                    }
                }

                zin.closeEntry();
            }

        } catch (Exception ex) {

            deleteRecursively(dest);

            if (ex instanceof IOException) {
                throw (IOException) ex;
            }

            throw new IOException(
                "فشل استيراد ZIP: " + ex.getMessage(),
                ex
            );
        }
    }

    static void importTree(
        Context c,
        Uri treeUri,
        String name
    ) throws IOException {

        DocumentFile source =
            DocumentFile.fromTreeUri(c, treeUri);

        if (source == null ||
            !source.isDirectory()) {

            throw new IOException(
                "المجلد المختار غير صالح"
            );
        }

        File dest = new File(
            root(c),
            safeName(name)
        );

        int n = 2;

        while (dest.exists()) {

            dest = new File(
                root(c),
                safeName(name) + "_" + n++
            );
        }

        if (!dest.mkdirs()) {
            throw new IOException(
                "تعذر إنشاء المشروع"
            );
        }

        try {

            copyDocumentTree(
                c.getContentResolver(),
                source,
                dest
            );

        } catch (IOException e) {

            deleteRecursively(dest);
            throw e;
        }
    }

    private static void copyDocumentTree(
        ContentResolver resolver,
        DocumentFile source,
        File dest
    ) throws IOException {

        for (DocumentFile child : source.listFiles()) {

            File target = new File(
                dest,
                safeChildName(child.getName())
            );

            if (child.isDirectory()) {

                if (!target.mkdirs() &&
                    !target.isDirectory()) {

                    throw new IOException(
                        "تعذر إنشاء " + target.getName()
                    );
                }

                copyDocumentTree(
                    resolver,
                    child,
                    target
                );

            } else {

                try (
                    InputStream in =
                        resolver.openInputStream(
                            child.getUri()
                        );

                    OutputStream out =
                        new BufferedOutputStream(
                            new FileOutputStream(target)
                        )
                ) {

                    if (in == null) {
                        throw new IOException(
                            "تعذر قراءة " + child.getName()
                        );
                    }

                    byte[] b = new byte[32 * 1024];

                    int r;

                    while ((r = in.read(b)) != -1) {
                        out.write(b, 0, r);
                    }
                }
            }
        }
    }

    static void exportZip(
        Context c,
        File project,
        Uri target
    ) throws IOException {

        OutputStream opened =
            c.getContentResolver()
                .openOutputStream(target);

        if (opened == null) {
            throw new IOException(
                "تعذر إنشاء ملف ZIP"
            );
        }

        try (
            OutputStream raw = opened;
            ZipOutputStream zout =
                new ZipOutputStream(
                    new BufferedOutputStream(raw)
                )
        ) {

            addToZip(project, project, zout);
        }
    }

    private static void addToZip(
        File root,
        File item,
        ZipOutputStream out
    ) throws IOException {

        File[] children = item.listFiles();

        if (children == null) return;

        for (File child : children) {

            String path = root.toURI()
                .relativize(child.toURI())
                .getPath();

            if (child.isDirectory()) {

                out.putNextEntry(
                    new ZipEntry(
                        path.endsWith("/")
                            ? path
                            : path + "/"
                    )
                );

                out.closeEntry();

                addToZip(root, child, out);

            } else {

                out.putNextEntry(
                    new ZipEntry(path)
                );

                try (
                    InputStream in =
                        new BufferedInputStream(
                            new FileInputStream(child)
                        )
                ) {

                    byte[] b = new byte[32 * 1024];

                    int r;

                    while ((r = in.read(b)) != -1) {
                        out.write(b, 0, r);
                    }
                }

                out.closeEntry();
            }
        }
    }

    static void deleteRecursively(File f) {

        if (f == null || !f.exists()) return;

        File[] children = f.listFiles();

        if (children != null) {

            for (File child : children) {
                deleteRecursively(child);
            }
        }

        // Best effort cleanup.
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
