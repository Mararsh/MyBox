package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.converter.IntegerStringFromatConverter;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(passwordInput, new Tooltip(Languages.message("UserPassword")));
            NodeStyleTools.setTooltip(toInput, new Tooltip(Languages.message("ToPageComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("userPassword"));
            userPasswordColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            userPasswordColumn.setOnEditCommit((TableColumn.CellEditEvent<PdfInformation, String> t) -> {
                if (t == null) {
                    return;
                }
                PdfInformation row = t.getRowValue();
                if (row == null) {
                    return;
                }
                row.setUserPassword(t.getNewValue());
            });
            userPasswordColumn.getStyleClass().add("editable-column");

            fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromPage"));
            fromColumn.setCellFactory((TableColumn<PdfInformation, Integer> param) -> {
                TableAutoCommitCell<PdfInformation, Integer> cell
                        = new TableAutoCommitCell<PdfInformation, Integer>(new IntegerStringFromatConverter()) {

                    @Override
                    public boolean setCellValue(Integer value) {
                        try {
                            if (value == null || value <= 0 || !isEditingRow()) {
                                cancelEdit();
                                return false;
                            }
                            PdfInformation row = tableData.get(editingRow);
                            row.setFromPage(value);
                            return super.setCellValue(value);
                        } catch (Exception e) {
                            MyBoxLog.debug(e);
                            return false;
                        }
                    }
                };
                return cell;
            });
            fromColumn.getStyleClass().add("editable-column");

            toColumn.setCellValueFactory(new PropertyValueFactory<>("toPage"));
            toColumn.setCellFactory((TableColumn<PdfInformation, Integer> param) -> {
                TableAutoCommitCell<PdfInformation, Integer> cell
                        = new TableAutoCommitCell<PdfInformation, Integer>(new IntegerStringFromatConverter()) {

                    @Override
                    public boolean setCellValue(Integer value) {
                        try {
                            if (value == null || !isEditingRow()) {
                                cancelEdit();
                                return false;
                            }
                            PdfInformation row = tableData.get(editingRow);
                            row.setToPage(value);
                            return super.setCellValue(value);
                        } catch (Exception e) {
                            MyBoxLog.debug(e);
                            return false;
                        }

                    }
                };
                return cell;
            });
            toColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initMore() {
        try {
            super.initMore();

            fromInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        Integer.parseInt(newValue);
                        fromInput.setStyle(null);
                    } catch (Exception e) {
                        fromInput.setStyle(UserConfig.badStyle());
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
                        toInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            setAllOrSelectedButton.disableProperty().bind(fromInput.styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(toInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

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
            fromInput.setStyle(UserConfig.badStyle());
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
            toInput.setStyle(UserConfig.badStyle());
            return;
        }
        boolean userPassword = tableView.getColumns().contains(userPasswordColumn);
        List<PdfInformation> rows = selectedItems();
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

}
