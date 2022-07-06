package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-7
 * @License Apache License Version 2.0
 */
public class Data2DMarkAbnormalController extends BaseSysTableController<Data2DStyle> {

    protected ControlData2DEditTable tableController;
    protected TableData2DStyle tableData2DStyle;
    protected String styleValue;
    protected Data2DStyle updatedStyle, orignialStyle;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected TableColumn<Data2DStyle, Long> sidColumn, fromColumn, toColumn;
    @FXML
    protected TableColumn<Data2DStyle, Integer> sequenceColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> columnsColumn, rowFilterColumn, columnFilterColumn,
            fontColorColumn, bgColorColumn, fontSizeColumn, boldColumn, moreColumn;
    @FXML
    protected CheckBox abnormalCheck, sizeCheck;
    @FXML
    protected TextField fromInput, toInput, sequenceInput;
    @FXML
    protected Label idLabel;
    @FXML
    protected FlowPane columnsPane;
    @FXML
    protected ControlData2DRowFilter rowFilterController;
    @FXML
    protected ControlData2DColumnFilter columnFilterController;
    @FXML
    protected ControlData2DStyle editController;

    public Data2DMarkAbnormalController() {
        baseTitle = message("MarkAbnormalValues");
        TipsLabelKey = "MarkAbnormalValuesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rowFilterController.setParameters(this);
            columnFilterController.setParameters(this);

            // For display, indices are 1-based and included
            // For internal, indices are 0-based and excluded
            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkInputs();
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkInputs();
                }
            });

            sequenceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkInputs();
                }
            });

            editController.showLabel = idLabel;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            sidColumn.setCellValueFactory(new PropertyValueFactory<>("d2sid"));
            sequenceColumn.setCellValueFactory(new PropertyValueFactory<>("sequence"));
            fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
            toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
            columnsColumn.setCellValueFactory(new PropertyValueFactory<>("columns"));
            rowFilterColumn.setCellValueFactory(new PropertyValueFactory<>("rowFilter"));
            columnFilterColumn.setCellValueFactory(new PropertyValueFactory<>("columnFilter"));
            fontColorColumn.setCellValueFactory(new PropertyValueFactory<>("fontColor"));
            bgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            fontSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fontSize"));
            boldColumn.setCellValueFactory(new PropertyValueFactory<>("bold"));
            moreColumn.setCellValueFactory(new PropertyValueFactory<>("moreStyle"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            tableData2DStyle = new TableData2DStyle();
            tableDefinition = tableData2DStyle;
            tableName = tableDefinition.getTableName();
            idColumn = tableDefinition.getIdColumn();

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            sourceChanged();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            getMyStage().setTitle(baseTitle + " - " + tableController.data2D.displayName());

            queryConditions = "  d2id=" + tableController.data2D.getD2did() + "";

            rowFilterController.data2D = tableController.data2D;
            columnFilterController.data2D = tableController.data2D;

            columnsPane.getChildren().clear();
            for (Data2DColumn column : tableController.data2D.getColumns()) {
                columnsPane.getChildren().add(new CheckBox(column.getColumnName()));
            }
            loadTableData();
            editStyle(orignialStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkInputs() {
        if (isSettingValues) {
            return;
        }
        if (updatedStyle == null) {
            updatedStyle = new Data2DStyle();
        }
        try {
            String sv = fromInput.getText();
            if (sv == null || sv.isBlank()) {
                updatedStyle.setRowStart(-1);
            } else {
                updatedStyle.setRowStart(Long.parseLong(sv) - 1);
            }
            fromInput.setStyle(null);
        } catch (Exception e) {
            fromInput.setStyle(UserConfig.badStyle());
        }
        try {
            String sv = toInput.getText();
            if (sv == null || sv.isBlank()) {
                updatedStyle.setRowEnd(-1);
            } else {
                updatedStyle.setRowEnd(Long.parseLong(sv));
            }
            toInput.setStyle(null);
        } catch (Exception e) {
            toInput.setStyle(UserConfig.badStyle());
        }
        try {
            String sv = sequenceInput.getText();
            if (sv == null || sv.isBlank()) {
                updatedStyle.setSequence(dataSize + 1);
            } else {
                updatedStyle.setSequence(Float.valueOf(sv));
            }
            sequenceInput.setStyle(null);
        } catch (Exception e) {
            sequenceInput.setStyle(UserConfig.badStyle());
        }
    }

    public void checkStyle() {
        if (isSettingValues) {
            return;
        }
        if (updatedStyle == null) {
            updatedStyle = new Data2DStyle();
        }
        editController.checkStyle(updatedStyle);
    }

    public void loadNull() {
        orignialStyle = new Data2DStyle();
        updatedStyle = orignialStyle;
        isSettingValues = true;
        fromInput.clear();
        toInput.clear();
        selectNoneColumn();
        rowFilterController.scriptInput.clear();
        editController.loadNull(updatedStyle);
        sequenceInput.setText((dataSize + 1) + "");
        abnormalCheck.setSelected(false);
        isSettingValues = false;
        checkStyle();
        checkInputs();
        recoverButton.setDisable(true);
    }

    @Override
    public void itemClicked() {
        editAction();
    }

    @Override
    protected void afterDeletion() {
        super.afterDeletion();
        reloadDataPage();
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        reloadDataPage();
    }

    @FXML
    @Override
    public void editAction() {
        Data2DStyle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        editStyle(selected);
    }

    // For display, indices are 1-based and included
    // For internal, indices are 0-based and excluded
    public void editStyle(Data2DStyle style) {
        if (style == null) {
            loadNull();
            return;
        }
        orignialStyle = style;
        updatedStyle = style.cloneAll();
        recoverButton.setDisable(updatedStyle.getD2sid() >= 0);

        isSettingValues = true;
        fromInput.setText(updatedStyle.getRowStart() < 0 ? "" : (updatedStyle.getRowStart() + 1) + "");
        toInput.setText(updatedStyle.getRowEnd() < 0 ? "" : updatedStyle.getRowEnd() + "");
        String scolumns = updatedStyle.getColumns();
        if (scolumns == null || scolumns.isBlank()) {
            selectAllColumns();
        } else {
            selectNoneColumn();
            String[] ns = scolumns.split(Data2DStyle.ColumnSeparator);
            for (String s : ns) {
                for (Node node : columnsPane.getChildren()) {
                    CheckBox cb = (CheckBox) node;
                    if (cb.getText().equals(s)) {
                        cb.setSelected(true);
                        break;
                    }
                }
            }
        }
        rowFilterController.scriptInput.setText(updatedStyle.getRowFilter());

        sequenceInput.setText(updatedStyle.getSequence() + "");
        abnormalCheck.setSelected(updatedStyle.isAbnoramlValues());

        editController.editStyle(updatedStyle);
        isSettingValues = false;
        checkStyle();
    }

    @FXML
    @Override
    public void createAction() {
        loadNull();
    }

    @FXML
    public void copyDataAction() {
        orignialStyle = updatedStyle.cloneAll();
        orignialStyle.setD2sid(-1);
        updatedStyle = orignialStyle;
        sequenceInput.setText((dataSize + 1) + "");
        checkStyle();
    }

    @FXML
    @Override
    public void recoverAction() {
        editStyle(orignialStyle);
    }

    @FXML
    public void selectAllColumns() {
        try {
            for (Node node : columnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneColumn() {
        try {
            for (Node node : columnsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(false);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void saveAction() {
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    checkStyle();
                    updatedStyle.setD2id(tableController.data2D.getD2did());
                    String columns = "";
                    boolean allColumns = true;
                    for (Node node : columnsPane.getChildren()) {
                        CheckBox cb = (CheckBox) node;
                        if (cb.isSelected()) {
                            if (columns.isBlank()) {
                                columns = cb.getText();
                            } else {
                                columns += Data2DStyle.ColumnSeparator + cb.getText();
                            }
                        } else {
                            allColumns = false;
                        }
                    }
                    if (allColumns) {
                        columns = null;
                    }
                    updatedStyle.setColumns(columns);
                    updatedStyle.setRowFilter(rowFilterController.scriptInput.getText());
                    updatedStyle.setAbnoramlValues(abnormalCheck.isSelected());
                    return tableData2DStyle.writeData(updatedStyle) != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                orignialStyle = updatedStyle;
                loadTableData();
                reloadDataPage();
            }
        };
        start(task);
    }

    public void reloadDataPage() {
        if (tableController.checkBeforeNextAction()) {
            tableController.dataController.goPage();
            tableController.requestMouse();
        }
    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DMarkAbnormalController open(ControlData2DEditTable tableController) {
        try {
            Data2DMarkAbnormalController controller = (Data2DMarkAbnormalController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DMarkAbnormalFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
