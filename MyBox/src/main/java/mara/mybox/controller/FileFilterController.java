package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileEditInformation;

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
        setTextType();
    }

    @Override
    protected void initializeNext() {
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
            filterConditions = initConditions + AppVaribles.getMessage("And") + conditions;
        } else {
            filterConditions = conditions;
        }
        filterConditionsLabel.setText(filterConditions);

        task = new Task<Void>() {
            private File file;

            @Override
            protected Void call() throws Exception {
                file = sourceInformation.filter(recordLineNumber);
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (file != null) {
                            if (file.length() == 0) {
                                popInformation(AppVaribles.getMessage("NoData"));
                            } else {
                                openTextFile(file);
                                saveButton.setDisable(true);
                            }
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
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
        final FileChooser fileChooser = new FileChooser();
        File path = AppVaribles.getUserConfigPath(FilePathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        AppVaribles.setUserConfigValue(FilePathKey, file.getParent());

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
                if (task.isCancelled()) {
                    return null;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            FileEditerController controller = openNewStage();
                            controller.openFile(file);
                            popInformation(AppVaribles.getMessage("Successful"));
                            getMyStage().close();
//                            sourceInformation.getFile().delete();
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
