package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Media extends MainMenuController_Data {

    @FXML
    protected void openMediaPlayer(ActionEvent event) {
        openScene(Fxmls.MediaPlayerFxml);
    }

    @FXML
    protected void openMediaList(ActionEvent event) {
        openScene(Fxmls.MediaListFxml);
    }

    @FXML
    protected void openScreenRecorder(ActionEvent event) {
        openScene(Fxmls.FFmpegScreenRecorderFxml);
    }

    @FXML
    protected void openFFmpegMergeImages(ActionEvent event) {
        openScene(Fxmls.FFmpegMergeImagesFxml);
    }

    @FXML
    protected void openFFmpegMergeImageFiles(ActionEvent event) {
        openScene(Fxmls.FFmpegMergeImageFilesFxml);
    }

    @FXML
    protected void openFFmpegInformation(ActionEvent event) {
        openScene(Fxmls.FFmpegInformationFxml);
    }

    @FXML
    protected void openFFmpegProbeMediaInformation(ActionEvent event) {
        openScene(Fxmls.FFmpegProbeMediaInformationFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaFiles(ActionEvent event) {
        openScene(Fxmls.FFmpegConvertMediaFilesFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaStreams(ActionEvent event) {
        openScene(Fxmls.FFmpegConvertMediaStreamsFxml);
    }

    @FXML
    protected void openGameElimniation(ActionEvent event) {
        openScene(Fxmls.GameElimniationFxml);
    }

    @FXML
    protected void openGameMine(ActionEvent event) {
        openScene(Fxmls.GameMineFxml);
    }

}
