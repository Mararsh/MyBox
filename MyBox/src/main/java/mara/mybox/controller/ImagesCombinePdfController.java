package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @Author Mara
 * @CreateDate 2018-9-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombinePdfController extends BaseImagesListController {

    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected HBox targetBox;

    public ImagesCombinePdfController() {
        baseTitle = AppVariables.message("ImagesCombinePdf");

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = CommonFxValues.PdfExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pdfOptionsController.set(baseName, true);
            targetBox.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(pdfOptionsController.customWidthInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfOptionsController.customHeightInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfOptionsController.marginSelector.styleProperty().isEqualTo(badStyle))
                            .or(pdfOptionsController.jpegQualitySelector.styleProperty().isEqualTo(badStyle))
                            .or(pdfOptionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        if (tableController.hasSampled()) {
            if (!FxmlControl.askSure(getMyStage().getTitle(), message("SureSampled"))) {
                return;
            }
        }

        final File file = chooseSaveFile(VisitHistoryTools.getSavedPath(VisitHistory.FileType.PDF),
                null, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.PDF);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        try ( PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                            int count = 0;
                            int total = tableData.size();
                            for (Object source : tableData) {
                                if (task == null || isCancelled()) {
                                    document.close();
                                    return false;
                                }
                                ImageInformation imageInfo = (ImageInformation) source;
                                BufferedImage bufferedImage = ImageInformation.getBufferedImage(imageInfo);
                                if (bufferedImage != null) {
                                    PdfTools.writePage(document, imageInfo.getImageFormat(), bufferedImage,
                                            ++count, total, pdfOptionsController);
                                }
                            }
                            PdfTools.setValues(document, pdfOptionsController.authorInput.getText(),
                                    "MyBox v" + CommonValues.AppVersion, pdfOptionsController.zoom, 1.0f);
                            document.save(file);
                            document.close();
                        }
                        return file.exists();

                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (viewCheck.isSelected()) {
                        view(file);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
