package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class DocumentController extends BaseController {

    @FXML
    protected Button overviewButton, PDFButton, imageButton, networkButton, desktopButton, devButton;
    @FXML
    protected Label label;

    public DocumentController() {
        baseTitle = message("Documents");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            label.setText(MessageFormat.format(message("DocumentComments"), AppPaths.getDownloadsPath()));
            checkStatus();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkStatus() {
        String userGuideVersion = "5.0", devGuideVersion = "2.1";
        String lang = Languages.getLanguage();
        String docPath = AppPaths.getDownloadsPath() + File.separator;
        String webPath = "https://mararsh.github.io/MyBox_documents/" + lang + "/";

        String fileName = "MyBox-UserGuide-" + userGuideVersion + "-Overview-" + lang + ".pdf";
        File file = new File(docPath + fileName);
        setButton(overviewButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + userGuideVersion + "-PdfTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(PDFButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + userGuideVersion + "-ImageTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(imageButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + userGuideVersion + "-NetworkTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(networkButton, file, webPath + fileName);

        fileName = "MyBox-UserGuide-" + userGuideVersion + "-DesktopTools-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(desktopButton, file, webPath + fileName);

        fileName = "MyBox-DevGuide-" + devGuideVersion + "-" + lang + ".pdf";
        file = new File(docPath + fileName);
        setButton(devButton, file, webPath + fileName);

    }

    protected void setButton(Button button, File file, String address) {
        try {
            if (file.exists()) {
                button.setText("");
                button.setGraphic(StyleTools.getIconImage("iconView.png"));
                NodeStyleTools.setTooltip(button, new Tooltip(message("View")));
                button.setDisable(false);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ControllerTools.openPdfViewer(null, file);
                    }
                });
            } else {
                button.setText("");
                button.setGraphic(StyleTools.getIconImage("iconDownload.png"));
                NodeStyleTools.setTooltip(button, new Tooltip(message("Download")));
                button.setDisable(false);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        browse(address);
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void openFolder() {
        view(AppPaths.getDownloadsPath());
    }

}
