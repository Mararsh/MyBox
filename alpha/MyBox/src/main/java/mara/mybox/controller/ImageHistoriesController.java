package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ImageHistoriesController extends BaseTableViewController<ImageEditHistory> {

    protected ImageEditorController imageController;
    protected TableImageEditHistory tableImageEditHistory;
    protected String imageHistoriesRootPath;
    protected File imageHistoriesPath;
    protected int maxEditHistories;

    @FXML
    protected TableColumn<ImageEditHistory, String> fileColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Image> imageColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Long> sizeColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Date> timeColumn;
    @FXML
    protected TableColumn<ImageEditHistory, String> typeColumn, descColumn;
    @FXML
    protected VBox historiesListBox;
    @FXML
    protected Button useButton, okHistoriesSizeButton;
    @FXML
    protected TextField maxHistoriesInput;
    @FXML
    protected CheckBox recordHistoriesCheck, loadCheck;
    @FXML
    protected Label fileLabel;

    public ImageHistoriesController() {
        baseTitle = message("EditHistories");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeColumn.setCellFactory(new TableFileSizeCell());

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("operationTime"));
            timeColumn.setCellFactory(new TableDateCell());

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(160);

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

            descColumn.setCellValueFactory(new PropertyValueFactory<>("desc"));

            imageColumn.setCellValueFactory(new PropertyValueFactory<>("thumbnail"));
            imageColumn.setCellFactory(new Callback<TableColumn<ImageEditHistory, Image>, TableCell<ImageEditHistory, Image>>() {
                @Override
                public TableCell<ImageEditHistory, Image> call(TableColumn<ImageEditHistory, Image> param) {

                    TableCell<ImageEditHistory, Image> cell = new TableCell<ImageEditHistory, Image>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        public void updateItem(Image item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            view.setFitWidth(AppVariables.thumbnailWidth);
                            view.setImage(item);
                            setGraphic(view);
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(viewButton, new Tooltip(message("View") + "\nCTRL+P / ALT+P"));
            NodeStyleTools.setTooltip(useButton, new Tooltip(message("Use") + "\n" + message("DoubleClick")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(ImageEditorController imageController) {
        try {
            this.imageController = imageController;
            this.sourceFile = imageController.sourceFile;
            if (sourceFile == null) {
                close();
                return;
            }

            baseTitle = imageController.baseTitle;

            fileLabel.setText(sourceFile.getAbsolutePath());
            setTitle(baseTitle + " - " + sourceFile.getAbsolutePath());

            tableImageEditHistory = new TableImageEditHistory();
            imageHistoriesRootPath = AppPaths.getImageHisPath();

            recordHistoriesCheck.setSelected(UserConfig.getBoolean("ImageHistoriesRecord", true));
            recordHistoriesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistoriesRecord", recordHistoriesCheck.isSelected());
                }
            });

            loadCheck.setSelected(UserConfig.getBoolean("ImageHistoriesRecordLoading", true));
            loadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistoriesRecordLoading", loadCheck.isSelected());
                }
            });

            maxEditHistories = UserConfig.getInt("MaxImageHistories", ImageEditHistory.Default_Max_Histories);
            if (maxEditHistories <= 0) {
                maxEditHistories = ImageEditHistory.Default_Max_Histories;
            }
            maxHistoriesInput.setText(maxEditHistories + "");
            maxHistoriesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(maxHistoriesInput.getText());
                        if (v >= 0) {
                            maxEditHistories = v;
                            UserConfig.setInt("MaxImageHistories", v);
                            maxHistoriesInput.setStyle(null);
                            okHistoriesSizeButton.setDisable(false);
                        } else {
                            maxHistoriesInput.setStyle(UserConfig.badStyle());
                            okHistoriesSizeButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        maxHistoriesInput.setStyle(UserConfig.badStyle());
                        okHistoriesSizeButton.setDisable(true);
                    }
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected boolean validFile() {
        return sourceFile != null
                && imageController != null
                && sourceFile.equals(imageController.sourceFile);
    }

    @Override
    public void itemDoubleClicked(Event event) {
        useHistory();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();
        boolean validFile = validFile();
        viewButton.setDisable(none);
        useButton.setDisable(none || !validFile);
        if (validFile) {
            imageController.hisSize = tableData.size();
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private List<ImageEditHistory> list;

            @Override
            protected boolean handle() {
                list = null;
                try (Connection conn = DerbyBase.getConnection()) {
                    list = tableImageEditHistory.read(conn, sourceFile);
                    if (list != null) {
                        for (ImageEditHistory his : list) {
                            if (task == null || isCancelled()) {
                                return true;
                            }
                            loadThumbnail(conn, his);
                        }
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            protected void loadThumbnail(Connection conn, ImageEditHistory his) {
                try {
                    if (his == null || his.getThumbnail() != null) {
                        return;
                    }
                    BufferedImage thumb;
                    File thumbfile = his.getThumbnailFile();
                    if (thumbfile == null || !thumbfile.exists()) {
                        String thumbname = FileNameTools.append(his.getHistoryFile().getAbsolutePath(), "_thumbnail");
                        thumb = ImageFileReaders.readImage(this,
                                his.getHistoryFile(), AppVariables.thumbnailWidth);
                        if (thumb != null) {
                            his.setThumbnailFile(new File(thumbname));
                            tableImageEditHistory.updateData(conn, his);
                        }
                    } else {
                        thumb = ImageFileReaders.readImage(this, thumbfile);
                    }
                    if (thumb != null) {
                        his.setThumbnail(SwingFXUtils.toFXImage(thumb, null));
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (list != null && !list.isEmpty()) {
                    tableData.setAll(list);
                } else {
                    tableData.clear();
                }
                bottomLabel.setText(message("Total") + ": " + tableData.size());
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<ImageEditHistory> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            clearAction();
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private int deletedCount = 0;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    for (ImageEditHistory item : selected) {
                        deletedCount += tableImageEditHistory.deleteData(conn, item);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Deleted") + ":" + deletedCount);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (deletedCount > 0) {
                    refreshAction();
                }
            }

        };
        start(task);

    }

    @FXML
    @Override
    public void clearAction() {
        if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private long deletedCount = 0;

            @Override
            protected boolean handle() {
                deletedCount = tableImageEditHistory.clearHistories(this, sourceFile);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (validFile()) {
                    imageController.setHistoryIndex(-1);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (deletedCount > 0) {
                    popInformation(message("Deleted") + ":" + deletedCount);
                }
                refreshAction();
            }

        };
        start(task);
    }

    @FXML
    public void useHistory() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        if (validFile()) {
            imageController.loadImageHistory(index);
        } else {
            popError(message("InvalidData"));
        }
    }

    @FXML
    protected void okHistoriesSize(ActionEvent event) {
        try {
            UserConfig.setInt("MaxImageHistories", maxEditHistories);
            popSuccessful();
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void viewAction() {
        ImageEditHistory selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image hisImage;

            @Override
            protected boolean handle() {
                try {
                    hisImage = selected.historyImage(this);
                    return hisImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (hisImage != null) {
                    ImagePopController.openImage(myController, hisImage);
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public boolean popAction() {
        viewAction();
        return true;
    }

    @FXML
    public void hisPath() {
        FxTask pathtask = new FxSingletonTask<Void>(this) {
            File path;

            @Override
            protected boolean handle() {
                MyBoxLog.console(sourceFile);
                path = tableImageEditHistory.path(sourceFile);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                MyBoxLog.console(path);
                if (path == null) {
                    path = new File(imageHistoriesRootPath);
                }
                MyBoxLog.console(path);
                browseURI(path.toURI());
            }

        };
        start(pathtask, false);

    }

    /*
        static
     */
    public static ImageHistoriesController open(ImageEditorController parent) {
        try {
            ImageHistoriesController controller = (ImageHistoriesController) WindowTools.branchStage(
                    parent, Fxmls.ImageHistoriesFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void updateList(File file) {
        try {
            if (file == null || !file.isFile() || !file.exists()) {
                return;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!window.isShowing()) {
                    continue;
                }
                Object object = window.getUserData();
                if (object == null || !(object instanceof ImageHistoriesController)) {
                    continue;
                }
                try {
                    ImageHistoriesController controller = (ImageHistoriesController) object;
                    if (!controller.isShowing() || !file.equals(controller.sourceFile)) {
                        continue;
                    }
                    controller.refreshAction();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
