package de.tobias.simpsocserv.serverManagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.RawSocketEventHandler;
import de.tobias.simpsocserv.external.SimpleSocketEventHandler;
import de.tobias.simpsocserv.external.SimpleSocketRequest;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import io.socket.socketio.server.SocketIoSocket;

import java.util.ArrayList;
import java.util.Date;

public class InternalRequestEventRawHandler extends RawSocketEventHandler {

    public static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    public InternalRequestEventRawHandler(ArrayList<SimpleSocketRequestHandler> pHandlers) {
        super("SIMPLESOCSERVER_REQUEST", (socket, eventName, data) -> {
            Logger.info("SOCREQ", "Received Request");

            if(data.length >= 2) {
                //Data[0] = PAYLOAD
                //Data[1] = Callback

                /*if(data.length == 3) {
                    //Data[2] = Encryption Data
                }*/

                String payloadString = data[0].toString();
                JsonElement requestDataJson;

                try {
                    requestDataJson = gson.fromJson(payloadString, JsonElement.class);
                } catch (Exception ex) {
                    Logger.warning("SOCREQ", "Request failed: Data not JSON");
                    return false;
                }

                if(!requestDataJson.isJsonObject()) {
                    Logger.warning("SOCREQ", "Request failed: JSON is no JSON Object (invalid Format)");
                    return false;
                }

                JsonObject requestDataJsonObject = requestDataJson.getAsJsonObject();

                if(!requestDataJsonObject.has("ID") || !requestDataJsonObject.has("NAME") || !requestDataJsonObject.has("SUBFIELDS") || !requestDataJsonObject.has("PAYLOAD") || !requestDataJsonObject.has("SENT") || !requestDataJsonObject.has("METHOD")) {
                    Logger.warning("SOCREQ", "Request failed: JSON has not all Properties (invalid Format)");
                    return false;
                }

                SimpleSocketRequest request;
                try {
                    request = new SimpleSocketRequest(socket, (SocketIoSocket.ReceivedByLocalAcknowledgementCallback) data[1], requestDataJsonObject.get("ID").getAsInt(), requestDataJsonObject.get("NAME").getAsString(), requestDataJsonObject.get("SUBFIELDS").getAsString().split(","), requestDataJsonObject.get("PAYLOAD"), new Date(requestDataJsonObject.get("SENT").getAsLong()), requestDataJsonObject.get("METHOD").getAsString());
                } catch (Exception ex) {
                    Logger.warning("SOCREQ", "Request failed: JSON Types not valid (invalid Format)");
                    return false;
                }

                try {
                    for(SimpleSocketRequestHandler handler : pHandlers) {
                        if(handler.getName().equalsIgnoreCase(request.getName()) && (handler.getMethod().equalsIgnoreCase(request.getMethod()) || handler.getMethod().equalsIgnoreCase("*"))) {
                            if(handler.getCallback().onRequest(request)) break;
                        }
                    }
                    request.sendResponse(500, "ABORTED_NOT_HANDLED");
                    Logger.info("SOCREQ", "Request handled");
                } catch (Exception ex) {
                    Logger.info("SOCREQ", "Request failed: Handler failed");
                    ex.printStackTrace();
                    request.sendResponse(500, "HANDLER_ERROR");
                }
            } else {
                Logger.warning("SOCEVENT", "Request failed: Data Length out of Range");
                return false;
            }
            return false;
        });
    }
}
