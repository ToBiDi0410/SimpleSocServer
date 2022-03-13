package de.tobias.simpsocserv.external;

import de.tobias.simpsocserv.serverManagement.DataStorage;

public class SimpleSocketEventHandler {

    String eventName;
    SimpleSocketEventCallback callback;

    public SimpleSocketEventHandler(String pEventName, SimpleSocketEventCallback pCallback) {
        this.eventName = pEventName;
        this.callback = pCallback;
    }

    public String getEventName() {
        return eventName;
    }

    public SimpleSocketEventCallback getCallback() {
        return callback;
    }

    public interface SimpleSocketEventCallback {
        boolean onEvent(SimpleSocketEvent event, DataStorage socketData);
    }
}
