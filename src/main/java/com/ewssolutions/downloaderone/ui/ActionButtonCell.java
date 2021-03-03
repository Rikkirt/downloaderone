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

import com.ewssolutions.downloaderone.DownloadItemTask;
import com.ewssolutions.downloaderone.DownloaderControler;
import com.sapher.youtubedl.YoutubeDLException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ActionButtonCell extends TableCell<DownloadItemTask, Boolean> {

    // pads and centers the add button in the cell.
    private final StackPane paddedButton2 = new StackPane();
    private final StackPane paddedButton3 = new StackPane();
    private final StackPane paddedButton4 = new StackPane();

    // records the y pos of the last button press so that the add person dialog can be shown next to the cell.
    final DoubleProperty buttonY = new SimpleDoubleProperty();


    private HBox hbox =  new HBox(paddedButton2, paddedButton3,paddedButton4);

    private javafx.scene.control.TableView table;
    /**
     * AddActionCell constructor
     * @param stage the stage in which the table is placed.
     * @param myDownloadControler the table to which a new downloadButton can be added.
     */
    public ActionButtonCell(final Stage stage, final DownloaderControler myDownloadControler) {

        table = myDownloadControler.downloadTable;

        paddedButton2.setPadding(new Insets(3));
        /* A table cell containing buttons */
        Button clearButton = new Button("Clear");
        paddedButton2.getChildren().add(clearButton);
        paddedButton3.setPadding(new Insets(3));
        Button copyButton = new Button("Copy");
        paddedButton3.getChildren().add(copyButton);
        paddedButton4.setPadding(new Insets(3));
        Button restartButton = new Button("Restart");
        paddedButton4.getChildren().add(restartButton);
        clearButton.setTooltip(new Tooltip("Clear this downloadButton from the table"));
        copyButton.setTooltip(new Tooltip("Copies the urlColumn to the clipboard"));
        restartButton.setTooltip(new Tooltip("Restarts the downloadButton for this urlColumn"));


        clearButton.setOnAction(actionEvent -> table.getItems().remove(getTableRow().getItem()));

        copyButton.setOnAction(actionEvent -> {

            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();

            DownloadItemTask Item = (DownloadItemTask) getTableRow().getItem();

            content.putString(Item.getUrlItem());
            clipboard.setContent(content);
        });


        restartButton.setOnAction(actionEvent -> {
            DownloadItemTask Item = (DownloadItemTask) getTableRow().getItem();


            myDownloadControler.prepareDownloadItemTask(Item.getUrlItem(),Item.getReferenceItem(),"");

            try {

                myDownloadControler.executeDownloads();

            } catch (YoutubeDLException e) {
                e.printStackTrace();
            }

            table.getItems().remove(getTableRow().getItem());

            getScene().setCursor(Cursor.WAIT);

        });

    }

    /** places buttons in the row only if the row is not empty. */
    @Override protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {

            hbox.alignmentProperty().set(Pos.CENTER);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(hbox);
        }else{

            if(item==null){
                setGraphic(null);
            }
        }

    }


}

