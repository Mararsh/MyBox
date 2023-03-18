package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-8
 * @License Apache License Version 2.0
 */
public class ControlSynchronizeOptions extends BaseController {

    protected FileSynchronizeAttributes copyAttr;

    @FXML
    protected VBox conditionsBox;
    @FXML
    protected TextField notCopyInput;
    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected RadioButton conditionallyRadio;
    @FXML
    protected CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck, copyReadonlyCheck,
            copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck, notCopyCheck, copyAttrCheck, continueCheck,
            deleteSourceCheck;
    @FXML
    protected DatePicker modifyAfterInput;

    public ControlSynchronizeOptions() {
        baseTitle = message("DirectorySynchronize");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            deleteNonExistedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (deleteNonExistedCheck.isSelected()) {
                        deleteNonExistedCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteNonExistedCheck.setStyle(null);
                    }
                }
            });

            deleteSourceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (deleteSourceCheck.isSelected()) {
                        deleteSourceCheck.setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    } else {
                        deleteSourceCheck.setStyle(null);
                    }
                }
            });

            copyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    conditionsBox.setDisable(!conditionallyRadio.isSelected());
                }
            });
            conditionsBox.setDisable(!conditionallyRadio.isSelected());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected FileSynchronizeAttributes pickOptions() {
        try {
            copyAttr = new FileSynchronizeAttributes();
            copyAttr.setContinueWhenError(continueCheck.isSelected());
            copyAttr.setCopyAttrinutes(copyAttrCheck != null ? copyAttrCheck.isSelected() : true);
            copyAttr.setCopyEmpty(copyEmptyCheck.isSelected());
            copyAttr.setConditionalCopy(conditionallyRadio.isSelected());
            copyAttr.setCopyExisted(copyExistedCheck.isSelected());
            copyAttr.setCopyHidden(copyHiddenCheck.isSelected());
            copyAttr.setCopyNew(copyNewCheck.isSelected());
            copyAttr.setCopySubdir(copySubdirCheck.isSelected());
            copyAttr.setNotCopySome(notCopyCheck.isSelected());
            copyAttr.setOnlyCopyReadonly(copyReadonlyCheck.isSelected());
            List<String> notCopy = new ArrayList<>();
            String inputs = notCopyInput.getText();
            if (copyAttr.isNotCopySome() && inputs != null && !inputs.isBlank()) {
                List<String> values = Arrays.asList(inputs.trim().split(","));
                notCopy.addAll(values);
                TableStringValues.add(interfaceName + "Histories", values);
            }
            copyAttr.setNotCopyNames(notCopy);
            copyAttr.setOnlyCopyModified(copyModifiedCheck.isSelected());
            copyAttr.setModifyAfter(0);
            if (copyAttr.isOnlyCopyModified() && modifyAfterInput.getValue() != null) {
                copyAttr.setModifyAfter(DateTools.localDateToDate(modifyAfterInput.getValue()).getTime());
            }
            copyAttr.setDeleteNotExisteds(deleteNonExistedCheck.isSelected());

            if (!copyAttr.isCopyNew() && !copyAttr.isCopyExisted() && !copyAttr.isCopySubdir()) {
                alertInformation(message("NothingCopy"));
                return null;
            }
            return copyAttr;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    protected void popNameHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            showNameHistories(event);
        }
    }

    @FXML
    protected void showNameHistories(Event event) {
        PopTools.popStringValues(this, notCopyInput, event, interfaceName + "Histories", true, true);
    }

}
