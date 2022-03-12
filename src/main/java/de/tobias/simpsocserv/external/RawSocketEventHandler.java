package de.tobias.simpsocserv.external;

import io.socket.socketio.server.SocketIoSocket;

public class RawSocketEventHandler {

    String NAME;
    SocketEventHandlerCallback CALLBACK;

    public RawSocketEventHandler(String pName, SocketEventHandlerCallback pCallback) {
        this.NAME = pName;
        this.CALLBACK = pCallback;
    }

    public interface SocketEventHandlerCallback {
        boolean onEvent(SocketIoSocket socket, String eventName, Object... data);
    }

    public String getEventName() {
        return NAME;
    }

    public SocketEventHandlerCallback getCallback() {
        return CALLBACK;
    }
}
