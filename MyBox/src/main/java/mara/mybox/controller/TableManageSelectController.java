package mara.mybox.controller;

import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.ColumnDefinition;
import mara.mybox.db.ColumnDefinition.ColumnType;
import mara.mybox.db.TableBase;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-07-16
 * @License Apache License Version 2.0
 */
public class TableManageSelectController extends BaseController {

    protected TableManageConditionController conditionController;
    protected TableBase tableDefinition;

    @FXML
    private Label titleLabel;
    @FXML
    private VBox columnsBox;

    public TableManageSelectController() {
        baseTitle = message("TableSelectConditions");
    }

    public void setValues(TableManageConditionController conditionController,
            TableBase tableDefinition) {
        try {
            this.conditionController = conditionController;
            this.tableDefinition = tableDefinition;

            titleLabel.setText(tableDefinition.name());
            columnsBox.getChildren().clear();
            for (Object o : tableDefinition.getColumns()) {
                ColumnDefinition column = (ColumnDefinition) o;
                HBox line = new HBox();
                line.setAlignment(Pos.TOP_CENTER);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(line, Priority.NEVER);
                HBox.setHgrow(line, Priority.ALWAYS);
                line.setSpacing(5);

                Label label = new Label(column.getLabel());

                TextField input = new TextField();
                HBox.setHgrow(input, Priority.ALWAYS);
                input.setMaxWidth(Double.MAX_VALUE);
                if (column.getType() == ColumnType.Date || column.getType() == ColumnType.Datetime) {
                    FxmlControl.setTooltip(input, message("EraComments"));
                }
                if (column.getMinValue() != null) {
                    String s = message("LargerThan") + " " + column.getMinValue();
                    if (column.getMaxValue() != null) {
                        s += " " + message("And") + " " + message("LessThan") + " " + column.getMaxValue();
                    }
                    FxmlControl.setTooltip(input, s);
                } else {
                    if (column.getMaxValue() != null) {
                        FxmlControl.setTooltip(input, message("LessThan") + " " + column.getMaxValue());
                    }
                }
                input.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            checkInput(column, input, newValue);
                        });

                Button button = new Button(message("Condition"));
                button.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        buttonMenu(column, input, button);
                    }
                });
                line.getChildren().addAll(label, button, input);
                columnsBox.getChildren().add(line);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkInput(ColumnDefinition column, TextField input, String value) {
        try {
            column.setValue(null);
            if (value == null || value.trim().isBlank()) {
                input.setStyle(null);
                return;
            }
            switch (column.getType()) {
                case Short: {
                    short v = Short.parseShort(value.trim());
                    if (column.getMaxValue() != null && v > (short) (column.getMaxValue())
                            || column.getMinValue() != null && v < (short) (column.getMinValue())) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(v);
                        input.setStyle(null);
                    }
                    break;
                }
                case Integer: {
                    int v = Integer.parseInt(value.trim());
                    if (column.getMaxValue() != null && v > (int) (column.getMaxValue())
                            || column.getMinValue() != null && v < (int) (column.getMinValue())) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(v);
                        input.setStyle(null);
                    }
                    break;
                }
                case Long: {
                    long v = Long.parseLong(value.trim());
                    if (column.getMaxValue() != null && v > (long) (column.getMaxValue())
                            || column.getMinValue() != null && v < (long) (column.getMinValue())) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(v);
                        input.setStyle(null);
                    }
                    break;
                }
                case Double: {
                    double v = Double.parseDouble(value.trim());
                    if (column.getMaxValue() != null && v > (double) (column.getMaxValue())
                            || column.getMinValue() != null && v < (double) (column.getMinValue())) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(v);
                        input.setStyle(null);
                    }
                    break;
                }
                case Float: {
                    float v = Float.parseFloat(value.trim());
                    if (column.getMaxValue() != null && v > (float) (column.getMaxValue())
                            || column.getMinValue() != null && v < (float) (column.getMinValue())) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(v);
                        input.setStyle(null);
                    }
                    break;
                }
                case String: {
                    if (column.getLength() > 0 && value.trim().length() > column.getLength()) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(value.trim());
                    }
                    break;
                }
                case Datetime:
                case Date: {
                    Date d = DateTools.encodeEra(value.trim());
                    if (d == null) {
                        input.setStyle(badStyle);
                    } else {
                        column.setValue(d);
                        input.setStyle(null);
                    }
                    break;
                }
                default:
                    input.setStyle(null);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            input.setStyle(badStyle);
        }
    }

    protected void buttonMenu(ColumnDefinition column, TextField input, Button button) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Clear"));
            menu.setOnAction((ActionEvent event) -> {
                input.clear();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("IsNull"));
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + " IS NULL");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("IsNotNull"));
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + " IS NOT NULL");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("=");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat("=", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<>");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat("<>", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(">");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat(">", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat("<", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(">=");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat(">=", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<=");
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat("<=", column));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Between"));
            menu.setOnAction((ActionEvent event) -> {
                input.setText(column.getName() + compareFormat("<=", column));
                String s = column.getName() + " BETWEEN ";
                switch (column.getType()) {
                    case String:
                        s += "'Mara' AND 'MyBox'";
                        break;
                    case Date:
                        s += "'2020-03-01' AND '2020-03-31'";
                        break;
                    case Datetime:
                        s += "'2020-03-01 12:00:00' AND '2020-03-01 23:59:59'";
                        break;
                    default:
                        s += " 0 AND 9";
                        break;
                }
                input.setText(s);
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow(button, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String compareFormat(String operator, ColumnDefinition column) {
        switch (column.getType()) {
            case String:
                return operator + "'MyBox'";
            case Date:
                return operator + "'2020-03-01'";
            case Datetime:
                return operator + "'2020-03-01 11:12:33'";
            default:
                return operator + "0";
        }
    }

}
