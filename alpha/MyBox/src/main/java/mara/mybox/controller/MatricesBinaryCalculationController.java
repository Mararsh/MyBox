package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataMatrix;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-22
 * @License Apache License Version 2.0
 */
public class MatricesBinaryCalculationController extends ControlMatricesList {

    protected DataMatrix dataMatrix2, resultMatrix;
    protected double[][] result;

    @FXML
    protected ControlData2D data2Controller, resultController;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected RadioButton plusRadio, minusRadio, multiplyRadio,
            hadamardProductRadio, kroneckerProductRadio, verticalMergeRadio, horizontalMergeRadio;
    @FXML
    protected Label resultLabel, checkLabel;
    @FXML
    protected Button matrixAButton, matrixBButton, calculateButton;
    @FXML
    protected ImageView leftPaneListControl;
    @FXML
    protected SplitPane listSplitPane;
    @FXML
    protected ScrollPane listPane;

    public MatricesBinaryCalculationController() {
        baseTitle = message("MatricesBinaryCalculation");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            data2Controller.setDataType(this, Data2D.Type.Matrix);
            dataMatrix2 = (DataMatrix) data2Controller.data2D;

            resultController.setDataType(this, Data2D.Type.Matrix);
            resultMatrix = (DataMatrix) resultController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            opGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                        checkMatrices();
                    });
            checkMatrices();

            if (UserConfig.getBoolean("ControlSplitPanesEntered", true)) {
                leftPaneListControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlListPane();
                    }
                });
            }
            leftPaneListControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlListPane();
                }
            });
            leftPaneListControl.setPickOnBounds(UserConfig.getBoolean("ControlSplitPanesSensitive", false));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(plusRadio, new Tooltip(message("MatricesPlusComments")));
            NodeStyleTools.setTooltip(minusRadio, new Tooltip(message("MatricesMinusComments")));
            NodeStyleTools.setTooltip(multiplyRadio, new Tooltip(message("MatricesMultiplyComments")));
            NodeStyleTools.setTooltip(hadamardProductRadio, new Tooltip(message("HadamardProductComments")));
            NodeStyleTools.setTooltip(kroneckerProductRadio, new Tooltip(message("KroneckerProductComments")));
            NodeStyleTools.setTooltip(verticalMergeRadio, new Tooltip(message("VerticalMergeComments")));
            NodeStyleTools.setTooltip(horizontalMergeRadio, new Tooltip(message("HorizontalMergeComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void controlListPane() {
        if (listSplitPane.getItems().contains(listPane)) {
            isSettingValues = true;
            listSplitPane.getItems().remove(listPane);
            isSettingValues = false;
            StyleTools.setIconName(leftPaneListControl, "iconDoubleRight.png");
        } else {
            isSettingValues = true;
            listSplitPane.getItems().add(0, listPane);
            isSettingValues = false;
            StyleTools.setIconName(leftPaneListControl, "iconDoubleLeft.png");
        }
        listSplitPane.applyCss();
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        loadTableData();
        createAction();

        dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                checkMatrices();
            }
        });

        data2Controller.statusNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                checkMatrices();
            }
        });

    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        deleteButton.setDisable(none);
        matrixAButton.setDisable(none);
        matrixBButton.setDisable(none);
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("SetAsMatrixA"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                matrixAAction();
            });
            items.add(menu);

            menu = new MenuItem(message("SetAsMatrixB"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                matrixBAction();
            });
            items.add(menu);

            List<MenuItem> superItems = super.makeTableContextMenu();
            if (!superItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(superItems);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemDoubleClicked() {
    }

    @Override
    public void itemClicked() {

    }

    @FXML
    public void matrixAAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            dataController.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void matrixBAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            data2Controller.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkMatrices() {
        checkLabel.setText("");
        if (plusRadio.isSelected() || minusRadio.isSelected() || hadamardProductRadio.isSelected()) {
            if (dataMatrix.getColsNumber() != dataMatrix2.getColsNumber()
                    || dataMatrix.getRowsNumber() != dataMatrix2.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSame"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (multiplyRadio.isSelected()) {
            if (dataMatrix.getColsNumber() != dataMatrix2.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateMultiply"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (verticalMergeRadio.isSelected()) {
            if (dataMatrix.getColsNumber() != dataMatrix2.getColsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameCols"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (horizontalMergeRadio.isSelected()) {
            if (dataMatrix.getRowsNumber() != dataMatrix2.getRowsNumber()) {
                checkLabel.setText(message("MatricesCannotCalculateShouldSameRows"));
                calculateButton.setDisable(true);
                return false;
            }

        }
        calculateButton.setDisable(false);
        return true;
    }

    @FXML
    public void calculateAction() {
        if (!checkMatrices()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            resultLabel.setText("");
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        if (plusRadio.isSelected()) {
                            result = MatrixDoubleTools.add(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (minusRadio.isSelected()) {
                            result = MatrixDoubleTools.subtract(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (multiplyRadio.isSelected()) {
                            result = MatrixDoubleTools.multiply(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (hadamardProductRadio.isSelected()) {
                            result = MatrixDoubleTools.hadamardProduct(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (kroneckerProductRadio.isSelected()) {
                            result = MatrixDoubleTools.kroneckerProduct(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (verticalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.vertivalMerge(dataMatrix.toArray(), dataMatrix2.toArray());

                        } else if (horizontalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.horizontalMerge(dataMatrix.toArray(), dataMatrix2.toArray());

                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return result != null;
                }

                @Override
                protected void whenSucceeded() {
                    cost = new Date().getTime() - startTime.getTime();
                    String op = ((RadioButton) opGroup.getSelectedToggle()).getText();
                    resultLabel.setText(op + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                    resultController.loadMatrix(result, false);
//                    if (resultController.autoNameCheck.isSelected()) {
//                        resultController.nameInput.setText(op);
//                    }
                }

            };
            start(task);
        }
    }

}
