package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
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

    public ControlData2DRowExpression() {
        TipsLabelKey = "RowExpressionTips";
    }

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
            placeholdersList.getItems().clear();
            if (data2D == null || !data2D.isValid()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();
            for (Data2DColumn column : columns) {
                String name = column.getColumnName();
                placeholdersList.getItems().add("#{" + name + "}");
                if (!onlyStatisticCheck.isSelected() || column.isNumberType()) {
                    placeholdersList.getItems().add("#{" + name + "-" + message("Mean") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("Median") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("Mode") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("MinimumQ0") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("LowerQuartile") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("UpperQuartile") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("MaximumQ4") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("LowerExtremeOutlierLine") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("LowerMildOutlierLine") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("UpperMildOutlierLine") + "}");
                    placeholdersList.getItems().add("#{" + name + "-" + message("UpperExtremeOutlierLine") + "}");
                }
            }
            placeholdersList.getItems().add("#{" + message("TableRowNumber") + "}");
            placeholdersList.getItems().add("#{" + message("DataRowNumber") + "}");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void moreExampleButtons(MenuController controller) {
        try {
            if (data2D == null || !data2D.isValid()) {
                return;
            }
            String col1 = data2D.columnNames().get(0);
            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "#{" + message("DataRowNumber") + "} % 2 == 0",
                    "#{" + message("DataRowNumber") + "} % 2 == 1",
                    "#{" + message("DataRowNumber") + "} >= 9",
                    "#{" + message("TableRowNumber") + "} % 2 == 0",
                    "#{" + message("TableRowNumber") + "} % 2 == 1",
                    "#{" + message("TableRowNumber") + "} == 1",
                    "#{" + col1 + "} == 0",
                    "Math.abs(#{" + col1 + "}) >= 0",
                    "#{" + col1 + "} < 0 || #{" + col1 + "} > 100 ",
                    "#{" + col1 + "} != 6"
            ), true, 2);

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "'#{" + col1 + "}' == ''",
                    "'#{" + col1 + "}'.length > 0",
                    "'#{" + col1 + "}' == '2016-05-19 11:34:28'",
                    "'#{" + col1 + "}'.search(/Hello/ig) >= 0",
                    "'#{" + col1 + "}'.indexOf('Hello') == 3",
                    "'#{" + col1 + "}'.startsWith('Hello')",
                    "'#{" + col1 + "}'.endsWith('Hello')",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "array.includes('#{" + col1 + "}')"
            ), true, 3);
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

}
