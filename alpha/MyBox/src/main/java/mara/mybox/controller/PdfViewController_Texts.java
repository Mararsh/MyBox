package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public abstract class PdfViewController_Texts extends PdfViewController_OCR {

    protected PDFTextStripper stripper;
    protected int textsPage;
    protected Task textsTask;

    @FXML
    protected TextArea textsArea;
    @FXML
    protected Label textsLabel;
    @FXML
    protected CheckBox wrapTextsCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapTexts", true));
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapTexts", newValue);
                    textsArea.setWrapText(newValue);
                }
            });
            textsArea.setWrapText(wrapTextsCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void extractTexts(boolean pop) {
        if (imageView.getImage() == null) {
            return;
        }
        if (textsTask != null) {
            textsTask.cancel();
        }
        textsTask = new FxTask<Void>(this) {

            protected String texts;

            @Override
            protected boolean handle() {
                try (PDDocument doc = Loader.loadPDF(sourceFile, password)) {
                    if (stripper == null) {
                        stripper = new PDFTextStripper();
                    }
                    stripper.setStartPage(frameIndex + 1);  // 1-based
                    stripper.setEndPage(frameIndex + 1);
                    texts = stripper.getText(doc);
                    doc.close();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (pop) {
                    TextPopController.loadText(texts);
                } else {
                    textsArea.setText(texts);
                    textsLabel.setText(message("CharactersNumber") + ": " + textsArea.getLength());
                    textsPage = frameIndex;
                }
            }
        };
        start(textsTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
    }

    @FXML
    public void editTexts() {
        if (textsArea.getText().isEmpty()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController.edit(textsArea.getText());
    }

}
