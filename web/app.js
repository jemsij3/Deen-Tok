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
  CURRENT_USER: 'deentok_current_user_v1'
};

const DEFAULT_USERS = [
  {
    userId: 'current_user',
    username: '@me',
    fullName: 'Jemal Jima',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
    role: 'ADMIN', // 'USER', 'CREATOR', 'ADMIN'
    status: 'ACTIVE',
    bio: 'Beneficial reminders for the soul. Al-Qur\'an & Sunnah learner. 🌙',
    followersCount: 1420,
    followingCount: 38,
    likesCount: 5210,
    isVerified: true
  },
  {
    userId: 'creator_1',
    username: '@quran_reminders',
    fullName: 'Sheikh Mishary Reminders',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
    role: 'CREATOR',
    status: 'ACTIVE',
    bio: 'Daily Quranic recitation & reflection. Verily in remembrance do hearts find rest.',
    followersCount: 28400,
    followingCount: 12,
    likesCount: 198000,
    isVerified: true
  },
  {
    userId: 'creator_2',
    username: '@sunnah_gems',
    fullName: 'Sunnah Gems Official',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
    role: 'CREATOR',
    status: 'ACTIVE',
    bio: 'Authentic Hadith of Prophet Muhammad (ﷺ). Sharing knowledge with love and mercy.',
    followersCount: 18900,
    followingCount: 45,
    likesCount: 112000,
    isVerified: true
  },
  {
    userId: 'creator_3',
    username: '@muslim_reminders',
    fullName: 'Daily Duas & Supplications',
    avatarUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=200&q=80',
    role: 'CREATOR',
    status: 'ACTIVE',
    bio: 'Power of sincere Dua. Never lose hope in the mercy of Allah. 🤲🏽',
    followersCount: 31200,
    followingCount: 20,
    likesCount: 245000,
    isVerified: false
  },
  {
    userId: 'creator_4',
    username: '@deen_history',
    fullName: 'Islamic History & Wisdom',
    avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=200&q=80',
    role: 'CREATOR',
    status: 'ACTIVE',
    bio: 'House of Wisdom, Islamic Golden Age, and inspiring stories of scholars.',
    followersCount: 9400,
    followingCount: 15,
    likesCount: 43200,
    isVerified: false
  }
];

const DEFAULT_VIDEOS = [
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
    quranRef: 'Surah Ar-Rahman (55:1-13)',
    createdAt: Date.now() - 1000 * 60 * 60 * 2
  },
  {
    id: 2,
    userId: 'creator_2',
    username: '@sunnah_gems',
    userAvatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
    videoUrl: 'https://assets.mixkit.co/videos/preview/mixkit-clouds-and-blue-sky-2408-large.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=600&q=80',
    title: 'Hadith on Kindness & Mercy ❤️',
    description: 'Prophet Muhammad (ﷺ) said: "Allah will not be merciful to those who are not merciful to people." Let\'s spread love and compassion. #hadith #sunnah #mercy',
    likesCount: 1820,
    commentsCount: 1,
    sharesCount: 98,
    viewsCount: 3100,
    isLiked: false,
    isSaved: true,
    isFollowingCreator: true,
    musicName: 'Soft Ambient Nasheed Instrumental',
    category: 'Hadith',
    hadithRef: 'Sahih al-Bukhari 7376',
    createdAt: Date.now() - 1000 * 60 * 60 * 6
  },
  {
    id: 3,
    userId: 'creator_3',
    username: '@muslim_reminders',
    userAvatarUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=200&q=80',
    videoUrl: 'https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-1610-large.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80',
    title: 'The Power of Sincere Dua 🤲🏽',
    description: 'Allah says: "Call upon Me; I will respond to you." Never give up on your prayers. Your breakthrough is coming. #dua #prayer #trust',
    likesCount: 3110,
    commentsCount: 2,
    sharesCount: 275,
    viewsCount: 6800,
    isLiked: true,
    isSaved: false,
    isFollowingCreator: false,
    musicName: 'Beautiful Dua Melodic Background',
    category: 'Dua',
    quranRef: 'Surah Ghafir (40:60)',
    createdAt: Date.now() - 1000 * 60 * 60 * 18
  },
  {
    id: 4,
    userId: 'creator_4',
    username: '@deen_history',
    userAvatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=200&q=80',
    videoUrl: 'https://assets.mixkit.co/videos/preview/mixkit-set-of-plateaus-seen-from-the-sky-in-a-sunset-26070-large.mp4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80',
    title: 'Islamic Golden Age: House of Wisdom 🏛️',
    description: 'A short journey through the history of Baghdad\'s House of Wisdom, where science, mathematics, and translation flourished. #history #knowledge #scholars',
    likesCount: 1250,
    commentsCount: 0,
    sharesCount: 42,
    viewsCount: 2400,
    isLiked: false,
    isSaved: false,
    isFollowingCreator: false,
    musicName: 'Classical Oud Background',
    category: 'Islamic History',
    quranRef: '',
    createdAt: Date.now() - 1000 * 60 * 60 * 36
  }
];

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
  adminTab: 'users'
};

