package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-27
 * @License Apache License Version 2.0
 */
public class HtmlSetCharsetController extends BaseBatchHtmlController {

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
    public Charset chartset(File srcFile) {
        return charset;
    }

    @Override
    public String covertHtml(FxTask currentTask, File srcFile, Charset charset) {
        String html = TextFileTools.readTexts(currentTask, srcFile);
        if (currentTask == null || !currentTask.isWorking()) {
            return message("Canceled");
        }
        if (html == null) {
            return message("Failed");
        }
        return HtmlWriteTools.setCharset(currentTask, html, charset);
    }

}
