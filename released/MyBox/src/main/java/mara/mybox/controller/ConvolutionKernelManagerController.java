package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.ConvolutionKernel.Convolution_Type;
import mara.mybox.db.table.TableConvolutionKernel;
import mara.mybox.db.table.TableFloatMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-11-06
 * @Description
 * @License Apache License Version 2.0
 */
public class ConvolutionKernelManagerController extends BaseTableViewController<ConvolutionKernel> {

    private int width, height, type, edge_Op;
    private boolean matrixValid;
    private GridPane matrixPane;
    private TextField[][] matrixInputs;
    private float[][] matrixValues;
    private String name, description;
    private ConvolutionKernel kernel;

    @FXML
    protected VBox mainPane;
    @FXML
    protected Button gaussButton;
    @FXML
    protected TableColumn<ConvolutionKernel, String> nameColumn, modifyColumn, createColumn, desColumn;
    @FXML
    protected TableColumn<ConvolutionKernel, Integer> widthColumn, heightColumn;
    @FXML
    protected ToggleGroup typeGroup, edgesGroup;
    @FXML
    protected TextField nameInput, desInput;
    @FXML
    protected ComboBox<String> widthBox, heightBox;
    @FXML
    protected HBox actionBox;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected CheckBox grayCheck, invertCheck;
    @FXML
    protected RadioButton zeroRadio, keepRadio;

