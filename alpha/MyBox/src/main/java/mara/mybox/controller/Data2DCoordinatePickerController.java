package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class Data2DCoordinatePickerController extends CoordinatePickerController {

    protected BaseData2DTableController dataTable;
    protected int rowIndex;
    protected ControlData2DRowEdit editControl;

    @FXML
    protected ComboBox<String> longitudeSelector, latitudeSelector;

    public void setParameters(BaseData2DTableController dataControl, int rowIndex) {
        try {
            this.dataTable = dataControl;
            this.rowIndex = rowIndex;

            List<String> loNames = new ArrayList<>();
            List<String> laNames = new ArrayList<>();
            int loIndex = -1, laIndex = -1;
            for (int i = 0; i < dataControl.data2D.getColumns().size(); i++) {
                Data2DColumn column = dataControl.data2D.getColumns().get(i);
                ColumnType type = column.getType();
                String name = column.getColumnName();
                if (type == ColumnType.Longitude) {
                    loNames.add(name);
                    if (loIndex < 0) {
                        loIndex = i;
                    }
                } else if (type == ColumnType.Latitude) {
                    laNames.add(name);
                    if (laIndex < 0) {
                        laIndex = i;
                    }
                }
            }
            if (loNames.isEmpty() || laNames.isEmpty()) {
                close();
                return;
            }
            longitudeSelector.getItems().addAll(loNames);
            longitudeSelector.getSelectionModel().select(0);
            latitudeSelector.getItems().addAll(laNames);
            latitudeSelector.getSelectionModel().select(0);

            try {
                List<String> row = dataControl.getTableData().get(rowIndex);
                double lo = Double.parseDouble(row.get(loIndex + 1));
                double la = Double.parseDouble(row.get(laIndex + 1));
                loadCoordinate(lo, la);
            } catch (Exception ex) {
//                MyBoxLog.console(ex);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DRowEdit editControl) {
        try {
            this.editControl = editControl;

            List<String> loNames = new ArrayList<>();
            List<String> laNames = new ArrayList<>();

            TextField loInput = null, laInput = null;
            for (Data2DColumn column : editControl.inputs.keySet()) {
                ColumnType type = column.getType();
                String name = column.getColumnName();
                if (type == ColumnType.Longitude) {
                    loNames.add(name);
                    if (loInput == null) {
                        loInput = (TextField) editControl.inputs.get(column);
                    }
                } else if (type == ColumnType.Latitude) {
                    laNames.add(name);
                    if (laInput == null) {
                        laInput = (TextField) editControl.inputs.get(column);
                    }
                }
            }

            if (loInput == null || laInput == null) {
                close();
                return;
            }
            longitudeSelector.getItems().addAll(loNames);
            longitudeSelector.getSelectionModel().select(0);
            latitudeSelector.getItems().addAll(laNames);
            latitudeSelector.getSelectionModel().select(0);

            try {
                double lo = Double.parseDouble(loInput.getText());
                double la = Double.parseDouble(laInput.getText());
                loadCoordinate(lo, la);
            } catch (Exception ex) {
//                MyBoxLog.console(ex);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            geographyCode = mapController.geographyCode;
            if (geographyCode == null) {
                popError(message("NoData"));
                return;
            }
            if (dataTable != null) {
                int loIndex = dataTable.data2D.colOrder(longitudeSelector.getValue());
                int laIndex = dataTable.data2D.colOrder(latitudeSelector.getValue());
                List<String> row = dataTable.getTableData().get(rowIndex);
                row.set(loIndex + 1, geographyCode.getLongitude() + "");
                row.set(laIndex + 1, geographyCode.getLatitude() + "");
                dataTable.getTableData().set(rowIndex, row);

            } else if (editControl != null) {
                for (Data2DColumn column : editControl.inputs.keySet()) {
                    String name = column.getColumnName();
                    if (name.equals(longitudeSelector.getValue())) {
                        ((TextField) editControl.inputs.get(column)).setText(geographyCode.getLongitude() + "");

                    } else if (name.equals(latitudeSelector.getValue())) {
                        ((TextField) editControl.inputs.get(column)).setText(geographyCode.getLatitude() + "");
                    }
                }
            }
            close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DCoordinatePickerController open(BaseData2DTableController dataControl, int rowIndex) {
        try {
            Data2DCoordinatePickerController controller
                    = (Data2DCoordinatePickerController) WindowTools.childStage(
                            dataControl, Fxmls.Data2DCoordinatePickerFxml);
            controller.setParameters(dataControl, rowIndex);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DCoordinatePickerController open(ControlData2DRowEdit editControl) {
        try {
            Data2DCoordinatePickerController controller
                    = (Data2DCoordinatePickerController) WindowTools.childStage(
                            editControl, Fxmls.Data2DCoordinatePickerFxml);
            controller.setParameters(editControl);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
