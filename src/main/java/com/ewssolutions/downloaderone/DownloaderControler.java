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

import com.ewssolutions.downloaderone.util.PrefKeys;
import com.ewssolutions.downloaderone.util.ProcessReadTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapher.youtubedl.*;
import com.sapher.youtubedl.mapper.VideoInfo;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

import static com.ewssolutions.downloaderone.Start.prod;


public class DownloaderControler {

    @FXML
    public WebView webview;

    @FXML
    public TextArea downloadUrlTextField;

    @FXML
    public ComboBox<String> site;

    @FXML
    public Button downloadButton;

    @FXML
    public Button clearButton;

    @FXML
    public GridPane gridpaneDownloads;

    @FXML
    public Tab tabDownload;

    @FXML
    public Tab tabSearch;

    @FXML
    public Tab tabSettings;

    @FXML
    public TableView<DownloadItemTask> downloadTable;

    @FXML
    public TableColumn<DownloadItemTask, String> videoColumn;

    @FXML
    public TableColumn<DownloadItemTask, String> titleColumn;


    @FXML
    public TableColumn<DownloadItemTask, String> filesizeColumn;

    @FXML
    public TableColumn<DownloadItemTask, Integer> idColumn;

    @FXML
    public TableColumn<DownloadItemTask, String> stateColumn;

    @FXML
    public TableColumn<DownloadItemTask,Double> columnProgress;


    public TableColumn<DownloadItemTask,Double> progressPercentageColumn;


    public TableColumn<DownloadItemTask,Double> progressBarColumn;

    @FXML
    public TableColumn<DownloadItemTask, String> startColumn;

    @FXML
    public TableColumn<DownloadItemTask, String> urlColumn;

    @FXML
    public TableColumn<DownloadItemTask, String> referenceColumn;

    @FXML
    public TableColumn<DownloadItemTask, Boolean> actionColumn;

    @FXML
    public CheckBox checkVideo;
    public CheckBox setttingsCheckUseSocksProxy;
    public CheckBox setttingsCheckShowDownloadErrorMessage;

    @FXML
    public Label version;
    public Label settingslabelSocksHostPortMsg;
    public Label settingsInputSocksHostPortLabel;
    public Label settingsInputSocksHostIPLabel;

    @FXML
    public ImageView imageAbout;
    public ImageView imageTorActive;

    @FXML
    public ProgressBar progressbar;

    @FXML
    public TabPane tabPaneDownloaderOne;

    @FXML
    public TextField settingsInputDownloadLocation;
    public TextField settingsInputTorLocation;
    public TextField settingsInputSocksHostIp;
    public TextField settingsInputSocksHostPort;
    public ChoiceBox settingsFileTypeChoiceBox;

    public Label settingsYoutubeDlVersion;

    @FXML
    public Button settingsCancelButton;
    public Button settingsSaveButton;
    public Button installYoutubeDlLatestVersionButton;

    // string array
    String audioFileType[] = { "MP3", "Opus"};

    public String youtubeDlVersion = "";

    private Process processTor;

    private java.net.CookieManager manager;

    private Boolean continueDownload = false;

    private String downloadReferenceText;

    private ObservableList<DownloadItemTask> downloadTableItemTasks = FXCollections.observableArrayList();

    private Collection<DownloadItemTask> downloadTaskList = new ArrayList<DownloadItemTask>();

    private VideoInfo videoInfo;

    public Stage owner;

    public Preferences prefs;

    private BooleanProperty torStartedProperty = new SimpleBooleanProperty();

    private long torPid;

    private Boolean continueCompare = true;

    private ExecutorService executor;

    private String latestVersion;

    public DownloaderControler(){

        version = new Label("Development");

        manager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(manager);

        manager.setCookiePolicy((uri, cookie) -> false);

    }




    /*
        Action Combobox website url
     */
    public void website(ActionEvent actionEvent) {
        if (prod) {

            if (site != null && site.getValue() != null) {

                if (!site.getValue().isEmpty()) {

                    String urlString = site.getValue();
                    if (!urlString.isEmpty()) {

                        manager.getCookieStore().removeAll();

                        String url;

                        if(urlString.contains("https://www.") || urlString.contains("https://")){
                            url = urlString;

                        } else {
                            url = "https://".concat(urlString);
                        }

                        webview.getEngine().load(url);

                        progressbar.setVisible(true);

                    }
                }
            }
        }
    }

