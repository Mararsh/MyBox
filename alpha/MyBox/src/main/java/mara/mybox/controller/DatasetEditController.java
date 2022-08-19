package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.Era;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableDataset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-07-18
 * @License Apache License Version 2.0
 */
public class DatasetEditController extends BaseController {

    protected Dataset loadedDataset;

    @FXML
    protected TextField idInput, datasetInput;
    @FXML
    protected Label textLabel;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<String> categorySelector;
    @FXML
    protected RadioButton datetimeRadio, dateRadio, yearRadio, monthRadio, timeRadio,
            timeMsRadio, datetimeZoneRadio, datatimeMsRadio, datatimeMsZoneRadio;
    @FXML
    protected CheckBox omitADCheck;
    @FXML
    protected ColorSet textColorSetController, backgroundColorSetController, chartColorSetController;

    public DatasetEditController() {
        baseTitle = Languages.message("Dataset");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> dataCategories = new ArrayList<>();
            for (String category : TableDataset.dataCategories()) {
                dataCategories.add(Languages.tableMessage(category));
            }
            categorySelector.getItems().addAll(dataCategories);
            categorySelector.getSelectionModel().select(0);

            initColors();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(idInput, Languages.message("AssignedByMyBox"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initColors() {
        textColorSetController.init(this, baseName + "TextColor", Color.BLACK);
        backgroundColorSetController.init(this, baseName + "BackgroundColor", Color.WHITE);
        chartColorSetController.init(this, baseName + "ChartColor", Color.web("#961c1c"));

        textLabel.setStyle("-fx-text-fill: " + textColorSetController.rgb()
                + "; -fx-background-color: " + backgroundColorSetController.rgb());

        textColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
            @Override
            public void changed(ObservableValue<? extends Paint> observable,
                    Paint oldValue, Paint newValue) {
                textLabel.setStyle("-fx-text-fill: " + textColorSetController.rgb()
                        + "; -fx-background-color: " + backgroundColorSetController.rgb());
            }
        });

        backgroundColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
            @Override
            public void changed(ObservableValue<? extends Paint> observable,
                    Paint oldValue, Paint newValue) {
                textLabel.setStyle("-fx-text-fill: " + textColorSetController.rgb()
                        + "; -fx-background-color: " + backgroundColorSetController.rgb());
            }
        });

    }

    public void initEditor(BaseController parentController, Dataset dataset) {
        try {
            this.parentController = parentController;
            loadData(dataset);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData(Dataset dataset) {
        loadedDataset = dataset;
        loadData();
    }

    public void loadData() {
        try {
            if (loadedDataset == null) {
                clearData();
                return;
            }
            if (loadedDataset.getDsid() > 0) {
                idInput.setText(loadedDataset.getDsid() + "");
            } else {
                idInput.clear();
            }

            categorySelector.getSelectionModel().select(Languages.tableMessage(loadedDataset.getDataCategory()));
            datasetInput.setText(loadedDataset.getDataSet());
            switch (loadedDataset.getTimeFormat()) {
                case Date:
                    dateRadio.setSelected(true);
                    break;
                case Year:
                    yearRadio.setSelected(true);
                    break;
                case Month:
                    monthRadio.setSelected(true);
                    break;
                case Time:
                    timeRadio.setSelected(true);
                    break;
                case TimeMs:
                    timeMsRadio.setSelected(true);
                    break;
                case DatetimeMs:
                    datatimeMsRadio.setSelected(true);
                    break;
                case DatetimeZone:
                    datetimeZoneRadio.setSelected(true);
                    break;
                case DatetimeMsZone:
                    datatimeMsZoneRadio.setSelected(true);
                    break;
                default:
                    datetimeRadio.setSelected(true);
                    break;
            }
            omitADCheck.setSelected(loadedDataset.isOmitAD());

            if (loadedDataset.getTextColor() != null) {
                textColorSetController.setColor(loadedDataset.getTextColor());
            }
            if (loadedDataset.getBgColor() != null) {
                backgroundColorSetController.setColor(loadedDataset.getBgColor());
            }

            if (loadedDataset.getChartColor() != null) {
                chartColorSetController.setColor(loadedDataset.getChartColor());
            }

            if (loadedDataset.getImage() != null) {
                sourceFileInput.setText(loadedDataset.getImage().getAbsolutePath());
            } else {
                sourceFileInput.clear();
            }
            if (loadedDataset.getComments() != null) {
                commentsArea.setText(loadedDataset.getComments());
            } else {
                commentsArea.clear();
            }

            datasetInput.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void clearData() {
        try {
            idInput.clear();
            categorySelector.getSelectionModel().select(0);
            datasetInput.clear();
            datetimeRadio.setSelected(true);
            sourceFileInput.clear();
            commentsArea.clear();
            initColors();

            datasetInput.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        loadData();
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            String name = datasetInput.getText().trim();
            if (name.isBlank()) {
                popError(Languages.message("MissDataset"));
                return;
            }
            Dataset dataset = new Dataset().setDataSet(name);
            if (idInput.getText() == null || idInput.getText().isBlank()) {
                dataset.setDsid(-1);
            } else {
                dataset.setDsid(Long.valueOf(idInput.getText()));
            }
            String category = categorySelector.getSelectionModel().getSelectedItem();
            for (String c : TableDataset.dataCategories()) {
                if (Languages.tableMessage(c).equals(category)) {
                    dataset.setDataCategory(c);
                    break;
                }
            }
            if (dateRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.Date);
            } else if (yearRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.Year);
            } else if (monthRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.Month);
            } else if (timeRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.Time);
            } else if (timeMsRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.TimeMs);
            } else if (datetimeZoneRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.DatetimeZone);
            } else if (datatimeMsRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.DatetimeMs);
            } else if (datatimeMsZoneRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.DatetimeMsZone);
            } else {
                dataset.setTimeFormat(Era.Format.Datetime);
            }
            dataset.setOmitAD(omitADCheck.isSelected());

            dataset.setTextColor(Color.web(textColorSetController.rgba()));
            dataset.setBgColor(Color.web(backgroundColorSetController.rgba()));
            dataset.setChartColor(Color.web(chartColorSetController.rgba()));

            if (sourceFile != null && sourceFile.exists()) {
                dataset.setImage(sourceFile);
            }
            String comments = commentsArea.getText().trim();
            if (!comments.isBlank()) {
                dataset.setComments(comments);
            }

            if (new TableDataset().writeData(dataset) == null) {
                popFailed();
                return;
            }

            if (dataset.getDsid() > 0) {
                popInformation(Languages.message("UpdateSuccessfully"));
                idInput.setText(dataset.getDsid() + "");
            } else {
                popInformation(Languages.message("InsertSuccessfully"));
            }
            closeStage();

            if (parentController != null) {
                ((BaseDataManageController) parentController).refreshAction();
                parentController.getMyStage().requestFocus();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void copyAction() {
        idInput.clear();
        datasetInput.setText(datasetInput.getText() + " - " + Languages.message("Copy"));
    }

}
