package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowEdit extends BaseController {

    protected BaseData2DLoadController editController;
    protected int rowIndex;
    protected Map<Data2DColumn, Object> inputs;
    protected TableColor tableColor;

    @FXML
    protected VBox valuesBox;
    @FXML
    protected TextField indexInput;
    @FXML
    protected Button locationButton;

    public void setParameters(BaseData2DLoadController editController) {
        try {
            this.editController = editController;
            rowIndex = -1;

            makeInputs();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DLoadController editController, int index) {
        try {
            this.editController = editController;
            rowIndex = index;

            makeInputs();
            loadRow(index);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeInputs() {
        try {
            if (editController == null || editController.data2D == null) {
                return;
            }
            locationButton.setVisible(editController.data2D.includeCoordinate());

            valuesBox.getChildren().clear();
            inputs = new HashMap<>();
            List<Data2DColumn> columns = editController.data2D.getColumns();

            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                ColumnType type = column.getType();
                if (column.isEnumType()) {
                    makeEnumInput(column);

                } else if (type == ColumnType.Boolean) {
                    makeBooleanInput(column);

                } else if (type == ColumnType.Color) {
                    makeColorInput(column);

                } else if (column.isDBNumberType()) {
                    makeTextField(column);

                } else if (column.isTimeType()) {
                    makeDateInput(column);

                } else if (editController.data2D.supportMultipleLine()) {
                    makeTextArea(column);

                } else {
                    makeTextField(column);

                }
            }

            thisPane.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeTextField(Data2DColumn column) {
        try {
            HBox line = makeTextField(column.getColumnName(), column.isEditable());
            TextField input = (TextField) line.getChildren().get(1);
            inputs.put(column, input);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public HBox makeTextField(String name, boolean editable) {
        try {
            HBox line = makeLineBox(name);

            TextField input = new TextField();
            input.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(input, Priority.ALWAYS);
            input.setDisable(!editable);
            line.getChildren().add(input);

            return line;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void makeTextArea(Data2DColumn column) {
        try {
            Label label = new Label(column.getColumnName());
            label.setWrapText(true);
            valuesBox.getChildren().add(label);

            TextArea input = new TextArea();
            input.setPrefHeight(60);
            input.setMinHeight(60);
            input.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(input, Priority.ALWAYS);
            input.setEditable(column.isEditable());
            valuesBox.getChildren().add(input);
            inputs.put(column, input);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeBooleanInput(Data2DColumn column) {
        try {
            HBox line = makeLineBox(column.getColumnName());

            RadioButton trueButton = new RadioButton(message("true"));
            RadioButton falseButton = new RadioButton(message("false"));
            ToggleGroup group = new ToggleGroup();
            group.getToggles().addAll(trueButton, falseButton);
            line.getChildren().addAll(trueButton, falseButton);

            trueButton.setDisable(!column.isEditable());
            falseButton.setDisable(!column.isEditable());

            falseButton.setSelected(true);

            inputs.put(column, group);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeDateInput(Data2DColumn column) {
        try {
            HBox line = makeTextField(column.getColumnName(), column.isEditable());
            TextField input = (TextField) line.getChildren().get(1);
            inputs.put(column, input);

            if (column.isEditable()) {
                Button dateButton = new Button();
                dateButton.setGraphic(StyleTools.getIconImageView("iconExamples.png"));
                NodeStyleTools.setTooltip(dateButton, new Tooltip(message("Example")));
                dateButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        switch (column.getType()) {
                            case Datetime:
                                popMenu = PopTools.popDatetimeExamples(myController, popMenu, input, event);
                                break;
                            case Date:
                                popMenu = PopTools.popDateExamples(myController, popMenu, input, event);
                                break;
                            case Era:
                                PopTools.popEraExamples(myController, input, event);
                                break;
                        }
                    }
                });
                line.getChildren().add(dateButton);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeColorInput(Data2DColumn column) {
        try {
            HBox line = makeLineBox(column.getColumnName());

            Rectangle rectangle = new Rectangle(30, 20);
            Color color = Color.web(UserConfig.getString(baseName, FxColorTools.color2rgba(Color.WHITE)));
            if (tableColor == null) {
                tableColor = new TableColor();
            }
            NodeStyleTools.setTooltip(rectangle, FxColorTools.colorNameDisplay(tableColor, color));
            rectangle.setStrokeWidth(1);
            rectangle.setStroke(Color.BLACK);
            rectangle.setFill(color);
            rectangle.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString(baseName, ((Color) newValue).toString());
                }
            });
            line.getChildren().add(rectangle);
            inputs.put(column, rectangle);

            if (column.isEditable()) {
                Button paletteButton = new Button();
                paletteButton.setGraphic(StyleTools.getIconImageView("iconPalette.png"));
                NodeStyleTools.setTooltip(paletteButton, new Tooltip(message("ColorPalette")));
                paletteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(
                                    WindowTools.class.getResource(Fxmls.ColorPalettePopupFxml), AppVariables.CurrentBundle);
                            Pane pane = fxmlLoader.load();
                            ColorPalettePopupController controller = (ColorPalettePopupController) fxmlLoader.getController();
                            controller.load(myController, rectangle);

                            popup = makePopup();
                            popup.getContent().add(pane);
                            LocateTools.locateCenter(paletteButton, popup);
                        } catch (Exception e) {
                            MyBoxLog.debug(e);
                        }
                    }
                });
                line.getChildren().add(paletteButton);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeEnumInput(Data2DColumn column) {
        try {
            HBox line = makeLineBox(column.getColumnName());

            ComboBox<String> selector = new ComboBox<>();
            selector.getItems().addAll(column.enumNames());
            selector.setEditable(column.getType() == ColumnType.EnumerationEditable);
            line.getChildren().add(selector);

            inputs.put(column, selector);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public HBox makeLineBox(String name) {
        try {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER_LEFT);
            line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            line.setSpacing(5);
            HBox.setHgrow(line, Priority.ALWAYS);
            valuesBox.getChildren().add(line);

            Label label = new Label(name);
            label.setWrapText(true);
            line.getChildren().addAll(label);

            return line;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void loadRow(int index) {
        try {
            List<String> row = editController.tableData.get(index);
            if (row == null) {
                return;
            }
            rowIndex = index;
            indexInput.setText(row.get(0));
            List<Data2DColumn> columns = editController.data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                Object input = inputs.get(column);
                String value = row.get(i + 1);
                if (input instanceof TextField) {
                    ((TextField) input).setText(value);

                } else if (input instanceof TextArea) {
                    ((TextArea) input).setText(value);

                } else if (input instanceof ComboBox) {
                    ((ComboBox) input).setValue(value);

                } else if (input instanceof ToggleGroup) {
                    try {
                        if (StringTools.isTrue(value)) {
                            ((RadioButton) ((ToggleGroup) input).getToggles().get(0)).setSelected(true);
                        } else {
                            ((RadioButton) ((ToggleGroup) input).getToggles().get(1)).setSelected(true);
                        }
                    } catch (Exception e) {
                    }

                } else if (input instanceof Rectangle) {
                    try {
                        Color color = Color.web(value);
                        Rectangle rect = (Rectangle) input;
                        rect.setFill(color);
                    } catch (Exception e) {
                    }

                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<String> pickValues(boolean checkValid) {
        try {
            List<String> row = new ArrayList<>();
            row.add(indexInput.getText());
            List<Data2DColumn> columns = editController.data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                Object input = inputs.get(column);
                String value = null;
                if (input instanceof TextField) {
                    value = ((TextField) input).getText();

                } else if (input instanceof TextArea) {
                    value = ((TextArea) input).getText();

                } else if (input instanceof ComboBox) {
                    value = ((ComboBox<String>) input).getValue();

                } else if (input instanceof ToggleGroup) {
                    try {
                        if (((RadioButton) ((ToggleGroup) input).getToggles().get(0)).isSelected()) {
                            value = message("true");
                        } else {
                            value = message("false");
                        }
                    } catch (Exception e) {
                    }

                } else if (input instanceof Rectangle) {
                    try {
                        value = ((Color) ((Rectangle) input).getFill()).toString();
                    } catch (Exception e) {
                    }

                }
                if (checkValid) {
                    if (column.isAuto()
                            || (column.validValue(value) && editController.data2D.validValue(value))) {
                        row.add(value);
                    } else {
                        popError(message("Invalid") + ": " + column.getColumnName());
                        return null;
                    }
                } else {
                    row.add(value);
                }
            }
            return row;
        } catch (Exception ex) {
            MyBoxLog.error(ex);
            return null;
        }
    }

    @FXML
    public void locationAction() {
        Data2DCoordinatePickerController.open(this);
    }

}
