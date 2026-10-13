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

  // --- API ROUTING LOGIC ---
  if (pathname.startsWith('/api/')) {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });

    req.on('end', async () => {
      res.setHeader('Content-Type', 'application/json');

      const dbPath = path.join(ROOT_DIR, 'database.json');
      let db = { users: [], videos: [], comments: [], messages: [], notifications: [], wallet: [] };

      try {
        if (fs.existsSync(dbPath)) {
          db = JSON.parse(await fs.promises.readFile(dbPath, 'utf8'));
        } else {
            // Setup default data from old defaults if missing
            db.users = [
              {
                userId: 'admin_user',
                username: '@me',
                email: 'admin@deentok.org',
                passwordHash: 'admin123',
                fullName: 'Jemal Jima',
                avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
                role: 'ADMIN',
                status: 'ACTIVE',
                bio: 'Platform Administrator. Beneficial reminders for the soul. Al-Qur\'an & Sunnah learner. 🌙',
                followersCount: 1420,
                followingCount: 38,
                likesCount: 5210,
                isVerified: true
              },
              {
                userId: 'creator_1',
                username: '@quran_reminders',
                email: 'quran@deentok.org',
                passwordHash: 'creator123',
                fullName: 'Sheikh Mishary Reminders',
                avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
                role: 'CREATOR',
                status: 'ACTIVE',
                bio: 'Daily Quranic recitation & reflection. Verily in remembrance do hearts find rest.',
                followersCount: 28400,
                followingCount: 12,
                likesCount: 198000,
                isVerified: true
              }
            ];
            db.videos = [
              {
                id: 1,
                userId: 'creator_1',
                username: '@quran_reminders',
                userAvatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
                videoUrl: 'https://assets.mixkit.co/videos/preview/mixkit-tree-branches-in-the-breeze-1188-large.mp4',
                thumbnailUrl: 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80',
                title: 'Beautiful Surah Ar-Rahman Recitation 📖✨',
                description: 'Let the beautiful words of Surah Ar-Rahman soothe your soul. Verily, in the remembrance of Allah do hearts find rest. #quran #peace #deentok',
                likesCount: 2450,
                commentsCount: 3,
                sharesCount: 189,
                viewsCount: 5400,
                isLiked: false,
                isSaved: false,
                isFollowingCreator: false,
                musicName: 'Soothing Quran Recitation - Sheikh Mishary',
                category: 'Quran',
                createdAt: Date.now() - 1000000
              }
            ];
            await fs.promises.writeFile(dbPath, JSON.stringify(db, null, 2));
        }
      } catch (err) {
        console.error('Error reading DB:', err);
      }

      if (req.method === 'GET' && pathname === '/api/data') {
        res.writeHead(200);
        return res.end(JSON.stringify(db));
      }

      if (req.method === 'POST') {
        try {
          const payload = body ? JSON.parse(body) : {};

          if (pathname === '/api/login') {
            const { identifier, password } = payload;
            const cleanId = identifier.startsWith('@') ? identifier : `@${identifier}`;
            const user = db.users.find(u =>
              u.username.toLowerCase() === cleanId.toLowerCase() ||
              u.username.toLowerCase() === identifier.toLowerCase() ||
              (u.email && u.email.toLowerCase() === identifier.toLowerCase())
            );

            if (user && user.passwordHash === password) {
              res.writeHead(200);
              return res.end(JSON.stringify({ success: true, user }));
            } else {
              res.writeHead(401);
              return res.end(JSON.stringify({ success: false, error: 'Invalid credentials' }));
            }
          }

          if (pathname === '/api/register') {
            const { user } = payload;
            const exists = db.users.some(u => u.username.toLowerCase() === user.username.toLowerCase());
            if (exists) {
              res.writeHead(400);
              return res.end(JSON.stringify({ success: false, error: 'Username taken' }));
            }
            db.users.push(user);
            await fs.promises.writeFile(dbPath, JSON.stringify(db, null, 2));
            res.writeHead(200);
            return res.end(JSON.stringify({ success: true, user }));
          }

          if (pathname === '/api/sync') {
            const { key, data } = payload;
            const STORAGE_KEYS = {
              VIDEOS: 'deentok_videos_v1',
              COMMENTS: 'deentok_comments_v1',
              MESSAGES: 'deentok_messages_v1',
              NOTIFICATIONS: 'deentok_notifications_v1',
              USERS: 'deentok_users_v1',
              WALLET: 'deentok_wallet_v1'
            };

            if (key === STORAGE_KEYS.USERS) db.users = data;
            if (key === STORAGE_KEYS.VIDEOS) db.videos = data;
            if (key === STORAGE_KEYS.COMMENTS) db.comments = data;
            if (key === STORAGE_KEYS.MESSAGES) db.messages = data;
            if (key === STORAGE_KEYS.NOTIFICATIONS) db.notifications = data;
            if (key === STORAGE_KEYS.WALLET) db.wallet = data;

            await fs.promises.writeFile(dbPath, JSON.stringify(db, null, 2));
            res.writeHead(200);
            return res.end(JSON.stringify({ success: true }));
          }

        } catch (err) {
          console.error('API Error:', err);
          res.writeHead(500);
          return res.end(JSON.stringify({ success: false, error: err.message }));
        }
      }

      res.writeHead(404);
      return res.end(JSON.stringify({ error: 'API route not found' }));
    });
    return;
  }
  // --- END API ROUTING LOGIC ---


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
