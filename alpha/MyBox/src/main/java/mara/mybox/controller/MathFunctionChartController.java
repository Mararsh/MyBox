package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class MathFunctionChartController extends MathFunctionDataController {

    @FXML
    protected VBox xyzChartBox;
    @FXML
    protected ControlChartXYZ xyzController;

    public MathFunctionChartController() {
        baseTitle = message("MathFunction");
    }

    @Override
    public void setParameters(ControlDataMathFunction editor) {
        try {
            super.setParameters(editor);

            if (variablesSize != 1 && variablesSize != 2) {
                close();
                return;
            }

            xyzController.colorGradientRadio.setSelected(true);
            xyzController.colorColumnsRadio.setDisable(true);

            if (variablesSize == 2) {
                Tab tab = new Tab(message("Chart"));
                tab.setClosable(false);
                dataTabPane.getTabs().add(tab);
                tab.setContent(xyzChartBox);
                refreshStyle(xyzChartBox);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (variablesSize == 1) {
            xyChartAction();
        } else if (variablesSize == 2) {
            xyzChartAction();
        }
    }

    @FXML
    public void xyChartAction() {
        if (!initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<List<String>> rows;
            private List<Data2DColumn> columns;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV dataFile = generateData();
                    dataFile.setTask(this);
                    rows = dataFile.allRows(false);
                    if (rows == null) {
                        return false;
                    }
                    FileDeleteTools.delete(dataFile.getFile());
                    columns = dataFile.getColumns();
                    return columns != null && columns.size() == 2;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                MathFunctionXYChartController.open(columns, rows, title());
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task);
    }

    @FXML
    public void xyzChartAction() {
        if (!initData() || !xyzController.checkParameters()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private File chartFile;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV dataFile = generateData();
                    dataFile.setTask(this);
                    List<List<String>> rows = dataFile.allRows(false);
                    if (rows == null) {
                        return false;
                    }
                    FileDeleteTools.delete(dataFile.getFile());
                    List<Data2DColumn> columns = dataFile.getColumns();
                    if (columns == null || columns.size() != 3) {
                        return false;
                    }

                    chartFile = xyzController.makeChart(columns, rows, 1, title(), dataScale, false, false, false);
                    return chartFile != null && chartFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(chartFile.getParentFile());
                browse(chartFile);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task);
    }

    /*
        static
     */
    public static MathFunctionChartController open(ControlDataMathFunction editorController) {
        try {
            MathFunctionChartController controller = (MathFunctionChartController) WindowTools.operationStage(
                    editorController.nodeEditor, Fxmls.MathFunctionChartFxml);
            controller.setParameters(editorController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
