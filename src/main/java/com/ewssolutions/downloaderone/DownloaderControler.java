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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
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
    public Label settingsYoutubeDlVersion;

    @FXML
    public Button settingsCancelButton;
    public Button settingsSaveButton;
    public Button installYoutubeDlLatestVersionButton;


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

    //private Boolean torStartedProperty=false;

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

        //executor = Executors.newFixedThreadPool(20);


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
        HashMap <Label,String> errorMap = new HashMap<Label,String>();


        //Check for error before saving if needed....



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
        setttingsCheckUseSocksProxy.setSelected(Boolean.valueOf(prefs.get(PrefKeys.USE_SOCKS_PROXY.getKey(),PrefKeys.USE_SOCKS_PROXY.getDefaultValue())));
        setttingsCheckShowDownloadErrorMessage.setSelected(Boolean.valueOf(prefs.get(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue())));
        settingsYoutubeDlVersion.setText(prefs.get(PrefKeys.YOUTUBE_DL_VERSION.getKey(),PrefKeys.YOUTUBE_DL_VERSION.getDefaultValue()));

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

        if(file!= null && file.exists()) {

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
        request.setOption("audio-format","mp3");

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

        //Metadata use Apache Tika
//        request.setOption("add-metadata");
//        request.setOption("metadata-from-title","%(artist)s %(title)s");
//        request.setOption("postprocessor-args","-metadata artist=%(artist)s");


        //--add-metadata --postprocessor-args "-metadata artist=Pink\ Floyd"



        DownloadItemTask downloadItemTaskTask = new DownloadItemTask(request,owner);
        downloadItemTaskTask.setCheckShowDownloadErrorMessage(Boolean.valueOf(prefs.get(PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getKey(),PrefKeys.SHOW_DOWNLOAD_ERROR_MESSAGE.getDefaultValue())));

        //Notifications.create().owner(owner).hideAfter(Duration.seconds(5)).text("Videoinfo ok").position(Pos.CENTER).showInformation();

        //Updates the table view
        downloadTableItemTasks.add(downloadItemTaskTask);
        //List executable Tasks
        downloadTaskList.add(downloadItemTaskTask);

        downloadItemTaskTask.setVideoItem(checkVideo.isSelected()?DownloadItemTask.YES:DownloadItemTask.NO);
        downloadItemTaskTask.setReferenceItem(reference+extraReference);
        downloadItemTaskTask.setIdItem(downloadTableItemTasks.size());

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


        // Request focus on the username field by default.
        Platform.runLater(referenceTextField::requestFocus);



        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {

            String result="No reference";

            if (dialogButton == okButtonType) {
                continueDownload=true;
                result = referenceTextField.getText();

//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        owner.getScene().setCursor(Cursor.WAIT);
//                    }
//                });



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


//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    owner.getScene().setCursor(Cursor.WAIT);
//                }
//            });
//
//            Task task = new Task<Void>() {
//                @Override
//                protected Void call() throws Exception {
//                    owner.getScene().setCursor(Cursor.WAIT);
//                    return null;
//
//                }
//            };
//
//            new Thread(task).start();
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

            InputStream in = this.getClass().getClassLoader().getResourceAsStream("data/website.txt");

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
        check if update is needed
        "youtube-dl is up-to-date (2021.02.10)"
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
                public void onProgressUpdate(float progress, long etaInSeconds) {
                    System.out.println("Update control: "+progress+"%, "+etaInSeconds+" sec/ to go." );
                }
            };

            response = YoutubeDL.execute(request, callback);
            result  =  response.getOut().contains(responseText);

        } catch (YoutubeDLException e) {
            e.printStackTrace();
        }

        return result;

    }

    /*
        Update Youtube-dl before start

        youtube-dl --version
        sudo youtube-dl --update
    */
     //curl --silent "https://api.github.com/repos/ytdl-org/youtube-dl/releases/latest" |  grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/'
    //"tag_name":"2019.08.13",
    @Deprecated
    public String checkLatestYoutubeDlVersionOnline(){

        String result = "";
        latestVersion= prefs.get(PrefKeys.YOUTUBE_DL_VERSION.getKey(),PrefKeys.YOUTUBE_DL_VERSION.getDefaultValue());

        try {

            URLConnection uc = new URL("https://api.github.com/repos/ytdl-org/youtube-dl/releases/latest").openConnection();

            InputStreamReader inputStreamReader = new InputStreamReader(uc.getInputStream());

            Scanner s = new Scanner(inputStreamReader).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";



            //ORG
            //int intIndex = result.indexOf("\"tag_name\":");
//            if(intIndex != - 1) {
//                latestVersion = result.substring(intIndex+12,intIndex+22);
//            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode Result = mapper.readTree(result);

            latestVersion = Result.get("tag_name").asText();



        } catch (IOException e) {
            e.printStackTrace();

        }

        return latestVersion;
    }