    public ConvolutionKernelManagerController() {
        baseTitle = Languages.message("ConvolutionKernelManager");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initEditFields();
            loadList();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));
            heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));
            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            createColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            desColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void loadList() {
        List<ConvolutionKernel> records = TableConvolutionKernel.read();
        tableData.clear();
        tableData.addAll(records);

        if (parentController != null && parentController instanceof ImageManufactureEnhancementOptionsController) {
            ImageManufactureEnhancementOptionsController p = (ImageManufactureEnhancementOptionsController) parentController;
            p.loadKernelsList(records);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean none = isNoneSelected();
        editButton.setDisable(none);
        deleteButton.setDisable(none);
        copyButton.setDisable(none);
    }

    @Override
    public void itemClicked() {
        editAction();
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
            widthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSize();
                }
            });

            heightBox.getItems().addAll(sizeList);
            heightBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
            MyBoxLog.error(e.toString());
        }
    }

    private void checkType() {
        try {
            type = ConvolutionKernel.Convolution_Type.NONE;
            grayCheck.setDisable(true);
            invertCheck.setDisable(true);
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            if (Languages.message("Blur").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.BLUR;

            } else if (Languages.message("Sharpen").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.SHARPNEN;

            } else if (Languages.message("Emboss").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.EMBOSS;
                grayCheck.setDisable(false);
                invertCheck.setDisable(false);

            } else if (Languages.message("EdgeDetection").equals(selected.getText())) {
                type = ConvolutionKernel.Convolution_Type.EDGE_DETECTION;
                grayCheck.setDisable(false);
                invertCheck.setDisable(false);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkEdges() {
        try {
            if (isSettingValues) {
                return;
            }
            edge_Op = ConvolutionKernel.Edge_Op.COPY;
            RadioButton selected = (RadioButton) edgesGroup.getSelectedToggle();
            if (selected == null) {
                return;
            }
            if (!Languages.message("KeepValues").equals(selected.getText())) {
                edge_Op = ConvolutionKernel.Edge_Op.FILL_ZERO;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkSize() {
        try {
            width = Integer.valueOf(widthBox.getSelectionModel().getSelectedItem());
            if (width > 2 && width % 2 != 0) {
                ValidationTools.setEditorNormal(widthBox);
            } else {
                width = 0;
                ValidationTools.setEditorBadStyle(widthBox);
            }
        } catch (Exception e) {
            width = 0;
            ValidationTools.setEditorBadStyle(widthBox);
        }

        try {
            height = Integer.valueOf(heightBox.getSelectionModel().getSelectedItem());
            if (height > 2 && height % 2 != 0) {
                ValidationTools.setEditorNormal(heightBox);
            } else {
                ValidationTools.setEditorBadStyle(heightBox);
            }
        } catch (Exception e) {
            height = 0;
            ValidationTools.setEditorBadStyle(heightBox);
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
            matrixValues[width % 2][height % 2] = 1;
        } else if (height != matrixValues.length || width != matrixValues[0].length) {
            float[][] old = matrixValues;
            matrixValues = new float[height][width];
            for (int j = 0; j < Math.min(height, old.length); ++j) {
                System.arraycopy(old[j], 0, matrixValues[j], 0, Math.min(width, old[0].length));
            }

        }

        double colWidth = Math.max(60, scrollPane.getWidth() / width - 6);

        matrixPane = null;
        matrixPane = new GridPane();
        matrixInputs = null;
        matrixInputs = new TextField[height][width];
        matrixPane.setPadding(new Insets(5.0));
        matrixPane.setHgap(5);
        matrixPane.setVgap(5);
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
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
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                TextField valueInput = matrixInputs[j][i];
                try {
                    matrixValues[j][i] = Float.valueOf(valueInput.getText());
                    valueInput.setStyle(null);
                } catch (Exception e) {
                    matrixValid = false;
                    valueInput.setStyle(UserConfig.badStyle());
                }
            }
        }
        checkKernel();
    }

    private void checkKernel() {
        actionBox.setDisable(!matrixValid);
        saveButton.setDisable(!matrixValid || name == null || name.isEmpty());
    }

    @FXML
    @Override
    public void createAction() {
        isSettingValues = true;
        kernel = new ConvolutionKernel();
        nameInput.setText("");
        desInput.setText("");
        widthBox.getSelectionModel().select("3");
        heightBox.getSelectionModel().select("3");
        NodeTools.setRadioSelected(typeGroup, Languages.message("None"));
        matrixValues = null;
        nameInput.setDisable(false);
        isSettingValues = false;
        initMatrix();
    }

    @FXML
    @Override
    public void editAction() {
        final List<ConvolutionKernel> selected = selectedItems();
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
            NodeTools.setRadioSelected(typeGroup, Languages.message("Blur"));
        } else if (type == Convolution_Type.SHARPNEN) {
            NodeTools.setRadioSelected(typeGroup, Languages.message("Sharpen"));
        } else if (type == Convolution_Type.EMBOSS) {
            NodeTools.setRadioSelected(typeGroup, Languages.message("Emboss"));
            grayCheck.setDisable(false);
            invertCheck.setDisable(false);
        } else if (type == Convolution_Type.EDGE_DETECTION) {
            NodeTools.setRadioSelected(typeGroup, Languages.message("EdgeDetection"));
            grayCheck.setDisable(false);
            invertCheck.setDisable(false);
        } else {
            NodeTools.setRadioSelected(typeGroup, Languages.message("None"));
        }
        if (kernel.getEdge() == ConvolutionKernel.Edge_Op.COPY) {
            keepRadio.setSelected(true);
        } else {
            zeroRadio.setSelected(true);
        }
        grayCheck.setSelected(kernel.isGray());
        invertCheck.setSelected(kernel.isInvert());
        nameInput.setDisable(true);
        matrixValues = null;
        matrixValues = TableFloatMatrix.read(kernel.getName(), kernel.getWidth(), kernel.getHeight());
        isSettingValues = false;
        initMatrix();
    }

    @FXML
    @Override
    public void copyAction() {
        editAction();
        nameInput.setDisable(false);
        nameInput.setText(kernel.getName() + " mmm");

    }

    @FXML
    @Override
    public void deleteAction() {
        final List<ConvolutionKernel> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        if (!PopTools.askSure(getTitle(), Languages.message("SureDelete"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    List<String> names = new ArrayList<>();
                    for (ConvolutionKernel k : selected) {
                        names.add(k.getName());
                    }
                    TableConvolutionKernel.delete(names);
                    TableFloatMatrix.delete(names);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadList();
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        if (tableData.isEmpty()) {
            return;
        }
        if (!PopTools.askSure(getTitle(), Languages.message("SureDelete"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    new TableConvolutionKernel().clearData();
                    new TableFloatMatrix().clearData();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadList();
                }
            };
            start(task);
        }
    }

    @FXML
    protected void normalization(ActionEvent event) {
        float sum = 0.0f;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                sum += Math.abs(matrixValues[j][i]);
            }
        }
        if (sum == 0) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                matrixInputs[j][i].setText(FloatTools.roundFloat5(matrixValues[j][i] / sum) + "");
            }
        }
        isSettingValues = false;
        checkMatrix();
    }

    @FXML
    protected void gaussianDistribution() {
        if (width != height || width < 3) {
            gaussButton.setDisable(true);
            return;
        }
        float[][] m = ConvolutionKernel.makeGaussMatrix(width / 2);
        isSettingValues = true;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                matrixInputs[j][i].setText(m[j][i] + "");
            }
        }
        matrixValues = m;
        isSettingValues = false;
    }

    @FXML
    protected void zeroAction() {
        if (width < 3 || height < 3) {
            return;
        }
        matrixValues = new float[height][width];
        isSettingValues = true;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                matrixInputs[j][i].setText("0");
            }
        }
        isSettingValues = false;
    }

    @FXML
    public void oneAction() {
        if (width < 3 || height < 3) {
            return;
        }
        matrixValues = new float[height][width];
        isSettingValues = true;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                matrixValues[j][i] = 1;
                matrixInputs[j][i].setText("1");
            }
        }
        isSettingValues = false;
    }

    @FXML
    public void examplesAction(ActionEvent event) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    TableConvolutionKernel.writeExamples();
                    TableFloatMatrix.writeExamples();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadList();
                }
            };
            start(task);
        }
    }

    public boolean pickKernel() {
        if (kernel == null || matrixValues == null || !matrixValid) {
            return false;
        }
        kernel.setName(name);
        kernel.setWidth(width);
        kernel.setHeight(height);
        kernel.setType(type);
        kernel.setGray(grayCheck.isSelected());
        kernel.setInvert(invertCheck.isSelected());
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
    public void demo(ActionEvent event) {
        if (!pickKernel()) {
            return;
        }
        ImageManufactureController c
                = (ImageManufactureController) openStage(Fxmls.ImageManufactureFxml);
        c.loadImage(new Image("img/exg2.png"));
        c.applyKernel(kernel);
    }

    @FXML
    @Override
    public void saveAction() {
        if (!pickKernel() || name == null || name.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    TableConvolutionKernel.write(kernel);
                    TableFloatMatrix.write(name, matrixValues);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadList();
                }
            };
            start(task);
        }
    }

}
