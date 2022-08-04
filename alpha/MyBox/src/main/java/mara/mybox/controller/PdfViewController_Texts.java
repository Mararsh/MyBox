package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2021-8-8
 * @License Apache License Version 2.0
 */
public abstract class PdfViewController_Texts extends PdfViewController_OCR {

    protected PDFTextStripper stripper;
    protected String password;
    protected int textsPage;
    protected Task textsTask;

    @FXML
    protected Tab textsTab;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected Label textsLabel;

    @FXML
    public void extractTexts() {
        if (imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (textsTask != null) {
                textsTask.cancel();
            }
            textsTask = new SingletonTask<Void>(this) {

                protected String texts;

                @Override
                protected boolean handle() {
                    try ( PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
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
                    textsArea.setText(texts);
                    textsLabel.setText(message("CharactersNumber") + ": " + textsArea.getLength());
                    textsPage = frameIndex;
                }
            };
            start(textsTask, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
        }

    }

    @FXML
    public void editTexts() {
        if (textsArea.getText().isEmpty()) {
            popError(message("NoData"));
            return;
        }
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsArea.getText());
    }

}
