package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.RecentVisitMenu;
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
public class ImageManufactureScopeController_Outline extends ImageManufactureScopeController_Colors {

    public void initPixTab() {
        try {
            List<Image> prePixList = Arrays.asList(
                    new Image("img/ww1.png"), new Image("img/jade.png"),
                    new Image("img/ww3.png"), new Image("img/ww4.png"), new Image("img/ww6.png"),
                    new Image("img/ww7.png"), new Image("img/ww8.png"), new Image("img/ww9.png"),
                    new Image("img/About.png"), new Image("img/MyBox.png"), new Image("img/DataTools.png"),
                    new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                    new Image("img/DocumentTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                    new Image("img/zz1.png")
            );
            outlinesList.getItems().addAll(prePixList);
            outlinesList.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });
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
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popOutlineFile(MouseEvent event) {
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

    public void loadOutlineSource(File file) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private BufferedImage bufferedImage;

                @Override
                protected boolean handle() {
                    try {
                        bufferedImage = ImageFileReaders.readImage(file);
                        return bufferedImage != null;
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadOutlineSource(bufferedImage);
                }

            };
            parentController.handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return;
        }
        loadOutlineSource(bufferedImage, new DoubleRectangle(0, 0,
                bufferedImage.getWidth(), bufferedImage.getHeight()));
    }

    public void loadOutlineSource(BufferedImage bufferedImage, DoubleRectangle rect) {
        if (bufferedImage == null || rect == null) {
            return;
        }
        outlineSource = bufferedImage;
        maskRectangleData = rect.cloneValues();
        setMaskRectangleLineVisible(true);
        drawMaskRectangleLineAsData();

        makeOutline();
    }

    public void loadOutlineSource(Image image) {
        if (isSettingValues || image == null) {
            return;
        }
        loadOutlineSource(SwingFXUtils.fromFXImage(image, null));
    }

    public void makeOutline() {
        try {
            if (isSettingValues || image == null
                    || scope == null || scope.getScopeType() != ImageScope.ScopeType.Outline
                    || outlineSource == null || maskRectangleData == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private BufferedImage[] outline;

                    @Override
                    protected boolean handle() {
                        try {
                            outline = AlphaTools.outline(outlineSource,
                                    maskRectangleData, (int) getImageWidth(), (int) getImageHeight(),
                                    scopeOutlineKeepRatioCheck.isSelected(),
                                    ColorConvertTools.converColor(Color.WHITE), areaExcludedCheck.isSelected());
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return outline != null;
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (scope == null) {   // this may happen jn quitOpearting()
                            return;
                        }
                        maskRectangleData = new DoubleRectangle(
                                maskRectangleData.getSmallX(), maskRectangleData.getSmallY(),
                                maskRectangleData.getSmallX() + outline[0].getWidth(),
                                maskRectangleData.getSmallY() + outline[0].getHeight());
                        drawMaskRectangleLineAsData();
                        scope.setOutlineSource(outlineSource);
                        scope.setOutline(outline[1]);
                        scope.setRectangle(maskRectangleData.cloneValues());
                        displayOutline(outline[1]);
                    }

                };
                parentController.handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void displayOutline(BufferedImage bufferedImage) {
        if (scope == null || bufferedImage == null || scope.getScopeType() != ImageScope.ScopeType.Outline) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image outlineImage;

                @Override
                protected boolean handle() {
                    try {
                        outlineImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return outlineImage != null;
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    scopeView.setImage(outlineImage);
                    double radio = imageView.getBoundsInParent().getWidth() / getImageWidth();
                    double offsetX = maskRectangleData.getSmallX() >= 0 ? 0 : maskRectangleData.getSmallX();
                    double offsetY = maskRectangleData.getSmallY() >= 0 ? 0 : maskRectangleData.getSmallY();
                    scopeView.setLayoutX(imageView.getLayoutX() + offsetX * radio);
                    scopeView.setLayoutY(imageView.getLayoutY() + offsetY * radio);
                    scopeView.setFitWidth(outlineImage.getWidth() * radio);
                    scopeView.setFitHeight(outlineImage.getHeight() * radio);
                }
            };
            parentController.handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

}
