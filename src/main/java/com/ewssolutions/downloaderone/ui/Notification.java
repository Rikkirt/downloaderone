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

package com.ewssolutions.downloaderone.ui;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class Notification  {

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
        long dur = Math.min(duration, 10);
        mNotifications.hideAfter(Duration.seconds(dur));
        mNotifications.show();
    }
}

