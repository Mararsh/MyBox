package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.db.TableFloatMatrix;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ConvolutionKernel;
import mara.mybox.objects.ConvolutionKernel.Convolution_Type;
import mara.mybox.tools.DateTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-06
 * @Description
 * @License Apache License Version 2.0
 */
public class ConvolutionKernelManagerController extends BaseController {

    protected ObservableList<ConvolutionKernel> tableData = FXCollections.observableArrayList();
    private int width, height, type, edge_Op;
    private boolean isSettingValues, matrixValid;
    private GridPane matrixPane;
    private TextField[][] matrixInputs;
    private float[][] matrixValues;
    private String name, description;
    private ConvolutionKernel kernel;

    @FXML
    private VBox mainPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Button editButton, deleteButton, saveButton, copyButton, gaussButton;
    @FXML
    private TableView<ConvolutionKernel> tableView;
    @FXML
    private TableColumn<ConvolutionKernel, String> nameColumn, modifyColumn, createColumn, desColumn;
    @FXML
    private TableColumn<ConvolutionKernel, Integer> widthColumn, heightColumn;
    @FXML
    private ToggleGroup typeGroup, edgesGroup;
    @FXML
    private TextField nameInput, desInput;
    @FXML
    private ComboBox<String> widthBox, heightBox;
    @FXML
    private HBox actionBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private CheckBox grayCheck;
    @FXML
    private RadioButton zeroRadio, keepRadio;