//    /*
//        Not Working correctly because of sudo needed
//    */
//    @Deprecated
//    @FXML
//    public String installYoutubeDlLatestVersion() throws YoutubeDLException {
//
//        installYoutubeDlLatestVersionButton.setDisable(true);
//        owner.getScene().setCursor(Cursor.WAIT);
//
//
//        ArrayList<String> command = new ArrayList<>();
//        ProcessBuilder pbYou = new ProcessBuilder();
//
//        pbYou.command(command);
//
//        //Request password for sudo
//        PasswordDialog pd = new PasswordDialog();
//        Optional<String> result = pd.showAndWait();
//
//        result.ifPresent(password -> {
//
//            InputStreamReader input;
//            OutputStreamWriter output;
//
//            try {
//
//                //command.add("/home/ews/tor-browser_nl/Browser/start-tor-browser");
//                //command.add("-c");
////                command.add("sudo");
////                command.add("-A");
////                command.add(password.concat("\n"));
////                command.add("echo");
////                command.add(password);
////                command.add("|");
//                command.add("sudo");
//                command.add("-S");
//                command.add("youtube-dl");
//                command.add("--update");
//
//
//                //command.add(password);
//                //command.add("\n\r");
//
////                command.add("sudo");
////                command.add("-S");
//
//
//                //echo password | sudo -S
//
//
//                Process processYou = pbYou.start();
//
//                output = new OutputStreamWriter(processYou.getOutputStream());
//                input = new InputStreamReader(processYou.getInputStream());
//
//                output.write(password.concat("\n"));
//                output.write('\n');
//                output.flush();
//
//
//                int bytes, tryies = 0;
//                char buffer[] = new char[1024];
//                while ((bytes = input.read(buffer, 0, 1024)) != -1) {
//                    if(bytes == 0)
//                        continue;
//                    //Output the data to console, for debug purposes
//                    String data = String.valueOf(buffer, 0, bytes);
//
//                    System.out.println(data);
//                    // Check for password request
//                    //if (data.contains("[sudo] password")) {
//                    if (data.contains("Updating to version")) {
//                        // Here you can request the password to user using JOPtionPane or System.console().readPassword();
//                        // I'm just hard coding the password, but in real it's not good.
//                        //char password[] = new char[]{'t','e','s','t'};
//                        System.out.println("password requested");
////
////                        output.write(password);
////                        output.write('\n');
////                        output.flush();
//                        // erase password data, to avoid security issues.
//
//                        //Arrays.fill(password.getBytes(), '\0');
//
//                        tryies++;
//                    }
//                }
//
//
//
//
//
//
//
//                //process.getInput().write(/*password as bytes plus \n */)…
//                //processYou.getOutputStream().write(password.concat("\n").getBytes());
//
//                //youtubeDlVersion = new BufferedReader(new InputStreamReader(processYou.getInputStream())).lines().collect(Collectors.joining("\n"));
//                //System.out.println("ytdlVersion: " + youtubeDlVersion);
//
//                int err = 0;
//
//                InputStream error = processYou.getErrorStream();
//                for (int i = 0; i < error.available(); i++) {
//                    System.out.println("" + error.read());
//                    err = error.read();
//                }
//
//                if(error.read()>0){
//                    System.out.println("Error installing version youtube-dl");
//                    Notifications.create().owner(owner).hideAfter(Duration.seconds(3)).text("Error installing version youtube-dl: "+err).position(Pos.CENTER).showInformation();
//
//                }else{
//                    Notifications.create().owner(owner).hideAfter(Duration.seconds(3)).text("Latest version Youtube-dl installed ("+latestVersion+")").position(Pos.CENTER).showInformation();
//
//                    prefs.put(PrefKeys.YOUTUBE_DL_VERSION.getKey(),latestVersion);
//                    settingsYoutubeDlVersion.setText(latestVersion);
//                    installYoutubeDlLatestVersionButton.setVisible(false);
//
//                }
//
//            } catch (IOException e) {
//
//                try {
//                    throw new YoutubeDLException(e);
//                } catch (YoutubeDLException ex) {
//                    ex.printStackTrace();
//                }
//
//
//            }finally {
//                owner.getScene().setCursor(Cursor.DEFAULT);
//                installYoutubeDlLatestVersionButton.setDisable(false);
//            }
//
//        });
//
//
//        owner.getScene().setCursor(Cursor.DEFAULT);
//        installYoutubeDlLatestVersionButton.setDisable(false);
//
//
//
//        return youtubeDlVersion;
//
//
//    }

    public void startTor() throws YoutubeDLException {


        Iterator<ProcessHandle> iter = ProcessHandle.allProcesses().iterator();


        while(continueCompare && iter.hasNext()){
             compareProcess(iter.next());
        }

        //reset for next run
        continueCompare = true;

//        ProcessHandle.allProcesses().forEach(processHandler ->{
//
//
//
//                    if(!torStartedProperty){
//                        compareProcess(processHandler);
//
//
//                    }else {
//                        return;
//                    }
//
//                    //System.out.println(processDetails(processHandler));
//
//                }
//
//        );

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
                Thread.currentThread().sleep(5000);
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

        ///String command = processHandle.info().command().toString();
        //System.out.println(processDetails(processHandle));


        if(processHandle.info().commandLine().toString().contains("tor") && processHandle.info().commandLine().toString().contains("/firefox.real")) {
        //if(processHandle.info().commandLine().toString().contains("torbrowser") && processHandle.info().commandLine().toString().contains("/firefox.real")) {
        //if(processHandle.info().commandLine().toString().contains("tor-browser") && !processHandle.info().commandLine().toString().endsWith("tab")) {
        //if(processHandle.info().commandLine().toString().contains(prefs.get(PrefKeys.TOR_LOCATION.getKey(),PrefKeys.TOR_LOCATION.getDefaultValue()))) {
            torStartedProperty.setValue(true);
            torPid = processHandle.pid();
            continueCompare = false;

            //System.out.println("Tor running, pid: "+processHandle.pid());

        }else{
            torStartedProperty.setValue(false);
            torPid = 0;
            continueCompare = true;

            //System.out.println("Tor not running");
        }

    }

    //Test methods displaying all processes
    private static String processDetails(ProcessHandle process) {
        //return String.format("%8d %8s %10s %26s %-20s %-20s",
        return String.format("%8d %-20s",
                process.pid(),
                //text(process.parent().map(ProcessHandle::pid)),
                //text(process.info().user()),
                //text(process.info().startInstant()),
                text(Optional.ofNullable(process.info().command().orElse("no command found"))),
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
