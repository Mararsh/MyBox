package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
    @FXML
    protected Label infoLabel;

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

            lineNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkLineNumber();
                }
            });

            objectLocationInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkObjectLocation();
                }
            });

            lineFromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkLinesRange();
                }
            });

            lineToInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkLinesRange();
                }
            });

            objectFromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkObjectsRange();
                }
            });

            objectToInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkObjectsRange();
                }
            });

            locateGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkValues();
                }
            });

            checkValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkLineNumber() {
        setInfo(null);
        if (!lineNumberRadio.isSelected()) {
            lineNumberInput.setStyle(null);
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
            lineNumberInput.setStyle(null);
            return true;
        } else {
            lineNumberInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameter") + ": " + message("LineNumber"));
            return false;
        }
    }

    public boolean checkObjectLocation() {
        setInfo(null);
        if (!objectLocationRadio.isSelected()) {
            objectLocationInput.setStyle(null);
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
            objectLocationInput.setStyle(null);
            return true;
        } else {
            objectLocationInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameter") + ": " + objectLocationRadio.getText());
            return false;
        }
    }

    public boolean checkLinesRange() {
        setInfo(null);
        if (!linesRangeRadio.isSelected()) {
            lineFromInput.setStyle(null);
            lineToInput.setStyle(null);
            return true;
        }
        long f, t;
        try {
            f = Long.parseLong(lineFromInput.getText()) - 1;
        } catch (Exception e) {
            f = -1;
        }
        if (f < 0 || f >= fileController.sourceInformation.getLinesNumber()) {
            lineFromInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }
        lineFromInput.setStyle(null);

        try {
            t = Long.parseLong(lineToInput.getText());
        } catch (Exception e) {
            t = -1;
        }
        if (t < 0 || t > fileController.sourceInformation.getLinesNumber() || f > t) {
            lineToInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameters") + ": " + message("LinesRange"));
            return false;
        }
        lineToInput.setStyle(null);

        from = f;
        to = t;
        return true;
    }

    public boolean checkObjectsRange() {
        setInfo(null);
        if (!objectRangeRadio.isSelected()) {
            objectFromInput.setStyle(null);
            objectToInput.setStyle(null);
            return true;
        }
        long f, t;
        try {
            f = Long.parseLong(objectFromInput.getText()) - 1;
        } catch (Exception e) {
            f = -1;
        }
        if (f < 0 || f >= fileController.sourceInformation.getObjectsNumber()) {
            objectFromInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameters") + ": " + objectRangeRadio.getText());
            return false;
        }
        objectFromInput.setStyle(null);

        try {
            t = Long.parseLong(objectToInput.getText());
        } catch (Exception e) {
            t = -1;
        }
        if (t < 0 || t > fileController.sourceInformation.getObjectsNumber() || f > t) {
            objectToInput.setStyle(UserConfig.badStyle());
            setInfo(message("InvalidParameters") + ": " + objectRangeRadio.getText());
            return false;
        }
        objectToInput.setStyle(null);

        from = f;
        to = t;
        return true;
    }

    public boolean checkValues() {
        setInfo(null);
        return checkLineNumber()
                && checkObjectLocation()
                && checkLinesRange()
                && checkObjectsRange();
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkValues()) {
            popError(message("InvalidParameters"));
            return;
        }
        setInfo(null);
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
