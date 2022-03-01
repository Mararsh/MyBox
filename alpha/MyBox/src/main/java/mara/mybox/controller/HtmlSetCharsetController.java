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
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;

import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
        baseTitle = Languages.message("HtmlSetCharset");
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
            charset = Charset.forName(UserConfig.getString(baseName + "Charset", Charset.defaultCharset().name()));
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }
        charsetSelector.setValue(charset.name());
        charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                charset = Charset.forName(newValue);
                UserConfig.setString(baseName + "Charset", charset.name());
            }
        });
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.suffix(file.getName());
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
            String changed = HtmlWriteTools.setCharset(srcFile, charset, true);
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
            String namePrefix = FileNameTools.prefix(sourceFile.getName());
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
