package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFile;
import mara.mybox.data.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends BaseChildController {
    
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable editController;
    protected Data2D data2D;
    protected List<List<String>> handledData;
    protected List<Data2DColumn> handledColumns;
    protected DataFileCSV handledCSV;
    protected DataFile handledFile;
    
    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected TextField nameInput;
    @FXML
    protected CheckBox importCheck;
    @FXML
    protected Label dataLabel, infoLabel;
    
    public Data2DConvertToDataBaseController() {
        baseTitle = message("ConvertToDatabaseTable");
    }
    
    @Override
    public void setStageStatus() {
        setAsNormal();
    }
    
    public void setParameters(ControlData2DEditTable editController) {
        try {
            this.editController = editController;
            tableData2DDefinition = editController.tableData2DDefinition;
            tableData2DColumn = editController.tableData2DColumn;
            
            columnsController.setParameters(this);
            data2D = columnsController.data2D;
            
            editController.columnChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    columnsController.refreshData();
                    checkOptions();
                }
            });
            
            checkOptions();
            
            importCheck.setSelected(UserConfig.getBoolean(baseName + "ImportData", true));
            importCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ImportData", importCheck.isSelected());
                }
            });
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public boolean checkOptions() {
        getMyStage().setTitle(editController.getTitle());
        nameInput.setText(data2D.getDataName() + (data2D.isTable() ? "_m" : ""));
        
        if (infoLabel != null) {
            infoLabel.setText("");
        }
        
        okButton.setDisable(false);
        return true;
    }
    
    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions()) {
                return;
            }
            
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(ControlData2DEditTable tableController) {
        try {
            Data2DConvertToDataBaseController controller = (Data2DConvertToDataBaseController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DConvertToDatabaseFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
    
}
