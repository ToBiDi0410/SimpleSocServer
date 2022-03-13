package de.tobias.simpsocserv.external;

import de.tobias.simpsocserv.serverManagement.DataStorage;

public class SimpleSocketRequestHandler {

    String name;
    String method;

    SimpleSocketRequestHandlerCallback callback;

    public SimpleSocketRequestHandler(String pName, String pMethod, SimpleSocketRequestHandlerCallback pCallback) {
        this.name = pName;
        this.method = pMethod;
        this.callback = pCallback;
    }

    public String getName() {
        return name;
    }

    public void setCallback(SimpleSocketRequestHandlerCallback h) {
        this.callback = h;
    }

    public String getMethod() {
        return method;
    }

    public SimpleSocketRequestHandlerCallback getCallback() {
        return callback;
    }

    public interface SimpleSocketRequestHandlerCallback {
        boolean onRequest(SimpleSocketRequest request, DataStorage socketData) throws Exception;
    }
}
