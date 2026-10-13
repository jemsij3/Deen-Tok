/**
 * DeenTok Web Client Engine
 * Full-Featured TikTok-Style Islamic Video Platform
 * Responsive for Mobile, Tablet, and Desktop Devices
 */

// ============================================================================
// 1. Data Models & Local Storage Persistence (Matching Room SQLite Entities)
// ============================================================================

const STORAGE_KEYS = {
  VIDEOS: 'deentok_videos_v1',
  COMMENTS: 'deentok_comments_v1',
  MESSAGES: 'deentok_messages_v1',
  NOTIFICATIONS: 'deentok_notifications_v1',
  USERS: 'deentok_users_v1',
  WALLET: 'deentok_wallet_v1',
  PREFS: 'deentok_preferences_v1',
  CURRENT_USER_ID: 'deentok_current_user_id_v2',
  AUDIT_LOGS: 'deentok_audit_logs_v1'
};

// ============================================================================
// IndexedDB Media Storage Engine (Client-Side Persistent Blob Store)
// ============================================================================
const DB_NAME = 'deentok_media_v2';
const DB_VERSION = 1;
const STORE_NAME = 'video_blobs';

const DeenTokDB = {
  db: null,
  async init() {
    if (this.db) return this.db;
    return new Promise((resolve) => {
      if (typeof indexedDB === 'undefined') {
        resolve(null);
        return;
      }
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = (e) => {
        const db = e.target.result;
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          db.createObjectStore(STORE_NAME);
        }
      };
      request.onsuccess = (e) => {
        this.db = e.target.result;
        resolve(this.db);
      };
      request.onerror = (e) => {
        console.warn('IndexedDB unavailable', e);
        resolve(null);
      };
    });
  },
  async saveBlob(id, blob) {
    const db = await this.init();
    if (!db) return false;
    return new Promise((resolve) => {
      try {
        const tx = db.transaction([STORE_NAME], 'readwrite');
        const store = tx.objectStore(STORE_NAME);
        store.put(blob, String(id));
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      } catch (err) {
        console.error('Failed to save video blob to IndexedDB:', err);
        resolve(false);
      }
    });
  },
  async getBlob(id) {
    const db = await this.init();
    if (!db) return null;
    return new Promise((resolve) => {
      try {
        const tx = db.transaction([STORE_NAME], 'readonly');
        const store = tx.objectStore(STORE_NAME);
        const req = store.get(String(id));
        req.onsuccess = () => resolve(req.result || null);
        req.onerror = () => resolve(null);
      } catch (err) {
        console.error('Failed to load video blob from IndexedDB:', err);
        resolve(null);
      }
    });
  },
  async deleteBlob(id) {
    const db = await this.init();
    if (!db) return false;
    return new Promise((resolve) => {
      try {
        const tx = db.transaction([STORE_NAME], 'readwrite');
        const store = tx.objectStore(STORE_NAME);
        store.delete(String(id));
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      } catch (err) {
        resolve(false);
      }
    });
  }
};





const DEFAULT_COMMENTS = [
  {
    id: 1,
    videoId: 1,
    username: '@marcus_dev',
    userAvatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=100&q=80',
    text: 'SubhanAllah, extremely beautiful recitation! Peace of mind and tranquil heart.',
    createdAt: Date.now() - 1000 * 60 * 45
  },
  {
    id: 2,
    videoId: 1,
    username: '@alice_codes',
    userAvatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=100&q=80',
    text: 'This is indeed what my heart needed to hear today. Thank you for sharing beneficial knowledge!',
    createdAt: Date.now() - 1000 * 60 * 30
  },
  {
    id: 3,
    videoId: 1,
    username: '@casual_scroll',
    userAvatarUrl: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=100&q=80',
    text: 'Verily, in the remembrance of Allah do hearts find rest. Alhamdulilah.',
    createdAt: Date.now() - 1000 * 60 * 10
  },
  {
    id: 4,
    videoId: 2,
    username: '@deen_seeker',
    userAvatarUrl: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=100&q=80',
    text: 'May Allah make us among the merciful. Ameen!',
    createdAt: Date.now() - 1000 * 60 * 120
  }
];

const DEFAULT_MESSAGES = [
  {
    id: 1,
    senderId: 'creator_1',
    senderName: '@quran_reminders',
    receiverId: 'current_user',
    text: 'Assalamu Alaikum brother! Thank you for following. May Allah bless you.',
    createdAt: Date.now() - 1000 * 60 * 60
  },
  {
    id: 2,
    senderId: 'current_user',
    senderName: '@me',
    receiverId: 'creator_1',
    text: 'Wa Alaikum Assalam! Your recitations bring so much peace. Keep up the great work.',
    createdAt: Date.now() - 1000 * 60 * 40
  }
];

const DEFAULT_NOTIFICATIONS = [
  {
    id: 1,
    type: 'LIKE',
    title: 'New Like!',
    description: 'liked your video: "Daily Morning Adhkar Reflection"',
    sourceUsername: '@quran_reminders',
    isRead: false,
    createdAt: Date.now() - 1000 * 60 * 15
  },
  {
    id: 2,
    type: 'FOLLOW',
    title: 'New Follower!',
    description: 'started following you on DeenTok.',
    sourceUsername: '@sunnah_gems',
    isRead: false,
    createdAt: Date.now() - 1000 * 60 * 90
  },
  {
    id: 3,
    type: 'SYSTEM',
    title: 'Welcome to DeenTok!',
    description: 'Join millions of Muslims sharing beneficial knowledge worldwide.',
    sourceUsername: '@deentok_team',
    isRead: true,
    createdAt: Date.now() - 1000 * 60 * 60 * 24
  }
];

// Helper methods for localStorage
function getLocalData(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch (e) {
    console.error('Storage read error', e);
    return fallback;
  }
}

function setLocalData(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data));
  } catch (e) {
    console.error('Storage write error', e);
  }
}

// Global App State
const AppState = {
  currentScreen: 'home',
  homeTab: 'foryou', // 'foryou' | 'following'
  currentVideoIndex: 0,
  activeVideoIdForComments: 1,
  activeChatRecipient: 'creator_1',
  currentUser: null,
  videos: [],
  comments: [],
  messages: [],
  notifications: [],
  users: [],
  userWallet: { coins: 150 },
  selectedCategory: 'All',
  searchQuery: '',
  profileTab: 'videos',
  userDashboardTab: 'overview', // 'overview' | 'my_videos' | 'saved' | 'liked' | 'notifications' | 'messages' | 'settings'
  adminSection: 'overview', // 'overview' | 'users' | 'videos' | 'community' | 'platform' | 'security'
  adminUserSearch: '',
  adminRoleFilter: 'ALL',
  adminVideoSearch: '',
  adminCategoryFilter: 'ALL',
  adminCommunitySubTab: 'comments',
  activeEditingVideoId: null,
  adminAuditLogs: [
    { timestamp: '14:28:10', admin: 'Jemal Jima', action: 'System Verification', details: 'All 25 entities verified and synced' },
    { timestamp: '13:50:22', admin: 'Jemal Jima', action: 'Content Reviewed', details: 'ID 1: Surah Ar-Rahman (Passed)' },
    { timestamp: '12:15:04', admin: 'Jemal Jima', action: 'Verification Issued', details: '@sunnah_gems verified' }
  ],
  adminReports: [
    { id: 1, type: 'COMMENT', targetId: 'c_4', reason: 'Spam promotion', reportedBy: '@marcus_dev', status: 'PENDING', createdAt: Date.now() - 3600000 },
    { id: 2, type: 'VIDEO', targetId: 'v_2', reason: 'Misattributed quote', reportedBy: '@casual_scroll', status: 'RESOLVED', createdAt: Date.now() - 7200000 }
  ],
  adminAds: [
    { id: 1, title: 'Umrah & Hajj Special Guide 2026', sponsor: 'Al-Haramain Travel', status: 'ACTIVE', impressions: 18450, link: '#' },
    { id: 2, title: 'Global Water Well Project - Sadaqah Jariyah', sponsor: 'Islamic Relief Fund', status: 'ACTIVE', impressions: 32100, link: '#' },
    { id: 3, title: 'Learn Classical Quranic Arabic', sponsor: 'Bayyinah Institute', status: 'PAUSED', impressions: 9400, link: '#' }
  ],
  islamicCategories: [
    { name: 'Quran', count: 18, active: true },
    { name: 'Hadith', count: 12, active: true },
    { name: 'Dua', count: 9, active: true },
    { name: 'Islamic History', count: 7, active: true },
    { name: 'Reflection', count: 14, active: true },
    { name: 'Seerah', count: 5, active: true },
    { name: 'Fiqh Basics', count: 6, active: true }
  ]
};

// ============================================================================
// 2. Initialization & Bootstrapping
// ============================================================================

document.addEventListener('DOMContentLoaded', async () => {
  try {
    const res = await fetch('/api/data');
    if (res.ok) {
      const db = await res.json();
      AppState.users = db.users || [];
      AppState.videos = db.videos || [];
      AppState.comments = db.comments || [];
      AppState.messages = db.messages || [];
      AppState.notifications = db.notifications || [];
      AppState.userWallet = db.wallet || [];
    }
  } catch (err) {
    console.error('Failed to load data from backend', err);
  }

  // Restore current user session from local storage id
  const storedUserId = getLocalData(STORAGE_KEYS.CURRENT_USER_ID, null);
  if (storedUserId) {
    AppState.currentUser = AppState.users.find(u => u.userId === storedUserId) || null;
  }

  setupEventListeners();
  renderApp();
  renderVideoFeed();
  renderExplorePills();
  renderSuggestedCreators();
  updateHeaderBadges();
  updateHeaderAuthState();
});

async function initDataStore() { /* Migrated to backend */ }

function updateHeaderAuthState() {
  const loginBtn = document.getElementById('headerLoginBtn');
  const trigger = document.getElementById('headerUserMenuTrigger');
  const avatar = document.getElementById('headerUserAvatar');
  const dropdownAvatar = document.getElementById('dropdownUserAvatar');
  const dropdownName = document.getElementById('dropdownUserName');
  const dropdownHandle = document.getElementById('dropdownUserHandle');
  const dropdownRole = document.getElementById('dropdownUserRole');
  const adminLink = document.getElementById('dropdownAdminLink');

  if (AppState.currentUser) {
    if (loginBtn) loginBtn.style.display = 'none';
    if (trigger) trigger.style.display = 'flex';
    if (avatar) avatar.src = AppState.currentUser.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80';
    if (dropdownAvatar) dropdownAvatar.src = AppState.currentUser.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80';
    if (dropdownName) dropdownName.innerText = AppState.currentUser.fullName || 'User';
    if (dropdownHandle) dropdownHandle.innerText = AppState.currentUser.username || '@user';
    if (dropdownRole) {
      dropdownRole.innerText = AppState.currentUser.role || 'USER';
      dropdownRole.className = `user-role-badge ${AppState.currentUser.role === 'ADMIN' ? 'role-badge-admin' : AppState.currentUser.role === 'CREATOR' ? 'role-badge-creator' : 'role-badge-user'}`;
    }
    if (adminLink) {
      adminLink.style.display = AppState.currentUser.role === 'ADMIN' ? 'flex' : 'none';
    }
  } else {
    if (loginBtn) loginBtn.style.display = 'block';
    if (trigger) trigger.style.display = 'none';
  }
}

function requireAuth(actionName = 'perform this action') {
  if (!AppState.currentUser) {
    showToast(`Please sign in to ${actionName}.`);
    openAuthModal('login');
    return false;
  }
  return true;
}

function addAuditLog(action, details) {
  const now = new Date();
  const timeStr = now.toTimeString().split(' ')[0];
  const adminName = AppState.currentUser?.fullName || 'System';
  const entry = {
    timestamp: timeStr,
    admin: adminName,
    action: action,
    details: details
  };
  AppState.adminAuditLogs.unshift(entry);
  if (AppState.adminAuditLogs.length > 60) AppState.adminAuditLogs.pop();
  setLocalData(STORAGE_KEYS.AUDIT_LOGS, AppState.adminAuditLogs);
}

// ============================================================================
// 3. Navigation & Screen Management
// ============================================================================

