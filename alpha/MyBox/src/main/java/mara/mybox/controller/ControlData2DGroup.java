package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class ControlData2DGroup extends BaseTableViewController<DataFilter> {

    protected BaseData2DGroupController dataController;
    protected ChangeListener<Boolean> listener;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton valuesRadio, intervalRadio, numberRadio, conditionsRadio;
    @FXML
    protected VBox groupBox, columnsBox, conditionsBox;
    @FXML
    protected HBox columnlBox, intervalBox, numberBox;
    @FXML
    protected TextField intervalInput, numberInput;
    @FXML
    protected ComboBox<String> columnSelector;
    @FXML
    protected TableColumn<DataFilter, String> conditionColumn;
    @FXML
    protected TableColumn<DataFilter, Boolean> reverseColumn;
    @FXML
    protected TableColumn<DataFilter, Long> maxColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();

            columnsController.setParameters(this, message("Column"), message("GroupBy"));
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkGroupType();
                }
            });
            checkGroupType();

            conditionColumn.setCellValueFactory(new PropertyValueFactory<>("sourceScript"));
            reverseColumn.setCellValueFactory(new PropertyValueFactory<>("reversed"));
            maxColumn.setCellValueFactory(new PropertyValueFactory<>("maxPassed"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseData2DGroupController dataController) {
        try {
            this.dataController = dataController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            columnsController.loadNames(null);
            columnSelector.getItems().clear();
            tableData.clear();
            if (!dataController.data2D.isValid()) {
                return;
            }
            List<String> names = dataController.data2D.columnNames();
            columnsController.loadNames(names);
            columnSelector.getItems().setAll(names);
            columnSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkGroupType() {
        try {
            groupBox.getChildren().clear();
            if (valuesRadio.isSelected()) {
                groupBox.getChildren().add(columnsBox);

            } else if (intervalRadio.isSelected()) {
                groupBox.getChildren().addAll(columnlBox, intervalBox);

            } else if (numberRadio.isSelected()) {
                groupBox.getChildren().addAll(columnlBox, numberBox);

            } else if (conditionsRadio.isSelected()) {
                groupBox.getChildren().add(conditionsBox);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void addAction() {
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(dataController, null);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.add(filter);
                }
                controller.close();
            }
        };
        controller.notify.addListener(listener);
    }

    @FXML
    @Override
    public void editAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        DataFilter selected = tableData.get(index);
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(dataController, selected);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.set(index, filter);
                }
                controller.close();
            }
        };
        controller.notify.addListener(listener);
    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            List<DataFilter> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                clearAction();
                return;
            }
            tableData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
