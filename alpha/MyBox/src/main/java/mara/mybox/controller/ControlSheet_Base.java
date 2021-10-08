package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Base extends BaseController {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected DataDefinition dataDefinition;
    protected DataDefinition.DataType dataType;
    protected List<ColumnDefinition> columns;
    protected ColumnDefinition.ColumnType defaultColumnType;
    protected String dataName, defaultColValue, colPrefix, inputStyle;
    protected boolean defaultColNotNull;
    protected String[][] pageData;
    protected int colsNumber, rowsNumber;

    protected TextField[][] sheetInputs;
    protected CheckBox[] colsCheck, rowsCheck;
    protected SimpleBooleanProperty sheetChangedNotify, dataChangedNotify;
    protected int currentRow, currentCol; // 0-based
    protected boolean isMatrix2;
    protected Label noDataLabel;
    protected String sourceDelimiterName, editDelimiterName, displayDelimiterName;

    protected long totalSize, currentPageStart, currentPageEnd;   // // 1-based, excluded end
    protected int pagesNumber, widthChange, currentPage, pageSize;// 1-based

    protected ControlFileBackup backupController;
    protected boolean sourceWithNames, totalRead;
    protected List<ColumnDefinition> savedColumns;

    protected int maxRandom;
    protected short scale;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab sheetTab, editTab, htmlTab, textsDisplayTab, optionsTab, defTab;
    @FXML
    protected VBox sheetBox, defBox;
    @FXML
    protected Button analyseSheetButton, synchronizeEditButton,
            rowsAddButton, rowsDeleteButton, columnsAddButton, columnsDeleteButton, widthSheetButton,
            calculateSheetButton, sortSheetButton, deleteSheetButton, equalSheetButton;
    @FXML
    protected CheckBox htmlTitleCheck, htmlColumnCheck, htmlRowCheck, textTitleCheck, textColumnCheck, textRowCheck,
            htmlAllCheck, textAllCheck, overPopMenuCheck, rightClickPopMenuCheck;
    @FXML
    protected TextArea textsDisplayArea;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector, pageSizeSelector, pageSelector;
    @FXML
    protected ControlWebView htmlViewController;
    @FXML
    protected ControlTextDelimiter displayDelimiterController;
    @FXML
    protected RadioButton sumRadio, addRadio, subRadio, multiplyRadio, mergeRadio, ascendingRadio, descendingRadio, copyRadio;
    @FXML
    protected Button trimColumnsButton;
    @FXML
    protected HBox defBottunsBox, paginationBox;
    @FXML
    protected Label pageLabel, totalLabel;

    public ControlSheet_Base() {
        baseTitle = message("Data");
        dataType = DataDefinition.DataType.DataFile;
        dataName = "sheet";
        colPrefix = "Field";
        defaultColumnType = ColumnDefinition.ColumnType.String;
        defaultColValue = "";
        defaultColNotNull = false;
    }

}
