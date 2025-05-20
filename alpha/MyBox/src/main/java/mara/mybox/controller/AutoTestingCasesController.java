package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.TestCase;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-6
 * @License Apache License Version 2.0
 */
public class AutoTestingCasesController extends BaseTableViewController<TestCase> {

    @FXML
    protected TableColumn<TestCase, String> typeColumn, operationColumn,
            objectColumn, versionColumn, stageColumn;

    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected TextArea textArea;

    public AutoTestingCasesController() {
        baseTitle = message("TestCases");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            operationColumn.setCellValueFactory(new PropertyValueFactory<>("operationName"));
            objectColumn.setCellValueFactory(new PropertyValueFactory<>("object"));
            versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
            stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData.setAll(TestCase.testCases());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void startAction() {
        try {
            List<TestCase> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                selected = new ArrayList<>();
                selected.addAll(tableData);
            }
            AutoTestingExecutionController.open(this, selected);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
