package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javafx.scene.control.IndexRange;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.controller.HtmlViewerController;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class HtmlTools {

    public static String HttpUserAgent = new WebView().getEngine().getUserAgent();
    public static String Indent = "    ";
    public static final String BaseStyle
            = ".center { text-align:center;  max-width:95%; }\n"
            + "table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
            + "th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
            + "th { font-weight:bold;  text-align:center;}\n"
            + "tr { height: 1.2em;  }\n"
            + ".boldText { font-weight:bold;  }\n";
    public static final String DefaultStyle
            = BaseStyle
            + ".valueText { color:#2e598a;  }\n";
    public static final String ConsoleStyle
            = "body { background-color:black; color:#CCFF99; }\n"
            + "table, th, td { border: #CCFF99; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:skyblue;  }\n"
            + BaseStyle;
    public static final String BlackboardStyle
            = "body { background-color:#336633; color:white; }\n"
            + "table, th, td { border: white; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:wheat;  }\n"
            + BaseStyle;
    public static final String LinkStyle
            = "body { background-color:transparent;  }\n"
            + "table { border-collapse:collapse; max-width:95%; }\n"
            + "table, th, td { border: 0px solid; }\n"
            + "td { padding:20px;  }\n"
            + BaseStyle;
    public static final String AgoStyle
            = "body { background-color:darkblue; color:white;  }\n"
            + "table, th, td { border: white; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:wheat;  }\n"
            + BaseStyle;
    public static final String BookStyle
            = "body { background-color:#F6F1EB; color:black;  }\n"
            + BaseStyle;

    public enum HtmlStyle {
        Default, Console, Blackboard, Ago, Book
    }

    public static URI uri(String address) {
        try {
            URI u;
            if (address.startsWith("file:")) {
                u = new URI(address);
            } else if (!address.startsWith("http")) {
                u = new URI("http://" + address);
            } else {
                u = new URI(address);
            }
            return u;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String path(URL url) {
        if (url == null) {
            return null;
        }
        String urlPath = url.getPath();
        int pos = urlPath.lastIndexOf("/");
        String path = pos < 0 ? "" : urlPath.substring(0, pos + 1);
        return path;
    }

    public static String fullPath(URL url) {
        if (url == null) {
            return null;
        }
        String fullPath = url.getProtocol() + "://" + url.getHost() + path(url);
        return fullPath;
    }

    public static String fullPath(String address) {
        try {
            String path = fullPath(new URL(address));
            return path == null ? address : path;
        } catch (Exception e) {
            return address;
        }
    }

    public static String file(URL url) {
        if (url == null) {
            return null;
        }
        try {
            String urlPath = url.getPath();
            int pos = urlPath.lastIndexOf("/");
            if (pos >= 0) {
                return pos < urlPath.length() - 1 ? urlPath.substring(pos + 1) : null;
            } else {
                return urlPath;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String filePrefix(URL url) {
        if (url == null) {
            return "";
        }
        String file = file(url);
        if (file == null) {
            return "";
        }
        int pos = file.lastIndexOf(".");
        file = pos < 0 ? file : file.substring(0, pos);
        return file;
    }

    public static String fileSuffix(URL url) {
        if (url == null) {
            return "";
        }
        String name = file(url);
        if (name == null) {
            return "";
        }
        int pos = name.lastIndexOf(".");
        name = pos < 0 ? "" : name.substring(pos);
        return name;
    }

    public static String styleValue(HtmlStyle style) {
        switch (style) {
            case Default:
                return DefaultStyle;
            case Console:
                return ConsoleStyle;
            case Blackboard:
                return BlackboardStyle;
//            case Link:
//                return LinkStyle;
            case Ago:
                return AgoStyle;
            case Book:
                return BookStyle;
        }
        return DefaultStyle;
    }

    public static String styleValue(String styleName) {
        return styleValue(styleName(styleName));
    }

    public static HtmlStyle styleName(String styleName) {
        for (HtmlStyle style : HtmlStyle.values()) {
            if (style.name().equals(styleName)
                    || message(style.name()).equals(styleName)) {
                return style;
            }
        }
        return HtmlStyle.Default;
    }

    public static String body(String html) {
        return body(html, true);
    }

    public static String body(String html, boolean withTag) {
        if (html == null) {
            return null;
        }
        int from = 0, to = html.length();
        FindReplaceString finder = FindReplaceString.finder(false, true).setInputString(html).setAnchor(0);
        IndexRange start = null;
        if (finder.setFindString("<body").run()) {
            start = finder.getStringRange();
        }
        if (start != null) {
            from = start.getStart();
            if (!withTag) {
                if (finder.setAnchor(from).setFindString(">").run()) {
                    IndexRange tagEnd = finder.getStringRange();
                    if (tagEnd != null) {
                        from = tagEnd.getEnd();
                    }
                }
            }
        } else {
            if (finder.setFindString("</head>").run()) {
                IndexRange headend = finder.getStringRange();
                if (headend != null) {
                    from = headend.getEnd();
                }
            }
        }
        IndexRange end = null;
        if (finder.setFindString("</body>").run()) {
            end = finder.getStringRange();
        }
        if (end != null) {
            to = withTag ? end.getEnd() : end.getStart();
        } else {
            if (finder.setFindString("</html>").run()) {
                IndexRange htmlend = finder.getStringRange();
                if (htmlend != null) {
                    to = htmlend.getStart();
                }
            }
        }
        return html.substring(from, to);
    }

    public static String preHtml(String html) {
        if (html == null) {
            return "";
        }
        IndexRange start = FindReplaceString.next(html, "<html", 0, false, true, false);
        if (start == null || start.getStart() == 0) {
            return "";
        }
        return html.substring(0, start.getStart());
    }

    public static String tag(String string, String tag, boolean withTag) {
        if (string == null || tag == null) {
            return null;
        }
        return tag(FindReplaceString.finder(true, true), string, tag, withTag);
    }

    public static String tag(FindReplaceString finder, String string, String tag, boolean withTag) {
        if (finder == null || string == null || tag == null) {
            return null;
        }
        finder.setInputString(string);
        if (!finder.setAnchor(0).setFindString("<" + tag + ">|<" + tag + " ").run()) {
            return null;
        }
        IndexRange start = finder.getStringRange();
        if (start == null) {
            return null;
        }
        int from = start.getStart();
        if (!withTag) {
            if (!finder.setAnchor(from).setFindString(">").run()) {
                return null;
            }
            IndexRange tagEnd = finder.getStringRange();
            if (tagEnd == null) {
                return null;
            }
            from = tagEnd.getEnd();
        }
        if (!finder.setAnchor(start.getEnd()).setFindString("</" + tag + ">").run()) {
            return null;
        }
        IndexRange end = finder.getStringRange();
        if (end == null) {
            return null;
        }
        int to = withTag ? end.getEnd() : end.getStart();
        return string.substring(from, to);
    }

    public static String htmlTitle(String html) {
        FindReplaceString finder = FindReplaceString.finder(true, true);
        return tag(finder, tag(finder, html, "head", true), "title", false);
    }

    public static String charsetName(String head) {
        if (head == null) {
            return null;
        }
        String flag = " charset=";
        int from = head.indexOf(flag);
        if (from < 0) {
            return null;
        }
        String s = head.substring(from + flag.length()).trim();
        int pos = s.indexOf(">");
        if (pos < 0) {
            return null;
        }
        s = s.substring(0, pos).trim();
        if (s.startsWith("\"")) {
            s = s.substring(1).trim();
            pos = s.indexOf("\"");
            if (pos >= 0) {
                return s.substring(0, pos).trim();
            } else {
                return s;
            }
        } else if (s.startsWith("\'")) {
            s = s.substring(1).trim();
            pos = s.indexOf("\'");
            if (pos >= 0) {
                return s.substring(0, pos).trim();
            } else {
                return s;
            }
        } else {
            pos = s.indexOf("\"");
            if (pos >= 0) {
                return s.substring(0, pos).trim();
            } else {
                pos = s.indexOf("\'");
                if (pos >= 0) {
                    return s.substring(0, pos).trim();
                } else {
                    pos = s.indexOf(";");
                    if (pos >= 0) {
                        return s.substring(0, pos).trim();
                    } else {
                        return s;
                    }
                }
            }
        }
    }

    public static Charset charset(String head) {
        try {
            if (head == null) {
                return null;
            }
            String name = charsetName(head);
            if (name == null) {
                return null;
            }
            return Charset.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static String emptyHmtl() {
        return htmlWithStyleValue(null, null, "<BODY>\n\n\n</BODY>\n");
    }

    public static File writeHtml(String html) {
        try {
            File htmFile = FileTools.getTempFile(".htm");
            FileTools.writeFile(htmFile, html);
            return htmFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void editHtml(String html) {
        try {
            File htmFile = writeHtml(html);
            FxmlStage.openHtmlEditor(null, htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void editHtml(String title, String body) {
        HtmlTools.editHtml(html(title, body));
    }

    public static HtmlViewerController viewHtml(String title, String body) {
        return FxmlStage.openHtmlViewer(null, body);
    }

    public static String html(String title, String body) {
        return htmlWithStyleValue(title, DefaultStyle, body);
    }

    public static String htmlPrefix(String title, String styleValue) {
        StringBuilder s = new StringBuilder();
        s.append("<!DOCTYPE html><HTML>\n").
                append(Indent).append("<HEAD>\n").
                append(Indent).append(Indent).append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
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
        return htmlPrefix(null, DefaultStyle);
    }

    public static String htmlWithStyleValue(String title, String styleValue, String body) {
        StringBuilder s = new StringBuilder();
        s.append(htmlPrefix(title, styleValue));
        s.append(body);
        s.append("</HTML>\n");
        return s.toString();
    }

    public static String html(String title, HtmlStyle style, String body) {
        return htmlWithStyleValue(title, styleValue(style), body);
    }

    public static String html(String title, String styleName, String body) {
        return html(title, styleName(styleName), body);
    }

    public static String setStyle(String html, HtmlStyle style) {
        return setStyleValue(html, styleValue(style));
    }

    public static String setStyle(String html, String styleName) {
        return setStyle(html, styleName(styleName));
    }

    public static String setStyleValue(String html, String styleValue) {
        String title = htmlTitle(html);
        String body = body(html, true);
        return htmlWithStyleValue(title, styleValue, body);
    }

    public static String readURL(String address) {
        try {
            if (address == null) {
                return null;
            }
            URL url = new URL(address);
            try ( BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static File readIcon(String address, boolean download) {
        try {
            if (address == null) {
                return null;
            }
            URL url = new URL(address);
            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return null;
            }
            File file = FxmlControl.getInternalFile("/icons/" + host + ".png", "icons", host + ".png");
            if (file == null || !file.exists()) {
                file = FxmlControl.getInternalFile("/icons/" + host + ".ico", "icons", host + ".ico");
                if ((file == null || !file.exists()) && download) {
                    file = new File(MyboxDataPath + File.separator + "icons" + File.separator + host + ".ico");
                    file = readIcon(address, file);
                }
            }
            return file != null && file.exists() ? file : null;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // https://www.cnblogs.com/luguo3000/p/3767380.html
    public static File readIcon(String address, File targetFile) {
        File actualTarget = readHostIcon(address, targetFile);
        if (actualTarget == null) {
            actualTarget = readHtmlIcon(address, targetFile);
        }
        if (actualTarget != null) {
            BufferedImage image = ImageFileReaders.readImage(actualTarget);
            if (image != null) {
                return actualTarget;
            } else {
                FileTools.delete(actualTarget);
            }
        }
        return null;
    }

    public static File readHostIcon(String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return null;
            }
            URL url = new URL(address);
            String iconUrl = "https://" + url.getHost() + "/favicon.ico";
            File actualTarget = downloadIcon(iconUrl, targetFile);
            if (actualTarget == null) {
                iconUrl = "http://" + url.getHost() + "/favicon.ico";
                actualTarget = downloadIcon(iconUrl, targetFile);
            }
            return actualTarget;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static File readHtmlIcon(String address, File targetFile) {
        try {
            if (address == null) {
                return null;
            }
            String iconUrl = htmlIconAddress(address);
            if (iconUrl == null) {
                return null;
            }
            return downloadIcon(iconUrl, targetFile);
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static File downloadIcon(String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return null;
            }
            File iconFile = url2File(address);
            if (iconFile == null || !iconFile.exists()) {
                return null;
            }
            String suffix = FileTools.getFileSuffix(address);
            File actualTarget = targetFile;
            if (suffix != null && !suffix.isBlank()) {
                actualTarget = new File(FileTools.replaceFileSuffix(targetFile.getAbsolutePath(), suffix));

            }
            FileTools.rename(iconFile, actualTarget);
            return actualTarget;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String htmlIconAddress(String address) {
        try {
            if (address == null) {
                return null;
            }
            String html = readURL(address);
            Pattern[] ICON_PATTERNS = new Pattern[]{
                Pattern.compile("rel=[\"']shortcut icon[\"'][^\r\n>]+?((?<=href=[\"']).+?(?=[\"']))"),
                Pattern.compile("((?<=href=[\"']).+?(?=[\"']))[^\r\n<]+?rel=[\"']shortcut icon[\"']")};
            for (Pattern iconPattern : ICON_PATTERNS) {
                Matcher matcher = iconPattern.matcher(html);
                if (matcher.find()) {
                    String iconUrl = matcher.group(1);
                    if (iconUrl.contains("http")) {
                        return iconUrl;
                    }
                    if (iconUrl.charAt(0) == '/') {
                        URL url = new URL(address);
                        iconUrl = url.getProtocol() + "://" + url.getHost() + iconUrl;
                    } else {
                        iconUrl = address + "/" + iconUrl;
                    }
                    return iconUrl;
                }
            }
            return null;
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<Link> links(Link addressLink, File path, Link.FilenameType nameType) {
        try {
            if (addressLink == null || path == null) {
                return null;
            }
            List<Link> validLinks = new ArrayList<>();
            URL url = addressLink.getUrl();
            Link coverLink = Link.create().setUrl(url).setAddress(url.toString())
                    .setName("0000_" + path.getName()).setTitle(path.getName());
            coverLink.setIndex(0).setFile(new File(coverLink.filename(path, nameType)).getAbsolutePath());
            validLinks.add(coverLink);
            String html = addressLink.getHtml();
            if (html == null) {
                html = FileTools.readTexts(new File(addressLink.getFile()));
            }
            List<Link> links = links(url, html);
            for (Link link : links) {
                if (link.getAddress() == null) {
                    continue;
                }
                String filename = link.filename(path, nameType);

                if (filename == null) {
                    continue;
                }
                link.setFile(new File(filename).getAbsolutePath());
                link.setIndex(validLinks.size());

                validLinks.add(link);
            }
            return validLinks;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean relinkPage(File httpFile,
            Map<File, Link> completedLinks, Map<String, File> completedAddresses) {
        try {
            if (httpFile == null || !httpFile.exists() || completedAddresses == null) {
                return false;
            }
            Link baseLink = completedLinks.get(httpFile);
            String html = FileTools.readTexts(httpFile);
            List<Link> links = links(baseLink.getUrl(), html);
            String replaced = "", unchecked = html;
            int pos;
            for (Link link : links) {
                try {
                    String originalAddress = link.getAddressOriginal();
                    pos = unchecked.indexOf("\"" + originalAddress + "\"");
                    if (pos < 0) {
                        pos = unchecked.indexOf("\'" + originalAddress + "\'");
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
            File tmpFile = FileTools.getTempFile();
            FileTools.writeFile(tmpFile, replaced, FileTools.charset(httpFile));

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
                        return FileTools.compareFilename(f1, f2);
                    }
                });
                File frameFile = new File(path.getAbsolutePath() + File.separator + "0000_" + message("PathIndex") + ".html");
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

    // files should have been sorted
    public static boolean generateFrameset(List<File> files, File targetFile) {
        try {
            if (files == null || files.isEmpty()) {
                return false;
            }
            String namePrefix = FileTools.getFilePrefix(targetFile.getName());
            File navFile = new File(targetFile.getParent() + File.separator + namePrefix + "_nav.html");
            StringBuilder nav = new StringBuilder();
            File first = null;
            for (File file : files) {
                String filepath = file.getAbsolutePath();
                String name = file.getName();
                if (filepath.equals(targetFile.getAbsolutePath()) || filepath.equals(navFile.getAbsolutePath())) {
                    FileTools.delete(file);
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
            FileTools.writeFile(navFile, HtmlTools.html(message("PathIndex"), body));
            String frameset = " <FRAMESET border=2 cols=400,*>\n"
                    + "<FRAME name=nav src=\"" + namePrefix + "_nav.html\" />\n";
            if (first.getParent().equals(targetFile.getParent())) {
                frameset += "<FRAME name=main src=\"" + first.getName() + "\" />\n";
            } else {
                frameset += "<FRAME name=main src=\"" + first.toURI() + "\" />\n";
            }
            frameset += "</FRAMESET>";
            File frameFile = new File(targetFile.getParent() + File.separator + namePrefix + ".html");
            FileTools.writeFile(frameFile, HtmlTools.html(null, frameset));
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
            String listPrefix = "0000_" + message("PathIndex") + "_list";
            StringBuilder csv = new StringBuilder();
            String s = message("Address") + "," + message("File") + "," + message("Title") + "," + message("Name") + "," + message("Index") + "," + message("Time") + "\n";
            csv.append(s);
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("File"), message("Address"), message("Title"), message("Name"), message("Index"), message("Time")));
            StringTable table = new StringTable(names, message("DownloadHistory"));
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(listPrefix)) {
                    FileTools.delete(file);
                } else {
                    Link link = completedLinks.get(file);
                    if (link == null) {
                        s = file.getAbsolutePath() + ",,,," + DateTools.datetimeToString(FileTools.createTime(file));
                        csv.append(s);
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(file.getAbsolutePath(), "", "", "", "", DateTools.datetimeToString(FileTools.createTime(file))));
                        table.add(row);
                    } else {
                        s = link.getUrl() + "," + file.getAbsolutePath() + ","
                                + (link.getTitle() != null ? link.getTitle() : "") + ","
                                + (link.getName() != null ? link.getName() : "") + ","
                                + (link.getIndex() > 0 ? link.getIndex() : "") + ","
                                + (link.getDlTime() != null ? DateTools.datetimeToString(link.getDlTime()) : "") + "\n";
                        csv.append(s);
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(file.getAbsolutePath(),
                                link.getUrl().toString(),
                                link.getTitle() != null ? link.getTitle() : "",
                                link.getName() != null ? link.getName() : "",
                                link.getIndex() > 0 ? link.getIndex() + "" : "",
                                link.getDlTime() != null ? DateTools.datetimeToString(link.getDlTime()) : ""
                        ));
                        table.add(row);
                    }
                }
            }
            String filename = path.getAbsolutePath() + File.separator + listPrefix + ".csv";
            FileTools.writeFile(new File(filename), csv.toString());
            filename = path.getAbsolutePath() + File.separator + listPrefix + ".html";
            FileTools.writeFile(new File(filename), table.html());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static Map<String, String> readCookie(WebEngine webEngine) {
        try {
            String s = (String) webEngine.executeScript("document.cookie;");
            String[] vs = s.split(";");
            Map<String, String> m = new HashMap<>();
            for (String v : vs) {
                String[] vv = v.split("=");
                if (vv.length < 2) {
                    continue;
                }
                m.put(vv[0].trim(), vv[1].trim());
            }
            return m;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String textToHtml(String text) {
        String body = "" + FindReplaceString.replaceAll(text, "\n", "</br>");
        return html(null, body);
    }

    public static boolean isUTF8(File htmlFile) {
        Charset charset = htmlCharset(htmlFile);
        return charset == null || charset.equals(Charset.forName("utf-8"));
    }

    public static String toUTF8(File htmlFile, boolean must) {
        return setCharset(htmlFile, Charset.forName("utf-8"), must);
    }

    public static Charset htmlCharset(File htmlFile) {
        try {
            if (htmlFile == null) {
                return null;
            }
            Charset fileCharset = FileTools.charset(htmlFile);
            String html = FileTools.readTexts(htmlFile, fileCharset);
            String head = tag(html, "head", true);
            if (head == null) {
                return fileCharset;
            } else {
                Charset charset = charset(head);
                if (charset == null) {
                    return fileCharset;
                } else {
                    return charset;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Charset htmlCharset(String html) {
        try {
            if (html == null) {
                return null;
            }
            Charset charset = charset(tag(html, "head", true));
            if (charset == null) {
                return Charset.forName("UTF-8");
            } else {
                return charset;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String setCharset(File htmlFile, Charset charset, boolean must) {
        try {
            if (htmlFile == null || charset == null) {
                return "InvalidData";
            }
            Charset fileCharset = FileTools.charset(htmlFile);
            String html = FileTools.readTexts(htmlFile, fileCharset);
            String head = tag(html, "head", false);
            String preHtml = preHtml(html);
            if (head == null) {
                if (!must && fileCharset.equals(charset)) {
                    return "NeedNot";
                }
                html = preHtml + "<html>\n"
                        + "    <head>\n"
                        + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + charset.name() + "\" />\n"
                        + "    </head>\n"
                        + html + "\n"
                        + "</html>";
            } else {
                String newHead;
                Charset headCharset = charset(head);
                if (!must && fileCharset.equals(charset)
                        && (headCharset == null || charset.equals(headCharset))) {
                    return "NeedNot";
                }
                if (headCharset != null) {
                    newHead = FindReplaceString.replace(head, headCharset.name(), charset.name(), 0, false, true, false);
                } else {
                    newHead = head + "\n<meta charset=\"text/html; charset=" + charset.name() + "\"/>";
                }
                html = preHtml + "<html>\n"
                        + "    <head>\n"
                        + newHead + "\n"
                        + "    </head>\n"
                        + body(html, true) + "\n"
                        + "</html>";
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e, charset.displayName());
            return null;
        }
    }

    public static String setStyle(File htmlFile, String css, boolean ignoreOriginal) {
        try {
            if (htmlFile == null || css == null) {
                return "InvalidData";
            }
            Charset fileCharset = FileTools.charset(htmlFile);
            String html = FileTools.readTexts(htmlFile, fileCharset);
            String preHtml = preHtml(html);
            String head;
            if (ignoreOriginal) {
                head = "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + fileCharset.name() + "\" />\n";
            } else {
                head = tag(html, "head", false);
                if (head == null) {
                    head = "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + fileCharset.name() + "\" />\n";
                }
            }
            html = preHtml + "<html>\n"
                    + "    <head>\n"
                    + head + "\n"
                    + "        <style type=\"text/css\">/>\n"
                    + css
                    + "        </style>/>\n"
                    + "    </head>\n"
                    + body(html, true) + "\n"
                    + "</html>";
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Charset charset(Document doc) {
        if (doc == null) {
            return Charset.defaultCharset();
        }
        NodeList nodeList = doc.getElementsByTagName("meta");
        if (nodeList == null || nodeList.getLength() < 1) {
            return Charset.defaultCharset();
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node == null) {
                continue;
            }
            Element element = (Element) node;
            String attr = element.getAttribute("content");
            if (attr != null) {
                attr = attr.toLowerCase();
                int pos = attr.indexOf("charset=");
                if (pos >= 0) {
                    String charset = attr.substring(pos + "charset=".length());
                    pos = charset.indexOf(";");
                    if (pos >= 0) {
                        charset = charset.substring(0, pos);
                    }
                    return Charset.forName(charset);
                }
            }
            attr = element.getAttribute("charset");
            if (attr != null) {
                return Charset.forName(attr);
            }
        }
        return Charset.defaultCharset();
    }

    public static String toc(Document doc, int indentSize) {
        if (doc == null) {
            return null;
        }
        NodeList nodeList = doc.getElementsByTagName("body");
        if (nodeList == null || nodeList.getLength() < 1) {
            return null;
        }
        String indent = "";
        for (int i = 0; i < indentSize; i++) {
            indent += " ";
        }
        StringBuilder s = new StringBuilder();
        toc(nodeList.item(0), indent, s);
        return s.toString();
    }

    public static void toc(Node node, String indent, StringBuilder s) {
        if (node == null || s == null || indent == null) {
            return;
        }
        String tag = node.getNodeName();
        String content = node.getTextContent();
        if (tag != null && content != null && !content.isBlank()) {
            content += "\n";
            if (tag.equalsIgnoreCase("h1")) {
                s.append(content);
            } else if (tag.equalsIgnoreCase("h2")) {
                s.append(indent).append(content);
            } else if (tag.equalsIgnoreCase("h3")) {
                s.append(indent).append(indent).append(content);
            } else if (tag.equalsIgnoreCase("h4")) {
                s.append(indent).append(indent).append(indent).append(content);
            } else if (tag.equalsIgnoreCase("h5")) {
                s.append(indent).append(indent).append(indent).append(indent).append(content);
            } else if (tag.equalsIgnoreCase("h6")) {
                s.append(indent).append(indent).append(indent).append(indent).append(indent).append(content);
            }
        }
        Node child = node.getFirstChild();
        while (child != null) {
            toc(child, indent, s);
            child = child.getNextSibling();
        }
    }

    public static String toc(String html, int indentSize) {
        try {
            if (html == null) {
                return null;
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            if (doc == null) {
                return null;
            }
            org.jsoup.nodes.Element body = doc.body();
            if (body == null) {
                return null;
            }
            String indent = "";
            for (int i = 0; i < indentSize; i++) {
                indent += " ";
            }
            StringBuilder s = new StringBuilder();
            toc(body, indent, s);
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void toc(org.jsoup.nodes.Element element, String indent, StringBuilder s) {
        try {
            if (element == null || s == null || indent == null) {
                return;
            }
            String tag = element.tagName();
            String content = element.text();
            if (tag != null && content != null && !content.isBlank()) {
                content += "\n";
                if (tag.equalsIgnoreCase("h1")) {
                    s.append(content);
                } else if (tag.equalsIgnoreCase("h2")) {
                    s.append(indent).append(content);
                } else if (tag.equalsIgnoreCase("h3")) {
                    s.append(indent).append(indent).append(content);
                } else if (tag.equalsIgnoreCase("h4")) {
                    s.append(indent).append(indent).append(indent).append(content);
                } else if (tag.equalsIgnoreCase("h5")) {
                    s.append(indent).append(indent).append(indent).append(indent).append(content);
                } else if (tag.equalsIgnoreCase("h6")) {
                    s.append(indent).append(indent).append(indent).append(indent).append(indent).append(content);
                }
            }
            org.jsoup.select.Elements children = element.children();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (org.jsoup.nodes.Element child : children) {
                toc(child, indent, s);
            }
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
        FindReplaceString finder = FindReplaceString.create()
                .setOperation(FindReplaceString.Operation.FindNext).setFindString(findString)
                .setIsRegex(reg).setCaseInsensitive(caseInsensitive).setMultiline(true);
        String replaceSuffix = " style=\"color:" + color
                + "; background: " + bgColor
                + "; font-size:" + font + ";\">" + findString + "</span>";
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

    public static String doc2html(Document doc, String charset) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));
            baos.flush();
            baos.close();
            return baos.toString(charset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<Link> links(URL baseURL, String html) {
        if (html == null) {
            return null;
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        org.jsoup.select.Elements elements = doc.getElementsByTag("a");
        List<Link> links = new ArrayList<>();
        for (org.jsoup.nodes.Element element : elements) {
            String linkAddress = element.attr("href");
            try {
                URL url = new URL(baseURL, linkAddress);
                Link link = Link.create()
                        .setUrl(url).setAddress(url.toString())
                        .setAddressOriginal(linkAddress)
                        .setName(element.text())
                        .setTitle(element.attr("title"))
                        .setIndex(links.size());
                links.add(link);
            } catch (Exception e) {
//                MyBoxLog.console(linkAddress);
            }
        }
        return links;
    }

    public static int frameIndex(WebEngine webEngine, String frameName) {
        try {
            if (frameName == null) {
                return -1;
            }
            Object c = webEngine.executeScript("function checkFrameIndex(frameName) { "
                    + "  for (i=0; i<window.frames.length; i++) { "
                    + "     if ( window.frames[i].name == frameName ) return i ;"
                    + "  };"
                    + "  return -1; "
                    + "};"
                    + "checkFrameIndex(\"" + frameName + "\");");
            if (c == null) {
                return -1;
            }
            return ((int) c);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return -1;
        }
    }

    public static File url2File(String urlAddress) {
        try {
            if (urlAddress == null) {
                return null;
            }
            URL url = new URL(urlAddress);
            File tmpFile = FileTools.getTempFile();
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                FileTools.copyFile(new File(url.getFile()), tmpFile);

            } else if ("https".equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
                sc.init(null, null, null);
                connection.setSSLSocketFactory(sc.getSocketFactory());
                connection.setConnectTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
                connection.setReadTimeout(AppVariables.getUserConfigInt("WebReadTimeout", 10000));
                connection.setRequestProperty("User-Agent", HttpUserAgent);
                connection.connect();
                if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                    try ( BufferedInputStream inStream = new BufferedInputStream(new GZIPInputStream(connection.getInputStream()));
                             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                } else {
                    try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                }
            } else if ("http".equalsIgnoreCase(url.getProtocol())) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", HttpUserAgent);
                connection.setConnectTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
                connection.setReadTimeout(AppVariables.getUserConfigInt("WebReadTimeout", 10000));
                connection.setUseCaches(false);
                connection.connect();
                if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                    try ( BufferedInputStream inStream = new BufferedInputStream(new GZIPInputStream(connection.getInputStream()));
                             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                } else {
                    try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                }
            }
            if (tmpFile == null || !tmpFile.exists()) {
                return null;
            }
            if (tmpFile.length() == 0) {
                FileTools.delete(tmpFile);
                return null;
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return null;
        }
    }

    public static String url2text(String urlAddress) {
        File tmpFile = url2File(urlAddress);
        if (tmpFile == null) {
            return null;
        }
        return FileTools.readTexts(tmpFile);
    }

    public static org.jsoup.nodes.Document url2doc(String urlAddress) {
        try {
            String html = url2text(urlAddress);
            if (html == null) {
                return null;
            }
            return Jsoup.parse(html);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static org.jsoup.nodes.Document file2doc(File file) {
        try {
            String html = FileTools.readTexts(file);;
            if (html == null) {
                return null;
            }
            return Jsoup.parse(html);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String checkURL(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String address = value;
        String addressS = address.toLowerCase();
        if (addressS.startsWith("file:/")
                || addressS.startsWith("http://")
                || addressS.startsWith("https://")) {

        } else if (address.startsWith("//")) {
            address = "http:" + value;
        } else {
            File file = new File(address);
            if (file.exists()) {
                address = file.toURI().toString();
            } else {
                address = "http://" + value;
            }
        }
        address = decodeURL(address);
        return address;
    }

    public static String decodeURL(String value) {
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, Charset.forName("UTF-8"));
    }

    public static String decodeURL(File file) {
        if (file == null) {
            return null;
        }
        return decodeURL(file.toURI().toString());
    }

    public static String encodeEscape(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\s", "&nbsp;")
                .replaceAll("©", "&copy;")
                .replaceAll("®", "&reg;")
                .replaceAll("™", "&trade;");
    }

    public static String fullAddress(String baseAddress, String address) {
        try {
            URL baseURL = new URL(baseAddress);
            URL url = new URL(baseURL, address);
            return url.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return address;
        }
    }

}
