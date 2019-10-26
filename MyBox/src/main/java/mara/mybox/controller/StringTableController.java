package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.StringTable;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class StringTableController extends HtmlViewerController {

    protected String style;
    protected List<String> fields;
    protected StringTable table;

    @FXML
    protected CheckBox consoleCheck;

    public StringTableController() {
        baseTitle = AppVariables.message("StringTable");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            style = HtmlTools.DefaultStyle;
            consoleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    AppVariables.setUserConfigValue("InformationConsoleKey", consoleCheck.isSelected()
                    );
                    if (consoleCheck.isSelected()) {
                        style = HtmlTools.ConsoleStyle;
                    } else {
                        style = HtmlTools.DefaultStyle;
                    }
                    loadInformation();
                }
            });
            consoleCheck.setSelected(AppVariables.getUserConfigBoolean("InformationConsoleKey", false));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void editHtml() {
        if (table == null) {
            return;
        }
        table.display();
        if (table.getTitle() != null) {
            myStage.setTitle(table.getTitle());
        }
    }

    public void loadInformation() {
        try {
            if (table == null) {
                return;
            }
            html = HtmlTools.html(title, style, StringTable.tableDiv(table));
            webView.getEngine().loadContent​(html);
            if (myStage != null && table.getTitle() != null) {
                myStage.setTitle(table.getTitle());
            }
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
        loadInformation();
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

}
