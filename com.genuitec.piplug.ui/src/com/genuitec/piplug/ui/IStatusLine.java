package com.genuitec.piplug.ui;

public interface IStatusLine {
    /**
     * Can be called off of the UI thread, implementors will switch to the UI
     * thread to display the message
     * 
     * @param message
     */
    void updateMessage(String message);
}
