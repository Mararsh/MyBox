package mara.mybox.tools;

import java.io.File;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-8-19
 * @License Apache License Version 2.0
 */
public class HtmlTools {

    public static String indent = "    ";
    public static final String defaultStyle
            = "h1, h2 { text-align:center; }\n"
            + "table { border-collapse:collapse; width:90%; }\n"
            + "table, th, td { border: 1px solid; }\n"
            //            + "tr { height: 1.2em;;  }\n"
            + "td { padding: 5px;  }\n";
    public static final String consoleStyle
            = "body { background-color:black; color:#66FF66; }\n"
            + "table, th, td { border: #66FF66; }\n"
            + defaultStyle;
    public static final String linkStyle
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
            HtmlEditorController controller = FxmlStage.openHtmlEditor(null, htmFile);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void displayHtml(String title, String body) {
        displayHtml(html(title, body));

    }

    public static String html(String title, String body) {
        return html(title, defaultStyle, body);
    }

    public static String html(String title, String style, String body) {
        StringBuilder s = new StringBuilder();
        s.append("<HTML>\n").
                append(indent).append("<HEAD>\n").
                append(indent).append(indent).append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
        if (title != null && !title.trim().isEmpty()) {
            s.append(indent).append(indent).append("<TITLE>").append(title).append("</TITLE>\n");
        }
        if (style != null && !style.trim().isEmpty()) {
            s.append(indent).append(indent).append("<style type=\"text/css\">\n");
            s.append(indent).append(indent).append(indent).append(style).append("\n");
            s.append(indent).append(indent).append("</style>\n");
        }
        s.append(indent).append("</HEAD>\n");
        s.append(indent).append("<BODY>\n");
        s.append(body);
        s.append(indent).append("</BODY>\n");
        s.append("</HTML>\n");
        return s.toString();
    }

}
