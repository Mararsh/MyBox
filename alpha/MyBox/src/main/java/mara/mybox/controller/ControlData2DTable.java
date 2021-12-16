package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class ControlData2DTable extends BaseSysTableController<Data2DDefinition> {

    protected Data2DManufactureController manufactureController;
    protected Data2D data2D;

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2didColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> rowsColumn, colsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn, typeColumn, fileColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> modifyColumn;
    @FXML
    protected Button clearDataButton, deleteDataButton, editDataButton, renameDataButton;

    public ControlData2DTable() {
        baseTitle = Languages.message("ManufactureData");
    }

    public void setParameters(Data2DManufactureController manufactureController) {
        try {
            this.manufactureController = manufactureController;

            this.data2D = manufactureController.data2D;
            tableDefinition = data2D.getTableData2DDefinition();

            loadTableData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            d2didColumn.setCellValueFactory(new PropertyValueFactory<>("d2did"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));
            colsColumn.setCellValueFactory(new PropertyValueFactory<>("colsNumber"));
            colsColumn.setCellFactory(new TableNumberCell());
            rowsColumn.setCellValueFactory(new PropertyValueFactory<>("rowsNumber"));
            rowsColumn.setCellFactory(new TableNumberCell());
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
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
            menu.setDisable(editDataButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Edit"), StyleTools.getIconImage("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                editAction();
            });
            menu.setDisable(editDataButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameDataButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteAction();
            });
            menu.setDisable(deleteDataButton.isDisable());
            items.add(menu);

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
    public void updateStatus() {
        super.updateStatus();
        if (getMyStage() != null) {
            String title = baseTitle;
            if (!data2D.isTmpData()) {
                title += " " + data2D.getDataName();
            }
            myStage.setTitle(title);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        clearDataButton.setDisable(isEmpty);
        deleteDataButton.setDisable(none);
        editDataButton.setDisable(none);
        renameDataButton.setDisable(none);
    }

    public void load() {
        try {
            manufactureController.load(tableView.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        Data2DDefinition.open(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void renameAction() {

    }

    @Override
    public void cleanPane() {
        try {
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
