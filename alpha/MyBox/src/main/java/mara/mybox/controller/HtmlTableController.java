package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class HtmlTableController extends BaseWebViewController {

    protected String body;
    protected List<String> fields;
    protected StringTable table;
    protected String html;
    protected String title;

    public HtmlTableController() {
        baseTitle = Languages.message("Html");
    }

    public String styleString() {
        String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
        return HtmlStyles.styleValue(HtmlStyles.styleName(htmlStyle));
    }

    public String styleTag() {
        return "\n<style type=\"text/css\">/>\n" + styleString() + "</style>\n";
    }

    @FXML
    protected void editHtml() {
        String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
        if (table != null) {
            html = HtmlWriteTools.html(title, htmlStyle, StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlWriteTools.html(title, htmlStyle, body);
        }
        HtmlWriteTools.editHtml(html);
    }

    public void displayHtml() {
        String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
        if (table != null) {
            html = HtmlWriteTools.html(title, htmlStyle, StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlWriteTools.html(title, htmlStyle, body);

        } else if (html != null) {
            this.body = HtmlReadTools.body(html);
            html = HtmlWriteTools.html(title, htmlStyle, body);

        }
        displayHtml(html);
    }

    public void displayHtml(String html) {
        this.html = html;
        if (html == null) {
            webView.getEngine().loadContent​("");
        } else {
            webView.getEngine().loadContent​(html);
        }
        if (myStage != null && title != null) {
            myStage.setTitle(title);
        }
    }

    public void loadHtml(String html) {
        displayHtml(html);
    }

    public void loadBody(String body) {
        try {
            this.body = body;
            if (body == null) {
                return;
            }
            String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
            html = HtmlWriteTools.html(title, htmlStyle, body);
            displayHtml(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadTable(StringTable table) {
        this.table = table;
        if (table == null) {
            return;
        }
        this.title = table.getTitle();
        this.fields = table.getNames();
        String htmlStyle = UserConfig.getString(baseName + "HtmlStyle", "Default");
        html = HtmlWriteTools.html(title, htmlStyle, StringTable.tableDiv(table));
        displayHtml(html);
    }

    public void initTable(String title, List<String> fields) {
        this.title = title;
        this.fields = fields;
        table = new StringTable(fields, title);
    }

    public void initTable(String title) {
        this.title = title;
        this.fields = null;
        table = new StringTable(null, title);
    }

    public void addData(String name, String value) {
        if (table == null) {
            return;
        }
        table.add(Arrays.asList(name, value));
    }

    public void addData(List<String> data) {
        if (table == null) {
            return;
        }
        table.add(data);
    }

    public void clear() {
        html = null;
        webView.getEngine().loadContent("");
    }

    @FXML
    public void popLinksStyle(MouseEvent mouseEvent) {
        popMenu = PopTools.popHtmlStyle(mouseEvent, this, popMenu, webView.getEngine());
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (html == null) {
            return;
        }
        String name = title;
        if (name != null) {
            name += ".htm";
        }
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        save(file, html, false);

    }

    @FXML
    public void editAction() {
        File file = TmpFileTools.getTempFile(".html");
        save(file, html, true);
    }

    public void save(File file, String txt, boolean isEdit) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    ok = TextFileTools.writeFile(file, txt) != null;
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isEdit) {
                        ControllerTools.openHtmlEditor(null, file);
                    } else {
                        popSuccessful();
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    public void dataAction() {
        webViewController.tables(html, title);
    }


    /*
        get/set
     */
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
