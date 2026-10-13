// DeenTok Production-Grade Zero-Dependency HTTP & Static Streaming Server
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.APP_PORT || 3000;
const ROOT_DIR = __dirname;

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8',
  '.json': 'application/json; charset=UTF-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.mp4': 'video/mp4',
  '.webm': 'video/webm',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf'
};

const server = http.createServer((req, res) => {
  // Parse clean URL
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  // Normalize path
  if (pathname === '/') {
    pathname = '/index.html';
  }

  let filePath = path.join(ROOT_DIR, pathname);

  // Safety check to prevent directory traversal
  if (!filePath.startsWith(ROOT_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    return res.end('403 Forbidden');
  }

  // Check if file exists, else fallback to web/ or index.html for SPA
  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // Check in /web/ folder as fallback
      const webPath = path.join(ROOT_DIR, 'web', pathname);
      fs.stat(webPath, (webErr, webStats) => {
        if (!webErr && webStats.isFile()) {
          serveFile(req, res, webPath, webStats);
        } else {
          // SPA routing fallback: serve index.html for application routes
          const indexPath = path.join(ROOT_DIR, 'index.html');
          fs.stat(indexPath, (indexErr, indexStats) => {
            if (!indexErr && indexStats.isFile()) {
              serveFile(req, res, indexPath, indexStats);
            } else {
              res.writeHead(404, { 'Content-Type': 'text/plain' });
              res.end('404 Not Found');
            }
          });
        }
      });
      return;
    }

    serveFile(req, res, filePath, stats);
  });
});

function serveFile(req, res, filePath, stats) {
  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  // Support HTTP range requests for smooth video and audio streaming
  const range = req.headers.range;
  if (range && (contentType.startsWith('video/') || contentType.startsWith('audio/'))) {
    const parts = range.replace(/bytes=/, "").split("-");
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : stats.size - 1;
    const chunksize = (end - start) + 1;

    res.writeHead(206, {
      'Content-Range': `bytes ${start}-${end}/${stats.size}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': contentType,
      'Cache-Control': 'no-cache'
    });

    const fileStream = fs.createReadStream(filePath, { start, end });
    fileStream.pipe(res);
    return;
  }

  res.writeHead(200, {
    'Content-Type': contentType,
    'Content-Length': stats.size,
    'Accept-Ranges': 'bytes',
    'Cache-Control': 'public, max-age=3600'
  });

  const fileStream = fs.createReadStream(filePath);
  fileStream.pipe(res);
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`DeenTok Web Server listening on port ${PORT}`);
});
