package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
public class Data2DSetStylesController extends BaseSysTableController<Data2DStyle> {

    protected ControlData2DEditTable tableController;
    protected TableData2DStyle tableData2DStyle;
    protected String styleValue;
    protected Data2DStyle updatedStyle, orignialStyle;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected TableColumn<Data2DStyle, Long> sidColumn, fromColumn, toColumn;
    @FXML
    protected TableColumn<Data2DStyle, String> columnsColumn, filterColumn,
            fontColorColumn, bgColorColumn, fontSizeColumn, boldColumn, moreColumn;
    @FXML
    protected ToggleGroup colorGroup, bgGroup;
    @FXML
    protected ColorSet fontColorController, bgColorController;
    @FXML
    protected ComboBox<String> fontSizeSelector;
    @FXML
    protected CheckBox colorCheck, bgCheck, sizeCheck, boldCheck;
    @FXML
    protected TextField fromInput, toInput;
    @FXML
    protected TextArea moreInput;
    @FXML
    protected Label idLabel;
    @FXML
    protected RadioButton colorDefaultRadio, colorSetRadio, bgDefaultRadio, bgSetRadio;
    @FXML
    protected FlowPane columnsPane;
    @FXML
    protected ControlData2DRowFilter filterController;

    public Data2DSetStylesController() {
        baseTitle = message("SetStyles");
        TipsLabelKey = "Data2DSetStylesTips";
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
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        if (updatedStyle == null) {
                            updatedStyle = new Data2DStyle();
                        }
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
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        if (updatedStyle == null) {
                            updatedStyle = new Data2DStyle();
                        }
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
                }
            });

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle();
                }
            });
            fontColorController.thisPane.disableProperty().bind(colorDefaultRadio.selectedProperty());
            fontColorController.init(this, baseName + "Color", Color.BLACK);
            fontColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle();
                }
            });

            bgGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkStyle();
                }
            });
            bgColorController.thisPane.disableProperty().bind(bgDefaultRadio.selectedProperty());
            bgColorController.init(this, baseName + "BgColor", Color.TRANSPARENT);
            bgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    checkStyle();
                }
            });

            List<String> sizes = Arrays.asList(
                    message("Default"), "0.8em", "1.2em",
                    "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            fontSizeSelector.getItems().addAll(sizes);
            fontSizeSelector.getSelectionModel().select(UserConfig.getString(baseName + "FontSize", message("Default")));
            fontSizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> o, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString(baseName + "FontSize", newValue);
                    checkStyle();
                }
            });

            boldCheck.setSelected(UserConfig.getBoolean(baseName + "Bold", false));
            boldCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "Bold", newValue);
                    checkStyle();
                }
            });

            moreInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkStyle();
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
            fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
            toColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
            columnsColumn.setCellValueFactory(new PropertyValueFactory<>("columns"));
            filterColumn.setCellValueFactory(new PropertyValueFactory<>("moreConditions"));
            fontColorColumn.setCellValueFactory(new PropertyValueFactory<>("fontColor"));
            bgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            fontSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fontSize"));
            boldColumn.setCellValueFactory(new PropertyValueFactory<>("bold"));
            moreColumn.setCellValueFactory(new PropertyValueFactory<>("moreStyle"));
            sidColumn.setCellValueFactory(new PropertyValueFactory<>("d2sid"));

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

            filterController.tipsView.setVisible(false);

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

            filterController.data2D = tableController.data2D;

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

    public void checkStyle() {
        if (isSettingValues) {
            return;
        }
        if (updatedStyle == null) {
            updatedStyle = new Data2DStyle();
        }
        if (colorDefaultRadio.isSelected()) {
            updatedStyle.setFontColor(null);
        } else {
            updatedStyle.setFontColor(fontColorController.rgb());
        }
        if (bgDefaultRadio.isSelected()) {
            updatedStyle.setBgColor(null);
        } else {
            updatedStyle.setBgColor(bgColorController.rgb());
        }
        updatedStyle.setFontSize(fontSizeSelector.getValue());
        updatedStyle.setBold(boldCheck.isSelected());
        updatedStyle.setMoreStyle(moreInput.getText());
        idLabel.setText(updatedStyle.getD2sid() < 0
                ? message("NewData") : (message("ID") + ": " + updatedStyle.getD2sid()));
        idLabel.setStyle(updatedStyle.finalStyle());
    }

    public void loadNull() {
        orignialStyle = new Data2DStyle();
        updatedStyle = orignialStyle;
        isSettingValues = true;
        fromInput.clear();
        toInput.clear();
        selectNoneColumn();
        filterController.scriptInput.clear();
        colorDefaultRadio.fire();
        bgDefaultRadio.fire();
        fontSizeSelector.setValue(message("Default"));
        boldCheck.setSelected(false);
        moreInput.clear();
        isSettingValues = false;
        checkStyle();
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
        loadNull();
        if (style == null) {
            return;
        }
        orignialStyle = style;
        updatedStyle = orignialStyle.cloneAll();
        recoverButton.setDisable(updatedStyle.getD2sid() >= 0);

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
        filterController.scriptInput.setText(updatedStyle.getMoreConditions());

        isSettingValues = true;
        if (updatedStyle.getFontColor() != null && !updatedStyle.getFontColor().isBlank()) {
            fontColorController.setColor(Color.web(updatedStyle.getFontColor()));
            colorSetRadio.fire();
        }
        if (updatedStyle.getBgColor() != null && !updatedStyle.getBgColor().isBlank()) {
            bgColorController.setColor(Color.web(updatedStyle.getBgColor()));
            bgSetRadio.fire();
        }
        fontSizeSelector.setValue(updatedStyle.getFontSize());
        boldCheck.setSelected(updatedStyle.isBold());
        moreInput.setText(updatedStyle.getMoreStyle());
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
                    updatedStyle.setMoreConditions(filterController.scriptInput.getText());
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
        }
    }

    @FXML
    public void clearMoreSyles() {
        moreInput.clear();
    }

    @FXML
    public void cssGuide() {
        WebBrowserController.oneOpen("https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html", true);
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
