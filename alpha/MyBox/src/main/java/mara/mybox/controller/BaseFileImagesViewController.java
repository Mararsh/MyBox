package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-22
 * @License Apache License Version 2.0
 */
public abstract class BaseFileImagesViewController extends BaseImageController {

    protected final int ThumbWidth = 200;
    protected int percent;
    protected SingletonTask thumbTask;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected ComboBox<String> percentBox, dpiBox, pageSelector;
    @FXML
    protected CheckBox thumbCheck;
    @FXML
    protected SplitPane mainPane;
    @FXML
    protected ScrollPane thumbScrollPane;
    @FXML
    protected VBox thumbBox;

    @FXML
    protected Label pageLabel;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.All, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();

            if (pageSelector != null) {
                pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkCurrentPage();
                    }
                });
            }

            if (thumbCheck != null) {
                thumbCheck.setSelected(UserConfig.getBoolean(baseName + "Thumbnails", false));
                thumbCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkThumbs();
                        UserConfig.setBoolean(baseName + "Thumbnails", thumbCheck.isSelected());
                    }
                });

                thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        loadThumbs();
                    }
                });
            }

            if (percentBox != null) {
                percentBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
                percent = UserConfig.getInt(baseName + "Percent", 100);
                if (percent < 0) {
                    percent = 100;
                }
                percentBox.getSelectionModel().select(percent + "");
                percentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
                                ValidationTools.setEditorNormal(percentBox);
                            } else {
                                ValidationTools.setEditorBadStyle(percentBox);
                            }

                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(percentBox);
                        }
                    }
                });
            }

            if (dpiBox != null) {
                dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
                dpi = UserConfig.getInt(baseName + "DPI", 96);
                if (dpi < 0) {
                    dpi = 96;
                }
                dpiBox.getSelectionModel().select(dpi + "");
                dpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
                                ValidationTools.setEditorNormal(dpiBox);
                            } else {
                                ValidationTools.setEditorBadStyle(dpiBox);
                            }
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(dpiBox);
                        }
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || imageView == null || imageView.getImage() == null) {
            return;
        }
        super.viewSizeChanged(change);
        percent = (int) (imageView.getFitHeight() * 100 / imageView.getImage().getHeight());
        isSettingValues = true;
        percentBox.getSelectionModel().select(percent + "");
        isSettingValues = false;
    }

    @Override
    public void initImageView() {
        try {
            if (imageView == null) {
                return;
            }
            super.initImageView();

            mainPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            scrollPane.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    double deltaY = event.getDeltaY();
                    if (event.isControlDown()) {
                    } else {
                        if (deltaY > 0) {
                            if (scrollPane.getVvalue() == scrollPane.getVmin()) {
                                event.consume();
                                pagePreviousAction();
                            }
                        } else {

                            if (scrollPane.getHeight() >= imageView.getFitHeight()
                                    || scrollPane.getVvalue() == scrollPane.getVmax()) {
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
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.parseInt(value) - 1;
            if (v >= 0 && v < framesNumber) {
                setCurrentPage(v);
                loadPage();
                pageSelector.getEditor().setStyle(null);
                return true;
            } else {
                pageSelector.getEditor().setStyle(UserConfig.badStyle());
                return false;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected void checkThumbs() {
        if (thumbCheck.isSelected()) {
            if (!mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().add(0, thumbScrollPane);
            }
            loadThumbs();

        } else {
            if (mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().remove(thumbScrollPane);
            }
        }
        adjustSplitPane();

    }

    protected void adjustSplitPane() {
        try {
            int size = mainPane.getItems().size();
            switch (size) {
                case 1:
                    mainPane.setDividerPositions(1);
                    break;
                case 2:
                    mainPane.setDividerPosition(0, 0.3);
                    break;
                case 3:
                    mainPane.setDividerPosition(0, 0.2);
                    mainPane.setDividerPosition(1, 0.5);
                    break;
            }
            mainPane.applyCss();
            mainPane.layout();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setSourceFile(File file) {
        sourceFile = file;
        checkSystemMethodButton(file);
        if (imageView != null) {
            imageView.setImage(null);
            imageView.setTranslateX(0);
        }
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
        imageView.setPreserveRatio(true);
        imageView.setImage(inImage);
        image = inImage;
        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " - " + message("Page") + " " + frameIndex);
        if (percent == 0) {
            paneSize();
        } else {
            setPercent(percent);
        }
        setImageChanged(false);
        updateLabelsTitle();
        imageView.requestFocus();
    }

    protected void setPercent(int percent) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
        refinePane();
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
        setCurrentPage(pageNumber);
        loadPage();
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
        task = new SingletonCurrentTask<Void>(this) {

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
        if (thumbTask != null && !thumbTask.isQuit()) {
            return;
        }
        if (thumbBox.getChildren().isEmpty()) {
            for (int i = 0; i < framesNumber; ++i) {
                ImageView view = new ImageView();
                view.setFitHeight(50);
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
        for (int i = pos; i < end; ++i) {
            ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
            if (view.getImage() == null) {
                missed.add(i);
            }
        }
        if (missed.isEmpty()) {
            return;
        }
        thumbTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return loadThumbs(missed);
            }

            @Override
            protected void whenSucceeded() {
                thumbBox.layout();
                adjustSplitPane();
            }

        };
        start(thumbTask, false);
    }

    protected boolean loadThumbs(List<Integer> missed) {
        return true;
    }

    @FXML
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

    @Override
    public File imageFile() {
        return null;
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
