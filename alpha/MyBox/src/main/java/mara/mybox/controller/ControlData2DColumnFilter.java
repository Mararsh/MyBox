package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ColumnFilter;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-7-5
 * @License Apache License Version 2.0
 */
public class ControlData2DColumnFilter extends ControlData2DRowExpression {

    protected ColumnFilter columnFilter;

    @FXML
    protected CheckBox emptyCheck, zeroCheck, negativeCheck, positiveCheck,
            equalCheck, largerCheck, lessCheck, expressionCheck;
    @FXML
    protected RadioButton noneRadio, workRadio, trueRadio, falseRadio,
            q3Radio, e3Radio, e4Radio, largerValueRadio,
            q1Radio, e2Radio, e1Radio, lessValueRadio;
    @FXML
    protected TextField equalInput, largerInput, lessInput;
    @FXML
    protected VBox conditionsBox;

    public ControlData2DColumnFilter() {
        TipsLabelKey = "ColumnFilterTips";
    }

    @Override
    public void initCalculator() {
        try {
            columnFilter = new ColumnFilter();
            calculator = columnFilter;

            conditionsBox.disableProperty().bind(noneRadio.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            baseName = parent.baseName;

            if (tableController != null) {
                columnFilter.setWebEngine(tableController.dataController.viewController.htmlController.webEngine);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(ColumnFilter columnFilter) {
        if (columnFilter == null || !columnFilter.isWork()) {
            noneRadio.fire();
            emptyCheck.setSelected(false);
            zeroCheck.setSelected(false);
            negativeCheck.setSelected(false);
            positiveCheck.setSelected(false);
            equalCheck.setSelected(false);
            largerCheck.setSelected(false);
            lessCheck.setSelected(false);
            expressionCheck.setSelected(false);
            largerValueRadio.fire();
            lessValueRadio.fire();
            equalInput.clear();
            largerInput.clear();
            lessInput.clear();
            scriptInput.clear();
            trueRadio.fire();
        } else {
            workRadio.fire();
            emptyCheck.setSelected(columnFilter.isEmpty());
            zeroCheck.setSelected(columnFilter.isZero());
            negativeCheck.setSelected(columnFilter.isNegative());
            positiveCheck.setSelected(columnFilter.isPositive());
            equalCheck.setSelected(columnFilter.isEqual());
            if (columnFilter.isEqual()) {
                equalInput.setText(columnFilter.getEqualValue());
            }
            largerCheck.setSelected(columnFilter.isLarger());
            String larger = columnFilter.getLargerThan();
            if (ColumnFilter.Q3.equals(larger)) {
                q3Radio.fire();
            } else if (ColumnFilter.E3.equals(larger)) {
                e3Radio.fire();
            } else if (ColumnFilter.E4.equals(larger)) {
                e4Radio.fire();
            } else {
                largerValueRadio.fire();
                largerInput.setText(larger);
            }
            lessCheck.setSelected(columnFilter.isLess());
            String less = columnFilter.getLessThan();
            if (ColumnFilter.Q1.equals(larger)) {
                q1Radio.fire();
            } else if (ColumnFilter.E2.equals(larger)) {
                e2Radio.fire();
            } else if (ColumnFilter.E1.equals(larger)) {
                e1Radio.fire();
            } else {
                lessValueRadio.fire();
                lessInput.setText(less);
            }
            expressionCheck.setSelected(columnFilter.isColumnExpression());
            scriptInput.setText(columnFilter.getScript());
            if (columnFilter.reversed) {
                falseRadio.fire();
            } else {
                trueRadio.fire();
            }
        }
    }

    public ColumnFilter pickValues() {
        columnFilter.setWork(workRadio.isSelected())
                .setEmpty(emptyCheck.isSelected())
                .setZero(zeroCheck.isSelected())
                .setPositive(positiveCheck.isSelected())
                .setNegative(negativeCheck.isSelected())
                .setEqual(equalCheck.isSelected())
                .setLarger(largerCheck.isSelected())
                .setLess(lessCheck.isSelected())
                .setColumnExpression(expressionCheck.isSelected());
        if (equalCheck.isSelected()) {
            columnFilter.setEqualValue(equalInput.getText());
        }
        columnFilter.setLargerThanNumber(AppValues.InvalidDouble);
        if (largerCheck.isSelected()) {
            if (q3Radio.isSelected()) {
                columnFilter.setLargerThan(ColumnFilter.Q3);
            } else if (e3Radio.isSelected()) {
                columnFilter.setLargerThan(ColumnFilter.E3);
            } else if (e4Radio.isSelected()) {
                columnFilter.setLargerThan(ColumnFilter.E4);
            } else if (largerValueRadio.isSelected()) {
                columnFilter.setLargerThan(largerInput.getText());
                try {
                    columnFilter.setLargerThanNumber(Double.valueOf(largerInput.getText()));
                } catch (Exception e) {
                }
            }
        } else {
            columnFilter.setLargerThan(null);
        }
        columnFilter.setLessThanNumber(AppValues.InvalidDouble);
        if (largerCheck.isSelected()) {
            if (q1Radio.isSelected()) {
                columnFilter.setLessThan(ColumnFilter.Q1);
            } else if (e2Radio.isSelected()) {
                columnFilter.setLessThan(ColumnFilter.E2);
            } else if (e1Radio.isSelected()) {
                columnFilter.setLessThan(ColumnFilter.E1);
            } else if (lessValueRadio.isSelected()) {
                columnFilter.setLargerThan(lessInput.getText());
                try {
                    columnFilter.setLessThanNumber(Double.valueOf(lessInput.getText()));
                    lessInput.setStyle(null);
                } catch (Exception e) {
                    lessInput.setStyle(UserConfig.badStyle());
                }
            }
        } else {
            columnFilter.setLessThan(null);
        }
        if (expressionCheck.isSelected()) {
            columnFilter.setScript(scriptInput.getText());
            columnFilter.setReversed(falseRadio.isSelected());
        } else {
            columnFilter.setScript(null);
        }
        return columnFilter;
    }

    @Override
    protected void scriptExampleButtons(MenuController controller) {
        try {
            List<String> names = new ArrayList<>();
            String placehold = ColumnFilter.placehold();
            names.add(placehold);
            PopTools.addButtonsPane(controller, scriptInput, names);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    placehold + " == 100",
                    placehold + " != 6",
                    placehold + " >= 0 && " + placehold + " <= 100 ",
                    placehold + " < 0 || " + placehold + " > 100 "
            ));

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "'" + placehold + "'.search(/Hello/ig) >= 0",
                    "'" + placehold + "'.length > 0",
                    "'" + placehold + "'.indexOf('Hello') == 3",
                    "'" + placehold + "'.startsWith('Hello')",
                    "'" + placehold + "'.endsWith('Hello')",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "array.includes('" + placehold + "')"
            ));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
