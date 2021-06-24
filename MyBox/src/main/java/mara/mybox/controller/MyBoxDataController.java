package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-22
 * @License Apache License Version 2.0
 */
public class MyBoxDataController extends BaseDataTableController<BaseTable> {

    @FXML
    protected ListView<String> tablesList;
    @FXML
    protected Tab dataTab, colorsTab;
    @FXML
    protected FlowPane buttonsPane, colorsPane;

    public MyBoxDataController() {
        baseTitle = AppVariables.message("MyBoxData");
        TipsLabelKey = "ColorsManageTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tablesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            tablesList.setCellFactory(p -> new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    setText(AppVariables.tableMessage(item.toLowerCase()));
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initButtons() {
        try {
            exportButton.disableProperty().bind(Bindings.isEmpty(tableData));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        hideRightPane();
        refreshTables();
    }

    /*
        tables list
     */
    @FXML
    protected void refreshTables() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<String> tables;

                @Override
                protected boolean handle() {
                    tables = new ArrayList<>();
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<String> allTables = DerbyBase.tables(conn);
                        for (String table : allTables) {
                            if (!table.startsWith("User_Data_".toUpperCase())) {
                                tables.add(table);
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tablesList.getItems().setAll(tables);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }


    /*
       Data
     */
    protected void refreshPalette() {

        loadTableData();
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {

    }

    @FXML
    protected void popExportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {

    }

    @Override
    protected int clearData() {
        return 1;
    }

    @Override
    public void clearView() {
        super.clearView();

    }

    /*
       Data
     */
    @Override
    public int readDataSize() {
        return 1;
    }

    @Override
    public List<BaseTable> readPageData() {
        return null;
    }

    @Override
    protected int deleteData(List<BaseTable> data) {
        return 0;
    }

    @FXML
    @Override
    public void refreshAction() {
        refreshPalette();
    }

    @Override
    protected void checkSelected() {
        super.checkSelected();

    }

    @FXML
    @Override
    public void copyAction() {

    }

}
