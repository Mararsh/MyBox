package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableMessageCell;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ControlImagesClipboard extends BaseDataTableController<ImageClipboard> {

    protected Image lastSystemClip;
    protected boolean loaded;
    protected int thumbWidth = AppVariables.getUserConfigInt("ThumbnailWidth", 100);

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
    public void setTableDefinition() {
        tableDefinition = new TableImageClipboard();
    }

    @Override
    protected void initColumns() {
        try {
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
        this.parentController = parent;
        if (use) {
            buttonsBox.getChildren().remove(copyButton);
            copyButton.setVisible(false);
            useClipButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        } else {
            buttonsBox.getChildren().remove(useClipButton);
            FxmlControl.setTooltip(copyButton, new Tooltip(message("CopyToSystemClipboard")));
            copyButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        }
        loadTableData();
    }

    @FXML
    public void loadSystemClipboard() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            final Image clip = SystemTools.fetchImageInClipboard(false);
            if (clip == null
                    || (lastSystemClip != null
                    && FxmlImageManufacture.sameImage(lastSystemClip, clip))) {
                parentController.popInformation(message("NoImageInClipboard"));
                return;
            }
            lastSystemClip = clip;
            task = new SingletonTask<Void>() {

                private ImageClipboard clip;

                @Override
                protected boolean handle() {
                    clip = ImageClipboard.add(lastSystemClip, ImageClipboard.ImageSource.SystemClipBoard, false);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
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
                task = new SingletonTask<Void>() {

                    private ImageClipboard clip;

                    @Override
                    protected boolean handle() {
                        clip = ImageClipboard.add(file);
                        return clip != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        refreshAction();
                    }
                };
                if (parentController != null) {
                    parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
                } else {
                    openHandlingStage(task, Modality.WINDOW_MODAL);
                }
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public List<ImageClipboard> readPageData() {
        try ( Connection conn = DerbyBase.getConnection()) {
            ((TableImageClipboard) tableDefinition).validateData(conn);
            return tableDefinition.queryConditions(conn, queryConditions, currentPageStart - 1, currentPageSize);
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
            FileTools.delete(clip.getImageFile());
            FileTools.delete(clip.getThumbnailFile());
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected int clearData() {
        FileTools.clearDir(new File(AppVariables.getImageClipboardPath()));
        return tableDefinition.deleteCondition(queryConditions);
    }

    @FXML
    @Override
    public void refreshAction() {
        updateClipboards();
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        ImageClipboard clip = tableView.getSelectionModel().getSelectedItem();
        if (clip == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image selectedImage;

                @Override
                protected boolean handle() {
                    selectedImage = ImageClipboard.loadImage(clip);
                    return selectedImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    final ImageManufactureController controller
                            = (ImageManufactureController) FxmlWindow.openStage(CommonValues.ImageManufactureFxml);
                    controller.loadImage(selectedImage);
                }
            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void copyAction() {
        ImageClipboard clip = tableView.getSelectionModel().getSelectedItem();
        if (clip == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image selectedImage;

                @Override
                protected boolean handle() {
                    selectedImage = ImageClipboard.loadImage(clip);
                    return selectedImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    ClipboardContent cc = new ClipboardContent();
                    cc.putImage(selectedImage);
                    Clipboard.getSystemClipboard().setContent(cc);
                    popInformation(message("ImageCopiedInSystemClipBoard"));
                }
            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void clipsPath() {
        File path = new File(AppVariables.getImageClipboardPath());
        browseURI(path.toURI());
    }

    @Override
    public void loadExamples() {
        List<Image> examples = Arrays.asList(
                new Image("img/ww1.png"), new Image("img/ww2.png"), new Image("img/ww5.png"),
                new Image("img/ww3.png"), new Image("img/ww4.png"), new Image("img/ww6.png"),
                new Image("img/ww7.png"), new Image("img/ww8.png"), new Image("img/ww9.png"),
                new Image("img/About.png"), new Image("img/MyBox.png"), new Image("img/DataTools.png"),
                new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                new Image("img/DocumentTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                new Image("img/Settings.png"), new Image("img/zz1.png"), new Image("img/jade.png")
        );
        List<ImageClipboard> clips = new ArrayList<>();
        for (int i = examples.size() - 1; i >= 0; --i) {
            ImageClipboard clip = ImageClipboard.create(examples.get(i), ImageClipboard.ImageSource.Example);
            if (clip == null) {
                continue;
            }
            clips.add(clip);
        }
        tableDefinition.insertList(clips);
    }

    /*
        static methods
     */
    public static void updateClipboards() {
        Platform.runLater(() -> {
            for (Window window : Window.getWindows()) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImagesInMyBoxClipboardController) {
                    try {
                        ((ImagesInMyBoxClipboardController) controller).loadClipboard();
                    } catch (Exception e) {
                    }
                } else if (controller instanceof ImageManufactureController) {
                    try {
                        ImageManufactureController imageController = (ImageManufactureController) controller;
                        if (imageController.operationsController.clipboardController != null) {
                            imageController.operationsController.clipboardController.loadClipboard();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
