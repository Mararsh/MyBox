package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-28
 * @License Apache License Version 2.0
 */
public class Data2DManageQueryController extends BaseChildController {

    protected ControlData2DList listController;

    @FXML
    protected ToggleGroup orderGroup;
    @FXML
    protected CheckBox csvCheck, excelCheck, textsCheck, matrixCheck, databaseCheck,
            myBoxClipboardCheck, descCheck;
    @FXML
    protected RadioButton idRadio, nameRadio, rowsRadio, colsRadio, timeRadio, fileRadio;

    public Data2DManageQueryController() {
        baseTitle = message("ManageData");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvCheck.setSelected(UserConfig.getBoolean(baseName + "CSV", true));
            csvCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CSV", csvCheck.isSelected());
                }
            });

            textsCheck.setSelected(UserConfig.getBoolean(baseName + "Text", true));
            textsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Text", textsCheck.isSelected());
                }
            });

            excelCheck.setSelected(UserConfig.getBoolean(baseName + "Xlsx", true));
            excelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Xlsx", excelCheck.isSelected());
                }
            });

            matrixCheck.setSelected(UserConfig.getBoolean(baseName + "Matrix", true));
            matrixCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Matrix", matrixCheck.isSelected());
                }
            });

            databaseCheck.setSelected(UserConfig.getBoolean(baseName + "Database", true));
            databaseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Database", databaseCheck.isSelected());
                }
            });

            myBoxClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "DataClipboard", true));
            myBoxClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "DataClipboard", myBoxClipboardCheck.isSelected());
                }
            });

            descCheck.setSelected(UserConfig.getBoolean(baseName + "Desc", false));
            descCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Desc", descCheck.isSelected());
                }
            });

            String order = UserConfig.getString(baseName + "Order", null);
            if (message("ID").equals(order)) {
                idRadio.setSelected(true);
            } else if (message("Name").equals(order)) {
                nameRadio.setSelected(true);
            } else if (message("RowsNumber").equals(order)) {
                rowsRadio.setSelected(true);
            } else if (message("ColumnsNumber").equals(order)) {
                colsRadio.setSelected(true);
            } else if (message("File").equals(order)) {
                fileRadio.setSelected(true);
            } else {
                timeRadio.setSelected(true);
            }
            orderGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setString(baseName + "Order", ((RadioButton) newValue).getText());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DList manageController) {
        this.listController = manageController;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            String condition = "";
            if (textsCheck.isSelected()) {
                condition += " data_type=0 ";
            }
            if (csvCheck.isSelected()) {
                condition += (condition.isEmpty() ? "" : " OR ") + " data_type=1 ";
            }
            if (excelCheck.isSelected()) {
                condition += (condition.isEmpty() ? "" : " OR ") + " data_type=2 ";
            }
            if (myBoxClipboardCheck.isSelected()) {
                condition += (condition.isEmpty() ? "" : " OR ") + " data_type=3 ";
            }
            if (matrixCheck.isSelected()) {
                condition += (condition.isEmpty() ? "" : " OR ") + " data_type=4 ";
            }
            if (databaseCheck.isSelected()) {
                condition += (condition.isEmpty() ? "" : " OR ") + " data_type=5 ";
            }
            condition += " AND data_type != " + Data2D.type(Data2DDefinition.Type.InternalTable);
            String orderColumns = null;
            if (idRadio.isSelected()) {
                orderColumns = " d2did ";
            } else if (nameRadio.isSelected()) {
                orderColumns = " data_name ";
            } else if (rowsRadio.isSelected()) {
                orderColumns = " rows_number ";
            } else if (colsRadio.isSelected()) {
                orderColumns = " columns_number ";
            } else if (timeRadio.isSelected()) {
                orderColumns = " modify_time ";
            } else if (fileRadio.isSelected()) {
                orderColumns = " file ";
            }
            if (orderColumns != null && descCheck.isSelected()) {
                orderColumns += " DESC ";
            }
            listController.queryConditions = condition;
            listController.orderColumns = orderColumns;
            listController.refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DManageQueryController open(ControlData2DList manageController) {
        try {
            Data2DManageQueryController controller = (Data2DManageQueryController) WindowTools.branchStage(
                    manageController, Fxmls.Data2DManageQueryFxml);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
