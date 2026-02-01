const http = require('http');

const requestListener = function (req, res) {
  res.writeHead(200);
  res.end('Hello from JS Web Server!');
}

const server = http.createServer(requestListener);
server.listen(8080, () => {
    console.log('Server is running on port 8080');
});
