package de.tobias.simpsocserv.utils;

import de.tobias.simpsocserv.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RequestResponseUtils {

    public static boolean sendFile(HttpServletResponse res, File f) {
        try {
            res.setStatus(200);
            InputStream in = new FileInputStream(f);
            OutputStream out = res.getOutputStream();
            IOUtils.copy(in, out);
            in.close();
            out.close();
            return true;
        } catch (Exception ex) {
            Logger.error("UTILS", "Failed to Respond with File: ");
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean sendResource(HttpServletResponse res, String path, Class classname) {
        try {
            InputStream in = classname.getClassLoader().getResourceAsStream(path);
            if(in != null) {
                res.setStatus(200);
                String fileName = path.split("/")[path.split("/").length - 1];
                String mimeType = getMimeTypeByFilename(fileName);
                res.setContentType(mimeType);
                IOUtils.copy(in, res.getOutputStream());
                in.close();
            } else {
                res.setStatus(404);
                res.getWriter().write("Class Resource not found");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getMimeTypeByFilename(String path) {
        String extension = FilenameUtils.getExtension(path);

        if(extension.equalsIgnoreCase("js")) return "application/javascript";
        if(extension.equalsIgnoreCase("json")) return "application/json";
        if(extension.equalsIgnoreCase("xml")) return "application/xml";
        if(extension.equalsIgnoreCase("zip")) return "application/zip";
        if(extension.equalsIgnoreCase("pdf")) return "application/pdf";

        if(extension.equalsIgnoreCase("css")) return "text/css";
        if(extension.equalsIgnoreCase("html")) return "text/html";
        if(extension.equalsIgnoreCase("txt")) return "text/plain";

        if(extension.equalsIgnoreCase("mp3")) return "audio/mpeg";
        if(extension.equalsIgnoreCase("mp4")) return "audio/mp4";
        if(extension.equalsIgnoreCase("ogg")) return "audio/ogg";
        if(extension.equalsIgnoreCase("wav")) return "audio/wav";

        return "application/octet-stream";
    }
}
