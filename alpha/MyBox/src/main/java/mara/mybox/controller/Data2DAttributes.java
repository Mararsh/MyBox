package mara.mybox.controller;

import java.util.Arrays;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class Data2DAttributes extends BaseChildController {

    protected BaseData2DLoadController dataController;
    protected TableData2DDefinition tableData2DDefinition;
    protected Data2D data2D;
    protected String dataName;
    protected short scale;
    protected int maxRandom;

    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected TextArea descInput, infoArea;
    @FXML
    protected TextField idInput, timeInput, dataTypeInput, dataNameInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;

    public Data2DAttributes() {
        baseTitle = message("DataDefinition");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setParameters(BaseData2DLoadController controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            dataController = controller;
            tableData2DDefinition = dataController.tableData2DDefinition;

            loadValues();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    loadValues();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadValues() {
        try {
            if (dataController == null || !dataController.isShowing()) {
                close();
                return;
            }
            data2D = dataController.data2D;
            if (data2D == null) {
                close();
                return;
            }

            columnsController.setParameters(dataController);

            idInput.setText(data2D.getD2did() >= 0 ? data2D.getD2did() + "" : message("NewData"));
            timeInput.setText(DateTools.datetimeToString(data2D.getModifyTime()));
            dataTypeInput.setText(message(data2D.getType().name()));
            isSettingValues = true;
            dataNameInput.setText(data2D.getDataName());
            scaleSelector.setValue(data2D.getScale() + "");
            randomSelector.setValue(data2D.getMaxRandom() + "");
            descInput.setText(data2D.getComments());
            infoArea.setText(data2D.info());
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        String name = dataNameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("DataName"));
            return false;
        }
        dataName = name;

        int v = -1;
        try {
            v = Integer.parseInt(scaleSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0 && v <= 15) {
            scale = (short) v;
        } else {
            popError(message("InvalidParameter") + ": " + message("DecimalScale"));
            return false;
        }

        v = -1;
        try {
            v = Integer.parseInt(randomSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            maxRandom = v;
        } else {
            popError(message("InvalidParameter") + ": " + message("MaxRandom"));
            return false;
        }

        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (!pickValues()) {
            return;
        }
        data2D.setDataName(dataName);
        data2D.setScale(scale);
        data2D.setMaxRandom(maxRandom);
        data2D.setComments(descInput.getText());
        data2D.setModifyTime(new Date());
        columnsController.okAction();
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    /*
        static
     */
    public static Data2DAttributes open(BaseData2DLoadController tableController) {
        try {
            Data2DAttributes controller = (Data2DAttributes) WindowTools.branchStage(
                    tableController, Fxmls.Data2DAttributesFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
