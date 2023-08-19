package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.util.Callback;
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
import mara.mybox.fxml.cell.ListImageCell;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-13
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureScopeController_Outline extends ImageManufactureScopeController_Colors {

    public void initPixTab() {
        try {
            outlinesList.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });
            outlinesList.getItems().clear();
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
                }

            };
            start(outlinesTask);

            outlinesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldValue, Image newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    loadOutlineSource(newValue);
                }
            });

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

    @FXML
    public void showOutlineFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber * 3 / 4;
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

    public void loadOutlineSource(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private BufferedImage bufferedImage;

            @Override
            protected boolean handle() {
                try {
                    bufferedImage = ImageFileReaders.readImage(file);
                    return bufferedImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadOutlineSource(bufferedImage);
            }

        };
        start(task);
    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return;
        }
        loadOutlineSource(bufferedImage, DoubleRectangle.image(bufferedImage));
    }

    public void loadOutlineSource(Image image) {
        if (isSettingValues || image == null) {
            return;
        }
        loadOutlineSource(SwingFXUtils.fromFXImage(image, null));
    }

    public void loadOutlineSource(BufferedImage bufferedImage, DoubleRectangle rect) {
        if (bufferedImage == null || rect == null) {
            return;
        }
        scope.setOutlineSource(bufferedImage);
        maskRectangleData = rect.copy();
        makeOutline();
    }

    public void makeOutline() {
        if (isSettingValues || image == null
                || scope == null || scope.getScopeType() != ImageScope.ScopeType.Outline
                || scope.getOutlineSource() == null || maskRectangleData == null) {
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
                    Image bgImage = editor.imageView.getImage();
                    outline = AlphaTools.outline(scope.getOutlineSource(),
                            maskRectangleData, (int) bgImage.getWidth(), (int) bgImage.getHeight(),
                            scopeOutlineKeepRatioCheck.isSelected());
                    if (outline == null || task == null || isCancelled()) {
                        return false;
                    }
                    maskRectangleData = DoubleRectangle.xywh(
                            maskRectangleData.getX(), maskRectangleData.getY(),
                            outline[0].getWidth(), outline[0].getHeight());
                    scope.setOutline(outline[1]);
                    scope.setRectangle(maskRectangleData.copy());
                    finalScope();
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                            bgImage, scope, PixelsOperation.OperationType.ShowScope);
                    pixelsOperation.setSkipTransparent(ignoreTransparentCheck.isSelected());
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
            }

        };
        start(task, thisPane);
    }

}
