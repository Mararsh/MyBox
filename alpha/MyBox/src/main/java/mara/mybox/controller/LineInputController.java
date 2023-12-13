package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-8-2
 * @License Apache License Version 2.0
 */
public class LineInputController extends BaseInputController {

    protected List<DoublePoint> line, picked;

    @FXML
    protected TextArea textArea;

    public void setParameters(BaseController parent, String title, List<DoublePoint> inLine) {
        try {
            super.setParameters(parent, title);

            line = inLine;
            recoverButton.setVisible(line != null);
            if (line != null) {
                textArea.setText(DoublePoint.imageCoordinatesToText(line, "\n"));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @FXML
    @Override
    public boolean checkInput() {
        picked = null;
        picked = DoublePoint.parseImageCoordinates(textArea.getText());
        if (picked == null || picked.isEmpty()) {
            popError(message("InvalidValue"));
            return false;
        }
        return true;
    }

    @FXML
    @Override
    public void recoverAction() {
        if (line == null) {
            return;
        }
        textArea.setText(DoublePoint.imageCoordinatesToText(line, "\n"));
    }

    public static LineInputController open(BaseController parent,
            String title, List<DoublePoint> line) {
        try {
            LineInputController controller = (LineInputController) WindowTools.childStage(
                    parent, Fxmls.LineInputFxml);
            controller.setParameters(parent, title, line);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
