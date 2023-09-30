package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import static mara.mybox.db.data.ColumnDefinition.ColumnType.Color;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-19
 * @License Apache License Version 2.0
 */
public class ControlData2DColumnEdit extends BaseChildController {

    protected BaseData2DColumnsController columnsController;
    protected int columnIndex;

    @FXML
    protected TextField nameInput, defaultInput, lengthInput, widthInput, scaleInput, formatInput, centuryInput;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton stringRadio, doubleRadio, floatRadio, longRadio, intRadio, shortRadio, booleanRadio,
            datetimeRadio, dateRadio, eraRadio, longitudeRadio, latitudeRadio, enumRadio, colorRadio, clobRadio,
            invalidAsEmptyRadio, invalidAsZeroRadio, invalidAsSkipRadio;
    @FXML
    protected CheckBox notNullCheck, editableCheck, fixYearCheck;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected TextArea enumInput, descInput;
    @FXML
    protected VBox optionsBox, enumBox;
    @FXML
    protected HBox formatBox;
    @FXML
    protected FlowPane typesPane, fixPane, centuryPane, invalidPane;
    @FXML
    protected Label lengthLabel;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(rightTipsView, message("SqlIdentifierComments"));
            NodeStyleTools.setTooltip(tipsView, message("ColumnComments"));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void init(BaseData2DColumnsController columnsController) {
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

            centuryPane.disableProperty().bind(fixYearCheck.selectedProperty().not());

            rightTipsView.setVisible(columnsController.data2D != null && columnsController.data2D.isTable());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void setParameters(BaseData2DColumnsController columnsController) {
        init(columnsController);
        loadColumn(-1);
    }

    public void setParameters(BaseData2DColumnsController columnsController, int index) {
        init(columnsController);
        loadColumn(index);
    }

    public void checkType() {
        try {
            if (isSettingValues) {
                return;
            }
            optionsBox.getChildren().clear();
            defaultInput.clear();
            formatInput.clear();
            fixYearCheck.setSelected(false);
            invalidAsSkipRadio.setSelected(true);

            if (enumRadio.isSelected()) {
                optionsBox.getChildren().add(enumBox);

            } else if (datetimeRadio.isSelected()) {
                optionsBox.getChildren().addAll(formatBox, fixPane);
                formatInput.setText(TimeFormats.Datetime);

            } else if (dateRadio.isSelected()) {
                optionsBox.getChildren().addAll(formatBox, fixPane);
                formatInput.setText(TimeFormats.Date);

            } else if (eraRadio.isSelected()) {
                optionsBox.getChildren().addAll(formatBox, fixPane);
                formatInput.setText(TimeFormats.DateA + " G");

            } else if (doubleRadio.isSelected() || floatRadio.isSelected()
                    || longRadio.isSelected() || intRadio.isSelected() || shortRadio.isSelected()) {
                optionsBox.getChildren().add(formatBox);
                formatInput.setText(message("GroupInThousands"));
                invalidAsZeroRadio.setSelected(true);

            }

            if (clobRadio.isSelected()) {
                lengthInput.setText("2G");
                lengthInput.setDisable(true);
                lengthLabel.setText("(<= 2G)");
            } else {
                lengthInput.setText("32672");
                lengthInput.setDisable(false);
                lengthLabel.setText("(<= 32672)");
            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadColumn(int index) {
        try {
            if (index >= 0 && index < columnsController.tableData.size()) {
                Data2DColumn column = columnsController.tableData.get(index);
                columnIndex = index;
                loadColumn(column);
            } else {
                columnIndex = -1;
                loadColumn(new Data2DColumn());
            }
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
                case Color:
                    colorRadio.setSelected(true);
                    break;
                case Clob:
                    clobRadio.setSelected(true);
                    break;
                default:
                    stringRadio.setSelected(true);
            }
            isSettingValues = false;
            checkType();

            nameInput.setText(column.getColumnName());
            lengthInput.setText(column.getLength() + "");
            widthInput.setText(column.getWidth() + "");
            scaleInput.setText(column.getScale() + "");
            fixYearCheck.setSelected(column.isFixTwoDigitYear());
            centuryInput.setText(column.getCentury() + "");
            formatInput.clear();
            enumInput.clear();
            String format = column.getFormat();
            if (format != null) {
                if (enumRadio.isSelected()) {
                    enumInput.setText(format);

                } else if (datetimeRadio.isSelected() || dateRadio.isSelected() || eraRadio.isSelected()) {
                    formatInput.setText(format);

                } else if (doubleRadio.isSelected() || floatRadio.isSelected()
                        || longRadio.isSelected() || intRadio.isSelected() || shortRadio.isSelected()) {
                    formatInput.setText(format);

                }
            }
            defaultInput.setText(column.getDefaultValue());
            descInput.setText(column.getDescription());

            notNullCheck.setSelected(column.isNotNull());
            editableCheck.setSelected(column.isEditable());

            colorController.setColor(column.getColor());

            InvalidAs invalidAs = column.getInvalidAs();
            if (invalidAs == InvalidAs.Blank) {
                invalidAsEmptyRadio.setSelected(true);
            } else if (invalidAs == InvalidAs.Zero) {
                invalidAsZeroRadio.setSelected(true);
            } else {
                invalidAsSkipRadio.setSelected(true);
            }

            boolean canNotChange = columnsController.data2D != null
                    && columnsController.data2D.isTable() && columnIndex >= 0;
            nameInput.setDisable(canNotChange);
            for (int i = 1; i < typesPane.getChildren().size(); i++) {
                typesPane.getChildren().get(i).setDisable(canNotChange);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Data2DColumn pickValues() {
        try {
            String name = nameInput.getText();
            if (columnsController.data2D != null && columnsController.data2D.isTable()) {
                name = DerbyBase.fixedIdentifier(name);
            }
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
            if (clobRadio.isSelected()) {
                length = Integer.MAX_VALUE;
            } else {
                try {
                    length = Integer.parseInt(lengthInput.getText());
                    if (length < 0 || length > BaseTable.StringMaxLength) {
                        length = BaseTable.StringMaxLength;
                    }
                } catch (Exception ee) {
                    popError(message("InvalidParameter") + ": " + message("Length"));
                    return null;
                }
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
            int century = 2000;
            if (fixYearCheck.isSelected()) {
                try {
                    century = Integer.parseInt(centuryInput.getText());
                } catch (Exception ee) {
                    popError(message("InvalidParameter") + ": " + message("Century"));
                    return null;
                }
            }

            String enumString = enumInput.getText();
            if (enumRadio.isSelected() && (enumString == null || enumString.isBlank())) {
                popError(message("InvalidParameter") + ": " + message("EnumerateValues"));
                return null;
            }
            InvalidAs invalidAs;
            if (invalidAsEmptyRadio.isSelected()) {
                invalidAs = InvalidAs.Blank;
            } else if (invalidAsZeroRadio.isSelected()) {
                invalidAs = InvalidAs.Zero;
            } else {
                invalidAs = InvalidAs.Skip;
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
                    .setFixTwoDigitYear(fixYearCheck.isSelected())
                    .setCentury(century)
                    .setInvalidAs(invalidAs)
                    .setDescription(descInput.getText());

            String format = formatInput.getText();
            if (message("None").equals(format)) {
                format = null;
            }
            if (stringRadio.isSelected()) {
                column.setType(ColumnType.String).setFormat(null);
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
                column.setType(ColumnType.Boolean).setFormat(null);
            } else if (datetimeRadio.isSelected()) {
                column.setType(ColumnType.Datetime).setFormat(format);
            } else if (dateRadio.isSelected()) {
                column.setType(ColumnType.Date).setFormat(format);
            } else if (eraRadio.isSelected()) {
                column.setType(ColumnType.Era).setFormat(format);
            } else if (enumRadio.isSelected()) {
                column.setType(ColumnType.Enumeration)
                        .setFormat(enumString);
            } else if (longitudeRadio.isSelected()) {
                column.setType(ColumnType.Longitude).setFormat(null);
            } else if (latitudeRadio.isSelected()) {
                column.setType(ColumnType.Latitude).setFormat(null);
            } else if (colorRadio.isSelected()) {
                column.setType(ColumnType.Color).setFormat(null);
            } else if (clobRadio.isSelected()) {
                column.setType(ColumnType.Clob).setFormat(null);
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

    @FXML
    public void showExamples(Event event) {
        if (doubleRadio.isSelected() || floatRadio.isSelected()) {
            List<String> values = new ArrayList<>();
            values.add(message("GroupInThousands"));
            values.add(message("GroupInTenThousands"));
            values.add(message("ScientificNotation"));
            values.add(message("None"));
            popExamples(event, values, message("DecimalFormat"), HelpTools.decimalFormatLink());

        } else if (longRadio.isSelected() || intRadio.isSelected() || shortRadio.isSelected()) {
            List<String> values = new ArrayList<>();
            values.add(message("GroupInThousands"));
            values.add(message("GroupInTenThousands"));
            values.add(message("None"));
            popExamples(event, values, message("DecimalFormat"), HelpTools.decimalFormatLink());

        } else if (datetimeRadio.isSelected()) {
            List<String> values = new ArrayList<>();
            values.add(TimeFormats.Datetime);
            values.add(TimeFormats.DatetimeMs);
            values.add(TimeFormats.Date);
            values.add(TimeFormats.Month);
            values.add(TimeFormats.Year);
            values.add(TimeFormats.TimeMs);
            values.add(TimeFormats.DatetimeZone);
            values.add(TimeFormats.DatetimeC);
            values.add(TimeFormats.DatetimeMsC);
            values.add(TimeFormats.DateC);
            values.add(TimeFormats.MonthC);
            values.add(TimeFormats.DatetimeZoneC);
            values.add(TimeFormats.DatetimeE);
            values.add(TimeFormats.DatetimeMsE);
            values.add(TimeFormats.DateE);
            values.add(TimeFormats.MonthE);
            values.add(TimeFormats.DatetimeZoneE);
            popExamples(event, values, message("DateFormat"), HelpTools.simpleDateFormatLink());

        } else if (dateRadio.isSelected()) {
            List<String> values = new ArrayList<>();
            values.add(TimeFormats.Date);
            values.add(TimeFormats.Month);
            values.add(TimeFormats.Year);
            values.add(TimeFormats.DateC);
            values.add(TimeFormats.MonthC);
            values.add(TimeFormats.DateE);
            values.add(TimeFormats.MonthE);
            popExamples(event, values, message("DateFormat"), HelpTools.simpleDateFormatLink());

        } else if (eraRadio.isSelected()) {
            List<String> values = new ArrayList<>();
            values.add(TimeFormats.DatetimeA + " G");
            values.add(TimeFormats.DatetimeMsA + " G");
            values.add(TimeFormats.DateA + " G");
            values.add(TimeFormats.MonthA + " G");
            values.add(TimeFormats.YearA + " G");
            values.add("G" + TimeFormats.DatetimeA);
            values.add("G" + TimeFormats.DatetimeMsA);
            values.add("G" + TimeFormats.DateA);
            values.add("G" + TimeFormats.MonthA);
            values.add("G" + TimeFormats.YearA);
            popExamples(event, values, message("DateFormat"), HelpTools.simpleDateFormatLink());

        }

    }

    @FXML
    public void popExamples(Event event) {
        if (UserConfig.getBoolean("Data2DColumnEditPopWhenMouseHovering", true)) {
            showExamples(event);
        }
    }

    public void popExamples(Event event, List<String> values, String linkName, String linkAddress) {
        try {
            if (values == null || values.isEmpty()) {
                return;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ClearInputArea"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    formatInput.clear();
                }
            });
            items.add(menu);
            items.add(new SeparatorMenuItem());

            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        formatInput.setText(value);
                    }
                });
                items.add(menu);
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(linkName);
            menu.setStyle("-fx-text-fill: blue;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    myController.openLink(linkAddress);
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("Data2DColumnEditPopWhenMouseHovering", true));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DColumnEditPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
