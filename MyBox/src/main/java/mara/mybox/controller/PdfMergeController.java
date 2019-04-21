package mara.mybox.controller;

import mara.mybox.controller.base.PdfBatchBaseController;
import mara.mybox.fxml.FxmlStage;
import java.io.File;
import java.util.Calendar;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import static mara.mybox.value.AppVaribles.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfMergeController extends PdfBatchBaseController {

    final private String AuthorKey;

    @FXML
    private Button openTargetButton;
    @FXML
    private TextField authorInput;
    @FXML
    private CheckBox deleteCheck;
    @FXML
    protected HBox pdfMemBox;
    @FXML
    private ToggleGroup pdfMemGroup;
    @FXML
    private RadioButton pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;

    public PdfMergeController() {
        baseTitle = AppVaribles.getMessage("MergePdf");

        AuthorKey = "AuthorKey";
    }

    @Override
    public void initializeNext2() {
        try {
            allowPaused = false;

            initTargetSection();
            initOptionsSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    openTargetButton.setDisable(true);
                    try {
                        targetFile = new File(newValue);
                        if (!newValue.toLowerCase().endsWith(".pdf")) {
                            targetFile = null;
                            targetFileInput.setStyle(badStyle);
                            return;
                        }
                        targetFileInput.setStyle(null);
                        AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
                    } catch (Exception e) {
                        targetFile = null;
                        targetFileInput.setStyle(badStyle);
                    }
                }
            });
            saveButton.disableProperty().bind(Bindings.isEmpty(filesTableController.filesTableView.getItems())
                    .or(Bindings.isEmpty(targetFileInput.textProperty()))
                    .or(targetFileInput.styleProperty().isEqualTo(badStyle))
            );

            FxmlControl.quickTooltip(saveButton, new Tooltip("ENTER / F2 / CTRL+s"));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initOptionsSection() {

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                AppVaribles.setUserConfigValue(AuthorKey, newValue);
            }
        });
        authorInput.setText(AppVaribles.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

        Tooltip tips = new Tooltip(getMessage("PdfMemComments"));
        tips.setFont(new Font(16));
        FxmlControl.quickTooltip(pdfMemBox, tips);

        checkPdfMem();

    }

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    protected void openTargetAction(ActionEvent event) {
        if (!targetFile.exists()) {
            openTargetButton.setDisable(true);
            return;
        }
        openTargetButton.setDisable(false);
        FxmlStage.openTarget(getClass(), null, targetFile.getAbsolutePath());
    }

    @FXML
    @Override
    public void saveAction() {
        sourceFilesInformation = filesTableController.sourceFilesInformation;
        if (sourceFilesInformation == null || sourceFilesInformation.isEmpty()
                || targetFile == null) {
            return;
        }
        task = new Task<Void>() {
            private boolean ok;
            private PDDocument document;
            private String errorString;

            @Override
            protected Void call() throws Exception {
                try {
                    final MemoryUsageSetting memSettings = AppVaribles.pdfMemUsage.setTempDir(AppVaribles.getUserTempPath());

                    PDFMergerUtility mergePdf = new PDFMergerUtility();
                    for (FileInformation source : sourceFilesInformation) {
                        mergePdf.addSource(source.getFile());
                    }
                    mergePdf.setDestinationFileName(targetFile.getAbsolutePath());
                    mergePdf.mergeDocuments(memSettings);

                    try (PDDocument doc = PDDocument.load(targetFile, memSettings)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(authorInput.getText());
                        doc.setDocumentInformation(info);
                        document = doc;

                        PDPage page = doc.getPage(0);
                        PDPageXYZDestination dest = new PDPageXYZDestination();
                        dest.setPage(page);
                        dest.setZoom(1f);
                        dest.setTop((int) page.getCropBox().getHeight());
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        doc.getDocumentCatalog().setOpenAction(action);

                        doc.save(targetFile);
                    }

                    if (deleteCheck.isSelected()) {
                        for (FileInformation source : sourceFilesInformation) {
                            try {
                                source.getFile().delete();
                            } catch (Exception e) {
                            }
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                filesTableController.clearAction();
                            }
                        });
                    }

                    ok = true;

                } catch (Exception e) {
                    errorString = e.toString();
                    logger.error(e.toString());
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ok && targetFile.exists()) {
                                FxmlStage.openTarget(getClass(), null, targetFile.getAbsolutePath());
                                openTargetButton.setDisable(false);
                            } else {
                                popError(errorString);
                            }
                        } catch (Exception e) {
                            logger.error(e.toString());
                            popError(e.toString());
                        }
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
