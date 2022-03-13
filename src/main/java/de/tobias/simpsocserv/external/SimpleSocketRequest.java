package de.tobias.simpsocserv.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import de.tobias.simpsocserv.serverManagement.DataStorage;
import de.tobias.simpsocserv.utils.AESPair;
import io.socket.socketio.server.SocketIoSocket;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    AESPair encryptPair;

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
    public void setAESPair(AESPair pAESPair) {
        encryptPair = pAESPair;
    }

    public void setDataStorage(DataStorage store) {

    }

    public void sendResponse(Integer code, Object data) {
        if(responded) return;
        respondedDate = new Date();

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("TYPE", "REQUEST_FINISHED");
        payload.put("ID", ID);
        payload.put("CODE", code);
        payload.put("DATA", data);
        payload.put("RESPONDED", respondedDate.getTime());

        this.sendCallback(gson.toJson(payload));
    }

    private void sendCallback(String s) {
        if(encryptPair != null) {
            try {
                callback.sendAcknowledgement(Base64.getEncoder().encodeToString(encryptPair.encrypt(s.getBytes(StandardCharsets.UTF_8))));
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        callback.sendAcknowledgement(s);
    }
}

