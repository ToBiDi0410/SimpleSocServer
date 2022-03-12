package de.tobias.simpsocserv.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.socket.socketio.server.SocketIoSocket;

import java.util.Date;
import java.util.HashMap;

public class SimpleSocketRequest {

    public static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    SocketIoSocket emitter;
    SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback;
    Integer ID;

    String name;
    String[] subfields;
    JsonElement payload;
    String method;

    Date sentDate;
    Date respondedDate;

    boolean responded = false;
    Integer responseCode = 500;
    JsonElement responseData = null;

    public SimpleSocketRequest(SocketIoSocket pSoc, SocketIoSocket.ReceivedByLocalAcknowledgementCallback pCallback, Integer pID, String pName, String[] pSubFields, JsonElement pData, Date pSentDate, String pMethod) {
        this.emitter = pSoc;
        this.callback = pCallback;
        this.ID = pID;
        this.name = pName;
        this.subfields = pSubFields;
        this.payload = pData;
        this.sentDate = pSentDate;
        this.method = pMethod;
    }

    public void setAESRSAEncryptionPair() {

    }

    public JsonElement getData() { return payload; }
    public SocketIoSocket getSocket() { return emitter; }
    public String getName() { return name; }
    public String getMethod() { return method; }
    public Date getSentData() { return sentDate; }
    public Date getRespondedDate() { return respondedDate; }

    public void sendResponse(Integer code, Object data) {
        if(responded) return;
        respondedDate = new Date();

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("TYPE", "REQUEST_FINISHED");
        payload.put("ID", ID);
        payload.put("CODE", code);
        payload.put("DATA", data);
        payload.put("RESPONDED", respondedDate.getTime());

        callback.sendAcknowledgement(gson.toJson(payload));
        responded = true;
    }
}

