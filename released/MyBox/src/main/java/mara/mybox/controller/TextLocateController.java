package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class TextLocateController extends BaseChildController {

    protected BaseTextController fileController;
    protected long locateLine, locateObject;  // 0-based
    protected long from, to;  // 0-based, exlcuded end

    @FXML
    protected ToggleGroup locateGroup;
    @FXML
    protected RadioButton lineNumberRadio, objectLocationRadio, linesRangeRadio, objectRangeRadio;
    @FXML
    protected TextField lineNumberInput, objectLocationInput, lineFromInput, lineToInput,
            objectFromInput, objectToInput;

    public void setParameters(BaseTextController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.sourceInformation == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Locate") + " - " + fileController.getTitle());

            if (fileController.isBytes()) {
                objectLocationRadio.setText(message("ByteLocation"));
                objectRangeRadio.setText(message("BytesRange"));
            }

            locateLine = locateObject = from = to = -1;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkLineNumber() {
        if (!lineNumberRadio.isSelected()) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(lineNumberInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0 && v <= fileController.sourceInformation.getLinesNumber()) {
            locateLine = v - 1;  // 0-based
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("LineNumber"));
            return false;
        }
    }

    public boolean checkObjectLocation() {
        if (!objectLocationRadio.isSelected()) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(objectLocationInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0 && v <= fileController.sourceInformation.getObjectsNumber()) {
            locateObject = v - 1;  // 0-based
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + objectLocationRadio.getText());
            return false;
        }
    }

    public boolean checkLinesRange() {
        if (!linesRangeRadio.isSelected()) {
            return true;
        }
        long f, t;
        try {
            f = Long.parseLong(lineFromInput.getText()) - 1;
        } catch (Exception e) {
            f = -1;
        }
        if (f < 0 || f >= fileController.sourceInformation.getLinesNumber()) {
            popError(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }

        try {
            t = Long.parseLong(lineToInput.getText());
        } catch (Exception e) {
            t = -1;
        }
        if (t < 0 || t > fileController.sourceInformation.getLinesNumber() || f > t) {
            popError(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }

        from = f;
        to = t;
        return true;
    }

    public boolean checkObjectsRange() {
        if (!objectRangeRadio.isSelected()) {
            return true;
        }
        long f, t;
        try {
            f = Long.parseLong(objectFromInput.getText()) - 1;
        } catch (Exception e) {
            f = -1;
        }
        if (f < 0 || f >= fileController.sourceInformation.getObjectsNumber()) {
            popError(message("InvalidParameters") + ": " + objectRangeRadio.getText());
            return false;
        }

        try {
            t = Long.parseLong(objectToInput.getText());
        } catch (Exception e) {
            t = -1;
        }
        if (t < 0 || t > fileController.sourceInformation.getObjectsNumber() || f > t) {
            popError(message("InvalidParameters") + ": " + objectRangeRadio.getText());
            return false;
        }

        from = f;
        to = t;
        return true;
    }

    public boolean checkValues() {
        return checkLineNumber()
                && checkObjectLocation()
                && checkLinesRange()
                && checkObjectsRange();
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkValues()) {
            return;
        }
        boolean ok;
        if (lineNumberRadio.isSelected()) {
            ok = fileController.locateLine(locateLine);

        } else if (objectLocationRadio.isSelected()) {
            ok = fileController.locateObject(locateObject);

        } else if (linesRangeRadio.isSelected()) {
            ok = fileController.locateLinesRange(from, to);

        } else if (objectRangeRadio.isSelected()) {
            ok = fileController.locateObjectsRange(from, to);
        } else {
            return;
        }
        if (ok && closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static TextLocateController open(BaseTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            TextLocateController controller = (TextLocateController) WindowTools.branchStage(
                    parent, Fxmls.TextLocateFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
