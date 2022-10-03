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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppValues;
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
    protected TextField nameInput, defaultInput, lengthInput, widthInput, formatInput, scaleInput;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton stringRadio, doubleRadio, floatRadio, longRadio, intRadio, shortRadio, booleanRadio,
            datetimeRadio, dateRadio, eraRadio, longitudeRadio, latitudeRadio, enumRadio;
    @FXML
    protected CheckBox notNullCheck, editableCheck;
    @FXML
    protected ColorSet colorController;
    @FXML
    protected TextArea enumInput, descInput;
    @FXML
    protected VBox optionsBox, enumBox;
    @FXML
    protected HBox formatBox;

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
                    checkType();
                }
            });
            loadColumn(null);

            rightTipsView.setVisible(columnsController.data2D.isTable());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void setParameters(ControlData2DColumns columnsController, int index) {
        setParameters(columnsController);
        loadColumn(index);
    }

    public void checkType() {
        try {
            if (isSettingValues) {
                return;
            }
            optionsBox.getChildren().clear();
            defaultInput.clear();

            if (doubleRadio.isSelected() || floatRadio.isSelected()
                    || longRadio.isSelected() || intRadio.isSelected() || shortRadio.isSelected()) {
                optionsBox.getChildren().add(formatBox);

            } else if (datetimeRadio.isSelected() || dateRadio.isSelected() || eraRadio.isSelected()) {
                optionsBox.getChildren().add(formatBox);

            } else if (enumRadio.isSelected()) {
                optionsBox.getChildren().add(enumBox);

            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadColumn(int index) {
        try {
            Data2DColumn column = columnsController.tableData.get(index);
            if (column == null) {
                column = new Data2DColumn();
            }
            loadColumn(column);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadColumn(Data2DColumn column) {
        try {
            if (column == null) {
                return;
            }
            isSettingValues = true;
            switch (column.getType()) {
                case String:
                    stringRadio.setSelected(true);
                    break;
                case Double:
                    doubleRadio.setSelected(true);
                    break;
                case Float:
                    floatRadio.setSelected(true);
                    break;
                case Long:
                    longRadio.setSelected(true);
                    break;
                case Integer:
                    intRadio.setSelected(true);
                    break;
                case Short:
                    shortRadio.setSelected(true);
                    break;
                case Boolean:
                    booleanRadio.setSelected(true);
                    break;
                case Datetime:
                    datetimeRadio.setSelected(true);
                    break;
                case Date:
                    dateRadio.setSelected(true);
                    break;
                case Era:
                    eraRadio.setSelected(true);
                    break;
                case Enumeration:
                    enumRadio.setSelected(true);
                    break;
                case Longitude:
                    longitudeRadio.setSelected(true);
                    break;
                case Latitude:
                    latitudeRadio.setSelected(true);
                    break;
                default:
                    stringRadio.setSelected(true);
            }
            isSettingValues = false;
            checkType();

            columnIndex = column.getIndex();
            nameInput.setText(column.getColumnName());
            lengthInput.setText(column.getLength() + "");
            widthInput.setText(column.getWidth() + "");
            scaleInput.setText(column.getScale() + "");
            String format = column.getFormat();
            if (format == null) {
                enumInput.clear();
                formatInput.clear();
            } else {
                enumInput.setText(format.replaceAll(AppValues.MyBoxSeparator, "\n"));
                formatInput.setText(format);
            }
            defaultInput.setText(column.getDefaultValue());
            descInput.setText(column.getDescription());

            notNullCheck.setSelected(column.isNotNull());
            editableCheck.setSelected(column.isEditable());

            colorController.setColor(column.getColor());

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
            int scale;
            try {
                scale = Integer.parseInt(scaleInput.getText());
            } catch (Exception ee) {
                popError(message("InvalidParameter") + ": " + message("DecimialScale"));
                return null;
            }
            Data2DColumn column;
            if (columnIndex >= 0) {
                column = columnsController.tableData.get(columnIndex);
            } else {
                column = new Data2DColumn();
            }
            column.setColumnName(name)
                    .setLength(length).setWidth(width).setScale(scale)
                    .setNotNull(notNullCheck.isSelected())
                    .setEditable(editableCheck.isSelected())
                    .setColor((Color) colorController.rect.getFill())
                    .setDescription(descInput.getText());
            String format = formatInput.getText();
            if (stringRadio.isSelected()) {
                column.setType(ColumnType.String);
            } else if (doubleRadio.isSelected()) {
                column.setType(ColumnType.Double).setFormat(format);
            } else if (floatRadio.isSelected()) {
                column.setType(ColumnType.Float).setFormat(format);
            } else if (longRadio.isSelected()) {
                column.setType(ColumnType.Long).setFormat(format);
            } else if (intRadio.isSelected()) {
                column.setType(ColumnType.Integer).setFormat(format);
            } else if (shortRadio.isSelected()) {
                column.setType(ColumnType.Short).setFormat(format);
            } else if (booleanRadio.isSelected()) {
                column.setType(ColumnType.Boolean);
            } else if (datetimeRadio.isSelected()) {
                column.setType(ColumnType.Datetime).setFormat(format);
            } else if (dateRadio.isSelected()) {
                column.setType(ColumnType.Date).setFormat(format);
            } else if (eraRadio.isSelected()) {
                column.setType(ColumnType.Era).setFormat(format);
            } else if (enumRadio.isSelected()) {
                column.setType(ColumnType.Enumeration)
                        .setFormat(format != null ? format.replaceAll("\n", AppValues.MyBoxSeparator) : null);
            } else if (longitudeRadio.isSelected()) {
                column.setType(ColumnType.Longitude);
            } else if (latitudeRadio.isSelected()) {
                column.setType(ColumnType.Latitude);
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
