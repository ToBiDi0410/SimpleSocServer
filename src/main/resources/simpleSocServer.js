const SimpleSocServerConnector = {
    socketIOLibrary: null,
    socket: null,
    async timer(time) {
        return new Promise((resolve, reject) => setTimeout(resolve, time));
    }
};

(async function() {
    var blockDiv = document.createElement("div");
    blockDiv.style.height = "100vh";
    blockDiv.style.width = "100vw";
    blockDiv.style.position = "absolute";
    blockDiv.style.top = 0;
    blockDiv.style.left = 0;
    blockDiv.style.backgroundColor = "gray";
    blockDiv.style.textAlign = "center";
    blockDiv.style.zIndex = 9999;
    blockDiv.innerText = "Connecting to the Server...";

    var appendedBlocker = document.body.appendChild(blockDiv);

    SimpleSocServerConnector.socketIOLibrary = await
    import ("/socket.io/socket.io.js");

    SimpleSocServerConnector.socket = SimpleSocServerConnector.socketIOLibrary.io();

    appendedBlocker.remove();
})();

class SocketEvent {

    ID = Math.random() * 100000000000000000;
    NAME;
    DATA;

    SENT = false;
    COMPLETED = false;
    STATE = "UNKNOWN";
    COMPLETED_DATA = null;

    constructor(name, payload) {
        this.NAME = name;
        this.DATA = payload;
    }

    send() {
        if (this.COMPLETED) return;
        SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_EVENT", JSON.stringify({ ID: this.ID, NAME: this.NAME, DATA: this.DATA }), async(stateData) => {
            stateData = JSON.parse(stateData);
            if (stateData.TYPE != null) {
                if (stateData.TYPE == "COMPLETED") {
                    this.COMPLETED_DATA = stateData.COMPLETEDATA;
                    this.COMPLETED = true;
                    this.STATE = "FINISHED";
                    return;
                }

                if (stateData.TYPE == "STATEUPDATE") {
                    if (this.COMPLETED) return;
                    this.COMPLETED = false;
                    this.STATE = stateData.STATE;
                    return;
                }
            }

            console.warn("[SimpleSocServ] Recieved unknown Event State Update");
        });
    }

    async waitForCompletion() {
        while (!this.COMPLETED) {
            await SimpleSocServerConnector.timer(250);
        }
    }
}

class SocketRequest {
    ID = Math.random() * 100000000000000000;

    NAME = "NULL";
    SUBFIELDS = [];
    PAYLOAD = null;

    sent = false;
    sentDate;
    respondedDate;

    responseRecieved = false;
    responseCode = -1;
    responseData;

    constructor(name, subfields, payload) {
        this.NAME = name;
        this.SUBFIELDS = subfields;
        this.PAYLOAD = this.PAYLOAD;
    }

    send() {
        if (this.sent) return;

        this.sentDate = new Date();
        SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_REQUEST", JSON.stringify({ ID: this.ID, NAME: this.NAME, SUBFIELDS: this.SUBFIELDS.join(","), PAYLOAD: this.PAYLOAD, SENT: this.sentDate.getDate(), METHOD: "GET" }), async(responseData) => {
            responseData = JSON.parse(responseData);
            this.responseCode = responseData.CODE;
            this.responseData = responseData.DATA;
            this.respondedDate = new Date(responseData.RESPONDED);
            this.responseRecieved = true;
        });

        this.sent = true;
    }

    async waitForResponse() {
        while (!this.responseRecieved) {
            await SimpleSocServerConnector.timer(250);
        }
    }
}