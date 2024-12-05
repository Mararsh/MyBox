package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-6-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowExpression extends ControlData2DRowFilter {

    @FXML
    protected CheckBox onlyStatisticCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

//            onlyStatisticCheck.setSelected(UserConfig.getBoolean(baseName + "OnlyStatisticNumbers", false));
//            onlyStatisticCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
//                    UserConfig.setBoolean(baseName + "OnlyStatisticNumbers", nv);
//                    setPlaceholders();
//                }
//            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    protected void showScriptExamples(Event event) {
        PopTools.popRowExpressionExamples(this, event, scriptInput, baseName + "Examples", data2D);
    }

    public boolean checkExpression(boolean allPages) {
        error = null;
        if (data2D == null || !data2D.isValidDefinition()) {
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
