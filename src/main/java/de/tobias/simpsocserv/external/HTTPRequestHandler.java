package de.tobias.simpsocserv.external;

import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HTTPRequestHandler {

    HttpMethod METHOD;
    String PATH;

    HTTPRequestHandlerCallback handler;

    public HTTPRequestHandler(String PATH, HttpMethod METHOD, HTTPRequestHandlerCallback callback) {
        this.METHOD = METHOD;
        this.PATH = PATH;
        this.handler = callback;
    }

    public HTTPRequestHandlerCallback getHandler() { return handler; }
    public void setCallback(HTTPRequestHandlerCallback callback) {
        handler = callback;
    }

    public interface HTTPRequestHandlerCallback {
        boolean onRequest(HttpServletRequest req, HttpServletResponse res) throws Exception;
    }

    public String getPath() { return PATH; }


}
