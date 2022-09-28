package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-19
 * @License Apache License Version 2.0
 *
 */
public class ControlData2DColumnEdit extends BaseChildController {

    protected ControlData2DColumns columnsController;
    protected int columnIndex;

    @FXML
    protected TextField nameInput, defaultInput, lengthInput, widthInput;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton stringRadio, doubleRadio, floatRadio, longRadio, intRadio, shortRadio, booleanRadio, dateRadio;
    @FXML
    protected CheckBox notNullCheck, editableCheck, formatCheck;
    @FXML
    protected ColorSet colorController;
    @FXML
    protected TextArea descInput;

    public ControlData2DColumnEdit() {
        TipsLabelKey = message("SqlIdentifierComments");
    }

    protected void setParameters(ControlData2DColumns columnsController) {
        try {
            this.columnsController = columnsController;
            columnIndex = -1;

            colorController.init(this, baseName + "Color");
            colorController.setColor(FxColorTools.randomColor());

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (stringRadio.isSelected()) {
                        defaultInput.setText("");
                    } else if (doubleRadio.isSelected()) {
                        defaultInput.setText("0");
                    } else if (floatRadio.isSelected()) {
                        defaultInput.setText("0");
                    } else if (longRadio.isSelected()) {
                        defaultInput.setText("0");
                    } else if (intRadio.isSelected()) {
                        defaultInput.setText("0");
                    } else if (shortRadio.isSelected()) {
                        defaultInput.setText("0");
                    } else if (booleanRadio.isSelected()) {
                        defaultInput.setText("false");
                    } else if (dateRadio.isSelected()) {
                        defaultInput.setText(DateTools.nowString());
                    }
                }
            });

            rightTipsView.setVisible(columnsController.data2D.isTable());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void setParameters(ControlData2DColumns columnsController, int index) {
        setParameters(columnsController);
        loadColumn(index);
    }

    public void loadColumn(int index) {
        try {
            Data2DColumn column = columnsController.tableData.get(index);
            if (column == null) {
                return;
            }
            columnIndex = index;
            nameInput.setText(column.getColumnName());
            lengthInput.setText(column.getLength() + "");
            widthInput.setText(column.getWidth() + "");
            defaultInput.setText(column.getDefaultValue());
            descInput.setText(column.getDescription());

            notNullCheck.setSelected(column.isNotNull());
            editableCheck.setSelected(column.isEditable());
            formatCheck.setSelected(column.isNeedFormat());

            colorController.setColor(column.getColor());
            switch (column.getType()) {
                case String:
                    stringRadio.setSelected(true);
                    return;
                case Double:
                    doubleRadio.setSelected(true);
                    return;
                case Float:
                    floatRadio.setSelected(true);
                    return;
                case Long:
                    longRadio.setSelected(true);
                    return;
                case Integer:
                    intRadio.setSelected(true);
                    return;
                case Short:
                    shortRadio.setSelected(true);
                    return;
                case Boolean:
                    booleanRadio.setSelected(true);
                    return;
                case Datetime:
                    dateRadio.setSelected(true);
                    return;
                default:
                    stringRadio.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Data2DColumn pickValues() {
        try {
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return null;
            }
            for (int i = 0; i < columnsController.tableData.size(); i++) {
                Data2DColumn col = columnsController.tableData.get(i);
                if (i != columnIndex && name.equalsIgnoreCase(col.getColumnName())) {
                    popError(message("AlreadyExisted"));
                    return null;
                }
            }
            int length;
            try {
                length = Integer.parseInt(lengthInput.getText());
                if (length < 0 || length > BaseTable.StringMaxLength) {
                    length = BaseTable.StringMaxLength;
                }
            } catch (Exception ee) {
                popError(message("InvalidParameter") + ": " + message("Length"));
                return null;
            }
            int width;
            try {
                width = Integer.parseInt(widthInput.getText());
            } catch (Exception ee) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return null;
            }
            Data2DColumn column;
            if (columnIndex >= 0) {
                column = columnsController.tableData.get(columnIndex);
            } else {
                column = new Data2DColumn();
            }
            column.setColumnName(name).setLength(length).setWidth(width)
                    .setNotNull(notNullCheck.isSelected())
                    .setEditable(editableCheck.isSelected())
                    .setNeedFormat(formatCheck.isSelected())
                    .setColor((Color) colorController.rect.getFill())
                    .setDescription(descInput.getText());
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
            String dv = defaultInput.getText();
            if (dv != null) {
                column.setDefaultValue(dv);
            }
            return column;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static ControlData2DColumnEdit open(ControlData2DColumns columnsController) {
        try {
            ControlData2DColumnEdit controller = (ControlData2DColumnEdit) WindowTools.openChildStage(
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
