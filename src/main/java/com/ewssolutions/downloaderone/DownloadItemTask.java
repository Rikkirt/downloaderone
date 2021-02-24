package com.ewssolutions.downloaderone;

import com.ewssolutions.downloaderone.ui.Notification;
import com.ewssolutions.downloaderone.ui.NotificationType;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.sapher.youtubedl.mapper.VideoInfo;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.tk.ScreenConfigurationAccessor;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.text.WordUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.image.*;
import java.util.logging.Logger;

public class DownloadItemTask extends Task<Void>{


    public static final String SEARCHING = "Searching";
    public static final String FINISHED = "Finished";
    public static final String DOWNLOADING = "Downloading";
    public static final String CONVERTING = "Converting";
    public static final String ERROR = "Error";
    public static final String CANCELED = "Canceled";
    public static final String YES = "Yes";
    public static final String NO = "No";


    private final StringProperty videoItem;
    private final StringProperty titleItem;
    private final IntegerProperty idItem;
    private final StringProperty startItem;
    private final StringProperty stateItem;
    private final StringProperty urlItem;
    private final StringProperty referenceItem;
    private final DoubleProperty progressItem;
    private final DoubleProperty progressBarItem;
    private final StringProperty videoFilesize;
    private final YoutubeDLRequest request;

    private YoutubeDLResponse response;
    private VideoInfo videoInfo;

    public boolean checkShowDownloadErrorMessage;


    public String getVideoFilesize() {
        return videoFilesize.get();
    }

    private void setVideoFilesize() {

        ArrayList<Long> filesizeArray= new ArrayList<Long>() ;

        if(videoInfo!=null){
            videoInfo.formats.iterator().forEachRemaining(item-> {
                if(videoInfo.ext.contentEquals(item.ext)){
                    filesizeArray.add(item.filesize);
                }
            });

            //FETCH highest mp4 filesize
            Collections.sort(filesizeArray);

            this.videoFilesize.set(String.format( "%.1f MB", filesizeArray.get(filesizeArray.size() - 1) * 0.000001));

        }else{
            this.videoFilesize.set("0.0");
        }
    }

    public StringProperty videoFilesizeProperty() {

        setVideoFilesize();

        return videoFilesize;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        this.setTitleItem(videoInfo.title);
        this.setVideoFilesize();

    }

    public String getVideoItem() {
        return videoItem.get();
    }

    public StringProperty videoItemProperty() {
        return videoItem;
    }

    public void setVideoItem(String videoItem) {
        this.videoItem.set(videoItem);
    }


    public int getIdItem() {
        return idItem.get();
    }

    public IntegerProperty idItemProperty() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem.set(idItem);
    }

    public double getProgressItem() {
        return progressItem.get();
    }

    public DoubleProperty progressItemProperty() {
        return progressItem;
    }

    public void setProgressItem(int progress) {
        this.progressItem.set(progress>0?progress*100:0);
    }


    public double getProgressBarItem() {
        return progressBarItem.get();
    }

    public DoubleProperty progressBarItemProperty() {
        return progressBarItem;
    }

    public void setProgressBarItem(double progress) {
        this.progressBarItem.set(progress);
    }





    public String getReferenceItem() {
        return referenceItem.get();
    }

    public StringProperty referenceItemProperty() {
        return referenceItem;
    }

    public void setReferenceItem(String reference) {
        this.referenceItem.set(reference);
    }


    public String getUrlItem() {
        return urlItem.get();
    }

    public StringProperty urlItemProperty() {
        return urlItem;
    }

    public void setUrlItem(String url) {
        this.urlItem.set(url);
    }


    public String getStartItem() {
        return startItem.get();
    }

    public StringProperty startItemProperty() {
        return startItem;
    }

    public void setStartItem(String start) {
        this.startItem.set(start);
    }



    public StringProperty getStateItem() {
        return stateItem;
    }


    public StringProperty stateItemProperty() {
        return stateItem;
    }

    public void setStateItem(String stateItem) {
        this.stateItem.set(stateItem);
    }


    public StringProperty getTitleItem() {
        return titleItem;
    }

    public StringProperty titleItemProperty() {
        return titleItem;
    }

    public void setTitleItem(String titleItem) {
        this.titleItem.set(titleItem);
    }




    public YoutubeDLResponse getResponse(){
        return response;
    }


    public void setCheckShowDownloadErrorMessage(Boolean checkShowDownloadErrorMessage){
        this.checkShowDownloadErrorMessage = checkShowDownloadErrorMessage;
    }



    public DownloadItemTask(YoutubeDLRequest aRequest,Stage owner) {

        this.request = aRequest;

        this.idItem = new SimpleIntegerProperty(0);

        this.referenceItem=new SimpleStringProperty("No reference");
        this.urlItem = new SimpleStringProperty(aRequest.getUrl());

        this.startItem = new SimpleStringProperty(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));

        this.progressItem = new SimpleDoubleProperty(0); //getProgress()


//        NumberFormat format1  = NumberFormat.getInstance();
//        format1.getPercentInstance();
//
//        this.progressItem = new SimpleDoubleProperty(new Double(format1.format(0)));

        this.progressBarItem = new SimpleDoubleProperty(0); //getProgress()

        this.stateItem = new SimpleStringProperty();

        this.videoItem = new SimpleStringProperty(YES);

        this.titleItem = new SimpleStringProperty("unkown");

        this.videoFilesize = new SimpleStringProperty("0");

        this.progressItem.unbind();
        this.progressBarItem.unbind();

        this.progressItem.bind(progressProperty());
        this.progressBarItem.bind(progressProperty());

        this.stateItem.unbind();
        this.stateItem.bind(messageProperty());

        setOnScheduled(event -> {
            updateMessage(SEARCHING);
            updateProgress(1, 100);
        });

        setOnRunning(event -> {
            updateMessage(DOWNLOADING);
            owner.getScene().setCursor(Cursor.DEFAULT);
            updateProgress(0, 100);
        });

        setOnSucceeded(event -> {

            updateMessage(FINISHED);
            owner.getScene().setCursor(Cursor.DEFAULT);

            String msg = "Finished with downloading and conversion of '".concat(getReferenceItem()).concat("'");
            new Notification(NotificationType.Info).setText(msg).show(5);
        });

        setOnFailed(event -> {
            updateMessage(ERROR);
            updateProgress(0, 100);

            owner.getScene().setCursor(Cursor.DEFAULT);

            String msg="";

            if(checkShowDownloadErrorMessage){
                msg = WordUtils.wrap(event.getSource().getException().getLocalizedMessage(), 100);
            }else{
                msg = "Error downloading ".concat(getReferenceItem());
            }

            new Notification(NotificationType.Error).setText(msg).show(5);


        });

        setOnCancelled(event -> {

            updateMessage(CANCELED);
            updateProgress(0, 100);

            String msg = "Request cancelled for '".concat(getReferenceItem()).concat("'");
            new Notification(NotificationType.Warning).setText(msg).show(7);

        });

    }


    @Override
    protected Void call() throws Exception {

        setVideoInfo(YoutubeDL.getVideoInfo(request.getUrl()));

        response = YoutubeDL.execute(request, (aProgress, etaInSeconds) -> {

            if(aProgress==100.0){
                updateMessage(CONVERTING);
                updateProgress(0,100);
            }

            updateProgress(aProgress, 100);

        });

        return null;
    }

}