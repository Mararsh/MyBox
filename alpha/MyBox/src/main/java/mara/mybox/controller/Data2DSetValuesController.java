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
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;
    protected String value;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, setRadio,
            normalizationRadio, gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected FlowPane matrixPane;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            selectController.setParameters(tableController, false, false);
            getMyStage().setTitle(tableController.getBaseTitle());

            initValueRadios();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
                case AppValues.MyBoxRandomFlag:
                    randomRadio.fire();
                    break;
                case "MyBox##normalization":
                    normalizationRadio.fire();
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
                value = AppValues.MyBoxRandomFlag;
            } else if (normalizationRadio.isSelected()) {
                value = "MyBox##normalization";
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

    @FXML
    @Override
    public void okAction() {
        try {
            checkValue();
            if (value == null) {
                popError(message("InvalidParameter"));
                return;
            }
            selectedRowsIndices = selectController.checkedRowsIndices();
            selectedColumnsIndices = selectController.checkedColsIndices();
            if (selectedColumnsIndices.isEmpty() || selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }

            if (normalizationRadio.isSelected()) {
                normalization();
            } else if (gaussianDistributionRadio.isSelected()) {
                gaussianDistribution();
            } else if (identifyRadio.isSelected()) {
                identifyMatrix();
            } else if (upperTriangleRadio.isSelected()) {
                upperTriangleMatrix();
            } else if (lowerTriangleRadio.isSelected()) {
                lowerTriangleMatrix();
            } else {
                setValue();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean setValue() {
        try {
            Random random = new Random();
            tableController.isSettingValues = true;
            for (int row : selectedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                for (int col : selectedColumnsIndices) {
                    String v = value;
                    if (randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col);
                    }
                    values.set(col + 1, v);
                }
                tableController.tableData.set(row, values);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean normalization() {
        try {
            double sum = 0d;
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                for (int col : selectedColumnsIndices) {
                    try {
                        sum += Double.valueOf(tableRow.get(col + 1));
                    } catch (Exception e) {
                    }
                }
            }
            if (sum == 0) {
                popError(message("SumIsZero"));
                return false;
            }
            tableController.isSettingValues = true;
            int scale = tableController.data2D.getScale();
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                for (int col : selectedColumnsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(Double.valueOf(tableRow.get(col + 1)) / sum, scale));
                    } catch (Exception e) {
                    }
                }
                tableController.tableData.set(row, tableRow);
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean gaussianDistribution() {
        try {
            if (selectedRowsIndices.size() != selectedColumnsIndices.size()) {
                popError(message("OnlyHanldeSquareMatrix"));
                return false;
            }
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) selectedRowsIndices.size() / 2);
            tableController.isSettingValues = true;
            int scale = tableController.data2D.getScale();
            int rowIndex = 0, colIndex;
            for (int row : selectedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : selectedColumnsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, tableRow);
                rowIndex++;
            }
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean identifyMatrix() {
        try {
            if (selectedRowsIndices.size() != selectedColumnsIndices.size()) {
                popError(message("OnlyHanldeSquareMatrix"));
                return false;
            }
            tableController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : selectedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : selectedColumnsIndices) {
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
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean upperTriangleMatrix() {
        try {
            if (selectedRowsIndices.size() != selectedColumnsIndices.size()) {
                popError(message("OnlyHanldeSquareMatrix"));
                return false;
            }
            tableController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : selectedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : selectedColumnsIndices) {
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
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

    public boolean lowerTriangleMatrix() {
        try {
            if (selectedRowsIndices.size() != selectedColumnsIndices.size()) {
                popError(message("OnlyHanldeSquareMatrix"));
                return false;
            }
            tableController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : selectedRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : selectedColumnsIndices) {
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
            tableController.tableView.refresh();
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            popDone();
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
