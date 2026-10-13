# DeenTok Responsive Web Application

DeenTok is a modern, responsive short-video sharing website tailored for Islamic reminders, Quran recitations, Hadith gems, and educational reflections. It works across mobile browsers (Android & iPhone), tablets, and desktop computers.

---

## 📱 Supported Devices & Responsive Layout

* **Mobile (Phones)**:
  * Full-screen vertical swipe video feed (touch gestures).
  * Fixed bottom navigation bar (Home, Explore, Upload, Inbox, Profile).
  * Floating action sidebar (Like with counter, Comments drawer, Save, Virtual Gifts, Share).
* **Desktop & Laptops**:
  * TikTok-style Left Navigation Sidebar with quick access to For You, Following, Explore, LIVE, Creator Studio, and Admin Console.
  * Centered 9:16 vertical video player framed for widescreen monitors.
  * Keyboard navigation:
    * `↓` / `J` : Next video
    * `↑` / `K` : Previous video
    * `Space` : Play / Pause
    * `L` : Like / Unlike
* **Tablets & Large Screens**:
  * Multi-column grid for Explore and Profile views.
  * Side-by-side direct messaging interface.

---

## 💾 Data Persistence & Architecture

* **Database Engine**: Web LocalStorage & IndexedDB storage layer matching the 25 entity tables from the Android Room database (`videos`, `comments`, `messages`, `notifications`, `users`, `wallets`, `virtualGifts`, etc.).
* **Preserved Content**:
  * Pre-seeded with authentic Islamic content (Surah Ar-Rahman, Hadith on kindness & mercy, Dua reminders, House of Wisdom history).
  * Video uploads immediately persist to storage and prepend to the active feed.
  * Real-time commenting, liking, following, and messaging.

---

## 🚀 Deployment Options

You can host and deploy the `/web` directory using any static or containerized web host:

### 1. Static Web Hosting (Vercel / Netlify / Cloudflare Pages / GitHub Pages)
Deploy the `/web` folder directly:
```bash
# Example with Vercel CLI
cd web
vercel --prod
```

### 2. Node.js Simple Static Server
```bash
npx serve web
# Serves at http://localhost:3000
```

### 3. Docker / Cloud Run
```dockerfile
FROM nginx:alpine
COPY web /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```
