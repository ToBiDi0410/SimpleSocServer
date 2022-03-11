package de.tobias.simpsocserv.external;

import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HTTPRequestHandler {

    HttpMethod METHOD;
    String PATH;

    InternalHTTPRequestHandler handler;

    public HTTPRequestHandler(String PATH, HttpMethod METHOD) {
        this.METHOD = METHOD;
        this.PATH = PATH;
    }

    public InternalHTTPRequestHandler getHandler() { return handler; }
    public void setCallback(InternalHTTPRequestHandler callback) {
        handler = callback;
    }

    public interface InternalHTTPRequestHandler {
        boolean onRequest(HttpServletRequest req, HttpServletResponse res) throws Exception;
    }

    public String getPath() { return PATH; }


}
