package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class DocumentController extends BaseController {

    @FXML
    private Button overviewButton, pdfButton, imageButton, networkButton, desktopButton, devButton;
    @FXML
    protected Label label;

    public DocumentController() {
        baseTitle = AppVariables.message("Documents");
    }

    @Override
    public void initializeNext() {
        try {
            label.setText(MessageFormat.format(message("DocumentComments"), AppVariables.MyBoxDownloadsPath));
            checkStatus();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkStatus() {
        String docVersion = "5.0";
        String lang = AppVariables.getLanguage();
        String docPath = AppVariables.MyBoxDownloadsPath + File.separator;
        String webPath = "https://mararsh.github.io/MyBox_documents/" + lang + "/";

        String fileName = "MyBox-UserGuide-" + docVersion + "-Overview-" + lang + ".pdf";
        File file = new File(docPath + fileName);
        setButton(overviewButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + docVersion + "-PdfTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(pdfButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + docVersion + "-ImageTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(imageButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + docVersion + "-NetworkTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(networkButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + docVersion + "-DesktopTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(desktopButton, file, webPath + fileName);

        fileName = "MyBox-DevGuide-2.0-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(devButton, file, webPath + fileName);

    }

    protected void setButton(Button button, File file, String address) {
        try {

            File tmp = new File(file.getAbsolutePath() + ".downloading");
            if (tmp.exists()) {
                button.setText(message("Downloading"));
                button.setDisable(true);
            } else if (file.exists()) {
                button.setText(message("Open"));
                button.setDisable(false);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        FxmlStage.openPdfViewer(null, file);
                    }
                });
            } else {
                button.setText(message("Download"));
                button.setDisable(false);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        DownloadController controller
                                = (DownloadController) openScene(null, CommonValues.DownloadFxml);
                        controller.download(address);
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
