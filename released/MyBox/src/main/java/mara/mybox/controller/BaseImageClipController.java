package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.data.ImageItem;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableMessageCell;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.InternalImages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class BaseImageClipController extends BaseSysTableController<ImageClipboard> {

    protected Image lastSystemClip;
    protected int thumbWidth = AppVariables.thumbnailWidth;

    @FXML
    protected TableColumn<ImageClipboard, Integer> widthColumn, heightColumn;
    @FXML
    protected TableColumn<ImageClipboard, String> sourceColumn;
    @FXML
    protected TableColumn<ImageClipboard, ImageClipboard> thumbColumn;
    @FXML
    protected TableColumn<ImageClipboard, Date> timeColumn;

    public BaseImageClipController() {
        baseTitle = message("ImagesInMyBoxClipboard");
    }

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
                            imageview.setImage(item.loadThumb(null));
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
            MyBoxLog.error(e);
        }
    }

    @Override
    public List<ImageClipboard> readPageData(FxTask currentTask, Connection conn) {
        try {
            ((TableImageClipboard) tableDefinition).clearInvalid(null, conn);
            return tableDefinition.queryConditions(conn, queryConditions, orderColumns,
                    pagination.startRowOfCurrentPage, pagination.pageSize);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return new ArrayList<>();
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private ImageClipboard clip;

            @Override
            protected boolean handle() {
                clip = ImageClipboard.add(this, file);
                return clip != null;
            }

            @Override
            protected void whenSucceeded() {
                refreshAction();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        Image clip = ImageClipboardTools.fetchImageInClipboard(false);
        if (clip == null) {
            popInformation(message("NoImageInClipboard"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        lastSystemClip = clip;
        task = new FxSingletonTask<Void>(this) {

            private ImageClipboard clipData;

            @Override
            protected boolean handle() {
//                    if (lastSystemClip != null && FxImageTools.sameImage(lastSystemClip, clip)) {
//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                popInformation(message("NoImageInClipboard"));
//                            }
//                        });
//                        return false;
//                    }
                clipData = ImageClipboard.add(this,
                        lastSystemClip, ImageClipboard.ImageSource.SystemClipBoard);
                return clipData != null;
            }

            @Override
            protected void whenSucceeded() {
                refreshAction();
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task);
    }

    @FXML
    public void examplesAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<ImageClipboard> clips;

            @Override
            protected boolean handle() {
                clips = new ArrayList<>();
                for (ImageItem item : InternalImages.all()) {
                    if (task == null || isCancelled()) {
                        return true;
                    }
                    Image image = item.readImage();
                    if (image != null) {
                        ImageClipboard clip = ImageClipboard.create(this,
                                image, ImageClipboard.ImageSource.Example);
                        if (clip == null) {
                            continue;
                        }
                        clips.add(clip);
//                        task.setInfo(item.getName());
                    }

                }
                tableDefinition.insertList(clips);
                return true;
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                refreshAction();
            }
        };
        start(task);
    }

    @FXML
    public void clipsPath() {
        File path = new File(AppPaths.getImageClipboardPath());
        browseURI(path.toURI());
    }

}
