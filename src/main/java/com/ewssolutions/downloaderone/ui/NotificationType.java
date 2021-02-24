package com.ewssolutions.downloaderone.ui;

public enum NotificationType {

    Info("icons/info_black.png"),
    Error("icons/error_black.png"),
    Warning("icons/questionAnswer_black.png");

    private final String icon;

    NotificationType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

}
