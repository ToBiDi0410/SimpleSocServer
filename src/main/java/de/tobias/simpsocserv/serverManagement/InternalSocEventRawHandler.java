package de.tobias.simpsocserv.serverManagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.RawSocketEventHandler;
import de.tobias.simpsocserv.external.SimpleSocketEvent;
import de.tobias.simpsocserv.external.SimpleSocketEventHandler;
import io.socket.socketio.server.SocketIoSocket;

import java.util.ArrayList;

public class InternalSocEventRawHandler extends RawSocketEventHandler {

    public static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    public InternalSocEventRawHandler(ArrayList<SimpleSocketEventHandler> pHandlers) {
        super("SIMPLESOCSERVER_EVENT", (socket, eventName, data) -> {
            Logger.info("SOCEVENT", "Received Event");
            if(data.length == 2) {
                String eventDataString = data[0].toString();
                JsonElement eventDataJson;
                try {
                    eventDataJson = gson.fromJson(eventDataString, JsonElement.class);
                } catch (Exception ex) {
                    Logger.warning("SOCEVENT", "Event failed: Data not JSON");
                    return false;
                }

                if(!eventDataJson.isJsonObject()) {
                    Logger.warning("SOCEVENT", "Event failed: JSON is no JSON Object (invalid Format)");
                    return false;
                }

                JsonObject eventDataJsonObject = eventDataJson.getAsJsonObject();

                if(!eventDataJsonObject.has("NAME") || !eventDataJsonObject.has("DATA") || !eventDataJsonObject.has("ID")) {
                    Logger.warning("SOCEVENT", "Event failed: JSON has not all Properties (invalid Format)");
                    return false;
                }

                SimpleSocketEvent event;

                try {
                    event = new SimpleSocketEvent(socket, (SocketIoSocket.ReceivedByLocalAcknowledgementCallback) data[1], eventDataJsonObject.get("ID").getAsInt(), eventDataJsonObject.get("NAME").getAsString(), eventDataJsonObject.get("DATA"));
                } catch (Exception ex) {
                    Logger.warning("SOCEVENT", "Event failed: JSON Types not valid (invalid Format)");
                    return false;
                }

                try {
                    for(SimpleSocketEventHandler handler : pHandlers) {
                        if(handler.getEventName().equalsIgnoreCase(event.getEventName())) {
                            if(handler.getCallback().onEvent(event)) break;
                        }
                    }
                    event.complete("ABORTED_NOT_HANDLED");
                    Logger.info("SOCEVENT", "Event handled");
                } catch (Exception ex) {
                    Logger.info("SOCEVENT", "Event failed: Handler failed");
                    ex.printStackTrace();
                    event.complete("HANDLER_ERROR");
                }
            } else {
                Logger.warning("SOCEVENT", "Event failed: Data Length out of Range");
                return false;
            }
            return false;
        });
    }
}