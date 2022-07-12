package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ColumnFilter;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-5
 * @License Apache License Version 2.0
 */
public class ControlData2DColumnFilter extends ControlData2DRowExpression {

    @FXML
    protected CheckBox emptyCheck, zeroCheck, negativeCheck, positiveCheck,
            q1Check, q3Check, e4Check, e3Check, e2Check, e1Check;
    @FXML
    protected RadioButton trueRadio, othersRadio;

    public ControlData2DColumnFilter() {
        TipsLabelKey = "ColumnFilterTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        try {
            baseName = parent.baseName;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(ColumnFilter columnFilter) {
        if (columnFilter == null) {
            scriptInput.clear();
            trueRadio.fire();
        } else {
            scriptInput.setText(columnFilter.getScript());
            if (columnFilter.reversed) {
                othersRadio.fire();
            } else {
                trueRadio.fire();
            }
        }
    }

    @Override
    protected void scriptExampleButtons(MenuController controller) {
        try {
            List<String> colnames = data2D.columnNames();
            List<String> names = new ArrayList<>();
            names.add("#{" + message("ColumnValue") + "}");
            PopTools.addButtonsPane(controller, scriptInput, names);
            controller.addNode(new Separator());

            if (!colnames.isEmpty()) {
                String col1 = colnames.get(0);
                PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                        "#{" + message("ColumnValue") + "} == 100",
                        "#{" + message("ColumnValue") + "} != 6",
                        "#{" + message("ColumnValue") + "} >= 0 && #{" + message("ColumnValue") + "} <= 100 ",
                        "#{" + message("ColumnValue") + "} < 0 || #{" + message("ColumnValue") + "} > 100 "
                ));

                PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                        "'#{" + message("ColumnValue") + "}'.search(/Hello/ig) >= 0",
                        "'#{" + message("ColumnValue") + "}'.length > 0",
                        "'#{" + message("ColumnValue") + "}'.indexOf('Hello') == 3",
                        "'#{" + message("ColumnValue") + "}'.startsWith('Hello')",
                        "'#{" + message("ColumnValue") + "}'.endsWith('Hello')",
                        "var array = [ 'A', 'B', 'C', 'D' ];\n"
                        + "array.includes('#{" + message("ColumnValue") + "}')"
                ));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
