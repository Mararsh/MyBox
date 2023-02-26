package mara.mybox.controller;

import com.ibm.icu.text.MessageFormat;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
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
    protected int indentSize = 4;

    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;
    @FXML
    protected TextField titleInput;

    public MarkdownToHtmlController() {
        baseTitle = Languages.message("MarkdownToHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(UserConfig.getString(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    UserConfig.setString(baseName + "Emulation", newValue);
                }
            });

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(UserConfig.getString(baseName + "Indent", "4"));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            UserConfig.setString(baseName + "Indent", newValue);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(UserConfig.getBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Indent", trimCheck.isSelected());
                }
            });

            appendCheck.setSelected(UserConfig.getBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Append", appendCheck.isSelected());
                }
            });

            discardCheck.setSelected(UserConfig.getBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Discard", discardCheck.isSelected());
                }
            });

            linesCheck.setSelected(UserConfig.getBoolean(baseName + "Trim", false));
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Trim", linesCheck.isSelected());
                }
            });

            List<String> styles = new ArrayList<>();
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                styles.add(Languages.message(style.name()));
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
            htmlOptions = MarkdownTools.htmlOptions(emulationSelector.getValue(), indentSize,
                    trimCheck.isSelected(), discardCheck.isSelected(), appendCheck.isSelected());
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.suffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "md".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            Node document = htmlParser.parse(TextFileTools.readTexts(srcFile));
            String html = htmlRender.render(document);
            String style = UserConfig.getString(baseName + "HtmlStyle", null);
            html = HtmlWriteTools.html(titleInput.getText(), style, html);

            TextFileTools.writeFile(target, html, Charset.forName("utf-8"));
            updateLogs(MessageFormat.format(Languages.message("ConvertSuccessfully"),
                    srcFile.getAbsolutePath(), target.getAbsolutePath()));
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
