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
        loadScene(Fxmls.MediaPlayerFxml);
    }

    @FXML
    protected void openMediaList(ActionEvent event) {
        loadScene(Fxmls.MediaListFxml);
    }

    @FXML
    protected void openScreenRecorder(ActionEvent event) {
        loadScene(Fxmls.FFmpegScreenRecorderFxml);
    }

    @FXML
    protected void openFFmpegMergeImages(ActionEvent event) {
        loadScene(Fxmls.FFmpegMergeImagesFxml);
    }

    @FXML
    protected void openFFmpegMergeImageFiles(ActionEvent event) {
        loadScene(Fxmls.FFmpegMergeImageFilesFxml);
    }

    @FXML
    protected void openFFmpegInformation(ActionEvent event) {
        loadScene(Fxmls.FFmpegInformationFxml);
    }

    @FXML
    protected void openFFmpegProbeMediaInformation(ActionEvent event) {
        loadScene(Fxmls.FFmpegProbeMediaInformationFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaFiles(ActionEvent event) {
        loadScene(Fxmls.FFmpegConvertMediaFilesFxml);
    }

    @FXML
    protected void openFFmpegConvertMediaStreams(ActionEvent event) {
        loadScene(Fxmls.FFmpegConvertMediaStreamsFxml);
    }

    @FXML
    protected void ImagesInMyBoxClipboard(ActionEvent event) {
        ImageInMyBoxClipboardController.oneOpen();
    }

    @FXML
    protected void ImagesInSystemClipboard(ActionEvent event) {
        ImageInSystemClipboardController.oneOpen();
    }

    @FXML
    protected void openGameElimniation(ActionEvent event) {
        loadScene(Fxmls.GameElimniationFxml);
    }

    @FXML
    protected void openGameMine(ActionEvent event) {
        loadScene(Fxmls.GameMineFxml);
    }

}
