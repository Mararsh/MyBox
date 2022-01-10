package mara.mybox.controller;

import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends Data2DHandleController {

    protected String value;

    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, randomNnRadio,
            setRadio, gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected FlowPane matrixPane;

    @Override
    public void initControls() {
        super.initControls();
        initValueRadios();
    }

    public void initValueRadios() {
        try {
            value = UserConfig.getString(baseName + "Value", "0");
            switch (value) {
                case "0":
                    zeroRadio.fire();
                    break;
                case "1":
                    oneRadio.fire();
                    break;
                case "blank":
                    blankRadio.fire();
                    break;
                case "MyBox##random":
                    randomRadio.fire();
                    break;
                case "MyBox##randomNn":
                    randomNnRadio.fire();
                    break;
                case "MyBox##gaussianDistribution":
                    gaussianDistributionRadio.fire();
                    break;
                case "MyBox##identify":
                    identifyRadio.fire();
                    break;
                case "MyBox##upperTriangle":
                    upperTriangleRadio.fire();
                    break;
                case "MyBox##lowerTriangle":
                    lowerTriangleRadio.fire();
                    break;
                default:
                    valueInput.setText(value);
                    setRadio.fire();
            }
            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkValue();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkValue() {
        try {
            if (isSettingValues) {
                return;
            }
            valueInput.setStyle(null);
            if (setRadio.isSelected()) {
                value = valueInput.getText();
            } else if (zeroRadio.isSelected()) {
                value = "0";
            } else if (oneRadio.isSelected()) {
                value = "1";
            } else if (blankRadio.isSelected()) {
                value = " ";
            } else if (randomRadio.isSelected()) {
                value = "MyBox##random";
            } else if (randomNnRadio.isSelected()) {
                value = "MyBox##randomNn";
            } else if (gaussianDistributionRadio.isSelected()) {
                value = "MyBox##gaussianDistribution";
            } else if (identifyRadio.isSelected()) {
                value = "MyBox##identify";
            } else if (upperTriangleRadio.isSelected()) {
                value = "MyBox##upperTriangle";
            } else if (lowerTriangleRadio.isSelected()) {
                value = "MyBox##lowerTriangle";
            }
            UserConfig.setString(baseName + "Value", value);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        infoLabel.setText("");
        if (allPages()) {
            matrixPane.setDisable(true);
            if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                    || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                zeroRadio.fire();
                return false;
            }
        } else {
            boolean isSquare = tableController.isSquare(allRowsRadio.isSelected());
            boolean canGD = isSquare && tableController.checkedColsIndices.size() % 2 != 0;
            gaussianDistributionRadio.setDisable(!canGD);
            identifyRadio.setDisable(!isSquare);
            upperTriangleRadio.setDisable(!isSquare);
            lowerTriangleRadio.setDisable(!isSquare);
            if (!isSquare) {
                if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                        || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                    zeroRadio.fire();
                    return false;
                }
            }
            if (!canGD) {
                if (gaussianDistributionRadio.isSelected()) {
                    zeroRadio.fire();
                    return false;
                }
            }
            matrixPane.setDisable(false);
        }
        checkValue();
        if (value == null) {
            okButton.setDisable(true);
            return false;
        } else {
            okButton.setDisable(false);
            return ok;
        }
    }

    @Override
    public void handleFileTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    return data2D.setValue(tableController.checkedColsIndices, value);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                tableController.dataController.goPage();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    @Override
    public void handleRowsTask() {
        try {
            tableController.isSettingValues = true;
            boolean ok;
            if (gaussianDistributionRadio.isSelected()) {
                ok = gaussianDistribution();
            } else if (identifyRadio.isSelected()) {
                ok = identifyMatrix();
            } else if (upperTriangleRadio.isSelected()) {
                ok = upperTriangleMatrix();
            } else if (lowerTriangleRadio.isSelected()) {
                ok = lowerTriangleMatrix();
            } else {
                ok = setValue();
            }
            tableController.isSettingValues = false;
            if (ok) {
                tableController.restoreSelections();
                tableController.tableView.refresh();
            }
            tableController.tableChanged(true);
            popDone();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public boolean setValue() {
        try {
            Random random = new Random();
            for (int row : tableController.checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                for (int col : tableController.checkedColsIndices) {
                    String v = value;
                    if (randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, false);
                    } else if (randomNnRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, true);
                    }
                    values.set(col + 1, v);
                }
                tableController.tableData.set(row, values);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean gaussianDistribution() {
        try {
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) tableController.checkedRowsIndices.size() / 2);
            int scale = tableController.data2D.getScale();
            int rowIndex = 0, colIndex;
            for (int row : tableController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : tableController.checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, tableRow);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean identifyMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : tableController.checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : tableController.checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean upperTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : tableController.checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : tableController.checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean lowerTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : tableController.checkedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : tableController.checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
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
    public static Data2DSetValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetValuesFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
