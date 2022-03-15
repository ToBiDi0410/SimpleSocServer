# SimpleSocServer
A simple Netty HTTP and Socket.IO Server that supports Sockets with various functions surrounding them.

## Features
- Socket.IO Socket Support
- Local Data Associated with Sockets (Sessions)
- Static Webfile Handlers (from FS) and Class Handler (for Class Resources)
- Two-way Encryption (RSA + AES)
- API for requests and responses

## Todo
- Custom HTTP Error Pages
- Better and fancier logging with possibilitie to disable
- Custom Ports
- Code Overhault (Attribute & Method Access, etc.)

## Usage
```java

//Create a new SimpleSocServer Object
SimpleSocServer sc = new SimpleSocServer();


//Create an HTTP Request Handler (like Netty Servlet Handler) for URL "/staticTest/TEST"
HTTPRequestHandler yourHTTPHandler = new HTTPRequestHandler("/staticTest/TEST", HttpMethod.GET, (req, res) -> {
  //YOUR CODE HERE
});

//Register the HTTP Handler
sc.addHTTPRequestHandler(yourHTTPHandler);

sc.addHTTPRequestHandler(new StaticHTTPRequestHandler("/staticTest/*", new File("./testweb/"))); //Create and Register Static HTTP Request Handler (for URL Path "/staticTest/*" from FS Directory "testweb")
sc.addHTTPRequestHandler(new ClassResourceHTTPRequestHandler("/classres/*", Main.class, "")); //Create and Register Class HTTP Request Handler (for URL Path "/classres/*" with reference to the Main Class for Resource getting and no prefix) (Prefix might contain a subdirectory in Class Resources such as "www/")


//Create a Raw Socket Handler for Event "TESTEVENT" (provides just the raw data from Socket.IO Server Package together the Socket Data Storage)
RawSocketEventHandler rawSocHand = new RawSocketEventHandler("TESTEVENT", (socket, eventName, socketData, data) -> {
  //YOUR CODE HERE
});

//Register the Raw Socket Event Handler
sc.addRawSocketEventHandler(rawSocHand);


//Create a Simple Socket Event Handler for Event "DIED" (with the Libraries Custom Event Object, which allows states and completion acknowledgment) 
sc.addSimpleSocketEventHandler(
SimpleSocketEventHandler yourSimpSocEventHand = new SimpleSocketEventHandler("DIED", (event, socketData) -> {
  //YOUR CODE HERE
}));

//Register the Simple Socket Event Handler
sc.addSimpleSocketEventHandler(yourSimpSocEventHand);


//Create Simple Socket Request Handler for NAME "test" with Method "GET"
SimpleSocketRequestHandler yourSimpSocReqHand = new SimpleSocketRequestHandler("test", "GET", (request, socketData) -> {
  //YOUR CODE HERE
}));

//Register the Simple Socket Request Handler
sc.addSimpleSocketRequestHandler(yourSimpSocReqHand);


//Start the Socket Server
sc.start();
```

#### ⚠️ Currently, this Project is in work there is no guarentee it will work.
