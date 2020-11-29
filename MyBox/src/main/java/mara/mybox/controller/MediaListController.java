package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.MediaList;
import mara.mybox.db.TableMediaList;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class MediaListController extends BaseController {

    protected MediaPlayerController playerController;
    protected ObservableList<MediaList> tableData;

    @FXML
    protected TableView<MediaList> tableView;
    @FXML
    protected TableColumn<MediaList, String> nameColumn;
    @FXML
    protected MediaTableController tableController;
    @FXML
    protected ImageView supportTipsView;

    public MediaListController() {
        baseTitle = AppVariables.message("ManageMediaLists");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData = FXCollections.observableArrayList();
            tableController.setParentController(this);

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkSelected();
                }
            });
            checkSelected();

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        playAction();
                    }
                }
            });

            tableView.setItems(tableData);
            loadList(null);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        FxmlControl.setTooltip(supportTipsView, new Tooltip(message("MediaPlayerSupports")));
        supportTipsView.applyCss();

    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        MediaList selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            playButton.setDisable(true);
            saveAsButton.setDisable(true);
            deleteButton.setDisable(true);
            tableController.clearFilesAction();
        } else {
            playButton.setDisable(false);
            saveAsButton.setDisable(false);
            deleteButton.setDisable(false);
            tableController.loadMedias(selected);
        }
    }

    protected void clearSelection() {
        tableView.getSelectionModel().clearSelection();
    }

    public void loadList(String selectName) {
        tableData.clear();
        tableData.addAll(TableMediaList.read());
        if (selectName != null) {
            for (int i = 0; i < tableData.size(); ++i) {
                MediaList list = tableData.get(i);
                if (list.getName().equals(selectName)) {
                    tableView.getSelectionModel().select(list);
                    return;
                }
            }
        }
    }

    @FXML
    @Override
    public void playAction() {
        if (playerController == null || playerController.getMyStage() == null) {
            playerController = (MediaPlayerController) openStage(CommonValues.MediaPlayerFxml);
        } else {
            playerController.getMyStage().show();
        }
        MediaList selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        playerController.tableController.loadMedias(selected);
        playerController.getMyStage().toFront();

    }

    @FXML
    @Override
    public void saveAsAction() {
        MediaList selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getMedias() == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(message("ManageMediaLists"));
        dialog.setHeaderText(message("InputMediaListName"));
        dialog.setContentText("");
        dialog.getEditor().setPrefWidth(400);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isBlank()) {
            return;
        }
        String newName = result.get().trim();
        for (MediaList list : tableData) {
            if (list.getName().equals(newName)) {
                popError(message("AlreadyExisted"));
                return;
            }
        }
        if (TableMediaList.set(newName, selected.getMedias())) {
            popSuccessful();
            tableData.add(MediaList.create().setName(newName).setMedias(selected.getMedias()));
        } else {
            popFailed();
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            if (TableMediaList.delete(tableData.get(index).getName())) {
                tableData.remove(index);
            }
        }
        tableView.refresh();
        isSettingValues = false;
        checkSelected();
    }

    @FXML
    public void update(String name) {
        List<MediaInformation> medias = new ArrayList();
        medias.addAll(tableController.tableData);
        for (int i = 0; i < tableData.size(); ++i) {
            MediaList list = tableData.get(i);
            if (list.getName().equals(name)) {
                list.setMedias(medias);
                return;
            }
        }
        tableData.add(MediaList.create().setName(name).setMedias(medias));
    }

    /*
        get/set
     */
    public MediaPlayerController getPlayerController() {
        return playerController;
    }

    public void setPlayerController(MediaPlayerController playerController) {
        this.playerController = playerController;
    }

}
