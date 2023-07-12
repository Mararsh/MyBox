package mara.mybox.controller;

import com.ibm.icu.text.MessageFormat;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-10-29
 * @License Apache License Version 2.0
 */
public class MarkdownToHtmlController extends BaseBatchFileController {

    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataHolder htmlOptions;

    @FXML
    protected ComboBox<String> styleSelector;
    @FXML
    protected ControlMarkdownOptions optionsController;

    public MarkdownToHtmlController() {
        baseTitle = message("MarkdownToHtml");
        targetFileSuffix = "html";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> styles = new ArrayList<>();
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                styles.add(message(style.name()));
            }
            styleSelector.getItems().addAll(styles);
            styleSelector.getSelectionModel().select(UserConfig.getString(baseName + "HtmlStyleName", message("Default")));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    String styleValue;
                    if (newValue == null || newValue.equals(message("Default"))) {
                        styleValue = null;
                    } else {
                        styleValue = HtmlStyles.styleValue(newValue);
                    }
                    UserConfig.setString(baseName + "HtmlStyle", styleValue);
                    UserConfig.setString(baseName + "HtmlStyleName", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            htmlOptions = optionsController.options();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            Node document = htmlParser.parse(TextFileTools.readTexts(srcFile));
            String html = htmlRender.render(document);
            String style = UserConfig.getString(baseName + "HtmlStyle", null);
            html = HtmlWriteTools.html(null, style, html);

            TextFileTools.writeFile(target, html, Charset.forName("utf-8"));
            updateLogs(MessageFormat.format(message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
            targetFileGenerated(target);
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

}
