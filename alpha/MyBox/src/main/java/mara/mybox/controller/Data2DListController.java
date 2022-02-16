package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DListController extends BaseSysTableController<Data2DDefinition> {

    protected Data2D data2D;

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2didColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> rowsColumn, colsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn, typeColumn, fileColumn, sheetColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> modifyColumn;
    @FXML
    protected Button clearDataButton, deleteDataButton, renameDataButton;
    @FXML
    protected Label dataNameLabel;
    @FXML
    protected ControlData2DLoad loadController;

    public Data2DListController() {
        baseTitle = message("ManageData");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            setData2D();

            tableDefinition = data2D.getTableData2DDefinition();
            tableName = tableDefinition.getTableName();
            idColumn = tableDefinition.getIdColumn();
            setQueryConditions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setData2D() {
        data2D = Data2D.create(Data2DDefinition.Type.DatabaseTable);
    }

    public void setQueryConditions() {
        queryConditions = null;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initList();

            loadController.dataLabel = dataNameLabel;
            loadController.baseTitle = baseTitle;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initList() {
        loadTableData();
    }

    public void load(Data2DDefinition source) {
        try {
            if (data2D == null || source == null
                    || loadController == null || !checkBeforeNextAction()) {
                return;
            }
            loadController.resetStatus();
            data2D = Data2D.create(source.getType());
            data2D.cloneAll(source);

            loadController.setData(data2D);
            loadController.readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        return true;
    }

    @FXML
    @Override
    public void editAction() {
        if (data2D == null) {
            return;
        }
        Data2DDefinition.open(data2D);
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

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Load"), StyleTools.getIconImage("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                load();
            });
            items.add(menu);

            if (renameDataButton != null) {
                menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    renameAction();
                });
                menu.setDisable(renameDataButton.isDisable());
                items.add(menu);
            }

            if (deleteDataButton != null) {
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

    public void load() {
        try {
            load(tableView.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void renameAction() {
        if (loadController == null) {
            return;
        }
        int index = tableView.getSelectionModel().getSelectedIndex();
        Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        loadController.renameAction(this, index, selected);
    }

    @Override
    public void cleanPane() {
        try {
            loadController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
