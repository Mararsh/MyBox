package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    protected double mouseX, mouseY;
    protected int zoomStep = 10;
    protected int currentAngle = 0, rotateAngle = 90;

    @FXML
    protected TextField imageFile;
    @FXML
    protected HBox toolBar;
    @FXML
    protected Button iButton, mButton, gButton, pButton, inButton, outButton, lButton, rButton;
    @FXML
    protected Button tButton, sButton, mrButton, mlButton, upButton, downButton;

    @Override
    protected void initializeNext2() {
        try {

            toolBar.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );
//            setTips();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file.isDirectory()) {
            AppVaribles.setConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVaribles.setConfigValue(sourcePathKey, file.getParent());
        }
        loadImage(file, false);
    }

    @Override
    public void afterImageLoaded() {
        imageView.setPreserveRatio(true);
//        logger.debug(scrollPane.getHeight() + " " + imageInformation.getyPixels());
        if (scrollPane.getHeight() < imageInformation.getyPixels()) {
            imageView.setFitHeight(scrollPane.getHeight() - 5);
            imageView.setFitWidth(scrollPane.getWidth() - 1);
        } else {
            imageView.setFitHeight(imageInformation.getyPixels());
            imageView.setFitWidth(imageInformation.getxPixels());
        }
        try {
            imageView.setImage(image);
//                        imageView.setImage(new Image("file:" + fileName, true));
            if (imageFile != null) {
                imageFile.setText(sourceFile.getName());
            }

        } catch (Exception e) {
            imageView.setImage(null);
            popInformation(AppVaribles.getMessage("NotSupported"));
        }
    }

//    @FXML
//    public void imageMousePressed(MouseEvent event) {
//        mouseX = event.getX();
//        mouseY = event.getY();
//    }
//    @FXML
//    public void imageMouseReleased(MouseEvent event) {
//        FxmlTools.setScrollPane(scrollPane, mouseX - event.getX(), mouseY - event.getY());
//    }
    @FXML
    public void popImageInformation() {
        showImageInformation(imageInformation);
    }

    @FXML
    public void popImageInformation2() {
        showImageInformation(imageInformation);
    }

    @FXML
    public void popMetaData() {
        showImageMetaData(imageInformation);
    }

    @FXML
    public void popMetaData2() {
        showImageMetaData(imageInformation);
    }

    @FXML
    public void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 + zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 + zoomStep / 100.0f));
    }

    @FXML
    public void zoomOut() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 - zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 - zoomStep / 100.0f));

    }

    @FXML
    public void imageSize() {
        imageView.setFitHeight(imageInformation.getyPixels());
        imageView.setFitWidth(imageInformation.getxPixels());
    }

    @FXML
    public void paneSize() {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
    }

    @FXML
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        currentAngle = (currentAngle + rotateAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void rotateLeft() {
        currentAngle = (360 - rotateAngle + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void turnOver() {
        currentAngle = (180 + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
    }

    public void setQuickTips() {
        if (iButton != null) {
            FxmlTools.quickTooltip(iButton, new Tooltip(AppVaribles.getMessage("ImageInformation")));
            FxmlTools.quickTooltip(mButton, new Tooltip(AppVaribles.getMessage("ImageMetaData")));
        }
        if (pButton != null) {
            FxmlTools.quickTooltip(pButton, new Tooltip(AppVaribles.getMessage("PaneSize")));
            FxmlTools.quickTooltip(gButton, new Tooltip(AppVaribles.getMessage("ImageSize")));
            FxmlTools.quickTooltip(inButton, new Tooltip(AppVaribles.getMessage("ZoomIn")));
            FxmlTools.quickTooltip(outButton, new Tooltip(AppVaribles.getMessage("ZoomOut")));
            FxmlTools.quickTooltip(lButton, new Tooltip(AppVaribles.getMessage("RotateLeft")));
            FxmlTools.quickTooltip(rButton, new Tooltip(AppVaribles.getMessage("RotateRight")));
            FxmlTools.quickTooltip(tButton, new Tooltip(AppVaribles.getMessage("TurnOver")));
            FxmlTools.quickTooltip(sButton, new Tooltip(AppVaribles.getMessage("Straighten")));
        }
        if (mrButton != null) {
            FxmlTools.quickTooltip(mrButton, new Tooltip(AppVaribles.getMessage("MoveRight")));
            FxmlTools.quickTooltip(mlButton, new Tooltip(AppVaribles.getMessage("MoveLeft")));
            FxmlTools.quickTooltip(upButton, new Tooltip(AppVaribles.getMessage("MoveUp")));
            FxmlTools.quickTooltip(downButton, new Tooltip(AppVaribles.getMessage("MoveDown")));
        }
    }

    public void setTips() {
        if (iButton != null) {
            iButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageInformation")));
            mButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageMetaData")));
        }
        if (pButton != null) {
            pButton.setTooltip(new Tooltip(AppVaribles.getMessage("PaneSize")));
            gButton.setTooltip(new Tooltip(AppVaribles.getMessage("ImageSize")));
            inButton.setTooltip(new Tooltip(AppVaribles.getMessage("ZoomIn")));
            outButton.setTooltip(new Tooltip(AppVaribles.getMessage("ZoomOut")));
            lButton.setTooltip(new Tooltip(AppVaribles.getMessage("RotateRight")));
            rButton.setTooltip(new Tooltip(AppVaribles.getMessage("RotateRight")));
            tButton.setTooltip(new Tooltip(AppVaribles.getMessage("TurnOver")));
            sButton.setTooltip(new Tooltip(AppVaribles.getMessage("Straighten")));
        }
        if (mrButton != null) {
            mrButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveRight")));
            mlButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveLeft")));
            upButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveUp")));
            downButton.setTooltip(new Tooltip(AppVaribles.getMessage("MoveDown")));
        }
    }

    public void loadImage(final String fileName) {
        try {
            sourceFile = new File(fileName).getAbsoluteFile(); // Must convert to AbsoluteFile!
            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                loadImage(sourceFile, false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageInformation(ImageFileInformation info) {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageInformationController controller = fxmlLoader.getController();
            controller.loadInformation(info);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(getMyStage().getTitle());
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageMetaData(ImageFileInformation info) {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageMetaDataFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageMetaDataController controller = fxmlLoader.getController();
            controller.loadData(info);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(getMyStage().getTitle());
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public HBox getToolBar() {
        return toolBar;
    }

    public void setToolBar(HBox toolBar) {
        this.toolBar = toolBar;
    }

    public Button getiButton() {
        return iButton;
    }

    public void setiButton(Button iButton) {
        this.iButton = iButton;
    }

    public Button getmButton() {
        return mButton;
    }

    public void setmButton(Button mButton) {
        this.mButton = mButton;
    }

    public Button getgButton() {
        return gButton;
    }

    public void setgButton(Button gButton) {
        this.gButton = gButton;
    }

    public Button getpButton() {
        return pButton;
    }

    public void setpButton(Button pButton) {
        this.pButton = pButton;
    }

    public Button getInButton() {
        return inButton;
    }

    public void setInButton(Button inButton) {
        this.inButton = inButton;
    }

    public Button getOutButton() {
        return outButton;
    }

    public void setOutButton(Button outButton) {
        this.outButton = outButton;
    }

    public Button getlButton() {
        return lButton;
    }

    public void setlButton(Button lButton) {
        this.lButton = lButton;
    }

    public Button getrButton() {
        return rButton;
    }

    public void setrButton(Button rButton) {
        this.rButton = rButton;
    }

}
