package de.tobias.simpsocserv.serverManagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.RawSocketEventHandler;
import de.tobias.simpsocserv.external.SimpleSocketRequest;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import de.tobias.simpsocserv.utils.AESPair;
import io.socket.socketio.server.SocketIoSocket;

import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;

public class InternalRequestRawHandler extends RawSocketEventHandler {

    public static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    public InternalRequestRawHandler(SimpleSocServer srv) {
        super("SIMPLESOCSERVER_REQUEST", (socket, eventName, socketData, data) -> {
            Logger.info("SOCREQ", "Received Request");

            if(data.length >= 2) {
                //Data[0] = PAYLOAD
                //Data[1] = Callback
                //Data[2] = ANY (INDICATES ENCPRYTION)

                AESPair clientPair = srv.clientPairs.get(socket.getId());
                if(data.length == 3) {
                    try {
                        byte[] encryptedPayloadBytes = Base64.getDecoder().decode(data[0].toString());
                        data[0] = new String(clientPair.decrypt(encryptedPayloadBytes));
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }

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
                    request = new SimpleSocketRequest(socket, (SocketIoSocket.ReceivedByLocalAcknowledgementCallback) data[data.length - 1], requestDataJsonObject.get("ID").getAsInt(), requestDataJsonObject.get("NAME").getAsString(), requestDataJsonObject.get("SUBFIELDS").getAsString().split(","), requestDataJsonObject.get("PAYLOAD"), new Date(requestDataJsonObject.get("SENT").getAsLong()), requestDataJsonObject.get("METHOD").getAsString());
                    if(data.length == 3) request.setAESPair(clientPair);
                } catch (Exception ex) {
                    Logger.warning("SOCREQ", "Request failed: JSON Types not valid (invalid Format)");
                    return false;
                }

                try {
                    for(SimpleSocketRequestHandler handler : srv.simpleSocketRequestHandlers) {
                        if(handler.getName().equalsIgnoreCase(request.getName()) && (handler.getMethod().equalsIgnoreCase(request.getMethod()) || handler.getMethod().equalsIgnoreCase("*"))) {
                            if(handler.getCallback().onRequest(request, srv.socketData.get(socket.getId()))) break;
                        }
                    }

                    request.sendResponse(500, "ABORTED_NOT_HANDLED");
                    Logger.info("SOCREQ", "Request handled");
                } catch (Exception ex) {
                    Logger.info("SOCREQ", "Request failed: Handler failed");
                    ex.printStackTrace();
                    request.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "HANDLER_ERROR");
                }
            } else {
                Logger.warning("SOCEVENT", "Request failed: Data Length out of Range");
                return false;
            }
            return false;
        });
    }
}
