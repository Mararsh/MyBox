package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-13
 * @License Apache License Version 2.0
 */
public class HtmlSetEquivController extends BaseBatchHtmlController {

    protected String key, value;

    @FXML
    protected ComboBox<String> keySelector;
    @FXML
    protected TextField valueInput;

    public HtmlSetEquivController() {
        baseTitle = message("HtmlSetEquiv");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initOptionsSection() {
        keySelector.getItems().addAll(Arrays.asList(
                "cache-control", "content-type", "expires",
                "keywords", "description", "refresh", "set-cookie", "pragma"));
        key = UserConfig.getString(baseName + "KeyName", "cache-control");
        keySelector.setValue(key);
        keySelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue v, String ov, String nv) {
                checkKey(nv);
            }
        });
        checkKey(keySelector.getValue());
    }

    public void checkKey(String k) {
        if (k == null || k.isBlank()) {
            popError(message("InvalidParameters"));
            return;
        }
        key = k;
        if ("cache-control".equalsIgnoreCase(key)) {
            valueInput.setText("no-cache");
        } else if ("content-type".equalsIgnoreCase(key)) {
            valueInput.setText("text/html;charset=utf-8");
        } else if ("expires".equalsIgnoreCase(key)) {
            valueInput.setText("0");
        } else if ("keywords".equalsIgnoreCase(key)) {
            valueInput.setText("red,yellow,blue");
        } else if ("description".equalsIgnoreCase(key)) {
            valueInput.setText("hello");
        } else if ("refresh".equalsIgnoreCase(key)) {
            valueInput.setText("2；URL=https://sourceforge.net");
        } else if ("set-cookie".equalsIgnoreCase(key)) {
            valueInput.setText("Mon,12 May 2001 10:10:00GMT");
        } else if ("pragma".equalsIgnoreCase(key)) {
            valueInput.setText("no-cache");
        }
        UserConfig.setString(baseName + "KeyName", key);
    }

    @Override
    public boolean makeMoreParameters() {
        key = keySelector.getValue();
        value = valueInput.getText();
        if (key == null || key.isBlank() || value == null || value.isBlank()) {
            popError(message("InvalidParameters"));
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String covertHtml(File srcFile, Charset charset) {
        return HtmlWriteTools.setEquiv(srcFile, charset, key, value);
    }

}
