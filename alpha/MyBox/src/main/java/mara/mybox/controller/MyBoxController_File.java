package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_File extends MyBoxController_Image {

    @FXML
    protected void showFileMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem filesArrangement = new MenuItem(Languages.message("FilesArrangement"));
        filesArrangement.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesArrangementFxml);
        });

        MenuItem dirSynchronize = new MenuItem(Languages.message("DirectorySynchronize"));
        dirSynchronize.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DirectorySynchronizeFxml);
        });

        MenuItem filesRename = new MenuItem(Languages.message("FilesRename"));
        filesRename.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesRenameFxml);
        });

        MenuItem fileCut = new MenuItem(Languages.message("FileCut"));
        fileCut.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FileCutFxml);
        });

        MenuItem filesMerge = new MenuItem(Languages.message("FilesMerge"));
        filesMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesMergeFxml);
        });

        MenuItem filesCopy = new MenuItem(Languages.message("FilesCopy"));
        filesCopy.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesCopyFxml);
        });

        MenuItem filesMove = new MenuItem(Languages.message("FilesMove"));
        filesMove.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesMoveFxml);
        });

        MenuItem filesFind = new MenuItem(Languages.message("FilesFind"));
        filesFind.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesFindFxml);
        });

        MenuItem filesCompare = new MenuItem(Languages.message("FilesCompare"));
        filesCompare.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesCompareFxml);
        });

        MenuItem filesRedundancy = new MenuItem(Languages.message("FilesRedundancy"));
        filesRedundancy.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesRedundancyFxml);
        });

        MenuItem filesDelete = new MenuItem(Languages.message("FilesDelete"));
        filesDelete.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteFxml);
        });

        MenuItem DeleteEmptyDirectories = new MenuItem(Languages.message("DeleteEmptyDirectories"));
        DeleteEmptyDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteEmptyDirFxml);
        });

        MenuItem DeleteSysTemporaryPathFiles = new MenuItem(Languages.message("DeleteSysTemporaryPathFiles"));
        DeleteSysTemporaryPathFiles.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteSysTempFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(Languages.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteNestedDirFxml);
        });

        Menu fileDeleteMenu = new Menu(Languages.message("FilesDelete"));
        fileDeleteMenu.getItems().addAll(
                DeleteSysTemporaryPathFiles, DeleteEmptyDirectories, filesDelete, DeleteNestedDirectories
        );

        MenuItem filesArchiveCompress = new MenuItem(Languages.message("FilesArchiveCompress"));
        filesArchiveCompress.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesArchiveCompressFxml);
        });

        MenuItem filesCompress = new MenuItem(Languages.message("FilesCompressBatch"));
        filesCompress.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesCompressBatchFxml);
        });

        MenuItem filesDecompressUnarchive = new MenuItem(Languages.message("FileDecompressUnarchive"));
        filesDecompressUnarchive.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FileDecompressUnarchiveFxml);
        });

        MenuItem filesDecompressUnarchiveBatch = new MenuItem(Languages.message("FilesDecompressUnarchiveBatch"));
        filesDecompressUnarchiveBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDecompressUnarchiveBatchFxml);
        });

        Menu archiveCompressMenu = new Menu(Languages.message("FilesArchiveCompress"));
        archiveCompressMenu.getItems().addAll(
                filesDecompressUnarchive, filesDecompressUnarchiveBatch,
                filesArchiveCompress, filesCompress
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                fileCut, filesMerge, new SeparatorMenuItem(),
                filesFind, filesRedundancy, filesCompare, new SeparatorMenuItem(),
                filesRename, filesCopy, filesMove, new SeparatorMenuItem(),
                fileDeleteMenu, new SeparatorMenuItem(),
                archiveCompressMenu
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(fileBox, event);

        view.setImage(new Image("img/FileTools.png"));
        text.setText(Languages.message("FileToolsImageTips"));
        locateImage(fileBox, false);
    }

}
