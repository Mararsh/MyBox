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
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class HtmlSetStyleController extends BaseBatchFileController {

    protected Charset charset;
    protected String css;

    @FXML
    protected TextArea cssArea;
    @FXML
    protected CheckBox ignoreCheck;

    public HtmlSetStyleController() {
        baseTitle = AppVariables.message("HtmlSetStyle");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initOptionsSection() {
        cssArea.setText(HtmlTools.BaseStyle);

        ignoreCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                AppVariables.setUserConfigValue(baseName + "IgnoreOriginal", ignoreCheck.isSelected());
            }
        });
        ignoreCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "IgnoreOriginal", false));
    }

    @Override
    public boolean makeMoreParameters() {
        css = cssArea.getText();
        if (css.isBlank()) {
            popError(message("InvalidParameters"));
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
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
                return AppVariables.message("Skip");
            }
            String changed = HtmlTools.setStyle(srcFile, css, ignoreCheck.isSelected());
            if (changed == null) {
                return AppVariables.message("Failed");
            }
            FileTools.writeFile(target, changed, charset);
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
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
