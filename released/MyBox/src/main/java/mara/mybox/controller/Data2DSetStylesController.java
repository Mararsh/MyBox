package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DStyle;
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
public class Data2DSetStylesController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected Data2DStyle currentStyle;
    protected ChangeListener<Boolean> tableStatusListener;
    protected DataFilter filter;
    protected Data2DStyle updatedStyle;

    @FXML
    protected ControlData2DStyleList listController;
    @FXML
    protected Label idLabel;
    @FXML
    protected Tab baseTab, dataTab, filterTab, styleTab;
    @FXML
    protected CheckBox abnormalCheck, sizeCheck;
    @FXML
    protected TextField titleInput, fromInput, toInput, sequenceInput;
    @FXML
    protected FlowPane columnsPane;
    @FXML
    protected ControlData2DFilter filterController;
    @FXML
    protected ControlData2DStyle editController;

    public Data2DSetStylesController() {
        baseTitle = message("SetStyles");
        TipsLabelKey = "SetStylesTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            rightPaneControl = listController.rightPaneControl;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            listController.setParameters(this);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            filterController.setParameters(this);

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

            listController.sourceChanged();
            filterController.setData2D(tableController.data2D.cloneAll());

            columnsPane.getChildren().clear();
            for (Data2DColumn column : tableController.data2D.getColumns()) {
                columnsPane.getChildren().add(new CheckBox(column.getColumnName()));
            }
            loadStyle(currentStyle);

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
            updatedStyle.setTitle(titleInput.getText());
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
                updatedStyle.setSequence(listController.dataSize + 1);
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
        currentStyle = new Data2DStyle();
        updatedStyle = currentStyle;
        isSettingValues = true;
        titleInput.clear();
        fromInput.clear();
        toInput.clear();
        selectNoneColumn();
        filterController.load(null, false);
        editController.loadNull(currentStyle);
        sequenceInput.setText((listController.dataSize + 1) + "");
        abnormalCheck.setSelected(false);
        isSettingValues = false;
        checkStyle();
        checkInputs();
        recoverButton.setDisable(true);
    }

    public void loadStyle(Data2DStyle style) {
        if (style == null || tableController == null || tableController.data2D == null
                || style.getD2id() != tableController.data2D.getD2did()) {
            loadNull();
            return;
        }
        currentStyle = style;
        updatedStyle = style.cloneAll();
        recoverButton.setDisable(updatedStyle.getD2sid() >= 0);

        isSettingValues = true;
        titleInput.setText(style.getTitle());
        // For display, indices are 1-based and included
        // For internal, indices are 0-based and excluded
        fromInput.setText(updatedStyle.getRowStart() < 0 ? "" : (updatedStyle.getRowStart() + 1) + "");
        toInput.setText(updatedStyle.getRowEnd() < 0 ? "" : updatedStyle.getRowEnd() + "");
        String scolumns = updatedStyle.getColumns();
        selectNoneColumn();
        if (scolumns != null && !scolumns.isBlank()) {
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
        filterController.load(updatedStyle.getFilter(), updatedStyle.isFilterReversed());

        sequenceInput.setText(updatedStyle.getSequence() + "");
        abnormalCheck.setSelected(updatedStyle.isAbnoramlValues());

        editController.editStyle(updatedStyle);
        isSettingValues = false;
        checkStyle();
    }

    public void reloadDataPage() {
        if (tableController == null || !tableController.checkBeforeNextAction()) {
            return;
        }
        tableController.dataController.goPage();
        tableController.requestMouse();
    }

    @FXML
    @Override
    public void createAction() {
        loadNull();
    }

    @FXML
    public void copyDataAction() {
        currentStyle = updatedStyle.cloneAll();
        currentStyle.setD2sid(-1);
        updatedStyle = currentStyle;
        sequenceInput.setText((listController.dataSize + 1) + "");
        checkStyle();
    }

    @FXML
    @Override
    public void recoverAction() {
        loadStyle(currentStyle);
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

    public boolean pickValues() {
        try {
            checkInputs();
            if (UserConfig.badStyle().equals(sequenceInput.getStyle())) {
                popError(message("InvalidParameters"));
                tabPane.getSelectionModel().select(baseTab);
                return false;
            }
            if (UserConfig.badStyle().equals(fromInput.getStyle())
                    || UserConfig.badStyle().equals(toInput.getStyle())) {
                popError(message("InvalidParameters"));
                tabPane.getSelectionModel().select(dataTab);
                return false;
            }
            if (!filterController.checkExpression(false)) {
                popError(filterController.error);
                tabPane.getSelectionModel().select(filterTab);
                return false;
            }
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

            updatedStyle.setFilter(filterController.filter.getSourceScript());
            updatedStyle.setFilterReversed(filterController.filter.isReversed());
            updatedStyle.setAbnoramlValues(abnormalCheck.isSelected());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (!pickValues()) {
            return;
        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    return listController.tableData2DStyle.writeData(updatedStyle) != null;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.console(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                currentStyle = updatedStyle;
                listController.loadTableData();
                reloadDataPage();
            }
        };
        start(task);
    }

    @FXML
    public void dataAction() {
        Data2DSetStylesController.open(tableController);
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
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
    public static Data2DSetStylesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetStylesController controller = (Data2DSetStylesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetStylesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
