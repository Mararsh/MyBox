package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import mara.mybox.db.data.Matrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-22
 * @License Apache License Version 2.0
 */
public class MatricesBinaryCalculationController extends ControlMatricesList {

    protected double[][] result;

    @FXML
    protected ControlMatrix edit2Controller, resultTableController;
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
        baseTitle = Languages.message("MatricesBinaryCalculation");
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
            NodeStyleTools.setTooltip(plusRadio, new Tooltip(Languages.message("MatricesPlusComments")));
            NodeStyleTools.setTooltip(minusRadio, new Tooltip(Languages.message("MatricesMinusComments")));
            NodeStyleTools.setTooltip(multiplyRadio, new Tooltip(Languages.message("MatricesMultiplyComments")));
            NodeStyleTools.setTooltip(hadamardProductRadio, new Tooltip(Languages.message("HadamardProductComments")));
            NodeStyleTools.setTooltip(kroneckerProductRadio, new Tooltip(Languages.message("KroneckerProductComments")));
            NodeStyleTools.setTooltip(verticalMergeRadio, new Tooltip(Languages.message("VerticalMergeComments")));
            NodeStyleTools.setTooltip(horizontalMergeRadio, new Tooltip(Languages.message("HorizontalMergeComments")));
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
        editController.initManager(this);
        edit2Controller.initManager(this);
        resultTableController.initManager(this);
        editController.sheetChangedNotify.addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    checkMatrices();
                });
        edit2Controller.sheetChangedNotify.addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    checkMatrices();
                });
    }

    @Override
    protected int checkSelected() {
        if (isSettingValues) {
            return -1;
        }
        int selection = super.checkSelected();
        deleteButton.setDisable(selection == 0);
        matrixAButton.setDisable(selection == 0);
        matrixBButton.setDisable(selection == 0);
        selectedLabel.setText(Languages.message("Selected") + ": " + selection);
        return selection;
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(Languages.message("SetAsMatrixA"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                matrixAAction();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("SetAsMatrixB"));
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
            Matrix selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            editController.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void matrixBAction() {
        try {
            Matrix selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            edit2Controller.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkMatrices() {
        checkLabel.setText("");
        if (plusRadio.isSelected() || minusRadio.isSelected() || hadamardProductRadio.isSelected()) {
            if (editController.colsNumber != edit2Controller.colsNumber
                    || editController.rowsNumber != edit2Controller.rowsNumber) {
                checkLabel.setText(Languages.message("MatricesCannotCalculateShouldSame"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (multiplyRadio.isSelected()) {
            if (editController.colsNumber != edit2Controller.rowsNumber) {
                checkLabel.setText(Languages.message("MatricesCannotCalculateMultiply"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (verticalMergeRadio.isSelected()) {
            if (editController.colsNumber != edit2Controller.colsNumber) {
                checkLabel.setText(Languages.message("MatricesCannotCalculateShouldSameCols"));
                calculateButton.setDisable(true);
                return false;
            }

        } else if (horizontalMergeRadio.isSelected()) {
            if (editController.rowsNumber != edit2Controller.rowsNumber) {
                checkLabel.setText(Languages.message("MatricesCannotCalculateShouldSameRows"));
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
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        if (plusRadio.isSelected()) {
                            result = MatrixDoubleTools.add(editController.matrix(), edit2Controller.matrix());

                        } else if (minusRadio.isSelected()) {
                            result = MatrixDoubleTools.subtract(editController.matrix(), edit2Controller.matrix());

                        } else if (multiplyRadio.isSelected()) {
                            result = MatrixDoubleTools.multiply(editController.matrix(), edit2Controller.matrix());

                        } else if (hadamardProductRadio.isSelected()) {
                            result = MatrixDoubleTools.hadamardProduct(editController.matrix(), edit2Controller.matrix());

                        } else if (kroneckerProductRadio.isSelected()) {
                            result = MatrixDoubleTools.kroneckerProduct(editController.matrix(), edit2Controller.matrix());

                        } else if (verticalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.vertivalMerge(editController.matrix(), edit2Controller.matrix());

                        } else if (horizontalMergeRadio.isSelected()) {
                            result = MatrixDoubleTools.horizontalMerge(editController.matrix(), edit2Controller.matrix());

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
                    resultLabel.setText(op + "  " + Languages.message("Cost") + ":" + DateTools.datetimeMsDuration(cost));
                    resultTableController.idInput.clear();
                    resultTableController.loadMatrix(result);
                    if (resultTableController.autoNameCheck.isSelected()) {
                        resultTableController.nameInput.setText(op);
                    }
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
