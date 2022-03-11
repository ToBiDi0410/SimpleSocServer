const SimpleSocServerConnector = {
    SOCKET: null,
    connect() {
        console.debug("[SimpleSocServ] Connecting to Server...");
    }
}

(async function() {
    SimpleSocServerConnector.io = await
    import ("/socket.io/socket.io.js");
    console.log("TEST");
})();