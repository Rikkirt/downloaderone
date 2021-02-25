package com.ewssolutions.downloaderone.ui;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class Notification  {

    private long duration = 5;
    private final Notifications mNotifications;

    public Notification(Stage owner, NotificationType type){

        mNotifications = Notifications.create()
        .title(type.name().concat(" DownloaderOne"))
        .graphic(new ImageView(type.getIcon()))
        .owner(owner)
        .position(Pos.TOP_RIGHT);

    }

    public Notification setText(String text) {
        mNotifications.text(text);
        return this;
    }

    /*
        @duration: Max 10 seconds
     */
    public void show(long duration){
        this.duration = Math.min(duration, 10);
        mNotifications.hideAfter(Duration.seconds(this.duration));
        mNotifications.show();
    }
}

