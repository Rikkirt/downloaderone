/*

   Copyright 2021 EWS Solutions

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/


package com.ewssolutions.downloaderone;

import com.ewssolutions.downloaderone.ui.Notification;
import com.ewssolutions.downloaderone.ui.NotificationType;
import com.ewssolutions.downloaderone.util.PrefKeys;
import com.mpatric.mp3agic.*;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.sapher.youtubedl.mapper.VideoInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.prefs.Preferences;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.WordUtils;

import static com.ewssolutions.downloaderone.Start.myDownloadController;
import static com.ewssolutions.downloaderone.util.PrefKeys.DOWNLOAD_DIR;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


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

    private String destination;
    private String dirReferenceItem;

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

    public void setDirReferenceItem(String reference) {
        this.dirReferenceItem = reference;
    }

    public String getDirReferenceItem() {
        return this.dirReferenceItem;
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

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return this.destination;
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

            //setMetaFile Tags
            if(myDownloadController.checkVideo.isSelected()){
                System.out.println("When video is selected, metatags can not be set for now. Sorry!");
            }else{
                setMetaTags();
            }

            updateMessage(FINISHED);
            owner.getScene().setCursor(Cursor.DEFAULT);

            String msg = "Finished with downloading and conversion of '".concat(getReferenceItem()).concat("'");
            new Notification(owner, NotificationType.Info).setText(msg).show(5);

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

            new Notification(owner, NotificationType.Error).setText(msg).show(7);

        });

        setOnCancelled(event -> {

            updateMessage(CANCELED);
            updateProgress(0, 100);

            String msg = "Request cancelled for '".concat(getReferenceItem()).concat("'");
            new Notification(owner, NotificationType.Warning).setText(msg).show(7);

        });

    }

    @Override
    protected Void call() throws Exception {

        setVideoInfo(YoutubeDL.getVideoInfo(request.getUrl()));

        response = YoutubeDL.execute(request, (aDestination, aProgress, etaInSeconds) -> {

            if(aProgress==100.0){
                setDestination(aDestination);
                updateMessage(CONVERTING);
                updateProgress(0,100);
             }

            updateProgress(aProgress, 100);

        });

        return null;
    }

    /*
        Maybe swith to: http://www.jthink.net/jaudiotagger/examples_write.jsp
        No problem with saving to same file.

     */
    private void setMetaTags(){



        Mp3File mp3file = null;

        String ext = FilenameUtils.getExtension(destination);
        String newDestination = destination;

        /*
            Search only mp3 for now. Make variable for different extensions.
         */
        if(!ext.contentEquals("mp3")){
            newDestination = getDestination().substring(0,getDestination().length()-ext.length()).concat("mp3");
        }

        String location =  myDownloadController.prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue()).concat("/"+getDirReferenceItem()).concat("/"+newDestination);
        File originalMP3 = new File(location);

        if(originalMP3.exists()){

            try {

                mp3file = new Mp3File(location);

                ID3v2 id3v2Tag;
                if (mp3file.hasId3v2Tag()) {
                    id3v2Tag = mp3file.getId3v2Tag();
                } else {
                    // mp3 does not have an ID3v2 tag, let's create one..
                    id3v2Tag = new ID3v24Tag();
                    mp3file.setId3v2Tag(id3v2Tag);
                }

                int slash = newDestination.lastIndexOf("/");

                String name, title;
                name = title = newDestination.substring(slash+1,getDestination().length()-ext.length()-1);

                if(newDestination.substring(slash).contains("-")){
                    name = newDestination.substring(slash+1,newDestination.lastIndexOf("-")-1);
                    title = newDestination.substring(newDestination.lastIndexOf("-")+1,getDestination().length()-ext.length()-1);
                }

                id3v2Tag.setArtist(name);
                id3v2Tag.setTitle(title);
                id3v2Tag.setAlbum("Best of");
                id3v2Tag.setAlbumArtist(name);

                location =  myDownloadController.prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue())
                        .concat("/"+getDirReferenceItem())
                        .concat("/"+title.trim())
                        .concat(".mp3");

                new File(location);

                mp3file.save(location);

                if(!originalMP3.delete()){
                    System.out.println("File could not be removed");
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("No MP3 File Found. Tags not set");
        }
    }
}