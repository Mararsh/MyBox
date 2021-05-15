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
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;

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
        baseTitle = AppVariables.message("ExtractTextsFromMS");
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
                charset = Charset.forName(AppVariables.getUserConfigValue(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(FileTools.namePrefix(srcFile.getName()), ".txt", targetPath);
        if (target == null) {
            return AppVariables.message("Skip");
        }
        try ( POITextExtractor extractor = ExtractorFactory.createExtractor(srcFile)) {
            String text = extractor.getText();
            FileTools.writeFile(target, text, charset);
            targetFileGenerated(target);
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
        return AppVariables.message("Successful");
    }

}
