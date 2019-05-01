package mara.mybox.controller.base;

import mara.mybox.controller.FilesBatchController;
import java.io.File;
import java.util.ArrayList;
import javafx.fxml.FXML;
import mara.mybox.controller.PdfSourceSelectionController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class PdfBatchBaseController extends FilesBatchController {

    public String PdfSourceFromKey, PdfSourceToKey;

    @FXML
    public PdfSourceSelectionController sourceSelectionController;

    public PdfBatchBaseController() {
        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetPathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.PDF;
        AddPathType = VisitHistory.FileType.PDF;

        targetPathKey = "PdfTargetPath";
        creatSubdirKey = "PdfCreatSubdir";
        fillZeroKey = "PdfFillZero";
        previewKey = "PdfPreview";
        sourcePathKey = "PdfSourcePath";
        appendColorKey = "PdfAppendColor";
        appendCompressionTypeKey = "PdfAppendCompressionType";
        appendDensityKey = "PdfAppendDensity";
        appendQualityKey = "PdfAppendQuality";
        appendSizeKey = "PdfAppendSize";

        PdfSourceFromKey = "PdfSourceFromKey";
        PdfSourceToKey = "PdfSourceToKey";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            allowPaused = true;

            if (sourceSelectionController != null) {
                sourceSelectionController.parentController = this;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        super.sourceFileChanged(file);

        String filename = file.getName();
        if (targetPrefixInput != null) {
            targetPrefixInput.setText(FileTools.getFilePrefix(filename));
        }
        if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
            targetPathInput.setText(FileTools.getFilePath(filename));
        }
    }

    @FXML
    @Override
    public void previewAction() {
        isPreview = true;
        makeActualParameters();
        previewParameters = copyParameters(actualParameters);
        int page = 0;
        if (previewInput != null) {
            try {
                page = Integer.parseInt(previewInput.getText()) - 1;
            } catch (Exception e) {
                page = 0;
            }
            if (page < 0 || page > sourceSelectionController.pdfInformation.getNumberOfPages()) {
                page = 0;
                previewInput.setText("1");
            }
        }
        previewParameters.fromPage = page;
        previewParameters.startPage = page;
        previewParameters.toPage = page;
        previewParameters.acumStart = previewParameters.acumFrom;
        previewParameters.currentPage = 0;
        previewParameters.status = "start";
        currentParameters = previewParameters;
        doCurrentProcess();
    }

    @Override
    public void makeSingleParameters() {
        actualParameters.isBatch = false;
        
        actualParameters.sourceFile = sourceSelectionController.pdfInformation.getFile();
        sourceFiles = new ArrayList();
        sourceFiles.add(actualParameters.sourceFile);
        logger.debug(sourceFiles.size());

        actualParameters.fromPage = sourceSelectionController.readFromPage() - 1; // Interface From 1, actual from 0
//        AppVaribles.setUserConfigInt(PdfSourceFromKey, actualParameters.fromPage);
        actualParameters.toPage = sourceSelectionController.readToPage() - 1;  // Interface From 1, actual from 0
//        AppVaribles.setUserConfigInt(PdfSourceFromKey, actualParameters.toPage);
        actualParameters.currentNameNumber = actualParameters.acumFrom;
        actualParameters.password = sourceSelectionController.readPassword();
        actualParameters.startPage = actualParameters.fromPage;
        if (acumFromInput != null) {
            actualParameters.acumFrom = FxmlControl.getInputInt(acumFromInput);
            actualParameters.acumStart = actualParameters.acumFrom;
            actualParameters.acumDigit
                    = ((actualParameters.acumFrom + actualParameters.toPage - actualParameters.fromPage + 1) + "").length();
        }

    }

}
