package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.logger;
import net.sf.image4j.codec.ico.ICODecoder;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class HtmlTools {

    public enum HtmlStyle {
        Default, Console, Blackboard, Link
    }

    public static String Indent = "    ";
    public static final String BaseStyle
            = ".center { text-align:center;  max-width:95%; }\n"
            + "table { border-collapse:collapse;  margin: 8px;  max-width:95%;}\n"
            + "table, th, td { border: 1px solid; padding: 8px;}\n"
            + "th { font-weight:bold;  text-align:center;}\n"
            + "tr { height: 1.2em;  }\n"
            + ".boldText { font-weight:bold;  }\n";
    public static final String DefaultStyle
            = BaseStyle
            + ".valueText { color:#2e598a;  }\n";
    public static final String ConsoleStyle
            = "body { background-color:black; color:#CCFF99; }\n"
            + "table, th, td { border: #66FF66; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + ".valueText { color:skyblue;  }\n"
            + BaseStyle;
    public static final String BlackboardStyle
            = "body { background-color:#336633; color:white; }\n"
            + "table, th, td { border: #66FF66; }\n"
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

    public static String style(HtmlStyle style) {
        switch (style) {
            case Default:
                return DefaultStyle;
            case Console:
                return ConsoleStyle;
            case Blackboard:
                return BlackboardStyle;
            case Link:
                return LinkStyle;
        }
        return DefaultStyle;
    }

    public static File writeHtml(String html) {
        try {
            File htmFile = FileTools.getTempFile(".htm");
            FileTools.writeFile(htmFile, html);
            return htmFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void editHtml(String html) {
        try {
            File htmFile = writeHtml(html);
            FxmlStage.openHtmlEditor(null, htmFile);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void editHtml(String title, String body) {
        HtmlTools.editHtml(html(title, body));
    }

    public static void editHtml(String title, String style, String body) {
        HtmlTools.editHtml(html(title, style, body));
    }

    public static void viewHtml(String title, String body) {
        FxmlStage.openHtmlViewer(null, body);
    }

    public static String html(String title, String body) {
        return html(title, DefaultStyle, body);
    }

    public static String html(String title, String style, String body) {
        StringBuilder s = new StringBuilder();
        s.append("<HTML>\n").
                append(Indent).append("<HEAD>\n").
                append(Indent).append(Indent).append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
        if (title != null && !title.trim().isEmpty()) {
            s.append(Indent).append(Indent).append("<TITLE>").append(title).append("</TITLE>\n");
        }
        if (style != null && !style.trim().isEmpty()) {
            s.append(Indent).append(Indent).append("<style type=\"text/css\">\n");
            s.append(Indent).append(Indent).append(Indent).append(style).append("\n");
            s.append(Indent).append(Indent).append("</style>\n");
        }
        s.append(Indent).append("</HEAD>\n");
        s.append(Indent).append("<BODY>\n");
        s.append(body);
        s.append(Indent).append("</BODY>\n");
        s.append("</HTML>\n");
        return s.toString();
    }

    public static String html(String title, HtmlStyle style, String body) {
        return html(title, style(style), body);
    }

    public static String body(String html) {
        int start = html.indexOf("<BODY>");
        if (start <= 0) {
            start = html.indexOf("<body>");
            if (start <= 0) {
                start = html.indexOf("<Body>");
                if (start <= 0) {
                    start = 0;
                }
            }
        } else {
            start += "<BODY>".length();
        }
        int end = html.indexOf("</BODY>");
        if (end <= 0) {
            end = html.indexOf("</body>");
            if (end <= 0) {
                end = html.indexOf("</Body>");
                if (end <= 0) {
                    end = html.length();
                }
            }

        }
        return html.substring(start, end);
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
//                    logger.debug(e.toString());
                }
                if (image == null) {
                    try {
                        image = ImageIO.read(in);
                    } catch (Exception e) {
//                        logger.debug(e.toString());
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
            logger.debug(e.toString());
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
            logger.debug(e.toString());
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
            logger.debug(e.toString());
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
            logger.debug(e.toString());
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
            logger.debug(e.toString());
            return null;
        }
    }

}
