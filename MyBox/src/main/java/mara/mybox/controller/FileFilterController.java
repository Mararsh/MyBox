package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileEditInformation;
import mara.mybox.objects.FileEditInformationFactory;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class FileFilterController extends FileEditerController {

    private boolean askSave;

    @FXML
    private TextField filterConditionsLabel;
    @FXML
    private CheckBox askCheck;

    public FileFilterController() {
        editType = Edit_Type.Text;
        saveAsType = SaveAsType.Load;
        askSave = true;

        FilePathKey = "TextFilePathKey";
        DisplayKey = "TextEditerDisplayHex";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            initFilterTab();
            askCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("FilterAsk", askCheck.isSelected());
                }
            });
            askCheck.setSelected(AppVaribles.getUserConfigBoolean("FilterAsk", true));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void filterFile(final FileEditInformation sourceInfo, String initConditions) {
        if (sourceInfo.getFilterStrings() == null
                || sourceInfo.getFilterStrings().length == 0) {
            return;
        }
        sourceInformation = FileEditInformationFactory.newEditInformation(sourceInfo.getEditType(), sourceInfo.getFile());
        sourceInformation.setCharset(sourceInfo.getCharset());
        sourceInformation.setFilterStrings(sourceInfo.getFilterStrings());
        sourceInformation.setFilterInclude(sourceInfo.isFilterInclude());
        sourceInformation.setPageSize(sourceInfo.getPageSize());
        sourceInformation.setLineBreak(sourceInfo.getLineBreak());
        sourceInformation.setWithBom(sourceInfo.isWithBom());
        askSave = true;

//        sourceInformation.setObjectsNumber(sourceInfo.getObjectsNumber());
//        sourceInformation.setLinesNumber(sourceInfo.getLinesNumber());
        String conditions;
        if (sourceInformation.isFilterInclude()) {
            conditions = " (" + AppVaribles.getMessage("IncludeOneOf") + ":"
                    + Arrays.asList(sourceInformation.getFilterStrings()) + ") ";
        } else {
            conditions = " (" + AppVaribles.getMessage("NotIncludeAnyOf") + ":"
                    + Arrays.asList(sourceInformation.getFilterStrings()) + ") ";
        }
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
                file = sourceInformation.filter();
                if (task.isCancelled()) {
                    return null;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (file != null) {
                            if (file.length() == 0) {
                                popInformation(AppVaribles.getMessage("Nothing"));
                            } else {
                                openFile(file);
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
    protected void filterAction() {
        askSave = false;
        super.filterAction();
    }

    @FXML
    @Override
    protected void saveAction() {
        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigValue(FilePathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(LastPathKey, file.getParent());
        AppVaribles.setUserConfigValue(FilePathKey, file.getParent());

        targetInformation.setFile(file);
        targetInformation.setCharset(sourceInformation.getCharset());
        targetInformation.setLineBreak(sourceInformation.getLineBreak());
        targetInformation.setWithBom(sourceInformation.isWithBom());
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
                            final FileEditerController controller = (FileEditerController) openStage(CommonValues.TextEditerFxml,
                                    AppVaribles.getMessage("TextEncoding"), false, true);
                            sourceInformation.setCurrentPage(1);
                            controller.openFile(file);
                            popInformation(AppVaribles.getMessage("Successful"));
                            askSave = false;
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

    @Override
    public boolean checkSavingForNextAction() {
        if (!askCheck.isSelected()) {
            return true;
        }
        if (!askSave) {
            askSave = true;
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVaribles.getMessage("AskSaveFilterResults"));
        ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
        ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        ButtonType buttonNotAsk = new ButtonType(AppVaribles.getMessage("NotAskAnyMore"));
        alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonNotAsk, buttonCancel
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            saveAction();
            return true;
        } else if (result.get() == buttonNotAsk) {
            askCheck.setSelected(false);
            return true;
        } else if (result.get() == buttonNotSave) {
            return true;
        } else {
            return false;
        }

    }

}
