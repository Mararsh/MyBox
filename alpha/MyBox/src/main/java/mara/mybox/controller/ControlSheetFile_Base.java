package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_Base extends ControlSheet {

    protected BaseDataFileController fileController;
    protected SimpleBooleanProperty fileLoadedNotify;
    protected boolean userSavedDataDefinition;

    @FXML
    protected Tab defTab, reportTab;
    @FXML
    protected VBox defBox;
    @FXML
    protected Button trimColumnsButton;
    @FXML
    protected HBox defBottunsBox;
    @FXML
    protected ControlWebView reportViewController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, totalLabel;

}
