package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_File extends MainMenuController_Image {

    @FXML
    protected void openFilesRename(ActionEvent event) {
        loadScene(Fxmls.FilesRenameFxml);
    }

    @FXML
    protected void openDirectorySynchronize(ActionEvent event) {
        loadScene(Fxmls.DirectorySynchronizeFxml);
    }

    @FXML
    protected void openFilesArrangement(ActionEvent event) {
        loadScene(Fxmls.FilesArrangementFxml);
    }

    @FXML
    protected void openDeleteEmptyDirectories(ActionEvent event) {
        loadScene(Fxmls.FilesDeleteEmptyDirFxml);
    }

    @FXML
    protected void openDeleteSysTempPath(ActionEvent event) {
        loadScene(Fxmls.FilesDeleteSysTempFxml);
    }

    @FXML
    protected void openDeleteNestedDirectories(ActionEvent event) {
        loadScene(Fxmls.FilesDeleteNestedDirFxml);
    }

    @FXML
    protected void openAlarmClock(ActionEvent event) {
        loadScene(Fxmls.AlarmClockFxml);
    }

    @FXML
    protected void openFileCut(ActionEvent event) {
        loadScene(Fxmls.FileCutFxml);
    }

    @FXML
    protected void openFilesMerge(ActionEvent event) {
        loadScene(Fxmls.FilesMergeFxml);
    }

    @FXML
    protected void openFilesDelete(ActionEvent event) {
        loadScene(Fxmls.FilesDeleteFxml);
    }

    @FXML
    protected void openFilesCopy(ActionEvent event) {
        loadScene(Fxmls.FilesCopyFxml);
    }

    @FXML
    protected void openFilesMove(ActionEvent event) {
        loadScene(Fxmls.FilesMoveFxml);
    }

    @FXML
    protected void openFilesFind(ActionEvent event) {
        loadScene(Fxmls.FilesFindFxml);
    }

    @FXML
    protected void openBarcodeCreator(ActionEvent event) {
        loadScene(Fxmls.BarcodeCreatorFxml);
    }

    @FXML
    protected void openBarcodeDecoder(ActionEvent event) {
        loadScene(Fxmls.BarcodeDecoderFxml);
    }

    @FXML
    protected void openMessageDigest(ActionEvent event) {
        loadScene(Fxmls.MessageDigestFxml);
    }

    @FXML
    protected void Base64Conversion(ActionEvent event) {
        loadScene(Fxmls.Base64Fxml);
    }

    @FXML
    protected void openFilesCompare(ActionEvent event) {
        loadScene(Fxmls.FilesCompareFxml);
    }

    @FXML
    protected void openFilesArchiveCompress(ActionEvent event) {
        loadScene(Fxmls.FilesArchiveCompressFxml);
    }

    @FXML
    protected void openFilesCompressBatch(ActionEvent event) {
        loadScene(Fxmls.FilesCompressBatchFxml);
    }

    @FXML
    protected void openFileDecompressUnarchive(ActionEvent event) {
        loadScene(Fxmls.FileDecompressUnarchiveFxml);
    }

    @FXML
    protected void openFilesDecompressUnarchiveBatch(ActionEvent event) {
        loadScene(Fxmls.FilesDecompressUnarchiveBatchFxml);
    }

    @FXML
    protected void openFilesRedundancy(ActionEvent event) {
        loadScene(Fxmls.FilesRedundancyFxml);
    }

}
