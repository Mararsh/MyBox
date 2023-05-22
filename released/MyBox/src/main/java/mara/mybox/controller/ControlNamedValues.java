package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.NamedValues;
import mara.mybox.db.table.TableNamedValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableTextTruncCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-8
 * @License Apache License Version 2.0
 */
public class ControlNamedValues extends BaseSysTableController<NamedValues> {

    protected TableNamedValues tableNamedValues;
    protected String key;
    protected SimpleBooleanProperty uesNotify;

    @FXML
    protected TableColumn<NamedValues, String> nameColumn, valueColumn;
    @FXML
    protected TableColumn<NamedValues, Date> modifyColumn;
    @FXML
    protected Button useButton, clearDataButton, deleteDataButton, renameDataButton;
    @FXML
    protected FlowPane buttonsPane;

    public ControlNamedValues() {
        uesNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setTableDefinition() {
        tableNamedValues = new TableNamedValues();
        tableDefinition = tableNamedValues;
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new TableTextTruncCell());
            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            modifyColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadList(String key) {
        try {
            this.key = key;
            queryConditions = "key_name='" + key + "'";
            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void save(String name, String value) {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override

            protected boolean handle() {
                NamedValues data = new NamedValues(key, name, value, new Date());
                return tableNamedValues.writeData(data) != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                refreshAction();
            }

        };
        start(task);
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isNoneSelected();
        clearDataButton.setDisable(isEmpty);
        deleteDataButton.setDisable(none);
        renameDataButton.setDisable(none);
        useButton.setDisable(none);
    }

    @FXML
    @Override
    public void editAction() {
        NamedValues selected = selectedItem();
        if (selected != null) {
            uesNotify.set(!uesNotify.get());
        }
    }

    @FXML
    public void useAction() {
        editAction();
    }

    @FXML
    public void renameAction() {
        int index = selectedIndix();
        if (index < 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        NamedValues selected = tableData.get(index);
        String newName = PopTools.askValue(getTitle(), message("CurrentName") + ":" + selected.getName(),
                message("NewName"), selected.getName() + "m");
        task = new SingletonTask<Void>(this) {
            NamedValues updated;

            @Override

            protected boolean handle() {
                selected.setName(newName);
                updated = tableNamedValues.updateData(selected);
                return updated != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                tableData.set(index, updated);
            }

        };
        start(task);
    }

    @FXML
    public void queryAction() {
//        Data2DManageQueryController.open(this);
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Use"), StyleTools.getIconImageView("iconYes.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                useAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameDataButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
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
    public void cleanPane() {
        try {
            uesNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
