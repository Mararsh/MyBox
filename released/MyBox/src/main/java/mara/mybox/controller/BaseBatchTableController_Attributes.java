package mara.mybox.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.value.FileFilters;

/**
 * @param <P> T must be subClass of FileInformation
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchTableController_Attributes<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected long totalFilesNumber, totalFilesSize, fileSelectorSize, fileSelectorTime, currentIndex;
    protected FileSelectorType fileSelectorType;

    @FXML
    protected Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            deleteFilesButton, clearFilesButton, upFilesButton, downFilesButton, viewFileButton, editFileButton,
            unselectAllFilesButton, selectAllFilesButton, listButton, exampleRegexButton;
    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, String> handledColumn, fileColumn, typeColumn;
    @FXML
    protected TableColumn<P, Long> tableIndexColumn, numberColumn, sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected CheckBox tableSubdirCheck, tableCreateDirCheck, tableExpandDirCheck, countDirCheck;
    @FXML
    protected ComboBox<String> nameFiltersSelector;
    @FXML
    protected TextField tableFiltersInput;
    @FXML
    protected Label tableLabel;

    public BaseBatchTableController_Attributes() {
        TipsLabelKey = "TableTips";
        sourceExtensionFilter = FileFilters.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    public ObservableList<P> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<P> tableData) {
        this.tableData = tableData;
    }

    public long getTotalFilesNumber() {
        return totalFilesNumber;
    }

    public void setTotalFilesNumber(long totalFilesNumber) {
        this.totalFilesNumber = totalFilesNumber;
    }

    public long getTotalFilesSize() {
        return totalFilesSize;
    }

    public void setTotalFilesSize(long totalFilesSize) {
        this.totalFilesSize = totalFilesSize;
    }

    public Button getAddFilesButton() {
        return addFilesButton;
    }

    public void setAddFilesButton(Button addFilesButton) {
        this.addFilesButton = addFilesButton;
    }

    public Button getInsertFilesButton() {
        return insertFilesButton;
    }

    public void setInsertFilesButton(Button insertFilesButton) {
        this.insertFilesButton = insertFilesButton;
    }

    public Button getAddDirectoryButton() {
        return addDirectoryButton;
    }

    public void setAddDirectoryButton(Button addDirectoryButton) {
        this.addDirectoryButton = addDirectoryButton;
    }

    public Button getInsertDirectoryButton() {
        return insertDirectoryButton;
    }

    public void setInsertDirectoryButton(Button insertDirectoryButton) {
        this.insertDirectoryButton = insertDirectoryButton;
    }

    public Button getDeleteFilesButton() {
        return deleteFilesButton;
    }

    public void setDeleteFilesButton(Button deleteFilesButton) {
        this.deleteFilesButton = deleteFilesButton;
    }

    public Button getClearFilesButton() {
        return clearFilesButton;
    }

    public void setClearFilesButton(Button clearFilesButton) {
        this.clearFilesButton = clearFilesButton;
    }

    public Button getUpFilesButton() {
        return upFilesButton;
    }

    public void setUpFilesButton(Button upFilesButton) {
        this.upFilesButton = upFilesButton;
    }

    public Button getDownFilesButton() {
        return downFilesButton;
    }

    public void setDownFilesButton(Button downFilesButton) {
        this.downFilesButton = downFilesButton;
    }

    public Button getViewFileButton() {
        return viewFileButton;
    }

    public void setViewFileButton(Button viewFileButton) {
        this.viewFileButton = viewFileButton;
    }

    public Button getUnselectAllFilesButton() {
        return unselectAllFilesButton;
    }

    public void setUnselectAllFilesButton(Button unselectAllFilesButton) {
        this.unselectAllFilesButton = unselectAllFilesButton;
    }

    public Button getSelectAllFilesButton() {
        return selectAllFilesButton;
    }

    public void setSelectAllFilesButton(Button selectAllFilesButton) {
        this.selectAllFilesButton = selectAllFilesButton;
    }

    public TableView<P> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<P> tableView) {
        this.tableView = tableView;
    }

    public TableColumn<P, String> getHandledColumn() {
        return handledColumn;
    }

    public void setHandledColumn(TableColumn<P, String> handledColumn) {
        this.handledColumn = handledColumn;
    }

    public TableColumn<P, String> getFileColumn() {
        return fileColumn;
    }

    public void setFileColumn(TableColumn<P, String> fileColumn) {
        this.fileColumn = fileColumn;
    }

    public TableColumn<P, Long> getNumberColumn() {
        return numberColumn;
    }

    public void setNumberColumn(TableColumn<P, Long> numberColumn) {
        this.numberColumn = numberColumn;
    }

    public TableColumn<P, Long> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<P, Long> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public TableColumn<P, Long> getModifyTimeColumn() {
        return modifyTimeColumn;
    }

    public void setModifyTimeColumn(TableColumn<P, Long> modifyTimeColumn) {
        this.modifyTimeColumn = modifyTimeColumn;
    }

    public TableColumn<P, Long> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<P, Long> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public TableColumn<P, String> getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(TableColumn<P, String> typeColumn) {
        this.typeColumn = typeColumn;
    }

    public CheckBox getTableSubdirCheck() {
        return tableSubdirCheck;
    }

    public void setTableSubdirCheck(CheckBox tableSubdirCheck) {
        this.tableSubdirCheck = tableSubdirCheck;
    }

    public ComboBox<String> getNameFiltersSelector() {
        return nameFiltersSelector;
    }

    public void setNameFiltersSelector(ComboBox<String> nameFiltersSelector) {
        this.nameFiltersSelector = nameFiltersSelector;
    }

    public CheckBox getTableExpandDirCheck() {
        return tableExpandDirCheck;
    }

    public void setTableExpandDirCheck(CheckBox tableExpandDirCheck) {
        this.tableExpandDirCheck = tableExpandDirCheck;
    }

    public TextField getTableFiltersInput() {
        return tableFiltersInput;
    }

    public void setTableFiltersInput(TextField tableFiltersInput) {
        this.tableFiltersInput = tableFiltersInput;
    }

    public Label getTableLabel() {
        return tableLabel;
    }

    public void setTableLabel(Label tableLabel) {
        this.tableLabel = tableLabel;
    }

    public FileSelectorType getFileSelectorType() {
        return fileSelectorType;
    }

    public void setFileSelectorType(FileSelectorType fileSelectorType) {
        this.fileSelectorType = fileSelectorType;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
    }

}
