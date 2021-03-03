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

import com.ewssolutions.downloaderone.ui.ActionButtonCell;
import com.ewssolutions.downloaderone.ui.AutoCompleteComboBoxListener;

import com.ewssolutions.downloaderone.ui.Notification;
import com.ewssolutions.downloaderone.ui.NotificationType;
import com.ewssolutions.downloaderone.util.PrefKeys;
import com.sapher.youtubedl.YoutubeDL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.*;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.web.WebView;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.awt.*;

import java.io.*;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static com.ewssolutions.downloaderone.DownloadItemTask.CONVERTING;
import static java.awt.SplashScreen.getSplashScreen;

public class Start extends Application {


    public static String version = "4.2.0";

    //private String testUrl = "https://youtu.be/PinCg7IGqHg\nhttps://youtu.be/HWOWwO7XGgY\nhttps://youtu.be/gmPEB4DAaQo\nhttps://youtu.be/HWOWwO7XGgY\nhttps://youtu.be/gmPEB4DAaQo";
    //private String testUrl = "https://www.youtube.com/watch?v=GpMoRS_9bcM\nhttps://youtu.be/HWOWwO7XGgY\nhttps://youtu.be/gmPEB4DAaQo\nhttps://youtu.be/PinCg7IGqHg\nhttps://youtu.be/HWOWwO7XGgY";
    //private String testUrl = "https://www.youtube.com/watch?v=afF3XHW7mZ4&list=PLIgtqVSOWgBYF-K0KYfq8nFsyDDq0LrI7\nhttps://youtu.be/HWOWwO7XGgY"; //Dolly
    private String testUrl = "https://youtu.be/HWOWwO7XGgY\nhttps://www.youtube.com/watch?v=kUg7OO1gZk0&list=PLlQHeJpCWHxTr1Bs1tX840Tp2gKIU9l25"; //Undertones
    //private String testUrl = "https://youtu.be/HWOWwO7XGgY"; //Manse

    private Stage aboutDialog=null;
    private static Logger logger;
    public static DownloaderControler myDownloadController;
    private Boolean isDownloading=false;
    private Preferences prefs;
    /*
        "dev" startup command overrides this
    */
    public static boolean prod = true;
    public boolean showUpdateNotification = false;
    private String msg = "New version of youtube-dl available! You can upgrade with 'sudo youtube-dl --update'";

    public static void main(String[] args) {

        if(args.length>0){
            if(args[0].matches("dev")){
                prod =false;
            }
        }

        launch(args);

        logger = Logger.getLogger("DownloaderOne");
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        final SplashScreen splash = getSplashScreen();

        if(splash!=null){
            splash.createGraphics().scale(0.5,0.5);
            splash.update();
        }



        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("fxml/downloader.fxml"));

        AnchorPane myAnchorPane = loader.load();

        Platform.setImplicitExit(true);

        myDownloadController = loader.getController();
        //TODO refactor like this
        //myDownloadControler.intialize();
        //myDownloaderOneMenuBar().initialize();
        //TODO refactor

        myDownloadController.webview.getEngine().getHistory().setMaxSize(0);
        myDownloadController.owner = primaryStage;

        //Location /home/ews/.java/.userPrefs/
        myDownloadController.prefs = prefs = Preferences.userRoot().node(this.getClass().getName());

        //load default values in prefs
        if(prefs.keys().length==0){


            prefs.put(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue());
            prefs.put(PrefKeys.TOR_LOCATION.getKey(),PrefKeys.TOR_LOCATION.getDefaultValue());
            prefs.put(PrefKeys.SOCKS_PROXY_HOST.getKey(),PrefKeys.SOCKS_PROXY_HOST.getDefaultValue());
            prefs.put(PrefKeys.SOCKS_PROXY_PORT.getKey(),PrefKeys.SOCKS_PROXY_PORT.getDefaultValue());
            prefs.put(PrefKeys.USE_SOCKS_PROXY.getKey(),PrefKeys.USE_SOCKS_PROXY.getDefaultValue());
            prefs.put(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue());
            prefs.put(PrefKeys.YOUTUBE_DL_VERSION.getKey(),PrefKeys.YOUTUBE_DL_VERSION.getDefaultValue());

        }

