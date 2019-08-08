package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import mara.mybox.controller.base.TableController;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class PDFsTableController extends TableController<PdfInformation> {

    @FXML
    protected TableColumn<PdfInformation, String> userPasswordColumn, ownerPasswordColumn;
    @FXML
    protected TableColumn<PdfInformation, Integer> fromColumn, toColumn;
    @FXML
    protected TextField passwordInput, fromInput, toInput;
    @FXML
    protected Button setButton;
    @FXML
    protected HBox setBox, fromToBax;
    @FXML
    protected Label tableCommentsLabel, setLabel;

    public PDFsTableController() {
        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetPathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.PDF;
        AddPathType = VisitHistory.FileType.PDF;

        targetPathKey = "PdfTargetPath";
        sourcePathKey = "PdfSourcePath";
        sourceExtensionFilter = CommonValues.PdfExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initTable() {
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

        setButton.disableProperty().bind(fromInput.styleProperty().isEqualTo(badStyle)
                .or(toInput.styleProperty().isEqualTo(badStyle))
        );

    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("userPassword"));
            userPasswordColumn.setCellFactory(TextFieldTableCell.forTableColumn());
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

            ownerPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("ownerPassword"));
            ownerPasswordColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            ownerPasswordColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PdfInformation, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PdfInformation, String> t) {
                    if (t == null) {
                        return;
                    }
                    PdfInformation row = t.getRowValue();
                    row.setOwnerPassword(t.getNewValue());
                }
            });

            fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromPage"));
            fromColumn.setCellFactory(new Callback<TableColumn<PdfInformation, Integer>, TableCell<PdfInformation, Integer>>() {
                @Override
                public TableCell<PdfInformation, Integer> call(TableColumn<PdfInformation, Integer> param) {
                    TextFieldTableCell<PdfInformation, Integer> cell
                            = new TextFieldTableCell<PdfInformation, Integer>(new IntegerStringConverter()) {
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
                }
            });
            fromColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PdfInformation, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PdfInformation, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    if (t.getNewValue() > 0) {
                        PdfInformation row = t.getRowValue();
                        row.setFromPage(t.getNewValue());
                    }
                }
            });

            toColumn.setCellValueFactory(new PropertyValueFactory<>("toPage"));
            toColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
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

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected PdfInformation create(File file) {
        PdfInformation d = new PdfInformation(file);
        return d;
    }

    @FXML
    protected void setAction(ActionEvent event) {
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
        boolean ownerPassword = tableView.getColumns().contains(ownerPasswordColumn);
        for (PdfInformation info : tableData) {
            if (userPassword) {
                info.setUserPassword(password);
            }
            if (ownerPassword) {
                info.setOwnerPassword(password);
            }
            info.setFromPage(fromPage);
            info.setToPage(toPage);
        }
        tableView.refresh();

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

    public TableColumn<PdfInformation, String> getOwnerPasswordColumn() {
        return ownerPasswordColumn;
    }

    public void setOwnerPasswordColumn(TableColumn<PdfInformation, String> ownerPasswordColumn) {
        this.ownerPasswordColumn = ownerPasswordColumn;
    }

    public Button getSetButton() {
        return setButton;
    }

    public void setSetButton(Button setButton) {
        this.setButton = setButton;
    }

    public HBox getSetBox() {
        return setBox;
    }

    public void setSetBox(HBox setBox) {
        this.setBox = setBox;
    }

    public HBox getFromToBax() {
        return fromToBax;
    }

    public void setFromToBax(HBox fromToBax) {
        this.fromToBax = fromToBax;
    }

    public Label getTableCommentsLabel() {
        return tableCommentsLabel;
    }

    public void setTableCommentsLabel(Label tableCommentsLabel) {
        this.tableCommentsLabel = tableCommentsLabel;
    }

    public Label getSetLabel() {
        return setLabel;
    }

    public void setSetLabel(Label setLabel) {
        this.setLabel = setLabel;
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
