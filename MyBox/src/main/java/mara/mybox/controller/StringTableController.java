package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class StringTableController extends BaseController {

    protected String html;

    protected String title, style;
    protected List<String> fields;
    protected StringTable table;

    @FXML
    protected WebView webView;
    @FXML
    protected CheckBox consoleCheck;

    public StringTableController() {
        baseTitle = AppVariables.message("StringTable");
        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = "HtmlFilePath";
        targetPathKey = "HtmlFilePath";

        sourceExtensionFilter = CommonImageValues.HtmlExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            style = HtmlTools.defaultStyle;
            consoleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVariables.setUserConfigValue("InformationConsoleKey", consoleCheck.isSelected()
                    );
                    if (consoleCheck.isSelected()) {
                        style = HtmlTools.consoleStyle;
                    } else {
                        style = HtmlTools.defaultStyle;
                    }
                    loadInformation();
                }
            });
            consoleCheck.setSelected(AppVariables.getUserConfigBoolean("InformationConsoleKey", false));

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
                    }
                });
            }
        }, 1000);
    }

    public void loadInformation() {
        try {
            html = HtmlTools.html(title, style, StringTable.tableDiv(table));
            webView.getEngine().loadContent​(html);
        } catch (Exception e) {
            logger.error(e.toString());
        }
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

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (html == null) {
                return;
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    baseTitle, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            try (BufferedWriter out = new BufferedWriter(new FileWriter(file, Charset.forName("utf-8"), false))) {
                out.write(html);
                out.flush();
            }

            popSuccessul();

        } catch (Exception e) {
            logger.error(e.toString());
            popError(e.toString());
        }

    }

}
