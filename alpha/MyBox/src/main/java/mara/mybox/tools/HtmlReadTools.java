package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javafx.concurrent.Task;
import javafx.scene.control.IndexRange;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.HtmlTableController;
import mara.mybox.data.DownloadTask;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
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

    public final static String httpUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0";


    /*
        read html
     */
    public static org.jsoup.nodes.Document url2doc(String urlAddress) {
        try {
            if (urlAddress == null) {
                return null;
            }
            URL url;
            try {
                url = new URL(urlAddress);
            } catch (Exception e) {
                return null;
            }
            String protocal = url.getProtocol();
            if ("file".equalsIgnoreCase(protocal)) {
                return file2doc(new File(url.getFile()));

            } else if ("http".equalsIgnoreCase(protocal) || "https".equalsIgnoreCase(protocal)) {
                return Jsoup.connect(url.toString()).get();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString() + " " + urlAddress);
        }
        return null;
    }

    public static org.jsoup.nodes.Document file2doc(File file) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            return Jsoup.parse(TextFileTools.readTexts(file));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString() + " " + file);
            return null;
        }
    }

    public static String url2html(String urlAddress) {
        try {
            org.jsoup.nodes.Document doc = url2doc(urlAddress);
            if (doc != null) {
                return doc.html();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString() + " " + urlAddress);
        }
        return null;
    }

    public static File url2file(String urlAddress) {
        try {
            org.jsoup.nodes.Document doc = url2doc(urlAddress);
            if (doc == null) {
                return null;
            }
            String html = doc.html();
            File tmpFile = TmpFileTools.getTempFile();
            TextFileTools.writeFile(tmpFile, html, doc.charset());
            if (tmpFile == null || !tmpFile.exists()) {
                return null;
            }
            if (tmpFile.length() == 0) {
                FileDeleteTools.delete(tmpFile);
                return null;
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString() + " " + urlAddress);
            return null;
        }
    }

    public static String baseURI(String urlAddress) {
        try {
            org.jsoup.nodes.Document doc = url2doc(urlAddress);
            if (doc == null) {
                return null;
            }
            return doc.baseUri();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString() + " " + urlAddress);
            return null;
        }
    }

    public static File download(String urlAddress) {
        try {
            if (urlAddress == null) {
                return null;
            }
            URL url;
            try {
                url = new URL(urlAddress);
            } catch (Exception e) {
                return null;
            }
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
                connection.setRequestProperty("User-Agent", httpUserAgent);
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
                connection.setRequestProperty("User-Agent", httpUserAgent);
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

    public static File url2image(String address, String name) {
        try {
            if (address == null) {
                return null;
            }
            String suffix = null;
            if (name != null && !name.isBlank()) {
                suffix = FileNameTools.suffix(name);
            }
            String addrSuffix = FileNameTools.suffix(address);
            if (addrSuffix != null && !addrSuffix.isBlank()) {
                if (suffix == null || suffix.isBlank()
                        || !addrSuffix.equalsIgnoreCase(suffix)) {
                    suffix = addrSuffix;
                }
            }
            if (suffix == null || (suffix.length() != 3
                    && !"jpeg".equalsIgnoreCase(suffix) && !"tiff".equalsIgnoreCase(suffix))) {
                suffix = "jpg";
            }
            File tmpFile = download(address);
            if (tmpFile == null) {
                return null;
            }
            File imageFile = new File(tmpFile.getAbsoluteFile() + "." + suffix);
            if (FileTools.rename(tmpFile, imageFile)) {
                return imageFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e, address);
            return null;
        }
    }

    public static void requestHead(BaseController controller, String link) {
        if (controller == null || link == null) {
            return;
        }
        Task infoTask = new DownloadTask() {

            @Override
            protected boolean initValues() {
                readHead = true;
                address = link;
                return super.initValues();
            }

            @Override
            protected void whenSucceeded() {
                if (head == null) {
                    controller.popError(Languages.message("InvalidData"));
                    return;
                }
                String table = requestHeadTable(url, head);
                HtmlTableController.open(table);
            }

            @Override
            protected void whenFailed() {
                if (error != null) {
                    controller.popError(error);
                } else {
                    controller.popFailed();
                }
            }

        };
        controller.start(infoTask);
    }

    public static HttpURLConnection getConnection(URL url) {
        try {
            if ("https".equals(url.getProtocol())) {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                SSLContext sc = SSLContext.getInstance(AppValues.HttpsProtocal);
                sc.init(null, null, null);
                conn.setSSLSocketFactory(sc.getSocketFactory());
                return conn;
            } else {
                return (HttpURLConnection) url.openConnection();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static Map<String, String> requestHead(URL url) {
        try {
            if (!url.getProtocol().startsWith("http")) {
                return null;
            }
            HttpURLConnection connection = getConnection(url);
            Map<String, String> head = HtmlReadTools.requestHead(connection);
            connection.disconnect();
            return head;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static Map<String, String> requestHead(HttpURLConnection connection) {
        try {
            Map<String, String> head = new HashMap();
            connection.setRequestMethod("HEAD");
            head.put("ResponseCode", connection.getResponseCode() + "");
            head.put("ResponseMessage", connection.getResponseMessage());
            head.put("RequestMethod", connection.getRequestMethod());
            head.put("ContentEncoding", connection.getContentEncoding());
            head.put("ContentType", connection.getContentType());
            head.put("ContentLength", connection.getContentLength() + "");
            head.put("Expiration", DateTools.datetimeToString(connection.getExpiration()));
            head.put("LastModified", DateTools.datetimeToString(connection.getLastModified()));
            for (String key : connection.getHeaderFields().keySet()) {
                head.put("HeaderField_" + key, connection.getHeaderFields().get(key).toString());
            }
            for (String key : connection.getRequestProperties().keySet()) {
                head.put("RequestProperty_" + key, connection.getRequestProperties().get(key).toString());
            }
            return head;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static String requestHeadTable(URL url) {
        return requestHeadTable(url, HtmlReadTools.requestHead(url));
    }

    public static String requestHeadTable(URL url, Map<String, String> head) {
        try {
            if (head == null) {
                return null;
            }
            StringBuilder s = new StringBuilder();
            s.append("<h1  class=\"center\">").append(url.toString()).append("</h1>\n");
            s.append("<hr>\n");
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("Name"), Languages.message("Value")));
            StringTable table = new StringTable(names);
            for (String name : head.keySet()) {
                if (name.startsWith("HeaderField_") || name.startsWith("RequestProperty_")) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(name, head.get(name)));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
            s.append("<h2  class=\"center\">").append("Header Fields").append("</h2>\n");
            int hlen = "HeaderField_".length();
            for (Object key : head.keySet()) {
                String name = (String) key;
                if (!name.startsWith("HeaderField_")) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(name.substring(hlen), (String) head.get(key)));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
            s.append("<h2  class=\"center\">").append("Request Property").append("</h2>\n");
            int rlen = "RequestProperty_".length();
            for (String name : head.keySet()) {
                if (!name.startsWith("RequestProperty_")) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(name.substring(rlen), head.get(name)));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }

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
        return tag(tag(html, "head", true), "title", false);
    }

    public static String head(String html) {
        return tag(html, "head", true);
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

    public static String charsetName(String html) {
        return charsetNameInHead(head(html));
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
        if (finder.setFindString("</body>").setAnchor(from).run()) {
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
        try {
            if (string == null || tag == null) {
                return null;
            }
            IndexRange range = tagRange(FindReplaceString.finder(false, true), string, tag, withTag);
            if (range == null) {
                return null;
            }
            return string.substring(range.getStart(), range.getEnd());
        } catch (Exception e) {
            return null;
        }
    }

    public static IndexRange tagRange(FindReplaceString finder, String string, String tag, boolean withTag) {
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
        return new IndexRange(from, to);
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
                Link link = Link.create().setUrl(url).setAddress(url.toString()).setAddressOriginal(linkAddress)
                        .setName(element.text()).setTitle(element.attr("title")).setIndex(links.size());
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

    public static List<StringTable> Tables(String html, String name) {
        try {
            if (html == null || html.isBlank()) {
                return null;
            }
            List<StringTable> tables = new ArrayList<>();
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Elements tablesList = doc.getElementsByTag("table");
            String titlePrefix = name != null ? name + "_t_" : "t_";
            int count = 0;
            if (tablesList != null) {
                for (org.jsoup.nodes.Element table : tablesList) {
                    StringTable stringTable = new StringTable();
                    stringTable.setTitle(titlePrefix + (++count));
                    List<List<String>> data = new ArrayList<>();
                    List<String> names = null;
                    Elements trList = table.getElementsByTag("tr");
                    if (trList != null) {
                        for (org.jsoup.nodes.Element tr : trList) {
                            if (names == null) {
                                Elements thList = tr.getElementsByTag("th");
                                if (thList != null) {
                                    names = new ArrayList<>();
                                    for (org.jsoup.nodes.Element th : thList) {
                                        names.add(th.text());
                                    }
                                    if (!names.isEmpty()) {
                                        stringTable.setNames(names);
                                    }
                                }
                            }
                            Elements tdList = tr.getElementsByTag("td");
                            if (tdList != null) {
                                List<String> row = new ArrayList<>();
                                for (org.jsoup.nodes.Element td : tdList) {
                                    row.add(td.text());
                                }
                                if (!row.isEmpty()) {
                                    data.add(row);
                                }
                            }
                        }
                    }
                    stringTable.setData(data);
                    tables.add(stringTable);
                }
            }
            return tables;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String removeNode(String html, String id) {
        try {
            if (html == null || id == null || id.isBlank()) {
                return html;
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            org.jsoup.nodes.Element element = doc.getElementById(id);
            if (element != null) {
                element.remove();
                return doc.html();
            } else {
                return html;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return html;
        }
    }

}