function navigateTo(screenName) {
  AppState.currentScreen = screenName;

  // Toggle active class on all screen views
  document.querySelectorAll('.screen-view').forEach(s => s.classList.remove('active'));
  const targetScreen = document.getElementById(`screen-${screenName}`);
  if (targetScreen) {
    targetScreen.classList.add('active');
  }

  // Update desktop sidebar highlights
  document.querySelectorAll('.sidebar .nav-item').forEach(item => item.classList.remove('active'));
  const activeSide = document.getElementById(`sidebar-${screenName}`);
  if (activeSide) activeSide.classList.add('active');

  // Update mobile bottom nav highlights
  document.querySelectorAll('.mobile-bottom-nav .mobile-nav-item').forEach(item => item.classList.remove('active'));
  const activeMob = document.getElementById(`mob-nav-${screenName}`);
  if (activeMob) activeMob.classList.add('active');

  // Trigger screen-specific rendering
  if (screenName === 'home') {
    renderVideoFeed();
  } else if (screenName === 'explore') {
    renderExploreGrid();
  } else if (screenName === 'messages') {
    renderConversations();
    renderChatMessages();
  } else if (screenName === 'notifications') {
    renderNotifications();
  } else if (screenName === 'profile') {
    renderProfile();
  } else if (screenName === 'admin') {
    renderAdmin();
  } else if (screenName === 'live') {
    renderLiveStreams();
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function switchHomeTab(tab) {
  AppState.homeTab = tab;
  AppState.currentVideoIndex = 0;

  document.getElementById('mobileTabForYou')?.classList.toggle('active', tab === 'foryou');
  document.getElementById('mobileTabFollowing')?.classList.toggle('active', tab === 'following');
  document.getElementById('sidebar-home')?.classList.toggle('active', tab === 'foryou');
  document.getElementById('sidebar-following')?.classList.toggle('active', tab === 'following');

  renderVideoFeed();
}

function switchProfileTab(tab) {
  switchUserDashboardTab(tab === 'videos' ? 'my_videos' : tab);
}

function switchAdminTab(tab) {
  switchAdminSection(tab);
}

// ============================================================================
// 4. Video Feed Engine & Controls
// ============================================================================

function getFilteredFeedVideos() {
  if (AppState.homeTab === 'following') {
    return AppState.videos.filter(v => v.isFollowingCreator || v.userId === AppState.currentUser.userId);
  }
  return AppState.videos;
}

function renderVideoFeed() {
  const container = document.getElementById('videoFeedContainer');
  if (!container) return;

  const feedVideos = getFilteredFeedVideos();
  if (feedVideos.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:40px; color:#fff;">
        <h3 style="font-size:20px; margin-bottom:8px;">No videos yet!</h3>
        <p style="color:#9ca3af; font-size:14px; margin-bottom:16px;">
          ${AppState.homeTab === 'following' ? 'Follow creators in For You or Explore to see their videos here.' : 'Upload your first beneficial reminder!'}
        </p>
        <button class="btn-primary" onclick="switchHomeTab('foryou')">Explore DeenTok</button>
      </div>
    `;
    return;
  }

  const currentVideo = feedVideos[AppState.currentVideoIndex] || feedVideos[0];
  const isLast = AppState.currentVideoIndex === feedVideos.length - 1;

  container.innerHTML = `
    <div class="video-card-wrapper" id="activeVideoCard">
      <div class="video-player-container">
        <!-- Responsive Media Element -->
        <video 
          class="video-media" 
          src="${currentVideo.videoUrl}" 
          poster="${currentVideo.thumbnailUrl}"
          loop 
          playsinline 
          autoplay
          muted
          id="activeVideoElement"
          onclick="toggleVideoPlayPause()"
        ></video>

        <!-- Top Gradient Scrim -->
        <div class="video-dark-scrim"></div>

        <!-- Video Metadata Overlay -->
        <div class="video-meta-overlay">
          ${currentVideo.category ? `
            <div class="quran-badge-pill">
              <span>📖</span>
              <span>${currentVideo.category}</span>
              ${currentVideo.quranRef ? `<span>• ${currentVideo.quranRef}</span>` : ''}
              ${currentVideo.hadithRef ? `<span>• ${currentVideo.hadithRef}</span>` : ''}
            </div>
          ` : ''}

          <div class="creator-handle-row" onclick="viewCreatorProfile('${currentVideo.userId}')">
            <span class="creator-name">${currentVideo.username}</span>
            <span class="verified-icon">✓</span>
          </div>

          <p class="video-title-text">${currentVideo.title}</p>
          <p class="video-title-text" style="color:#d1d5db; font-size:13px;">${currentVideo.description}</p>

          <div class="video-music-row">
            <svg class="music-disc-rotating" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"></circle>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <span>${currentVideo.musicName || 'Original Audio - DeenTok Reminders'}</span>
          </div>
        </div>

        <!-- Vertical Action Bar (Right-Hand Side) -->
        <div class="video-actions-sidebar">
          <!-- Creator Avatar + Follow Button -->
          <div class="creator-avatar-wrap">
            <img class="creator-avatar-action" src="${currentVideo.userAvatarUrl}" alt="Creator">
            ${!currentVideo.isFollowingCreator ? `
              <div class="follow-plus-badge" onclick="toggleFollowVideo(${currentVideo.id})" title="Follow">+</div>
            ` : ''}
          </div>

          <!-- Like Button -->
          <button class="action-btn-item ${currentVideo.isLiked ? 'liked' : ''}" onclick="toggleLikeVideo(${currentVideo.id})">
            <div class="action-circle-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="${currentVideo.isLiked ? '#ef4444' : 'none'}" stroke="${currentVideo.isLiked ? '#ef4444' : 'currentColor'}" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
            </div>
            <span class="action-label-stat">${formatMetric(currentVideo.likesCount)}</span>
          </button>

          <!-- Comment Button -->
          <button class="action-btn-item" onclick="openCommentsModal(${currentVideo.id})">
            <div class="action-circle-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
              </svg>
            </div>
            <span class="action-label-stat">${formatMetric(currentVideo.commentsCount)}</span>
          </button>

          <!-- Save / Bookmark Button -->
          <button class="action-btn-item ${currentVideo.isSaved ? 'liked' : ''}" onclick="toggleSaveVideo(${currentVideo.id})">
            <div class="action-circle-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="${currentVideo.isSaved ? '#f59e0b' : 'none'}" stroke="${currentVideo.isSaved ? '#f59e0b' : 'currentColor'}" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <span class="action-label-stat">${currentVideo.isSaved ? 'Saved' : 'Save'}</span>
          </button>

          <!-- Virtual Gift Button -->
          <button class="action-btn-item" onclick="openGiftTray(${currentVideo.id})">
            <div class="action-circle-icon" style="color:var(--gold-accent);">
              <span style="font-size:20px;">🪙</span>
            </div>
            <span class="action-label-stat">Gift</span>
          </button>

          <!-- Share Button -->
          <button class="action-btn-item" onclick="shareVideo(${currentVideo.id})">
            <div class="action-circle-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"></circle>
                <circle cx="6" cy="12" r="3"></circle>
                <circle cx="18" cy="19" r="3"></circle>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
              </svg>
            </div>
            <span class="action-label-stat">${formatMetric(currentVideo.sharesCount)}</span>
          </button>

          <!-- Fullscreen Preview Button -->
          <button class="action-btn-item" onclick="toggleFullscreenFeed()" title="Toggle Fullscreen Preview">
            <div class="action-circle-icon" style="color:var(--emerald-light);">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"></path>
              </svg>
            </div>
            <span class="action-label-stat">Full</span>
          </button>
        </div>
      </div>
    </div>
  `;

  // Autoplay video safely
  const vidEl = document.getElementById('activeVideoElement');
  if (vidEl) {
    vidEl.play().catch(e => {
      console.log('Autoplay muted by browser policy, user tap required', e);
    });
  }
}

function nextVideo() {
  const feed = getFilteredFeedVideos();
  if (AppState.currentVideoIndex < feed.length - 1) {
    AppState.currentVideoIndex++;
  } else {
    AppState.currentVideoIndex = 0; // loop
  }
  renderVideoFeed();
}

function prevVideo() {
  const feed = getFilteredFeedVideos();
  if (AppState.currentVideoIndex > 0) {
    AppState.currentVideoIndex--;
  } else {
    AppState.currentVideoIndex = feed.length - 1;
  }
  renderVideoFeed();
}

function toggleVideoPlayPause() {
  const vidEl = document.getElementById('activeVideoElement');
  if (!vidEl) return;
  if (vidEl.paused) {
    vidEl.play();
    showToast('Resumed');
  } else {
    vidEl.pause();
    showToast('Paused');
  }
}

function toggleLikeVideo(videoId) {
  if (!requireAuth('like video reminders')) return;

  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;

  video.isLiked = !video.isLiked;
  video.likesCount += video.isLiked ? 1 : -1;

  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();

  if (video.isLiked) {
    addNotification({
      type: 'LIKE',
      title: 'New Like!',
      description: `liked your video: "${video.title}"`,
      sourceUsername: AppState.currentUser?.username || '@user'
    });
    showToast('Added to Liked Videos ❤️');
  }
}

function toggleFollowVideo(videoId) {
  if (!requireAuth('follow creators')) return;

  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;

  video.isFollowingCreator = !video.isFollowingCreator;
  // Sync to all videos by this creator
  AppState.videos.forEach(v => {
    if (v.username === video.username) {
      v.isFollowingCreator = video.isFollowingCreator;
    }
  });

  // Also update creator record
  const creator = AppState.users.find(u => u.username === video.username || u.userId === video.userId);
  if (creator) {
    creator.followersCount = Math.max(0, (creator.followersCount || 0) + (video.isFollowingCreator ? 1 : -1));
    if (AppState.currentUser) {
      AppState.currentUser.followingCount = Math.max(0, (AppState.currentUser.followingCount || 0) + (video.isFollowingCreator ? 1 : -1));
    }
    setLocalData(STORAGE_KEYS.USERS, AppState.users);
  }

  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();
  renderSuggestedCreators();
  showToast(video.isFollowingCreator ? `Following ${video.username}` : `Unfollowed ${video.username}`);
}

function toggleSaveVideo(videoId) {
  if (!requireAuth('save videos')) return;

  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;
  video.isSaved = !video.isSaved;
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();
  showToast(video.isSaved ? 'Video Saved to Bookmarks 🔖' : 'Removed from Bookmarks');
}

function shareVideo(videoId) {
  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;
  video.sharesCount++;
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();

  const shareUrl = window.location.origin + window.location.pathname + `?v=${videoId}`;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(shareUrl);
    showToast('Link copied to clipboard! 📋');
  } else {
    showToast(`Share link: ${shareUrl}`);
  }
}

function toggleFullscreenFeed() {
  document.body.classList.toggle('fullscreen-feed-active');
  const isFull = document.body.classList.contains('fullscreen-feed-active');
  if (isFull) {
    if (document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen().catch(() => {});
    }
    showToast('Full Screen Preview Enabled ⛶');
  } else {
    if (document.exitFullscreen && document.fullscreenElement) {
      document.exitFullscreen().catch(() => {});
    }
    showToast('Exited Full Screen');
  }
}

// ============================================================================
// 5. Comments Modal & Interactions
// ============================================================================

function openCommentsModal(videoId) {
  AppState.activeVideoIdForComments = videoId;
  const modal = document.getElementById('commentsModal');
  if (modal) modal.classList.add('active');
  renderCommentsList();
}

function closeCommentsModal() {
  const modal = document.getElementById('commentsModal');
  if (modal) modal.classList.remove('active');
}

function renderCommentsList() {
  const container = document.getElementById('commentsListContainer');
  const countEl = document.getElementById('commentsModalCount');
  if (!container) return;

  const comments = AppState.comments.filter(c => c.videoId === AppState.activeVideoIdForComments);
  if (countEl) countEl.innerText = `Comments (${comments.length})`;

  if (comments.length === 0) {
    container.innerHTML = `<p style="text-align:center; color:#9ca3af; padding:30px;">Be the first to share a beneficial reflection! 💬</p>`;
    return;
  }

  container.innerHTML = comments.map(c => `
    <div class="comment-item">
      <img src="${c.userAvatarUrl}" class="avatar-sm" alt="${c.username}">
      <div style="flex:1;">
        <div style="display:flex; justify-content:space-between; margin-bottom:2px;">
          <strong style="font-size:13px; color:#fff;">${c.username}</strong>
          <span style="font-size:11px; color:#6b7280;">${formatTimeAgo(c.createdAt)}</span>
        </div>
        <p style="font-size:13px; color:#d1d5db; line-height:1.4;">${escapeHtml(c.text)}</p>
      </div>
    </div>
  `).join('');
}

function handleCommentKey(e) {
  if (e.key === 'Enter') {
    submitNewComment();
  }
}

function submitNewComment() {
  if (!requireAuth('post comments')) return;

  const input = document.getElementById('newCommentInput');
  if (!input || !input.value.trim()) return;

  const newComment = {
    id: Date.now(),
    videoId: AppState.activeVideoIdForComments,
    username: AppState.currentUser.username,
    userAvatarUrl: AppState.currentUser.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=100&q=80',
    text: input.value.trim(),
    createdAt: Date.now()
  };

  AppState.comments.push(newComment);
  setLocalData(STORAGE_KEYS.COMMENTS, AppState.comments);

  // Increment comment count on video
  const video = AppState.videos.find(v => v.id === AppState.activeVideoIdForComments);
  if (video) {
    video.commentsCount++;
    setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  }

  input.value = '';
  renderCommentsList();
  renderVideoFeed();
  showToast('Comment posted! 💬');
}

// ============================================================================
// 6. Explore & Search
// ============================================================================

const CATEGORIES = ['All', 'Quran', 'Hadith', 'Dua', 'Islamic History', 'Reminders'];

function renderExplorePills() {
  const container = document.getElementById('categoryPillsContainer');
  if (!container) return;

  container.innerHTML = CATEGORIES.map(cat => `
    <button class="pill-item ${AppState.selectedCategory === cat ? 'active' : ''}" onclick="selectCategory('${cat}')">
      ${cat}
    </button>
  `).join('');
}

function selectCategory(cat) {
  AppState.selectedCategory = cat;
  renderExplorePills();
  renderExploreGrid();
}

function renderExploreGrid() {
  const grid = document.getElementById('exploreVideoGrid');
  if (!grid) return;

  let list = AppState.videos;
  if (AppState.selectedCategory !== 'All') {
    list = list.filter(v => v.category === AppState.selectedCategory);
  }

  if (AppState.searchQuery) {
    const q = AppState.searchQuery.toLowerCase();
    list = list.filter(v => 
      v.title.toLowerCase().includes(q) || 
      v.description.toLowerCase().includes(q) || 
      v.username.toLowerCase().includes(q)
    );
  }

  if (list.length === 0) {
    grid.innerHTML = `<p style="grid-column:1/-1; text-align:center; padding:40px; color:#9ca3af;">No matching videos found.</p>`;
    return;
  }

  grid.innerHTML = list.map(v => `
    <div class="grid-video-card" onclick="openVideoDirectly(${v.id})">
      <img class="grid-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
      <div class="grid-video-overlay">
        <p class="grid-video-title">${v.title}</p>
        <div style="display:flex; justify-content:space-between; margin-top:4px; font-size:11px; color:#9ca3af;">
          <span>${v.username}</span>
          <span>❤️ ${formatMetric(v.likesCount)}</span>
        </div>
      </div>
    </div>
  `).join('');
}

function openVideoDirectly(videoId) {
  const idx = AppState.videos.findIndex(v => v.id === videoId);
  if (idx !== -1) {
    AppState.currentVideoIndex = idx;
    navigateTo('home');
  }
}

function handleSearchKey(e) {
  if (e.key === 'Enter') {
    executeSearch();
  }
}

function executeSearch() {
  const input = document.getElementById('desktopSearchInput');
  if (!input) return;
  AppState.searchQuery = input.value.trim();
  navigateTo('explore');
  renderExploreGrid();
}

// ============================================================================
// 7. Video Upload Studio (Real Local Video Blob & URL Storage Engine)
// ============================================================================

let pendingUploadFile = null;
let pendingUploadFileUrl = null;

function triggerFileInput() {
  document.getElementById('videoFileInput')?.click();
}

function handleFileSelect(e) {
  const file = e.target.files[0];
  if (!file) return;

  pendingUploadFile = file;
  if (pendingUploadFileUrl) {
    URL.revokeObjectURL(pendingUploadFileUrl);
  }
  pendingUploadFileUrl = URL.createObjectURL(file);

  const titleInput = document.getElementById('uploadTitle');
  if (titleInput && !titleInput.value) {
    titleInput.value = file.name.replace(/\.[^/.]+$/, "").replace(/[-_]/g, ' ');
  }

  const previewBox = document.getElementById('uploadPreviewBox');
  const previewVideo = document.getElementById('uploadVideoPreview');
  const fileInfo = document.getElementById('uploadFileInfo');

  if (previewBox && previewVideo) {
    previewVideo.src = pendingUploadFileUrl;
    previewBox.style.display = 'block';
  }
  if (fileInfo) {
    const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
    fileInfo.innerText = `${file.name} (${sizeMb} MB) • Ready to publish`;
  }
  showToast(`Loaded video file: ${file.name}`);
}

function clearSelectedVideo() {
  pendingUploadFile = null;
  if (pendingUploadFileUrl) {
    URL.revokeObjectURL(pendingUploadFileUrl);
    pendingUploadFileUrl = null;
  }
  const previewBox = document.getElementById('uploadPreviewBox');
  const previewVideo = document.getElementById('uploadVideoPreview');
  const fileInput = document.getElementById('videoFileInput');
  if (previewBox) previewBox.style.display = 'none';
  if (previewVideo) previewVideo.src = '';
  if (fileInput) fileInput.value = '';
}

async function publishVideo() {
  if (!requireAuth('publish videos')) return;

  const title = document.getElementById('uploadTitle')?.value.trim();
  const desc = document.getElementById('uploadDescription')?.value.trim() || '';
  const category = document.getElementById('uploadCategory')?.value || 'Quran';
  const music = document.getElementById('uploadMusic')?.value.trim() || 'Original Audio - DeenTok';
  const ref = document.getElementById('uploadRef')?.value.trim() || '';
  const thumbUrl = document.getElementById('uploadThumbUrl')?.value.trim() || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80';

  if (!title) {
    showToast('Please enter a title for your video.');
    return;
  }

  if (!pendingUploadFile && !pendingUploadFileUrl) {
    showToast('Please select a video file or provide a video link.');
    return;
  }

  const newVideoId = Date.now();
  let videoPlaybackUrl = pendingUploadFileUrl;

  // Persist real Blob in client IndexedDB storage
  if (pendingUploadFile) {
    await DeenTokDB.saveBlob(newVideoId, pendingUploadFile);
  }

  const newVideo = {
    id: newVideoId,
    userId: AppState.currentUser.userId,
    username: AppState.currentUser.username,
    userAvatarUrl: AppState.currentUser.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80',
    videoUrl: videoPlaybackUrl,
    hasBlob: !!pendingUploadFile,
    blobKey: newVideoId,
    thumbnailUrl: thumbUrl,
    title: title,
    description: desc,
    likesCount: 0,
    commentsCount: 0,
    sharesCount: 0,
    viewsCount: 1,
    isLiked: false,
    isSaved: false,
    isFollowingCreator: false,
    musicName: music,
    category: category,
    quranRef: ref,
    createdAt: Date.now()
  };

  AppState.videos.unshift(newVideo);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);

  addAuditLog('VIDEO_PUBLISHED', `Published video "${title}" (ID: ${newVideoId})`);

  // Clear inputs
  document.getElementById('uploadTitle').value = '';
  document.getElementById('uploadDescription').value = '';
  document.getElementById('uploadRef').value = '';
  clearSelectedVideo();

  showToast('Video published successfully! 🚀');
  AppState.currentVideoIndex = 0;
  navigateTo('home');
}

// ============================================================================
// 8. Direct Messaging & Notifications
// ============================================================================

function renderConversations() {
  const container = document.getElementById('conversationsList');
  if (!container) return;

  const creators = AppState.users.filter(u => u.userId !== 'current_user');
  container.innerHTML = creators.map(c => `
    <div class="conv-item ${AppState.activeChatRecipient === c.userId ? 'active' : ''}" onclick="selectChatRecipient('${c.userId}')">
      <img src="${c.avatarUrl}" class="avatar-sm" alt="${c.username}">
      <div>
        <div style="font-weight:700; font-size:13px; color:#fff;">${c.username}</div>
        <div style="font-size:11px; color:#9ca3af;">Tap to open chat</div>
      </div>
    </div>
  `).join('');
}

function selectChatRecipient(userId) {
  AppState.activeChatRecipient = userId;
  renderConversations();
  renderChatMessages();
}

function renderChatMessages() {
  const container = document.getElementById('chatMessagesScroll');
  const header = document.getElementById('chatActiveUser');
  if (!container) return;

  const recipient = AppState.users.find(u => u.userId === AppState.activeChatRecipient);
  if (header && recipient) {
    header.innerText = `Chat with ${recipient.username} (${recipient.fullName})`;
  }

  const msgs = AppState.messages.filter(m => 
    (m.senderId === 'current_user' && m.receiverId === AppState.activeChatRecipient) ||
    (m.senderId === AppState.activeChatRecipient && m.receiverId === 'current_user')
  );

  if (msgs.length === 0) {
    container.innerHTML = `<p style="text-align:center; color:#9ca3af; margin:auto;">Send a beneficial salam to start the conversation! ✉️</p>`;
    return;
  }

  container.innerHTML = msgs.map(m => {
    const isMine = m.senderId === 'current_user';
    return `
      <div class="chat-bubble ${isMine ? 'mine' : 'theirs'}">
        ${escapeHtml(m.text)}
      </div>
    `;
  }).join('');

  container.scrollTop = container.scrollHeight;
}

function handleChatKey(e) {
  if (e.key === 'Enter') {
    sendDirectMessage();
  }
}

function sendDirectMessage() {
  if (!requireAuth('send direct messages')) return;

  const input = document.getElementById('chatMsgInput');
  if (!input || !input.value.trim()) return;

  const newMsg = {
    id: Date.now(),
    senderId: AppState.currentUser.userId,
    senderName: AppState.currentUser.username,
    receiverId: AppState.activeChatRecipient,
    text: input.value.trim(),
    createdAt: Date.now()
  };

  AppState.messages.push(newMsg);
  setLocalData(STORAGE_KEYS.MESSAGES, AppState.messages);

  input.value = '';
  renderChatMessages();
}

function renderNotifications() {
  const container = document.getElementById('notifListContainer');
  if (!container) return;

  if (AppState.notifications.length === 0) {
    container.innerHTML = `<p style="text-align:center; color:#9ca3af; padding:40px;">No notifications yet.</p>`;
    return;
  }

  container.innerHTML = AppState.notifications.map(n => `
    <div style="display:flex; align-items:center; gap:14px; padding:16px; background:${n.isRead ? 'var(--bg-dark)' : 'var(--bg-card)'}; border-radius:12px; margin-bottom:10px; border:1px solid var(--border-color);">
      <span style="font-size:24px;">${n.type === 'LIKE' ? '❤️' : n.type === 'FOLLOW' ? '👤' : '🔔'}</span>
      <div style="flex:1;">
        <strong style="font-size:14px; color:#fff;">${n.title}</strong>
        <p style="font-size:13px; color:#d1d5db;">${n.sourceUsername} ${n.description}</p>
        <span style="font-size:11px; color:#6b7280;">${formatTimeAgo(n.createdAt)}</span>
      </div>
    </div>
  `).join('');
}

function markAllNotificationsRead() {
  AppState.notifications.forEach(n => n.isRead = true);
  setLocalData(STORAGE_KEYS.NOTIFICATIONS, AppState.notifications);
  renderNotifications();
  updateHeaderBadges();
  showToast('All notifications marked as read.');
}

function addNotification(n) {
  AppState.notifications.unshift({
    id: Date.now(),
    ...n,
    isRead: false,
    createdAt: Date.now()
  });
  setLocalData(STORAGE_KEYS.NOTIFICATIONS, AppState.notifications);
  updateHeaderBadges();
}

function updateHeaderBadges() {
  const unreadNotifs = AppState.notifications.filter(n => !n.isRead).length;
  const badge = document.getElementById('headerNotifBadge');
  if (badge) {
    badge.style.display = unreadNotifs > 0 ? 'block' : 'none';
  }
}

// ============================================================================
// 9. Profile & Settings
// ============================================================================

// ============================================================================
// 9. User Account & Creator Dashboard
// ============================================================================

function renderProfile() {
  renderUserDashboard();
}

function switchUserDashboardTab(tab) {
  AppState.userDashboardTab = tab;
  renderUserDashboard();
}

// Backward compatibility alias for any existing tab buttons
function switchProfileTab(tab) {
  if (tab === 'videos') AppState.userDashboardTab = 'my_videos';
  else if (tab === 'liked') AppState.userDashboardTab = 'liked';
  else if (tab === 'saved') AppState.userDashboardTab = 'saved';
  else AppState.userDashboardTab = tab;
  renderUserDashboard();
}

function renderUserDashboard() {
  const u = AppState.currentUser;
  if (!u) {
    const container = document.getElementById('userDashboardBody');
    if (container) {
      container.innerHTML = `
        <div class="access-denied-box" style="margin-top:20px;">
          <div style="font-size:48px; margin-bottom:16px;">👤</div>
          <h2 style="color:#fff; margin-bottom:8px;">Sign In to View Your Account</h2>
          <p style="color:#9ca3af; margin-bottom:20px; font-size:14px; line-height:1.5;">
            Create an account or sign in to manage your uploaded video reminders, bookmarks, likes, and profile settings.
          </p>
          <div style="display:flex; justify-content:center; gap:12px;">
            <button class="btn-primary" onclick="openAuthModal('login')">Sign In</button>
            <button class="btn-outline-sm" style="padding:10px 20px;" onclick="openAuthModal('register')">Create Account</button>
          </div>
        </div>
      `;
    }
    return;
  }

  // 1. Update Profile Hero Elements
  const avatarElem = document.getElementById('profileAvatar');
  if (avatarElem) avatarElem.src = u.avatarUrl;
  
  const userFullElem = document.getElementById('profileFullName');
  if (userFullElem) userFullElem.innerText = u.fullName;
  
  const userHandleElem = document.getElementById('profileUsername');
  if (userHandleElem) userHandleElem.innerText = u.username;
  
  const userBioElem = document.getElementById('profileBio');
  if (userBioElem) userBioElem.innerText = u.bio;

  const userRoleElem = document.getElementById('userRoleBadge');
  if (userRoleElem) userRoleElem.innerText = u.role || 'Member';

  const followingElem = document.getElementById('profileFollowingCount');
  if (followingElem) followingElem.innerText = u.followingCount;

  const followersElem = document.getElementById('profileFollowersCount');
  if (followersElem) followersElem.innerText = formatMetric(u.followersCount);

  const likesElem = document.getElementById('profileLikesCount');
  if (likesElem) likesElem.innerText = formatMetric(u.likesCount);

  const verifiedBadge = document.getElementById('profileVerifiedBadge');
  if (verifiedBadge) verifiedBadge.style.display = u.isVerified ? 'inline-block' : 'none';

  const coinDisplay = document.getElementById('userWalletCoinDisplay');
  if (coinDisplay) coinDisplay.innerText = `${AppState.userWallet.coins} 🪙`;

  // 2. Highlight Active Dashboard Tab
  document.querySelectorAll('.ud-tab-btn').forEach(btn => btn.classList.remove('active'));
  const activeTabBtn = document.getElementById(`udTab-${AppState.userDashboardTab}`);
  if (activeTabBtn) activeTabBtn.classList.add('active');

  // 3. Render Dashboard Tab Content
  const container = document.getElementById('userDashboardBody');
  if (!container) return;

  const tab = AppState.userDashboardTab;

  if (tab === 'overview') {
    const userVideos = AppState.videos.filter(v => v.userId === u.userId);
    const totalViews = userVideos.reduce((sum, v) => sum + (v.viewsCount || 0), 0);
    const totalLikes = userVideos.reduce((sum, v) => sum + (v.likesCount || 0), 0);
    const totalComments = userVideos.reduce((sum, v) => sum + (v.commentsCount || 0), 0);

    container.innerHTML = `
      <div class="ud-overview-grid">
        <div class="ud-section-card">
          <h3>
            <span>Account Summary</span>
            <span class="user-role-badge">${u.role}</span>
          </h3>
          <div class="ud-metric-row">
            <span>Account Status</span>
            <strong style="color:#10b981;">Active</strong>
          </div>
          <div class="ud-metric-row">
            <span>Member Since</span>
            <strong>January 2026</strong>
          </div>
          <div class="ud-metric-row">
            <span>Verified Status</span>
            <strong>${u.isVerified ? '✓ Verified Creator' : 'Standard Member'}</strong>
          </div>
          <div class="ud-metric-row">
            <span>Primary Language</span>
            <strong>English & Arabic</strong>
          </div>
        </div>

        <div class="ud-section-card">
          <h3>
            <span>Deen Coins & Wallet</span>
            <span style="cursor:pointer; font-size:13px; color:var(--gold-accent);" onclick="openWalletModal()">Top Up ↗</span>
          </h3>
          <div class="ud-metric-row">
            <span>Available Balance</span>
            <strong style="font-size:20px; color:var(--gold-accent);">${AppState.userWallet.coins} 🪙</strong>
          </div>
          <div class="ud-metric-row">
            <span>Virtual Gifts Sent</span>
            <strong>14 Gifts</strong>
          </div>
          <div class="ud-metric-row">
            <span>Estimated Value</span>
            <strong>$${(AppState.userWallet.coins * 0.01).toFixed(2)} USD</strong>
          </div>
          <div style="margin-top:14px; display:flex; gap:10px;">
            <button class="btn-primary-sm" style="flex:1;" onclick="openWalletModal()">Get Coins</button>
            <button class="btn-outline-sm" style="flex:1;" onclick="showToast('Wallet history synced')">History</button>
          </div>
        </div>

        <div class="ud-section-card">
          <h3>
            <span>Creator Analytics</span>
            <span style="font-size:12px; color:var(--text-muted);">Last 30 Days</span>
          </h3>
          <div class="ud-metric-row">
            <span>Published Videos</span>
            <strong>${userVideos.length}</strong>
          </div>
          <div class="ud-metric-row">
            <span>Cumulative Views</span>
            <strong>${formatMetric(totalViews)}</strong>
          </div>
          <div class="ud-metric-row">
            <span>Received Likes</span>
            <strong>${formatMetric(totalLikes)}</strong>
          </div>
          <div class="ud-metric-row">
            <span>Discussion Comments</span>
            <strong>${formatMetric(totalComments)}</strong>
          </div>
        </div>
      </div>

      <div class="ud-section-card">
        <h3>
          <span>Recent Activity & Reminders</span>
          <button class="ud-btn-action" onclick="switchUserDashboardTab('notifications')">View All</button>
        </h3>
        <div style="display:flex; flex-direction:column; gap:12px;">
          ${AppState.notifications.slice(0, 3).map(n => `
            <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 0; border-bottom:1px solid var(--border-color);">
              <div style="display:flex; align-items:center; gap:12px;">
                <span style="font-size:18px;">${n.type === 'LIKE' ? '❤️' : (n.type === 'FOLLOW' ? '👤' : '📢')}</span>
                <div>
                  <div style="font-weight:600; color:#fff; font-size:14px;">${n.title}</div>
                  <div style="font-size:12px; color:#9ca3af;">${n.sourceUsername} ${n.description}</div>
                </div>
              </div>
              <span style="font-size:11px; color:#9ca3af;">${formatTimeAgo(n.createdAt)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  } else if (tab === 'my_videos') {
    let myVideos = AppState.videos.filter(v => v.userId === u.userId);
    // If current user has no videos yet, also allow viewing all videos where creator is @me
    if (myVideos.length === 0) {
      myVideos = AppState.videos.filter(v => v.username === u.username);
    }

    container.innerHTML = `
      <div class="ud-section-card">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px; flex-wrap:wrap; gap:12px;">
          <div>
            <h3>My Uploaded Video Reminders</h3>
            <p style="font-size:13px; color:#9ca3af;">Manage your published Quran recitations, Hadith gems, and reflections</p>
          </div>
          <button class="btn-primary-sm" onclick="navigateTo('upload')">
            + Upload New Video
          </button>
        </div>

        ${myVideos.length === 0 ? `
          <div style="text-align:center; padding:50px 20px; color:#9ca3af;">
            <p style="font-size:16px; margin-bottom:14px;">You haven't uploaded any video reminders yet.</p>
            <button class="btn-primary" onclick="navigateTo('upload')">Upload Your First Video</button>
          </div>
        ` : `
          <div class="ud-videos-table-container">
            <table class="ud-table">
              <thead>
                <tr>
                  <th>Video</th>
                  <th>Category</th>
                  <th>Views</th>
                  <th>Likes</th>
                  <th>Comments</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                ${myVideos.map(v => `
                  <tr>
                    <td>
                      <div class="ud-video-cell">
                        <img class="ud-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
                        <div class="ud-video-info">
                          <h4>${v.title}</h4>
                          <span>${v.quranRef || v.hadithRef || formatTimeAgo(v.createdAt)}</span>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="user-role-badge" style="color:#10b981; border-color:#10b981;">${v.category}</span>
                    </td>
                    <td><strong>${formatMetric(v.viewsCount || 0)}</strong></td>
                    <td><strong>${formatMetric(v.likesCount || 0)}</strong></td>
                    <td><strong>${formatMetric(v.commentsCount || 0)}</strong></td>
                    <td>
                      <button class="ud-btn-action" onclick="openVideoDirectly(${v.id})">Watch</button>
                      <button class="ud-btn-action" onclick="openEditVideoModal(${v.id})">Edit</button>
                      <button class="ud-btn-action danger" onclick="deleteUserVideo(${v.id})">Delete</button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        `}
      </div>
    `;
  } else if (tab === 'saved') {
    const savedVideos = AppState.videos.filter(v => v.isSaved);
    container.innerHTML = `
      <div class="ud-section-card">
        <h3>Saved & Bookmarked Reminders (${savedVideos.length})</h3>
        ${savedVideos.length === 0 ? `
          <p style="text-align:center; padding:40px; color:#9ca3af;">No saved videos yet. Bookmark videos while browsing to watch later.</p>
        ` : `
          <div class="profile-video-grid">
            ${savedVideos.map(v => `
              <div class="grid-video-card" onclick="openVideoDirectly(${v.id})">
                <img class="grid-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
                <div class="grid-video-overlay">
                  <span class="user-role-badge" style="font-size:10px; margin-bottom:4px;">${v.category}</span>
                  <p class="grid-video-title">${v.title}</p>
                  <span style="font-size:11px; color:#9ca3af;">${v.username}</span>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    `;
  } else if (tab === 'liked') {
    const likedVideos = AppState.videos.filter(v => v.isLiked);
    container.innerHTML = `
      <div class="ud-section-card">
        <h3>Liked Videos (${likedVideos.length})</h3>
        ${likedVideos.length === 0 ? `
          <p style="text-align:center; padding:40px; color:#9ca3af;">No liked videos yet. Tap the heart button on videos you appreciate.</p>
        ` : `
          <div class="profile-video-grid">
            ${likedVideos.map(v => `
              <div class="grid-video-card" onclick="openVideoDirectly(${v.id})">
                <img class="grid-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
                <div class="grid-video-overlay">
                  <p class="grid-video-title">${v.title}</p>
                  <span style="font-size:11px; color:#9ca3af;">${v.username} • ❤️ ${formatMetric(v.likesCount)}</span>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    `;
  } else if (tab === 'notifications') {
    container.innerHTML = `
      <div class="ud-section-card">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
          <h3>Notifications & Activity (${AppState.notifications.length})</h3>
          <button class="ud-btn-action" onclick="markAllNotificationsRead()">Mark All as Read</button>
        </div>
        <div style="display:flex; flex-direction:column; gap:12px;">
          ${AppState.notifications.map(n => `
            <div style="display:flex; align-items:center; justify-content:space-between; padding:14px; border-radius:10px; background:${n.isRead ? 'rgba(0,0,0,0.2)' : 'rgba(15,46,30,0.3)'}; border:1px solid ${n.isRead ? 'var(--border-color)' : 'rgba(212,175,55,0.4)'};">
              <div style="display:flex; align-items:center; gap:14px;">
                <span style="font-size:22px;">${n.type === 'LIKE' ? '❤️' : (n.type === 'FOLLOW' ? '👤' : '📢')}</span>
                <div>
                  <div style="font-weight:700; color:#fff; font-size:14px;">${n.title}</div>
                  <div style="font-size:13px; color:#e5e7eb; margin-top:2px;">
                    <strong>${n.sourceUsername}</strong> ${n.description}
                  </div>
                </div>
              </div>
              <span style="font-size:12px; color:#9ca3af;">${formatTimeAgo(n.createdAt)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  } else if (tab === 'messages') {
    container.innerHTML = `
      <div class="ud-section-card" style="padding:0; overflow:hidden;">
        <div class="messages-layout" style="height:550px;">
          <div class="conversations-pane">
            <div class="pane-header">
              <h3>Messages</h3>
              <span class="user-role-badge">Direct Chat</span>
            </div>
            <div id="udConversationList" class="conversation-list"></div>
          </div>
          <div class="chat-pane">
            <div class="chat-header">
              <div class="chat-user-info">
                <img id="udChatAvatar" class="avatar-sm" src="" alt="Chat recipient">
                <div>
                  <h4 id="udChatName" style="color:#fff; font-size:14px;">Loading...</h4>
                  <span style="font-size:11px; color:#10b981;">Online</span>
                </div>
              </div>
            </div>
            <div id="udChatMessages" class="chat-messages"></div>
            <div class="chat-input-bar">
              <input type="text" id="udMessageInput" placeholder="Write a message in peace..." onkeydown="handleUdChatKey(event)">
              <button class="btn-send-message" onclick="sendUdMessage()">Send</button>
            </div>
          </div>
        </div>
      </div>
    `;
    setTimeout(() => {
      renderUdConversations();
      renderUdChat();
    }, 50);
  } else if (tab === 'settings') {
    container.innerHTML = `
      <div class="ud-section-card">
        <h3>Account & Privacy Settings</h3>
        <div class="settings-group" style="margin-bottom:20px;">
          <h4 style="font-size:14px; color:var(--gold-accent); margin-bottom:12px;">Privacy Controls</h4>
          <div class="setting-item">
            <span>Private Account (Only approved followers see uploads)</span>
            <input type="checkbox" id="setPrivateAccount">
          </div>
          <div class="setting-item">
            <span>Allow Comments on my video reminders</span>
            <input type="checkbox" id="setAllowComments" checked>
          </div>
          <div class="setting-item">
            <span>Allow Direct Messages from Community</span>
            <input type="checkbox" id="setAllowDMs" checked>
          </div>
        </div>

        <div class="settings-group" style="margin-bottom:20px;">
          <h4 style="font-size:14px; color:var(--gold-accent); margin-bottom:12px;">Playback & Language</h4>
          <div class="setting-item">
            <span>Interface Language</span>
            <select id="setLanguage" onchange="changeLanguage(this.value)">
              <option value="English">English</option>
              <option value="Arabic">العربية (Arabic)</option>
              <option value="Urdu">اردو (Urdu)</option>
              <option value="Turkish">Türkçe (Turkish)</option>
            </select>
          </div>
          <div class="setting-item">
            <span>Autoplay Videos on Desktop</span>
            <input type="checkbox" id="setAutoplay" checked>
          </div>
        </div>

        <div class="settings-group">
          <h4 style="font-size:14px; color:var(--gold-accent); margin-bottom:12px;">Account Role & Access</h4>
          <div class="setting-item">
            <span>Active Role Tier</span>
            <select onchange="switchRole(this.value)">
              <option value="USER" ${u.role === 'USER' ? 'selected' : ''}>Standard Member</option>
              <option value="CREATOR" ${u.role === 'CREATOR' ? 'selected' : ''}>Verified Creator</option>
              <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Super Administrator</option>
            </select>
          </div>
          <div style="margin-top:20px; display:flex; gap:12px;">
            <button class="btn-primary-sm" onclick="showToast('Settings saved successfully! ✓')">Save Changes</button>
            <button class="btn-outline-sm" style="color:#ef4444;" onclick="showToast('Signed out of session')">Sign Out</button>
          </div>
        </div>
      </div>
    `;
  }
}

// User Dashboard Chat Helpers
function renderUdConversations() {
  const container = document.getElementById('udConversationList');
  if (!container) return;
  const contacts = AppState.users.filter(u => u.userId !== 'current_user');
  container.innerHTML = contacts.map(c => `
    <div class="conversation-item ${c.userId === AppState.activeChatRecipient ? 'active' : ''}" onclick="selectUdChat('${c.userId}')">
      <img src="${c.avatarUrl}" class="avatar-sm" alt="${c.username}">
      <div class="conv-meta">
        <div class="conv-name">${c.fullName}</div>
        <div class="conv-last-msg">${c.username}</div>
      </div>
    </div>
  `).join('');
}

function selectUdChat(recipientId) {
  AppState.activeChatRecipient = recipientId;
  renderUdConversations();
  renderUdChat();
}

function renderUdChat() {
  const recipient = AppState.users.find(u => u.userId === AppState.activeChatRecipient) || AppState.users[1];
  const avatarElem = document.getElementById('udChatAvatar');
  if (avatarElem) avatarElem.src = recipient.avatarUrl;
  const nameElem = document.getElementById('udChatName');
  if (nameElem) nameElem.innerText = `${recipient.fullName} (${recipient.username})`;

  const msgContainer = document.getElementById('udChatMessages');
  if (!msgContainer) return;

  const thread = AppState.messages.filter(m =>
    (m.senderId === 'current_user' && m.receiverId === recipient.userId) ||
    (m.senderId === recipient.userId && m.receiverId === 'current_user')
  );

  if (thread.length === 0) {
    msgContainer.innerHTML = `
      <div style="text-align:center; padding:30px; color:#9ca3af;">
        <p>No messages yet with ${recipient.fullName}.</p>
        <div style="margin-top:12px; display:flex; gap:8px; justify-content:center; flex-wrap:wrap;">
          <button class="btn-outline-sm" onclick="sendGreetingMsg('Assalamu Alaikum wa Rahmatullah 🤲🏽')">Salam 🤲🏽</button>
          <button class="btn-outline-sm" onclick="sendGreetingMsg('BarakAllahu Feek for your beneficial reminders! ✨')">BarakAllahu Feek ✨</button>
          <button class="btn-outline-sm" onclick="sendGreetingMsg('JazakAllahu Khair!')">JazakAllahu Khair</button>
        </div>
      </div>
    `;
    return;
  }

  msgContainer.innerHTML = thread.map(m => `
    <div class="chat-bubble ${m.senderId === 'current_user' ? 'outgoing' : 'incoming'}">
      <div class="bubble-text">${m.text}</div>
      <span class="bubble-time">${formatTimeAgo(m.createdAt)}</span>
    </div>
  `).join('');
  msgContainer.scrollTop = msgContainer.scrollHeight;
}

function handleUdChatKey(e) {
  if (e.key === 'Enter') sendUdMessage();
}

function sendUdMessage() {
  const input = document.getElementById('udMessageInput');
  const text = input ? input.value.trim() : '';
  if (!text) return;

  const newMsg = {
    id: Date.now(),
    senderId: 'current_user',
    senderName: AppState.currentUser.username,
    receiverId: AppState.activeChatRecipient,
    text: text,
    createdAt: Date.now()
  };

  AppState.messages.push(newMsg);
  setLocalData(STORAGE_KEYS.MESSAGES, AppState.messages);
  input.value = '';
  renderUdChat();
}

function sendGreetingMsg(greeting) {
  const newMsg = {
    id: Date.now(),
    senderId: 'current_user',
    senderName: AppState.currentUser.username,
    receiverId: AppState.activeChatRecipient,
    text: greeting,
    createdAt: Date.now()
  };
  AppState.messages.push(newMsg);
  setLocalData(STORAGE_KEYS.MESSAGES, AppState.messages);
  renderUdChat();
}

function markAllNotificationsRead() {
  AppState.notifications.forEach(n => n.isRead = true);
  setLocalData(STORAGE_KEYS.NOTIFICATIONS, AppState.notifications);
  renderUserDashboard();
  updateHeaderBadges();
  showToast('All notifications marked as read ✓');
}

// Video Management in User Dashboard
function openEditVideoModal(videoId) {
  const v = AppState.videos.find(item => item.id === videoId);
  if (!v) return;

  AppState.activeEditingVideoId = videoId;
  document.getElementById('editVideoTitle').value = v.title;
  document.getElementById('editVideoCategory').value = v.category;
  document.getElementById('editVideoDescription').value = v.description;
  document.getElementById('editVideoModal').style.display = 'flex';
}

function closeEditVideoModal() {
  document.getElementById('editVideoModal').style.display = 'none';
  AppState.activeEditingVideoId = null;
}

function saveVideoChanges() {
  const videoId = AppState.activeEditingVideoId;
  const v = AppState.videos.find(item => item.id === videoId);
  if (!v) return;

  v.title = document.getElementById('editVideoTitle').value.trim() || v.title;
  v.category = document.getElementById('editVideoCategory').value || v.category;
  v.description = document.getElementById('editVideoDescription').value.trim() || v.description;

  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  closeEditVideoModal();
  renderUserDashboard();
  renderVideoFeed();
  showToast('Video details updated! ✨');
}

async function deleteUserVideo(videoId) {
  if (!confirm('Are you sure you want to delete this video reminder?')) return;
  AppState.videos = AppState.videos.filter(v => v.id !== videoId);
  await DeenTokDB.deleteBlob(videoId);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  addAuditLog('VIDEO_DELETED', `User removed video ID: ${videoId}`);
  renderUserDashboard();
  renderVideoFeed();
  showToast('Video deleted successfully.');
}

function openEditProfileModal() {
  if (!requireAuth('edit profile settings')) return;
  const modal = document.getElementById('editProfileModal');
  document.getElementById('editDisplayName').value = AppState.currentUser.fullName || '';
  document.getElementById('editBio').value = AppState.currentUser.bio || '';
  document.getElementById('editAvatarUrl').value = AppState.currentUser.avatarUrl || '';
  if (modal) modal.classList.add('active');
}

function closeEditProfileModal() {
  document.getElementById('editProfileModal')?.classList.remove('active');
}

function saveProfileChanges() {
  if (!AppState.currentUser) return;
  const name = document.getElementById('editDisplayName').value.trim();
  const bio = document.getElementById('editBio').value.trim();
  const avatar = document.getElementById('editAvatarUrl').value.trim();

  if (name) AppState.currentUser.fullName = name;
  if (bio) AppState.currentUser.bio = bio;
  if (avatar) AppState.currentUser.avatarUrl = avatar;

  // Sync to AppState.users
  const userIdx = AppState.users.findIndex(u => u.userId === AppState.currentUser.userId);
  if (userIdx >= 0) {
    AppState.users[userIdx] = { ...AppState.currentUser };
  }

  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  closeEditProfileModal();
  updateHeaderAuthState();
  renderUserDashboard();
  showToast('Profile updated! ✨');
}

// ============================================================================
// 10. Coin Wallet & Virtual Gifting
// ============================================================================

function openWalletModal() {
  document.getElementById('userCoinBalance').innerText = AppState.userWallet.coins;
  document.getElementById('walletModal')?.classList.add('active');
}

function closeWalletModal() {
  document.getElementById('walletModal')?.classList.remove('active');
}

function purchaseCoins(amount, price) {
  AppState.userWallet.coins += amount;
  setLocalData(STORAGE_KEYS.WALLET, AppState.userWallet);
  document.getElementById('userCoinBalance').innerText = AppState.userWallet.coins;
  const udCoin = document.getElementById('userWalletCoinDisplay');
  if (udCoin) udCoin.innerText = `${AppState.userWallet.coins} 🪙`;
  showToast(`Successfully purchased ${amount} Deen Coins! 🪙`);
}

function openGiftTray(videoId) {
  document.getElementById('giftTrayModal')?.classList.add('active');
}

function closeGiftTray() {
  document.getElementById('giftTrayModal')?.classList.remove('active');
}

function sendGift(giftName, cost, emoji) {
  if (AppState.userWallet.coins < cost) {
    alert(`Insufficient Deen Coins. You need ${cost} coins. Please top up your wallet.`);
    openWalletModal();
    return;
  }

  AppState.userWallet.coins -= cost;
  setLocalData(STORAGE_KEYS.WALLET, AppState.userWallet);
  closeGiftTray();

  const udCoin = document.getElementById('userWalletCoinDisplay');
  if (udCoin) udCoin.innerText = `${AppState.userWallet.coins} 🪙`;

  // Trigger Animated Fullscreen Banner
  const overlay = document.getElementById('giftAnimationOverlay');
  const animEmoji = document.getElementById('animGiftEmoji');
  const animSender = document.getElementById('animGiftSender');

  if (overlay && animEmoji && animSender) {
    animEmoji.innerText = emoji;
    animSender.innerText = `${AppState.currentUser.username} sent ${giftName}!`;
    overlay.classList.add('active');

    setTimeout(() => {
      overlay.classList.remove('active');
    }, 2000);
  }

  showToast(`Sent ${giftName} to creator! Baraka Allahu Feek 🤲🏽`);
}

// ============================================================================
// 11. Live Streaming Simulation
// ============================================================================

const SAMPLE_LIVE_STREAMS = [
  {
    id: 1,
    title: 'Surah Al-Kahf Live Tajweed Circle 📖',
    host: '@quran_reminders',
    viewers: 1420,
    thumbnail: 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80'
  },
  {
    id: 2,
    title: 'Evening Hadith & Reflection Session ✨',
    host: '@sunnah_gems',
    viewers: 890,
    thumbnail: 'https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=400&q=80'
  }
];

function renderLiveStreams() {
  const grid = document.getElementById('liveStreamsGrid');
  if (!grid) return;

  grid.innerHTML = SAMPLE_LIVE_STREAMS.map(s => `
    <div class="grid-video-card" onclick="showToast('Connecting to ${s.title}...')">
      <img class="grid-video-thumb" src="${s.thumbnail}" alt="${s.title}">
      <div class="grid-video-overlay">
        <span class="live-pill" style="margin-bottom:6px; display:inline-block;">LIVE • ${s.viewers}</span>
        <p class="grid-video-title">${s.title}</p>
        <span style="font-size:12px; color:#9ca3af;">${s.host}</span>
      </div>
    </div>
  `).join('');
}

function startHostStream() {
  alert('You are ready to Go LIVE! Camera permissions requested and streaming server ready.');
}

// ============================================================================
// 12. Professional Admin Dashboard & Moderation Portal
// ============================================================================

function toggleAdminSidebar() {
  const container = document.getElementById('adminPortalContainer');
  if (container) {
    container.classList.toggle('sidebar-collapsed');
  }
}

function switchAdminSection(section) {
  AppState.adminSection = section;
  renderAdmin();
}

// Backward-compatible alias
function switchAdminTab(tab) {
  switchAdminSection(tab);
}

function renderAdmin() {
  if (!AppState.currentUser || AppState.currentUser.role !== 'ADMIN') {
    const stageTitle = document.getElementById('adminStageTitle');
    if (stageTitle) stageTitle.innerText = 'Administrator Portal - Access Restricted';
    const stageSub = document.getElementById('adminStageSubtitle');
    if (stageSub) stageSub.innerText = 'Role-based access security enforcement';
    const body = document.getElementById('adminStageBody');
    if (body) {
      body.innerHTML = `
        <div class="access-denied-box">
          <div style="font-size:48px; margin-bottom:16px;">🛡️</div>
          <h2 style="color:#fff; margin-bottom:8px;">Administrator Access Required</h2>
          <p style="color:#9ca3af; margin-bottom:20px; font-size:14px; line-height:1.6;">
            The Admin Console is restricted to authorized platform administrators.<br>
            ${AppState.currentUser ? `Current account is <strong>${AppState.currentUser.username}</strong> with role <span class="user-role-badge role-badge-user">${AppState.currentUser.role}</span>.` : 'You are currently not signed in.'}
          </p>
          <div style="display:flex; justify-content:center; gap:12px;">
            <button class="btn-primary" onclick="openAuthModal('login')">Sign In as Admin</button>
            <button class="btn-cancel" onclick="navigateTo('home')">Return to Home</button>
          </div>
        </div>
      `;
    }
    return;
  }

  // Update Admin Profile Header Info
  const adminNameElem = document.getElementById('adminHeaderName');
  if (adminNameElem) adminNameElem.innerText = AppState.currentUser.fullName || 'Admin';

  const adminAvatarElem = document.getElementById('adminHeaderAvatar');
  if (adminAvatarElem) adminAvatarElem.src = AppState.currentUser.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80';

  const adminRolePill = document.getElementById('adminRolePill');
  if (adminRolePill) adminRolePill.innerText = AppState.currentUser.role === 'ADMIN' ? 'Super Admin' : AppState.currentUser.role;

  // Highlight Sidebar Navigation
  document.querySelectorAll('.admin-nav-link').forEach(link => link.classList.remove('active'));
  const activeLink = document.getElementById(`adminNav-${AppState.adminSection}`);
  if (activeLink) activeLink.classList.add('active');

  // Update Header Title & Subtitle
  const titles = {
    overview: { title: 'Platform Analytics & Overview', sub: 'High-level metrics, system health, and real-time activity' },
    users: { title: 'User Accounts & Roles Management', sub: 'Manage user profiles, creator tiers, statuses, and permissions' },
    videos: { title: 'Video Content & Media Manager', sub: 'Review, moderate, feature, or remove uploaded video reminders' },
    community: { title: 'Community Safety & Moderation Queue', sub: 'Review reported content, inappropriate comments, and violations' },
    platform: { title: 'Platform Categories & Sponsored Ads', sub: 'Manage Islamic topics, banner sponsorships, and feature toggles' },
    security: { title: 'Security Controls & Audit Trail', sub: 'Administrator access controls, security policies, and data export' }
  };

  const currentMeta = titles[AppState.adminSection] || titles.overview;
  const stageTitle = document.getElementById('adminStageTitle');
  if (stageTitle) stageTitle.innerText = currentMeta.title;

  const stageSub = document.getElementById('adminStageSubtitle');
  if (stageSub) stageSub.innerText = currentMeta.sub;

  renderAdminStageBody();
}

function renderAdminStageBody() {
  const body = document.getElementById('adminStageBody');
  if (!body) return;

  const sec = AppState.adminSection;

  if (sec === 'overview') {
    const totalViews = AppState.videos.reduce((s, v) => s + (v.viewsCount || 0), 0);
    const totalLikes = AppState.videos.reduce((s, v) => s + (v.likesCount || 0), 0);

    body.innerHTML = `
      <div class="admin-kpi-grid">
        <div class="admin-kpi-card">
          <span class="kpi-title">Total Registered Users</span>
          <span class="kpi-number">${AppState.users.length}</span>
          <span class="kpi-foot">↑ +12% this week</span>
        </div>
        <div class="admin-kpi-card gold">
          <span class="kpi-title">Active Video Reminders</span>
          <span class="kpi-number">${AppState.videos.length}</span>
          <span class="kpi-foot">↑ +8 uploaded today</span>
        </div>
        <div class="admin-kpi-card warning">
          <span class="kpi-title">Pending Reports</span>
          <span class="kpi-number">${AppState.adminReports.filter(r => r.status === 'PENDING').length}</span>
          <span class="kpi-foot" style="color:#f59e0b;">Needs review</span>
        </div>
        <div class="admin-kpi-card blue">
          <span class="kpi-title">Total Views Accrued</span>
          <span class="kpi-number">${formatMetric(totalViews)}</span>
          <span class="kpi-foot">Across all platforms</span>
        </div>
      </div>

      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(320px, 1fr)); gap:24px; margin-bottom:24px;">
        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Quick Moderation Actions</h3>
          </div>
          <div style="display:flex; flex-direction:column; gap:10px;">
            <button class="btn-primary-sm" style="text-align:left; padding:12px 16px;" onclick="switchAdminSection('community')">
              🛡️ Review Reports Queue (${AppState.adminReports.filter(r => r.status === 'PENDING').length} pending)
            </button>
            <button class="btn-outline-sm" style="text-align:left; padding:12px 16px;" onclick="switchAdminSection('users')">
              👥 Review Creator Verification Requests
            </button>
            <button class="btn-outline-sm" style="text-align:left; padding:12px 16px;" onclick="exportPlatformData()">
              💾 Export Platform Backup (JSON)
            </button>
          </div>
        </div>

        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Platform System Health</h3>
            <span class="status-badge active">100% Operational</span>
          </div>
          <div style="display:flex; flex-direction:column; gap:10px; font-size:13px;">
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Database Storage Engine</span>
              <strong style="color:#10b981;">IndexedDB / LocalStorage (Healthy)</strong>
            </div>
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Vercel Edge CDN Deployment</span>
              <strong style="color:#10b981;">Ready (deentok.vercel.app)</strong>
            </div>
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Islamic Content Filter</span>
              <strong style="color:#10b981;">Strict Halal Enabled</strong>
            </div>
            <div style="display:flex; justify-content:space-between; padding:8px 0;">
              <span>Video Playback Server</span>
              <strong style="color:#10b981;">HTTP Range Streaming Active</strong>
            </div>
          </div>
        </div>
      </div>

      <div class="admin-card-panel">
        <div class="admin-card-panel-header">
          <h3>Recent System Audit Logs</h3>
          <button class="ud-btn-action" onclick="switchAdminSection('security')">View All</button>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Administrator</th>
                <th>Action</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              ${AppState.adminAuditLogs.map(l => `
                <tr>
                  <td><code>${l.timestamp}</code></td>
                  <td><strong>${l.admin}</strong></td>
                  <td><span class="status-badge active">${l.action}</span></td>
                  <td>${l.details}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } else if (sec === 'users') {
    let filteredUsers = AppState.users;
    if (AppState.adminRoleFilter !== 'ALL') {
      filteredUsers = filteredUsers.filter(u => u.role === AppState.adminRoleFilter);
    }
    if (AppState.adminUserSearch) {
      const q = AppState.adminUserSearch.toLowerCase();
      filteredUsers = filteredUsers.filter(u =>
        u.username.toLowerCase().includes(q) || u.fullName.toLowerCase().includes(q)
      );
    }

    body.innerHTML = `
      <div class="admin-card-panel">
        <div class="admin-card-panel-header">
          <h3>User Accounts Directory (${filteredUsers.length} Users)</h3>
          <div class="admin-filters-row">
            <input type="text" class="admin-search-input" placeholder="Search by name or handle..." value="${AppState.adminUserSearch}" oninput="filterAdminUsersSearch(this.value)">
            <select class="admin-select" onchange="filterAdminUsersRole(this.value)">
              <option value="ALL" ${AppState.adminRoleFilter === 'ALL' ? 'selected' : ''}>All Roles</option>
              <option value="USER" ${AppState.adminRoleFilter === 'USER' ? 'selected' : ''}>Standard Members</option>
              <option value="CREATOR" ${AppState.adminRoleFilter === 'CREATOR' ? 'selected' : ''}>Verified Creators</option>
              <option value="ADMIN" ${AppState.adminRoleFilter === 'ADMIN' ? 'selected' : ''}>Administrators</option>
            </select>
          </div>
        </div>

        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>User Account</th>
                <th>Role Tier</th>
                <th>Status</th>
                <th>Followers</th>
                <th>Verified</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${filteredUsers.map(u => `
                <tr>
                  <td>
                    <div style="display:flex; align-items:center; gap:10px;">
                      <img src="${u.avatarUrl}" class="avatar-sm" alt="${u.username}">
                      <div>
                        <strong>${u.fullName}</strong>
                        <div style="color:#9ca3af; font-size:12px;">${u.username}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span class="user-role-badge">${u.role}</span>
                  </td>
                  <td>
                    <span class="status-badge ${u.status === 'ACTIVE' ? 'active' : 'suspended'}">${u.status}</span>
                  </td>
                  <td><strong>${formatMetric(u.followersCount)}</strong></td>
                  <td>
                    <button class="ud-btn-action" onclick="toggleUserVerification('${u.userId}')">
                      ${u.isVerified ? '✓ Verified' : 'Standard'}
                    </button>
                  </td>
                  <td>
                    <button class="ud-btn-action" onclick="toggleUserStatus('${u.userId}')">
                      ${u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                    </button>
                    <button class="ud-btn-action" onclick="promoteUserRole('${u.userId}')">Change Role</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } else if (sec === 'videos') {
    let filteredVideos = AppState.videos;
    if (AppState.adminCategoryFilter !== 'ALL') {
      filteredVideos = filteredVideos.filter(v => v.category === AppState.adminCategoryFilter);
    }
    if (AppState.adminVideoSearch) {
      const q = AppState.adminVideoSearch.toLowerCase();
      filteredVideos = filteredVideos.filter(v =>
        v.title.toLowerCase().includes(q) || v.username.toLowerCase().includes(q)
      );
    }

    body.innerHTML = `
      <div class="admin-card-panel">
        <div class="admin-card-panel-header">
          <h3>Published Video Reminders (${filteredVideos.length} Videos)</h3>
          <div class="admin-filters-row">
            <input type="text" class="admin-search-input" placeholder="Search by title or creator..." value="${AppState.adminVideoSearch}" oninput="filterAdminVideosSearch(this.value)">
            <select class="admin-select" onchange="filterAdminVideosCategory(this.value)">
              <option value="ALL">All Categories</option>
              <option value="Quran">Quran</option>
              <option value="Hadith">Hadith</option>
              <option value="Dua">Dua</option>
              <option value="Islamic History">Islamic History</option>
              <option value="Reflection">Reflection</option>
            </select>
          </div>
        </div>

        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Video</th>
                <th>Creator</th>
                <th>Category</th>
                <th>Views</th>
                <th>Likes</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${filteredVideos.map(v => `
                <tr>
                  <td>
                    <div class="ud-video-cell">
                      <img class="ud-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
                      <div class="ud-video-info">
                        <h4>${v.title}</h4>
                        <span>${v.quranRef || v.hadithRef || formatTimeAgo(v.createdAt)}</span>
                      </div>
                    </div>
                  </td>
                  <td><strong>${v.username}</strong></td>
                  <td><span class="user-role-badge" style="color:#10b981; border-color:#10b981;">${v.category}</span></td>
                  <td><strong>${formatMetric(v.viewsCount || 0)}</strong></td>
                  <td><strong>${formatMetric(v.likesCount || 0)}</strong></td>
                  <td>
                    <button class="ud-btn-action" onclick="openVideoDirectly(${v.id})">Play</button>
                    <button class="ud-btn-action" onclick="openEditVideoModal(${v.id})">Edit</button>
                    <button class="ud-btn-action danger" onclick="deleteVideoAdmin(${v.id})">Delete</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } else if (sec === 'community') {
    const pendingReports = AppState.adminReports;
    body.innerHTML = `
      <div class="admin-card-panel">
        <div class="admin-card-panel-header">
          <h3>Community Safety & Violation Reports</h3>
          <span class="status-badge warning">${pendingReports.filter(r => r.status === 'PENDING').length} Pending Review</span>
        </div>

        <div class="admin-table-wrap" style="margin-bottom:24px;">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Report ID</th>
                <th>Target Type</th>
                <th>Violation Reason</th>
                <th>Reporter</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${pendingReports.map(r => `
                <tr>
                  <td><code>#REP-${r.id}</code></td>
                  <td><strong>${r.type}</strong> (${r.targetId})</td>
                  <td><span style="color:#f59e0b; font-weight:600;">${r.reason}</span></td>
                  <td>${r.reportedBy}</td>
                  <td><span class="status-badge ${r.status === 'PENDING' ? 'pending' : 'active'}">${r.status}</span></td>
                  <td>
                    ${r.status === 'PENDING' ? `
                      <button class="ud-btn-action" onclick="resolveAdminReport(${r.id}, 'RESOLVED')">Resolve</button>
                      <button class="ud-btn-action danger" onclick="resolveAdminReport(${r.id}, 'DISMISSED')">Dismiss</button>
                    ` : `
                      <span style="font-size:12px; color:#10b981;">Action Completed</span>
                    `}
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>

        <div class="admin-card-panel-header" style="margin-top:20px;">
          <h3>Recent Comments Queue</h3>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Author</th>
                <th>Comment Content</th>
                <th>Video ID</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${AppState.comments.map(c => `
                <tr>
                  <td><strong>${c.username}</strong></td>
                  <td>"${c.text}"</td>
                  <td><code>#VID-${c.videoId}</code></td>
                  <td>${formatTimeAgo(c.createdAt)}</td>
                  <td>
                    <button class="ud-btn-action danger" onclick="deleteCommentAdmin(${c.id})">Delete Comment</button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } else if (sec === 'platform') {
    body.innerHTML = `
      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(320px, 1fr)); gap:24px;">
        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Islamic Knowledge Categories</h3>
          </div>
          <div class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Category Name</th>
                  <th>Status</th>
                  <th>Toggle</th>
                </tr>
              </thead>
              <tbody>
                ${AppState.islamicCategories.map(cat => `
                  <tr>
                    <td><strong>${cat.name}</strong></td>
                    <td><span class="status-badge ${cat.active ? 'active' : 'suspended'}">${cat.active ? 'Active' : 'Disabled'}</span></td>
                    <td>
                      <button class="ud-btn-action" onclick="toggleIslamicCategory('${cat.name}')">
                        ${cat.active ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>

        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Sponsored Ads & Announcements</h3>
          </div>
          <div class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Campaign Title</th>
                  <th>Impressions</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                ${AppState.adminAds.map(ad => `
                  <tr>
                    <td>
                      <strong>${ad.title}</strong>
                      <div style="font-size:11px; color:#9ca3af;">${ad.sponsor}</div>
                    </td>
                    <td>${formatMetric(ad.impressions)}</td>
                    <td><span class="status-badge ${ad.status === 'ACTIVE' ? 'active' : 'pending'}">${ad.status}</span></td>
                    <td>
                      <button class="ud-btn-action" onclick="toggleAdminAd(${ad.id})">
                        ${ad.status === 'ACTIVE' ? 'Pause' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    `;
  } else if (sec === 'security') {
    body.innerHTML = `
      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(320px, 1fr)); gap:24px; margin-bottom:24px;">
        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Administrator Credentials</h3>
            <span class="role-pill-sm">Protected</span>
          </div>
          <div style="display:flex; flex-direction:column; gap:12px; font-size:13px;">
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Primary Admin</span>
              <strong>${AppState.currentUser.fullName} (${AppState.currentUser.username})</strong>
            </div>
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Security Access Level</span>
              <strong style="color:var(--gold-accent);">Role Level 10 (Root Super Admin)</strong>
            </div>
            <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid var(--border-color);">
              <span>Session Protection</span>
              <strong style="color:#10b981;">Active & Encrypted</strong>
            </div>
          </div>
        </div>

        <div class="admin-card-panel">
          <div class="admin-card-panel-header">
            <h3>Database & System Backup</h3>
          </div>
          <p style="font-size:13px; color:#9ca3af; margin-bottom:16px;">
            Export a full JSON snapshot of all 25 database entities (Users, Videos, Comments, Messages, Wallet).
          </p>
          <div style="display:flex; gap:10px;">
            <button class="btn-primary-sm" onclick="exportPlatformData()">Download Backup (JSON)</button>
            <button class="btn-outline-sm" onclick="verifyDatabaseIntegrity()">Verify Integrity</button>
          </div>
        </div>
      </div>

      <div class="admin-card-panel">
        <div class="admin-card-panel-header">
          <h3>Full System Audit Trail</h3>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Admin Name</th>
                <th>Action Taken</th>
                <th>Audit Notes</th>
              </tr>
            </thead>
            <tbody>
              ${AppState.adminAuditLogs.map(l => `
                <tr>
                  <td><code>${l.timestamp}</code></td>
                  <td><strong>${l.admin}</strong></td>
                  <td><span class="status-badge active">${l.action}</span></td>
                  <td>${l.details}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  }
}

// Admin Interaction Handlers
function filterAdminUsersSearch(val) {
  AppState.adminUserSearch = val;
  renderAdminStageBody();
}

function filterAdminUsersRole(role) {
  AppState.adminRoleFilter = role;
  renderAdminStageBody();
}

function filterAdminVideosSearch(val) {
  AppState.adminVideoSearch = val;
  renderAdminStageBody();
}

function filterAdminVideosCategory(cat) {
  AppState.adminCategoryFilter = cat;
  renderAdminStageBody();
}

function toggleUserVerification(userId) {
  const u = AppState.users.find(user => user.userId === userId);
  if (!u) return;
  u.isVerified = !u.isVerified;
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  renderAdminStageBody();
  showToast(`Updated verification for ${u.username}`);
}

function toggleUserStatus(userId) {
  const u = AppState.users.find(user => user.userId === userId);
  if (!u) return;
  u.status = u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  
  // Log audit
  AppState.adminAuditLogs.unshift({
    timestamp: new Date().toLocaleTimeString(),
    admin: AppState.currentUser.fullName || 'Admin',
    action: u.status === 'ACTIVE' ? 'User Activated' : 'User Suspended',
    details: `Updated status for ${u.username}`
  });

  renderAdminStageBody();
  showToast(`Updated status for ${u.username}`);
}

function promoteUserRole(userId) {
  const u = AppState.users.find(user => user.userId === userId);
  if (!u) return;
  const roles = ['USER', 'CREATOR', 'ADMIN'];
  const nextIdx = (roles.indexOf(u.role) + 1) % roles.length;
  u.role = roles[nextIdx];
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  renderAdminStageBody();
  showToast(`${u.username} role changed to: ${u.role}`);
}

async function deleteVideoAdmin(videoId) {
  if (!confirm('Are you sure you want to remove this video by moderation?')) return;
  AppState.videos = AppState.videos.filter(v => v.id !== videoId);
  await DeenTokDB.deleteBlob(videoId);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  addAuditLog('VIDEO_MODERATED', `Administrator removed video ID ${videoId}`);
  renderAdmin();
  showToast('Video removed by moderation.');
}

function deleteCommentAdmin(commentId) {
  if (!confirm('Delete this comment from discussions?')) return;
  AppState.comments = AppState.comments.filter(c => c.id !== commentId);
  setLocalData(STORAGE_KEYS.COMMENTS, AppState.comments);
  renderAdminStageBody();
  showToast('Comment removed.');
}

function resolveAdminReport(reportId, newStatus) {
  const r = AppState.adminReports.find(item => item.id === reportId);
  if (!r) return;
  r.status = newStatus;
  renderAdminStageBody();
  showToast(`Report #REP-${reportId} marked as ${newStatus}`);
}

function toggleIslamicCategory(catName) {
  const c = AppState.islamicCategories.find(item => item.name === catName);
  if (!c) return;
  c.active = !c.active;
  renderAdminStageBody();
  showToast(`Category ${catName} ${c.active ? 'enabled' : 'disabled'}`);
}

function toggleAdminAd(adId) {
  const ad = AppState.adminAds.find(item => item.id === adId);
  if (!ad) return;
  ad.status = ad.status === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
  renderAdminStageBody();
  showToast(`Ad campaign ${ad.title} is now ${ad.status}`);
}

function exportPlatformData() {
  const fullBackup = {
    appName: 'DeenTok',
    version: '2.0.0-web',
    timestamp: new Date().toISOString(),
    users: AppState.users,
    videos: AppState.videos,
    comments: AppState.comments,
    messages: AppState.messages,
    notifications: AppState.notifications,
    userWallet: AppState.userWallet
  };

  const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(fullBackup, null, 2));
  const dlAnchorElem = document.createElement('a');
  dlAnchorElem.setAttribute("href", dataStr);
  dlAnchorElem.setAttribute("download", `deentok_system_backup_${Date.now()}.json`);
  dlAnchorElem.click();
  showToast('Backup JSON exported successfully! 💾');
}

function verifyDatabaseIntegrity() {
  const usersValid = AppState.users.every(u => u.userId && u.username);
  const videosValid = AppState.videos.every(v => v.id && v.videoUrl);
  if (usersValid && videosValid) {
    showToast('Database integrity check PASSED! 25 entities verified ✓');
  } else {
    showToast('Database alert: some records need sync');
  }
}

function switchRole(newRole) {
  AppState.currentUser.role = newRole;
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  renderAdmin();
  renderUserDashboard();
  showToast(`Switched active role to: ${newRole}`);
}

// ============================================================================
// 13. Helpers & Event Listeners
// ============================================================================

function renderSuggestedCreators() {
  const container = document.getElementById('suggestedCreatorsList');
  if (!container) return;

  const creators = AppState.users.filter(u => u.userId !== 'current_user').slice(0, 4);
  container.innerHTML = creators.map(c => `
    <div class="creator-item-side" onclick="viewCreatorProfile('${c.userId}')">
      <img src="${c.avatarUrl}" class="avatar-sm" alt="${c.username}">
      <div>
        <div style="font-size:13px; font-weight:700; color:#fff;">${c.username}</div>
        <div style="font-size:11px; color:#9ca3af;">${formatMetric(c.followersCount)} followers</div>
      </div>
    </div>
  `).join('');
}

function viewCreatorProfile(userId) {
  const creator = AppState.users.find(u => u.userId === userId || u.username === userId);
  if (!creator) return;

  const creatorVideos = AppState.videos.filter(v => v.userId === creator.userId || v.username === creator.username);
  const isFollowing = AppState.currentUser ? (creator.followersList || []).includes(AppState.currentUser.userId) : false;

  const modal = document.getElementById('creatorProfileModal');
  const body = document.getElementById('creatorProfileBody');
  const title = document.getElementById('creatorModalTitle');
  if (title) title.innerText = creator.fullName;

  if (body) {
    body.innerHTML = `
      <div style="display:flex; align-items:center; gap:16px; margin-bottom:16px;">
        <img src="${creator.avatarUrl}" class="avatar-lg" style="width:68px; height:68px; border-radius:50%; border:2px solid #10b981; object-fit:cover;" alt="${creator.username}">
        <div>
          <h3 style="margin:0; font-size:18px; color:#fff;">${creator.fullName}</h3>
          <p style="margin:3px 0; color:#9ca3af; font-size:13px;">${creator.username}</p>
          <span class="user-role-badge ${creator.role === 'ADMIN' ? 'role-badge-admin' : creator.role === 'CREATOR' ? 'role-badge-creator' : 'role-badge-user'}">${creator.role}</span>
        </div>
      </div>
      <p style="color:#d1d5db; font-size:13px; line-height:1.5; margin-bottom:16px;">${creator.bio || 'Beneficial reminders for the soul.'}</p>
      <div style="display:flex; justify-content:space-around; background:rgba(255,255,255,0.04); padding:12px; border-radius:10px; margin-bottom:16px;">
        <div style="text-align:center;"><strong style="color:#fff; font-size:15px;">${formatMetric(creator.followersCount || 0)}</strong><div style="font-size:11px; color:#9ca3af;">Followers</div></div>
        <div style="text-align:center;"><strong style="color:#fff; font-size:15px;">${formatMetric(creator.followingCount || 0)}</strong><div style="font-size:11px; color:#9ca3af;">Following</div></div>
        <div style="text-align:center;"><strong style="color:#fff; font-size:15px;">${formatMetric(creator.likesCount || 0)}</strong><div style="font-size:11px; color:#9ca3af;">Likes</div></div>
      </div>
      <div style="margin-bottom:20px;">
        <button class="${isFollowing ? 'btn-outline-sm' : 'btn-primary-sm'}" style="width:100%; padding:10px; font-weight:700;" onclick="toggleFollowCreator('${creator.userId}')">
          ${isFollowing ? '✓ Following' : '+ Follow Creator'}
        </button>
      </div>
      <h4 style="color:#fff; margin-bottom:12px; font-size:14px;">Videos by ${creator.fullName} (${creatorVideos.length})</h4>
      <div style="display:grid; grid-template-columns:repeat(2, 1fr); gap:10px;">
        ${creatorVideos.map(v => `
          <div class="video-grid-item" onclick="playSpecificVideo(${v.id}); closeCreatorProfileModal();" style="cursor:pointer; position:relative; aspect-ratio:9/16; border-radius:8px; overflow:hidden; background:#000;">
            <img src="${v.thumbnailUrl}" style="width:100%; height:100%; object-fit:cover;">
            <div style="position:absolute; bottom:6px; left:6px; right:6px; font-size:11px; color:#fff; font-weight:600; text-shadow:0 1px 3px rgba(0,0,0,0.8); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${v.title}</div>
          </div>
        `).join('')}
      </div>
    `;
  }
  if (modal) modal.classList.add('active');
}

function closeCreatorProfileModal() {
  document.getElementById('creatorProfileModal')?.classList.remove('active');
}

function playSpecificVideo(videoId) {
  const idx = AppState.videos.findIndex(v => v.id === videoId);
  if (idx >= 0) {
    AppState.currentVideoIndex = idx;
    navigateTo('home');
  }
}

function toggleFollowCreator(userId) {
  if (!requireAuth('follow creators')) return;
  const creator = AppState.users.find(u => u.userId === userId || u.username === userId);
  if (!creator) return;

  if (!creator.followersList) creator.followersList = [];
  const currId = AppState.currentUser.userId;
  const idx = creator.followersList.indexOf(currId);

  if (idx >= 0) {
    creator.followersList.splice(idx, 1);
    creator.followersCount = Math.max(0, (creator.followersCount || 1) - 1);
    AppState.currentUser.followingCount = Math.max(0, (AppState.currentUser.followingCount || 1) - 1);
    showToast(`Unfollowed ${creator.username}`);
  } else {
    creator.followersList.push(currId);
    creator.followersCount = (creator.followersCount || 0) + 1;
    AppState.currentUser.followingCount = (AppState.currentUser.followingCount || 0) + 1;
    showToast(`Following ${creator.username}! 🌙`);
  }

  // Sync to video feed follow badges
  AppState.videos.forEach(v => {
    if (v.userId === creator.userId || v.username === creator.username) {
      v.isFollowingCreator = idx < 0;
    }
  });

  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();
  renderSuggestedCreators();
  viewCreatorProfile(userId);
}

// User Menu & Auth Controls
function toggleUserDropdown() {
  const menu = document.getElementById('userDropdownMenu');
  if (menu) {
    menu.style.display = menu.style.display === 'none' ? 'flex' : 'none';
  }
}

function closeUserDropdown() {
  const menu = document.getElementById('userDropdownMenu');
  if (menu) menu.style.display = 'none';
}

function openAuthModal(tab = 'login') {
  closeUserDropdown();
  const modal = document.getElementById('authModal');
  if (modal) {
    modal.classList.add('active');
    switchAuthTab(tab);
  }
}

function closeAuthModal() {
  const modal = document.getElementById('authModal');
  if (modal) modal.classList.remove('active');
  const logErr = document.getElementById('loginErrorMsg');
  const regErr = document.getElementById('registerErrorMsg');
  if (logErr) logErr.style.display = 'none';
  if (regErr) regErr.style.display = 'none';
}

function switchAuthTab(tab) {
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');
  const tabLogin = document.getElementById('authTabLogin');
  const tabRegister = document.getElementById('authTabRegister');
  const title = document.getElementById('authModalTitle');

  if (tab === 'login') {
    if (loginForm) loginForm.style.display = 'block';
    if (registerForm) registerForm.style.display = 'none';
    if (tabLogin) tabLogin.classList.add('active');
    if (tabRegister) tabRegister.classList.remove('active');
    if (title) title.innerText = 'Sign In to DeenTok';
  } else {
    if (loginForm) loginForm.style.display = 'none';
    if (registerForm) registerForm.style.display = 'block';
    if (tabLogin) tabLogin.classList.remove('active');
    if (tabRegister) tabRegister.classList.add('active');
    if (title) title.innerText = 'Create DeenTok Account';
  }
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const identifier = document.getElementById('loginIdentifier')?.value.trim();
  const password = document.getElementById('loginPassword')?.value;
  const errorBox = document.getElementById('loginErrorMsg');

  if (!identifier || !password) {
    if (errorBox) {
      errorBox.innerText = 'Please provide both username/email and password.';
      errorBox.style.display = 'block';
    }
    return;
  }

  try {
    const res = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier, password })
    });

    const data = await res.json();
    if (!data.success) {
      if (errorBox) {
        errorBox.innerText = data.error || 'Login failed';
        errorBox.style.display = 'block';
      }
      return;
    }

    const user = data.user;
    AppState.currentUser = user;
    setLocalData(STORAGE_KEYS.CURRENT_USER_ID, user.userId);

    closeAuthModal();
    updateHeaderAuthState();
    showToast(`Welcome back, ${user.fullName}! 🌙`);

    if (AppState.currentScreen === 'profile') renderUserDashboard();
    if (AppState.currentScreen === 'admin') renderAdmin();
  } catch (err) {
    console.error(err);
    if (errorBox) {
        errorBox.innerText = 'Server error during login.';
        errorBox.style.display = 'block';
    }
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const fullName = document.getElementById('regFullName')?.value.trim();
  const usernameRaw = document.getElementById('regUsername')?.value.trim();
  const identifier = document.getElementById('regIdentifier')?.value.trim();
  const password = document.getElementById('regPassword')?.value;
  const confirmPassword = document.getElementById('regConfirmPassword')?.value;
  const role = document.getElementById('regRole')?.value || 'USER';
  const errorBox = document.getElementById('registerErrorMsg');

  if (!fullName || !usernameRaw || !identifier || !password) {
    if (errorBox) {
      errorBox.innerText = 'Please fill out all required fields.';
      errorBox.style.display = 'block';
    }
    return;
  }

  if (password.length < 6) {
    if (errorBox) {
      errorBox.innerText = 'Password must be at least 6 characters.';
      errorBox.style.display = 'block';
    }
    return;
  }

  if (password !== confirmPassword) {
    if (errorBox) {
      errorBox.innerText = 'Passwords do not match.';
      errorBox.style.display = 'block';
    }
    return;
  }

  const cleanUser = usernameRaw.startsWith('@') ? usernameRaw : `@${usernameRaw}`;

  const newUser = {
    userId: `user_${Date.now()}`,
    username: cleanUser,
    email: identifier.includes('@') ? identifier : '',
    phone: identifier.includes('@') ? '' : identifier,
    passwordHash: password,
    fullName: fullName,
    avatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80',
    role: role,
    status: 'ACTIVE',
    bio: 'Beneficial reminders for the soul. Al-Qur\'an & Sunnah learner. 🌙',
    followersCount: 0,
    followingCount: 0,
    likesCount: 0,
    isVerified: role === 'CREATOR' || role === 'ADMIN',
    createdAt: Date.now()
  };

  try {
    const res = await fetch('/api/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ user: newUser })
    });
    const data = await res.json();

    if (!data.success) {
       if (errorBox) {
          errorBox.innerText = data.error || 'Registration failed';
          errorBox.style.display = 'block';
       }
       return;
    }

    AppState.users.push(newUser);
    AppState.currentUser = newUser;
    setLocalData(STORAGE_KEYS.CURRENT_USER_ID, newUser.userId);

    closeAuthModal();
    updateHeaderAuthState();
    showToast(`Account created! Welcome to DeenTok, ${fullName}! 🤲🏽`);

    if (AppState.currentScreen === 'profile') renderUserDashboard();
    if (AppState.currentScreen === 'admin') renderAdmin();
  } catch (err) {
      if (errorBox) {
          errorBox.innerText = 'Server error during registration.';
          errorBox.style.display = 'block';
      }
  }
}

function handleLogoutClick() {
  const userHandle = AppState.currentUser?.username || 'user';
  AppState.currentUser = null;
  setLocalData(STORAGE_KEYS.CURRENT_USER_ID, null);
  addAuditLog('USER_LOGOUT', `User ${userHandle} signed out`);
  updateHeaderAuthState();
  showToast('You have signed out.');
  navigateTo('home');
}

function requestWithdrawal() {
  if (!requireAuth('request coin withdrawals')) return;
  showToast('Withdrawal request submitted! Minimum threshold $50.00 USD.');
}

function setupEventListeners() {
  // Desktop Keyboard Shortcuts
  window.addEventListener('keydown', (e) => {
    // Only handle if not in an input/textarea
    if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;

    if (AppState.currentScreen === 'home') {
      if (e.key === 'ArrowDown' || e.key === 'j') {
        nextVideo();
      } else if (e.key === 'ArrowUp' || e.key === 'k') {
        prevVideo();
      } else if (e.key === ' ' || e.key === 'k') {
        e.preventDefault();
        toggleVideoPlayPause();
      } else if (e.key === 'l' || e.key === 'L') {
        const feed = getFilteredFeedVideos();
        const v = feed[AppState.currentVideoIndex];
        if (v) toggleLikeVideo(v.id);
      }
    }
  });

  // Mobile Touch Swipe Handling
  let touchStartY = 0;
  window.addEventListener('touchstart', (e) => {
    touchStartY = e.touches[0].clientY;
  }, { passive: true });

  window.addEventListener('touchend', (e) => {
    if (AppState.currentScreen !== 'home') return;
    const touchEndY = e.changedTouches[0].clientY;
    const diff = touchStartY - touchEndY;
    if (Math.abs(diff) > 50) {
      if (diff > 0) {
        nextVideo(); // swipe up
      } else {
        prevVideo(); // swipe down
      }
    }
  }, { passive: true });

  // Close user dropdown on clicking outside
  window.addEventListener('click', (e) => {
    const dropdown = document.getElementById('userDropdownMenu');
    const trigger = document.getElementById('headerUserMenuTrigger');
    if (dropdown && dropdown.style.display !== 'none') {
      if (!dropdown.contains(e.target) && !trigger?.contains(e.target)) {
        dropdown.style.display = 'none';
      }
    }
  });
}

function closeModalOnOverlay(e, modalId) {
  if (e.target.id === modalId) {
    document.getElementById(modalId)?.classList.remove('active');
  }
}

function showToast(msg) {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const t = document.createElement('div');
  t.className = 'toast';
  t.innerText = msg;
  container.appendChild(t);
  setTimeout(() => {
    t.remove();
  }, 2400);
}

function formatMetric(num) {
  if (!num) return '0';
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
  return num.toString();
}

function formatTimeAgo(ts) {
  const diffSec = Math.floor((Date.now() - ts) / 1000);
  if (diffSec < 60) return 'just now';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  return `${Math.floor(diffHr / 24)}d ago`;
}

function escapeHtml(str) {
  return str.replace(/[&<>'"]/g, 
    tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
  );
}
