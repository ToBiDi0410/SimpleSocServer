package de.tobias.simpsocserv.serverManagement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import de.tobias.simpsocserv.utils.AESPair;
import de.tobias.simpsocserv.utils.RSAPair;

import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.HashMap;

public class InternalEncryptionSocketRequestHandler extends SimpleSocketRequestHandler {


    public InternalEncryptionSocketRequestHandler(RSAPair pServerPair, HashMap<String, AESPair> clientPairs) {
        super("SIMPLESOCSERVER_ENCRYPT", "GET", null);
        this.setCallback(request -> {
            JsonElement json = request.getData();

            if(!json.isJsonObject()) {
                request.sendResponse(HttpServletResponse.SC_BAD_REQUEST, "JSON_IS_NOT_JSON_OBJECT");
                return true;
            }

            JsonObject jsonObject = json.getAsJsonObject();
            if(!jsonObject.has("STEP")) {
                request.sendResponse(HttpServletResponse.SC_BAD_REQUEST, "MISSING_STEP");
                return true;
            }

            String step = jsonObject.get("STEP").getAsString();

            if(step.equalsIgnoreCase("GET_SERVER_PUBLICKEY")) {
                request.sendResponse(HttpServletResponse.SC_OK, Base64.getEncoder().encodeToString(pServerPair.publicKey));
                return true;
            }

            if(step.equalsIgnoreCase("SET_AES_KEY")) {
                if(!jsonObject.has("AESENCRYPTED") || !jsonObject.has("AESIV")) {
                    request.sendResponse(HttpServletResponse.SC_BAD_REQUEST, "MISSING_AES_KEYDATA");
                    return true;
                }

                byte[] aesEncrpyted = Base64.getDecoder().decode(jsonObject.get("AESENCRYPTED").getAsString());
                byte[] aesIV = Base64.getDecoder().decode(jsonObject.get("AESIV").getAsString());

                clientPairs.put(request.getSocket().getId(), new AESPair(pServerPair.decrypt(aesEncrpyted), aesIV));
                request.sendResponse(HttpServletResponse.SC_OK, "PAIR_ACCEPTED");
                return true;
            }


            request.sendResponse(HttpServletResponse.SC_BAD_REQUEST, "UNKNOWN_STEP");
            return true;
        });
    }
}
