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
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-13
 * @License Apache License Version 2.0
 */
public class ExtractTextsFromMSController extends BaseBatchFileController {

    protected Charset charset;

    @FXML
    protected ComboBox<String> charsetSelector;

    public ExtractTextsFromMSController() {
        baseTitle = Languages.message("ExtractTextsFromMS");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.All, VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "Charset", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        File target = makeTargetFile(FileNameTools.prefix(srcFile.getName()), ".txt", targetPath);
        if (target == null) {
            return Languages.message("Skip");
        }
        String text = MicrosoftDocumentTools.extractText(srcFile);
        if (text == null || TextFileTools.writeFile(target, text, charset) == null) {
            return Languages.message("Failed");
        }
        targetFileGenerated(target);
        return Languages.message("Successful");
    }

}
