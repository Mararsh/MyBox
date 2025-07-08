package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public abstract class BaseFileImagesController extends BaseFileController {

    protected int percent, thumbWidth, framesNumber, frameIndex;
    protected ImageView imageView;
    protected FxTask thumbTask;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected ComboBox<String> percentSelector, pageSelector, thumbWidthSelector;
    @FXML
    protected CheckBox viewThumbsCheck;
    @FXML
    protected ScrollPane thumbScrollPane;
    @FXML
    protected VBox thumbBox;
    @FXML
    protected Label pageLabel;
    @FXML
    protected ControlImageView imageController;
    @FXML
    protected FlowPane playPane;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.All, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();

            playPane.disableProperty().bind(imageController.imageView.imageProperty().isNull());

            if (pageSelector != null) {
                pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkCurrentPage();
                    }
                });
            }

            if (viewThumbsCheck != null) {
                viewThumbsCheck.setSelected(UserConfig.getBoolean(baseName + "Thumbnails", false));
                viewThumbsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Thumbnails", viewThumbsCheck.isSelected());
                        refreshThumbs();
                    }
                });

                thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        loadThumbs();
                    }
                });
            }

            if (percentSelector != null) {
                percentSelector.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
                percent = UserConfig.getInt(baseName + "Percent", 100);
                if (percent < 0) {
                    percent = 100;
                }
                percentSelector.getSelectionModel().select(percent + "");
                percentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                percent = v;
                                UserConfig.setInt(baseName + "Percent", percent);
                                setPercent(percent);
                                ValidationTools.setEditorNormal(percentSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(percentSelector);
                            }

                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(percentSelector);
                        }
                    }
                });
            }

            if (dpiSelector != null) {
                dpiSelector.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
                dpi = UserConfig.getInt(baseName + "DPI", 96);
                if (dpi < 0) {
                    dpi = 96;
                }
                dpiSelector.getSelectionModel().select(dpi + "");
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                dpi = v;
                                UserConfig.setInt(baseName + "DPI", dpi);
                                loadPage();
                                ValidationTools.setEditorNormal(dpiSelector);
                            } else {
                                ValidationTools.setEditorBadStyle(dpiSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(dpiSelector);
                        }
                    }
                });
            }

            if (thumbWidthSelector != null) {
                thumbWidthSelector.getItems().addAll(Arrays.asList("200", "100", "50", "150", "300"));
                thumbWidth = UserConfig.getInt(baseName + "ThumbnailWidth", 200);
                if (thumbWidth <= 0) {
                    thumbWidth = 200;
                }
                thumbWidthSelector.getSelectionModel().select(dpi + "");
                thumbWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                thumbWidth = v;
                                UserConfig.setInt(baseName + "ThumbnailWidth", thumbWidth);
                                ValidationTools.setEditorNormal(thumbWidthSelector);
                                refreshThumbs();
                            } else {
                                ValidationTools.setEditorBadStyle(thumbWidthSelector);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(thumbWidthSelector);
                        }
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initImageView() {
        try {
            imageView = imageController.imageView;

            imageController.sizeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    percent = (int) (imageView.getFitHeight() * 100
                            / imageView.getImage().getHeight());
                    isSettingValues = true;
                    percentSelector.getSelectionModel().select(percent + "");
                    isSettingValues = false;
                }
            });

            imageController.scrollPane.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    double deltaY = event.getDeltaY();
                    if (event.isControlDown()) {
                    } else {
                        if (deltaY > 0) {
                            if (imageController.scrollPane.getVvalue() == imageController.scrollPane.getVmin()) {
                                event.consume();
                                pagePreviousAction();
                            }
                        } else {

                            if (imageController.scrollPane.getHeight() >= imageView.getFitHeight()
                                    || imageController.scrollPane.getVvalue() == imageController.scrollPane.getVmax()) {
                                event.consume();
                                pageNextAction();
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null || framesNumber <= 0) {
            return false;
        }
        int v;
        try {
            v = Integer.parseInt(pageSelector.getValue()) - 1;
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v < framesNumber) {
            setCurrentPage(v);
            loadPage();
            pageSelector.getEditor().setStyle(null);
            return true;
        } else {
            pageSelector.getEditor().setStyle(UserConfig.badStyle());
            return false;
        }
    }

    @Override
    public void setSourceFile(File file) {
        sourceFile = file;
        if (imageView != null) {
            imageView.setImage(null);
            imageView.setTranslateX(0);
        }
        imageController.reset();
    }

    protected void setTotalPages(int total) {
        framesNumber = total;
    }

    protected void setCurrentPage(int page) {
        frameIndex = page;
    }

    public void setImage(Image inImage, int percent) {
        if (imageView == null) {
            return;
        }
        imageController.loadImage(this, inImage, framesNumber, frameIndex);
        if (percent <= 0) {
            imageController.paneSize();
        } else {
            setPercent(percent);
        }
    }

    protected void setPercent(int percent) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
        imageController.refinePane();
    }

    public void initPage(File file, int page) {
        try {
            sourceFile = file;
            setTotalPages(0);
            setCurrentPage(page);
            percent = 0;
            isSettingValues = true;
            pageSelector.getItems().clear();
            pageSelector.setValue(null);
            thumbBox.getChildren().clear();
            isSettingValues = false;
            bottomLabel.setText("");
            pageLabel.setText("");
            setSourceFile(file);
            if (openSourceButton != null) {
                openSourceButton.setDisable(sourceFile == null || !sourceFile.exists());
            }
            if (thumbTask != null) {
                thumbTask.cancel();
                thumbTask = null;
            }
            if (file == null) {
                getMyStage().setTitle(getBaseTitle());
                return;
            }
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void loadPage(int pageNumber) {
        MyBoxLog.console(pageNumber);
        setCurrentPage(pageNumber);
        loadPage();
        loadThumbs();
    }

    public void initCurrentPage() {
        if (frameIndex < 0) {
            setCurrentPage(0);
        } else if (frameIndex >= framesNumber) {
            setCurrentPage(framesNumber - 1);
        }
        isSettingValues = true;
        pageSelector.setValue((frameIndex + 1) + "");
        isSettingValues = false;
        pagePreviousButton.setDisable(frameIndex <= 0);
        pageNextButton.setDisable(frameIndex >= (framesNumber - 1));
        bottomLabel.setText("");
    }

    // frameIndex is 0-based
    protected void loadPage() {
        initCurrentPage();
        if (sourceFile == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image image;

            @Override
            protected boolean handle() {
                image = readPageImage();
                return image != null;
            }

            @Override
            protected void whenSucceeded() {
                setImage(image, percent);
            }
        };
        start(task, MessageFormat.format(message("LoadingPageNumber"), (frameIndex + 1) + ""));
    }

    // return error
    protected Image readPageImage() {
        return null;
    }

    protected void loadThumbs() {
        MyBoxLog.console(thumbTask != null);
        if (thumbTask != null) {
            thumbTask.cancel();
        }
        if (!viewThumbsCheck.isSelected()) {
            thumbBox.getChildren().clear();
            return;
        }
        if (thumbBox.getChildren().isEmpty()) {
            for (int i = 0; i < framesNumber; ++i) {
                ImageView view = new ImageView();
                view.setFitWidth(thumbWidth);
                view.setPreserveRatio(true);
                final int p = i;
                view.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        loadPage(p);
                    }
                });
                thumbBox.getChildren().add(view);
                Label label = new Label((i + 1) + "");
                label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        loadPage(p);
                    }
                });
                thumbBox.getChildren().add(label);
            }
        }
        int pos = Math.max(0, (int) (framesNumber * thumbScrollPane.getVvalue() / thumbScrollPane.getVmax()) - 1);
        int end = Math.min(pos + 20, framesNumber);
        List<Integer> missed = new ArrayList<>();
        MyBoxLog.console(pos);
        for (int i = pos; i < end; ++i) {
            ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
            if (view.getImage() == null) {
                missed.add(i);
                MyBoxLog.console(i);
            }
        }
        if (missed.isEmpty()) {
            return;
        }
        thumbTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                loadThumbs(missed);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                thumbBox.layout();
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(thumbTask, false);
    }

    protected boolean loadThumbs(List<Integer> missed) {
        return true;
    }

    @FXML
    @Override
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        if (frameIndex >= framesNumber - 1) {
            return;
        }
        setCurrentPage(++frameIndex);
        loadPage();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        if (frameIndex <= 0) {
            return;
        }
        setCurrentPage(--frameIndex);
        loadPage();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        setCurrentPage(0);
        loadPage();
    }

    @FXML
    @Override
    public void pageLastAction() {
        setCurrentPage(framesNumber - 1);
        loadPage();

    }

    @FXML
    public void refreshThumbs() {
        if (thumbTask != null) {
            thumbTask.cancel();
        }
        thumbBox.getChildren().clear();
        loadThumbs();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return imageController.keyEventsFilter(event);
    }

    @Override
    public void cleanPane() {
        try {
            if (thumbTask != null) {
                thumbTask.cancel();
                thumbTask = null;
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        super.cleanPane();
    }

}
