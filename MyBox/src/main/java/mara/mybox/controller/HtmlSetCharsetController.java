package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-10-27
 * @License Apache License Version 2.0
 */
public class HtmlSetCharsetController extends BaseBatchFileController {

    protected Charset charset;

    @FXML
    protected ComboBox<String> charsetSelector;

    public HtmlSetCharsetController() {
        baseTitle = AppVariables.message("HtmlSetCharset");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initOptionsSection() {
        List<String> names = TextTools.getCharsetNames();
        charsetSelector.getItems().addAll(names);
        try {
            charset = Charset.forName(AppVariables.getUserConfigValue(baseName + "Charset", Charset.defaultCharset().name()));
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }
        charsetSelector.setValue(charset.name());
        charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                charset = Charset.forName(newValue);
                AppVariables.setUserConfigValue(baseName + "Charset", charset.name());
            }
        });
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
            String changed = HtmlTools.setCharset(srcFile, charset, true);
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
