/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class ImagesBlendController implements Initializable {

    @FXML
    private BorderPane thisPane;
    @FXML
    private Label bottomLabel;
    @FXML
    private VBox mainPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Button addButton;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<?> sourceTable;
    @FXML
    private TableColumn<?, ?> imageColumn;
    @FXML
    private TableColumn<?, ?> sizeColumn;
    @FXML
    private TableColumn<?, ?> fileColumn;
    @FXML
    private RadioButton arrayColumnRadio;
    @FXML
    private ToggleGroup arrayGroup;
    @FXML
    private RadioButton arrayRowRadio;
    @FXML
    private RadioButton arrayColumnsRadio;
    @FXML
    private ComboBox<?> columnsBox;
    @FXML
    private ColorPicker bgPicker;
    @FXML
    private ComboBox<?> intervalBox;
    @FXML
    private RadioButton sizeKeepRadio;
    @FXML
    private ToggleGroup sizeGroup;
    @FXML
    private RadioButton sizeBiggerRadio;
    @FXML
    private RadioButton sizeSmallerRadio;
    @FXML
    private RadioButton sizeResizeRadio;
    @FXML
    private TextField resizeWidthInput;
    @FXML
    private TextField resizeHeightInput;
    @FXML
    private RadioButton sizeTotalRadio;
    @FXML
    private TextField totalWidthInput;
    @FXML
    private TextField totalHeightInput;
    @FXML
    private TextField targetPathInput;
    @FXML
    private Button openTargetButton;
    @FXML
    private TextField targetPrefixInput;
    @FXML
    private ComboBox<?> targetTypeBox;
    @FXML
    private Button combineButton;
    @FXML
    private VBox imageBox;
    @FXML
    private ScrollPane targetScroll;
    @FXML
    private ImageView targetView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
