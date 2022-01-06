package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.TestCase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2022-1-6
 * @License Apache License Version 2.0
 */
public class AutoTestingCasesController extends BaseTableViewController<TestCase> {
    
    @FXML
    protected TableColumn<TestCase, Integer> aidColumn;
    @FXML
    protected TableColumn<TestCase, String> typeColumn, operationColumn, objectColumn, versionColumn, stageColumn;
    
    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected TextArea textArea;
    
    public AutoTestingCasesController() {
        baseTitle = Languages.message("TestCases");
    }
    
    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            aidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            operationColumn.setCellValueFactory(new PropertyValueFactory<>("operationName"));
            objectColumn.setCellValueFactory(new PropertyValueFactory<>("object"));
            versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
            stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData.setAll(TestCase.testCases());
            
            startButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }
    
    @FXML
    @Override
    public void startAction() {
        try {
            List<TestCase> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null) {
                return;
            }
            AutoTestingExecutionController.open(this, selected);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }
    
}
