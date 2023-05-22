package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowExpression extends ControlJavaScriptRefer {

    protected Data2D data2D;
    public ExpressionCalculator calculator;

    @FXML
    protected CheckBox onlyStatisticCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCalculator();

            onlyStatisticCheck.setSelected(UserConfig.getBoolean(baseName + "OnlyStatisticNumbers", false));
            onlyStatisticCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "OnlyStatisticNumbers", nv);
                    setPlaceholders();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initCalculator() {
        calculator = new ExpressionCalculator();
    }

    public void setData2D(Data2D data2D) {
        try {
            this.data2D = data2D;
            setPlaceholders();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setPlaceholders() {
        try {
            Platform.runLater(() -> {
                placeholdersList.getItems().clear();
                if (data2D == null || !data2D.isValid()) {
                    return;
                }
                List<Data2DColumn> columns = data2D.getColumns();
                List<String> list = new ArrayList<>();
                for (Data2DColumn column : columns) {
                    String name = column.getColumnName();
                    list.add("#{" + name + "}");
                    if ((onlyStatisticCheck != null && !onlyStatisticCheck.isSelected())
                            || column.isNumberType()) {
                        list.add("#{" + name + "-" + message("Mean") + "}");
                        list.add("#{" + name + "-" + message("Median") + "}");
                        list.add("#{" + name + "-" + message("Mode") + "}");
                        list.add("#{" + name + "-" + message("MinimumQ0") + "}");
                        list.add("#{" + name + "-" + message("LowerQuartile") + "}");
                        list.add("#{" + name + "-" + message("UpperQuartile") + "}");
                        list.add("#{" + name + "-" + message("MaximumQ4") + "}");
                        list.add("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}");
                        list.add("#{" + name + "-" + message("LowerMildOutlierLine") + "}");
                        list.add("#{" + name + "-" + message("UpperMildOutlierLine") + "}");
                        list.add("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}");
                    }
                }
                list.add("#{" + message("TableRowNumber") + "}");
                list.add("#{" + message("DataRowNumber") + "}");

                placeholdersList.getItems().setAll(list);
                placeholdersList.applyCss();
                placeholdersList.layout();
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void moreExampleButtons(MenuController controller, String menuName) {
        try {
            if (data2D == null || !data2D.isValid()) {
                return;
            }
            String col1 = data2D.columnNames().get(0);
            PopTools.rowExpressionButtons(controller, scriptInput, col1, menuName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkExpression(boolean allPages) {
        error = null;
        if (data2D == null || !data2D.hasData()) {
            error = message("InvalidData");
            return false;
        }
        String script = scriptInput.getText();
        if (script == null || script.isBlank()) {
            return true;
        }
        if (calculator.validateExpression(data2D, script, allPages)) {
            TableStringValues.add(interfaceName + "Histories", script.trim());
            return true;
        } else {
            error = calculator.getError();
            return false;
        }
    }

    @FXML
    public void showRowExpressionHelps(Event event) {
        popEventMenu(event, HelpTools.rowExpressionHelps(this));
    }

    @FXML
    public void popRowExpressionHelps(Event event) {
        if (UserConfig.getBoolean("RowExpressionsHelpsPopWhenMouseHovering", false)) {
            showRowExpressionHelps(event);
        }
    }

}
