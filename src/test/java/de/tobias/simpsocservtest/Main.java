package de.tobias.simpsocservtest;

import de.tobias.simpsocserv.external.HTTPRequestHandler;
import de.tobias.simpsocserv.external.StaticHTTPRequestHandler;
import de.tobias.simpsocserv.serverManagement.SimpleSocServer;
import org.eclipse.jetty.http.HttpMethod;

import java.io.File;

public class Main {

    static SimpleSocServer sc = new SimpleSocServer();

    public static void main(String[] args) {
        sc.start();

        HTTPRequestHandler reqHand = new HTTPRequestHandler("/staticTest/testno", HttpMethod.GET);
        reqHand.setCallback((req, res) -> {
                res.setStatus(200);
                res.setContentType("text/javascript");
                res.getWriter().write("NO!");
                return true;
        });

        sc.addHTTPRequestHandler(reqHand);
        sc.addHTTPRequestHandler(new StaticHTTPRequestHandler("/staticTest/*", new File("./testweb/")));
    }
}
