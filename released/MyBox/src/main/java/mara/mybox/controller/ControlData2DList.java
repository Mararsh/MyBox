package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-16
 * @License Apache License Version 2.0
 */
public class ControlData2DList extends BaseSysTableController<Data2DDefinition> {

    protected BaseData2DController data2DController;
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
    protected Button openButton, clearDataButton, deleteDataButton, renameDataButton;
    @FXML
    protected FlowPane buttonsPane;

    public ControlData2DList() {
        baseTitle = message("ManageData");
        TipsLabelKey = "DataManageTips";
    }

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
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseData2DController data2DController) {
        try {
            this.data2DController = data2DController;
            tableData2DDefinition = data2DController.loadController.tableData2DDefinition;
            tableDefinition = tableData2DDefinition;
            tableName = tableDefinition.getTableName();
            idColumn = tableDefinition.getIdColumn();

            if (data2DController instanceof Data2DManageController) {
                queryConditions = " data_type != " + Data2D.type(Data2DDefinition.Type.InternalTable);

            } else if (data2DController instanceof MyBoxTablesController) {
                buttonsPane.getChildren().removeAll(openButton, queryButton, clearDataButton, deleteDataButton, renameDataButton);
                queryConditions = " data_type = " + Data2D.type(Data2DDefinition.Type.InternalTable);

            } else if (data2DController instanceof DataInMyBoxClipboardController) {
                buttonsPane.getChildren().removeAll(queryButton);
                queryConditions = " data_type = " + Data2D.type(Data2DDefinition.Type.MyBoxClipboard);

            } else if (data2DController instanceof MatricesManageController) {
                buttonsPane.getChildren().removeAll(openButton, queryButton);
                queryConditions = " data_type = " + Data2D.type(Data2DDefinition.Type.Matrix);

            } else if (data2DController instanceof DataTablesController) {
                buttonsPane.getChildren().removeAll(openButton, queryButton);
                queryConditions = " data_type = " + Data2D.type(Data2DDefinition.Type.DatabaseTable);

            } else {
                queryConditions = null;

            }

            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadList() {
        if (data2DController instanceof MyBoxTablesController) {
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        DataInternalTable dataTable = new DataInternalTable();
                        for (String name : DataInternalTable.InternalTables) {
                            if (tableData2DDefinition.queryTable(conn, name, Data2DDefinition.Type.InternalTable) == null) {
                                dataTable.readDefinitionFromDB(conn, name);
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

        } else if (data2DController instanceof DataTablesController) {
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<String> tables = DerbyBase.allTables(conn);
                        DataTable dataTable = new DataTable();
                        for (String name : tables) {
                            if (DataInternalTable.InternalTables.contains(name)) {
                                continue;
                            }
                            if (tableData2DDefinition.queryTable(conn, name, Data2DDefinition.Type.DatabaseTable) == null) {
                                dataTable.readDefinitionFromDB(conn, name);
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

        } else {
            loadTableData();
        }
    }

    @FXML
    @Override
    public void editAction() {
        if (data2DController.loadController.data2D == null) {
            return;
        }
        Data2DDefinition.open(data2DController.loadController.data2D);
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Load"), StyleTools.getIconImage("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                load();
            });
            items.add(menu);

            if (buttonsPane.getChildren().contains(renameDataButton)) {
                menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    renameAction();
                });
                menu.setDisable(renameDataButton.isDisable());
                items.add(menu);

            }

            if (buttonsPane.getChildren().contains(deleteDataButton)) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                menu.setDisable(deleteDataButton.isDisable());
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            items.addAll(super.makeTableContextMenu());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemClicked() {
        load();
    }

    @Override
    public void itemDoubleClicked() {
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (clearDataButton != null) {
            clearDataButton.setDisable(isEmpty);
        }
        if (deleteDataButton != null) {
            deleteDataButton.setDisable(none);
        }
        if (renameDataButton != null) {
            renameDataButton.setDisable(none);
        }
    }

    @Override
    protected int deleteData(List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        if (data2DController instanceof Data2DManageController
                || data2DController instanceof DataTablesController) {
            boolean changed = false;
            try ( Connection conn = DerbyBase.getConnection();
                     Statement statement = conn.createStatement()) {
                for (Data2DDefinition item : data) {
                    if (item.isUserTable() && item.getSheet() != null) {
                        try {
                            statement.executeUpdate("DROP TABLE " + item.getSheet());
                            changed = true;
                        } catch (Exception e) {
                            MyBoxLog.debug(e);
                        }
                        if (data2DController.loadController.data2D != null
                                && item.getD2did() == data2DController.loadController.data2D.getD2did()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data2DController.loadDef(null);
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
                        if (data2DController instanceof Data2DManageController) {
                            DataTablesController.updateList();
                        } else if (data2DController instanceof DataTablesController) {
                            Data2DManageController.updateList();
                        }
                    }
                });
            }
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected long clearData() {
        if (data2DController instanceof Data2DManageController
                || data2DController instanceof DataTablesController) {
            String sql = "SELECT d2did, sheet FROM " + tableData2DDefinition.getTableName()
                    + " WHERE  data_type = " + Data2D.type(Data2DDefinition.Type.DatabaseTable);
            try ( Connection conn = DerbyBase.getConnection();
                     Statement query = conn.createStatement();
                     Statement delete = conn.createStatement();
                     ResultSet results = query.executeQuery(sql)) {
                while (results.next()) {
                    String sheet = results.getString("sheet");
                    if (sheet != null) {
                        try {
                            delete.executeUpdate("DROP TABLE " + sheet);
                        } catch (Exception e) {
                            MyBoxLog.debug(e);
                        }
                        long id = results.getLong("d2did");
                        if (data2DController.loadController.data2D != null
                                && id == data2DController.loadController.data2D.getD2did()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data2DController.loadDef(null);
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                return -1;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (data2DController instanceof Data2DManageController) {
                        DataTablesController.updateList();
                    } else if (data2DController instanceof DataTablesController) {
                        Data2DManageController.updateList();
                    }
                }
            });
        }
        return tableDefinition.deleteCondition(queryConditions);
    }

    public void load() {
        try {
            load(tableView.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(Data2DDefinition source) {
        try {
            if (source == null || data2DController == null
                    || !data2DController.checkBeforeNextAction()) {
                return;
            }
            data2DController.loadDef(source);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void renameAction() {
        if (data2DController == null) {
            return;
        }
        int index = tableView.getSelectionModel().getSelectedIndex();
        Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        data2DController.loadController.renameAction(this, index, selected);
    }

    @FXML
    public void queryAction() {
        Data2DManageQueryController.open(this);
    }

    @FXML
    public void popOpen(MouseEvent mouseEvent) {
        if (!(data2DController instanceof Data2DManageController)) {
            return;
        }
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem("CSV", StyleTools.getIconImage("iconCSV.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.CSV);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("Excel", StyleTools.getIconImage("iconExcel.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Excel);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Texts"), StyleTools.getIconImage("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Texts);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Matrix"), StyleTools.getIconImage("iconMatrix.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Matrix);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("DatabaseTable"), StyleTools.getIconImage("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.DatabaseTable);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("MyBoxClipboard"), StyleTools.getIconImage("iconClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.MyBoxClipboard);
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            data2DController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}