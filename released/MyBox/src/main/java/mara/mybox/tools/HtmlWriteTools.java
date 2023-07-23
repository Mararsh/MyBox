package mara.mybox.tools;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Languages;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
            File htmFile = FileTmpTools.getTempFile(".html");
            Charset charset = HtmlReadTools.charset(html);
            TextFileTools.writeFile(htmFile, html, charset);
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void editHtml(String html) {
        try {
            File htmFile = writeHtml(html);
            HtmlEditorController.openFile(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void editHtml(String title, String body) {
        editHtml(html(title, body));
    }

    public static String emptyHmtl(String title) {
        String body = title == null ? "<BODY>\n\n\n</BODY>\n" : "<BODY>\n<h2>" + title + "</h2>\n</BODY>\n";
        return html(title, "utf-8", null, body);
    }

    public static String htmlPrefix(String title, String charset, String styleValue) {
        StringBuilder s = new StringBuilder();
        s.append("<!DOCTYPE html><HTML>\n").append(Indent).append("<HEAD>\n");
        if (charset == null || charset.isBlank()) {
            charset = "utf-8";
        }
        s.append(Indent).append(Indent)
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=")
                .append(charset).append("\" />\n");
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

    /*
        build html
     */
    public static String htmlPrefix() {
        return htmlPrefix(null, "utf-8", null);
    }

    public static String html(String title, String body) {
        return html(title, "utf-8", null, body);
    }

    public static String html(String title, String style, String body) {
        return html(title, "utf-8", style, body);
    }

    public static String html(String title, String charset, String styleValue, String body) {
        StringBuilder s = new StringBuilder();
        s.append(htmlPrefix(title, charset, styleValue));
        s.append(body);
        s.append("</HTML>\n");
        return s.toString();
    }

    public static String html(String body) {
        return html(null, "utf-8", HtmlStyles.DefaultStyle, body);
    }

    public static String style(String html, String styleValue) {
        try {
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return null;
            }
            Charset charset = doc.charset();
            if (charset == null) {
                charset = Charset.forName("UTF-8");
            }
            return html(doc.title(), charset.name(), styleValue,
                    HtmlReadTools.body(html, true));
        } catch (Exception e) {
            return null;
        }
    }

    public static String addStyle(String html, String style) {
        try {
            if (html == null || style == null) {
                return "InvalidData";
            }
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return null;
            }
            doc.head().appendChild(new Element("style").text(style));
            return doc.outerHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        convert html
     */
    public static String stringToHtml(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\x20", "&nbsp;")
                .replaceAll("©", "&copy;")
                .replaceAll("®", "&reg;")
                .replaceAll("™", "&trade;")
                .replaceAll("\r\n|\n|\r", "<BR>\n");
    }

    public static String textToHtml(String text) {
        if (text == null) {
            return null;
        }
        String body = stringToHtml(text);
        return html(null, body);
    }

    public static String htmlToText(String html) {
        try {
            if (html == null || html.isBlank()) {
                return html;
            } else {
                return Jsoup.parse(html).wholeText();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String md2html(String md, Parser htmlParser, HtmlRenderer htmlRender) {
        try {
            if (htmlParser == null || htmlRender == null || md == null || md.isBlank()) {
                return null;
            }
            com.vladsch.flexmark.util.ast.Node document = htmlParser.parse(md);
            String html = htmlRender.render(document);
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String md2html(String md) {
        try {
            if (md == null || md.isBlank()) {
                return null;
            }
            MutableDataHolder htmlOptions = MarkdownTools.htmlOptions();
            Parser htmlParser = Parser.builder(htmlOptions).build();
            HtmlRenderer htmlRender = HtmlRenderer.builder(htmlOptions).build();
            return md2html(md, htmlParser, htmlRender);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String setCharset(String html, Charset charset) {
        try {
            if (html == null) {
                return "InvalidData";
            }
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return "InvalidData";
            }
            setCharset(doc, charset);
            return doc.outerHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean setCharset(Document doc, Charset charset) {
        try {
            if (doc == null) {
                return false;
            }
            if (charset == null) {
                charset = Charset.forName("utf-8");
            }
            Elements children = doc.head().children();
            for (Element e : children) {
                if (!e.tagName().equalsIgnoreCase("meta")) {
                    continue;
                }
                if (e.hasAttr("charset")) {
                    e.remove();
                }
                if ("Content-Type".equalsIgnoreCase(e.attr("http-equiv")) && e.hasAttr("content")) {
                    e.remove();
                }
            }
            Element meta1 = new Element("meta")
                    .attr("http-equiv", "Content-Type")
                    .attr("content", "text/html; charset=" + charset.name());
            Element meta2 = new Element("meta")
                    .attr("charset", charset.name());
            doc.head().appendChild(meta1).appendChild(meta2);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static String setEquiv(File htmlFile, Charset charset, String key, String value) {
        try {
            if (htmlFile == null || key == null || value == null) {
                return "InvalidData";
            }
            String html = TextFileTools.readTexts(htmlFile, charset);
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return "InvalidData";
            }
            if (!"Content-Type".equalsIgnoreCase(key)) {
                setCharset(doc, charset);
            }
            Elements children = doc.head().children();
            for (Element e : children) {
                if (!e.tagName().equalsIgnoreCase("meta") || !e.hasAttr("http-equiv")) {
                    continue;
                }
                if (key.equalsIgnoreCase(e.attr("http-equiv")) && e.hasAttr("content")) {
                    e.remove();
                }
            }
            Element meta1 = new Element("meta")
                    .attr("http-equiv", key)
                    .attr("content", value);
            doc.head().appendChild(meta1);
            return doc.outerHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String ignoreHead(String html) {
        try {
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return "InvalidData";
            }
            doc.head().empty();
            Charset charset = doc.charset();
            if (charset == null) {
                charset = Charset.forName("utf-8");
            }
            setCharset(doc, charset);
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String toUTF8(File htmlFile) {
        return setCharset(TextFileTools.readTexts(htmlFile), Charset.forName("utf-8"));
    }

    public static String setStyle(File htmlFile, Charset charset, String css, boolean ignoreOriginal) {
        try {
            if (htmlFile == null || css == null) {
                return "InvalidData";
            }
            String html = TextFileTools.readTexts(htmlFile, charset);
            Document doc = Jsoup.parse(html);
            if (doc == null) {
                return "InvalidData";
            }
            if (charset == null) {
                charset = TextFileTools.charset(htmlFile);
            }
            if (ignoreOriginal) {
                doc.head().empty();
            }
            setCharset(doc, charset);
            doc.head().appendChild(new Element("style").text(css));
            return doc.outerHtml();
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
            String namePrefix = FileNameTools.prefix(targetFile.getName());
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

    public static int replace(org.w3c.dom.Document doc, String findString,
            boolean reg, boolean caseInsensitive, String color, String bgColor, String font) {
        if (doc == null) {
            return 0;
        }
        NodeList nodeList = doc.getElementsByTagName("body");
        if (nodeList == null || nodeList.getLength() < 1) {
            return 0;
        }
        FindReplaceString finder = FindReplaceString.create().setOperation(FindReplaceString.Operation.FindNext)
                .setFindString(findString).setIsRegex(reg).setCaseInsensitive(caseInsensitive).setMultiline(true);
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
                finder.setInputString(texts).setAnchor(0).handleString();
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
                    //                    MyBoxLog.debug(e);
                }
            }
            replaced += unchecked;
            File tmpFile = FileTmpTools.getTempFile();
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
                        return FileNameTools.compareName(f1, f2);
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
