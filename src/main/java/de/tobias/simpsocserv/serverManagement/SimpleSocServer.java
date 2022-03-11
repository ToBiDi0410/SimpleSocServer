package de.tobias.simpsocserv.serverManagement;

import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.HTTPRequestHandler;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class SimpleSocServer {

    private Server jettyServer;
    private EngineIoServer engineIoServer;
    private SocketIoServer socketIoServer;

    //Binding
    String host = "127.0.0.1";
    Integer port = 80;

    //Handlers
    ArrayList<HTTPRequestHandler> httpRequestHandlers = new ArrayList<>();

    public SimpleSocServer() {
        disableLogging();
    }

    private void disableLogging() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    public void addHTTPRequestHandler(HTTPRequestHandler h) {
        httpRequestHandlers.add(h);
        httpRequestHandlers.sort((o1, o2) -> o2.getPath().length() - o1.getPath().length());
    }

    public boolean start() {
        try {
            Logger.info("SERVER", "Preparing new Server with Binding: " + host + ":" + port);
            jettyServer = new Server();
            ServerConnector connector = new ServerConnector(jettyServer);
            connector.setHost(host);
            connector.setPort(port);
            jettyServer.setConnectors(new Connector[] { connector });
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        try {
            Logger.info("SERVER", "Registering our own handlers...");
            ServletHolder holder = new ServletHolder(new InternalHTTPRequestHandler(httpRequestHandlers));
            context.addServlet(holder, "/*");
            jettyServer.setHandler(context);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        try {
            Logger.info("SERVER", "Registering Socket.IO Handlers...");
            engineIoServer = new EngineIoServer();
            socketIoServer = new SocketIoServer(engineIoServer);

            context.addServlet(new ServletHolder(new HttpServlet() {
                @Override
                protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                    try {
                        if(request.getRequestURI().contains("socket.io.js")) {
                            RequestResponseUtils.sendResource(response, "socket.io.esm.min.js", getClass());
                            return;
                        }

                        if(request.getRequestURI().contains("socserv.js")) {
                            RequestResponseUtils.sendResource(response, "simpleSocServer.js", getClass());
                            return;
                        }

                        if(request.getRequestURI().contains(".map")) {
                            response.sendError(404);
                            return;
                        }

                        engineIoServer.handleRequest(new HttpServletRequestWrapper(request) {
                            @Override
                            public boolean isAsyncSupported() {
                                return true;
                            }
                        }, response);

                    } catch(Exception ex) {
                        response.sendError(500);
                        Logger.warning("SERVER", "Failed at handling Socket.IO Request:");
                        ex.printStackTrace();
                    }
                }
            }), "/socket.io/*");

            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(context);
            webSocketUpgradeFilter.addMapping(
                    new ServletPathSpec("/socket.io/*"),
                    (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(engineIoServer)
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        try {
            Logger.info("SERVER", "Starting prepared Server...");
            jettyServer.start();
            Logger.info("SERVER", "Server is now online!");
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }
}
