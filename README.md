# HTTP Server in Java

An HTTP/1.1 server built from scratch in Java using raw TCP sockets.
No HTTP libraries or frameworks are used. All parsing, routing and response handling is written by hand.

## Features

- Parses the request line, headers and body directly from the socket
- Routing:
  - `GET /` returns an empty 200 OK
  - `GET /echo/{str}` echoes the string back
  - `GET /user-agent` returns the client's `User-Agent` header
  - `GET /files/{name}` serves a file from the directory given with `--directory`
  - `POST /files/{name}` creates a file from the request body (`201 Created`)
  - Anything else returns `404 Not Found`
- Concurrent connections: one thread per connection
- Request bodies are read using `Content-Length`, including bodies that arrive in several pieces
- Gzip compression when the client sends `Accept-Encoding: gzip`
- Persistent connections (HTTP/1.1 keep-alive), including `Connection: close`

## Running it

Requires Java 25 and Maven.

```sh
./your_program.sh                     # start on port 4221
./your_program.sh --directory /tmp/   # enable the /files endpoint

curl -v http://localhost:4221/echo/hello
curl -v -H "Accept-Encoding: gzip" http://localhost:4221/echo/hello --output -
```

## Code structure

- `Main.java`: parses arguments, accepts connections and starts a thread for each one
- `RequestHandler.java`: reads requests from a connection in a loop, routes them and writes responses
- `Request.java`: holds the parsed request and the response being built

## How I built this

I wrote all of the code and did all of the debugging myself, except for once or twice when I was
really stuck and asked an AI assistant (Claude) for a hint or an explanation. I explicitly asked it not to give me code.
I also had it review my code so I could find bugs, the tests didn't catch and improve the structure.

## What I learned

- How an HTTP request is built: request line, headers, an empty line, then an optional body
- Why every response on a persistent connection needs a `Content-Length`, even when it's 0
- Why a socket read has to loop until all `Content-Length` bytes have arrived
- Why binary data (gzip, files) must stay as `byte[]` and never pass through a `String`
- Why a server needs one thread per connection, and why `Runnable` beats extending `Thread`
- Using try-with-resources so a socket is closed on every path
