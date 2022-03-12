package de.tobias.simpsocserv.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.simpsocserv.utils.AESPair;
import io.socket.socketio.server.SocketIoSocket;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class SimpleSocketEvent {

    public static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    SocketIoSocket emitter;
    Integer ID;
    String eventName;
    JsonElement data;

    String state;
    Object completedData;
    boolean completed;

    SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback;
    AESPair encryptPair;

    public SimpleSocketEvent(SocketIoSocket pSoc, SocketIoSocket.ReceivedByLocalAcknowledgementCallback pCallback, Integer pID, String pNAME, JsonElement pData) {
        this.ID = pID;
        this.eventName = pNAME;
        this.state = "RECEIVED";
        this.completed = false;
        this.data = pData;
        this.emitter = pSoc;
        this.callback = pCallback;
    }

    public Integer getEventID() {
        return ID;
    }
    public String getEventName() {
        return eventName;
    }
    public String getState() {
        return state;
    }
    public JsonElement getPayload() {
        return data;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setAESPair(AESPair pAESPair) { encryptPair = pAESPair; }

    public void setState(String pState) {
        this.state = pState;
        reportState();
    }


    //TODO: 11.03.2022 FIX NOT RECOGNIZED COMPLETION IF SHORTLY BEFORE A NEW STATE WAS SENT
    private void reportState() {
        if(this.completed) return;
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("TYPE", "STATEUPDATE");
        payload.put("STATE", state);
        this.sendCallback(gson.toJson(payload));
    }

    public void complete(Object pData) {
        if(this.completed) return;
        this.completed = true;
        this.completedData = pData;

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("TYPE", "COMPLETED");
        payload.put("COMPLETEDATA", completedData);
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
