package com.genuitec.piplug.client;

import java.util.List;

public interface IPiPlugClientListener {

    void handleEvents(List<BundleEvent> events);

}
