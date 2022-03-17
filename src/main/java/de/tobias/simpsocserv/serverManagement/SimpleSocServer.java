package de.tobias.simpsocserv.serverManagement;

import de.tobias.simpsocserv.Logger;
import de.tobias.simpsocserv.external.*;
import de.tobias.simpsocserv.utils.AESPair;
import de.tobias.simpsocserv.utils.RSAPair;
import de.tobias.simpsocserv.utils.RequestResponseUtils;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
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
import java.util.HashMap;

public class SimpleSocServer {

    private Server jettyServer;
    private EngineIoServer engineIoServer;
    private SocketIoServer socketIoServer;
    private SocketIoNamespace mainNamespace;

    HashMap<String, AESPair> clientPairs = new HashMap<>();

    //Binding
    String host = "127.0.0.1";
    Integer port = 80;

    //Handlers
    ArrayList<HTTPRequestHandler> httpRequestHandlers = new ArrayList<>();
    ArrayList<RawSocketEventHandler> rawSocketEventHandlers = new ArrayList<>();
    ArrayList<SimpleSocketEventHandler> simpleSocketEventHandlers = new ArrayList<>();
    ArrayList<SimpleSocketRequestHandler> simpleSocketRequestHandlers = new ArrayList<>();
    HashMap<String, DataStorage> socketData = new HashMap<>();

    public SimpleSocServer(String pHost, Integer pPort) {
        this.port = pPort;
        this.host = pHost;
        disableLogging();
    }

    public SimpleSocServer() { disableLogging(); }

    private void disableLogging() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    public void addHTTPRequestHandler(HTTPRequestHandler h) {
        httpRequestHandlers.add(h);
        httpRequestHandlers.sort((o1, o2) -> o2.getPath().length() - o1.getPath().length());
    }

    public void addRawSocketEventHandler(RawSocketEventHandler h) {
        rawSocketEventHandlers.add(h);
    }

    public void addSimpleSocketEventHandler(SimpleSocketEventHandler h) {
        simpleSocketEventHandlers.add(h);
    }

    public void addSimpleSocketRequestHandler(SimpleSocketRequestHandler h) { simpleSocketRequestHandlers.add(h); }

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

                        if(request.getRequestURI().contains("forge.js")) {
                            RequestResponseUtils.sendResource(response, "forge.min.js", getClass());
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
            webSocketUpgradeFilter.addMapping(new ServletPathSpec("/socket.io/*"), (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(engineIoServer));

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        try {
            Logger.info("SERVER", "Registering Socket.IO Namespace and Listeners...");
            mainNamespace = socketIoServer.namespace("/");
            mainNamespace.on("connect", args -> {
                SocketIoSocket socket = (SocketIoSocket) args[0];

                for(RawSocketEventHandler handler : rawSocketEventHandlers) {
                    socket.on(handler.getEventName(), eventArgs -> handler.getCallback().onEvent(socket, handler.getEventName(), socketData.get(socket.getId()), eventArgs));
                }

                socketData.put(socket.getId(), new DataStorage());

                Logger.info("SERVER", "Socket connected and setup: " + socket.getId());
            });

            addSimpleSocketRequestHandler(new InternalEncryptionSocketRequestHandler(new RSAPair(), clientPairs));
            addRawSocketEventHandler(new InternalSocEventRawHandler(this));
            addRawSocketEventHandler(new InternalRequestRawHandler(this));
            addHTTPRequestHandler(new ClassResourceHTTPRequestHandler("/error", SimpleSocServer.class, "error.html"));

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
