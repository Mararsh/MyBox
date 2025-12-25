package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class FileToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem filesArrangement = new MenuItem(Languages.message("FilesArrangement"));
        filesArrangement.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesArrangementFxml);
        });

        MenuItem dirSynchronize = new MenuItem(Languages.message("DirectorySynchronize"));
        dirSynchronize.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DirectorySynchronizeFxml);
        });

        MenuItem filesRename = new MenuItem(Languages.message("FilesRename"));
        filesRename.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesRenameFxml);
        });

        MenuItem fileCut = new MenuItem(Languages.message("FileCut"));
        fileCut.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FileCutFxml);
        });

        MenuItem filesMerge = new MenuItem(Languages.message("FilesMerge"));
        filesMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesMergeFxml);
        });

        MenuItem filesCopy = new MenuItem(Languages.message("FilesCopy"));
        filesCopy.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesCopyFxml);
        });

        MenuItem filesMove = new MenuItem(Languages.message("FilesMove"));
        filesMove.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesMoveFxml);
        });

        MenuItem filesFind = new MenuItem(Languages.message("FilesFind"));
        filesFind.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesFindFxml);
        });

        MenuItem filesCompare = new MenuItem(Languages.message("FilesCompare"));
        filesCompare.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesCompareFxml);
        });

        MenuItem filesRedundancy = new MenuItem(Languages.message("FilesRedundancy"));
        filesRedundancy.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesRedundancyFxml);
        });

        MenuItem filesDelete = new MenuItem(Languages.message("FilesDelete"));
        filesDelete.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesDeleteFxml);
        });

        MenuItem DeleteEmptyDirectories = new MenuItem(Languages.message("DeleteEmptyDirectories"));
        DeleteEmptyDirectories.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesDeleteEmptyDirFxml);
        });

        MenuItem DeleteJavaTemporaryPathFiles = new MenuItem(Languages.message("DeleteJavaIOTemporaryPathFiles"));
        DeleteJavaTemporaryPathFiles.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesDeleteJavaTempFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(Languages.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesDeleteNestedDirFxml);
        });

        Menu fileDeleteMenu = new Menu(Languages.message("Delete"));
        fileDeleteMenu.getItems().addAll(
                DeleteJavaTemporaryPathFiles, DeleteEmptyDirectories, filesDelete, DeleteNestedDirectories
        );

        MenuItem filesArchiveCompress = new MenuItem(Languages.message("FilesArchiveCompress"));
        filesArchiveCompress.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesArchiveCompressFxml);
        });

        MenuItem filesCompress = new MenuItem(Languages.message("FilesCompressBatch"));
        filesCompress.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesCompressBatchFxml);
        });

        MenuItem filesDecompressUnarchive = new MenuItem(Languages.message("FileDecompressUnarchive"));
        filesDecompressUnarchive.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FileDecompressUnarchiveFxml);
        });

        MenuItem filesDecompressUnarchiveBatch = new MenuItem(Languages.message("FilesDecompressUnarchiveBatch"));
        filesDecompressUnarchiveBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FilesDecompressUnarchiveBatchFxml);
        });

        Menu archiveCompressMenu = new Menu(Languages.message("FilesArchiveCompress"));
        archiveCompressMenu.getItems().addAll(
                filesDecompressUnarchive, filesDecompressUnarchiveBatch,
                filesArchiveCompress, filesCompress
        );

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                archiveCompressMenu, new SeparatorMenuItem(),
                fileCut, filesMerge, new SeparatorMenuItem(),
                filesFind, filesRedundancy, filesCompare, new SeparatorMenuItem(),
                filesRename, filesCopy, filesMove, new SeparatorMenuItem(),
                fileDeleteMenu));

        return items;

    }

}
