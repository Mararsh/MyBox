package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import mara.mybox.data.FileEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_Left extends BaseFileEditorController_Actions {
    
    protected void initFileTab() {
        try {
            infoController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initFormatTab() {
        try {
            if (formatPane != null) {
                formatPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "FormatPane", formatPane.isExpanded());
                });
                formatPane.setExpanded(UserConfig.getBoolean(baseName + "FormatPane", false));
            }
            
            if (charsetSelector != null) {
                charsetSelector.getItems().addAll(TextTools.getCharsetNames());
                charsetSelector.setValue(UserConfig.getString(baseName + "SourceCharset", "UTF-8"));
                charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        UserConfig.setString(baseName + "SourceCharset", newValue);
                    }
                });
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initSaveTab() {
        try {
            if (savePane != null) {
                savePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "SavePane", savePane.isExpanded());
                });
                savePane.setExpanded(UserConfig.getBoolean(baseName + "SavePane", false));
            }
            if (confirmCheck != null) {
                confirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmSave", true));
                confirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ConfirmSave", newValue);
                    }
                });
            }
            
            if (autoSaveCheck != null) {
                autoSaveCheck.setDisable(true);
                autoSaveCheck.disabledProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });
                autoSaveCheck.setSelected(UserConfig.getBoolean(baseName + "AutoSave", true));
                autoSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });
                
                autoSaveDurationController
                        .permitInvalid(!autoSaveCheck.isSelected())
                        .init(baseName + "AutoSaveDuration", 300);
                autoSaveDurationController.notify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                        checkAutoSave();
                    }
                });
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initBackupsTab() {
        try {
            if (backupPane == null) {
                return;
            }
            backupPane.setExpanded(UserConfig.getBoolean(baseName + "BackupPane", false));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "BackupPane", backupPane.isExpanded());
            });
            
            backupController.setControls(this, baseName);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initSaveAsTab() {
        try {
            if (saveAsPane != null) {
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
                saveAsPane.setExpanded(UserConfig.getBoolean(baseName + "SaveAsPane", false));
            }
            if (targetCharsetSelector != null) {
                targetCharsetSelector.getItems().addAll(TextTools.getCharsetNames());
                targetCharsetSelector.setValue(UserConfig.getString(baseName + "TargetCharset", "UTF-8"));
                targetCharsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        UserConfig.setString(baseName + "TargetCharset", newValue);
                        if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                                || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                                || "UTF-32LE".equals(newValue)) {
                            targetBomCheck.setDisable(false);
                        } else {
                            targetBomCheck.setDisable(true);
                            if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                                targetBomCheck.setSelected(true);
                            } else {
                                targetBomCheck.setSelected(false);
                            }
                        }
                    }
                });
            }
            
            if (lineBreakGroup != null) {
                initLineBreakGroup();
                lineBreakGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                        if (!isSettingValues) {
                            checkLineBreakGroup();
                        }
                    }
                });
                checkLineBreakGroup();
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void checkAutoSave() {
        try {
            if (autoSaveCheck == null) {
                return;
            }
            UserConfig.setBoolean(baseName + "AutoSave", autoSaveCheck.isSelected());
            autoSaveDurationController.permitInvalid(autoSaveCheck.isDisabled() || !autoSaveCheck.isSelected());
            if (confirmCheck != null) {
                confirmCheck.setVisible(autoSaveCheck.isDisabled() || !autoSaveCheck.isSelected());
            }
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }
            if (sourceFile == null || autoSaveCheck.isDisabled()
                    || !autoSaveCheck.isSelected() || autoSaveDurationController.value <= 0) {
                return;
            }
            long interval = autoSaveDurationController.value * 1000;
            autoSaveTimer = new Timer();
            autoSaveTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
//                        popInformation(message("Saving"));
                        if (fileChanged.get()) {
                            saveAction();
                        }
                    });
                }
            }, interval, interval);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        
    }
    
    protected void initLineBreakGroup() {
        try {
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void checkLineBreakGroup() {
        try {
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initFilterTab() {
        try {
            if (filterPane != null) {
                filterPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "FilterPane", filterPane.isExpanded());
                });
                filterPane.setExpanded(UserConfig.getBoolean(baseName + "FilterPane", false));
            }
            
            if (filterButton != null && filterController != null) {
                filterButton.disableProperty().bind(filterController.valid.not());
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        
    }
    
    protected void initLocateTab() {
        try {
            if (locatePane != null) {
                locatePane.setExpanded(UserConfig.getBoolean(baseName + "LocatePane", false));
                locatePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "LocatePane", locatePane.isExpanded());
                });
            }
            locateLine = -1;
            if (lineInput != null) {
                lineInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(lineInput.getText());
                            if (v > 0 && v <= sourceInformation.getLinesNumber()) {
                                locateLine = v - 1;  // 0-based
                                lineInput.setStyle(null);
                                goLineButton.setDisable(false);
                            } else {
                                lineInput.setStyle(UserConfig.badStyle());
                                goLineButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            lineInput.setStyle(UserConfig.badStyle());
                            goLineButton.setDisable(true);
                        }
                    }
                });
            }
            
            locateObject = -1;
            if (objectNumberInput != null) {
                objectNumberInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        try {
                            int v = Integer.valueOf(objectNumberInput.getText());
                            if (v > 0 && v <= sourceInformation.getObjectsNumber()) {
                                locateObject = v - 1; // 0-based
                                objectNumberInput.setStyle(null);
                                goObjectButton.setDisable(false);
                            } else {
                                objectNumberInput.setStyle(UserConfig.badStyle());
                                goObjectButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            objectNumberInput.setStyle(UserConfig.badStyle());
                            goObjectButton.setDisable(true);
                        }
                    }
                });
            }
            
            linesRange = null;
            objectsRange = null;
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        
    }
    
    protected void initFindTab() {
        try {
            if (findReplaceController == null) {
                return;
            }
            findPane.setExpanded(UserConfig.getBoolean(baseName + "FindPane", false));
            findPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean(baseName + "FindPane", findPane.isExpanded());
            });
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void initPageBar() {
        try {
            List<String> values = new ArrayList();
            if (editType == FileEditInformation.Edit_Type.Bytes) {
                values.addAll(Arrays.asList("100,000", "500,000", "50,000", "10,000", "20,000",
                        "200,000", "1,000,000", "2,000,000", "20,000,000", "200,000,000"));
            } else {
                values.addAll(Arrays.asList("200", "500", "100", "300", "600", "50", "20", "800", "1000", "2000"));
            }
            pageSizeSelector.getItems().addAll(values);
            int pageSize = UserConfig.getInt(baseName + "PageSize", defaultPageSize);
            if (pageSize <= 0) {
                pageSize = defaultPageSize;
            }
            pageSizeSelector.setValue(StringTools.format(pageSize));
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    Platform.runLater(() -> {
                        setPageSize();
                    });
                }
            });
            
            pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCurrentPage();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
}
