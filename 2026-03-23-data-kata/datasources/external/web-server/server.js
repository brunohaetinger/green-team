const http = require('http');
const { Pool } = require('pg');
const url = require('url');

const pool = new Pool({
  user: process.env.DB_USER,
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: process.env.DB_PORT,
});

const requestListener = async function (req, res) {
  const parsedUrl = url.parse(req.url, true); // true = parse query params
  const pathname = parsedUrl.pathname;
  const query = parsedUrl.query;

  if (pathname === '/sales' && req.method === 'GET') {
    try {
      const startId = query.startId ? parseInt(query.startId, 10) : null;

      let result;

      if (startId !== null && !isNaN(startId)) {
        result = await pool.query(
          'SELECT id, name, store_id FROM salesmans WHERE id > $1',
          [startId]
        );
      } else {
        result = await pool.query(
          'SELECT id, name, store_id FROM salesmans'
        );
      }

      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(result.rows));

    } catch (err) {
      console.error(err);
      res.writeHead(500);
      res.end('Error fetching sales');
    }
  } else {
    res.writeHead(404);
    res.end('Not Found');
  }
};

const server = http.createServer(requestListener);
server.listen(8080, () => {
  console.log('Server is running on port 8080');
});