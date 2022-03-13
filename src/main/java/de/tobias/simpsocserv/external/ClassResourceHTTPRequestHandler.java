package de.tobias.simpsocserv.external;

import de.tobias.simpsocserv.utils.RequestResponseUtils;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClassResourceHTTPRequestHandler extends HTTPRequestHandler{

    String basePath;
    Class baseClass;

    public ClassResourceHTTPRequestHandler(String path, Class baseClass, String basePath) {
        super(path, HttpMethod.GET, null);
        this.setCallback(this::onRequest);
        this.baseClass = baseClass;
        this.basePath = basePath;
    }

    private boolean onRequest(HttpServletRequest req, HttpServletResponse res) {
        String[] pathParts = (req.getRequestURI() + " ").split("/");
        String filePath = pathParts[pathParts.length - 1];
        return RequestResponseUtils.sendResource(res, basePath + filePath, baseClass);
    }
}
