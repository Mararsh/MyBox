package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableMessageCell;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ControlImagesClipboard extends BaseSysTableController<ImageClipboard> {

    protected Image lastSystemClip;
    protected int thumbWidth = AppVariables.thumbnailWidth;

    @FXML
    protected HBox buttonsBox;
    @FXML
    protected Button useClipButton, thumbsListButton;
    @FXML
    protected TableColumn<ImageClipboard, Integer> widthColumn, heightColumn;
    @FXML
    protected TableColumn<ImageClipboard, String> sourceColumn;
    @FXML
    protected TableColumn<ImageClipboard, ImageClipboard> thumbColumn;
    @FXML
    protected TableColumn<ImageClipboard, Date> timeColumn;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableImageClipboard();
        parentController = this;
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            thumbColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
            thumbColumn.setCellFactory(new Callback<TableColumn<ImageClipboard, ImageClipboard>, TableCell<ImageClipboard, ImageClipboard>>() {

                @Override
                public TableCell<ImageClipboard, ImageClipboard> call(TableColumn<ImageClipboard, ImageClipboard> param) {
                    ImageView imageview = new ImageView();
                    imageview.setPreserveRatio(true);
                    imageview.setFitWidth(thumbWidth);
                    imageview.setFitHeight(thumbWidth);
                    TableCell<ImageClipboard, ImageClipboard> cell = new TableCell<ImageClipboard, ImageClipboard>() {
                        @Override
                        public void updateItem(ImageClipboard item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            imageview.setImage(item.loadThumb());
                            setGraphic(imageview);
                        }
                    };
                    return cell;
                }
            });

            widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));
            heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableDateCell());

            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
            sourceColumn.setCellFactory(new TableMessageCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(BaseController parent, boolean use) {
        try {
            this.parentController = parent;
            if (use) {
//                buttonsBox.getChildren().remove(copyToSystemClipboardButton);
//                copyToSystemClipboardButton.setVisible(false);
                useClipButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
            } else {
                buttonsBox.getChildren().remove(useClipButton);
                copyToSystemClipboardButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
            }
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            Image clip = ImageClipboardTools.fetchImageInClipboard(false);
            if (clip == null) {
                parentController.popInformation(Languages.message("NoImageInClipboard"));
                return;
            }
            lastSystemClip = clip;
            task = new SingletonTask<Void>(this) {

                private ImageClipboard clipData;

                @Override
                protected boolean handle() {
                    if (lastSystemClip != null && FxImageTools.sameImage(lastSystemClip, clip)) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                parentController.popInformation(Languages.message("NoImageInClipboard"));
                            }
                        });
                        return true;
                    }
                    clipData = ImageClipboard.add(lastSystemClip, ImageClipboard.ImageSource.SystemClipBoard);
                    return clipData != null;
                }

            };
            parentController.start(task);
        }
    }

    @FXML
    @Override
    public void selectSourceFileDo(File file) {
        recordFileOpened(file);
        selectSourceFile(file);
    }

    @Override
    public void selectSourceFile(final File file) {
        try {
            if (file == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    private ImageClipboard clip;

                    @Override
                    protected boolean handle() {
                        clip = ImageClipboard.add(file);
                        return clip != null;
                    }

                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public List<ImageClipboard> readPageData() {
        try ( Connection conn = DerbyBase.getConnection()) {
            ((TableImageClipboard) tableDefinition).clearInvalid(conn);
            return tableDefinition.queryConditions(conn, queryConditions, orderColumns, startRowOfCurrentPage, pageSize);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return new ArrayList<>();
        }
    }

    @Override
    protected int deleteData(List<ImageClipboard> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (ImageClipboard clip : data) {
            FileDeleteTools.delete(clip.getImageFile());
            FileDeleteTools.delete(clip.getThumbnailFile());
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected void afterClear() {
        FileDeleteTools.clearDir(new File(AppPaths.getImageClipboardPath()));
        refreshAction();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
        updateStatus();
    }

    public void updateStatus() {
        if (ImageClipboardTools.isMonitoring()) {
            bottomLabel.setText(Languages.message("MonitoringImageInSystemClipboard"));
        } else {
            bottomLabel.setText(Languages.message("NotMonitoringImageInSystemClipboard"));
        }
    }

    @FXML
    @Override
    public void editAction() {
        ImageClipboard clip = tableView.getSelectionModel().getSelectedItem();
        if (clip == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image selectedImage;

                @Override
                protected boolean handle() {
                    selectedImage = ImageClipboard.loadImage(clip);
                    return selectedImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    final ImageManufactureController controller
                            = (ImageManufactureController) WindowTools.openStage(Fxmls.ImageManufactureFxml);
                    controller.loadImage(selectedImage);
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        ImageClipboard clip = tableView.getSelectionModel().getSelectedItem();
        if (clip == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image selectedImage;

                @Override
                protected boolean handle() {
                    selectedImage = ImageClipboard.loadImage(clip);
                    return selectedImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    ImageClipboardTools.copyToSystemClipboard(parentController, selectedImage);
                }
            };
            parentController.start(task);
        }
    }

    @FXML
    public void clipsPath() {
        File path = new File(AppPaths.getImageClipboardPath());
        browseURI(path.toURI());
    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private List<ImageClipboard> clips;

                @Override
                protected boolean handle() {
                    List<Image> examples = Arrays.asList(
                            new Image("img/ww1.png"), new Image("img/ww2.png"), new Image("img/ww5.png"),
                            new Image("img/ww3.png"), new Image("img/ww4.png"), new Image("img/ww6.png"),
                            new Image("img/ww7.png"), new Image("img/ww8.png"), new Image("img/ww9.png"),
                            new Image("img/About.png"), new Image("img/MyBox.png"), new Image("img/DataTools.png"),
                            new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                            new Image("img/DocumentTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                            new Image("img/Settings.png"), new Image("img/zz1.png"), new Image("img/jade.png")
                    );
                    clips = new ArrayList<>();
                    for (int i = examples.size() - 1; i >= 0; --i) {
                        ImageClipboard clip = ImageClipboard.create(examples.get(i), ImageClipboard.ImageSource.Example);
                        if (clip == null) {
                            continue;
                        }
                        clips.add(clip);
                    }
                    tableDefinition.insertList(clips);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            parentController.start(task);
        }
    }

    @FXML
    @Override
    public void systemClipBoard() {
        ImageInSystemClipboardController.oneOpen();
    }

    /*
        static methods
     */
    public static void updateClipboards() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageInMyBoxClipboardController) {
                    try {
                        ((ImageInMyBoxClipboardController) controller).clipsController.refreshAction();
                    } catch (Exception e) {
                    }
                } else if (controller instanceof ImageManufactureController) {
                    try {
                        ImageManufactureController imageController = (ImageManufactureController) controller;
                        if (imageController.operationsController.clipboardController != null) {
                            imageController.operationsController.clipboardController.clipsController.refreshAction();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    public static void updateClipboardsStatus() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageInMyBoxClipboardController) {
                    try {
                        ((ImageInMyBoxClipboardController) controller).clipsController.updateStatus();
                    } catch (Exception e) {
                    }
                } else if (controller instanceof ImageManufactureController) {
                    try {
                        ImageManufactureController imageController = (ImageManufactureController) controller;
                        if (imageController.operationsController.clipboardController != null) {
                            imageController.operationsController.clipboardController.clipsController.updateStatus();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
