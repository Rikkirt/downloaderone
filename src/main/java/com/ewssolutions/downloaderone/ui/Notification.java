package com.ewssolutions.downloaderone.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;

public class Notification {

    private Alert alert;
    private final String icon;
    private long duration = 5;

    public Notification(NotificationType type){
        this.icon = type.getIcon();
    }

    public Notification setText(String text){

        String title = "DownloaderOne information";

        try {

            alert = new Alert(Alert.AlertType.NONE,text, null);
            alert.setHeaderText(title);
            alert.initStyle(StageStyle.TRANSPARENT);
            // set result to allow programmatic closing of alert
            alert.setResult(ButtonType.CLOSE);
            alert.setGraphic(new ImageView(icon));
            alert.getDialogPane().setOnMouseClicked(mouseEvent -> {
                alert.close();
            });

            alert.setOnShowing(dialogEvent -> {
                //System.out.println("Dialog showing");
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(duration * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Runnable action = alert::close;
                    Platform.runLater(action);

                });

                t.setName("Notification Thread");
                t.setDaemon(true);
                t.start();

            });



        }catch (NullPointerException e){
            //do nothing?
        }

        return this;
    }

    /*
        @duration: Max 10 seconds
     */
    public void show(long duration){
        this.duration = Math.min(duration, 10);
        alert.show();
    }

    public void hide(){
        alert.hide();
    }


}

