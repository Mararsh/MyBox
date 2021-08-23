package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.table.ColumnDefinition;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-22
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetDisplay_Base extends BaseController {

    protected BaseSheetController sheetController;
    protected List<ColumnDefinition> columns;
    protected ColumnDefinition.ColumnType defaultColumnType;
    protected String dataName, defaultColValue, colPrefix, inputStyle;
    protected boolean defaultColNotNull, dataInvalid;
    protected String[][] sheet;
    protected int colsNumber, rowsNumber;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab defTab, htmlTab, textsTab, reportTab, calculationTab;
    @FXML
    protected CheckBox htmlTitleCheck, htmlColumnCheck, htmlRowCheck;
    @FXML
    protected HBox defBottunsBox;
    @FXML
    protected VBox defBox, calColumnsBox;
    @FXML
    protected Button exampleCalculationColumnsButton, exampleDisplayColumnsButton, calculatorButton;
    @FXML
    protected TextArea calculationColumnsArea, displayColumnsArea;
    @FXML
    protected ComboBox<String> rowFromSelector, rowToSelector;
    @FXML
    protected ControlWebView htmlViewController, reportViewController;
    @FXML
    protected ToggleGroup calGroup;
    @FXML
    protected RadioButton sumRadio, addRadio, subRadio, multiplyRadio, mergeRadio, ascendingRadio, descendingRadio, copyRadio;
    @FXML
    protected Label calColumnsLabel;

    public ControlSheetDisplay_Base() {
        baseTitle = message("Data");
        dataName = "sheet";
        colPrefix = "Field";
        defaultColumnType = ColumnDefinition.ColumnType.String;
        defaultColValue = "";
        defaultColNotNull = false;
    }

}
