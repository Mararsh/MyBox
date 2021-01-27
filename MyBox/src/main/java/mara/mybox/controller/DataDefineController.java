package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.Matrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-23
 * @License Apache License Version 2.0
 */
public class DataDefineController extends BaseDataTableController<Matrix> {

    protected double[][] result;

    @FXML
    protected BaseMatrixController edit2Controller, resultTableController;
    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected RadioButton plusRadio, minusRadio, multiplyRadio,
            hadamardProductRadio, kroneckerProductRadio, verticalMergeRadio, horizontalMergeRadio;
    @FXML
    protected Label resultLabel;
    @FXML
    protected Button matrixAButton, matrixBButton, calculateButton;
    @FXML
    protected ImageView leftPaneListControl;
    @FXML
    protected SplitPane listSplitPane;
    @FXML
    protected ScrollPane listPane;

    public DataDefineController() {
        baseTitle = message("MatricesBinaryCalculation");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (getUserConfigBoolean("ControlSplitPanesEntered", true)) {
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
            leftPaneListControl.setPickOnBounds(getUserConfigBoolean("ControlSplitPanesSensitive", false));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void controlListPane() {
        if (listSplitPane.getItems().contains(listPane)) {
            isSettingValues = true;
            listSplitPane.getItems().remove(listPane);
            isSettingValues = false;
            ControlStyle.setIconName(leftPaneListControl, "iconDoubleRight.png");
        } else {
            isSettingValues = true;
            listSplitPane.getItems().add(0, listPane);
            isSettingValues = false;
            ControlStyle.setIconName(leftPaneListControl, "iconDoubleLeft.png");
        }
        listSplitPane.applyCss();
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        loadTableData();

    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        deleteButton.setDisable(selection == 0);
        matrixAButton.setDisable(selection == 0);
        matrixBButton.setDisable(selection == 0);
        selectedLabel.setText(message("Selected") + ": " + selection);
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

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
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
    }

}
