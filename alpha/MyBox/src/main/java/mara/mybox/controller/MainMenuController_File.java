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
        openScene(Fxmls.FilesRenameFxml);
    }

    @FXML
    protected void openDirectorySynchronize(ActionEvent event) {
        openScene(Fxmls.DirectorySynchronizeFxml);
    }

    @FXML
    protected void openFilesArrangement(ActionEvent event) {
        openScene(Fxmls.FilesArrangementFxml);
    }

    @FXML
    protected void openDeleteEmptyDirectories(ActionEvent event) {
        openScene(Fxmls.FilesDeleteEmptyDirFxml);
    }

    @FXML
    protected void openDeleteSysTempPath(ActionEvent event) {
        openScene(Fxmls.FilesDeleteJavaTempFxml);
    }

    @FXML
    protected void openDeleteNestedDirectories(ActionEvent event) {
        openScene(Fxmls.FilesDeleteNestedDirFxml);
    }

    @FXML
    protected void openAlarmClock(ActionEvent event) {
        openScene(Fxmls.AlarmClockFxml);
    }

    @FXML
    protected void openFileCut(ActionEvent event) {
        openScene(Fxmls.FileCutFxml);
    }

    @FXML
    protected void openFilesMerge(ActionEvent event) {
        openScene(Fxmls.FilesMergeFxml);
    }

    @FXML
    protected void openFilesDelete(ActionEvent event) {
        openScene(Fxmls.FilesDeleteFxml);
    }

    @FXML
    protected void openFilesCopy(ActionEvent event) {
        openScene(Fxmls.FilesCopyFxml);
    }

    @FXML
    protected void openFilesMove(ActionEvent event) {
        openScene(Fxmls.FilesMoveFxml);
    }

    @FXML
    protected void openFilesFind(ActionEvent event) {
        openScene(Fxmls.FilesFindFxml);
    }

    @FXML
    protected void openBarcodeCreator(ActionEvent event) {
        openScene(Fxmls.BarcodeCreatorFxml);
    }

    @FXML
    protected void openBarcodeDecoder(ActionEvent event) {
        openScene(Fxmls.BarcodeDecoderFxml);
    }

    @FXML
    protected void openMessageDigest(ActionEvent event) {
        openScene(Fxmls.MessageDigestFxml);
    }

    @FXML
    protected void Base64Conversion(ActionEvent event) {
        openScene(Fxmls.Base64Fxml);
    }

    @FXML
    protected void openFilesCompare(ActionEvent event) {
        openScene(Fxmls.FilesCompareFxml);
    }

    @FXML
    protected void openFilesArchiveCompress(ActionEvent event) {
        openScene(Fxmls.FilesArchiveCompressFxml);
    }

    @FXML
    protected void openFilesCompressBatch(ActionEvent event) {
        openScene(Fxmls.FilesCompressBatchFxml);
    }

    @FXML
    protected void openFileDecompressUnarchive(ActionEvent event) {
        openScene(Fxmls.FileDecompressUnarchiveFxml);
    }

    @FXML
    protected void openFilesDecompressUnarchiveBatch(ActionEvent event) {
        openScene(Fxmls.FilesDecompressUnarchiveBatchFxml);
    }

    @FXML
    protected void openFilesRedundancy(ActionEvent event) {
        openScene(Fxmls.FilesRedundancyFxml);
    }

}
