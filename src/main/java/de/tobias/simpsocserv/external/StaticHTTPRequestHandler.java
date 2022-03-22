package de.tobias.simpsocserv.external;

import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.utils.RequestResponseUtils;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class StaticHTTPRequestHandler extends HTTPRequestHandler {

    File BASEDIR;

    public StaticHTTPRequestHandler(String path, File folder) {
        super(path, HttpMethod.GET, null);
        BASEDIR = folder;
        this.setCallback(this::onRequest);
    }

    private boolean onRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
        if(BASEDIR.exists()) {
            File requestedFile = new File(BASEDIR, (String) req.getAttribute("PATHNOHANDLER"));
            if(requestedFile.exists()) {
                if(requestedFile.isDirectory()) {
                    res.sendRedirect("index.html");
                    return true;
                }

                if(requestedFile.canRead()) {
                    RequestResponseUtils.sendFile(res, requestedFile);
                    return true;
                } else {
                    RequestResponseUtils.redirectToError(res, 500, "Invalid Server Configuration: No read access");
                }
            } else {
                RequestResponseUtils.redirectToError(res, 404, "File does not exist");
            }
        } else {
            RequestResponseUtils.redirectToError(res, 500, "Invalid Server Configuration: Web Directory not found");
        }
        return true;
    }
}
