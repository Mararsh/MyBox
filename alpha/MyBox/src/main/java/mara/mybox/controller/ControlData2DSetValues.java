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
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DSetValues extends BaseController {

    protected Data2DOperateController operateController;
    protected ControlData2DLoad loadController;
    protected ControlData2DSource sourceController;
    protected Data2D data2D;
    protected String value;

    @FXML
    protected ToggleGroup valueGroup, nObjectGroup, nAlgorithmGroup;
    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, setRadio,
            gaussianDistributionRadio, identifyRadio, upperTriangleRadio, lowerTriangleRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected VBox squareMatrixBox;

    public void setParameters(Data2DOperateController operateController) {
        try {
            initValueRadios();

            this.operateController = operateController;
            data2D = operateController.data2D;
            sourceController = operateController.sourceController;
            loadController = operateController.loadController;

            sourceController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            checkOptions();

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
                case "MyBox##random":
                    randomRadio.fire();
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

    public boolean checkOptions() {
        if (!sourceController.checkSource()) {
            okButton.setDisable(true);
            return false;
        }
        if (!sourceController.isSquare()
                || (sourceController.isAllData() && data2D.isMutiplePages())) {
            squareMatrixBox.setDisable(true);
            if (gaussianDistributionRadio.isSelected() || identifyRadio.isSelected()
                    || upperTriangleRadio.isSelected() || lowerTriangleRadio.isSelected()) {
                zeroRadio.fire();
                return false;
            }
        } else {
            squareMatrixBox.setDisable(false);
        }
        checkValue();
        if (value == null) {
            okButton.setDisable(true);
            return false;
        } else {
            okButton.setDisable(false);
            return true;
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions()) {
                return;
            }
            if (sourceController.isAllData() && data2D.isMutiplePages()) {
                data2D.setValue(sourceController.checkedColsIndices, value);
                loadController.dataController.goPage();
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
            MyBoxLog.debug(e);
        }
    }

    public boolean setValue() {
        try {
            Random random = new Random();
            loadController.isSettingValues = true;
            for (int row : sourceController.checkedRowsIndices) {
                List<String> values = loadController.tableData.get(row);
                for (int col : sourceController.checkedColsIndices) {
                    String v = value;
                    if (randomRadio.isSelected()) {
                        v = loadController.data2D.random(random, col);
                    }
                    values.set(col + 1, v);
                }
                loadController.tableData.set(row, values);
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);
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
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) sourceController.checkedRowsIndices.size() / 2);
            loadController.isSettingValues = true;
            int scale = loadController.data2D.getScale();
            int rowIndex = 0, colIndex;
            for (int row : sourceController.checkedRowsIndices) {
                List<String> tableRow = loadController.tableData.get(row);
                colIndex = 0;
                for (int col : sourceController.checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                loadController.tableData.set(row, tableRow);
                rowIndex++;
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);
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
            loadController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : sourceController.checkedRowsIndices) {
                List<String> values = loadController.tableData.get(row);
                colIndex = 0;
                for (int col : sourceController.checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                loadController.tableData.set(row, values);
                rowIndex++;
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);
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
            loadController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : sourceController.checkedRowsIndices) {
                List<String> values = loadController.tableData.get(row);
                colIndex = 0;
                for (int col : sourceController.checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                loadController.tableData.set(row, values);
                rowIndex++;
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);
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
            loadController.isSettingValues = true;
            int rowIndex = 0, colIndex;
            for (int row : sourceController.checkedRowsIndices) {
                List<String> values = loadController.tableData.get(row);
                colIndex = 0;
                for (int col : sourceController.checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                loadController.tableData.set(row, values);
                rowIndex++;
            }
            loadController.tableView.refresh();
            loadController.isSettingValues = false;
            loadController.tableChanged(true);
            popDone();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
            return false;
        }
    }

}
