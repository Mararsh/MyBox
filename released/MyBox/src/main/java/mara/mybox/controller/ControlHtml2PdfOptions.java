package mara.mybox.controller;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-10
 * @License Apache License Version 2.0
 */
public class ControlHtml2PdfOptions extends BaseController {

    protected DataHolder pdfOptions;

    @FXML
    protected ControlTTFSelecter ttfController;
    @FXML
    protected TextArea cssArea;
    @FXML
    protected CheckBox ignoreHeadCheck;

    public ControlHtml2PdfOptions() {
    }

    public void setControls(String baseName, boolean careHead) {
        try {
            this.baseName = baseName;

            ttfController.name(baseName);
            ttfController.ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isBlank()) {
                        return;
                    }
                    checkTtf();
                }
            });
            checkTtf();

            ignoreHeadCheck.setVisible(careHead);
            if (careHead) {
                ignoreHeadCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreHead", true));
                ignoreHeadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "IgnoreHead", ignoreHeadCheck.isSelected());
                    }
                });
            }

            // https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/java/samples/PdfConverter.java
            pdfOptions = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL
                    & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP), TocExtension.create())
                    .toMutable()
                    .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
                    .toImmutable();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkTtf() {
        String value = ttfController.ttfSelector.getValue();
        int pos = value.indexOf("    ");
        String ttf;
        if (pos < 0) {
            ttf = value;
        } else {
            ttf = value.substring(0, pos);
        }
        String css = "@font-face {\n"
                + "  font-family: 'myFont';\n"
                + "  src: url('file:///" + ttf.replaceAll("\\\\", "/") + "');\n"
                + "  font-weight: normal;\n"
                + "  font-style: normal;\n"
                + "}\n"
                + " body { font-family:  'myFont';}";
        cssArea.setText(css);
    }

    public String html2pdf(FxTask currentTask, String html, File target) {
        return PdfTools.html2pdf(currentTask, target, html, cssArea.getText().trim(),
                ignoreHeadCheck.isVisible() && ignoreHeadCheck.isSelected(), pdfOptions);
    }

}
