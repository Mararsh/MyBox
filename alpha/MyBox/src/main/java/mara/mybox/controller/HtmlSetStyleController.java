package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class HtmlSetStyleController extends BaseBatchHtmlController {

    protected String css;

    @FXML
    protected TextArea cssArea;
    @FXML
    protected CheckBox ignoreCheck;

    public HtmlSetStyleController() {
        baseTitle = message("HtmlSetStyle");
    }

    @Override
    public void initOptionsSection() {
        cssArea.setText(HtmlStyles.DefaultStyle);

        ignoreCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "IgnoreOriginal", ignoreCheck.isSelected());
            }
        });
        ignoreCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreOriginal", false));
    }

    @Override
    public boolean makeMoreParameters() {
        css = cssArea.getText();
        if (css.isBlank()) {
            popError(Languages.message("InvalidParameters"));
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String covertHtml(File srcFile, Charset charset) {
        return HtmlWriteTools.setStyle(srcFile, charset, css, ignoreCheck.isSelected());
    }

}
