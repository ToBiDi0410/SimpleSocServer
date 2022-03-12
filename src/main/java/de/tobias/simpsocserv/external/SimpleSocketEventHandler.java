package de.tobias.simpsocserv.external;

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
        boolean onEvent(SimpleSocketEvent event);
    }
}
