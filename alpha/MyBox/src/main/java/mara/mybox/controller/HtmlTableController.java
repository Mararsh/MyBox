package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
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

    @Override
    public String html() {
        if (table != null) {
            html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), body);
        }
        return html;
    }

    @FXML
    protected void editHtml() {
        HtmlWriteTools.editHtml(html());
    }

    public void displayHtml() {
        displayHtml(html());
    }

    public void displayHtml(String html) {
        this.html = html;
        loadContents(html);
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
            html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), body);
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
        html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), StringTable.tableDiv(table));
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
        displayHtml(null);
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
            name += ".html";
        }
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        save(file, html, false);
    }

    @FXML
    @Override
    public void editAction() {
        File file = FileTmpTools.getTempFile(".html");
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
                    recordFileWritten(file);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isEdit) {
                        HtmlEditorController.openFile(file);
                    } else {
                        WebBrowserController.openFile(file);
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

    @Override
    public void pageLoaded() {
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

    /*
        static
     */
    public static HtmlTableController open() {
        try {
            HtmlTableController controller = (HtmlTableController) WindowTools.openStage(Fxmls.HtmlTableFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlTableController open(String body) {
        return open(null, body);
    }

    public static HtmlTableController open(String title, String body) {
        try {
            HtmlTableController controller = open();
            if (controller != null) {
                controller.initTable(title);
                controller.loadBody(body);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
