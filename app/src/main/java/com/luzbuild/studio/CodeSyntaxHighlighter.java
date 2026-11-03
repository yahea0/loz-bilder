
package com.luzbuild.studio;

import android.text.Editable;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CodeSyntaxHighlighter {
    private static final int KEYWORD = 0xFFC792EA;
    private static final int STRING = 0xFFC3E88D;
    private static final int COMMENT = 0xFF718A7D;
    private static final int NUMBER = 0xFFF78C6C;
    private static final int TYPE = 0xFF82AAFF;
    private static final int TAG = 0xFFFFCB6B;
    private static final int ATTR = 0xFF89DDFF;
    private static final int METHOD = 0xFF80CBC4;

    private CodeSyntaxHighlighter() {}

    public static void apply(Editable text, String fileName) {
        if (text == null) return;

        ForegroundColorSpan[] old =
            text.getSpans(0, text.length(), ForegroundColorSpan.class);

        for (ForegroundColorSpan span : old) {
            if (span instanceof SyntaxSpan) {
                text.removeSpan(span);
            }
        }

        String ext = extension(fileName);

        if (ext.equals("xml") || ext.equals("html") ||
            ext.equals("htm") || ext.equals("svg")) {

            color(text, "</?[A-Za-z][A-Za-z0-9:_-]*|/?>", TAG, 0);
            color(text, "\\b[A-Za-z_:][A-Za-z0-9:_.-]*(?=\\s*=)", ATTR, 0);
            color(text, "&[A-Za-z#][A-Za-z0-9#]+;", NUMBER, 0);
            color(text, "\"[^\"]*\"|'[^']*'", STRING, 0);
            color(text, "<!--[\\s\\S]*?-->|<!--[\\s\\S]*$", COMMENT, 0);
            return;
        }

        if (ext.equals("json")) {
            color(text, "\"(?:\\\\.|[^\"\\\\])*\"", STRING, 0);
            color(text, "\"(?:\\\\.|[^\"\\\\])*\"(?=\\s*:)", ATTR, 0);
            color(text, "\\b(?:true|false|null)\\b", KEYWORD, 0);
            color(text, "-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b", NUMBER, 0);
            return;
        }

        if (ext.equals("md") || ext.equals("markdown")) {
            color(text, "`[^`]+`", STRING, 0);
            color(text, "\\*\\*[^*]+\\*\\*|__[^_]+__", KEYWORD, 0);
            color(text, "(?m)^\\s*[-*+]\\s+", METHOD, 0);
            color(text, "(?m)^#{1,6}.*$", TAG, 0);
            return;
        }

        String keywords = "\\b(?:abstract|assert|async|await|break|case|catch|class|const|continue|def|default|delete|do|else|enum|export|extends|false|final|finally|for|from|fun|function|if|implements|import|in|interface|let|match|namespace|new|null|of|override|package|private|protected|public|return|self|static|super|switch|this|throw|throws|trait|try|true|typeof|using|var|void|while|with|yield|suspend|data|object|sealed|internal|open|val|when|where|as|is|fn|mut|pub|use|mod|impl|move|go|defer|chan|select|range|map|struct|type|func|include|define|ifdef|endif|template|typename|auto|constexpr|virtual|operator|int|float|double|char|bool|long|short|unsigned|signed|echo|require|except|pass|lambda|None|True|False)\\b";

        color(text, keywords, KEYWORD, 0);
        color(text, "\\b[A-Z][A-Za-z0-9_]*(?=\\s*[<(])", TYPE, 0);
        color(text, "\\b[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\()", METHOD, 0);
        color(text, "\\b(?:0x[0-9a-fA-F]+|\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)\\b", NUMBER, 0);
        color(text, "\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|`(?:\\\\.|[^`\\\\])*`", STRING, 0);
        color(text, "(?m)//.*$|/\\*[\\s\\S]*?\\*/", COMMENT, 0);
        color(text, "(?m)#.*$", COMMENT, 0);
        color(text, "(?m)--.*$", COMMENT, 0);
    }

    private static void color(Editable text, String regex, int color, int flags) {
        try {
            Matcher m = Pattern.compile(regex, flags).matcher(text);

            while (m.find()) {
                text.setSpan(
                    new SyntaxSpan(color),
                    m.start(),
                    m.end(),
                    Editable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        } catch (Exception ignored) {
        }
    }

    public static String languageName(String name) {
        String e = extension(name);

        switch (e) {
            case "java": return "Java";
            case "kt": case "kts": return "Kotlin";
            case "xml": return "XML";
            case "js": case "mjs": return "JavaScript";
            case "ts": case "tsx": return "TypeScript";
            case "py": return "Python";
            case "json": return "JSON";
            case "gradle": return "Gradle";
            case "md": return "Markdown";
            case "html": case "htm": return "HTML";
            case "css": return "CSS";
            case "scss": return "SCSS";
            case "c": return "C";
            case "h": return "C/C++ Header";
            case "cpp": case "cc": case "cxx": return "C++";
            case "rs": return "Rust";
            case "go": return "Go";
            case "sh": case "bash": return "Shell";
            case "yaml": case "yml": return "YAML";
            case "sql": return "SQL";
            case "php": return "PHP";
            case "rb": return "Ruby";
            case "swift": return "Swift";
            case "dart": return "Dart";
            case "cs": return "C#";
            case "toml": return "TOML";
            default: return "نص عادي";
        }
    }

    public static String commentPrefix(String name) {
        String e = extension(name);

        if (e.equals("py") || e.equals("sh") ||
            e.equals("bash") || e.equals("yaml") ||
            e.equals("yml") || e.equals("rb") ||
            e.equals("toml") || e.equals("r")) {
            return "#";
        }

        if (e.equals("sql") || e.equals("lua") || e.equals("haskell")) {
            return "--";
        }

        if (e.equals("html") || e.equals("xml") || e.equals("svg")) {
            return "<!--";
        }

        return "//";
    }

    private static String extension(String file) {
        if (file == null) return "";
        int dot = file.lastIndexOf('.');
        return dot < 0 ? "" : file.substring(dot + 1).toLowerCase();
    }

    private static class SyntaxSpan extends ForegroundColorSpan {
        SyntaxSpan(int color) {
            super(color);
        }
    }
}
