package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.FileEditInformation;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class FileFilterController extends FileEditerController {

    @FXML
    private TextField filterConditionsLabel;

    public FileFilterController() {
        baseTitle = AppVaribles.message("FileFilter");

        setTextType();
    }

    @Override
    public void initializeNext() {
        try {
            initFilterTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void filterFile(final FileEditInformation sourceInfo, String initConditions,
            final boolean recordLineNumber) {

        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getFilterStrings() == null
                || sourceInfo.getFilterStrings().length == 0) {
            return;
        }
        sourceInformation = FileEditInformation.newEditInformation(sourceInfo.getEditType(), sourceInfo.getFile());
        sourceInformation.setCharset(sourceInfo.getCharset());
        sourceInformation.setWithBom(sourceInfo.isWithBom());
        sourceInformation.setFilterStrings(sourceInfo.getFilterStrings());
        sourceInformation.setFilterType(sourceInfo.getFilterType());
        sourceInformation.setPageSize(sourceInfo.getPageSize());
        sourceInformation.setLineBreak(sourceInfo.getLineBreak());
        sourceInformation.setLineBreakValue(sourceInfo.getLineBreakValue());
        sourceInformation.setLineBreakWidth(sourceInfo.getLineBreakWidth());

        String conditions = " (" + sourceInformation.filterTypeName() + ":"
                + Arrays.asList(sourceInformation.getFilterStrings()) + ") ";
        if (!initConditions.isEmpty()) {
            filterConditions = initConditions + AppVaribles.message("And") + conditions;
        } else {
            filterConditions = conditions;
        }
        filterConditionsLabel.setText(filterConditions);

        task = new Task<Void>() {
            private File file;

            @Override
            protected Void call() throws Exception {
                file = sourceInformation.filter(recordLineNumber);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (file != null) {
                            if (file.length() == 0) {
                                popInformation(AppVaribles.message("NoData"));
                            } else {
                                openTextFile(file);
                                saveButton.setDisable(true);
                            }
                        } else {
                            popInformation(AppVaribles.message("failed"));
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

    @FXML
    @Override
    public void saveAction() {
        final File file = chooseSaveFile(AppVaribles.getUserConfigPath(sourcePathKey),
                null, targetExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file);

        targetInformation.setFile(file);
        targetInformation.setCharset(sourceInformation.getCharset());
        targetInformation.setWithBom(sourceInformation.isWithBom());
        targetInformation.setLineBreak(sourceInformation.getLineBreak());
        targetInformation.setLineBreakValue(sourceInformation.getLineBreakValue());
        targetInformation.setLineBreakWidth(sourceInformation.getLineBreakWidth());
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = targetInformation.writePage(sourceInformation, mainArea.getText());
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            FileEditerController controller = openNewStage();
                            controller.openFile(file);
                            popInformation(AppVaribles.message("Successful"));
                            FxmlStage.closeStage(getMyStage());
//                            sourceInformation.getFile().delete();
                        } else {
                            popInformation(AppVaribles.message("failed"));
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
