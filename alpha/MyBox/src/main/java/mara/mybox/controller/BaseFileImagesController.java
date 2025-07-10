package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
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
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected ComboBox<String> percentSelector, pageSelector, thumbWidthSelector;
    @FXML
    protected CheckBox viewThumbsCheck;
    @FXML
    protected ListView<Integer> thumbsList;
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

            initThumbsList();

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

    public void initThumbsList() {
        try {
            thumbWidth = UserConfig.getInt(baseName + "ThumbnailWidth", 200);
            if (thumbWidth <= 0) {
                thumbWidth = 200;
            }
            if (thumbWidthSelector != null) {
                thumbWidthSelector.getItems().addAll(Arrays.asList("200", "100", "50", "150", "300"));
                thumbWidthSelector.getSelectionModel().select(thumbWidth + "");
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

            if (viewThumbsCheck != null) {
                viewThumbsCheck.setSelected(UserConfig.getBoolean(baseName + "Thumbnails", false));
                viewThumbsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Thumbnails", viewThumbsCheck.isSelected());
                        refreshThumbs();
                    }
                });
            }

            if (thumbsList != null) {
                thumbsList.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>() {
                    @Override
                    public ListCell<Integer> call(ListView<Integer> param) {
                        final ImageView imageview = new ImageView();
                        imageview.setPreserveRatio(true);
                        final Label label = new Label();
                        label.setAlignment(Pos.CENTER);
                        final VBox vbox = new VBox();
                        vbox.getChildren().addAll(imageview, label);
                        ListCell<Integer> cell = new ListCell<Integer>() {
                            @Override
                            public void updateItem(Integer item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                setGraphic(null);
                                if (empty || item == null || !viewThumbsCheck.isSelected()) {
                                    imageview.setImage(null);
                                    label.setText(null);
                                    return;
                                }
                                if (imageview.getImage() == null) {
                                    LoadThumbTask task = new LoadThumbTask(myController)
                                            .setCell(this).setBox(vbox).setPage(item);
                                    myController.start(task, false);
                                } else {
                                    setGraphic(vbox);
                                }
                            }
                        };
                        return cell;
                    }
                });
                thumbsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
                    @Override
                    public void changed(ObservableValue<? extends Integer> v, Integer oldV, Integer newV) {
                        if (newV != null) {
                            loadPage(newV);
                        }
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public class LoadThumbTask<Void> extends FxTask<Void> {

        private IndexedCell cell;
        private Integer page = null;
        private VBox box;

        public LoadThumbTask(BaseController controller) {
            this.controller = controller;
        }

        public LoadThumbTask<Void> setCell(IndexedCell cell) {
            this.cell = cell;
            return this;
        }

        public LoadThumbTask<Void> setPage(Integer page) {
            this.page = page;
            return this;
        }

        public LoadThumbTask<Void> setBox(VBox vbox) {
            this.box = vbox;
            return this;
        }

        @Override
        public void run() {
            if (cell == null || box == null || page == null) {
                return;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ImageView view = (ImageView) box.getChildren().get(0);
                    view.setFitWidth(thumbWidth);
                    if (view.getImage() == null) {
                        view.setImage(loadThumb(page));
                    }
                    Label label = (Label) box.getChildren().get(1);
                    label.setText((page + 1) + "");
                    label.setPrefWidth(thumbWidth);
                    box.setPrefWidth(thumbWidth + 10);
                    cell.setGraphic(box);
                }
            });
        }

    }

    protected Image loadThumb(Integer page) {
        return null;
    }

    protected void clearThumbs() {
        if (thumbsList != null) {
            thumbsList.getItems().clear();
        }
    }

    @FXML
    public void refreshThumbs() {
        if (thumbsList == null) {
            return;
        }
        thumbsList.getItems().clear();
        if (!viewThumbsCheck.isSelected()) {
            return;
        }
        for (int i = 0; i < framesNumber; i++) {
            thumbsList.getItems().add(i);
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
            clearThumbs();
            isSettingValues = false;
            bottomLabel.setText("");
            pageLabel.setText("");
            setSourceFile(file);
            if (openSourceButton != null) {
                openSourceButton.setDisable(sourceFile == null || !sourceFile.exists());
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
        if (task != null) {
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
