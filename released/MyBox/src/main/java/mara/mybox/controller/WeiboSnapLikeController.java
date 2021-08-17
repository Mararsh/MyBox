package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapLikeController extends WeiboSnapRunController {

    public WeiboSnapLikeController() {
        baseTitle = Languages.message("WeiboSnap");
    }

    @Override
    public void initControls() {
        super.initControls();
        snapType = SnapType.Like;
    }

    @Override
    protected void setStartPage() {
        startPage = parameters.getLikeStartPage();
    }

    @Override
    protected void updateParameters() {
        currentMonthString = Languages.message("Like");
        if (parameters.getWebAddress().endsWith("home")) {
            currentAddress = parameters.getWebAddress().substring(0, parameters.getWebAddress().length() - 4);
        } else if (parameters.getWebAddress().endsWith("profile")) {
            currentAddress = parameters.getWebAddress().substring(0, parameters.getWebAddress().length() - 7);
        } else {
            currentAddress = parameters.getWebAddress();
        }
        if (!currentAddress.endsWith("/")) {
            currentAddress += "/";
        }
        currentAddress += "like?mod=like"
                + "&page=" + currentPage + "&mmts=" + new Date().getTime();
        UserConfig.setString("WeiboLikeLastPage", currentPage + "");

        if (parameters.isCreatePDF()) {
            pdfPath = new File(rootPath.getAbsolutePath() + File.separator
                    + currentMonthString + File.separator + "pdf");
            if (!pdfPath.exists()) {
                pdfPath.mkdirs();
            }
            pdfFilename = pdfPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页.pdf";
        }
        if (parameters.isCreateHtml()) {
            htmlPath = new File(rootPath.getAbsolutePath() + File.separator
                    + currentMonthString + File.separator + "html");
            if (!htmlPath.exists()) {
                htmlPath.mkdirs();
            }
            htmlFilename = htmlPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页.html";
        }
        if (parameters.isSavePictures()) {
            pixPath = new File(rootPath.getAbsolutePath() + File.separator
                    + currentMonthString + File.separator + "picture");
            if (!pixPath.exists()) {
                pixPath.mkdirs();
            }
            pixFilePrefix = pixPath.getAbsolutePath() + File.separator + accountName + "-"
                    + currentMonthString + "-第" + currentPage + "页-图";
        }
        parameters.setTitle(accountName + "-" + currentMonthString + "-第" + currentPage + "页");

    }

    @Override
    protected void showBaseInfo() {
        if (!openLoadingStage()) {
            return;
        }
        loadingController.setText(Languages.message("WeiboAddress") + ": " + parameters.getWebAddress());
        loadingController.addLine(Languages.message("Account") + ": " + accountName);
        loadingController.addLine(Languages.message("TotalLikeCount") + ": " + totalLikeCount);
        loadingController.addLine(Languages.message("TotalLikePages") + ": " + currentMonthPageCount);
        loadingController.addLine(Languages.message("CurrentLoadingPage") + ": " + currentPage);
        loadingController.addLine(Languages.message("PdfFilesSaved") + ": " + (savedPagePdfCount + savedMonthPdfCount));
        loadingController.addLine(Languages.message("HtmlFilesSaved") + ": " + savedHtmlCount);
        loadingController.addLine(Languages.message("PicturesSaved") + ": " + savedPixCount);

        showMemInfo();

    }

    @Override
    protected void showDynamicInfo() {
        showBaseInfo();
        super.showDynamicInfo();

    }
}
