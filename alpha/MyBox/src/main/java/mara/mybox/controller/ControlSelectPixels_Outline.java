package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ControlSelectPixels_Outline extends ControlSelectPixels_Colors {

    public void outlineExamples() {
        try {
            SingletonCurrentTask outlinesTask = new SingletonCurrentTask<Void>(this) {

                @Override
                protected boolean handle() {
                    for (ImageItem item : ImageItem.predefined()) {
                        if (isCancelled()) {
                            return true;
                        }
                        Image image = item.readImage();
                        if (image != null) {
                            Platform.runLater(() -> {
                                isSettingValues = true;
                                outlinesList.getItems().add(image);
                                isSettingValues = false;
                            });
                            this.setInfo(item.getName());
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    outlinesList.getSelectionModel().select(0);
                }

            };
            start(outlinesTask);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectOutlineFile() {
        try {
            File file = FxFileTools.selectFile(this,
                    UserConfig.getPath(baseName + "SourcePath"),
                    FileFilters.AlphaImageExtensionFilter);
            if (file == null) {
                return;
            }
            loadOutlineSource(file);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadOutlineSource(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image outlineImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                    outlineImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    return outlineImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                outlinesList.getItems().add(0, outlineImage);
                isSettingValues = false;
                outlinesList.getSelectionModel().select(0);
            }

        };
        start(task);
    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (isSettingValues || bufferedImage == null) {
            return;
        }
        scope.setOutlineSource(bufferedImage);
        maskRectangleData = DoubleRectangle.image(bufferedImage);
        indicateOutline();
    }

    public void loadOutlineSource(Image image) {
        if (isSettingValues || image == null) {
            return;
        }
        loadOutlineSource(SwingFXUtils.fromFXImage(image, null));
    }

    public boolean validOutline() {
        return srcImage() != null
                && scope != null
                && scope.getScopeType() == ImageScope.ScopeType.Outline
                && scope.getOutlineSource() != null
                && maskRectangleData != null;
    }

    public void indicateOutline() {
        if (isSettingValues || !validOutline() || !pickBaseValues()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private BufferedImage[] outline;
            private Image outlineImage;

            @Override
            protected boolean handle() {
                try {
                    Image bgImage = srcImage();
                    outline = AlphaTools.outline(scope.getOutlineSource(),
                            maskRectangleData,
                            (int) bgImage.getWidth(),
                            (int) bgImage.getHeight(),
                            scopeOutlineKeepRatioCheck.isSelected());
                    if (outline == null || task == null || isCancelled()
                            || !validOutline()) {
                        return false;
                    }
                    maskRectangleData = DoubleRectangle.xywh(
                            maskRectangleData.getX(), maskRectangleData.getY(),
                            outline[0].getWidth(), outline[0].getHeight());
                    scope.setOutline(outline[1]);
                    scope.setRectangle(maskRectangleData.copy());

                    PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                            bgImage, scope, PixelsOperation.OperationType.ShowScope);
                    outlineImage = pixelsOperation.operateFxImage();
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return outlineImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                image = outlineImage;
                imageView.setImage(outlineImage);
                showMaskRectangle();
                showNotify.set(!showNotify.get());
            }

        };
        start(task, viewBox);
    }

    @FXML
    public void showOutlineFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, false) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber;
                return VisitHistoryTools.getRecentAlphaImages(fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectOutlineFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                loadOutlineSource(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void pickOutlineFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectOutlineFile();
        } else {
            showOutlineFileMenu(event);
        }
    }

    @FXML
    public void popOutlineFile(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showOutlineFileMenu(event);
        }
    }

}
