package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @License Apache License Version 2.0
 *
 * ImagesBrowserController < ImagesBrowserController_Pane <
 * ImagesBrowserController_Menu < ImagesBrowserController_Action <
 * ImagesBrowserController_Load < ImageViewerController
 */
public class ImagesBrowserController extends ImagesBrowserController_Pane {

    public ImagesBrowserController() {
        baseTitle = message("ImagesBrowser");
        TipsLabelKey = "ImagesBrowserTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            colsNum = -1;
            displayMode = DisplayMode.ImagesGrid;
            currentIndex = -1;
            thumbWidth = UserConfig.getInt(baseName + "ThumbnailWidth", 100);
            thumbWidth = thumbWidth > 0 ? thumbWidth : 100;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> cvalues = Arrays.asList("3", "4", "6",
                    message("ThumbnailsList"), message("FilesList"),
                    "2", "5", "7", "8", "9", "10", "16", "25", "20", "12", "15");
            colsnumBox.getItems().addAll(cvalues);
            colsnumBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        if (message("ThumbnailsList").equals(newValue)) {
                            displayMode = DisplayMode.ThumbnailsList;
                            zoomOutAllButton.setDisable(true);
                            zoomInAllButton.setDisable(true);
                            imageSizeAllButton.setDisable(true);
                            paneSizeAllButton.setDisable(true);
                        } else if (message("FilesList").equals(newValue)) {
                            displayMode = DisplayMode.FilesList;
                            zoomOutAllButton.setDisable(true);
                            zoomInAllButton.setDisable(true);
                            imageSizeAllButton.setDisable(true);
                            paneSizeAllButton.setDisable(true);
                        } else {
                            tableData.clear();
                            displayMode = DisplayMode.ImagesGrid;
                            colsNum = Integer.valueOf(newValue);
                            if (colsNum >= 0) {
                                ValidationTools.setEditorNormal(colsnumBox);
                            } else {
                                ValidationTools.setEditorBadStyle(colsnumBox);
                                return;
                            }
                            zoomOutAllButton.setDisable(false);
                            zoomInAllButton.setDisable(false);
                            imageSizeAllButton.setDisable(false);
                            paneSizeAllButton.setDisable(false);
                        }
                        makeImagesPane();
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(colsnumBox);
                    }
                }
            });

            filesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
                    try {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        filesNumber = Integer.valueOf(newValue);
                        if (filesNumber > 0) {
                            ValidationTools.setEditorNormal(filesBox);
                            makeImagesNevigator(true);

                        } else {
                            ValidationTools.setEditorBadStyle(filesBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(filesBox);
                    }
                }
            });

            thumbWidthSelector.getItems().addAll(Arrays.asList("100", "150", "50", "200", "300"));
            thumbWidthSelector.setValue(thumbWidth + "");
            thumbWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            ValidationTools.setEditorNormal(thumbWidthSelector);
                            UserConfig.setInt(baseName + "ThumbnailWidth", v);
                            thumbWidth = v;
                            loadImages();
                        } else {
                            ValidationTools.setEditorBadStyle(thumbWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(thumbWidthSelector);
                    }
                }
            });

            saveRotationCheck.setSelected(UserConfig.getBoolean(baseName + "SaveRotation", true));
            saveRotationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SaveRotation", saveRotationCheck.isSelected());
                }
            });

            fileBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            viewPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            browsePane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            mainBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            leftPaneControl.visibleProperty().bind(Bindings.isEmpty(imageFileList).not());
            rightPane.disableProperty().bind(Bindings.isEmpty(imageFileList));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(selectFileButton, new Tooltip(message("SelectMultipleFilesBrowse")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public ImagesBrowserController refreshInterfaceAndFile() {
        super.refreshInterface();
        makeImagesNevigator(true);
        return this;
    }

}
