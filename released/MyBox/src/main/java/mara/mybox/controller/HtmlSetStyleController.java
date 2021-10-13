package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class HtmlSetStyleController extends BaseBatchFileController {

    protected String css;

    @FXML
    protected TextArea cssArea;
    @FXML
    protected CheckBox ignoreCheck;

    public HtmlSetStyleController() {
        baseTitle = Languages.message("HtmlSetStyle");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initOptionsSection() {
        cssArea.setText(HtmlStyles.BaseStyle);

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
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            Charset charset = TextFileTools.charset(srcFile);
            String changed = HtmlWriteTools.setStyle(srcFile, charset, css, ignoreCheck.isSelected());
            if (changed == null) {
                return Languages.message("Failed");
            }
            TextFileTools.writeFile(target, changed, charset);
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".html";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
