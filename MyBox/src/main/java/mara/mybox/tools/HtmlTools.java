package mara.mybox.tools;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.IndexRange;
import javafx.scene.web.WebEngine;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.controller.HtmlViewerController;
import mara.mybox.data.DownloadHistory;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import mara.mybox.db.table.TableDownloadHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import net.sf.image4j.codec.ico.ICODecoder;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class HtmlTools {

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
        String urlPath = url.getPath();
        int pos = urlPath.lastIndexOf("/");
        if (pos >= 0) {
            return pos < urlPath.length() - 1 ? urlPath.substring(pos + 1) : null;
        } else {
            return urlPath;
        }
    }

    public static String filePrefix(URL url) {
        if (url == null) {
            return null;
        }
        String file = file(url);
        int pos = file.lastIndexOf(".");
        file = pos < 0 ? file : file.substring(0, pos);
        return file;
    }

    public static String fileSuffix(URL url) {
        if (url == null) {
            return "";
        }
        String name = file(url);
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

    // Not include <body> and </body>
    public static String bodyWithoutTag(String html) {
        if (html == null) {
            return null;
        }
        int from = 0, to = html.length();
        IndexRange start = FindReplaceString.next(html, "<body", 0, true, true, false);
        if (start != null) {
            start = FindReplaceString.next(html, ">", start.getEnd(), false, true, false);
            from = start.getEnd();
        } else {
            IndexRange headend = FindReplaceString.next(html, "</head>", 0, false, true, false);
            if (headend != null) {
                from = headend.getEnd();
            }
        }
        IndexRange end = FindReplaceString.next(html, "</body>", from, false, true, false);
        if (end != null) {
            to = end.getStart();
        } else {
            IndexRange htmlend = FindReplaceString.next(html, "</html>", 0, false, true, false);
            if (htmlend != null) {
                to = htmlend.getStart();
            }
        }
        return html.substring(from, to);
    }

    // Include <body> and </body>
    public static String body(String html) {
        if (html == null) {
            return null;
        }
        int from = 0, to = html.length();
        IndexRange start = FindReplaceString.next(html, "<body", 0, false, true, false);
        if (start != null) {
            from = start.getStart();
        } else {
            IndexRange headend = FindReplaceString.next(html, "</head>", 0, false, true, false);
            if (headend != null) {
                from = headend.getEnd();
            }
        }
        IndexRange end = FindReplaceString.next(html, "</body>", from, false, true, false);
        if (end != null) {
            to = end.getEnd();
        } else {
            IndexRange htmlend = FindReplaceString.next(html, "</html>", 0, false, true, false);
            if (htmlend != null) {
                to = htmlend.getStart();
            }
        }
        return html.substring(from, to);
    }

    // Not include <head> and </head>
    public static String headWithoutTag(String html) {
        if (html == null) {
            return null;
        }
        int from = 0;
        IndexRange start = FindReplaceString.next(html, "<head", 0, false, true, false);
        if (start == null) {
            return null;
        }
        start = FindReplaceString.next(html, ">", start.getEnd(), false, true, false);
        from = start.getEnd();
        IndexRange end = FindReplaceString.next(html, "</head>", from, false, true, false);
        if (end == null) {
            return null;
        }
        int to = end.getStart();
        return html.substring(from, to);
    }

    // Include <head> and </head>
    public static String head(String html) {
        if (html == null) {
            return null;
        }
        int from = 0;
        IndexRange start = FindReplaceString.next(html, "<head", 0, false, true, false);
        if (start == null) {
            return null;
        }
        from = start.getStart();
        IndexRange end = FindReplaceString.next(html, "</head>", from, false, true, false);
        if (end == null) {
            return null;
        }
        int to = end.getEnd();
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

    public static String title(String string) {
        if (string == null) {
            return null;
        }
        int from = 0;
        IndexRange start = FindReplaceString.next(string, "<title>", 0, false, true, false);
        if (start == null) {
            return null;
        }
        from = start.getEnd();
        IndexRange end = FindReplaceString.next(string, "</title>", from, false, true, false);
        if (end == null) {
            return null;
        }
        int to = end.getStart();
        return string.substring(from, to);
    }

    public static String htmlTitle(String html) {
        return title(head(html));
    }

    public static String title(File file) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            try (final BufferedReader reader = new BufferedReader(new FileReader(file, FileTools.charset(file)))) {
                String line, title;
                while ((line = reader.readLine()) != null) {
                    title = title(line);
                    if (title != null) {
                        return title.isBlank() ? null : title;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String charsetName(String head) {
        if (head == null) {
            return null;
        }
        IndexRange start = FindReplaceString.next(head, "charset=", 0, false, true, false);
        if (start == null) {
            return null;
        }
        String s = head.substring(start.getEnd()).trim();
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
            String name = charsetName(head);
            if (name == null) {
                return null;
            }
            return Charset.forName(name);
        } catch (Exception e) {
            return null;
        }
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

//    public static void editHtml(String title, String style, String body) {
//        HtmlTools.editHtml(html(title, style, body));
//    }
    public static HtmlViewerController viewHtml(String title, String body) {
        return FxmlStage.openHtmlViewer(null, body);
    }

    public static String html(String title, String body) {
        return htmlWithStyleValue(title, DefaultStyle, body);
    }

    public static String htmlPrefix(String title, String styleValue) {
        StringBuilder s = new StringBuilder();
        s.append("<HTML>\n").
                append(Indent).append("<HEAD>\n").
                append(Indent).append(Indent).append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
        if (title != null && !title.trim().isEmpty()) {
            s.append(Indent).append(Indent).append("<TITLE>").append(title).append("</TITLE>\n");
        }
        if (styleValue != null && !styleValue.trim().isEmpty()) {
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
        String body = body(html);
        return htmlWithStyleValue(title, styleValue, body);
    }

    public static String downloadHttp(String address, File targetFile) {
        try {
            return downloadHttp(new URL(address), targetFile);
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String downloadHttp(URL url, File targetFile) {
        try {
            if (targetFile == null || url == null) {
                return message("InvalidParameters");
            }
            if ("https".equals(url.getProtocol().toLowerCase())) {
                return downloadHttps(url, targetFile);
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000))");
                connection.setConnectTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
                connection.setReadTimeout(AppVariables.getUserConfigInt("WebReadTimeout", 10000));
                connection.connect();
                File tmpFile = FileTools.getTempFile();
                try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                    byte[] buf = new byte[CommonValues.IOBufferLength];
                    int len;
                    while ((len = inStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                }
                FileTools.rename(tmpFile, targetFile);
                if (targetFile.exists()) {
                    return null;
                } else {
                    return message("Failed");
                }
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String downloadHttps(URL url, File targetFile) {
        try {
            if (targetFile == null || url == null) {
                return message("InvalidParameters");
            }
            if ("http".equals(url.getProtocol().toLowerCase())) {
                return downloadHttp(url, targetFile);
            }
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
            sc.init(null, NetworkTools.trustAllManager(), new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(NetworkTools.trustAllVerifier());
//            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000))");
            connection.setConnectTimeout(AppVariables.getUserConfigInt("WebConnectTimeout", 10000));
            connection.setReadTimeout(AppVariables.getUserConfigInt("WebReadTimeout", 10000));
            connection.connect();
            File tmpFile = FileTools.getTempFile();
            try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
            FileTools.rename(tmpFile, targetFile);
            if (targetFile.exists()) {
                return null;
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static boolean downloadIcon(String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return false;
            }
            URL url = new URL(address);
//            if (TableBrowserBypassSSL.bypass(url.getHost())) {
//                NetworkTools.trustAll();
//            } else {
//                NetworkTools.defaultSSL();
//            }
            BufferedImage image = null;
            try ( InputStream in = new BufferedInputStream(url.openStream())) {
                try {
                    List<BufferedImage> images = ICODecoder.read(in);
                    if (images != null && !images.isEmpty()) {
                        image = images.get(0);
                    }
                } catch (Exception e) {
//                    MyBoxLog.debug(e.toString());
                }
                if (image == null) {
                    try {
                        image = ImageIO.read(in);
                    } catch (Exception e) {
//                        MyBoxLog.debug(e.toString());
                    }
                }
            }
            if (image != null) {
                String format = FileTools.getFileSuffix(targetFile);
                if (format == null || format.isBlank()) {
                    format = "png";
                }
                ImageIO.write(image, format, targetFile);
            }
//            NetworkTools.defaultSSL();
            return targetFile.exists();
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static String readURL(String address) {
        try {
            if (address == null) {
                return null;
            }
            URL url = new URL(address);
//            if (TableBrowserBypassSSL.bypass(url.getHost())) {
//                NetworkTools.trustAll();
//            } else {
//                NetworkTools.defaultSSL();
//            }
            try ( BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine).append("\n");
                }
//                NetworkTools.defaultSSL();
                return sb.toString();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // https://www.cnblogs.com/luguo3000/p/3767380.html
    public static boolean readIcon(String address, File targetFile) {
        if (readHostIcon(address, targetFile)) {
            return true;
        }
        return readHtmlIcon(address, targetFile);
    }

    public static boolean readHostIcon(String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return false;
            }
            URL url = new URL(address);
            String iconUrl = url.getProtocol() + "://" + url.getHost() + "/favicon.ico";
            return downloadIcon(iconUrl, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean readHtmlIcon(String address, File targetFile) {
        try {
            if (address == null) {
                return false;
            }
            String iconUrl = htmlIconAddress(address);
            if (iconUrl == null) {
                return false;
            }
            String suffix = FileTools.getFilePrefix(iconUrl);
            if (suffix == null || suffix.isBlank()) {
                return downloadIcon(iconUrl, targetFile);
            } else {
                return downloadIcon(iconUrl,
                        new File(FileTools.replaceFileSuffix(targetFile.getAbsolutePath(), suffix)));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<Link> hrefLinksInLine(String line) {
        try {
            if (line == null || line.isBlank()) {
                return null;
            }
            List<Link> links = new ArrayList<>();
            int pos;
            String string = line;
            String address;
            String addressOriginal;
            String name;
            String title = null;
            while (!string.isBlank()) {
                pos = string.toLowerCase().indexOf("<a ");
                if (pos < 0) {
                    break;
                }
                String aString = string.substring(pos + 2);
                String aStringLowerCase = aString.toLowerCase();
                pos = aStringLowerCase.indexOf(" href");
                if (pos < 0) {
                    string = aString.substring(pos + 5);
                    continue;
                }
                String hrefString = aString.substring(pos + 5).trim();
                if (!hrefString.startsWith("=")) {
                    string = hrefString.substring(pos);
                    continue;
                }
                hrefString = hrefString.substring(1).trim();
                //                MyBoxLog.debug("hrefString: " + hrefString);
                if (hrefString.startsWith("\"")) {
                    hrefString = hrefString.substring(1);
                    pos = hrefString.indexOf("\"");
                    if (pos <= 0) {
                        string = hrefString.substring(pos);
                        continue;
                    }
                    addressOriginal = "\"" + hrefString.substring(0, pos) + "\"";
                } else if (hrefString.startsWith("'")) {
                    hrefString = hrefString.substring(1);
                    pos = hrefString.indexOf("'");
                    if (pos <= 0) {
                        string = hrefString.substring(pos);
                        continue;
                    }
                    addressOriginal = "'" + hrefString.substring(0, pos) + "'";
                } else {
                    string = hrefString;
                    continue;
                }
                address = hrefString.substring(0, pos);
                if (address.toLowerCase().startsWith("javascript:") || address.startsWith("#")) {
                    string = hrefString.substring(pos);
                    continue;
                }
                pos = aStringLowerCase.indexOf(" title");
                if (pos >= 0) {
                    String titleString = aString.substring(pos + 6).trim();
                    if (titleString.startsWith("=")) {
                        titleString = titleString.substring(1).trim();
                        if (titleString.startsWith("\"")) {
                            titleString = titleString.substring(1);
                            pos = titleString.indexOf("\"");
                            if (pos > 0) {
                                title = titleString.substring(0, pos);
                            }
                        } else if (titleString.startsWith("'")) {
                            titleString = titleString.substring(1);
                            pos = titleString.indexOf("'");
                            if (pos > 0) {
                                title = titleString.substring(0, pos);
                            }
                        }
                    }
                }
                string = "";
                pos = aString.indexOf(">");
                if (pos < 0) {
                    name = "";
                } else {
                    String nameString = aString.substring(pos + 1);
                    pos = nameString.toLowerCase().indexOf("</a>");
                    if (pos < 0) {
                        name = nameString;
                    } else {
                        name = nameString.substring(0, pos);
                        string = nameString.substring(pos + 4);
                    }
                    //                    MyBoxLog.debug("nameString: " + nameString + " name: " + name);
                }
                Link alink = Link.create().setAddress(address.trim()).setAddressOriginal(addressOriginal).setName(FileTools.filenameFilter(name.trim())).setTitle(title == null ? null : FileTools.filenameFilter(title.trim()));
//                MyBoxLog.debug("address: " + address + " title: " + title + " name: " + name);
                links.add(alink);
                if (pos < 0) {
                    break;
                }
            }
            return links;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<Link> openLinksInLine(String line) {
        try {
            if (line == null || line.isBlank()) {
                return null;
            }
            List<Link> links = new ArrayList<>();
            int pos;
            String string = line;
            String address;
            String name;
            while (!string.isBlank()) {
                pos = string.toLowerCase().indexOf("window.open(");
                if (pos < 0) {
                    break;
                }
                String openString = string.substring(pos + 12).trim();
                if (openString.startsWith("\"")) {
                    openString = openString.substring(1);
                    pos = openString.indexOf("\"");
                    if (pos <= 0) {
                        break;
                    }
                } else if (openString.startsWith("'")) {
                    openString = openString.substring(1);
                    pos = openString.indexOf("'");
                    if (pos <= 0) {
                        break;
                    }
                }
                address = openString.substring(0, pos);
                string = "";
                pos = openString.indexOf(">");
                if (pos < 0) {
                    name = "";
                } else {
                    String nameString = openString.substring(pos + 1);
                    pos = nameString.indexOf("<");
                    if (pos < 0) {
                        name = nameString;
                    } else {
                        name = nameString.substring(0, pos);
                        string = nameString.substring(pos + 1);
                    }
                }
                Link alink = Link.create().setAddress(address.trim()).setName(FileTools.filenameFilter(name.trim()));
                links.add(alink);
                if (pos < 0) {
                    break;
                }
            }
            return links;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // Parser of flexmark-Java can do this better.
    // Just leave these codes here cause they cost lot of my time~
    public static List<Link> linksInAddress(String address, File httpFile,
            File path, Link.FilenameType nameType) {
        try {
            if (address == null || httpFile == null || path == null) {
                return null;
            }
            List<Link> validLinks = new ArrayList<>();
            URL url = new URL(address);
            Link httplink = Link.create().setUrl(url).setAddress(url.toString())
                    .setName(path.getName()).setTitle(path.getName());
            httplink.setFile(httplink.filename(path, nameType));
            validLinks.add(httplink);

            String linkRoot = url.getProtocol() + "://" + url.getHost();
            String linkPath;
            String urlString = url.toString();
            int pos = urlString.lastIndexOf("/");
            if (pos < 0) {
                linkPath = "/";
            } else {
                linkPath = url.toString().substring(0, pos);
            }
            List<Link> links = linksInFile(httpFile);
            for (Link link : links) {
                String linkAddress = link.getAddress();
                if (!linkAddress.toLowerCase().startsWith("http")) {
                    if (linkAddress.startsWith("/")) {
                        linkAddress = linkRoot + linkAddress;
                    } else {
                        linkAddress = linkPath + "/" + linkAddress;
                    }
                    //                    MyBoxLog.debug(link.getAddress() + "  --> " + linkAddress);
                }
                try {
                    URL linkURL = new URL(linkAddress);
                    link.setUrl(linkURL);
                    link.setAddress(linkURL.toString());
                    String filename = link.filename(path, nameType);
                    link.setFile(new File(filename).getAbsolutePath());
                    //                    MyBoxLog.debug(link.getAddress() + "  --> " + link.getFilename());
                    validLinks.add(link);
                } catch (Exception e) {
                }
            }
            return validLinks;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<Link> linksInFile(File file) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            List<Link> links = new ArrayList<>();
            try (final BufferedReader reader = new BufferedReader(new FileReader(file, FileTools.charset(file)))) {
                String line;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    List<Link> hrefLinks = hrefLinksInLine(line);
                    if (hrefLinks != null) {
                        for (Link link : hrefLinks) {
                            link.setIndex(++index);
                            links.add(link);
                        }
                    }
                    List<Link> openLinks = openLinksInLine(line);
                    if (openLinks != null) {
                        for (Link link : openLinks) {
                            link.setIndex(++index);
                            links.add(link);
                        }
                    }
                }
            }
            return links;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<Link> addressLinks(Link addressLink,
            Parser htmlParser, FlexmarkHtmlConverter mdConverter,
            File path, Link.FilenameType nameType) {
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

            String linkRoot = url.getProtocol() + "://" + url.getHost();
            String linkPath = fullPath(url);
            String html = FileTools.readTexts(new File(addressLink.getFile()));
            String md = mdConverter.convert(html);
            Node document = htmlParser.parse(md);
            List<Link> links = new ArrayList<>();
            MarkdownTools.links(document, links);
            for (Link link : links) {
                String linkAddress = link.getAddress();
                URL linkURL;
                try {
                    linkURL = new URL(linkAddress);
                } catch (Exception e) {
                    String fullAddress = linkAddress;
                    if (fullAddress.startsWith("/")) {
                        fullAddress = linkRoot + linkAddress;
                    } else {
                        fullAddress = linkPath + linkAddress;
                    }
                    try {
                        linkURL = new URL(fullAddress);
//                        MyBoxLog.debug(linkAddress + "   " + fullAddress);
                    } catch (Exception ex) {
//                        MyBoxLog.debug(linkAddress);
                        continue;
                    }
                }
                link.setUrl(linkURL);
                link.setAddress(linkURL.toString());
                String filename = link.filename(path, nameType);
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

    public static boolean relinkPage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login);
                final PreparedStatement filenameQeury = conn.prepareStatement(TableDownloadHistory.FilenameQeury);
                final PreparedStatement urlQuery = conn.prepareStatement(TableDownloadHistory.UrlQeury)) {
            return relinkPage(conn, filenameQeury, urlQuery, file, null);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean relinkPage(Connection conn,
            PreparedStatement filenameQeury, PreparedStatement urlQuery,
            File file, DownloadHistory fileHis) {
        try {
            if (file == null || !file.exists()) {
                return false;
            }
            TableDownloadHistory tableDownloadHistory = new TableDownloadHistory();
            DownloadHistory fileHistory = fileHis;
            if (fileHis == null) {
                fileHistory = tableDownloadHistory.query(conn, filenameQeury, file.getAbsolutePath());
            }
            String linkRoot = null;
            String linkPath = null;
            try {
                URL url = new URL(fileHistory.getUrl());
                linkRoot = url.getProtocol() + "://" + url.getHost();
                linkPath = path(url);
            } catch (Exception e) {
            }
            List<Link> links = new ArrayList<>();
            File tmpFile = FileTools.getTempFile();
            Charset charset = FileTools.charset(file);
            try (final BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                    final BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    links.clear();
                    List<Link> hrefLinks = hrefLinksInLine(line);
                    if (hrefLinks != null) {
                        links.addAll(hrefLinks);
                    }
                    List<Link> openLinks = openLinksInLine(line);
                    if (openLinks != null) {
                        links.addAll(openLinks);
                    }
                    String newLine = line;
                    for (Link link : links) {
                        String linkAddress = link.getAddress();
                        String fullAddress = linkAddress;
                        if (!linkAddress.toLowerCase().startsWith("http")) {
                            if (linkAddress.startsWith("/")) {
                                if (linkRoot == null) {
                                    continue;
                                }
                                fullAddress = linkRoot + linkAddress;
                            } else {
                                if (linkPath == null) {
                                    continue;
                                }
                                fullAddress = linkPath + linkAddress;
                            }
                        }
                        DownloadHistory linkHis = tableDownloadHistory.query(conn, urlQuery, fullAddress);
                        if (linkHis == null || linkHis.getFilename() == null) {
                            continue;
                        }
                        File linkFile = new File(linkHis.getFilename());
                        if (!linkFile.exists()) {
                            continue;
                        }
                        if (linkFile.getParent().equals(file.getParent())) {
                            newLine = newLine.replace(link.getAddressOriginal(), "\"" + linkFile.getName() + "\"");
                        } else {
                            newLine = newLine.replace(link.getAddressOriginal(), "\"" + linkFile.getAbsolutePath() + "\"");
                        }
                    }
                    writer.write(newLine + "\n");
                }
            }
            return FileTools.rename(tmpFile, file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean relinkPage(File httpFile,
            Parser htmlParser, FlexmarkHtmlConverter mdConverter,
            Map<File, Link> completedLinks, Map<URL, File> completedAddresses) {
        try {
            if (httpFile == null || !httpFile.exists() || completedAddresses == null) {
                return false;
            }
            String linkRoot = null;
            String linkPath = null;
            try {
                Link link = completedLinks.get(httpFile);
                URL url = link.getUrl();
                linkRoot = url.getProtocol() + "://" + url.getHost();
                linkPath = path(url);
            } catch (Exception e) {
            }

            String html = FileTools.readTexts(httpFile);
            String md = mdConverter.convert(html);
            Node document = htmlParser.parse(md);
            List<Link> links = new ArrayList<>();
            MarkdownTools.links(document, links);
            String replaced = "", unchecked = html;
            int pos;
            for (Link link : links) {
                try {
                    String linkAddress = link.getAddress();
                    pos = unchecked.indexOf("\"" + linkAddress + "\"");
                    if (pos < 0) {
                        pos = unchecked.indexOf("\'" + linkAddress + "\'");
                    }
                    if (pos < 0) {
                        continue;
                    }
                    replaced += unchecked.substring(0, pos);
                    unchecked = unchecked.substring(pos + linkAddress.length() + 2);
                    String fullAddress = linkAddress;
                    if (!linkAddress.toLowerCase().startsWith("http")) {
                        if (linkAddress.startsWith("/")) {
                            if (linkRoot == null) {
                                continue;
                            }
                            fullAddress = linkRoot + linkAddress;
                        } else {
                            if (linkPath == null) {
                                continue;
                            }
                            fullAddress = linkPath + linkAddress;
                        }
                    }
                    URL url = new URL(fullAddress);
                    File linkFile = completedAddresses.get(url);
                    if (linkFile == null || !linkFile.exists()) {
                        replaced += "\"" + linkAddress + "\"";
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
            String frameset = " <FRAMESET border=0 cols=400,*>\n"
                    + "<FRAME frameBorder=no marginHeight=15 marginWidth=5  name=nav src=\"" + namePrefix + "_Nav.html\">\n";
            if (first.getParent().equals(targetFile.getParent())) {
                frameset += "<FRAME frameBorder=no marginHeight=15 marginWidth=10  name=main src=\"" + first.getName() + "\">\n";
            } else {
                frameset += "<FRAME frameBorder=no marginHeight=15 marginWidth=10  name=main src=\"" + first.toURI() + "\">\n";
            }
            File frameFile = new File(targetFile.getParent() + File.separator + namePrefix + ".html");
            FileTools.writeFile(frameFile, frameset);
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
        Charset fileCharset = FileTools.charset(htmlFile);
        if (!fileCharset.equals(Charset.forName("utf-8"))) {
            return false;
        }
        String html = FileTools.readTexts(htmlFile, fileCharset);
        String head = head(html);
        if (head == null) {
            return true;
        } else {
            Charset charset = charset(head);
            return charset == null || charset.equals(Charset.forName("utf-8"));
        }
    }

    public static String toUTF8(File htmlFile, boolean must) {
        return setCharset(htmlFile, Charset.forName("utf-8"), must);
    }

    public static String setCharset(File htmlFile, Charset charset, boolean must) {
        try {
            if (htmlFile == null || charset == null) {
                return "InvalidData";
            }
            Charset fileCharset = FileTools.charset(htmlFile);
            String html = FileTools.readTexts(htmlFile, fileCharset);
            String head = headWithoutTag(html);
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
                        + body(html) + "\n"
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
                head = headWithoutTag(html);
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
                    + body(html) + "\n"
                    + "</html>";
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
