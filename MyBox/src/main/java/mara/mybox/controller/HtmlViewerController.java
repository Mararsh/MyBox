package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlViewerController extends BaseController {

    protected String body;
    protected List<String> fields;
    protected StringTable table;
    protected String html;
    protected String title;

    @FXML
    protected WebView webView;

    public HtmlViewerController() {
        baseTitle = AppVariables.message("Html");
    }

    public String styleString() {
        String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
        return HtmlTools.styleValue(HtmlTools.styleName(htmlStyle));
    }

    public String styleTag() {
        return "\n<style type=\"text/css\">/>\n" + styleString() + "</style>\n";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @FXML
    protected void editHtml() {
        String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
        if (table != null) {
            html = HtmlTools.html(title, htmlStyle, StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlTools.html(title, htmlStyle, body);
        }
        HtmlTools.editHtml(html);
    }

    public void displayHtml() {
        String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
        if (table != null) {
            html = HtmlTools.html(title, htmlStyle, StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlTools.html(title, htmlStyle, body);

        } else if (html != null) {
            this.body = HtmlTools.body(html);
            html = HtmlTools.html(title, htmlStyle, body);

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
            String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            html = HtmlTools.html(title, htmlStyle, body);
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
        String htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
        html = HtmlTools.html(title, htmlStyle, StringTable.tableDiv(table));
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
        popMenu = FxmlControl.popHtmlStyle(mouseEvent, this, popMenu, webView.getEngine());
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
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        save(file, html, false);

    }

    @FXML
    public void editAction(ActionEvent event) {
        File file = FileTools.getTempFile(".html");
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
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = FileTools.writeFile(file, txt) != null;
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isEdit) {
                        FxmlStage.openHtmlEditor(null, file);
                    } else {
                        popSuccessful();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
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
