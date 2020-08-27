package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import static mara.mybox.controller.MediaPlayerController.MiaoGuaiGuaiBenBen;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.MediaList;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableMedia;
import mara.mybox.db.TableMediaList;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.TableDurationCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-4
 * @Description
 * @License Apache License Version 2.0
 */
public class MediaTableController extends BatchTableController<MediaInformation> {

    protected String mediaListName;
    protected List<String> examples;

    @FXML
    protected TableColumn<MediaInformation, String> addressColumn,
            resolutionColumn, audioColumn, videoColumn;
    @FXML
    protected TableColumn<MediaInformation, Long> durationColumn;
    @FXML
    protected Button linkButton;

    public MediaTableController() {

        SourceFileType = VisitHistory.FileType.Media;
        SourcePathType = VisitHistory.FileType.Media;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;
        AddFileType = VisitHistory.FileType.Media;
        AddPathType = VisitHistory.FileType.Media;

        sourceExtensionFilter = CommonFxValues.JdkMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        super.initializeNext();

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
            logger.error(e.toString());
        }
    }

    @Override
    protected MediaInformation create(File file) {
        try {
            MediaInformation info = new MediaInformation(file);
            readMediaInfo(info);
            return info;
        } catch (Exception e) {
            logger.debug(e.toString());
            popError(message("FailOpenMedia"));
            return null;
        }
    }

    protected MediaInformation create(String address) {
        try {
            MediaInformation info = TableMedia.read(address);
            if (info != null) {
                return info;
            }
            parentController.popInformation(message("ReadingStreamMedia...") + "\n" + address, 6000);
            info = new MediaInformation(address);
            readMediaInfo(info);
            return info;
        } catch (Exception e) {
            popError(e.toString());
            logger.debug(e.toString());
            return null;
        }
    }

    protected void readMediaInfo(MediaInformation info) {

        synchronized (this) {
            try {
                Task infoTask = new Task<Void>() {

                    private String error;

                    @Override
                    protected Void call() {
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
                                return null;
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
                                return null;
                            }

                            player.setOnReady(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        readMediaInfo(info, media);
                                        player.dispose();
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                tableView.refresh();
                                                updateLabel();
                                            }
                                        });

                                    } catch (Exception e) {
                                        popError(message("FailOpenMedia"));
                                        logger.debug(e.toString());
                                    }
                                }
                            });

                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        if (error != null) {
                            popError(error);
                        }

                    }

                };
                parentController.openHandlingStage(infoTask, Modality.WINDOW_MODAL,
                        message("ReadingMedia...") + "\n" + info.getURI().toString());
                Thread thread = new Thread(infoTask);
                thread.setDaemon(true);
                thread.start();

            } catch (Exception e) {
                popError(message("FailOpenMedia"));
                logger.debug(e.toString());
            }
        }
    }

    public boolean readMediaInfo(final MediaInformation info, Media media) {
        try {
            if (info == null || info.getAddress() == null || media == null) {
                return false;
            }
            Duration duration = media.getDuration();
            if (duration != null) {
                info.setDuration(Math.round(duration.toMillis()));
            }

            StringBuilder s = new StringBuilder();
            s.append(message("Duration")).append(": ").append(DateTools.datetimeMsDuration(info.getDuration())).append("\n");
            if (media.getWidth() > 0 && media.getHeight() > 0) {
                info.setWidth(media.getWidth());
                info.setHeight(media.getHeight());
                s.append(message("Resolution")).append(": ").append(info.getResolution()).append("\n");
            }
            if (info.getFileSize() > 0) {
                s.append(message("Size")).append(": ").append(FileTools.showFileSize(info.getFileSize())).append("\n");
            }

            Map<String, Object> meta = media.getMetadata();
            if (meta != null && !meta.isEmpty()) {
                for (String mk : meta.keySet()) {
                    s.append(mk).append(": ").append(meta.get(mk).toString()).append("\n");
                }
            }
            Map<String, Duration> markers = media.getMarkers();
            if (markers != null && !markers.isEmpty()) {
                for (String mk : markers.keySet()) {
                    s.append(mk).append(": ").append(markers.get(mk).toString()).append("\n");
                }
            }
            List<Track> tracks = media.getTracks();

            if (tracks != null && !tracks.isEmpty()) {
                for (Track track : tracks) {
                    String name = "";
                    s.append("Track: ").append(track.getTrackID()).append("\n");

                    if (track.getName() != null) {
                        s.append("Name: ").append(track.getName()).append("\n");
                        name = track.getName();
                    }
                    if (track.getLocale() != null) {
                        s.append("Locale: ").append(track.getLocale().getDisplayName()).append("\n");
                    }
                    Map<String, Object> trackMeta = track.getMetadata();
                    if (trackMeta != null && !trackMeta.isEmpty()) {
                        for (String mk : trackMeta.keySet()) {
                            s.append(mk).append(": ").append(trackMeta.get(mk).toString()).append("\n");
                            if (mk.toLowerCase().contains("encoding")) {
                                if (name.toLowerCase().contains("audio")) {
                                    info.setAudioEncoding(name + " " + trackMeta.get(mk).toString());
                                } else if (name.toLowerCase().contains("video")) {
                                    info.setVideoEncoding(name + " " + trackMeta.get(mk).toString());
                                }
                            }
                        }
                    }
                }
            }
            info.setInfo(s.toString());
            makeHtml(info, media);
            TableMedia.write(info);
            return true;
        } catch (Exception e) {
            popError(message("FailOpenMedia"));
            logger.debug(e.toString());
            return false;
        }

    }

    public void makeHtml(MediaInformation info, Media media) {
        if (info == null) {
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append("<h1  class=\"center\">").append(info.getAddress()).append("</h1>\n");
        s.append("<hr>\n");
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Name"), message("Value")));
        StringTable table = new StringTable(names);
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Duration"), DateTools.datetimeMsDuration(info.getDuration())));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Resolution"), info.getResolution()));
        table.add(row);
        if (info.getFileSize() > 0) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("Size"), FileTools.showFileSize(info.getFileSize())));
            table.add(row);
        }
        if (info.getVideoEncoding() != null) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("VideoEncoding"), info.getVideoEncoding()));
            table.add(row);
        }
        if (info.getAudioEncoding() != null) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("AudioEncoding"), info.getAudioEncoding()));
            table.add(row);
        }
        s.append(StringTable.tableDiv(table));

        if (media == null) {
            info.setHtml(s.toString());
            return;
        }
        Map<String, Object> meta = media.getMetadata();
        if (meta != null && !meta.isEmpty()) {
            s.append("<h2  class=\"center\">").append("MetaData").append("</h2>\n");
            names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Meta"), message("Value")));
            table = new StringTable(names);
            for (String mk : meta.keySet()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(mk, meta.get(mk).toString()));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
        }

        Map<String, Duration> markers = media.getMarkers();
        if (markers != null && !markers.isEmpty()) {
            s.append("<h2  class=\"center\">").append("Markers").append("</h2>\n");
            names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Marker"), message("Value")));
            table = new StringTable(names);
            for (String mk : markers.keySet()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(mk, markers.get(mk).toString()));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
        }

        List<Track> tracks = media.getTracks();
        if (tracks != null && !tracks.isEmpty()) {
            s.append("<h2  class=\"center\">").append("Tracks").append("</h2>\n");
            for (Track track : tracks) {
                s.append("<h3  class=\"center\">").append("trackID:").append(track.getTrackID()).append("</h3>\n");
                names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Name"), message("Value")));
                table = new StringTable(names);
                if (track.getName() != null) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList("Name", track.getName()));
                    table.add(row);
                }
                if (track.getLocale() != null) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList("Locale", track.getLocale().getDisplayName()));
                    table.add(row);
                }
                Map<String, Object> trackMeta = track.getMetadata();
                if (trackMeta != null && !trackMeta.isEmpty()) {
                    for (String mk : trackMeta.keySet()) {
                        row = new ArrayList<>();
                        row.addAll(Arrays.asList(mk, trackMeta.get(mk).toString()));
                        table.add(row);
                    }
                }
                s.append(StringTable.tableDiv(table));
            }
        }
        info.setHtml(s.toString());

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
    }

    @Override
    public void popError(String error) {
        if (error == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            public void run() {
                parentController.popError(error);
            }
        });
    }

    @Override
    public void tableSelected() {
        super.tableSelected();
        MediaInformation selected = (MediaInformation) tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getInfo() == null) {
            return;
        }
        parentController.popInformation(selected.getInfo(), 5000);
    }

    @Override
    protected void tableChanged() {
        super.tableChanged();
        if (saveButton != null) {
            saveButton.setDisable(tableData.isEmpty());
        }
    }

    @Override
    public void countSize() {
        updateLabel();
    }

    @Override
    public void updateLabel() {
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
    public void itemDoubleClicked() {
        int index = tableView.getSelectionModel().getSelectedIndex();
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
            if (!result.isPresent()) {
                return;
            }
            String address = result.get();
            addLink(address);

        } catch (Exception e) {
            logger.error(e.toString());
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

            VisitHistory.visitStreamMedia(address);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popMediasLink(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        RecentVisitMenu menu = new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                List<VisitHistory> recent = VisitHistory.getRecentStreamMedia();
                return recent;
            }

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
    public void refreshAction() {
        tableView.refresh();
    }

    @FXML
    @Override
    public void infoAction() {
        MediaInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }
        popInfo(info);
    }

    public void popInfo(MediaInformation info) {
        if (info == null) {
            return;
        }
        if (info.getHtml() == null) {
            makeHtml(info, null);
        }
        FxmlStage.openHtmlViewer(null, info.getHtml());
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
        synchronized (this) {
            if (task != null) {
                return;
            }
            tableData.clear();
            try {
                task = new SingletonTask<Void>() {

                    List<File> miaos;

                    @Override
                    protected boolean handle() {

//                        MediaList list = TableMediaList.read(MiaoGuaiGuaiBenBen);
//                        if (list != null && list.getMedias() != null && !list.getMedias().isEmpty()) {
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    task = null;
//                                    mediaListName = null;
//                                    tableLabel.setText(message("MiaoSounds"));
//                                    isSettingValues = true;
//                                    tableData.addAll(list.getMedias());
//                                    tableView.refresh();
//                                    isSettingValues = false;
//                                    tableChanged();
//                                }
//                            });
//                            return true;
//                        }
                        miaos = new ArrayList();
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao1.mp3", "sound", "guaiMiao1.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao2.mp3", "sound", "guaiMiao2.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao3.mp3", "sound", "guaiMiao3.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao4.mp3", "sound", "guaiMiao4.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao5.mp3", "sound", "guaiMiao5.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao6.mp3", "sound", "guaiMiao6.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiMiao7.mp3", "sound", "guaiMiao7.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat1.mp3", "sound", "eat1.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat2.mp3", "sound", "eat2.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat3.mp3", "sound", "eat3.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat5.mp3", "sound", "eat5.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat6.mp3", "sound", "eat6.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat7.mp3", "sound", "eat7.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat8.mp3", "sound", "eat8.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat9.mp3", "sound", "eat9.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/eat10.mp3", "sound", "eat10.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiHulu.mp3", "sound", "guaiHulu.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/guaiHuluTian.mp3", "sound", "guaiHuluTian.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/benMiao11.mp3", "sound", "benMiao11.mp3"));
                        miaos.add(FxmlControl.getInternalFile("/sound/benMiao2.mp3", "sound", "benMiao2.mp3"));

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
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();

            } catch (Exception e) {
                popError(message("FailOpenMedia"));
                logger.debug(e.toString());
            }
        }

    }

    @FXML
    public void newAction() {
        tableView.getSelectionModel().clearSelection();

        mediaListName = null;
        tableLabel.setText("");
        if (parentController != null) {
            if (parentController instanceof MediaListController) {
                ((MediaListController) parentController).clearSelection();
            }
        }
    }

}
