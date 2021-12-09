package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.MatrixDoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;
    protected String target;

    @FXML
    protected ControlData2DSelect selectController;
    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, scRadio, mcRadio;
    @FXML
    protected CheckBox nameCheck;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            selectController.setParameters(tableController);
            getMyStage().setTitle(tableController.getBaseTitle());

            nameCheck.setSelected(UserConfig.getBoolean(baseName + "CopyWithNames", true));
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyWithNames", nameCheck.isSelected());
                }
            });

            initTargetRadios();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTargetRadios() {
        try {
            target = UserConfig.getString(baseName + "DataTarget", "csv");
            if (target == null) {
                csvRadio.fire();
                return;
            }
            switch (target) {
                case "csv":
                    csvRadio.fire();
                    break;
                case "excel":
                    excelRadio.fire();
                    break;
                case "texts":
                    textsRadio.fire();
                    break;
                case "matrix":
                    matrixRadio.fire();
                    break;
                case "sc":
                    scRadio.fire();
                    break;
                case "mc":
                    mcRadio.fire();
                    break;
            }
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkTarget();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkTarget() {
        try {
            if (isSettingValues) {
                return;
            }
            if (csvRadio.isSelected()) {
                target = "csv";
            } else if (excelRadio.isSelected()) {
                target = "excel";
            } else if (textsRadio.isSelected()) {
                target = "texts";
            } else if (matrixRadio.isSelected()) {
                target = "matrix";
            } else if (scRadio.isSelected()) {
                target = "sc";
            } else if (mcRadio.isSelected()) {
                target = "mc";
            }
            UserConfig.setString(baseName + "DataTarget", target);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            selectedRowsIndices = selectController.selectedRowsIndices();
            selectedColumnsIndices = selectController.selectedColumnsIndices();
            if (selectedColumnsIndices.isEmpty() || selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            List<List<String>> data = selectController.selectedData();
            if (nameCheck.isSelected()) {
                data.add(0, selectController.selectedColumnsNames());
            }
            double[][] array = MatrixDoubleTools.toArray(data);
            array = MatrixDoubleTools.transpose(array);
            data = MatrixDoubleTools.toList(array);

            if (csvRadio.isSelected()) {
                DataFileCSVController.open(null, data);

            } else if (excelRadio.isSelected()) {
                DataFileExcelController.open(null, data);

            } else if (textsRadio.isSelected()) {
                DataFileTextController.open(null, data);

            } else if (matrixRadio.isSelected()) {
                MatricesManageController controller = MatricesManageController.oneOpen();
                controller.dataController.loadTmpData(null, data);

            } else if (scRadio.isSelected()) {
                tableController.copyToSystemClipboard(null, data);

            } else if (mcRadio.isSelected()) {
                tableController.copyToMyBoxClipboard(null, data);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DTransposeController open(ControlData2DEditTable tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
