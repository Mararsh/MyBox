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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import mara.mybox.data.PdfInformation;
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
    private boolean isTransparent;

    @FXML
    protected Label pageLabel;
    @FXML
    protected Button pageGoButton;
    @FXML
    protected TextField pageInput;
    @FXML
    protected ComboBox<String> sizeBox, dpiBox;
    @FXML
    protected CheckBox transCheck;
    @FXML
    protected HBox pageNavBox;

    public PdfViewController() {
        sourcePathKey = "PdfSourcePath";
        TipsLabelKey = "PdfViewTips";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;
    }

    @Override
    protected void initializeNext2() {
        try {
            infoLoaded = new SimpleBooleanProperty(false);

            operation1Box.disableProperty().bind(Bindings.not(infoLoaded));
            pageNavBox.disableProperty().bind(Bindings.not(infoLoaded));

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
                    if (pageNavBox.isDisabled()) {
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

    @Override
    protected void initOperation2Box() {

        operation2Box.disableProperty().bind(
                Bindings.isNull(imageView.imageProperty())
        );

        cropCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                AppVaribles.setUserConfigValue(SelectKey, cropCheck.isSelected());
                checkSelect();
            }
        });
        cropCheck.setSelected(AppVaribles.getUserConfigBoolean(SelectKey, true));
        checkSelect();
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
        try {
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
        } catch (Exception e) {
            logger.debug(e.toString());
        }
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
        previousButton.setDisable(currentPage <= 0);
        nextButton.setDisable(infoLoaded.get() && currentPage >= (pdfInformation.getNumberOfPages() - 1));
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
                        fitSize();
                        checkSelect();

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
    @Override
    public void infoAction() {
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
    @Override
    public void nextAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage++;
        loadPage();
    }

    @FXML
    @Override
    public void previousAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage--;
        loadPage();
    }

    @FXML
    @Override
    public void firstAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = 0;
        loadPage();
    }

    @FXML
    @Override
    public void lastAction() {
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
    public void imageSize() {
        super.imageSize();
        setSizeBox();
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

}
