package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.MediaList;
import mara.mybox.db.table.TableMediaList;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class MediaListController extends BaseTableViewController<MediaList> {

    protected MediaPlayerController playerController;

    @FXML
    protected TableColumn<MediaList, String> nameColumn;
    @FXML
    protected ControlMediaTable tableController;

    public MediaListController() {
        baseTitle = message("ManageMediaLists");
        TipsLabelKey = message("MediaPlayerSupports");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableController.setParentController(this);

            loadList(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void doubleClicked(Event event) {
        playAction();
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();

        MediaList selected = selectedItem();
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
            playerController = (MediaPlayerController) openStage(Fxmls.MediaPlayerFxml);
        } else {
            playerController.getMyStage().show();
        }
        MediaList selected = selectedItem();
        if (selected == null) {
            return;
        }
        playerController.tableController.loadMedias(selected);
        playerController.getMyStage().requestFocus();

    }

    @FXML
    @Override
    public void saveAsAction() {
        MediaList selected = selectedItem();
        if (selected == null || selected.getMedias() == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(Languages.message("ManageMediaLists"));
        dialog.setHeaderText(Languages.message("InputMediaListName"));
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
                popError(Languages.message("AlreadyExisted"));
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
