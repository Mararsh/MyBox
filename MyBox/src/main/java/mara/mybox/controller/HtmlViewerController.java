package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.HtmlTools.HtmlStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

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
    protected HtmlStyle htmlStyle;

    @FXML
    protected WebView webView;
    @FXML
    protected ComboBox<String> htmlStyleSelector;

    public HtmlViewerController() {
        baseTitle = AppVariables.message("Html");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            htmlStyle = HtmlStyle.Default;
            htmlStyleSelector.getItems().addAll(Arrays.asList(
                    message("Default"), message("Console"), message("Blackboard")
            ));
            htmlStyleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (message("Console").equals(newValue)) {
                            htmlStyle = HtmlStyle.Console;
                        } else if (message("Blackboard").equals(newValue)) {
                            htmlStyle = HtmlStyle.Blackboard;
                        } else {
                            htmlStyle = HtmlStyle.Default;
                        }
                        AppVariables.setUserConfigValue("HtmlStyle", newValue);
                        displayHtml();
                    });
            htmlStyleSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("HtmlStyle", message("Default")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void toFront() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        myStage.toFront();
                        webView.requestFocus();
                        timer = null;
                    }
                });
            }
        }, 1000);
    }

    @FXML
    protected void editHtml() {
        if (table != null) {
            html = HtmlTools.html(title, htmlStyle, StringTable.tableDiv(table));

        } else if (body != null) {
            html = HtmlTools.html(title, htmlStyle, body);
        }
        HtmlTools.editHtml(html);
    }

    public void displayHtml() {
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
            html = HtmlTools.html(title, htmlStyle, body);
            displayHtml(html);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadTable(StringTable table) {
        this.table = table;
        if (table == null) {
            return;
        }
        this.title = table.getTitle();
        this.fields = table.getNames();
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
                name, targetExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        save(file, html, false);

    }

    @FXML
    public void editAction() {
        File file = FileTools.getTempFile(".html");
        save(file, html, true);
    }

    public void save(File file, String txt, boolean isEdit) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
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
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
