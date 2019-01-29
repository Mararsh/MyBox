package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.PdfTools;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfViewController extends ImageViewerController {

    private PdfInformation pdfInformation;
    private int currentPage, currentPageTmp, percent, dpi;
    protected SimpleBooleanProperty infoLoaded;
    private boolean isSettingValues, isTransparent;

    @FXML
    protected Label pageLabel, cropLabel;
    @FXML
    protected Button firstPageButton, perviousPageButton, nextPageButton, lastPageButton, pageGoButton;
    @FXML
    protected TextField pageInput;
    @FXML
    protected HBox navBox;
    @FXML
    protected ComboBox<String> sizeBox, dpiBox;
    @FXML
    protected CheckBox selectCheck, transCheck;
    @FXML
    protected ToolBar optionBar;

    public PdfViewController() {
        sourcePathKey = "PdfSourcePath";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;

    }

    @Override
    protected void initializeNext2() {
        try {
            infoLoaded = new SimpleBooleanProperty(false);

            optionBar.disableProperty().bind(Bindings.not(infoLoaded));
            infoBar.disableProperty().bind(Bindings.not(infoLoaded));
            navBox.disableProperty().bind(Bindings.not(infoLoaded));
            opeBox.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            selectCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    cropLabel.setDisable(!selectCheck.isSelected());
                    if (!selectCheck.isSelected()) {
                        selectAllAction();
                        bottomLabel.setText("");
                    }
                }
            });
            cropLabel.setDisable(!selectCheck.isSelected());

            transCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transCheck.isSelected();
                    loadPage();
                }
            });

            sizeBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
            sizeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            percent = v;
                            sizeBox.getEditor().setStyle(null);
                            setSize(percent);
                        } else {
                            sizeBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        sizeBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
            dpiBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            dpi = v;
                            dpiBox.getEditor().setStyle(null);
                            loadPage();
                        } else {
                            dpiBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        dpiBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            dpiBox.getSelectionModel().select(0);

            pageInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (navBox.isDisabled()) {
                        currentPageTmp = 0;
                        pageInput.setStyle(null);
                        return;
                    }
                    try {
                        int v = Integer.valueOf(pageInput.getText()) - 1;
                        if (v >= 0 && v < pdfInformation.getNumberOfPages()) {
                            currentPageTmp = v;
                            pageInput.setStyle(null);
                            pageGoButton.setDisable(false);
                        } else {
                            pageInput.setStyle(badStyle);
                            pageGoButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        pageInput.setStyle(badStyle);
                        pageGoButton.setDisable(true);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void setSize(int percent) {
        if (imageView.getImage() == null) {
            return;
        }
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
    }

    @Override
    public void sourceFileChanged(final File file) {
        imageView.setImage(null);
        imageView.setTranslateX(0);
        pdfInformation = null;
        currentPage = 0;
        infoLoaded.set(false);
        pageInput.setText("1");
        pageLabel.setText("");
        percent = 0;
        if (file == null) {
            return;
        }
        sourceFile = file;
        pdfInformation = new PdfInformation(sourceFile);
        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
        loadPage();
    }

    public void loadInformation() {
        if (pdfInformation == null) {
            return;
        }
        bottomLabel.setText(AppVaribles.getMessage("CountingTotalNumber"));
        backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation.readInformation(null);
                infoLoaded.set(true);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        bottomLabel.setText("");
                        pageLabel.setText("/" + pdfInformation.getNumberOfPages());
                    }
                });
                return null;
            }
        };
        Thread thread = new Thread(backgroundTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadPage() {
        if (pdfInformation == null) {
            return;
        }
        if (currentPage < 0) {
            currentPage = 0;
        } else if (infoLoaded.get() && currentPage >= pdfInformation.getNumberOfPages()) {
            currentPage = pdfInformation.getNumberOfPages() - 1;
        }
        isSettingValues = true;
        pageInput.setText((currentPage + 1) + "");
        isSettingValues = false;
        perviousPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(infoLoaded.get() && currentPage >= (pdfInformation.getNumberOfPages() - 1));
        bottomLabel.setText("");
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ImageType type = ImageType.RGB;
                if (isTransparent) {
                    type = ImageType.ARGB;
                }
                BufferedImage bufferedImage = PdfTools.page2image(sourceFile, null, currentPage, dpi, type);
                image = SwingFXUtils.toFXImage(bufferedImage, null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setPreserveRatio(true);
                        imageView.setImage(image);
                        if (percent == 0) {
                            sizeBox.getSelectionModel().select("100");
                        }
                        selectAllAction();
                        if (!infoLoaded.get()) {
                            loadInformation();
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void popPdfInformation(ActionEvent event) {
        if (pdfInformation == null) {
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PdfInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            final PdfInformationController controller = fxmlLoader.getController();
            Stage infoStage = new Stage();
            controller.setMyStage(infoStage);
            infoStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            infoStage.setTitle(getMyStage().getTitle());
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(CommonValues.AppIcon);
            infoStage.setScene(new Scene(root));
            infoStage.show();

            controller.setInformation(pdfInformation);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void nextPageAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage++;
        loadPage();
    }

    @FXML
    protected void previousPageAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage--;
        loadPage();
    }

    @FXML
    protected void firstPageAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = 0;
        loadPage();
    }

    @FXML
    protected void lastPageAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = pdfInformation.getNumberOfPages() - 1;
        loadPage();

    }

    @FXML
    protected void goPageAction() {
        currentPage = currentPageTmp;
        loadPage();
    }

    public void setSizeBox() {
        percent = (int) (imageView.getFitHeight() * 100 / imageView.getImage().getHeight());
        isSettingValues = true;
        sizeBox.getSelectionModel().select(percent + "");
        isSettingValues = false;
    }

    @FXML
    @Override
    public void paneSize() {
        super.paneSize();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomIn() {
        super.zoomIn();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomOut() {
        super.zoomOut();
        setSizeBox();
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (!selectCheck.isSelected()) {
            return;
        }
        super.clickImage(event);
    }

}
