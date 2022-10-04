package mara.mybox.controller;

import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-2
 * @License Apache License Version 2.0
 */
public class DatetimeInputController extends BaseInputController {

    protected ColumnType timeType;

    @FXML
    protected TextField timeInput;

    public DatetimeInputController() {
    }

    public void setParameters(BaseController parent, String title, String initValue, ColumnType timeType) {
        try {
            super.setParameters(parent, title);
            this.timeType = timeType;

            if (initValue != null) {
                timeInput.setText(initValue);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    public Date getDate() {
        return DateTools.encodeDate(timeInput.getText());
    }

    @Override
    public String getInputString() {
        return timeInput.getText();
    }

    @Override
    public boolean checkInput() {
        String s = timeInput.getText();
        if (s == null || s.isBlank()) {
            return true;
        }
        if (getDate() == null) {
            popError(message("InvalidFormat"));
            return false;
        }
        return true;
    }

    @FXML
    public void popTimeExample(MouseEvent mouseEvent) {
        if (timeType == null) {
            timeType = ColumnType.Datetime;
        }
        switch (timeType) {
            case Datetime:
                popMenu = PopTools.popDatetimeExamples(popMenu, timeInput, mouseEvent);
                break;
            case Date:
                popMenu = PopTools.popDateExamples(popMenu, timeInput, mouseEvent);
                break;
            case Era:
                popMenu = PopTools.popEraExamples(popMenu, timeInput, mouseEvent);
                break;

        }
    }

    public static DatetimeInputController open(BaseController parent, String title, String initValue, ColumnType timeType) {
        try {
            DatetimeInputController controller = (DatetimeInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.DatetimeInputFxml, true);
            controller.setParameters(parent, title, initValue, timeType);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