    @Override
    protected void initializeNext() {
        try {
            initList();
            initEditFields();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initList() {
        try {
            nameColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, String>("name"));
            widthColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, Integer>("width"));
            heightColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, Integer>("height"));
            modifyColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, String>("modifyTime"));
            createColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, String>("createTime"));
            desColumn.setCellValueFactory(new PropertyValueFactory<ConvolutionKernel, String>("description"));

            tableData.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    checkTableData();
                }
            });
            loadList();

            tableView.setItems(tableData);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        editAction();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void loadList() {
        List<ConvolutionKernel> records = TableConvolutionKernel.read();
        tableData.clear();
        tableData.addAll(records);

        if (parentController != null && parentFxml != null) {
            if (parentFxml.contains("ImageManufactureConvolution")) {
                ImageManufactureConvolutionController p = (ImageManufactureConvolutionController) parentController;
                p.loadList(records);
            } else if (parentFxml.contains("ImageManufactureBatchConvolution")) {
                ImageManufactureBatchConvolutionController p = (ImageManufactureBatchConvolutionController) parentController;
                p.loadList(records);
            }
        }
    }

    private void checkTableData() {
        if (tableData.size() > 0) {

        } else {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    private void checkTableSelected() {
        ObservableList<ConvolutionKernel> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected != null && selected.size() > 0) {
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            copyButton.setDisable(false);
        } else {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            copyButton.setDisable(true);
        }
    }

    protected void initEditFields() {
        try {
            kernel = new ConvolutionKernel();

            nameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    name = newValue;
                    if (name != null) {
                        name = name.trim();
                    }
                    checkKernel();
                }
            });
            desInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    description = newValue;
                    if (description != null) {
                        description = description.trim();
                    }
                }
            });

            List<String> sizeList = Arrays.asList(
                    "3", "5", "7", "9", "11", "13", "15", "17", "19");
            widthBox.getItems().addAll(sizeList);
            widthBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSize();
                }
            });

            heightBox.getItems().addAll(sizeList);
            heightBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSize();
                }
            });

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

            edgesGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkEdges();
                }
            });
            checkEdges();

            actionBox.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkType() {
        try {
            type = ConvolutionKernel.Convolution_Type.NONE;
            grayCheck.setDisable(true);
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            if (getMessage("Blur").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.BLUR;

            } else if (getMessage("Sharpen").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.SHARPNEN;

            } else if (getMessage("Emboss").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.EMBOSS;
                grayCheck.setDisable(false);

            } else if (getMessage("EdgeDetection").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.EDGE_DETECTION;

            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkEdges() {
        try {
            if (isSettingValues) {
                return;
            }
            edge_Op = ConvolutionKernel.Edge_Op.FILL_ZERO;
            RadioButton selected = (RadioButton) edgesGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            if (getMessage("KeepValues").equals(selected.getText())) {
                edge_Op = ConvolutionKernel.Edge_Op.COPY;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSize() {
        try {
            width = Integer.valueOf((String) widthBox.getSelectionModel().getSelectedItem());
            if (width > 2 && width % 2 != 0) {
                widthBox.getEditor().setStyle(null);
            } else {
                width = 0;
                widthBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            width = 0;
            widthBox.getEditor().setStyle(badStyle);
        }
        try {
            height = Integer.valueOf((String) heightBox.getSelectionModel().getSelectedItem());
            if (height > 2 && height % 2 != 0) {
                heightBox.getEditor().setStyle(null);
            } else {
                heightBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            height = 0;
            heightBox.getEditor().setStyle(badStyle);
        }
        if (isSettingValues) {
            return;
        }
        if (width == height && width > 2) {
            gaussButton.setDisable(false);
        } else {
            gaussButton.setDisable(true);
        }

        initMatrix();

    }

    private void initMatrix() {
        if (isSettingValues) {
            return;
        }
        if (width < 3 || width % 2 == 0
                || height < 3 || height % 2 == 0) {
            matrixPane = null;
            matrixInputs = null;
            matrixValues = null;
            matrixValid = false;
            checkKernel();
            return;
        }
        if (matrixValues == null) {
            matrixValues = new float[height][width];
        } else if (height != matrixValues.length || width != matrixValues[0].length) {
            float[][] old = matrixValues;
            matrixValues = new float[height][width];
            for (int j = 0; j < Math.min(height, old.length); j++) {
                for (int i = 0; i < Math.min(width, old[0].length); i++) {
                    matrixValues[j][i] = old[j][i];
                }
            }
            old = null;
        }

        double colWidth = Math.max(60, scrollPane.getWidth() / width - 6);

        matrixPane = null;
        matrixPane = new GridPane();
        matrixInputs = null;
        matrixInputs = new TextField[height][width];
        matrixPane.setPadding(new Insets(5.0));
        matrixPane.setHgap(5);
        matrixPane.setVgap(5);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                ColumnConstraints column = new ColumnConstraints(colWidth + 1);
                column.setHgrow(Priority.ALWAYS);
                matrixPane.getColumnConstraints().add(column);
                TextField valueInput = new TextField();
                valueInput.setPrefWidth(colWidth);
                valueInput.setText(matrixValues[j][i] + "");
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkMatrix();
                    }
                });
                GridPane.setHalignment(valueInput, HPos.RIGHT);
                matrixPane.add(valueInput, i, j);
                matrixInputs[j][i] = valueInput;
            }
        }
        scrollPane.setContent(matrixPane);

        checkMatrix();

    }

    private void checkMatrix() {
        if (isSettingValues) {
            return;
        }
        if (matrixInputs == null || matrixValues == null) {
            matrixValid = false;
            return;
        }
        matrixValid = true;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                TextField valueInput = matrixInputs[j][i];
                try {
                    matrixValues[j][i] = Float.valueOf(valueInput.getText());
                    valueInput.setStyle(null);
                } catch (Exception e) {
                    matrixValid = false;
                    valueInput.setStyle(badStyle);
                }
            }
        }
        checkKernel();
    }

    private void checkKernel() {
        actionBox.setDisable(!matrixValid || name == null || name.isEmpty());
    }

    @FXML
    private void createAction(ActionEvent event) {
        isSettingValues = true;
        kernel = new ConvolutionKernel();
        nameInput.setText("");
        desInput.setText("");
        widthBox.getSelectionModel().select("3");
        heightBox.getSelectionModel().select("3");
        FxmlTools.setRadioSelected(typeGroup, getMessage("None"));
        matrixValues = null;
        matrixValues = new float[height][width];
        nameInput.setDisable(false);
        isSettingValues = false;
        initMatrix();
    }

    @FXML
    private void editAction() {
        final List<ConvolutionKernel> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        kernel = selected.get(0);
        nameInput.setText(kernel.getName());
        desInput.setText(kernel.getDescription());
        widthBox.getSelectionModel().select(kernel.getWidth() + "");
        heightBox.getSelectionModel().select(kernel.getHeight() + "");
        type = kernel.getType();
        if (type == Convolution_Type.BLUR) {
            FxmlTools.setRadioSelected(typeGroup, getMessage("Blur"));
        } else if (type == Convolution_Type.SHARPNEN) {
            FxmlTools.setRadioSelected(typeGroup, getMessage("Sharpen"));
        } else if (type == Convolution_Type.EMBOSS) {
            FxmlTools.setRadioSelected(typeGroup, getMessage("Emboss"));
            grayCheck.setDisable(false);
        } else if (type == Convolution_Type.EDGE_DETECTION) {
            FxmlTools.setRadioSelected(typeGroup, getMessage("EdgeDetection"));
        } else {
            FxmlTools.setRadioSelected(typeGroup, getMessage("None"));
        }
        if (kernel.getEdge() == ConvolutionKernel.Edge_Op.COPY) {
            keepRadio.fire();
        } else {
            zeroRadio.fire();
        }
        grayCheck.setSelected(kernel.getGray() > 0);
        nameInput.setDisable(true);
        matrixValues = null;
        matrixValues = TableFloatMatrix.read(kernel.getName(), kernel.getWidth(), kernel.getHeight());
        isSettingValues = false;
        initMatrix();
    }

    @FXML
    private void copyAction() {
        editAction();
        nameInput.setDisable(false);
        nameInput.setText(kernel.getName() + " mmm");

    }

    @FXML
    private void deleteAction(ActionEvent event) {
        final List<ConvolutionKernel> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVaribles.getMessage("SureDelete"));
        ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonCancel) {
            return;
        }
        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<String> names = new ArrayList<>();
                    for (ConvolutionKernel k : selected) {
                        names.add(k.getName());
                    }
                    TableConvolutionKernel.delete(names);
                    TableFloatMatrix.delete(names);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadList();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearAction(ActionEvent event) {
        if (tableData.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVaribles.getMessage("SureDelete"));
        ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonCancel) {
            return;
        }
        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    new TableConvolutionKernel().clear();
                    new TableFloatMatrix().clear();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadList();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void normalization(ActionEvent event) {
        float sum = 0.0f;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                sum += matrixValues[j][i];
            }
        }
        if (sum == 0) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                matrixInputs[j][i].setText(ValueTools.roundFloat5(matrixValues[j][i] / sum) + "");
            }
        }
        isSettingValues = false;
        checkMatrix();
    }

    @FXML
    private void gaussianDistribution() {
        if (width != height || width < 3) {
            gaussButton.setDisable(true);
            return;
        }
        float[][] m = ConvolutionKernel.makeGaussMatrix(width / 2);
        isSettingValues = true;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                matrixInputs[j][i].setText(m[j][i] + "");
            }
        }
        matrixValues = m;
        isSettingValues = false;
    }

    @FXML
    private void examplesAction(ActionEvent event) {

        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    TableConvolutionKernel.writeExamples();
                    TableFloatMatrix.writeExamples();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadList();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();

    }

    private boolean pickKernel() {
        if (kernel == null || matrixValues == null
                || !matrixValid || name == null || name.isEmpty()) {
            return false;
        }
        kernel.setName(name);
        kernel.setWidth(width);
        kernel.setHeight(height);
        kernel.setType(type);
        kernel.setGray(grayCheck.isSelected() ? 1 : 0);
        kernel.setEdge(edge_Op);
        kernel.setDescription(description);
        if (kernel.getCreateTime() == null || kernel.getCreateTime().isEmpty()) {
            kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        }
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setMatrix(matrixValues);
        return true;
    }

    @FXML
    private void testAction(ActionEvent event) {
        if (!pickKernel()) {
            return;
        }
        ImageManufactureConvolutionController c
                = (ImageManufactureConvolutionController) openStage(CommonValues.ImageManufactureConvolutionFxml, true);
        c.setParentController(getMyController());
        c.setParentFxml(getMyFxml());
        c.loadImage(new Image("img/p3.png"));
        c.setTab("convolution");
        c.showRef();
        c.selectKernel(kernel);
    }

    @FXML
    private void saveAction(ActionEvent event) {
        if (!pickKernel()) {
            return;
        }
        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    TableConvolutionKernel.write(kernel);
                    TableFloatMatrix.write(name, matrixValues);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadList();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();

    }

}
