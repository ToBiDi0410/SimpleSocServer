const SimpleSocServerConnector = {
    socketIOLibrary: null,
    forgeLibrary: null,
    socket: null,
    async timer(time) {
        return new Promise((resolve, reject) => setTimeout(resolve, time));
    },
    defaultEncrypt: false,
    sharedRSAKey: { secretKey: null, iv: null }
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

    SimpleSocServerConnector.forgeLibrary = forge;

    SimpleSocServerConnector.socket = SimpleSocServerConnector.socketIOLibrary.io();

    SimpleSocServerConnector.sharedRSAKey.secretKey = forge.random.getBytesSync(16 * 2);
    SimpleSocServerConnector.sharedRSAKey.iv = forge.random.getBytesSync(16);

    var serverPublicKeyRequest = new SocketRequest("SIMPLESOCSERVER_ENCRYPT", [], { STEP: "GET_SERVER_PUBLICKEY" });
    serverPublicKeyRequest.send();
    await serverPublicKeyRequest.waitForResponse();
    var serverPublicKey = forge.pki.publicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" + serverPublicKeyRequest.responseData + "\n-----END PUBLIC KEY-----");

    var serverSetAESRequest = new SocketRequest("SIMPLESOCSERVER_ENCRYPT", [], { STEP: "SET_AES_KEY", AESENCRYPTED: btoa(serverPublicKey.encrypt(SimpleSocServerConnector.sharedRSAKey.secretKey)), AESIV: btoa(SimpleSocServerConnector.sharedRSAKey.iv) });
    serverSetAESRequest.send();
    await serverSetAESRequest.waitForResponse();
    SimpleSocServerConnector.defaultEncrypt = true;
    appendedBlocker.remove();

    var testRequest = new SocketRequest("TESeeeeeT", [], {});
    testRequest.send();
    await testRequest.waitForResponse();

    console.log(testRequest);
})();

class SocketEvent {

    ID = Math.random() * 100000000000000000;
    NAME;
    DATA;

    SENT = false;
    COMPLETED = false;
    STATE = "UNKNOWN";
    COMPLETED_DATA = null;

    encrypt = false;

    constructor(name, payload) {
        this.NAME = name;
        this.DATA = payload;
        this.encrypt = SimpleSocServerConnector.defaultEncrypt;
    }

    send() {
        if (this.COMPLETED) return;

        var payloadString = JSON.stringify({ ID: this.ID, NAME: this.NAME, DATA: this.DATA });
        if (this.encrypt) {
            var cipher = forge.cipher.createCipher('AES-CBC', SimpleSocServerConnector.sharedRSAKey.secretKey);
            cipher.start({ iv: SimpleSocServerConnector.sharedRSAKey.iv });
            cipher.update(forge.util.createBuffer(payloadString));
            cipher.finish();

            var encrypted = cipher.output.getBytes();
            payloadString = btoa(encrypted);
        }

        if (this.encrypt) {
            SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_EVENT", JSON.stringify({ ID: this.ID, NAME: this.NAME, DATA: this.DATA }), true, async(stateData) => this.stateCallback(stateData, this.encrypt));
        } else {
            SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_EVENT", JSON.stringify({ ID: this.ID, NAME: this.NAME, DATA: this.DATA }), async(stateData) => this.stateCallback(stateData, this.encrypt));
        }
    }

    async stateCallback(responseData, isencrypted) {
        if (isencrypted) {
            responseData = atob(responseData);
            var decipher = forge.cipher.createDecipher('AES-CBC', SimpleSocServerConnector.sharedRSAKey.secretKey);
            decipher.start({ iv: SimpleSocServerConnector.sharedRSAKey.iv });
            decipher.update(forge.util.createBuffer(responseData));
            decipher.finish();
            responseData = new String(decipher.output.getBytes());
        }

        var stateData = responseData;
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

    encrypt = false;

    constructor(name, subfields, payload) {
        this.NAME = name;
        this.SUBFIELDS = subfields;
        this.PAYLOAD = payload;
        this.encrypt = SimpleSocServerConnector.defaultEncrypt;
    }

    send() {
        if (this.sent) return;

        this.sentDate = new Date();

        var payloadString = JSON.stringify({ ID: this.ID, NAME: this.NAME, SUBFIELDS: this.SUBFIELDS.join(","), PAYLOAD: this.PAYLOAD, SENT: this.sentDate.getDate(), METHOD: "GET" });
        if (this.encrypt) {
            var cipher = forge.cipher.createCipher('AES-CBC', SimpleSocServerConnector.sharedRSAKey.secretKey);
            cipher.start({ iv: SimpleSocServerConnector.sharedRSAKey.iv });
            cipher.update(forge.util.createBuffer(payloadString));
            cipher.finish();

            var encrypted = cipher.output.getBytes();
            payloadString = btoa(encrypted);
        }

        if (this.encrypt) {
            SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_REQUEST", payloadString, true, async(responseData) => this.responseCallback(responseData, this.encrypt));
        } else {
            SimpleSocServerConnector.socket.emit("SIMPLESOCSERVER_REQUEST", payloadString, async(responseData) => this.responseCallback(responseData, this.encrypt));
        }
        this.sent = true;
    }

    async responseCallback(responseData, isencrypted) {
        if (isencrypted) {
            responseData = atob(responseData);
            var decipher = forge.cipher.createDecipher('AES-CBC', SimpleSocServerConnector.sharedRSAKey.secretKey);
            decipher.start({ iv: SimpleSocServerConnector.sharedRSAKey.iv });
            decipher.update(forge.util.createBuffer(responseData));
            decipher.finish();
            responseData = new String(decipher.output.getBytes());
        }

        responseData = JSON.parse(responseData);

        this.responseCode = responseData.CODE;
        this.responseData = responseData.DATA;
        this.respondedDate = new Date(responseData.RESPONDED);
        this.responseRecieved = true;
    }

    async waitForResponse() {
        while (!this.responseRecieved) {
            await SimpleSocServerConnector.timer(250);
        }
    }
}