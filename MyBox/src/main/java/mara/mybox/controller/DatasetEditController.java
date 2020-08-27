package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.data.Dataset;
import mara.mybox.data.Era;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableDataset;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.tableMessage;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-07-18
 * @License Apache License Version 2.0
 */
public class DatasetEditController extends BaseController {

    protected Dataset loadedDataset;
    protected Color textColor, bgColor, chartColor;

    @FXML
    protected TextField idInput, datasetInput;
    @FXML
    protected Rectangle colorRect;
    @FXML
    protected Label textLabel;
    @FXML
    protected Button paletteTextButton, paletteBgButton, paletteChartButton;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<String> categorySelector;
    @FXML
    protected RadioButton datetimeRadio, dateRadio, yearRadio, monthRadio, timeRadio, msRadio;
    @FXML
    protected CheckBox omitADCheck;

    public DatasetEditController() {
        baseTitle = message("Dataset");

        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourcePathKey = "ImageFilePath";
        SaveAsOptionsKey = "ImageSaveAsKey";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            FxmlControl.setTooltip(idInput, message("AssignedByMyBox"));

            List<String> dataCategories = new ArrayList<>();
            for (String category : TableDataset.dataCategories()) {
                dataCategories.add(tableMessage(category));
            }
            categorySelector.getItems().addAll(dataCategories);
            categorySelector.getSelectionModel().select(0);

            initColors();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initColors() {
        textColor = Color.BLACK;
        bgColor = Color.WHITE;
        textLabel.setStyle("-fx-text-fill: " + FxmlColor.rgb2Hex(textColor)
                + "; -fx-background-color: " + FxmlColor.rgb2Hex(bgColor));

        chartColor = Color.web("#961c1c");
        colorRect.setFill(chartColor);
        FxmlControl.setTooltip(colorRect, new Tooltip(FxmlColor.colorNameDisplay(chartColor)));
    }

    public void setValues(BaseController parentController, Dataset dataset) {
        try {
            this.parentController = parentController;
            loadData(dataset);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        try {
            if (paletteTextButton.equals(control)) {
                textColor = color;
                textLabel.setStyle("-fx-text-fill: " + FxmlColor.rgb2Hex(textColor)
                        + "; -fx-background-color: " + FxmlColor.rgb2Hex(bgColor));

            } else if (paletteBgButton.equals(control)) {
                bgColor = color;
                textLabel.setStyle("-fx-text-fill: " + FxmlColor.rgb2Hex(textColor)
                        + "; -fx-background-color: " + FxmlColor.rgb2Hex(bgColor));

            } else if (paletteChartButton.equals(control)) {
                chartColor = color;
                colorRect.setFill(color);
                FxmlControl.setTooltip(colorRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            popError(e.toString());
            return false;
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
            if (loadedDataset.getId() > 0) {
                idInput.setText(loadedDataset.getId() + "");
            } else {
                idInput.clear();
            }

            categorySelector.getSelectionModel().select(tableMessage(loadedDataset.getDataCategory()));
            datasetInput.setText(loadedDataset.getDataSet());
            switch (loadedDataset.getTimeFormat()) {
                case Date:
                    dateRadio.fire();
                    break;
                case Year:
                    yearRadio.fire();
                    break;
                case Month:
                    monthRadio.fire();
                    break;
                case Time:
                    timeRadio.fire();
                    break;
                case TimeWithMS:
                    msRadio.fire();
                    break;
                default:
                    datetimeRadio.fire();
                    break;
            }
            omitADCheck.setSelected(loadedDataset.isOmitAD());

            if (loadedDataset.getTextColor() != null) {
                textColor = loadedDataset.getTextColor();
            }
            if (loadedDataset.getBgColor() != null) {
                bgColor = loadedDataset.getBgColor();
            }
            textLabel.setStyle("-fx-text-fill: " + FxmlColor.rgb2Hex(textColor)
                    + "; -fx-background-color: " + FxmlColor.rgb2Hex(bgColor));

            if (loadedDataset.getChartColor() != null) {
                chartColor = loadedDataset.getChartColor();
                colorRect.setFill(chartColor);
                FxmlControl.setTooltip(colorRect, new Tooltip(FxmlColor.colorNameDisplay(chartColor)));
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

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clearData() {
        try {
            idInput.clear();
            categorySelector.getSelectionModel().select(0);
            datasetInput.clear();
            datetimeRadio.fire();
            sourceFileInput.clear();
            commentsArea.clear();
            initColors();

        } catch (Exception e) {
            logger.error(e.toString());
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
                popError(message("MissDataset"));
                return;
            }
            Dataset dataset = new Dataset().setDataSet(name);
            if (idInput.getText() == null || idInput.getText().isBlank()) {
                dataset.setId(-1);
            } else {
                dataset.setId(Long.valueOf(idInput.getText()));
            }
            String category = categorySelector.getSelectionModel().getSelectedItem();
            for (String c : TableDataset.dataCategories()) {
                if (tableMessage(c).equals(category)) {
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
            } else if (msRadio.isSelected()) {
                dataset.setTimeFormat(Era.Format.TimeWithMS);
            } else {
                dataset.setTimeFormat(Era.Format.Datetime);
            }
            dataset.setOmitAD(omitADCheck.isSelected());

            dataset.setTextColor(textColor);
            dataset.setBgColor(bgColor);
            dataset.setChartColor(chartColor);

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
            if (parentController != null) {
                ((DataAnalysisController) parentController).refreshAction();
                parentController.getMyStage().toFront();
            }
            if (dataset.getId() > 0) {
                popUpdateSuccessful();
                idInput.setText(dataset.getId() + "");
            } else {
                popInsertSuccessful();
            }
            if (saveCloseCheck.isSelected()) {
                closeStage();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void copyAction() {
        idInput.clear();
        datasetInput.setText(datasetInput.getText() + " - " + message("Copy"));
    }

}
