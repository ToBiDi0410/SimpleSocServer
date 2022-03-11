package de.tobias.simpsocserv.serverManagement;

import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.HTTPRequestHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;

public class InternalHTTPRequestHandler extends HttpServlet {

    ArrayList<HTTPRequestHandler> httpRequestHandlers;

    public InternalHTTPRequestHandler(ArrayList<HTTPRequestHandler> pHttpRequestHandlers) {
        httpRequestHandlers = pHttpRequestHandlers;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        Logger.info("INTHANDLER", "Request for: " + req.getRequestURI());
        for(HTTPRequestHandler handler : httpRequestHandlers) {
            // TODO: 11.03.2022 Add Method Check
            if (fitsTemplate(req.getRequestURI(), handler.getPath()) || req.getRequestURI().equalsIgnoreCase(handler.getPath().replace("*", ""))) {
                try {
                    boolean handled = handler.getHandler().onRequest(req, res);
                    if(handled) {
                        Logger.info("INTHANDLER", "Handler done: " + handler.getPath());
                        return;
                    } else {
                        Logger.warning("INTHANDLER", "Handler skipped: " + handler.getPath());
                    }
                } catch (Exception ex) {
                    Logger.error("INTHANDLER", "Handler failed: " + handler.getPath());
                    ex.printStackTrace();
                    return;
                }
            }
        }
    }

    private boolean fitsTemplate(String path, String template) {
        return FileSystems.getDefault().getPathMatcher("glob:" + template).matches(Paths.get(path));
    }
}
