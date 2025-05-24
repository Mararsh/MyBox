package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_File extends MyBoxController_Image {

    @FXML
    public void popFileMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showFileMenu(event);
        }
    }

    @FXML
    protected void showFileMenu(Event event) {
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

        MenuItem DeleteJavaTemporaryPathFiles = new MenuItem(Languages.message("DeleteJavaIOTemporaryPathFiles"));
        DeleteJavaTemporaryPathFiles.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteJavaTempFxml);
        });

        MenuItem DeleteNestedDirectories = new MenuItem(Languages.message("DeleteNestedDirectories"));
        DeleteNestedDirectories.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FilesDeleteNestedDirFxml);
        });

        Menu fileDeleteMenu = new Menu(Languages.message("Delete"));
        fileDeleteMenu.getItems().addAll(
                DeleteJavaTemporaryPathFiles, DeleteEmptyDirectories, filesDelete, DeleteNestedDirectories
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

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                archiveCompressMenu, new SeparatorMenuItem(),
                fileCut, filesMerge, new SeparatorMenuItem(),
                filesFind, filesRedundancy, filesCompare, new SeparatorMenuItem(),
                filesRename, filesCopy, filesMove, new SeparatorMenuItem(),
                fileDeleteMenu));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(fileBox, items);

    }

}
