/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import mara.mybox.objects.AppVaribles;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class FilesTableController extends BaseController {

    protected List<FileChooser.ExtensionFilter> fileExtensionFilter;

    @FXML
    private Pane filesTablePane;
    @FXML
    private Button addButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<?> filesTableView;

    @FXML
    private void addSourceFiles(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"))));
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
//            sourceFiles = fileChooser.showOpenMultipleDialog(getMyStage());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    private void clearSourceFiles(ActionEvent event) {
    }

}