// ============================================================================
// 2. Initialization & Bootstrapping
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
  initDataStore();
  setupEventListeners();
  renderApp();
  renderVideoFeed();
  renderExplorePills();
  renderSuggestedCreators();
  updateHeaderBadges();
});

function initDataStore() {
  AppState.users = getLocalData(STORAGE_KEYS.USERS, DEFAULT_USERS);
  AppState.videos = getLocalData(STORAGE_KEYS.VIDEOS, DEFAULT_VIDEOS);
  AppState.comments = getLocalData(STORAGE_KEYS.COMMENTS, DEFAULT_COMMENTS);
  AppState.messages = getLocalData(STORAGE_KEYS.MESSAGES, DEFAULT_MESSAGES);
  AppState.notifications = getLocalData(STORAGE_KEYS.NOTIFICATIONS, DEFAULT_NOTIFICATIONS);
  AppState.userWallet = getLocalData(STORAGE_KEYS.WALLET, { coins: 150 });
  AppState.currentUser = AppState.users.find(u => u.userId === 'current_user') || DEFAULT_USERS[0];

  // Save back defaults if newly initialized
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  setLocalData(STORAGE_KEYS.COMMENTS, AppState.comments);
  setLocalData(STORAGE_KEYS.MESSAGES, AppState.messages);
  setLocalData(STORAGE_KEYS.NOTIFICATIONS, AppState.notifications);
  setLocalData(STORAGE_KEYS.WALLET, AppState.userWallet);
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
  AppState.profileTab = tab;
  document.querySelectorAll('.profile-tabs .tab-btn').forEach(b => b.classList.remove('active'));
  if (tab === 'videos') document.getElementById('tabMyVideos')?.classList.add('active');
  if (tab === 'liked') document.getElementById('tabLikedVideos')?.classList.add('active');
  if (tab === 'saved') document.getElementById('tabSavedVideos')?.classList.add('active');
  renderProfileVideos();
}

function switchAdminTab(tab) {
  AppState.adminTab = tab;
  document.querySelectorAll('.admin-tabs .admin-tab').forEach(b => b.classList.remove('active'));
  event.target.classList.add('active');
  renderAdminTable();
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
  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;

  video.isLiked = !video.isLiked;
  video.likesCount += video.isLiked ? 1 : -1;

  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();

  if (video.isLiked) {
    // Add activity notification
    addNotification({
      type: 'LIKE',
      title: 'New Like!',
      description: `liked your video: "${video.title}"`,
      sourceUsername: AppState.currentUser.username
    });
    showToast('Added to Liked Videos ❤️');
  }
}

