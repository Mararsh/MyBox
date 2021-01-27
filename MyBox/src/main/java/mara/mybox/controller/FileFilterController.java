package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.StringFilterType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class FileFilterController extends BaseFileEditerController {

    @FXML
    protected ComboBox<String> filterTypeSelector;
    @FXML
    protected TextField filterConditionsLabel;
    @FXML
    protected Button exampleRegexButton;

    public FileFilterController() {
        baseTitle = AppVariables.message("FileFilter");

        setTextType();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFilterTab();

            filterTypeSelector.getItems().clear();
            for (StringFilterType type : StringFilterType.values()) {
                filterTypeSelector.getItems().add(message(type.name()));
            }
            filterTypeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldv, String newv) {
                    checkFilterType();
                }
            });
            filterTypeSelector.getSelectionModel().select(0);

            FxmlControl.setTooltip(filterTypeSelector, new Tooltip(AppVariables.message("FilterTypesComments")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkFilterType() {
        String selected = filterTypeSelector.getValue();
        for (StringFilterType type : StringFilterType.values()) {
            if (message(type.name()).equals(selected)) {
                filterType = type;
                break;
            }
        }
        if (filterType == FileEditInformation.StringFilterType.MatchRegularExpression
                || filterType == FileEditInformation.StringFilterType.NotMatchRegularExpression
                || filterType == FileEditInformation.StringFilterType.IncludeRegularExpression
                || filterType == FileEditInformation.StringFilterType.NotIncludeRegularExpression) {
            if (exampleRegexButton != null) {
                exampleRegexButton.setVisible(true);
            }
            FxmlControl.removeTooltip(filterInput);
        } else {
            if (exampleRegexButton != null) {
                exampleRegexButton.setVisible(false);
            }
            FxmlControl.setTooltip(filterInput, new Tooltip(message("SeparateByCommaBlanksInvolved")));
        }
    }

    public void filterFile(final FileEditInformation sourceInfo,
            String initConditions,
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
            filterConditions = initConditions + AppVariables.message("And") + conditions;
        } else {
            filterConditions = conditions;
        }
        filterConditionsLabel.setText(filterConditions);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private File file;

                @Override
                protected boolean handle() {
                    file = sourceInformation.filter(recordLineNumber);
                    return file != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (file.length() == 0) {
                        popInformation(AppVariables.message("NoData"));
                    } else {
                        openTextFile(file);
                        saveButton.setDisable(true);
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

    @FXML
    public void popRegexExample(MouseEvent mouseEvent) {
        popMenu = FxmlControl.popRegexExample(this, popMenu, filterInput, mouseEvent);
    }

    @FXML
    @Override
    public void saveAction() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter);
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
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return targetInformation.writePage(sourceInformation, mainArea.getText());
                }

                @Override
                protected void whenSucceeded() {
                    BaseFileEditerController controller = openNewStage();
                    controller.openFile(file);
                    popSuccessful();
                    FxmlStage.closeStage(getMyStage());
//                            sourceInformation.getFile().delete();
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
