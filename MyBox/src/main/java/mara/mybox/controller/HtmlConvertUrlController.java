package mara.mybox.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-24
 * @License Apache License Version 2.0
 */
public class HtmlConvertUrlController extends BaseController {

    @FXML
    private RadioButton decodeRadio, encodeRadio;
    @FXML
    private TextField valueInput, resultInput;
    @FXML
    private ComboBox<String> charsetsSelector;
    @FXML
    private Button equalButton;

    public HtmlConvertUrlController() {
        baseTitle = AppVariables.message("HtmlConvertUrl");
        TipsLabelKey = "ConvertUrlTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> setNames = TextTools.getCharsetNames();
            charsetsSelector.getItems().addAll(setNames);
            charsetsSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Charset", "UTF-8"));
            charsetsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "Charset", newValue);
                }
            });

            equalButton.disableProperty().bind(valueInput.textProperty().isEmpty());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void equalAction() {
        try {
            if (decodeRadio.isSelected()) {
                resultInput.setText(URLDecoder.decode(valueInput.getText(), charsetsSelector.getValue()));
            } else if (encodeRadio.isSelected()) {
                resultInput.setText(URLEncoder.encode(valueInput.getText(), charsetsSelector.getValue()));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = Clipboard.getSystemClipboard().getString();
        if (string != null && !string.isBlank()) {
            valueInput.setText(string);
        }
    }

    @FXML
    @Override
    public void copyAction() {
        try {
            String string = resultInput.getText();
            if (string != null && !string.isBlank()) {
                ClipboardContent cc = new ClipboardContent();
                cc.putString(string);
                Clipboard.getSystemClipboard().setContent(cc);
                popInformation(message("CopySuccessfully"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
