package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-13
 * @Description
 * @License Apache License Version 2.0
 */
public class WeiboSnapLikeController extends WeiboSnapRunController {

    public WeiboSnapLikeController() {
        baseTitle = AppVariables.message("WeiboSnap");
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
        currentMonthString = message("Like");
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
        AppVariables.setUserConfigValue("WeiboLikeLastPage", currentPage + "");

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
        loadingController.setText(AppVariables.message("WeiboAddress") + ": " + parameters.getWebAddress());
        loadingController.addLine(AppVariables.message("Account") + ": " + accountName);
        loadingController.addLine(AppVariables.message("TotalLikeCount") + ": " + totalLikeCount);
        loadingController.addLine(AppVariables.message("TotalLikePages") + ": " + currentMonthPageCount);
        loadingController.addLine(AppVariables.message("CurrentLoadingPage") + ": " + currentPage);
        loadingController.addLine(AppVariables.message("PdfFilesSaved") + ": " + (savedPagePdfCount + savedMonthPdfCount));
        loadingController.addLine(AppVariables.message("HtmlFilesSaved") + ": " + savedHtmlCount);
        loadingController.addLine(AppVariables.message("PicturesSaved") + ": " + savedPixCount);

        showMemInfo();

    }

    @Override
    protected void showDynamicInfo() {
        showBaseInfo();
        super.showDynamicInfo();

    }
}
