# Vercel Deployment Note

Since this web application uses `database.json` locally for persistence, be aware that Vercel Serverless functions have a read-only filesystem (except for `/tmp`). Modifications to `database.json` will only persist locally during development, or ephemerally during a single Vercel function execution.

For true long-term persistence in Vercel production, you must swap out the file-based logic in `api/*.js` to connect to a real database (e.g. Postgres, MongoDB, or Vercel KV) using the connection URL.
