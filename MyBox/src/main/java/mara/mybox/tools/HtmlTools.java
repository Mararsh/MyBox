package mara.mybox.tools;

import java.io.File;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class HtmlTools {

    public static String Indent = "    ";
    public static final String DefaultStyle
            = ".center { text-align:center; }\n"
            + "table { border-collapse:collapse; width:85%; }\n"
            + "table, th, td { border: 1px solid; }\n"
            //            + "tr { height: 1.2em;;  }\n"
            + "td { padding: 5px;  }\n";
    public static final String ConsoleStyle
            = "body { background-color:black; color:#66FF66; }\n"
            + "table, th, td { border: #66FF66; }\n"
            + "a:link {color: #FFFFFF}\n"
            + "a:visited  {color: #DDDDDD}\n"
            + DefaultStyle;
    public static final String LinkStyle
            = "body { background-color:transparent;  }\n"
            + "table { border-collapse:collapse; width:90%; }\n"
            + "table, th, td { border: 0px solid; }\n"
            + "td { padding:20px;  }\n";

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

    public static void displayHtml(String html) {
        try {
            File htmFile = writeHtml(html);
            FxmlStage.openHtmlEditor(null, htmFile);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void displayHtml(String title, String body) {
        displayHtml(html(title, body));

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

}