function toggleFollowVideo(videoId) {
  const video = AppState.videos.find(v => v.id === videoId);
  if (!video) return;

  video.isFollowingCreator = !video.isFollowingCreator;
  // Sync to all videos by this creator
  AppState.videos.forEach(v => {
    if (v.username === video.username) {
      v.isFollowingCreator = video.isFollowingCreator;
    }
  });

  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderVideoFeed();
  showToast(video.isFollowingCreator ? `Following ${video.username}` : `Unfollowed ${video.username}`);
}

function toggleSaveVideo(videoId) {
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
  const input = document.getElementById('newCommentInput');
  if (!input || !input.value.trim()) return;

  const newComment = {
    id: Date.now(),
    videoId: AppState.activeVideoIdForComments,
    username: AppState.currentUser.username,
    userAvatarUrl: AppState.currentUser.avatarUrl,
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
// 7. Video Upload Studio
// ============================================================================

function triggerFileInput() {
  document.getElementById('videoFileInput')?.click();
}

function handleFileSelect(e) {
  const file = e.target.files[0];
  if (file) {
    document.getElementById('uploadTitle').value = file.name.replace(/\.[^/.]+$/, "");
    showToast(`Selected file: ${file.name}`);
  }
}

function publishVideo() {
  const title = document.getElementById('uploadTitle')?.value.trim();
  const desc = document.getElementById('uploadDescription')?.value.trim() || '';
  const category = document.getElementById('uploadCategory')?.value || 'Quran';
  const music = document.getElementById('uploadMusic')?.value.trim() || 'Original Audio - DeenTok';
  const ref = document.getElementById('uploadRef')?.value.trim() || '';
  const thumbUrl = document.getElementById('uploadThumbUrl')?.value.trim() || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80';

  if (!title) {
    alert('Please enter a title for your video.');
    return;
  }

  const newVideo = {
    id: Date.now(),
    userId: AppState.currentUser.userId,
    username: AppState.currentUser.username,
    userAvatarUrl: AppState.currentUser.avatarUrl,
    videoUrl: 'https://assets.mixkit.co/videos/preview/mixkit-tree-branches-in-the-breeze-1188-large.mp4',
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

  // Clear inputs
  document.getElementById('uploadTitle').value = '';
  document.getElementById('uploadDescription').value = '';
  document.getElementById('uploadRef').value = '';

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
  const input = document.getElementById('chatMsgInput');
  if (!input || !input.value.trim()) return;

  const newMsg = {
    id: Date.now(),
    senderId: 'current_user',
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

function renderProfile() {
  const u = AppState.currentUser;
  document.getElementById('profileAvatar').src = u.avatarUrl;
  document.getElementById('profileUsername').innerText = u.username;
  document.getElementById('profileFullName').innerText = u.fullName;
  document.getElementById('profileBio').innerText = u.bio;
  document.getElementById('profileFollowingCount').innerText = u.followingCount;
  document.getElementById('profileFollowersCount').innerText = formatMetric(u.followersCount);
  document.getElementById('profileLikesCount').innerText = formatMetric(u.likesCount);

  const verifiedBadge = document.getElementById('profileVerifiedBadge');
  if (verifiedBadge) verifiedBadge.style.display = u.isVerified ? 'inline-block' : 'none';

  renderProfileVideos();
}

function renderProfileVideos() {
  const grid = document.getElementById('profileVideoGrid');
  if (!grid) return;

  let list = [];
  if (AppState.profileTab === 'videos') {
    list = AppState.videos.filter(v => v.userId === AppState.currentUser.userId);
  } else if (AppState.profileTab === 'liked') {
    list = AppState.videos.filter(v => v.isLiked);
  } else if (AppState.profileTab === 'saved') {
    list = AppState.videos.filter(v => v.isSaved);
  }

  if (list.length === 0) {
    grid.innerHTML = `<p style="grid-column:1/-1; text-align:center; padding:30px; color:#9ca3af;">No videos in this section.</p>`;
    return;
  }

  grid.innerHTML = list.map(v => `
    <div class="grid-video-card" onclick="openVideoDirectly(${v.id})">
      <img class="grid-video-thumb" src="${v.thumbnailUrl}" alt="${v.title}">
      <div class="grid-video-overlay">
        <p class="grid-video-title">${v.title}</p>
      </div>
    </div>
  `).join('');
}

function openEditProfileModal() {
  const modal = document.getElementById('editProfileModal');
  document.getElementById('editDisplayName').value = AppState.currentUser.fullName;
  document.getElementById('editBio').value = AppState.currentUser.bio;
  document.getElementById('editAvatarUrl').value = AppState.currentUser.avatarUrl;
  if (modal) modal.classList.add('active');
}

function closeEditProfileModal() {
  document.getElementById('editProfileModal')?.classList.remove('active');
}

function saveProfileChanges() {
  const name = document.getElementById('editDisplayName').value.trim();
  const bio = document.getElementById('editBio').value.trim();
  const avatar = document.getElementById('editAvatarUrl').value.trim();

  if (name) AppState.currentUser.fullName = name;
  if (bio) AppState.currentUser.bio = bio;
  if (avatar) AppState.currentUser.avatarUrl = avatar;

  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  closeEditProfileModal();
  renderProfile();
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
// 12. Admin Dashboard & Moderation
// ============================================================================

function renderAdmin() {
  document.getElementById('adminTotalUsers').innerText = AppState.users.length;
  document.getElementById('adminTotalVideos').innerText = AppState.videos.length;
  document.getElementById('adminPendingReports').innerText = '1';
  document.getElementById('adminActiveAds').innerText = '2';

  renderAdminTable();
}

function renderAdminTable() {
  const container = document.getElementById('adminTableContainer');
  if (!container) return;

  if (AppState.adminTab === 'users') {
    container.innerHTML = `
      <table class="admin-table">
        <thead>
          <tr>
            <th>User</th>
            <th>Role</th>
            <th>Status</th>
            <th>Followers</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${AppState.users.map(u => `
            <tr>
              <td><strong>${u.username}</strong><br><small style="color:#9ca3af;">${u.fullName}</small></td>
              <td>${u.role}</td>
              <td><span style="color:${u.status === 'ACTIVE' ? '#10b981' : '#ef4444'}; font-weight:700;">${u.status}</span></td>
              <td>${u.followersCount}</td>
              <td>
                <button class="btn-outline-sm" onclick="toggleUserStatus('${u.userId}')">
                  ${u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } else if (AppState.adminTab === 'videos') {
    container.innerHTML = `
      <table class="admin-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Creator</th>
            <th>Category</th>
            <th>Likes</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${AppState.videos.map(v => `
            <tr>
              <td>${v.title}</td>
              <td>${v.username}</td>
              <td>${v.category}</td>
              <td>${v.likesCount}</td>
              <td>
                <button class="btn-outline-sm" style="color:#ef4444;" onclick="deleteVideoAdmin(${v.id})">Delete</button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } else {
    container.innerHTML = `<p style="padding:20px; color:#9ca3af;">No pending items in ${AppState.adminTab}.</p>`;
  }
}

function toggleUserStatus(userId) {
  const u = AppState.users.find(user => user.userId === userId);
  if (!u) return;
  u.status = u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
  renderAdminTable();
  showToast(`Updated status for ${u.username}`);
}

function deleteVideoAdmin(videoId) {
  if (!confirm('Are you sure you want to remove this video?')) return;
  AppState.videos = AppState.videos.filter(v => v.id !== videoId);
  setLocalData(STORAGE_KEYS.VIDEOS, AppState.videos);
  renderAdmin();
  showToast('Video removed by moderation.');
}

function switchRole(newRole) {
  AppState.currentUser.role = newRole;
  setLocalData(STORAGE_KEYS.USERS, AppState.users);
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
  const u = AppState.users.find(user => user.userId === userId);
  if (u) {
    AppState.currentUser = u;
    navigateTo('profile');
  }
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