    /*
       Action Clear Button
    */
    public void clearButtonAction(ActionEvent actionEvent) {
            downloadUrlTextField.clear();
    }
    /*
        Action Download Button
     */
    public void downloadButtonAction(ActionEvent actionEvent) {

        if (downloadUrlTextField.getText().isEmpty()) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().add(new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE));
            dialog.setContentText("Enter url for download");
            dialog.setResizable(true);
            dialog.showAndWait();
        } else {


            showReferenceDialog();

            if(continueDownload) {

                String url;
                int nr = 1;


                //Start procesbuilder script
                //downloadUrlTextField
                //youtube-dl --audio-format mp3 -k --prefer-ffmpeg --proxy socks5://127.0.0.1:9150/ https://www.youtube.com/watch?v=Pgqa3cVOxUc

                List<String> DownloadUrls =  Arrays.asList(downloadUrlTextField.getText().split("\\n").clone());

                //Filter youtube playlist urls
                List<String> TreatedDownloadUrls = prepareYTPlaylistExtraction(DownloadUrls);

                //Extract each
                for (String urlString : TreatedDownloadUrls){

                    if(urlString.contains("https://www.") || urlString.contains("https://")){
                        url = urlString;
//                    if (downloadUrlTextField.getText().contains("https://www.") || downloadUrlTextField.getText().contains("https://")) {
//                        url = downloadUrlTextField.getText();
                    } else {
                        url = "https://".concat(urlString);
                    }

                    String extraText = "";

                    if(TreatedDownloadUrls.size()>1){
                        extraText = "item-"+nr+++"";
                    }

                    //Set commands
                    prepareDownloadItemTask(url,String.valueOf(downloadReferenceText),extraText);

                }

                try {
                    executeDownloads();
                } catch (YoutubeDLException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /*
        If url contains "youtube" and "&list=" put all list items as separate DownloadTaskItems
    */
    private List<String> prepareYTPlaylistExtraction(List<String> urls) {

        List<String> result = new ArrayList<String>();

        for(String url: urls) {

            if(url.contains("www.youtu") && url.contains("&list=")) {

                //ORG
                //List<String> playlist = youtubeDlPLaylistInfo(url);
                //NEW
                JsonNode jsonNode = null;
                ObjectMapper mapper = new ObjectMapper();

                Process process = null;
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("youtube-dl",url,"--flat-playlist","--dump-single-json");

                try {

                    process = processBuilder.start();

                    ProcessReadTask task = new ProcessReadTask(process.getInputStream());

                    executor = Executors.newSingleThreadExecutor();
                    Future<List<String>> future = executor.submit(task);

                    List<String> output = future.get(5, TimeUnit.SECONDS);
                    for (String s : output) {
                        jsonNode = mapper.readTree(s);
                    }

                } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
                }

                if(jsonNode!=null && jsonNode.has("entries")){
                    jsonNode.get("entries").forEach(entryNode -> {
                        result.add("https://youtu.be/".concat(entryNode.get("url").asText()));
                    });
                }

            }else{
                //Add normal
                result.add(url);
            }
        }

        return result;
    }


    /*
           Save inputs to prefs
    */
    public void settingsSaveButtonAction(ActionEvent actionEvent) {

        boolean continueSave = true;
        //todo
        //Check for error before saving if needed....
        HashMap <Label,String> errorMap = new HashMap<Label,String>();


        for(Label label:errorMap.keySet()){
            label.setText(errorMap.get(label));
            continueSave = false;
        }

        if(continueSave){
            prefs.put(PrefKeys.DOWNLOAD_DIR.getKey(),settingsInputDownloadLocation.getText());
            prefs.put(PrefKeys.TOR_LOCATION.getKey(),settingsInputTorLocation.getText());
            prefs.put(PrefKeys.SOCKS_PROXY_HOST.getKey(),settingsInputSocksHostIp.getText());
            prefs.put(PrefKeys.SOCKS_PROXY_PORT.getKey(),settingsInputSocksHostPort.getText());
            prefs.put(PrefKeys.USE_SOCKS_PROXY.getKey(), String.valueOf(setttingsCheckUseSocksProxy.isSelected()));
            prefs.put(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(), String.valueOf(setttingsCheckShowDownloadErrorMessage.isSelected()));
            prefs.put(PrefKeys.YOUTUBE_DL_VERSION.getKey(), settingsYoutubeDlVersion.getText());
            prefs.put(PrefKeys.SOCKS_PROXY_PORT.getKey(),settingsInputSocksHostPort.getText());
            prefs.put(PrefKeys.AUDIO_FIlE_TYPE.getKey(),settingsFileTypeChoiceBox.getSelectionModel().selectedItemProperty().getValue().toString());

            setVisibileSaveCancelButtons(false);

            if(setttingsCheckUseSocksProxy.isSelected()){
                try {

                    startTor();

                    site.fireEvent(new ActionEvent());

                } catch (YoutubeDLException e) {
                    e.printStackTrace();
                }
            }else{
                stopTor();

                site.fireEvent(new ActionEvent());
            }
        }
    }

    /*
            Restore prefs
     */
    public void settingsCancelButtonAction(ActionEvent actionEvent) {

        settingsInputDownloadLocation.setText(prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue()));
        settingsInputTorLocation.setText(prefs.get(PrefKeys.TOR_LOCATION.getKey(),PrefKeys.TOR_LOCATION.getDefaultValue()));
        settingsInputSocksHostIp.setText(prefs.get(PrefKeys.SOCKS_PROXY_HOST.getKey(),PrefKeys.SOCKS_PROXY_HOST.getDefaultValue()));
        settingsInputSocksHostPort.setText(prefs.get(PrefKeys.SOCKS_PROXY_PORT.getKey(),PrefKeys.SOCKS_PROXY_PORT.getDefaultValue()));
        setttingsCheckUseSocksProxy.setSelected(Boolean.parseBoolean(prefs.get(PrefKeys.USE_SOCKS_PROXY.getKey(),PrefKeys.USE_SOCKS_PROXY.getDefaultValue())));
        setttingsCheckShowDownloadErrorMessage.setSelected(Boolean.parseBoolean(prefs.get(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue())));
        settingsYoutubeDlVersion.setText(prefs.get(PrefKeys.YOUTUBE_DL_VERSION.getKey(),PrefKeys.YOUTUBE_DL_VERSION.getDefaultValue()));
        settingsFileTypeChoiceBox.getSelectionModel().select(prefs.get(PrefKeys.AUDIO_FIlE_TYPE.getKey(),PrefKeys.AUDIO_FIlE_TYPE.getDefaultValue()));

        checkUseSocksProxy();

        setVisibileSaveCancelButtons(false);
    }


      public void setVisibileSaveCancelButtons(Boolean setVisible){

        if (setVisible){
            settingsSaveButton.setVisible(true);
            settingsCancelButton.setVisible(true);
        }else{
            settingsSaveButton.setVisible(false);
            settingsCancelButton.setVisible(false);
        }

      }

    @FXML
    private void settingsDownloadLocationDialogButton(){
        DirectoryChooser myDirChooser =  new DirectoryChooser();
        myDirChooser.setTitle("Choose download locaction");

        File file = new File(settingsInputDownloadLocation.getText());

        if(file.exists()) {

            myDirChooser.setInitialDirectory(file);
            file = myDirChooser.showDialog(owner);

            settingsInputDownloadLocation.setText(file.getAbsolutePath());
        }else{


            String userDirectoryString = System.getProperty("user.home");
            File f = new File(userDirectoryString);
            if(f.exists() && f.isDirectory()) {
                myDirChooser.setInitialDirectory(f);
                File newfile = myDirChooser.showDialog(owner);

                settingsInputDownloadLocation.setText(newfile.getAbsolutePath());

            }

        }

    }


    @FXML
    public void checkUseSocksProxy(){

        if(setttingsCheckUseSocksProxy.isSelected()){
            settingsInputSocksHostIp.setDisable(false);
            settingsInputSocksHostPort.setDisable(false);
            settingsInputSocksHostIPLabel.setDisable(false);
            settingsInputSocksHostPortLabel.setDisable(false);
        }else{
            settingsInputSocksHostIp.setDisable(true);
            settingsInputSocksHostPort.setDisable(true);
            settingsInputSocksHostIPLabel.setDisable(true);
            settingsInputSocksHostPortLabel.setDisable(true);
        }
    }

    @FXML
    public void checkShowDowloadErrorMessage(){

    }

    @FXML
    private void settingsTorLocationDialogButton(){
        FileChooser myFileChooser =  new FileChooser();
        myFileChooser.setTitle("Choose Tor locaction");

        File f = new File(settingsInputTorLocation.getText());

        if(f.exists() && !f.isDirectory()) {
            myFileChooser.setInitialDirectory(new File(f.getParent()));
        }else{

            String userDirectoryString = System.getProperty("user.home");
            f = new File(userDirectoryString);
            if(f.exists() && f.isDirectory()) {
                myFileChooser.setInitialDirectory(f);
            }

        }

        File file = myFileChooser.showOpenDialog(owner);

        if (file != null) {
            settingsInputTorLocation.setText(file.getAbsolutePath());
        }
    }

    /*
      Execute download for given url and reference
      @param restart if restart don t add to downloadTableItemTasks List
      Reference is Dir name
      Extra Reference is Item name
    */
    public void prepareDownloadItemTask(String url, String reference, String extraReference){

        // Build request
        //YoutubeDLRequest request = new YoutubeDLRequest(url, path);
        if(reference.isEmpty()){
            reference=String.valueOf(new Date());
        }

        if(extraReference.isEmpty()){
            extraReference = "";
        }else{
            extraReference = "-".concat(extraReference);
        }

        //proxyUrl.append(prefs.get(PrefKeys.SOCKS_PROXY_HOST.getKey(),PrefKeys.SOCKS_PROXY_HOST.getDefaultValue()));
        String dirString = prefs.get(PrefKeys.DOWNLOAD_DIR.getKey(),PrefKeys.DOWNLOAD_DIR.getDefaultValue());

        //Using reference as DIR needed for metatag
        String dirName = dirString.concat("/"+reference);

        //Create Dir
        if(!new File(dirName).exists()){
            new File(dirName).mkdir();
        }

        YoutubeDLRequest request = new YoutubeDLRequest(url, dirName);


        //request.setOption("force-ipv4");		// -4 use ip4
        //request.setOption("geo-bypass");
        request.setOption("ignore-errors");		// --ignore-errors

        //request.setOption("output", "%(artist)s/%(album)s/%(title)s.%(ext)s");	// --output "%(id)s"
        request.setOption("output", "%(artist)s/%(title)s.%(ext)s");	// --output "%(id)s"
        request.setOption("extract-audio");

        if(prefs.get(PrefKeys.AUDIO_FIlE_TYPE.getKey(),PrefKeys.AUDIO_FIlE_TYPE.getDefaultValue()).equalsIgnoreCase("opus")){
            request.setOption("audio-format","opus");
        }else{
            request.setOption("audio-format","mp3");
        }
        request.setOption("audio-quality","192K");//7


        if (checkVideo.isSelected()) {
            request.setOption("keep-video");
        }

        StringBuilder proxyUrl = new StringBuilder();

        if (setttingsCheckUseSocksProxy.isSelected()) {
            proxyUrl.append("socks5://");
            proxyUrl.append(prefs.get(PrefKeys.SOCKS_PROXY_HOST.getKey(),PrefKeys.SOCKS_PROXY_HOST.getDefaultValue()));
            proxyUrl.append(":");
            proxyUrl.append(prefs.get(PrefKeys.SOCKS_PROXY_PORT.getKey(),PrefKeys.SOCKS_PROXY_PORT.getDefaultValue()));
            proxyUrl.append("/");

            request.setOption("proxy",proxyUrl.toString());

        }

        //request.setOption("prefer-ffmpeg");
        //ffmpeg
        request.setOption("ffmpeg-location",System.getProperty("user.home").concat("/development/ffmpeg/ffmpeg-4-libvpx/bin/ffmpeg"));


        //download dir
        //request.setOption("rm-cache-dir");

        //request.setOption("o",System.getProperty("user.home").concat("/Muziek/DownloaderOne/%(artist)s/%(album)s/%(title)s.%(ext)s"));

        //"youtube-dl -k -o '~/Muziek/DownloaderOne/%(artist)s/%(album)s/%(title)s.%(ext)s' -x --audio-format mp3
        // --ffmpeg-location '/home/ews/development/ffmpeg/ffmpeg-4-libvpx/bin/ffmpeg'";



        request.setOption("retries", 5);		// --retries 10

        //request.setOption("print-traffic");

        DownloadItemTask downloadItemTaskTask = new DownloadItemTask(request,owner);
        downloadItemTaskTask.setCheckShowDownloadErrorMessage(Boolean.valueOf(prefs.get(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue())));

        downloadItemTaskTask.setVideoItem(checkVideo.isSelected()?DownloadItemTask.YES:DownloadItemTask.NO);
        downloadItemTaskTask.setReferenceItem(reference.concat(extraReference));
        downloadItemTaskTask.setDirReferenceItem(reference);
        downloadItemTaskTask.setIdItem(downloadTableItemTasks.size());

        //Updates the table view
        downloadTableItemTasks.add(downloadItemTaskTask);
        //List executable Tasks
        downloadTaskList.add(downloadItemTaskTask);

    }

    public void executeDownloads() throws YoutubeDLException {
        executor = Executors.newFixedThreadPool(20);
        downloadTaskList.iterator().forEachRemaining(executor::execute);
        executor.shutdown();
    }

    private void showReferenceDialog() {

        //Slightly modified from http://code.makery.ch/blog/javafx-dialogs-official

        // Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reference");

        //DEV work in progress
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Set the button types.
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
//        gridPane.setHgap(10);
//        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);
        //gridPane.setPadding(new Insets(20, 20, 10, 10));

        TextField referenceTextField = new TextField();
        referenceTextField.setPromptText("Reference");
        referenceTextField.setPrefColumnCount(20);
        gridPane.add(referenceTextField, 0, 0);

        dialog.getDialogPane().setContent(gridPane);

        // Request focus on the reference field by default.
        Platform.runLater(referenceTextField::requestFocus);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {

            String result="No reference";

            if (dialogButton == okButtonType) {
                continueDownload=true;
                result = referenceTextField.getText();
            } else if(dialogButton ==  ButtonType.CANCEL){
                continueDownload=false;
                owner.getScene().setCursor(Cursor.DEFAULT);
            }

            return result;
        });

        owner.getScene().setCursor(Cursor.WAIT);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(ref -> {
            downloadReferenceText=ref;
        });
    }


    /*
        Initialise screen with data
     */
    public void initDownloadTableData() {

        List<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        //init Download Tabel
        downloadTable.setItems(downloadTableItemTasks);
        //init website url combobox
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("data/urls");
            reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().add(new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE));
            dialog.setContentText("Error:".concat(e.getLocalizedMessage()));

            if (prod) {
                dialog.setContentText("Not all urls could be loaded");
            }

            site.getItems().clear();
            site.getItems().addAll("check.torproject.org","youtube.com");
            site.setValue("check.torproject.org");
            site.fireEvent(new ActionEvent());

            dialog.setResizable(true);
            dialog.showAndWait();

        } finally {

            if(lines != null && lines.size()>0) {
                    site.getItems().clear();
                    site.getItems().addAll(FXCollections.observableArrayList(lines));
                    //Not load automatic first
                    site.setValue(lines.get(0));
                    site.fireEvent(new ActionEvent());
                    //site.getItems().addAll("check.torproject.org","youtube.com");

            }
        }
    }


    /*
        Check if update is needed for youtube-dl
        i.e. "youtube-dl is up-to-date (2021.02.10)"
    */
    public boolean checkYoutubeDlNeedsUpdate(){

        String responseText = "youtube-dl is up-to-date";
        boolean result= false;
        YoutubeDLResponse response = null;

        try {
            //YoutubeDL.getVersion();
            YoutubeDLRequest request = new YoutubeDLRequest();
            request.setOption("update");

            DownloadProgressCallback callback = new DownloadProgressCallback() {
                @Override
                public void onProgressUpdate(String destination, float progress, long etaInSeconds) {
                    //System.out.println("Destination: "+destination );
                    //System.out.println("Update control: "+progress+"%, "+etaInSeconds+" sec/ to go." );
                }

            };

            response = YoutubeDL.execute(request, callback);
            result  =  !response.getOut().contains(responseText);

        } catch (YoutubeDLException e) {
            e.printStackTrace();
        }

        return result;

    }

    public void startTor() throws YoutubeDLException {

        Iterator<ProcessHandle> iter = ProcessHandle.allProcesses().iterator();

        while(continueCompare && iter.hasNext()){
             compareProcess(iter.next());
        }

        //reset for next run
        continueCompare = true;

        if(!torStartedProperty.getValue()){

            ArrayList<String> command = new ArrayList<>();
            ProcessBuilder pbTor = new ProcessBuilder();


            //command.add("/home/ews/tor-browser_nl/Browser/start-tor-browser");
            command.add(prefs.get(PrefKeys.TOR_LOCATION.getKey(), PrefKeys.TOR_LOCATION.getDefaultValue()));
            command.add("--detach");

            pbTor.command(command);


            try {

                processTor = pbTor.start();

                int err = 0;

                InputStream error = processTor.getErrorStream();
                for (int i = 0; i < error.available(); i++) {
                    System.out.println("" + error.read());
                    err = error.read();
                }

                if(error.read()>0){
                    System.out.println("Error starting TorBrowser");
                    torPid=0;
                }else {
                    torPid = processTor.pid();
                    torStartedProperty.setValue(true);
                }

            } catch (IOException e) {
                throw new YoutubeDLException(e);
            }

            //processTor.waitFor(7000, TimeUnit.MILLISECONDS);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        // SOCKS Proxy
        System.setProperty("socksProxyHost", prefs.get(PrefKeys.SOCKS_PROXY_HOST.getKey(), PrefKeys.SOCKS_PROXY_HOST.getDefaultValue()));
        System.setProperty("socksProxyPort", prefs.get(PrefKeys.SOCKS_PROXY_PORT.getKey(), PrefKeys.SOCKS_PROXY_PORT.getDefaultValue()));
    }

    public void stopTor(){

        //ProcessHandle.allProcesses().forEach(processHandle -> compareProcess(processHandle));


        Iterator<ProcessHandle> iter = ProcessHandle.allProcesses().iterator();
        while(continueCompare && iter.hasNext()){
              compareProcess(iter.next());
        }


        if(torStartedProperty.getValue() && torPid>0){

            ArrayList<String> command = new ArrayList<>();
            ProcessBuilder pbStopTor = new ProcessBuilder();
            command.add("kill");
            command.add(String.valueOf(torPid));
            //command.add(String.valueOf("tor"));

            pbStopTor.command(command);

            try {
                pbStopTor.start();
                torStartedProperty.setValue(false);

            } catch (IOException e) {
                e.printStackTrace();
                torStartedProperty.setValue(true);
            }

            System.out.println("Tor stopped");

            // SOCKS Proxy
            System.setProperty("socksProxyHost", "");
            System.setProperty("socksProxyPort", "");
        }
    }

    //Test methods displaying all processes
    private static String text(Optional<?> optional) {
        return optional.map(Object::toString).orElse("-");
    }

    private void compareProcess(ProcessHandle processHandle) {

        if(processHandle.info().commandLine().toString().contains("tor") && processHandle.info().commandLine().toString().contains("/firefox.real")) {
            torStartedProperty.setValue(true);
            torPid = processHandle.pid();
            continueCompare = false;
        }else{
            torStartedProperty.setValue(false);
            torPid = 0;
            continueCompare = true;
        }
    }

    /*
        Test methods displaying all processes
     */
    private static String processDetails(ProcessHandle process) {
        return String.format("%8d %-20s %-20s",
                process.pid(),
                text(Optional.of(process.info().command().orElse("no command found"))),
                text(process.info().commandLine()));
    }

    public BooleanProperty getPropertyTorStarted() {
        return torStartedProperty;
    }

    public void setTorStartedImageOpacity(){
        if(getPropertyTorStarted().getValue()){
            imageTorActive.setOpacity(1);
        }else{
            imageTorActive.setOpacity(0.3);
        }
    }




}
