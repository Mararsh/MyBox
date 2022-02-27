package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-19
 * @License Apache License Version 2.0
 *
 */
public class Data2DColumnCreateController extends BaseChildController {

    protected ControlData2DColumns columnsController;

    @FXML
    protected TextField nameInput, lengthInput, widthInput;
    @FXML
    protected RadioButton stringRadio, doubleRadio, floatRadio, longRadio, intRadio, shortRadio, booleanRadio, dateRadio;
    @FXML
    protected CheckBox notNullCheck, editableCheck;
    @FXML
    protected ColorSet colorController;

    public Data2DColumnCreateController() {
        TipsLabelKey = message("SqlIdentifierComments");
    }

    protected void setParameters(ControlData2DColumns columnsController) {
        try {
            this.columnsController = columnsController;

            colorController.init(this, baseName + "Color");
            colorController.setColor(FxColorTools.randomColor());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return;
            }
            int length;
            try {
                length = Integer.parseInt(lengthInput.getText());
                if (length < 0 || length > BaseTable.StringMaxLength) {
                    length = BaseTable.StringMaxLength;
                }
            } catch (Exception ee) {
                popError(message("InvalidParameter") + ": " + message("Length"));
                return;
            }
            int width;
            try {
                width = Integer.parseInt(widthInput.getText());
            } catch (Exception ee) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return;
            }
            Data2DColumn column = new Data2DColumn();
            column.setColumnName(name).setLength(length).setWidth(width)
                    .setNotNull(notNullCheck.isSelected())
                    .setEditable(editableCheck.isSelected())
                    .setColor((Color) colorController.rect.getFill());
            if (stringRadio.isSelected()) {
                column.setType(ColumnType.String);
            } else if (doubleRadio.isSelected()) {
                column.setType(ColumnType.Double);
            } else if (floatRadio.isSelected()) {
                column.setType(ColumnType.Float);
            } else if (longRadio.isSelected()) {
                column.setType(ColumnType.Long);
            } else if (intRadio.isSelected()) {
                column.setType(ColumnType.Integer);
            } else if (shortRadio.isSelected()) {
                column.setType(ColumnType.Short);
            } else if (booleanRadio.isSelected()) {
                column.setType(ColumnType.Boolean);
            } else if (dateRadio.isSelected()) {
                column.setType(ColumnType.Datetime);
            }

            columnsController.addRow(column);
            popSuccessful();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DColumnCreateController open(ControlData2DColumns columnsController) {
        try {
            Data2DColumnCreateController controller = (Data2DColumnCreateController) WindowTools.openChildStage(
                    columnsController.getMyWindow(), Fxmls.Data2DColumnCreateFxml, false);
            controller.setParameters(columnsController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
