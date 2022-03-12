package de.tobias.simpsocserv.external;

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

    public String getMethod() {
        return method;
    }

    public SimpleSocketRequestHandlerCallback getCallback() {
        return callback;
    }

    public interface SimpleSocketRequestHandlerCallback {
        boolean onRequest(SimpleSocketRequest request);
    }
}