        //load default values in settings screen
        myDownloadController.settingsInputDownloadLocation.setText(prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue()));
        myDownloadController.settingsInputTorLocation.setText(prefs.get(PrefKeys.TOR_LOCATION.getKey(),PrefKeys.TOR_LOCATION.getDefaultValue()));
        myDownloadController.setttingsCheckUseSocksProxy.setSelected(Boolean.parseBoolean(prefs.get(PrefKeys.USE_SOCKS_PROXY.getKey(),PrefKeys.USE_SOCKS_PROXY.getDefaultValue())));
        myDownloadController.settingsInputSocksHostIp.setText(prefs.get(PrefKeys.SOCKS_PROXY_HOST.getKey(),PrefKeys.SOCKS_PROXY_HOST.getDefaultValue()));
        myDownloadController.settingsInputSocksHostPort.setText(prefs.get(PrefKeys.SOCKS_PROXY_PORT.getKey(),PrefKeys.SOCKS_PROXY_PORT.getDefaultValue()));
        myDownloadController.setttingsCheckShowDownloadErrorMessage.setSelected(Boolean.parseBoolean(prefs.get(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue())));

        //Check Youtube-dl version
        if(prod){

            String installedVersion = YoutubeDL.getVersion();
            if(myDownloadController.checkYoutubeDlNeedsUpdate()){
                //Update needed
                myDownloadController.settingsYoutubeDlVersion.setText(installedVersion.concat(" -- "+msg));
                //Show Notification when Stage is Shown
                showUpdateNotification = true;

            }else{
                //Update not needed
                myDownloadController.prefs.put(PrefKeys.YOUTUBE_DL_VERSION.getKey(),installedVersion);
                myDownloadController.settingsYoutubeDlVersion.setText(prefs.get(PrefKeys.YOUTUBE_DL_VERSION.getKey(),PrefKeys.YOUTUBE_DL_VERSION.getDefaultValue()));
            }
        }

        ChangeListener myChangeListenerBoolean = new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(oldValue==newValue){

                    myDownloadController.setVisibileSaveCancelButtons(false);
                }else{
                    myDownloadController.setVisibileSaveCancelButtons(true);
                }
            }

        };

        ChangeListener myChangeListener = new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                if(oldValue.contentEquals(newValue)){
                    myDownloadController.setVisibileSaveCancelButtons(false);
                }else{
                    myDownloadController.setVisibileSaveCancelButtons(true);
                }
            }
        };

        //Integer filter
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String text = change.getText();

            if (text.matches("[0-9]*")) {
                return change;
            }

            return null;
        };

        TextFormatter<String> integerTextFormatter = new TextFormatter<>(integerFilter);

        //IP adres filter
        String partialBlock = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))" ;
        String subsequentPartialBlock = "(\\."+partialBlock+")" ;
        String ipAddress = partialBlock+"?"+subsequentPartialBlock+"{0,3}";
        String regex = "^"+ipAddress ;

        final UnaryOperator<TextFormatter.Change> ipAddressFilter = c -> {
            String text = c.getControlNewText();
            if  (text.matches(regex)) {
                return c ;
            } else {
                return null ;
            }
        };

        TextFormatter<String> ipTextFormatter = new TextFormatter<>(ipAddressFilter);

        myDownloadController.settingsInputDownloadLocation.textProperty().addListener(myChangeListener);
        myDownloadController.settingsInputTorLocation.textProperty().addListener(myChangeListener);

        myDownloadController.setttingsCheckUseSocksProxy.selectedProperty().addListener(myChangeListenerBoolean);

        myDownloadController.settingsInputSocksHostIp.textProperty().addListener(myChangeListener);
        myDownloadController.settingsInputSocksHostIp.setTextFormatter(ipTextFormatter);

        myDownloadController.settingsInputSocksHostPort.textProperty().addListener(myChangeListener);
        myDownloadController.settingsInputSocksHostPort.setTextFormatter(integerTextFormatter);

        myDownloadController.setttingsCheckShowDownloadErrorMessage.selectedProperty().addListener(myChangeListenerBoolean);


        myDownloadController.checkUseSocksProxy();



        if (prod) {
            if(myDownloadController.setttingsCheckUseSocksProxy.isSelected()){
                myDownloadController.startTor();
            }
        }

        myDownloadController.progressbar.setProgress(0);
        myDownloadController.progressbar.setVisible(true);

        if (prod) {
            myDownloadController.version.setText("DownloaderOne");


            myDownloadController.progressbar.setVisible(true);
            myDownloadController.progressbar.setProgress(0);
            myDownloadController.progressbar.progressProperty().bind(myDownloadController.webview.getEngine().getLoadWorker().progressProperty());


            myDownloadController.checkVideo.setDisable(true);
            myDownloadController.downloadButton.setDisable(true);
            myDownloadController.clearButton.setDisable(true);


            myDownloadController.downloadUrlTextField.setPromptText("Download url(s)");
            myDownloadController.site.setPromptText("Select website");



        }else{
            myDownloadController.downloadUrlTextField.setText(testUrl);
            setdownloadUrlTextFieldSize(testUrl);
        }


        myDownloadController.downloadUrlTextField.textProperty().addListener((obs, oldText, newText) -> {

            if(newText.length()>0){
                myDownloadController.checkVideo.setDisable(false);
                myDownloadController.downloadButton.setDisable(false);
                myDownloadController.clearButton.setDisable(false);
            }else{
                myDownloadController.checkVideo.setDisable(true);
                myDownloadController.downloadButton.setDisable(true);
                myDownloadController.clearButton.setDisable(true);

            }

            setdownloadUrlTextFieldSize(newText);

        });

        primaryStage.getIcons().add(new Image("images/downloader_one.png"));
        primaryStage.setTitle("DownloaderOne");
        primaryStage.setScene(new Scene(myAnchorPane));

        //Stylesheet
        primaryStage.getScene().getStylesheets().add("css/downloader_one.css");

        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        myAnchorPane.getChildren().add(menuBar);

        // File menu - new, save, exit
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu downloadMenu = new Menu("Download");
        Menu helpMenu = new Menu("Help");

        MenuItem exitMenuItem = new MenuItem("Close");
        exitMenuItem.setOnAction(actionEvent -> {
            stop();
        });

        MenuItem downloadListItem = new MenuItem("List");
        downloadListItem.setOnAction(actionEvent -> {
            SingleSelectionModel<Tab> selectionModel = myDownloadController.tabPaneDownloaderOne.getSelectionModel();
            selectionModel.select(myDownloadController.tabDownload);
        });

        MenuItem searchItem = new MenuItem("Search");
        searchItem.setOnAction(actionEvent -> {
            SingleSelectionModel<Tab> selectionModel = myDownloadController.tabPaneDownloaderOne.getSelectionModel();
            selectionModel.select(myDownloadController.tabSearch);
        });


        MenuItem settingsMenuItem = new MenuItem("Settings");
        settingsMenuItem.setOnAction(actionEvent -> {
            SingleSelectionModel<Tab> selectionModel = myDownloadController.tabPaneDownloaderOne.getSelectionModel();
            selectionModel.select(myDownloadController.tabSettings);

        });


        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(actionEvent -> {

            aboutDialog = new Stage();

            aboutDialog.initModality(Modality.NONE);
            aboutDialog.initOwner(primaryStage);
            aboutDialog.setMinWidth(400);
            aboutDialog.setMinHeight(300);

            VBox dialogVbox = new VBox(10);
            dialogVbox.setAlignment(Pos.CENTER);

            try {

                ImageView myImageView = new ImageView();
                myImageView.setImage(new Image("images/downloaderOne.png"));
                myImageView.setFitHeight(150);
                myImageView.setFitWidth(150);
                myImageView.setPreserveRatio(true);

                Text myAboutText = new Text("DownloaderOne \nversion ".concat(version));
                myAboutText.setTextAlignment(TextAlignment.CENTER);

                //DEV work in progress
                WebView myWebView  = new WebView();
                String changesUrl = this.getClass().getClassLoader().getResource("data/changes.html").toURI().toString();
                myWebView.getEngine().load(changesUrl);
                myWebView.setMaxWidth(400);
                myWebView.setVisible(false);
                myWebView.setMinHeight(0);

                Button myChangesButton = new Button("Changes");
                myChangesButton.setVisible(true);
                //DEV work in progress
                myChangesButton.setOnAction(event -> {

                    if (myWebView.isVisible()){
                        myWebView.setVisible(false);
                        myWebView.setMinHeight(0);
                        aboutDialog.setHeight(300);

                    }else{
                        myWebView.setVisible(true);
                        myWebView.setMinHeight(200);
                        aboutDialog.setHeight(500);;
                    }
                });

                dialogVbox.getChildren().add(myImageView);
                dialogVbox.getChildren().add(myAboutText);
                dialogVbox.getChildren().add(myChangesButton);
                //DEV work in progress
                dialogVbox.getChildren().add(myWebView);


            } catch (URISyntaxException e) {
                //do nothing...!?
            }


            Scene dialogScene = new Scene(dialogVbox, 400, 300);
            aboutDialog.setScene(dialogScene);

            aboutDialog.centerOnScreen();
            aboutDialog.initStyle(StageStyle.DECORATED);
            aboutDialog.show();


            aboutDialog.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (! isNowFocused) {
                    aboutDialog.hide();
                }
            });

        });

        MenuItem openDirMenuItem = new MenuItem("Downloads");
        openDirMenuItem.setOnAction(actionEvent -> {

            if (Desktop.isDesktopSupported()) {

                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        File path = new File(prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue()));
                        Desktop.getDesktop().open(path);

                        return null;
                    }
                };

                new Thread(task).start();

            } else {
                logger.log(Level.INFO, "Not supported Desktop open");

                String msg = "\"Not supported Desktop open\"";
                new Notification(null, NotificationType.Warning).setText(msg).show(5);

            }

        });

        fileMenu.getItems().addAll(openDirMenuItem, new SeparatorMenuItem(), exitMenuItem);
        editMenu.getItems().add(settingsMenuItem);
        downloadMenu.getItems().addAll(downloadListItem,searchItem);
        helpMenu.getItems().addAll(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu,editMenu,downloadMenu,helpMenu);

        new AutoCompleteComboBoxListener<>(myDownloadController.site);

        //ImageTorActive
        myDownloadController.imageTorActive.setImage(new Image("images/tor_active.png"));
        myDownloadController.setTorStartedImageOpacity();
        myDownloadController.getPropertyTorStarted().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                myDownloadController.setTorStartedImageOpacity();
            }
        });

        myDownloadController.startColumn.setCellValueFactory(new PropertyValueFactory<>("startItem"));
        myDownloadController.startColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.08)); // w * 1/16

        myDownloadController.stateColumn.setCellValueFactory(new PropertyValueFactory<>("stateItem"));
        myDownloadController.stateColumn.setCellFactory(downloadItemTaskStringTableColumn -> new TableCell<DownloadItemTask,String>() {

            @Override
            protected void updateItem(String state, boolean empty) {

                if(!empty && Objects.equals(state, "")){
                    state = DownloadItemTask.SEARCHING;
                    setText(state);
                    super.updateItem(state, empty);
                }else{
                    setText(state);
                    super.updateItem(state, empty);
                }
            }
        });

        myDownloadController.stateColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.10)); // w * 1/16

        myDownloadController.progressPercentageColumn = new TableColumn<>();
        myDownloadController.progressBarColumn = new TableColumn<>();

        myDownloadController.columnProgress.getColumns().addAll(myDownloadController.progressBarColumn, myDownloadController.progressPercentageColumn);

        myDownloadController.progressPercentageColumn.setStyle("-fx-pref-height: 0;");
        myDownloadController.progressBarColumn.setStyle("-fx-pref-height: 0;");

        myDownloadController.progressPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("progressItem"));
        myDownloadController.progressPercentageColumn.setCellFactory(column-> new TableCell<DownloadItemTask, Double>() {

            @Override
            protected void updateItem(Double progress, boolean empty) {

                if(progress!=null){
                    if(progress>=0){
                        setText(NumberFormat.getPercentInstance().format(progress));
                        super.updateItem(progress, empty);
                    }else{
                        setText(NumberFormat.getPercentInstance().format(0));
                        super.updateItem(0.0, true);
                    }
                }else{
                    setText("");
                    super.updateItem(0.0, empty);
                }
            }
        });

        myDownloadController.progressPercentageColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.05)); // w * 1/16


        myDownloadController.progressBarColumn.setCellValueFactory(new PropertyValueFactory<>("progressBarItem"));
        myDownloadController.progressBarColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.10)); // w * 1/16
        myDownloadController.progressBarColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        myDownloadController.titleColumn.setCellValueFactory(new PropertyValueFactory<>("titleItem"));
        myDownloadController.titleColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.24)); // w * 1/16

        myDownloadController.videoColumn.setCellValueFactory(new PropertyValueFactory<>("videoItem"));
        myDownloadController.videoColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.05)); // w * 1/16

        myDownloadController.filesizeColumn.setCellValueFactory(new PropertyValueFactory<>("videoFilesize"));
        myDownloadController.filesizeColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.08)); // w * 1/16

        myDownloadController.referenceColumn.setCellValueFactory(new PropertyValueFactory<>("referenceItem"));
        myDownloadController.referenceColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.10)); // w * 1/16

        // define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
        myDownloadController.actionColumn.setCellValueFactory(features -> new SimpleBooleanProperty(features.getValue() != null));
        myDownloadController.actionColumn.prefWidthProperty().bind(myDownloadController.downloadTable.widthProperty().multiply(0.20)); // w * 1/16

        // create a cell value factory with an add button for each row in the table.
        myDownloadController.actionColumn.setCellFactory(downloadItemTaskTableColumn -> new ActionButtonCell(primaryStage, myDownloadController));

        myDownloadController.initDownloadTableData();

        if(!prod)
        myDownloadController.downloadUrlTextField.setText(testUrl);

        PseudoClass searching = PseudoClass.getPseudoClass(DownloadItemTask.SEARCHING);
        PseudoClass downloading = PseudoClass.getPseudoClass(DownloadItemTask.DOWNLOADING);
        PseudoClass converting = PseudoClass.getPseudoClass(CONVERTING);
        PseudoClass finished = PseudoClass.getPseudoClass(DownloadItemTask.FINISHED);
        PseudoClass error = PseudoClass.getPseudoClass(DownloadItemTask.ERROR);
        PseudoClass canceled = PseudoClass.getPseudoClass(DownloadItemTask.CANCELED);

        myDownloadController.downloadTable.setRowFactory(tv -> {

            TableRow<DownloadItemTask> row = new TableRow<>();

            ChangeListener<String> changeListener = (obs, oldState, newState) -> {
                row.pseudoClassStateChanged(searching,newState.equalsIgnoreCase(DownloadItemTask.SEARCHING));
                row.pseudoClassStateChanged(downloading, newState.equalsIgnoreCase(DownloadItemTask.DOWNLOADING));
                row.pseudoClassStateChanged(converting, newState.equalsIgnoreCase(CONVERTING));
                row.pseudoClassStateChanged(finished, newState.equalsIgnoreCase(DownloadItemTask.FINISHED));
                row.pseudoClassStateChanged(error, newState.equalsIgnoreCase(DownloadItemTask.ERROR));
                row.pseudoClassStateChanged(canceled, newState.equalsIgnoreCase(DownloadItemTask.CANCELED));
            };

            row.itemProperty().addListener((obs, previousState, currentState) -> {

                if (previousState != null) {
                    previousState.getStateItem().removeListener(changeListener);
                }

                if (currentState != null) {
                    currentState.getStateItem().addListener(changeListener);
                    //Set the initial state of the pseudo classes
                    row.pseudoClassStateChanged(searching, currentState.getStateItem().get().contentEquals(DownloadItemTask.SEARCHING));
                    row.pseudoClassStateChanged(downloading, currentState.getStateItem().get().contentEquals(DownloadItemTask.DOWNLOADING));
                    row.pseudoClassStateChanged(converting, currentState.getStateItem().get().contentEquals(CONVERTING));
                    row.pseudoClassStateChanged(finished, currentState.getStateItem().get().contentEquals(DownloadItemTask.FINISHED));
                    row.pseudoClassStateChanged(error, currentState.getStateItem().get().contentEquals(DownloadItemTask.ERROR));
                    row.pseudoClassStateChanged(canceled, currentState.getStateItem().get().contentEquals(DownloadItemTask.CANCELED));
                } else {
                    row.pseudoClassStateChanged(searching, false);
                    row.pseudoClassStateChanged(downloading, false);
                    row.pseudoClassStateChanged(converting, false);
                    row.pseudoClassStateChanged(finished, false);
                    row.pseudoClassStateChanged(error, false);
                    row.pseudoClassStateChanged(canceled, false);
                }


            });


            Tooltip tip = new Tooltip();


            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    //DownloadItemTask rowData = row.getItem();
                    //System.out.println(rowData);
                    openDirMenuItem.fire();
                }

                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    //Empty purposefull
                    //overrides default behavior (css border not 0px)
                }

            });

            row.setOnMouseMoved(event -> {
                if (!row.isEmpty()) {

                    row.setTooltip(tip);
                    tip.setText(new Formatter().format("Progress: %.0f%%%n",row.getItem().getProgress()*100).toString());

                }
            });


            return row;
        });

        myDownloadController.tabDownload.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                EventType et = event.getEventType();
            }

        });


        //Show Main Window
        primaryStage.show();
        //Close Splash
        if(splash!=null){
            splash.close();
        }

        if(showUpdateNotification){
            new Notification(myDownloadController.owner, NotificationType.Info).setText(msg).show(7);
        }
    }

    @Override
    public void stop() {

        //Check active downloads:
        myDownloadController.downloadTable.getItems().forEach(downloadItemTask -> {

            if(downloadItemTask.getStateItem().get().contentEquals(DownloadItemTask.DOWNLOADING)){
                isDownloading(true);
            }
        });

        if(isDownloading()){

            String msg = "Still downloading, please wait";
            new Notification(null, NotificationType.Warning).setText(msg).show(5);

            //reset to false for a next try
            isDownloading(false);
        }else{
            try {
                //DEV work in progress
                if (myDownloadController.webview.getEngine().getUserDataDirectory() != null) {
                    myDownloadController.webview.getEngine().getUserDataDirectory().deleteOnExit();
                }
                super.stop();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
            }

            Platform.exit();
            System.exit(0);

        }
    }

    private void isDownloading(boolean isDownloading){
        this.isDownloading = isDownloading;
    }

    private boolean isDownloading(){
        return this.isDownloading;
    }


    private void setdownloadUrlTextFieldSize(String urls){
        //Resize textinput if more then 1 line
        int url = urls.split("\\n").length;
        if(url>=2){
            myDownloadController.downloadUrlTextField.setMinHeight(myDownloadController.downloadUrlTextField.getMaxHeight());
        }else if(url<=1){
            myDownloadController.downloadUrlTextField.setMinHeight(myDownloadController.downloadUrlTextField.getPrefHeight());
        }
    }




}
