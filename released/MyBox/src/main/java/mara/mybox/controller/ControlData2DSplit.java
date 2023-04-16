package mara.mybox.controller;

import java.util.Arrays;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.ValueRange;
import mara.mybox.data.ValueRange.SplitType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableBooleanCell;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-11-21
 * @License Apache License Version 2.0
 */
public class ControlData2DSplit extends BaseTableViewController<ValueRange> {

    protected Data2DColumn column;
    protected double size;
    protected int number, scale;
    protected SplitType splitType;
    protected ChangeListener<Boolean> listener;

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected VBox inputsBox, listBox;
    @FXML
    protected FlowPane unitPane;
    @FXML
    protected ToggleGroup unitGroup;
    @FXML
    protected RadioButton sizeRadio, numberRadio, listRadio,
            yearsRadio, daysRadio, hoursRadio, minutesRadio, secondsRadio, msRadio;
    @FXML
    protected TextField sizeInput, numberInput;
    @FXML
    protected ComboBox<String> scaleSelector;
    @FXML
    protected HBox scaleBox;
    @FXML
    protected TableColumn<ValueRange, Double> startColumn, endColumn;
    @FXML
    protected TableColumn<ValueRange, Boolean> includeStartColumn, includeEndColumn;

    public ControlData2DSplit() {
        splitType = SplitType.Size;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.getSelectionModel().select(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            scale = v;
                            scaleSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "Scale", scale);
                        } else {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
            endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
            includeStartColumn.setCellValueFactory(new PropertyValueFactory<>("includeStart"));
            includeStartColumn.setCellFactory(new TableBooleanCell());
            includeEndColumn.setCellValueFactory(new PropertyValueFactory<>("includeEnd"));
            includeEndColumn.setCellFactory(new TableBooleanCell());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        try {
            parentController = parent;
            baseName = baseName + "_" + parent.baseName;

            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkSplitType();
                }
            });

            sizeInput.setText(UserConfig.getString(baseName + "Size", "100"));
            sizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkSize();
                }
            });

            numberInput.setText(UserConfig.getString(baseName + "Number", "3"));
            numberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkNumber();
                }
            });

            checkSplitType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setColumn(Data2DColumn column) {
        try {
            this.column = column;

            checkSplitType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public boolean checkSplitType() {
        inputsBox.getChildren().clear();
        sizeInput.setStyle(null);
        numberInput.setStyle(null);

        if (sizeRadio.isSelected()) {
            splitType = SplitType.Size;
            inputsBox.getChildren().add(sizeInput);
            if (column != null && column.isTimeType()) {
                inputsBox.getChildren().add(unitPane);
            } else {
                inputsBox.getChildren().add(scaleBox);
            }
            return checkSize();

        } else if (numberRadio.isSelected()) {
            splitType = SplitType.Number;
            inputsBox.getChildren().add(numberInput);
            if (column == null || !column.isTimeType()) {
                inputsBox.getChildren().add(scaleBox);
            }
            return checkNumber();

        } else if (listRadio.isSelected()) {
            splitType = SplitType.List;
            inputsBox.getChildren().add(listBox);
            if (column == null || !column.isTimeType()) {
                inputsBox.getChildren().add(scaleBox);
            }
            refreshStyle(listBox);
            return !tableData.isEmpty();
        }
        return false;
    }

    protected boolean checkSize() {
        if (isSettingValues) {
            return true;
        }
        try {
            double v = Double.parseDouble(sizeInput.getText());
            if (v > 0) {
                size = v;
                UserConfig.setString(baseName + "Size", size + "");
                sizeInput.setStyle(null);
            } else {
                sizeInput.setStyle(UserConfig.badStyle());
                return false;
            }
        } catch (Exception e) {
            sizeInput.setStyle(UserConfig.badStyle());
            return false;
        }
        if (column == null || !column.isTimeType()) {
            return true;
        }
        if (yearsRadio.isSelected()) {
            size = size * 365 * 24 * 3600 * 1000;
        } else if (daysRadio.isSelected()) {
            size = size * 24 * 3600 * 1000;
        } else if (hoursRadio.isSelected()) {
            size = size * 3600 * 1000;
        } else if (minutesRadio.isSelected()) {
            size = size * 60 * 1000;
        } else if (secondsRadio.isSelected()) {
            size = size * 1000;
        }
        return true;
    }

    protected boolean checkNumber() {
        try {
            int v = Integer.parseInt(numberInput.getText());
            if (v > 0) {
                numberInput.setStyle(null);
                number = v;
                UserConfig.setString(baseName + "Number", number + "");
                return true;
            } else {
                numberInput.setStyle(UserConfig.badStyle());
                return false;
            }
        } catch (Exception e) {
            numberInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected boolean isValid() {
        if (sizeRadio.isSelected()) {
            return !UserConfig.badStyle().equals(sizeInput.getStyle());
        } else if (numberRadio.isSelected()) {
            return !UserConfig.badStyle().equals(numberInput.getStyle());
        } else if (listRadio.isSelected()) {
            return !tableData.isEmpty();
        } else {
            return false;
        }
    }

    @FXML
    @Override
    public void addAction() {
        if (column == null) {
            return;
        }
        ValueRangeInputController controller = ValueRangeInputController.open(this, column, null);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                ValueRange range = controller.getRange();
                if (range != null) {
                    tableData.add(range);
                }
                controller.close();
            }
        };
        controller.notify.addListener(listener);
    }

    @FXML
    @Override
    public void editAction() {
        if (column == null) {
            return;
        }
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        ValueRange selected = tableData.get(index);
        ValueRangeInputController controller = ValueRangeInputController.open(this, column, selected);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                ValueRange range = controller.getRange();
                if (range != null) {
                    tableData.set(index, range);
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
            List<ValueRange> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                clearAction();
                return;
            }
            tableData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            listener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
