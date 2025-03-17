package mara.mybox.controller;

import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class ControlMatrixOptions extends BaseController {

    protected Charset charset;
    protected boolean autoDetermine;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton doubleRadio, floatRadio, longRadio, intRadio, shortRadio, booleanRadio;

    public void setParameters(String name) {
        try {
            baseName = name;

//            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//                @Override
//                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
//
//                }
//            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String type() {
        if (floatRadio.isSelected()) {
            return "Float";
        } else if (longRadio.isSelected()) {
            return "Long";
        } else if (intRadio.isSelected()) {
            return "Integer";
        } else if (shortRadio.isSelected()) {
            return "Short";
        } else if (booleanRadio.isSelected()) {
            return "Boolean";
        } else {
            return "Double";
        }
    }

}
