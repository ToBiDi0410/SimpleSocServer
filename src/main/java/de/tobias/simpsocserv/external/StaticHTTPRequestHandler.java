package de.tobias.simpsocserv.external;

import de.tobias.simpsocserv.serverManagement.RequestResponseUtils;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class StaticHTTPRequestHandler extends HTTPRequestHandler {

    File BASEDIR;

    public StaticHTTPRequestHandler(String path, File folder) {
        super(path, HttpMethod.GET);
        BASEDIR = folder;
        this.setCallback(this::onRequest);
    }

    private boolean onRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
        if(BASEDIR.exists()) {
            String[] pathParts = (req.getRequestURI() + " ").split("/");
            String filePath = pathParts[pathParts.length - 1];
            File requestedFile = new File(BASEDIR, filePath);

            if(requestedFile.exists()) {
                if(requestedFile.isDirectory()) {
                    res.sendRedirect("index.html");
                    return true;
                }

                if(requestedFile.canRead()) {
                    RequestResponseUtils.sendFile(res, requestedFile);
                } else {
                    res.setStatus(404);
                    res.setContentType("text/plain");
                    res.getWriter().write("Invalid Server Configuration: No read access");
                }
            } else {
                res.setStatus(500);
                res.setContentType("text/plain");
                res.getWriter().write("File not found");
            }
        } else {
            res.setStatus(500);
            res.setContentType("text/plain");
            res.getWriter().write("Invalid Server Configuration: Web Directory not found");
            System.out.println(BASEDIR.getAbsolutePath());
        }
        return true;
    }
}
