package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import static mara.mybox.controller.MediaPlayerController.MiaoGuaiGuaiBenBen;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.MediaList;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableMediaList;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.cell.TableDurationCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-4
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlMediaTable extends BaseBatchTableController<MediaInformation> {

    protected String mediaListName;
    protected List<String> examples;
    protected boolean loadInfo = true;

    @FXML
    protected TableColumn<MediaInformation, String> addressColumn,
            resolutionColumn, audioColumn, videoColumn;
    @FXML
    protected TableColumn<MediaInformation, Long> durationColumn;
    @FXML
    protected Button linkButton;

    public ControlMediaTable() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
    }

    @Override
    public void initControls() {
        super.initControls();

        examples = new ArrayList();
        examples.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            if (addressColumn != null) {
                addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            }
            if (durationColumn != null) {
                durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
                durationColumn.setCellFactory(new TableDurationCell());
            }
            if (resolutionColumn != null) {
                resolutionColumn.setCellValueFactory(new PropertyValueFactory<>("resolution"));
            }
            if (audioColumn != null) {
                audioColumn.setCellValueFactory(new PropertyValueFactory<>("audioEncoding"));
            }
            if (videoColumn != null) {
                videoColumn.setCellValueFactory(new PropertyValueFactory<>("videoEncoding"));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected MediaInformation create(FxTask currentTask, File file) {
        try {
            MediaInformation info = new MediaInformation(file);
            info.setDuration(-1);
            if (loadInfo) {
                loadMediaInfo(info);
            }
            return info;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            popError(message("FailOpenMedia"));
            return null;
        }
    }

    protected MediaInformation create(String address) {
        try {
            MediaInformation info = new MediaInformation(address);
            if (loadInfo) {
                loadMediaInfo(info);
            }
            return info;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.debug(e);
            return null;
        }
    }

    protected void loadMediaInfo(MediaInformation info) {
        FxTask infoTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Media media = new Media(info.getURI().toString());
                    if (media.getError() == null) {
                        media.setOnError(new Runnable() {
                            @Override
                            public void run() {
                                handleMediaError(info, media.getError());
                                cancel();
                            }
                        });
                    } else {
                        handleMediaError(info, media.getError());
                        cancel();
                        return false;
                    }
                    MediaPlayer player = new MediaPlayer(media);
                    if (player.getError() == null) {
                        player.setOnError(new Runnable() {
                            @Override
                            public void run() {
                                handleMediaError(info, player.getError());
                                cancel();
                            }
                        });
                    } else {
                        handleMediaError(info, player.getError());
                        cancel();
                        return false;
                    }

                    player.setOnReady(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                info.readMediaInfo(media);
                                player.dispose();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tableView.refresh();
                                        updateTableLabel();
                                    }
                                });

                            } catch (Exception e) {
                                popError(message("FailOpenMedia"));
                                MyBoxLog.debug(e);
                            }
                        }
                    });

                    return true;

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            @Override
            protected void whenFailed() {
                popError(error);
                tableView.refresh();
                updateTableLabel();
            }

        };
        start(infoTask, false);
    }

    public void handleMediaError(MediaInformation info, MediaException exception) {
        String msg = MediaInformation.exceptionMessage(exception);
        popError(info.getAddress() + "\n" + msg);
        String errorMsg = exception.getMessage();
        if (errorMsg.contains("ERROR_MEDIA_AUDIO_FORMAT_UNSUPPORTED")) {
            info.setAudioEncoding(message("NotSupport"));
        } else if (errorMsg.contains("ERROR_MEDIA_VIDEO_FORMAT_UNSUPPORTED")) {
            info.setVideoEncoding(message("NotSupport"));
        }
        info.setFinish(true);
    }

    @Override
    public void popError(String error) {
        if (error == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                parentController.popError(error);
            }
        });
    }

    @Override
    protected void tableChanged() {
        super.tableChanged();
        if (saveButton != null) {
            saveButton.setDisable(tableData.isEmpty());
        }
    }

    @Override
    public void countSize(boolean reset) {
        updateTableLabel();
    }

    @Override
    public void updateTableLabel() {
        long d = 0;
        totalFilesNumber = totalFilesSize = 0;
        for (MediaInformation m : tableData) {
            totalFilesNumber++;
            if (m.getFileSize() > 0) {
                totalFilesSize += m.getFileSize();
            }
            if (m.getDuration() > 0) {
                d += m.getDuration();
            }
        }
        String s = message("TotalDuration") + ": " + DateTools.timeMsDuration(d) + "  "
                + MessageFormat.format(message("TotalFilesNumberSize"),
                        totalFilesNumber, FileTools.showFileSize(totalFilesSize));
        tableLabel.setText(s);
    }

    @Override
    public void doubleClicked(Event event) {
        int index = selectedIndix();
        if (index < 0 || index > tableData.size() - 1) {
            return;
        }
        if (parentController != null && (parentController instanceof MediaPlayerController)) {
            ((MediaPlayerController) parentController).playIndex(index);
        }
    }

    @FXML
    public void addLinkAction() {
        try {
            // http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8
            TextInputDialog dialog = new TextInputDialog("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
            dialog.setTitle(message("HTTPLiveStreaming"));
            dialog.setHeaderText(message("InputAddress"));
            dialog.setContentText("HLS(.m3u8)");
            dialog.getEditor().setPrefWidth(500);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<String> result = dialog.showAndWait();
            if (result == null || !result.isPresent()) {
                return;
            }
            String address = result.get();
            addLink(address);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8089866
    // Some link can hang or crash whole application
    public void addLink(String address) {
        try {

            MediaInformation info = create(address);
            if (info == null) {
                return;
            }
            isSettingValues = true;
            tableData.add(info);
            isSettingValues = false;
            tableView.refresh();
            tableChanged();

            VisitHistoryTools.visitStreamMedia(address);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void showMediasLinkMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        RecentVisitMenu menu = new RecentVisitMenu(this, event, false) {
            @Override
            public List<VisitHistory> recentFiles() {
                List<VisitHistory> recent = VisitHistoryTools.getRecentStreamMedia();
                return recent;
            }

            @Override
            public List<String> paths() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return null;
            }

            @Override
            public void handleSelect() {
                addLinkAction();
            }

            @Override
            public void handleFile(String address) {
                addLink(address);
            }

            @Override
            public void handlePath(String fname) {
            }

        };
        menu.setExamples(examples);
        menu.pop();
    }

    @FXML
    public void pickMediasLink(Event event) {
        if (MenuTools.isPopMenu("RecentVisit") || AppVariables.fileRecentNumber <= 0) {
            addLinkAction();
        } else {
            showMediasLinkMenu(event);
        }
    }

    @FXML
    public void popMediasLink(Event event) {
        if (MenuTools.isPopMenu("RecentVisit")) {
            showMediasLinkMenu(event);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        tableView.refresh();
    }

    @FXML
    @Override
    public boolean infoAction() {
        MediaInformation info = selectedItem();
        if (info == null) {
            return false;
        }
        popInfo(info);
        return true;
    }

    public void popInfo(MediaInformation info) {
        if (info == null) {
            return;
        }
        if (info.getHtml() == null) {
            info.makeHtml(null);
        }
        HtmlTableController.open(info.getHtml());
    }

    @FXML
    @Override
    public void saveAction() {
        if (mediaListName == null || mediaListName.isBlank()) {
            if (tableData.isEmpty()) {
                tableLabel.setText("");
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
            mediaListName = result.get().trim();
        }
        if (TableMediaList.set(mediaListName, tableData)) {
            tableLabel.setText(message("MediaList") + ": " + mediaListName);
            if (parentController != null) {
                parentController.popSuccessful();
                if (parentController instanceof MediaListController) {
                    ((MediaListController) parentController).update(mediaListName);
                }
            }
        } else {
            if (parentController != null) {
                parentController.popFailed();
            }
        }
    }

    public void loadMedias(MediaList mediaList) {
        tableData.clear();
        if (mediaList == null || mediaList.getMedias() == null) {
            tableLabel.setText("");
            return;
        }
        mediaListName = mediaList.getName();
        tableLabel.setText(message("MediaList") + ": " + mediaListName);

        isSettingValues = true;
        tableData.addAll(mediaList.getMedias());
        tableView.refresh();
        isSettingValues = false;
        tableChanged();

    }

    public void loadMiaoSounds() {
        if (task != null) {
            task.cancel();
        }
        tableData.clear();
        task = new FxSingletonTask<Void>(this) {

            List<File> miaos;

            @Override
            protected boolean handle() {
                miaos = new ArrayList();
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao1.mp3", "sound", "guaiMiao1.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao2.mp3", "sound", "guaiMiao2.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao3.mp3", "sound", "guaiMiao3.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao4.mp3", "sound", "guaiMiao4.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao5.mp3", "sound", "guaiMiao5.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao6.mp3", "sound", "guaiMiao6.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiMiao7.mp3", "sound", "guaiMiao7.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat1.mp3", "sound", "eat1.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat2.mp3", "sound", "eat2.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat3.mp3", "sound", "eat3.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat5.mp3", "sound", "eat5.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat6.mp3", "sound", "eat6.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat7.mp3", "sound", "eat7.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat8.mp3", "sound", "eat8.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat9.mp3", "sound", "eat9.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/eat10.mp3", "sound", "eat10.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiHulu.mp3", "sound", "guaiHulu.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/guaiHuluTian.mp3", "sound", "guaiHuluTian.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/benMiao11.mp3", "sound", "benMiao11.mp3"));
                miaos.add(mara.mybox.fxml.FxFileTools.getInternalFile("/sound/benMiao2.mp3", "sound", "benMiao2.mp3"));

                List<MediaInformation> miaosInfo = new ArrayList();
                for (File file : miaos) {
                    miaosInfo.add(new MediaInformation(file));
                }
                TableMediaList.set(MiaoGuaiGuaiBenBen, miaosInfo);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (error != null) {
                    popError(error);
                }
                if (miaos != null) {
                    addFiles(0, miaos);
                }

            }

        };
        start(task);
    }

}
