package com.example.rise_of_city.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

/**
 * Utility class để parse markdown thành SpannableString cho TextView
 */
public class MarkdownHelper {
    
    /**
     * Parse markdown text thành SpannableString với formatting
     * Hỗ trợ: **bold**, *italic*, `code`, ## headers, - lists
     * Loại bỏ các ký tự markdown khỏi text hiển thị
     */
    public static SpannableString parseMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return new SpannableString("");
        }
        
        // Tạo text đã remove markdown markers
        StringBuilder cleanText = new StringBuilder();
        java.util.List<SpanInfo> spans = new java.util.ArrayList<>();
        
        // Parse từng dòng để xử lý headers và list markers
        String[] lines = markdown.split("\n", -1);
        int currentPos = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String cleanLine = line;
            int lineStart = currentPos;
            
            // Xử lý list markers trước (cần xử lý trước headers)
            if (line.matches("^[-*+]\\s+.*")) {
                // Remove list marker (như "- ", "* ", "+ ")
                cleanLine = line.replaceFirst("^[-*+]\\s+", "");
            }
            
            // Xử lý headers
            if (cleanLine.startsWith("## ")) {
                cleanLine = cleanLine.substring(3); // Remove "## "
                int start = lineStart;
                int end = lineStart + cleanLine.length();
                spans.add(new SpanInfo(start, end, new StyleSpan(Typeface.BOLD)));
                spans.add(new SpanInfo(start, end, new ForegroundColorSpan(0xFF1A73E8)));
            } else if (cleanLine.startsWith("# ") && !cleanLine.startsWith("##")) {
                cleanLine = cleanLine.substring(2); // Remove "# "
                int start = lineStart;
                int end = lineStart + cleanLine.length();
                spans.add(new SpanInfo(start, end, new StyleSpan(Typeface.BOLD)));
                spans.add(new SpanInfo(start, end, new ForegroundColorSpan(0xFF1A73E8)));
            } else {
                // Xử lý bold, italic, code trong dòng
                cleanLine = processInlineMarkdown(cleanLine, lineStart, spans);
            }
            
            cleanText.append(cleanLine);
            if (i < lines.length - 1) {
                cleanText.append("\n");
                currentPos += cleanLine.length() + 1;
            } else {
                currentPos += cleanLine.length();
            }
        }
        
        // Tạo SpannableString và apply spans
        SpannableString spannable = new SpannableString(cleanText.toString());
        for (SpanInfo spanInfo : spans) {
            if (spanInfo.start >= 0 && spanInfo.end <= spannable.length() && spanInfo.start < spanInfo.end) {
                spannable.setSpan(spanInfo.what, spanInfo.start, spanInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        
        return spannable;
    }
    
    private static String processInlineMarkdown(String line, int lineOffset, java.util.List<SpanInfo> spans) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        int cleanPos = lineOffset;
        
        while (pos < line.length()) {
            // Check for **bold** (ưu tiên cao nhất)
            if (pos < line.length() - 1 && line.charAt(pos) == '*' && line.charAt(pos + 1) == '*') {
                int end = line.indexOf("**", pos + 2);
                if (end != -1) {
                    String boldText = line.substring(pos + 2, end);
                    int startSpan = cleanPos;
                    result.append(boldText);
                    int endSpan = cleanPos + boldText.length();
                    spans.add(new SpanInfo(startSpan, endSpan, new StyleSpan(Typeface.BOLD)));
                    pos = end + 2;
                    cleanPos = endSpan;
                    continue;
                }
            }
            
            // Check for `code`
            if (line.charAt(pos) == '`') {
                int end = line.indexOf("`", pos + 1);
                if (end != -1) {
                    String codeText = line.substring(pos + 1, end);
                    int startSpan = cleanPos;
                    result.append(codeText);
                    int endSpan = cleanPos + codeText.length();
                    spans.add(new SpanInfo(startSpan, endSpan, new TypefaceSpan("monospace")));
                    spans.add(new SpanInfo(startSpan, endSpan, new ForegroundColorSpan(0xFF6E6E6E)));
                    pos = end + 1;
                    cleanPos = endSpan;
                    continue;
                }
            }
            
            // Check for *italic* (chỉ khi không phải **bold**)
            // Xử lý cả trường hợp format lỗi như "*text:**"
            if (line.charAt(pos) == '*') {
                // Skip nếu là phần của **
                if (pos < line.length() - 1 && line.charAt(pos + 1) == '*') {
                    // Đã xử lý ở trên, skip
                    pos++;
                    continue;
                }
                
                // Tìm closing *
                int end = -1;
                // Tìm * đóng gần nhất, nhưng không phải **
                for (int i = pos + 1; i < line.length(); i++) {
                    if (line.charAt(i) == '*') {
                        // Kiểm tra xem có phải ** không
                        if (i < line.length() - 1 && line.charAt(i + 1) == '*') {
                            // Đây là **, skip
                            i++; // Skip cả 2 ký tự
                            continue;
                        }
                        end = i;
                        break;
                    }
                }
                
                if (end != -1) {
                    String italicText = line.substring(pos + 1, end);
                    int startSpan = cleanPos;
                    result.append(italicText);
                    int endSpan = cleanPos + italicText.length();
                    spans.add(new SpanInfo(startSpan, endSpan, new StyleSpan(Typeface.ITALIC)));
                    pos = end + 1;
                    cleanPos = endSpan;
                    continue;
                }
            }
            
            // Normal character
            char c = line.charAt(pos);
            
            // Bỏ qua các ký tự markdown đơn lẻ không có closing
            if (c == '*' || c == '`') {
                // Đã xử lý ở trên, nếu đến đây thì là marker lỗi, bỏ qua
                pos++;
                continue;
            }
            
            result.append(c);
            cleanPos++;
            pos++;
        }
        
        return result.toString();
    }
    
    // Helper class để lưu thông tin span
    private static class SpanInfo {
        int start;
        int end;
        Object what;
        
        SpanInfo(int start, int end, Object what) {
            this.start = start;
            this.end = end;
            this.what = what;
        }
    }
    
    /**
     * Remove markdown syntax để lấy plain text (cho copy)
     */
    public static String removeMarkdown(String markdown) {
        if (markdown == null) return "";
        
        String text = markdown;
        // Remove **bold** (non-greedy)
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        // Remove *italic* (but not **bold**)
        text = text.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "$1");
        // Remove `code`
        text = text.replaceAll("`(.+?)`", "$1");
        // Remove ## headers (line by line)
        text = text.replaceAll("(?m)^##?\\s+", "");
        // Remove - list markers (line by line)
        text = text.replaceAll("(?m)^[-*+]\\s+", "");
        // Clean up extra spaces
        text = text.replaceAll("\\n\\s*\\n", "\n\n");
        
        return text.trim();
    }
}

