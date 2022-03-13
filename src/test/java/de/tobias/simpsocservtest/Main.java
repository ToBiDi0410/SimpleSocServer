package de.tobias.simpsocservtest;

import de.tobias.simpsocserv.external.*;
import de.tobias.simpsocserv.serverManagement.SimpleSocServer;
import org.eclipse.jetty.http.HttpMethod;

import java.io.File;

public class Main {

    static SimpleSocServer sc = new SimpleSocServer();

    public static void main(String[] args) {
        HTTPRequestHandler reqHand = new HTTPRequestHandler("/staticTest/testno", HttpMethod.GET, null);
        reqHand.setCallback((req, res) -> {
                res.setStatus(200);
                res.setContentType("text/javascript");
                res.getWriter().write("THIS IS A SUBHANDLER IN AN OTHER HANDLER THAT IS PRIOTIZED BEFORE THE OTHER HANDLER");
                return true;
        });
        sc.addHTTPRequestHandler(reqHand);
        sc.addHTTPRequestHandler(new StaticHTTPRequestHandler("/staticTest/*", new File("./testweb/"))); //STATIC TEST HANDLER

        RawSocketEventHandler rawSocHand = new RawSocketEventHandler("TESTEVENT", (socket, eventName, socketData, data) -> {
            System.out.println("RAW TEST EVENT");
            return false;
        });
        sc.addRawSocketEventHandler(rawSocHand);

        sc.addSimpleSocketEventHandler(new SimpleSocketEventHandler("DIED", (event, socketData) -> {
            System.out.println("DIED TEST EVENT");
            event.complete("RECIEVED");
            return false;
        }));

        sc.addSimpleSocketRequestHandler(new SimpleSocketRequestHandler("test", "GET", (request, socketData) -> {
            System.out.println(request.getName());
            System.out.println(request.getData());
            request.sendResponse(200, "YEP THIS IS GREAT");
            return true;
        }));

        sc.addHTTPRequestHandler(new ClassResourceHTTPRequestHandler("/classres/*", Main.class, ""));

        sc.start();
    }
}
