package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.data.FileEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public abstract class BaseTextController_Left extends BaseTextController_Actions {

    protected void initSaveTab() {
        try {
            if (savePane != null) {
                savePane.expandedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (!isSettingValues) {
                        UserConfig.setBoolean(baseName + "SavePane", savePane.isExpanded());
                    }
                });
            }
            if (autoSaveCheck != null) {
                autoSaveCheck.setSelected(UserConfig.getBoolean(baseName + "AutoSave", true));
                autoSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
                            int v = Integer.parseInt(lineInput.getText());
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
                            int v = Integer.parseInt(objectNumberInput.getText());
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }
    }

}
