package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class ControlSelection extends BaseTablePagesController<List<String>> {

    @FXML
    protected TableColumn<List<String>, String> nameColumn;
    @FXML
    protected Label titleLabel;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                    try {
                        List<String> row = (List<String>) param.getValue();
                        String value = row.get(0);
                        if (value == null) {
                            return null;
                        }
                        return new SimpleStringProperty(value);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent, String name, String title) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            setName(name);
            setTitle(title);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setName(String name) {
        nameColumn.setText(name);
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setWidth(int width) {
        nameColumn.setPrefWidth(width);
    }

    public void loadNames(List<String> names) {
        isSettingValues = true;
        tableData.clear();
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                List<String> row = new ArrayList<>();
                row.add(name);
                tableData.add(row);
            }
        }
        isSettingValues = false;
        tableChanged(true);
    }

    public void selectNames(List<String> names) {
        selectNone();
        if (tableData != null && names != null && !names.isEmpty()) {
            isSettingValues = true;
            List<List<String>> data = new ArrayList<>();
            for (String name : names) {
                List<String> row = new ArrayList<>();
                row.add(name);
                data.add(row);
            }
            for (List<String> row : tableData) {
                if (!names.contains(row.get(0))) {
                    data.add(row);
                }
            }
            tableData.setAll(data);
            tableView.getSelectionModel().selectRange(0, names.size());
            isSettingValues = false;
        }
        notifySelected();
    }

    public List<List<String>> selectedRows() {
        return selectedItems();
    }

    public List<String> selectedNames() {
        List<List<String>> selected = selectedRows();
        if (selected == null || selected.isEmpty()) {
            return null;
        }
        List<String> selectedNames = new ArrayList<>();
        for (List<String> row : selected) {
            selectedNames.add(row.get(0));
        }
        return selectedNames;
    }

    public String selectedNamesString(String seperator) {
        List<List<String>> selected = selectedRows();
        if (selected == null || selected.isEmpty()) {
            return null;
        }
        String selectedNames = null;
        for (List<String> row : selected) {
            if (selectedNames == null) {
                selectedNames = row.get(0);

            } else {
                selectedNames += seperator + row.get(0);
            }
        }
        return selectedNames;
    }

}
