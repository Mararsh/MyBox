package mara.mybox.tools;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class HtmlWriteTools {

    /*
        edit html
     */
    public static File writeHtml(String html) {
        try {
            File htmFile = TmpFileTools.getTempFile(".htm");
            TextFileTools.writeFile(htmFile, html);
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void editHtml(String html) {
        try {
            File htmFile = writeHtml(html);
            ControllerTools.openHtmlEditor(null, htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void editHtml(String title, String body) {
        editHtml(html(title, body));
    }

    /*
        build html
     */
    public static String emptyHmtl() {
        return htmlWithStyleValue(null, null, "<BODY>\n\n\n</BODY>\n");
    }

    public static String htmlPrefix(String title, String styleValue) {
        StringBuilder s = new StringBuilder();
        s.append("<!DOCTYPE html><HTML>\n").append(Indent).append("<HEAD>\n").append(Indent).append(Indent).append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
        if (title != null && !title.trim().isEmpty()) {
            s.append(Indent).append(Indent).append("<TITLE>").append(title).append("</TITLE>\n");
        }
        if (styleValue != null && !styleValue.isBlank()) {
            s.append(Indent).append(Indent).append("<style type=\"text/css\">\n");
            s.append(Indent).append(Indent).append(Indent).append(styleValue).append("\n");
            s.append(Indent).append(Indent).append("</style>\n");
        }
        s.append(Indent).append("</HEAD>\n");
        return s.toString();
    }

    public static String htmlPrefix() {
        return htmlPrefix(null, HtmlStyles.DefaultStyle);
    }

    public static String html(String title, String body) {
        return htmlWithStyleValue(title, HtmlStyles.DefaultStyle, body);
    }

    public static String html(String title, HtmlStyles.HtmlStyle style, String body) {
        return htmlWithStyleValue(title, HtmlStyles.styleValue(style), body);
    }

    public static String html(String title, String styleName, String body) {
        return html(title, HtmlStyles.styleName(styleName), body);
    }

    public static String setStyle(String html, HtmlStyles.HtmlStyle style) {
        return setStyleValue(html, HtmlStyles.styleValue(style));
    }

    public static String setStyle(String html, String styleName) {
        return setStyle(html, HtmlStyles.styleName(styleName));
    }

    public static String htmlWithStyleValue(String title, String styleValue, String body) {
        StringBuilder s = new StringBuilder();
        s.append(htmlPrefix(title, styleValue));
        s.append(body);
        s.append("</HTML>\n");
        return s.toString();
    }

    public static String setStyleValue(String html, String styleValue) {
        String title = HtmlReadTools.htmlTitle(html);
        String body = HtmlReadTools.body(html, true);
        return htmlWithStyleValue(title, styleValue, body);
    }

    /*
        convert html
     */
    public static String setCharset(File htmlFile, Charset charset, boolean must) {
        try {
            if (htmlFile == null || charset == null) {
                return "InvalidData";
            }
            Charset fileCharset = TextFileTools.charset(htmlFile);
            String html = TextFileTools.readTexts(htmlFile, fileCharset);
            String head = HtmlReadTools.tag(html, "head", false);
            String preHtml = HtmlReadTools.preHtml(html);
            if (head == null) {
                if (!must && fileCharset.equals(charset)) {
                    return "NeedNot";
                }
                html = preHtml + "<html>\n" + "    <head>\n" + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + charset.name() + "\" />\n" + "    </head>\n" + html + "\n" + "</html>";
            } else {
                String newHead;
                Charset headCharset = HtmlReadTools.charsetInHead(head);
                if (!must && fileCharset.equals(charset) && (headCharset == null || charset.equals(headCharset))) {
                    return "NeedNot";
                }
                if (headCharset != null) {
                    newHead = FindReplaceString.replace(head, headCharset.name(), charset.name(), 0, false, true, false);
                } else {
                    newHead = head + "\n<meta charset=\"text/html; charset=" + charset.name() + "\"/>";
                }
                html = preHtml + "<html>\n" + "    <head>\n" + newHead + "\n" + "    </head>\n" + HtmlReadTools.body(html, true) + "\n" + "</html>";
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e, charset.displayName());
            return null;
        }
    }

    public static String textToHtml(String text) {
        String body = "" + FindReplaceString.replaceAll(text, "\n", "</br>");
        return html(null, body);
    }

    public static String toUTF8(File htmlFile, boolean must) {
        return setCharset(htmlFile, Charset.forName("utf-8"), must);
    }

    public static String setStyle(File htmlFile, String css, boolean ignoreOriginal) {
        try {
            if (htmlFile == null || css == null) {
                return "InvalidData";
            }
            Charset fileCharset = TextFileTools.charset(htmlFile);
            String html = TextFileTools.readTexts(htmlFile, fileCharset);
            String preHtml = HtmlReadTools.preHtml(html);
            String head;
            if (ignoreOriginal) {
                head = "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + fileCharset.name() + "\" />\n";
            } else {
                head = HtmlReadTools.tag(html, "head", false);
                if (head == null) {
                    head = "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + fileCharset.name() + "\" />\n";
                }
            }
            html = preHtml + "<html>\n" + "    <head>\n" + head + "\n" + "        <style type=\"text/css\">/>\n" + css + "        </style>/>\n" + "    </head>\n" + HtmlReadTools.body(html, true) + "\n" + "</html>";
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // files should have been sorted
    public static boolean generateFrameset(List<File> files, File targetFile) {
        try {
            if (files == null || files.isEmpty()) {
                return false;
            }
            String namePrefix = FileNameTools.getFilePrefix(targetFile.getName());
            File navFile = new File(targetFile.getParent() + File.separator + namePrefix + "_nav.html");
            StringBuilder nav = new StringBuilder();
            File first = null;
            for (File file : files) {
                String filepath = file.getAbsolutePath();
                String name = file.getName();
                if (filepath.equals(targetFile.getAbsolutePath()) || filepath.equals(navFile.getAbsolutePath())) {
                    FileDeleteTools.delete(file);
                } else {
                    if (first == null) {
                        first = file;
                    }
                    if (file.getParent().equals(targetFile.getParent())) {
                        nav.append("<a href=\"./").append(name).append("\" target=main>").append(name).append("</a><br>\n");
                    } else {
                        nav.append("<a href=\"").append(file.toURI()).append("\" target=main>").append(filepath).append("</a><br>\n");
                    }
                }
            }
            if (first == null) {
                return false;
            }
            String body = nav.toString();
            TextFileTools.writeFile(navFile, html(Languages.message("PathIndex"), body));
            String frameset = " <FRAMESET border=2 cols=400,*>\n" + "<FRAME name=nav src=\"" + namePrefix + "_nav.html\" />\n";
            if (first.getParent().equals(targetFile.getParent())) {
                frameset += "<FRAME name=main src=\"" + first.getName() + "\" />\n";
            } else {
                frameset += "<FRAME name=main src=\"" + first.toURI() + "\" />\n";
            }
            frameset += "</FRAMESET>";
            File frameFile = new File(targetFile.getParent() + File.separator + namePrefix + ".html");
            TextFileTools.writeFile(frameFile, html(null, frameset));
            return frameFile.exists();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // files should have been sorted
    public static void makePathList(File path, List<File> files, Map<File, Link> completedLinks) {
        if (files == null || files.isEmpty()) {
            return;
        }
        try {
            String listPrefix = "0000_" + Languages.message("PathIndex") + "_list";
            StringBuilder csv = new StringBuilder();
            String s = Languages.message("Address") + "," + Languages.message("File") + "," + Languages.message("Title") + "," + Languages.message("Name") + "," + Languages.message("Index") + "," + Languages.message("Time") + "\n";
            csv.append(s);
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("File"), Languages.message("Address"), Languages.message("Title"), Languages.message("Name"), Languages.message("Index"), Languages.message("Time")));
            StringTable table = new StringTable(names, Languages.message("DownloadHistory"));
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(listPrefix)) {
                    FileDeleteTools.delete(file);
                } else {
                    Link link = completedLinks.get(file);
                    if (link == null) {
                        s = file.getAbsolutePath() + ",,,," + DateTools.datetimeToString(FileTools.createTime(file));
                        csv.append(s);
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(file.getAbsolutePath(), "", "", "", "", DateTools.datetimeToString(FileTools.createTime(file))));
                        table.add(row);
                    } else {
                        s = link.getUrl() + "," + file.getAbsolutePath() + "," + (link.getTitle() != null ? link.getTitle() : "") + "," + (link.getName() != null ? link.getName() : "") + "," + (link.getIndex() > 0 ? link.getIndex() : "") + "," + (link.getDlTime() != null ? DateTools.datetimeToString(link.getDlTime()) : "") + "\n";
                        csv.append(s);
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(file.getAbsolutePath(), link.getUrl().toString(), link.getTitle() != null ? link.getTitle() : "", link.getName() != null ? link.getName() : "", link.getIndex() > 0 ? link.getIndex() + "" : "", link.getDlTime() != null ? DateTools.datetimeToString(link.getDlTime()) : ""));
                        table.add(row);
                    }
                }
            }
            String filename = path.getAbsolutePath() + File.separator + listPrefix + ".csv";
            TextFileTools.writeFile(new File(filename), csv.toString());
            filename = path.getAbsolutePath() + File.separator + listPrefix + ".html";
            TextFileTools.writeFile(new File(filename), table.html());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static int replace(Document doc, String findString,
            boolean reg, boolean caseInsensitive, String color, String bgColor, String font) {
        if (doc == null) {
            return 0;
        }
        NodeList nodeList = doc.getElementsByTagName("body");
        if (nodeList == null || nodeList.getLength() < 1) {
            return 0;
        }
        FindReplaceString finder = FindReplaceString.create().setOperation(FindReplaceString.Operation.FindNext).setFindString(findString).setIsRegex(reg).setCaseInsensitive(caseInsensitive).setMultiline(true);
        String replaceSuffix = " style=\"color:" + color + "; background: " + bgColor + "; font-size:" + font + ";\">" + findString + "</span>";
        return replace(finder, nodeList.item(0), 0, replaceSuffix);
    }

    public static int replace(FindReplaceString finder, Node node, int index, String replaceSuffix) {
        if (node == null || replaceSuffix == null || finder == null) {
            return index;
        }
        String texts = node.getTextContent();
        int newIndex = index;
        if (texts != null && !texts.isBlank()) {
            StringBuilder s = new StringBuilder();
            while (true) {
                finder.setInputString(texts).setAnchor(0).run();
                if (finder.getStringRange() == null) {
                    break;
                }
                String replaceString = "<span id=\"MyBoxSearchLocation" + (++newIndex) + "\" " + replaceSuffix;
                if (finder.getLastStart() > 0) {
                    s.append(texts.substring(0, finder.getLastStart()));
                }
                s.append(replaceString);
                texts = texts.substring(finder.getLastEnd());
            }
            s.append(texts);
            node.setTextContent(s.toString());
        }
        Node child = node.getFirstChild();
        while (child != null) {
            replace(finder, child, newIndex, replaceSuffix);
            child = child.getNextSibling();
        }
        return newIndex;
    }

    public static boolean relinkPage(File httpFile, Map<File, Link> completedLinks, Map<String, File> completedAddresses) {
        try {
            if (httpFile == null || !httpFile.exists() || completedAddresses == null) {
                return false;
            }
            Link baseLink = completedLinks.get(httpFile);
            String html = TextFileTools.readTexts(httpFile);
            List<Link> links = HtmlReadTools.links(baseLink.getUrl(), html);
            String replaced = "";
            String unchecked = html;
            int pos;
            for (Link link : links) {
                try {
                    String originalAddress = link.getAddressOriginal();
                    pos = unchecked.indexOf("\"" + originalAddress + "\"");
                    if (pos < 0) {
                        pos = unchecked.indexOf("'" + originalAddress + "'");
                    }
                    if (pos < 0) {
                        continue;
                    }
                    replaced += unchecked.substring(0, pos);
                    unchecked = unchecked.substring(pos + originalAddress.length() + 2);
                    File linkFile = completedAddresses.get(link.getAddress());
                    if (linkFile == null || !linkFile.exists()) {
                        replaced += "\"" + originalAddress + "\"";
                        continue;
                    }
                    if (linkFile.getParent().startsWith(httpFile.getParent())) {
                        replaced += "\"" + linkFile.getName() + "\"";
                    } else {
                        replaced += "\"" + linkFile.getAbsolutePath() + "\"";
                    }
                } catch (Exception e) {
                    //                    MyBoxLog.debug(e.toString());
                }
            }
            replaced += unchecked;
            File tmpFile = TmpFileTools.getTempFile();
            TextFileTools.writeFile(tmpFile, replaced, TextFileTools.charset(httpFile));
            return FileTools.rename(tmpFile, httpFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static void makePathFrameset(File path) {
        try {
            if (path == null || !path.isDirectory()) {
                return;
            }
            File[] pathFiles = path.listFiles();
            if (pathFiles == null || pathFiles.length == 0) {
                return;
            }
            List<File> files = new ArrayList<>();
            for (File file : pathFiles) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
            if (!files.isEmpty()) {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return FileNameTools.compareFilename(f1, f2);
                    }
                });
                File frameFile = new File(path.getAbsolutePath() + File.separator + "0000_" + Languages.message("PathIndex") + ".html");
                generateFrameset(files, frameFile);
            }
            for (File file : pathFiles) {
                if (file.isDirectory()) {
                    makePathFrameset(file);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
