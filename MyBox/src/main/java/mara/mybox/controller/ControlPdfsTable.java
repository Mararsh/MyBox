package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlPdfsTable extends BaseBatchTableController<PdfInformation> {

    @FXML
    protected TableColumn<PdfInformation, String> userPasswordColumn;
    @FXML
    protected TableColumn<PdfInformation, Integer> fromColumn, toColumn;
    @FXML
    protected TextField passwordInput, fromInput, toInput;
    @FXML
    protected Button setAllOrSelectedButton;
    @FXML
    protected FlowPane setPDFPane;
    @FXML
    protected HBox fromToBox;
    @FXML
    protected Label tableCommentsLabel;

    public ControlPdfsTable() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF);
    }

    @Override
    public void initTable() {
        try {
            super.initTable();

            FxmlControl.setTooltip(passwordInput, new Tooltip(message("UserPassword")));

            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        Integer.parseInt(newValue);
                        fromInput.setStyle(null);
                    } catch (Exception e) {
                        fromInput.setStyle(badStyle);
                    }
                }
            });

            toInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        if (newValue == null || newValue.trim().isEmpty()) {
                            toInput.setStyle(null);
                            return;
                        }
                        Integer.parseInt(newValue);
                        toInput.setStyle(null);
                    } catch (Exception e) {
                        toInput.setStyle(badStyle);
                    }
                }
            });
            FxmlControl.setTooltip(toInput, new Tooltip(message("ToPageComments")));

            setAllOrSelectedButton.disableProperty().bind(fromInput.styleProperty().isEqualTo(badStyle)
                    .or(toInput.styleProperty().isEqualTo(badStyle))
            );

            tableSubdirCheck.setSelected(AppVariables.getUserConfigBoolean("PDFTableSubDir", true));
            tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("PDFTableSubDir", tableSubdirCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("userPassword"));
            userPasswordColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            userPasswordColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PdfInformation, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PdfInformation, String> t) {
                    if (t == null) {
                        return;
                    }
                    PdfInformation row = t.getRowValue();
                    row.setUserPassword(t.getNewValue());
                }
            });
            userPasswordColumn.getStyleClass().add("editable-column");

            fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromPage"));
            fromColumn.setCellFactory((TableColumn<PdfInformation, Integer> param) -> {
                TableAutoCommitCell<PdfInformation, Integer> cell
                        = new TableAutoCommitCell<PdfInformation, Integer>(new IntegerStringConverter()) {
                    @Override
                    public void commitEdit(Integer val) {
                        if (val <= 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            fromColumn.setOnEditCommit((TableColumn.CellEditEvent<PdfInformation, Integer> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() > 0) {
                    PdfInformation row = t.getRowValue();
                    row.setFromPage(t.getNewValue());
                }
            });
            fromColumn.getStyleClass().add("editable-column");

            toColumn.setCellValueFactory(new PropertyValueFactory<>("toPage"));
            toColumn.setCellFactory(TableAutoCommitCell.forTableColumn(new IntegerStringConverter()));
            toColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PdfInformation, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PdfInformation, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    PdfInformation row = t.getRowValue();
                    row.setToPage(t.getNewValue());
                }
            });
            toColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected PdfInformation create(File file) {
        PdfInformation d = new PdfInformation(file);
        return d;
    }

    @FXML
    protected void setAction(ActionEvent event) {
        if (tableData.isEmpty()) {
            return;
        }
        String password;
        int fromPage, toPage;
        String p = passwordInput.getText();
        if (p == null) {
            password = null;
        } else {
            password = p.trim();
            if (password.isEmpty()) {
                password = null;
            }
        }

        try {
            int f = Integer.parseInt(fromInput.getText());
            if (f > 0) {
                fromPage = f;
            } else {
                fromPage = 1;
            }
        } catch (Exception e) {
            fromInput.setStyle(badStyle);
            return;
        }

        try {
            String t = toInput.getText();
            if (t == null || t.trim().isEmpty()) {
                toPage = -1;
            } else {
                int v = Integer.parseInt(t);
                if (v > 0) {
                    toPage = v;
                } else {
                    toPage = -1;
                }
            }
        } catch (Exception e) {
            toInput.setStyle(badStyle);
            return;
        }
        boolean userPassword = tableView.getColumns().contains(userPasswordColumn);
        List<PdfInformation> rows = tableView.getSelectionModel().getSelectedItems();
        if (rows == null || rows.isEmpty()) {
            rows = tableData;
        }
        for (PdfInformation info : rows) {
            if (userPassword) {
                info.setUserPassword(password);
            }
            info.setFromPage(fromPage);
            info.setToPage(toPage);
        }
        tableView.refresh();

    }

    @Override
    protected boolean isValidFile(File file) {
        return PdfTools.isPDF(file);
    }

    /*
        get/set
     */
    public TableColumn<PdfInformation, String> getUserPasswordColumn() {
        return userPasswordColumn;
    }

    public void setUserPasswordColumn(TableColumn<PdfInformation, String> userPasswordColumn) {
        this.userPasswordColumn = userPasswordColumn;
    }

    public Button getSetAllButton() {
        return setAllOrSelectedButton;
    }

    public void setSetAllButton(Button setAllOrSelectedButton) {
        this.setAllOrSelectedButton = setAllOrSelectedButton;
    }

    public FlowPane getSetPDFPane() {
        return setPDFPane;
    }

    public void setSetPDFPane(FlowPane setPDFPane) {
        this.setPDFPane = setPDFPane;
    }

    public HBox getFromToBox() {
        return fromToBox;
    }

    public void setFromToBox(HBox fromToBox) {
        this.fromToBox = fromToBox;
    }

    public Label getTableCommentsLabel() {
        return tableCommentsLabel;
    }

    public void setTableCommentsLabel(Label tableCommentsLabel) {
        this.tableCommentsLabel = tableCommentsLabel;
    }

    public TableColumn<PdfInformation, Integer> getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(TableColumn<PdfInformation, Integer> fromColumn) {
        this.fromColumn = fromColumn;
    }

    public TableColumn<PdfInformation, Integer> getToColumn() {
        return toColumn;
    }

    public void setToColumn(TableColumn<PdfInformation, Integer> toColumn) {
        this.toColumn = toColumn;
    }

    public TextField getPasswordInput() {
        return passwordInput;
    }

    public void setPasswordInput(TextField passwordInput) {
        this.passwordInput = passwordInput;
    }

    public TextField getFromInput() {
        return fromInput;
    }

    public void setFromInput(TextField fromInput) {
        this.fromInput = fromInput;
    }

    public TextField getToInput() {
        return toInput;
    }

    public void setToInput(TextField toInput) {
        this.toInput = toInput;
    }

}
