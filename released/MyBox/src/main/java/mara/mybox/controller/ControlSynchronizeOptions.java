package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;
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
    protected TextField notCopyInput, permissionInput;
    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected RadioButton conditionallyRadio;
    @FXML
    protected CheckBox copySubdirCheck, copyEmptyCheck, copyNewCheck, copyHiddenCheck,
            copyReadonlyCheck, copyExistedCheck, copyModifiedCheck, deleteNonExistedCheck,
            notCopyCheck, copyAttrCheck, copyMtimeCheck, permissionCheck,
            deleteSourceCheck, errorContinueCheck;
    @FXML
    protected DatePicker modifyAfterInput;

    public ControlSynchronizeOptions() {
        baseTitle = message("DirectorySynchronize");
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void setControls() {
        try {
            conditionsBox.disableProperty().bind(conditionallyRadio.selectedProperty().not());

            copySubdirCheck.setSelected(UserConfig.getBoolean(baseName + "CopySubdir", true));
            copySubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopySubdir", nv);
                }
            });

            copyEmptyCheck.setSelected(UserConfig.getBoolean(baseName + "CopyEmpty", true));
            copyEmptyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyEmpty", nv);
                }
            });

            copyNewCheck.setSelected(UserConfig.getBoolean(baseName + "CopyNew", true));
            copyNewCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyNew", nv);
                }
            });

            copyHiddenCheck.setSelected(UserConfig.getBoolean(baseName + "CopyHidden", true));
            copyHiddenCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyHidden", nv);
                }
            });

            copyReadonlyCheck.setSelected(UserConfig.getBoolean(baseName + "CopyReadonly", false));
            copyReadonlyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyReadonly", nv);
                }
            });

            copyExistedCheck.setSelected(UserConfig.getBoolean(baseName + "CopyExisted", true));
            copyExistedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyExisted", nv);
                }
            });

            copyModifiedCheck.setSelected(UserConfig.getBoolean(baseName + "CopyModified", true));
            copyModifiedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyModified", nv);
                }
            });

            deleteNonExistedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (deleteNonExistedCheck.isSelected()) {
                        deleteNonExistedCheck.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        deleteNonExistedCheck.setStyle(null);
                    }
                    UserConfig.setBoolean(baseName + "DeleteNonExisted", nv);
                }
            });
            deleteNonExistedCheck.setSelected(UserConfig.getBoolean(baseName + "DeleteNonExisted", false));

            notCopyCheck.setSelected(UserConfig.getBoolean(baseName + "NotCopy", false));
            notCopyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "NotCopy", nv);
                }
            });

            if (copyAttrCheck != null) {
                copyAttrCheck.setSelected(UserConfig.getBoolean(baseName + "CopyAttr", true));
                copyAttrCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "CopyAttr", nv);
                    }
                });
            }

            if (copyMtimeCheck != null) {
                copyMtimeCheck.setSelected(UserConfig.getBoolean(baseName + "CopyMtime", true));
                copyMtimeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "CopyMtime", nv);
                    }
                });
            }

            if (permissionCheck != null) {
                permissionCheck.setSelected(UserConfig.getBoolean(baseName + "SetPermissions", false));
                permissionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "SetPermissions", nv);
                    }
                });
            }

            if (permissionInput != null) {
                permissionInput.setText(UserConfig.getString(baseName + "Permissions", "755"));
            }

            deleteSourceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (deleteSourceCheck.isSelected()) {
                        deleteSourceCheck.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        deleteSourceCheck.setStyle(null);
                    }
                    UserConfig.setBoolean(baseName + "DeleteSource", nv);
                }
            });
            deleteSourceCheck.setSelected(UserConfig.getBoolean(baseName + "DeleteSource", false));

            if (errorContinueCheck != null) {
                errorContinueCheck.setSelected(false);
                errorContinueCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (errorContinueCheck.isSelected()) {
                            errorContinueCheck.setStyle(NodeStyleTools.darkRedTextStyle());
                        } else {
                            errorContinueCheck.setStyle(null);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected FileSynchronizeAttributes pickOptions() {
        try {
            copyAttr = new FileSynchronizeAttributes();
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
            if (copyAttr.isOnlyCopyModified() && modifyAfterInput.getValue() != null) {
                Date d = DateTools.localDateToDate(modifyAfterInput.getValue());
                copyAttr.setModifyAfter(d.getTime());
                TableStringValues.add(interfaceName + "Modify", DateTools.datetimeToString(d, TimeFormats.DateC));
            } else {
                copyAttr.setModifyAfter(-Long.MAX_VALUE);
            }
            copyAttr.setContinueWhenError(errorContinueCheck.isSelected());
            copyAttr.setCopyAttrinutes(copyAttrCheck != null ? copyAttrCheck.isSelected() : true);
            copyAttr.setCopyMTime(copyMtimeCheck != null ? copyMtimeCheck.isSelected() : true);
            copyAttr.setSetPermissions(permissionCheck != null ? permissionCheck.isSelected() : false);
            copyAttr.setPermissions(-1);
            if (copyAttr.isSetPermissions() && permissionInput != null) {
                try {
                    int v = Integer.parseInt(permissionInput.getText(), 8);
                    copyAttr.setPermissions(v);
                    UserConfig.setString(baseName + "Permissions", permissionInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Permissions"));
                    return null;
                }
            }
            copyAttr.setDeleteNotExisteds(deleteNonExistedCheck.isSelected());

            if (!copyAttr.isCopyNew() && !copyAttr.isCopyExisted() && !copyAttr.isCopySubdir()) {
                alertInformation(message("NothingCopy"));
                return null;
            }
            return copyAttr;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
        PopTools.popSavedValues(this, notCopyInput, event, interfaceName + "Histories", true);
    }

    @FXML
    protected void popModifyHistories(Event event) {
        if (UserConfig.getBoolean(interfaceName + "ModifyPopWhenMouseHovering", false)) {
            showModifyHistories(event);
        }
    }

    @FXML
    protected void showModifyHistories(Event event) {
        PopTools.popSavedValues(this, modifyAfterInput.getEditor(), event, interfaceName + "Modify", true);
    }

}
