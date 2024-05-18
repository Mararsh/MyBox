package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-16
 * @License Apache License Version 2.0
 */
public class BaseData2DListController extends BaseSysTableController<Data2DDefinition> {

    protected TableData2DDefinition tableData2DDefinition;

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2didColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> rowsColumn, colsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn, typeColumn, fileColumn, sheetColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> modifyColumn;
    @FXML
    protected Button openButton, renameItemButton;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected ControlData2DView viewController;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            d2didColumn.setCellValueFactory(new PropertyValueFactory<>("d2did"));
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            }
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));
            colsColumn.setCellValueFactory(new PropertyValueFactory<>("colsNumber"));
            colsColumn.setCellFactory(new TableNumberCell());
            if (rowsColumn != null) {
                rowsColumn.setCellValueFactory(new PropertyValueFactory<>("rowsNumber"));
                rowsColumn.setCellFactory(new TableNumberCell());
            }
            if (fileColumn != null) {
                fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            }
            if (sheetColumn != null) {
                sheetColumn.setCellValueFactory(new PropertyValueFactory<>("sheet"));
            }
            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rightPaneControl = viewController.rightPaneControl;
            initRightPaneControl();

            tableData2DDefinition = new TableData2DDefinition();
            tableDefinition = tableData2DDefinition;
            tableName = tableDefinition.getTableName();
            idColumnName = tableDefinition.getIdColumnName();

            setConditions();

            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setConditions() {
        try {
            queryConditions = " data_type  != " + Data2D.type(Data2DDefinition.DataType.InternalTable);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadList() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    List<String> tables = DerbyBase.allTables(conn);
                    DataTable dataTable = new DataTable();
                    for (String referredName : tables) {
                        if (DataInternalTable.InternalTables.contains(referredName.toUpperCase())) {
                            continue;
                        }
                        if (tableData2DDefinition.queryTable(conn, referredName, Data2DDefinition.DataType.DatabaseTable) == null) {
                            dataTable.readDefinitionFromDB(conn, referredName);
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                loadTableData();
            }

        };
        start(task);
    }

    public void loadDef(Data2DDefinition def) {
        viewController.loadDef(def);
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        try (Connection conn = DerbyBase.getConnection();) {
            tableData2DDefinition.clearInvalid(null, conn, false);
        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public void itemClicked() {
        viewAction();
    }

    @FXML
    @Override
    public void viewAction() {
        Data2DDefinition selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        viewController.loadDef(selected);
    }

    @FXML
    @Override
    public void editAction() {
        Data2DDefinition selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        Data2DManufactureController.openDef(selected);
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Load"), StyleTools.getIconImageView("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                viewAction();
            });
            items.add(menu);

            if (buttonsPane.getChildren().contains(renameItemButton)) {
                menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    renameAction();
                });
                menu.setDisable(renameItemButton.isDisable());
                items.add(menu);

            }

            if (buttonsPane.getChildren().contains(deleteItemsButton)) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                menu.setDisable(deleteItemsButton.isDisable());
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            items.addAll(super.makeTableContextMenu());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        if (renameItemButton != null) {
            renameItemButton.setDisable(isNoneSelected());
        }
    }

    @Override
    protected int deleteData(FxTask currentTask, List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        boolean changed = false;
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            for (Data2DDefinition item : data) {
                if (currentTask == null || !currentTask.isWorking()) {
                    break;
                }
                if (item.isUserTable() && item.getSheet() != null) {
                    String referName = DerbyBase.fixedIdentifier(item.getSheet());
                    try {
                        statement.executeUpdate("DROP TABLE " + referName);
                        changed = true;
                    } catch (Exception e) {
                        MyBoxLog.debug(e, referName);
                    }
                    if (viewController.data2D != null
                            && item.getD2did() == viewController.data2D.getD2did()) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                viewController.loadNull();
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        if (changed) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    refreshAction();
                }
            });
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected long clearData(FxTask currentTask) {
        String sql = "SELECT d2did, sheet FROM " + tableData2DDefinition.getTableName()
                + " WHERE  data_type = " + Data2D.type(Data2DDefinition.DataType.DatabaseTable)
                + " AND ( " + queryConditions + " )";
        boolean isCurrent = false;
        try (Connection conn = DerbyBase.getConnection();
                Statement query = conn.createStatement();
                Statement delete = conn.createStatement();
                ResultSet results = query.executeQuery(sql)) {
            List<String> names = new ArrayList<>();
            while (results.next()) {
                if (currentTask == null || !currentTask.isWorking()) {
                    break;
                }
                names.add(results.getString("sheet"));
                isCurrent = viewController.data2D != null
                        && results.getLong("d2did") == viewController.data2D.getD2did();
            }
            if (currentTask != null && currentTask.isWorking()) {
                for (String name : names) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        break;
                    }
                    String tname = DerbyBase.fixedIdentifier(name);
                    try {
                        delete.executeUpdate("DROP TABLE " + tname);
                    } catch (Exception e) {
                        MyBoxLog.debug(e, tname);
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
        boolean loadNull = isCurrent;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (loadNull) {
                    viewController.loadNull();
                }
                refreshAction();
            }
        });
        return tableDefinition.deleteCondition(queryConditions);
    }

    @FXML
    public void renameAction() {
        int index = selectedIndix();
        Data2DDefinition selected = selectedItem();
        if (selected == null) {
            return;
        }
        viewController.renameAction(this, index, selected);
    }

    @FXML
    public void queryAction() {
        Data2DManageQueryController.open(this);
    }

}
