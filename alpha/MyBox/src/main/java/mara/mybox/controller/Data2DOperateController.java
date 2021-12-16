package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-12-16
 * @License Apache License Version 2.0
 */
public class Data2DOperateController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected List<String> selectedNames, handledNames;
    protected List<List<String>> handledData;
    protected List<Data2DColumn> selectedColumns, handledColumns;
    protected String value;

    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected ControlData2DResult resultController;
    @FXML
    protected VBox mainBox;
    @FXML
    protected TitledPane sourcePane, resultPane;
    @FXML
    protected Label dataNameLabel;
    @FXML
    protected Accordion accordionPane;
    @FXML
    protected TitledPane copyPane, setValuePane, statisticPane, percentagePane, transposePane, sortPane;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            accordionPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
                @Override
                public void changed(ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n) {
                    checkPaneStatus();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkPaneStatus() {
        try {
            TitledPane currentPane = accordionPane.getExpandedPane();
            if (tableController == null || currentPane == null) {
                return;
            }

            if (currentPane.equals(copyPane)) {

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            data2D = tableController.data2D;

            sourceController.setParameters(this);
            resultController.setParameters(this);

            getMyStage().setTitle(tableController.getBaseTitle());

            handle(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void handle(String operation) {
        if (operation == null) {
            hideResult(true);
            return;
        }
        switch (operation) {
            case "copy":
                hideResult(true);
                break;
            case "SetValues":
                hideResult(true);
                break;
            case "Statistic":
                hideResult(false);
                break;
            default:
                hideResult(true);
                break;
        }
    }

    public void hideResult(boolean hide) {
        if (hide) {
            if (mainBox.getChildren().contains(resultPane)) {
                mainBox.getChildren().remove(resultPane);
            }
        } else {
            if (!mainBox.getChildren().contains(resultPane)) {
                mainBox.getChildren().add(resultPane);
            }
        }

    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    /*
        static
     */
    public static Data2DOperateController oneOpen(ControlData2DEditTable tableController) {
        Data2DOperateController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DOperateController) {
                try {
                    controller = (Data2DOperateController) object;
                    if (controller.tableController.data2D.equals(tableController.data2D)) {
                        controller.toFront();
                    } else {
                        controller = null;
                    }
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DOperateController) WindowTools.openChildStage(tableController.getOwner(), Fxmls.Data2DOperateFxml, false);
        }
        return controller;
    }

    public static Data2DOperateController open(ControlData2DEditTable tableController, String operation) {
        Data2DOperateController controller = oneOpen(tableController);
        if (controller.data2D == null) {
            controller.setParameters(tableController);
        }
        controller.handle(operation);
        return controller;
    }

}
