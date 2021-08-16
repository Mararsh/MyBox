package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javafx.scene.control.IndexRange;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.controller.HtmlTableController;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class HtmlReadTools {

    /*
     read html
     */
    public static File url2File(String urlAddress) {
        try {
            if (urlAddress == null) {
                return null;
            }
            URL url = new URL(urlAddress);
            File tmpFile = TmpFileTools.getTempFile();
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                FileCopyTools.copyFile(new File(url.getFile()), tmpFile);
            } else if ("https".equalsIgnoreCase(url.getProtocol())) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                SSLContext sc = SSLContext.getInstance(AppValues.HttpsProtocal);
                sc.init(null, null, null);
                connection.setSSLSocketFactory(sc.getSocketFactory());
                connection.setConnectTimeout(UserConfig.getInt("WebConnectTimeout", 10000));
                connection.setReadTimeout(UserConfig.getInt("WebReadTimeout", 10000));
                connection.setRequestProperty("User-Agent", AppVariables.HttpUserAgent);
                connection.connect();
                if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                    try (final BufferedInputStream inStream = new BufferedInputStream(new GZIPInputStream(connection.getInputStream()));
                            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[AppValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                } else {
                    try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[AppValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                }
            } else if ("http".equalsIgnoreCase(url.getProtocol())) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", AppVariables.HttpUserAgent);
                connection.setConnectTimeout(UserConfig.getInt("WebConnectTimeout", 10000));
                connection.setReadTimeout(UserConfig.getInt("WebReadTimeout", 10000));
                connection.setUseCaches(false);
                connection.connect();
                if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                    try (final BufferedInputStream inStream = new BufferedInputStream(new GZIPInputStream(connection.getInputStream()));
                            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[AppValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, len);
                        }
                    }
                } else {
                    try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        byte[] buf = new byte[AppValues.IOBufferLength];
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
                FileDeleteTools.delete(tmpFile);
                return null;
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString() + " " + urlAddress);
            return null;
        }
    }

    public static String url2text(String urlAddress) {
        File tmpFile = url2File(urlAddress);
        if (tmpFile == null) {
            return null;
        }
        return TextFileTools.readTexts(tmpFile);
    }

    public static String readURL(String address) {
        try {
            if (address == null) {
                return null;
            }
            URL url = new URL(address);
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
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
            String html = TextFileTools.readTexts(file);
            ;
            if (html == null) {
                return null;
            }
            return Jsoup.parse(html);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static HtmlTableController htmlTable(String title, String body) {
        return ControllerTools.openHtmlTable(null, body);
    }

    /*
        parse html
     */
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

    public static String htmlTitle(String html) {
        FindReplaceString finder = FindReplaceString.finder(true, true);
        return tag(finder, tag(finder, html, "head", true), "title", false);
    }

    public static String charsetNameInHead(String head) {
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
        } else if (s.startsWith("'")) {
            s = s.substring(1).trim();
            pos = s.indexOf("'");
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
                pos = s.indexOf("'");
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

    public static Charset charsetInHead(String head) {
        try {
            if (head == null) {
                return null;
            }
            String name = charsetNameInHead(head);
            if (name == null) {
                return null;
            }
            return Charset.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static Charset htmlCharset(File htmlFile) {
        try {
            if (htmlFile == null) {
                return null;
            }
            Charset fileCharset = TextFileTools.charset(htmlFile);
            String html = TextFileTools.readTexts(htmlFile, fileCharset);
            String head = tag(html, "head", true);
            if (head == null) {
                return fileCharset;
            } else {
                Charset charset = charsetInHead(head);
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
            Charset charset = charsetInHead(tag(html, "head", true));
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

    public static boolean isUTF8(File htmlFile) {
        Charset charset = htmlCharset(htmlFile);
        return charset == null || charset.equals(Charset.forName("utf-8"));
    }

    public static String body(String html) {
        return body(html, true);
    }

    public static String body(String html, boolean withTag) {
        if (html == null || html.isBlank()) {
            return null;
        }
        int from = 0;
        int to = html.length();
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
            Elements children = element.children();
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

    public static List<Link> links(URL baseURL, String html) {
        if (html == null) {
            return null;
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("a");
        List<Link> links = new ArrayList<>();
        for (org.jsoup.nodes.Element element : elements) {
            String linkAddress = element.attr("href");
            try {
                URL url = new URL(baseURL, linkAddress);
                Link link = Link.create().setUrl(url).setAddress(url.toString()).setAddressOriginal(linkAddress).setName(element.text()).setTitle(element.attr("title")).setIndex(links.size());
                links.add(link);
            } catch (Exception e) {
                //                MyBoxLog.console(linkAddress);
            }
        }
        return links;
    }

    public static List<Link> links(Link addressLink, File path, Link.FilenameType nameType) {
        try {
            if (addressLink == null || path == null) {
                return null;
            }
            List<Link> validLinks = new ArrayList<>();
            URL url = addressLink.getUrl();
            Link coverLink = Link.create().setUrl(url).setAddress(url.toString()).setName("0000_" + path.getName()).setTitle(path.getName());
            coverLink.setIndex(0).setFile(new File(coverLink.filename(path, nameType)).getAbsolutePath());
            validLinks.add(coverLink);
            String html = addressLink.getHtml();
            if (html == null) {
                html = TextFileTools.readTexts(new File(addressLink.getFile()));
            }
            List<Link> links = HtmlReadTools.links(url, html);
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

}
