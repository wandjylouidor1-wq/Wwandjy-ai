/**
 * Wandjy Web Multi-Agent Suite - Core JS Logic
 * Powered by pure HTML5, CSS variables, and native APIs (Web Audio, Web Speech, LocalStorage, Canvas).
 */

document.addEventListener('DOMContentLoaded', () => {
  // Initialize State
  let state = {
    activeTab: 'home',
    currentVibe: 'classic', // classic, creative, developer, zen
    threads: [],
    activeThreadId: null,
    starredMessages: [],
    publishedSites: [],
    geminiApiKey: '',
    isThinking: false
  };

  // Vibe configuration styling mappings
  const vibeThemes = {
    classic: { glow: '#00f0ff', name: 'Classic Cyber' },
    creative: { glow: '#ff007f', name: 'Creative Beats' },
    developer: { glow: '#39ff14', name: 'Developer Terminal' },
    zen: { glow: '#ffd700', name: 'Zen Tutor' }
  };

  // LocalStorage Keys
  const KEYS = {
    THREADS: 'wandjy_threads_v1',
    STARRED: 'wandjy_starred_v1',
    SITES: 'wandjy_sites_v1',
    SETTINGS: 'wandjy_settings_v1'
  };

  // Load state from local storage
  function loadPersistedState() {
    try {
      const persistedThreads = localStorage.getItem(KEYS.THREADS);
      const persistedStarred = localStorage.getItem(KEYS.STARRED);
      const persistedSites = localStorage.getItem(KEYS.SITES);
      const persistedSettings = localStorage.getItem(KEYS.SETTINGS);

      if (persistedThreads) {
        state.threads = JSON.parse(persistedThreads);
      } else {
        // Mock default threads
        state.threads = [
          {
            id: 'thread-1',
            title: 'Welcome to Wandjy Portal',
            messages: [
              { id: 'm1', sender: 'assistant', text: 'Greetings! I am Wandjy, your Multi-Agent companion. How can I assist you on the edge grid today?', timestamp: new Date().toISOString(), starred: false }
            ]
          },
          {
            id: 'thread-2',
            title: 'Web Builder Workspace',
            messages: [
              { id: 'm2', sender: 'assistant', text: 'You can build stunning, glassmorphic reactive web interfaces here and publish them instantly. Try loading a template first!', timestamp: new Date().toISOString(), starred: false }
            ]
          }
        ];
        saveThreads();
      }

      state.starredMessages = persistedStarred ? JSON.parse(persistedStarred) : [];
      state.publishedSites = persistedSites ? JSON.parse(persistedSites) : [
        { id: 'site-1', title: 'Holographic Portfolio', url: 'https://holo-design.wandjy.app', html: '<h1>Mock H1</h1>', date: '2026-07-08' },
        { id: 'site-2', title: 'Cosmic Slate Music Landing', url: 'https://slate-beats.wandjy.app', html: '<h1>Mock Slate</h1>', date: '2026-07-08' }
      ];
      if (!persistedSites) savePublishedSites();

      if (persistedSettings) {
        const settings = JSON.parse(persistedSettings);
        state.geminiApiKey = settings.apiKey || '';
        document.getElementById('gemini-key-input').value = state.geminiApiKey;
      }

      state.activeThreadId = state.threads.length > 0 ? state.threads[0].id : null;
    } catch (e) {
      console.error("Error loading local state:", e);
    }
  }

  // Save state helpers
  function saveThreads() {
    localStorage.setItem(KEYS.THREADS, JSON.stringify(state.threads));
  }
  function saveStarred() {
    localStorage.setItem(KEYS.STARRED, JSON.stringify(state.starredMessages));
  }
  function savePublishedSites() {
    localStorage.setItem(KEYS.SITES, JSON.stringify(state.publishedSites));
  }
  function saveSettings() {
    localStorage.setItem(KEYS.SETTINGS, JSON.stringify({ apiKey: state.geminiApiKey }));
  }

  // Update dynamic CSS custom variables based on active vibe theme
  function updateThemeColors() {
    const theme = vibeThemes[state.currentVibe];
    document.documentElement.style.setProperty('--active-glow', theme.glow);
    
    // Update active theme glow border and text elements
    const browserBarGlowDot = document.getElementById('secure-glow-dot');
    const headerTitleGlow = document.getElementById('header-title-glow');

    // Update aesthetic glows on elements
    if (browserBarGlowDot) {
      browserBarGlowDot.style.backgroundColor = theme.glow;
      browserBarGlowDot.style.boxShadow = `0 0 10px ${theme.glow}`;
    }
    
    // Toggle active classes on Vibe pill selectors
    document.querySelectorAll('.vibe-pill').forEach(pill => {
      if (pill.dataset.vibe === state.currentVibe) {
        pill.classList.add('border-[var(--active-glow)]', 'text-white', 'bg-white/10');
        pill.classList.remove('border-transparent', 'text-slate-400');
      } else {
        pill.classList.remove('border-[var(--active-glow)]', 'text-white', 'bg-white/10');
        pill.classList.add('border-transparent', 'text-slate-400');
      }
    });

    // Update active tab borders
    document.querySelectorAll('.tab-btn').forEach(btn => {
      if (btn.dataset.tab === state.activeTab) {
        btn.classList.add('tab-btn-active');
      } else {
        btn.classList.remove('tab-btn-active');
      }
    });
  }

  // Navigation system (client-side router)
  function navigateTo(tabName) {
    state.activeTab = tabName;
    
    // Update address bar mockup
    const routeUrls = {
      home: 'https://www.wandjy.sh/',
      chat: 'https://www.wandjy.sh/chat',
      web: 'https://www.wandjy.sh/builder',
      beats: 'https://www.wandjy.sh/beats',
      studio: 'https://www.wandjy.sh/studio',
      study: 'https://www.wandjy.sh/study',
      starred: 'https://www.wandjy.sh/saved'
    };
    document.getElementById('browser-address').value = routeUrls[tabName] || routeUrls.home;

    // Show / Hide view panels
    document.querySelectorAll('.view-tab').forEach(panel => {
      if (panel.id === `view-${tabName}`) {
        panel.classList.remove('hidden');
      } else {
        panel.classList.add('hidden');
      }
    });

    // Handle tab state rendering inside modules
    if (tabName === 'chat') {
      renderThreads();
      renderChatMessages();
    } else if (tabName === 'starred') {
      renderStarredAnswers();
    } else if (tabName === 'home') {
      renderShowreelList();
    }

    updateThemeColors();
  }

  // Event handlers for menu selection
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const tab = btn.dataset.tab;
      navigateTo(tab);
    });
  });

  // Browser navigation mockup triggers
  document.getElementById('browser-btn-back').addEventListener('click', () => {
    if (state.activeTab !== 'home') navigateTo('home');
  });
  document.getElementById('browser-btn-refresh').addEventListener('click', () => {
    const refreshBtn = document.getElementById('browser-btn-refresh-icon');
    refreshBtn.classList.add('animate-spin');
    setTimeout(() => {
      refreshBtn.classList.remove('animate-spin');
      navigateTo(state.activeTab);
    }, 500);
  });

  // Toggle API settings modal drawer
  const settingsModal = document.getElementById('settings-drawer');
  document.getElementById('cloud-console-btn').addEventListener('click', () => {
    settingsModal.classList.toggle('hidden');
  });
  document.getElementById('close-settings-btn').addEventListener('click', () => {
    settingsModal.classList.add('hidden');
  });
  document.getElementById('save-settings-btn').addEventListener('click', () => {
    const keyInput = document.getElementById('gemini-key-input').value.trim();
    state.geminiApiKey = keyInput;
    saveSettings();
    settingsModal.classList.add('hidden');
    alert("Wandjy Core Node Settings Updated Successfully.");
  });


  /* ==========================================
     MODULE 1: Home Landing Portal & Telemetry
     ========================================== */
  
  // Render Published Site list on Home Landing Carousel
  function renderShowreelList() {
    const container = document.getElementById('home-showreel-list');
    if (!container) return;
    container.innerHTML = '';
    
    if (state.publishedSites.length === 0) {
      container.innerHTML = '<div class="text-slate-500 font-mono text-xs">No active builds deployed yet. Go to Web Builder.</div>';
      return;
    }

    state.publishedSites.forEach(site => {
      const card = document.createElement('div');
      card.className = 'glass-panel p-4 rounded-xl border border-white/5 flex flex-col justify-between hover-glow cursor-pointer';
      card.innerHTML = `
        <div>
          <div class="flex items-center gap-2 mb-2">
            <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
            <h4 class="text-sm font-bold font-orbitron text-white text-ellipsis overflow-hidden whitespace-nowrap">${site.title}</h4>
          </div>
          <p class="text-xs text-slate-400 font-mono truncate mb-4">${site.url}</p>
        </div>
        <div class="flex items-center justify-between text-[10px] text-slate-500 font-mono border-t border-white/5 pt-2">
          <span>SSL Secured</span>
          <a href="${site.url}" target="_blank" class="text-cyan-400 hover:underline flex items-center gap-1">
            Launch <i class="fas fa-external-link-alt text-[8px]"></i>
          </a>
        </div>
      `;
      // Click card to load code back in editor and open tab
      card.addEventListener('click', (e) => {
        if (e.target.tagName !== 'A') {
          document.getElementById('builder-title').value = site.title;
          document.getElementById('builder-html').value = site.html || '';
          navigateTo('web');
          updateBuilderPreview();
        }
      });
      container.appendChild(card);
    });
  }

  // Draw continuous networking sine waves on canvas
  const telemetryCanvas = document.getElementById('canvas-telemetry');
  if (telemetryCanvas) {
    const ctx = telemetryCanvas.getContext('2d');
    let offset = 0;
    
    function drawTelemetry() {
      if (state.activeTab !== 'home') {
        requestAnimationFrame(drawTelemetry);
        return;
      }
      
      // Handle canvas sizing resize
      const dpr = window.devicePixelRatio || 1;
      const rect = telemetryCanvas.getBoundingClientRect();
      telemetryCanvas.width = rect.width * dpr;
      telemetryCanvas.height = rect.height * dpr;
      ctx.scale(dpr, dpr);

      ctx.clearRect(0, 0, rect.width, rect.height);
      
      // Background Grid Lines
      ctx.strokeStyle = 'rgba(255,255,255,0.02)';
      ctx.lineWidth = 1;
      for (let x = 0; x < rect.width; x += 30) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, rect.height);
        ctx.stroke();
      }
      for (let y = 0; y < rect.height; y += 30) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(rect.width, y);
        ctx.stroke();
      }

      // Live sine waves
      const amplitude = 35;
      const frequency = 0.015;
      const themeColor = vibeThemes[state.currentVibe].glow;
      
      ctx.shadowBlur = 10;
      ctx.shadowColor = themeColor;

      // First Wave (Main)
      ctx.beginPath();
      ctx.strokeStyle = themeColor;
      ctx.lineWidth = 2.5;
      for (let x = 0; x < rect.width; x++) {
        const y = rect.height / 2 + Math.sin(x * frequency + offset) * amplitude + Math.cos(x * 0.005 + offset) * 10;
        if (x === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();

      // Second wave (faded overlay)
      ctx.beginPath();
      ctx.strokeStyle = `${themeColor}40`;
      ctx.lineWidth = 1.5;
      for (let x = 0; x < rect.width; x++) {
        const y = rect.height / 2 + Math.sin(x * (frequency * 1.5) - offset) * (amplitude * 0.7);
        if (x === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();

      ctx.shadowBlur = 0; // reset
      offset += 0.035;
      requestAnimationFrame(drawTelemetry);
    }
    
    // Start telemetry loop
    drawTelemetry();
  }


  /* ==========================================
     MODULE 2: AI Multi-Agent Chat Portal
     ========================================== */

  // Vibe Selection handlers
  document.querySelectorAll('.vibe-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      state.currentVibe = pill.dataset.vibe;
      updateThemeColors();
      
      // Append info banner in current active chat if exists
      const activeThread = state.threads.find(t => t.id === state.activeThreadId);
      if (activeThread) {
        const vibeName = vibeThemes[state.currentVibe].name;
        activeThread.messages.push({
          id: 'vibe-change-' + Date.now(),
          sender: 'system',
          text: `Grid link recalibrated to: [${vibeName} Mode]`,
          timestamp: new Date().toISOString()
        });
        saveThreads();
        renderChatMessages();
      }
    });
  });

  // Render chat threads list in sidebar
  function renderThreads() {
    const threadList = document.getElementById('chat-threads-list');
    if (!threadList) return;
    threadList.innerHTML = '';

    state.threads.forEach(thread => {
      const isActive = thread.id === state.activeThreadId;
      const item = document.createElement('div');
      item.className = `group flex items-center justify-between p-3 rounded-xl cursor-pointer transition-all ${
        isActive ? 'bg-white/10 border-l-2 border-[var(--active-glow)] text-white' : 'hover:bg-white/5 text-slate-400 hover:text-slate-200'
      }`;
      
      item.innerHTML = `
        <div class="flex items-center gap-3 overflow-hidden w-4/5 select-none">
          <i class="far fa-comments text-xs text-[var(--active-glow)]"></i>
          <span class="text-xs font-bold font-display truncate">${thread.title}</span>
        </div>
        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button class="rename-thread-btn p-1 hover:text-white transition-colors" data-id="${thread.id}">
            <i class="fas fa-pencil-alt text-[10px]"></i>
          </button>
          <button class="delete-thread-btn p-1 hover:text-rose-400 transition-colors" data-id="${thread.id}">
            <i class="far fa-trash-alt text-[10px]"></i>
          </button>
        </div>
      `;

      // Select Thread
      item.addEventListener('click', (e) => {
        // Prevent trigger if clicking buttons
        if (e.target.closest('button')) return;
        state.activeThreadId = thread.id;
        renderThreads();
        renderChatMessages();
      });

      threadList.appendChild(item);
    });

    // Add Rename/Delete action listeners
    document.querySelectorAll('.rename-thread-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const threadId = btn.dataset.id;
        const thread = state.threads.find(t => t.id === threadId);
        if (!thread) return;
        const newTitle = prompt("Enter new title for conversation thread:", thread.title);
        if (newTitle && newTitle.trim()) {
          thread.title = newTitle.trim();
          saveThreads();
          renderThreads();
        }
      });
    });

    document.querySelectorAll('.delete-thread-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const threadId = btn.dataset.id;
        if (confirm("Are you sure you want to delete this chat thread?")) {
          state.threads = state.threads.filter(t => t.id !== threadId);
          if (state.activeThreadId === threadId) {
            state.activeThreadId = state.threads.length > 0 ? state.threads[0].id : null;
          }
          saveThreads();
          renderThreads();
          renderChatMessages();
        }
      });
    });
  }

  // Create new thread
  document.getElementById('new-chat-btn').addEventListener('click', () => {
    const threadId = 'thread-' + Date.now();
    const newThread = {
      id: threadId,
      title: 'New Wandjy Thread ' + (state.threads.length + 1),
      messages: [
        { id: 'm-' + Date.now(), sender: 'assistant', text: 'New dialogue node opened. Enter your prompt to begin routing.', timestamp: new Date().toISOString() }
      ]
    };
    state.threads.unshift(newThread);
    state.activeThreadId = threadId;
    saveThreads();
    renderThreads();
    renderChatMessages();
  });

  // Render active chat viewport message blocks
  function renderChatMessages() {
    const chatContainer = document.getElementById('chat-messages-container');
    if (!chatContainer) return;
    chatContainer.innerHTML = '';

    const activeThread = state.threads.find(t => t.id === state.activeThreadId);
    if (!activeThread) {
      chatContainer.innerHTML = `
        <div class="h-full flex flex-col items-center justify-center text-slate-500 font-mono text-xs gap-3">
          <i class="fas fa-lock text-lg opacity-40"></i>
          <span>Secure routing environment ready. Click "New Thread" to start.</span>
        </div>
      `;
      return;
    }

    activeThread.messages.forEach(msg => {
      if (msg.sender === 'system') {
        // Render system log bar
        const log = document.createElement('div');
        log.className = 'w-full text-center py-2';
        log.innerHTML = `<span class="bg-[#1a1d2e] text-[10px] text-slate-400 font-mono px-3 py-1 rounded-full border border-white/5 shadow-inner">${msg.text}</span>`;
        chatContainer.appendChild(log);
        return;
      }

      const isMe = msg.sender === 'user';
      const wrapper = document.createElement('div');
      wrapper.className = `flex w-full mb-4 gap-3 ${isMe ? 'justify-end' : 'justify-start'}`;

      const avatarHtml = isMe 
        ? `<div class="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center font-bold text-xs select-none">ME</div>`
        : `<div class="w-8 h-8 rounded-full bg-[var(--active-glow)]/20 border border-[var(--active-glow)]/40 flex items-center justify-center font-bold text-xs text-[var(--active-glow)] select-none">WJ</div>`;

      // Copy & Star button selectors
      const actionsHtml = isMe ? '' : `
        <div class="flex items-center gap-2 mt-2 border-t border-white/5 pt-2 text-[10px] text-slate-500 font-mono">
          <button class="copy-text-btn hover:text-white flex items-center gap-1" data-text="${msg.text.replace(/"/g, '&quot;')}">
            <i class="far fa-copy"></i> Copy
          </button>
          <button class="star-text-btn hover:text-[var(--active-glow)] flex items-center gap-1" data-id="${msg.id}">
            <i class="${msg.starred ? 'fas text-amber-400' : 'far'} fa-star"></i> ${msg.starred ? 'Starred' : 'Star'}
          </button>
          <button class="speak-text-btn hover:text-sky-400 flex items-center gap-1" data-text="${msg.text.replace(/"/g, '&quot;')}">
            <i class="fas fa-volume-up"></i> Speak
          </button>
        </div>
      `;

      wrapper.innerHTML = `
        ${!isMe ? avatarHtml : ''}
        <div class="max-w-[75%]">
          <div class="p-3.5 rounded-2xl ${
            isMe ? 'bg-cyan-500/10 border border-cyan-400/30 text-slate-100 rounded-tr-none' : 'bg-white/5 border border-white/5 text-slate-200 rounded-tl-none'
          }">
            <div class="text-sm font-display leading-relaxed whitespace-pre-line">${msg.text}</div>
            ${actionsHtml}
          </div>
          <span class="text-[9px] text-slate-500 font-mono block mt-1 ${isMe ? 'text-right' : 'text-left'}">
            ${new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </span>
        </div>
        ${isMe ? avatarHtml : ''}
      `;

      chatContainer.appendChild(wrapper);
    });

    // Auto scroll bottom
    chatContainer.scrollTop = chatContainer.scrollHeight;

    // Attach speech / action triggers
    document.querySelectorAll('.copy-text-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        navigator.clipboard.writeText(btn.dataset.text);
        alert("Copied dialogue node to secure clipboard.");
      });
    });

    document.querySelectorAll('.star-text-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const msgId = btn.dataset.id;
        toggleStarMessage(msgId);
      });
    });

    document.querySelectorAll('.speak-text-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        speakText(btn.dataset.text);
      });
    });
  }

  // Toggle star message
  function toggleStarMessage(msgId) {
    const thread = state.threads.find(t => t.id === state.activeThreadId);
    if (!thread) return;
    const msg = thread.messages.find(m => m.id === msgId);
    if (!msg) return;

    msg.starred = !msg.starred;
    saveThreads();

    if (msg.starred) {
      // Add to star lists
      state.starredMessages.push({
        id: msg.id,
        threadId: thread.id,
        threadTitle: thread.title,
        text: msg.text,
        timestamp: msg.timestamp
      });
    } else {
      state.starredMessages = state.starredMessages.filter(sm => sm.id !== msgId);
    }
    
    saveStarred();
    renderChatMessages();
    renderStarredAnswers();
  }

  // Local static generative smart response engine
  const mockResponses = {
    classic: [
      "Wandjy local routing node processed inputs successfully. Secure edge sync completed in 14ms.",
      "The client-side sandbox container is fully loaded and listening on socket 8080. Would you like me to push a test commit to the local Git repository?",
      "Analyzing request packets. Multi-Agent telemetry shows optimal core operations across all web clusters.",
      "Indeed. Netlify deployments automatically provision global edge routers, static cache routing, and serverless background workers on CDN nodes."
    ],
    creative: [
      "Let's color this cyber universe! Synthesizing digital elements with deep pink frequency beats.",
      "I generated a retro aesthetic design concept with translucent overlay headers and glassmorphic card elements.",
      "Beats Synth audio loop activated on channel 4. How about we design a neon cyberpunk web visualizer?",
      "Creativity loaded. I recommend rendering custom vector profiles using our procedural Art Studio."
    ],
    developer: [
      "Running telemetry check: \n- Memory heap: 25.4MB\n- Local variables: Loaded\n- Thread context: Green\n- Node instance: Active",
      "I recommend utilizing standard CSS grid properties instead of nested rows. Here is a cleaner approach:\n`display: grid;\ngrid-template-columns: repeat(auto-fit, minmax(280px, 1fr));`",
      "Git branch is clean. No unstaged changes on master. Do you need a compilation check on the Android app build files?",
      "To bind dynamic variables in HTML templates, configure the live preview module using standard document selector bindings."
    ],
    zen: [
      "Breathing cycle aligned. Calm mind produces the cleanest, most efficient layouts.",
      "The calculus of visual space matches the balance of the universe. Utilize generous white margins and clear text hierarchy.",
      "Let us review Kotlin function design blocks. Simplify declarations to preserve processor state.",
      "Wisdom lies in simplicity. Avoid unrequested over-complications. Every visual element must serve a high-level goal."
    ]
  };

  // Submit chat message flow
  async function submitChatMessage() {
    const chatInput = document.getElementById('chat-text-input');
    if (!chatInput) return;
    const query = chatInput.value.trim();
    if (!query) return;

    chatInput.value = '';

    const activeThread = state.threads.find(t => t.id === state.activeThreadId);
    if (!activeThread) return;

    // Push User message
    const userMsg = { id: 'm-user-' + Date.now(), sender: 'user', text: query, timestamp: new Date().toISOString() };
    activeThread.messages.push(userMsg);
    saveThreads();
    renderChatMessages();

    // Trigger AI thinking state
    state.isThinking = true;
    showThinkingIndicator(true);

    let answerText = "";

    if (state.geminiApiKey) {
      // Call actual Gemini API (Free Serverless fetch)
      try {
        const systemPrompt = `You are Wandjy, a hyper-premium AI Coding assistant & Multi-Agent suite operating in ${vibeThemes[state.currentVibe].name} mode. Please keep answers clean, expert, visually stunning, and highly professional.`;
        const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${state.geminiApiKey}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: `${systemPrompt}\n\nUser Question: ${query}` }] }]
          })
        });
        const data = await response.json();
        if (data.candidates && data.candidates[0].content.parts[0].text) {
          answerText = data.candidates[0].content.parts[0].text;
        } else {
          throw new Error("Invalid API response format");
        }
      } catch (err) {
        console.error("Gemini API call failed:", err);
        answerText = `[API Routing Error: Fallback Enabled]\nI attempted to contact the Gemini network using your provided API key, but the request failed. \n\nFallback Answer:\n${getRandomMockResponse()}`;
      }
    } else {
      // Simulate real delay and generate smart local mock response
      await new Promise(resolve => setTimeout(resolve, 1200));
      answerText = getRandomMockResponse();
    }

    // Push assistant reply
    const assistantMsg = { id: 'm-ai-' + Date.now(), sender: 'assistant', text: answerText, timestamp: new Date().toISOString(), starred: false };
    activeThread.messages.push(assistantMsg);
    saveThreads();
    
    state.isThinking = false;
    showThinkingIndicator(false);
    renderChatMessages();

    // Update system title if thread is newly initiated
    if (activeThread.messages.length <= 3 && activeThread.title.startsWith('New Wandjy Thread')) {
      activeThread.title = query.substring(0, 24) + (query.length > 24 ? '...' : '');
      saveThreads();
      renderThreads();
    }
  }

  function getRandomMockResponse() {
    const list = mockResponses[state.currentVibe];
    return list[Math.floor(Math.random() * list.length)];
  }

  function showThinkingIndicator(show) {
    const indicator = document.getElementById('chat-thinking-indicator');
    if (indicator) {
      if (show) indicator.classList.remove('hidden');
      else indicator.classList.add('hidden');
    }
    const secureGlow = document.getElementById('secure-glow-dot');
    if (secureGlow) {
      if (show) {
        secureGlow.classList.add('pulse-glow');
        secureGlow.style.backgroundColor = vibeThemes[state.currentVibe].glow;
      } else {
        secureGlow.classList.remove('pulse-glow');
        secureGlow.style.backgroundColor = '#10b981'; // emerald green idle
        secureGlow.style.boxShadow = '0 0 10px #10b981';
      }
    }
  }

  // Send bindings
  const chatSendBtn = document.getElementById('chat-send-btn');
  const chatInputEl = document.getElementById('chat-text-input');
  if (chatSendBtn && chatInputEl) {
    chatSendBtn.addEventListener('click', submitChatMessage);
    chatInputEl.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        submitChatMessage();
      }
    });
  }


  /* ==========================================
     MODULE 3: Voice Call Overlay (Speech API)
     ========================================== */
  
  const voiceOverlay = document.getElementById('voice-overlay');
  const startVoiceBtn = document.getElementById('start-voice-btn');
  const endVoiceBtn = document.getElementById('end-voice-btn');
  const voiceMicBtn = document.getElementById('voice-mic-btn');
  const voiceVisualizer = document.getElementById('canvas-voice-visualizer');
  
  let voiceActive = false;
  let voiceMicActive = false;
  let voiceVisualizerId = null;
  let speechRecognizer = null;

  // Web Speech synthesis speak helper
  function speakText(text) {
    if (!window.speechSynthesis) return;
    // Cancel prior speech
    window.speechSynthesis.cancel();
    
    // Split code blocks or filter markdown for better reading
    const cleanText = text.replace(/`[^`]*`/g, '').replace(/[\*#_\-]/g, '');
    const utterance = new SpeechSynthesisUtterance(cleanText.substring(0, 300));
    
    // Choose futuristic pitch/voice if available
    utterance.pitch = 1.15;
    utterance.rate = 1.05;
    window.speechSynthesis.speak(utterance);
  }

  // Initiate Voice Call modal overlay
  startVoiceBtn.addEventListener('click', () => {
    voiceOverlay.classList.remove('hidden');
    voiceActive = true;
    voiceMicActive = true;
    updateVoiceButtons();
    startVoiceVisualizer();
    speakText("Initiating secure audio channel. Wandjy companion connected. How may I assist your flow today?");
    startSpeechRecognition();
  });

  endVoiceBtn.addEventListener('click', () => {
    voiceOverlay.classList.add('hidden');
    voiceActive = false;
    voiceMicActive = false;
    if (window.speechSynthesis) window.speechSynthesis.cancel();
    if (speechRecognizer) speechRecognizer.stop();
    cancelAnimationFrame(voiceVisualizerId);
  });

  voiceMicBtn.addEventListener('click', () => {
    voiceMicActive = !voiceMicActive;
    updateVoiceButtons();
    if (voiceMicActive) {
      startSpeechRecognition();
    } else {
      if (speechRecognizer) speechRecognizer.stop();
    }
  });

  function updateVoiceButtons() {
    if (voiceMicActive) {
      voiceMicBtn.classList.remove('bg-red-500/10', 'border-red-500/40', 'text-red-400');
      voiceMicBtn.classList.add('bg-cyan-500/10', 'border-cyan-500/40', 'text-cyan-400');
      voiceMicBtn.innerHTML = '<i class="fas fa-microphone text-lg"></i>';
      document.getElementById('voice-status-text').innerText = 'SECURE MIC ACTIVE - SPEECH TO TEXT ARMED';
    } else {
      voiceMicBtn.classList.add('bg-red-500/10', 'border-red-500/40', 'text-red-400');
      voiceMicBtn.classList.remove('bg-cyan-500/10', 'border-cyan-500/40', 'text-cyan-400');
      voiceMicBtn.innerHTML = '<i class="fas fa-microphone-slash text-lg"></i>';
      document.getElementById('voice-status-text').innerText = 'MICROPHONE MUTED';
    }
  }

  // Draw 3D Holographic audio lines in Voice Modal
  function startVoiceVisualizer() {
    if (!voiceVisualizer) return;
    const ctx = voiceVisualizer.getContext('2d');
    let angle = 0;

    function renderVoiceFrame() {
      if (!voiceActive) return;

      const dpr = window.devicePixelRatio || 1;
      const rect = voiceVisualizer.getBoundingClientRect();
      voiceVisualizer.width = rect.width * dpr;
      voiceVisualizer.height = rect.height * dpr;
      ctx.scale(dpr, dpr);

      ctx.clearRect(0,0, rect.width, rect.height);

      const centerX = rect.width / 2;
      const centerY = rect.height / 2;
      const baseRadius = 60;
      const glowColor = vibeThemes[state.currentVibe].glow;

      // Draw concentric neon rings
      ctx.shadowBlur = 15;
      ctx.shadowColor = glowColor;

      for (let j = 0; j < 3; j++) {
        ctx.beginPath();
        ctx.strokeStyle = `${glowColor}${j === 0 ? 'FF' : j === 1 ? '70' : '30'}`;
        ctx.lineWidth = 1.5;
        
        const radius = baseRadius + j * 20;
        
        for (let i = 0; i < 360; i += 2) {
          const rad = (i * Math.PI) / 180;
          // Add rhythmic expansion
          const scale = 1 + Math.sin(angle + j * 10) * (voiceMicActive ? 0.08 : 0.02) + Math.cos(angle * 2) * 0.02;
          const x = centerX + Math.cos(rad) * radius * scale;
          const y = centerY + Math.sin(rad) * radius * scale;
          
          if (i === 0) ctx.moveTo(x, y);
          else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.stroke();
      }

      ctx.shadowBlur = 0;
      angle += 0.04;
      voiceVisualizerId = requestAnimationFrame(renderVoiceFrame);
    }

    renderVoiceFrame();
  }

  // Continuous speech recognition
  function startSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      console.warn("Speech recognition not supported in this browser.");
      return;
    }

    speechRecognizer = new SpeechRecognition();
    speechRecognizer.continuous = false;
    speechRecognizer.interimResults = false;
    speechRecognizer.lang = 'en-US';

    speechRecognizer.onstart = () => {
      console.log("Speech recognition started.");
    };

    speechRecognizer.onresult = async (event) => {
      const speechToText = event.results[0][0].transcript;
      console.log("Recognized Speech:", speechToText);

      // Render recognized query block in overlay and chat
      const subtitle = document.getElementById('voice-subtitles');
      if (subtitle) subtitle.innerText = `"${speechToText}"`;

      // Trigger automatic reply
      const activeThread = state.threads.find(t => t.id === state.activeThreadId);
      if (activeThread) {
        // user message
        activeThread.messages.push({ id: 'mv-' + Date.now(), sender: 'user', text: speechToText, timestamp: new Date().toISOString() });
        saveThreads();
        renderChatMessages();

        // AI responds
        const reply = getRandomMockResponse();
        setTimeout(() => {
          activeThread.messages.push({ id: 'mva-' + Date.now(), sender: 'assistant', text: reply, timestamp: new Date().toISOString() });
          saveThreads();
          renderChatMessages();
          speakText(reply);
        }, 1000);
      }
    };

    speechRecognizer.onerror = (e) => {
      console.error("Speech Recognition Error:", e);
    };

    speechRecognizer.onend = () => {
      // Loop recognition if mic is active and modal is open
      if (voiceActive && voiceMicActive) {
        speechRecognizer.start();
      }
    };

    speechRecognizer.start();
  }


  /* ==========================================
     MODULE 4: Live Web Editor & Sandbox
     ========================================== */

  // Quick Template Injections
  const templates = {
    cyber: {
      title: 'Neon Cyber Portfolio',
      html: `<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    body { background-color: #07080d; color: #e2e8f0; font-family: 'Space Grotesk', sans-serif; }
    .neon-card { border: 1px solid #00f0ff; box-shadow: 0 0 15px rgba(0,240,255,0.2); background: rgba(16,18,30,0.8); }
  </style>
</head>
<body class="p-8 flex flex-col items-center justify-center min-h-screen">
  <div class="neon-card p-6 rounded-2xl max-w-md text-center backdrop-blur-md">
    <h1 class="text-3xl font-extrabold text-cyan-400 mb-2 tracking-wide font-mono">WANDJY LABS</h1>
    <p class="text-xs text-slate-400 font-mono mb-4">SSL SECURED NODE // DEPLOYED</p>
    <div class="h-[2px] bg-gradient-to-r from-transparent via-cyan-400 to-transparent w-full mb-4"></div>
    <p class="text-sm text-slate-300 leading-relaxed mb-6">This interactive single-page portfolio layout was compiled inside the Wandjy Edge Builder sandbox environment instantly.</p>
    <button onclick="alert('Uplink active!')" class="px-6 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-black font-bold text-xs uppercase tracking-widest transition-all">Connect Uplink</button>
  </div>
</body>
</html>`
    },
    glass: {
      title: 'Glassmorphic Portal Dashboard',
      html: `<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    body { background: radial-gradient(circle at top left, #1e1b4b, #030712); min-height: 100vh; color: white; }
  </style>
</head>
<body class="p-8 flex items-center justify-center">
  <div class="backdrop-blur-xl bg-white/5 border border-white/10 p-8 rounded-3xl max-w-lg shadow-2xl">
    <div class="flex items-center gap-3 mb-6">
      <div class="w-10 h-10 rounded-full bg-pink-500/20 border border-pink-400 flex items-center justify-center text-pink-400 font-bold">✨</div>
      <div>
        <h2 class="text-xl font-bold tracking-tight">Ethereal Glassmorphism</h2>
        <p class="text-xs text-pink-400 font-semibold uppercase tracking-wider">Design Standard 3.0</p>
      </div>
    </div>
    <p class="text-sm text-slate-300 leading-relaxed mb-6">Leverage backdrop filters with semi-transparent borders to achieve high-depth overlay layout structures without blocking viewport gradients.</p>
    <div class="grid grid-cols-2 gap-4">
      <div class="bg-white/5 border border-white/5 p-4 rounded-2xl text-center hover:bg-white/10 transition-colors cursor-pointer">
        <span class="block text-lg font-bold text-pink-400">99.8%</span>
        <span class="text-[10px] text-slate-400 uppercase tracking-widest">Glow Index</span>
      </div>
      <div class="bg-white/5 border border-white/5 p-4 rounded-2xl text-center hover:bg-white/10 transition-colors cursor-pointer">
        <span class="block text-lg font-bold text-indigo-400">14ms</span>
        <span class="text-[10px] text-slate-400 uppercase tracking-widest">Router ping</span>
      </div>
    </div>
  </div>
</body>
</html>`
    },
    retro: {
      title: 'Retro Cyberpunk Terminal',
      html: `<!DOCTYPE html>
<html>
<head>
  <style>
    body { background-color: black; color: #39ff14; font-family: 'Courier New', monospace; p-8; padding: 40px; }
    .cursor { display: inline-block; width: 8px; height: 15px; background: #39ff14; animation: blink 1s infinite; }
    @keyframes blink { 0%, 100% { opacity: 0; } 50% { opacity: 1; } }
  </style>
</head>
<body>
  <div style="border: 2px solid #39ff14; padding: 20px; border-radius: 4px; box-shadow: 0 0 15px rgba(57,255,20,0.3);">
    <div>WANDJY SYSTEM CORE BOOT DATA // CODENAME: GHOST-SHELL</div>
    <div style="margin: 10px 0; color: #888;">------------------------------------------------------</div>
    <div style="margin-bottom: 20px;">[OK] LOADING KERNEL SYSTEM DRIVERS<br>[OK] INITIATING SECURE SOCKET PROTOCOLS<br>[OK] NETLIFY EDGE TUNNEL CONNECTED SUCCESSFUL</div>
    <div>$ root_access --bypass_router<span class="cursor"></span></div>
  </div>
</body>
</html>`
    }
  };

  // Injects template into editor
  document.querySelectorAll('.template-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const type = btn.dataset.template;
      const tmpl = templates[type];
      if (tmpl) {
        document.getElementById('builder-title').value = tmpl.title;
        document.getElementById('builder-html').value = tmpl.html;
        updateBuilderPreview();
      }
    });
  });

  // Render HTML code inside preview Iframe in real time
  function updateBuilderPreview() {
    const htmlCode = document.getElementById('builder-html').value;
    const iframe = document.getElementById('builder-iframe-preview');
    if (!iframe) return;

    const doc = iframe.contentDocument || iframe.contentWindow.document;
    doc.open();
    doc.write(htmlCode);
    doc.close();
  }

  // Key event listeners for realtime typing preview
  document.getElementById('builder-html').addEventListener('input', updateBuilderPreview);

  // Publish Code layout to Local state showreel list
  document.getElementById('builder-publish-btn').addEventListener('click', () => {
    const titleInput = document.getElementById('builder-title');
    const htmlCode = document.getElementById('builder-html').value.trim();
    const title = titleInput.value.trim() || 'Untitled Sandbox Project';

    if (!htmlCode) {
      alert("Please compile some valid layout code before publishing.");
      return;
    }

    const cleanTitleSlug = title.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9\-]/g, '');
    const siteUrl = `https://${cleanTitleSlug || 'sandbox'}.wandjy.app`;

    const newSite = {
      id: 'site-' + Date.now(),
      title: title,
      url: siteUrl,
      html: htmlCode,
      date: new Date().toISOString().split('T')[0]
    };

    state.publishedSites.unshift(newSite);
    savePublishedSites();
    alert(`Build Hosted Successfully on local CDN node:\n${siteUrl}`);
    
    // Auto sync Home landing listings
    renderShowreelList();
    navigateTo('home');
  });


  /* ==========================================
     MODULE 5: Beats & Synthesizer Studio
     ========================================== */

  let audioCtx = null;
  let synthOscillator = null;
  let filterNode = null;
  let mainGainNode = null;
  
  // Beats looping configurations
  let loopInterval = null;
  let loopRunning = false;
  let loopStep = 0;
  
  const pads = {
    bass: Array(8).fill(false),
    snare: Array(8).fill(false),
    hat: Array(8).fill(false),
    seq: Array(8).fill(false)
  };

  // Setup Web Audio Context safely
  function initAudio() {
    if (audioCtx) return;
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    
    // Create filters and routing controls
    filterNode = audioCtx.createBiquadFilter();
    filterNode.type = 'lowpass';
    filterNode.frequency.value = 1200;
    filterNode.Q.value = 1;

    mainGainNode = audioCtx.createGain();
    mainGainNode.gain.value = 0.5;

    // Route: Osc -> Filter -> Gain -> Destination
    filterNode.connect(mainGainNode);
    mainGainNode.connect(audioCtx.destination);
  }

  // Play continuous active frequency note on keyboard hit
  function playNote(frequency) {
    initAudio();
    if (audioCtx.state === 'suspended') audioCtx.resume();

    // Prevent overlap
    if (synthOscillator) {
      try { synthOscillator.stop(); } catch(e) {}
    }

    const oscType = document.getElementById('synth-waveform').value;
    const filterFreq = document.getElementById('synth-filter').value;
    const volume = document.getElementById('synth-volume').value;

    filterNode.frequency.setValueAtTime(filterFreq, audioCtx.currentTime);
    mainGainNode.gain.setValueAtTime(volume, audioCtx.currentTime);

    // Create Oscillator
    synthOscillator = audioCtx.createOscillator();
    synthOscillator.type = oscType;
    synthOscillator.frequency.value = frequency;

    // Routing
    synthOscillator.connect(filterNode);
    
    // Trigger envelope attack
    const attack = 0.05;
    mainGainNode.gain.setValueAtTime(0, audioCtx.currentTime);
    mainGainNode.gain.linearRampToValueAtTime(volume, audioCtx.currentTime + attack);

    synthOscillator.start();

    // Visual feedback loop synced with frequency
    triggerVisualizerKick();
  }

  function stopNote() {
    if (!synthOscillator) return;
    const release = 0.15;
    try {
      mainGainNode.gain.setValueAtTime(mainGainNode.gain.value, audioCtx.currentTime);
      mainGainNode.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + release);
      setTimeout(() => {
        try { synthOscillator.stop(); } catch(e){}
      }, release * 1000);
    } catch (e) {}
  }

  // Keyboard note buttons trigger bindings
  document.querySelectorAll('.synth-key').forEach(key => {
    const freq = parseFloat(key.dataset.freq);
    key.addEventListener('mousedown', () => {
      playNote(freq);
      key.classList.add('active');
    });
    key.addEventListener('mouseup', () => {
      stopNote();
      key.classList.remove('active');
    });
    key.addEventListener('mouseleave', () => {
      stopNote();
      key.classList.remove('active');
    });
  });

  // Background Drum loop setup
  const playLoopBtn = document.getElementById('beats-loop-play-btn');
  if (playLoopBtn) {
    playLoopBtn.addEventListener('click', () => {
      initAudio();
      loopRunning = !loopRunning;
      if (loopRunning) {
        playLoopBtn.innerHTML = '<i class="fas fa-pause mr-2"></i> Pause Beat';
        playLoopBtn.classList.remove('bg-emerald-500/20', 'text-emerald-400');
        playLoopBtn.classList.add('bg-rose-500/20', 'text-rose-400');
        startLoopEngine();
      } else {
        playLoopBtn.innerHTML = '<i class="fas fa-play mr-2"></i> Play Beat';
        playLoopBtn.classList.add('bg-emerald-500/20', 'text-emerald-400');
        playLoopBtn.classList.remove('bg-rose-500/20', 'text-rose-400');
        stopLoopEngine();
      }
    });
  }

  // Set up pads loop grids click state
  document.querySelectorAll('.beat-pad').forEach(pad => {
    pad.addEventListener('click', () => {
      const row = pad.dataset.row;
      const col = parseInt(pad.dataset.col);
      pads[row][col] = !pads[row][col];
      
      if (pads[row][col]) {
        pad.classList.add('bg-[var(--active-glow)]/40', 'border-[var(--active-glow)]');
        pad.classList.remove('bg-[#141624]');
      } else {
        pad.classList.remove('bg-[var(--active-glow)]/40', 'border-[var(--active-glow)]');
        pad.classList.add('bg-[#141624]');
      }
    });
  });

  function startLoopEngine() {
    const tempo = 120; // BPM
    const stepTime = (60 / tempo) / 2; // eighth notes
    loopStep = 0;
    
    loopInterval = setInterval(() => {
      triggerStep();
    }, stepTime * 1000);
  }

  function stopLoopEngine() {
    clearInterval(loopInterval);
    document.querySelectorAll('.beat-pad-indicator').forEach(ind => ind.classList.add('invisible'));
  }

  function triggerStep() {
    // Render loop tracker step light indicator
    document.querySelectorAll('.beat-pad-indicator').forEach((ind, i) => {
      if (i === loopStep) ind.classList.remove('invisible');
      else ind.classList.add('invisible');
    });

    // Sound Syntheses for Pads
    if (pads.bass[loopStep]) playKickSound();
    if (pads.snare[loopStep]) playSnareSound();
    if (pads.hat[loopStep]) playHatSound();
    if (pads.seq[loopStep]) playSeqSound();

    loopStep = (loopStep + 1) % 8;
  }

  // Synthetic sound engines (Web Audio API)
  function playKickSound() {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.frequency.setValueAtTime(150, audioCtx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3);
    gain.gain.setValueAtTime(1, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3);

    osc.start();
    osc.stop(audioCtx.currentTime + 0.3);
    triggerVisualizerKick();
  }

  function playSnareSound() {
    // Snare synthesis noise oscillator
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.type = 'triangle';
    osc.frequency.setValueAtTime(180, audioCtx.currentTime);
    gain.gain.setValueAtTime(0.7, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.2);

    osc.start();
    osc.stop(audioCtx.currentTime + 0.2);
  }

  function playHatSound() {
    // Simple high pass noise click
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.type = 'sine';
    osc.frequency.setValueAtTime(10000, audioCtx.currentTime);
    gain.gain.setValueAtTime(0.3, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.05);

    osc.start();
    osc.stop(audioCtx.currentTime + 0.05);
  }

  function playSeqSound() {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.type = 'sawtooth';
    const notes = [261.63, 293.66, 329.63, 349.23, 392.00]; // C4 Scale
    const randomNote = notes[Math.floor(Math.random() * notes.length)];
    osc.frequency.setValueAtTime(randomNote, audioCtx.currentTime);
    
    gain.gain.setValueAtTime(0.2, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.15);

    osc.start();
    osc.stop(audioCtx.currentTime + 0.15);
  }

  // Interactive frequency spectrum analyzer canvas
  const synthCanvas = document.getElementById('canvas-beats-freq');
  let visualizerKick = 0;
  if (synthCanvas) {
    const ctx = synthCanvas.getContext('2d');
    
    function drawFrequencyAnalyzer() {
      if (state.activeTab !== 'beats') {
        requestAnimationFrame(drawFrequencyAnalyzer);
        return;
      }

      const dpr = window.devicePixelRatio || 1;
      const rect = synthCanvas.getBoundingClientRect();
      synthCanvas.width = rect.width * dpr;
      synthCanvas.height = rect.height * dpr;
      ctx.scale(dpr, dpr);

      ctx.clearRect(0,0, rect.width, rect.height);
      
      const barCount = 18;
      const barWidth = (rect.width / barCount) - 3;
      const themeColor = vibeThemes[state.currentVibe].glow;

      ctx.shadowBlur = 8;
      ctx.shadowColor = themeColor;

      for (let i = 0; i < barCount; i++) {
        // Compute procedural height based on steps and triggers
        let rVal = Math.sin(i * 0.5 + Date.now()*0.005) * 15 + 20;
        if (visualizerKick > 0) rVal += Math.random() * visualizerKick * 30;

        const h = Math.max(4, Math.min(rect.height - 10, rVal));
        const x = i * (barWidth + 3);
        const y = rect.height - h;

        ctx.fillStyle = themeColor;
        // Rounded bar look
        ctx.fillRect(x, y, barWidth, h);
      }

      ctx.shadowBlur = 0;
      if (visualizerKick > 0) visualizerKick -= 0.1; // decay

      requestAnimationFrame(drawFrequencyAnalyzer);
    }

    drawFrequencyAnalyzer();
  }

  function triggerVisualizerKick() {
    visualizerKick = 1.0;
  }


  /* ==========================================
     MODULE 6: Photography & Art Studio
     ========================================== */
  
  const drawCanvas = document.getElementById('canvas-art-board');
  let drawing = false;
  let drawColor = '#00f0ff';
  let drawWidth = 5;
  let glowDrawMode = false;
  let drawHistory = [];

  if (drawCanvas) {
    const ctx = drawCanvas.getContext('2d');
    
    // Set internal size matching display dimensions
    function resizeDrawCanvas() {
      const rect = drawCanvas.getBoundingClientRect();
      drawCanvas.width = rect.width;
      drawCanvas.height = rect.height;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      clearArtBoard();
    }
    resizeDrawCanvas();

    // Canvas drawing coordinates helper
    function getCoords(e) {
      const rect = drawCanvas.getBoundingClientRect();
      if (e.touches && e.touches[0]) {
        return {
          x: e.touches[0].clientX - rect.left,
          y: e.touches[0].clientY - rect.top
        };
      }
      return {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      };
    }

    // Drawing triggers
    function startDrawing(e) {
      drawing = true;
      const coords = getCoords(e);
      ctx.beginPath();
      ctx.moveTo(coords.x, coords.y);
    }

    function draw(e) {
      if (!drawing) return;
      e.preventDefault();
      const coords = getCoords(e);

      ctx.strokeStyle = drawColor;
      ctx.lineWidth = drawWidth;

      if (glowDrawMode) {
        ctx.shadowBlur = 10;
        ctx.shadowColor = drawColor;
      } else {
        ctx.shadowBlur = 0;
      }

      ctx.lineTo(coords.x, coords.y);
      ctx.stroke();
    }

    function stopDrawing() {
      if (drawing) {
        ctx.closePath();
        drawing = false;
        // Push state to undo stack
        drawHistory.push(ctx.getImageData(0,0, drawCanvas.width, drawCanvas.height));
      }
    }

    // Event listeners
    drawCanvas.addEventListener('mousedown', startDrawing);
    drawCanvas.addEventListener('mousemove', draw);
    drawCanvas.addEventListener('mouseup', stopDrawing);
    drawCanvas.addEventListener('mouseleave', stopDrawing);

    drawCanvas.addEventListener('touchstart', startDrawing);
    drawCanvas.addEventListener('touchmove', draw);
    drawCanvas.addEventListener('touchend', stopDrawing);

    // Color controls
    document.getElementById('art-brush-color').addEventListener('input', (e) => {
      drawColor = e.target.value;
    });
    document.getElementById('art-brush-size').addEventListener('input', (e) => {
      drawWidth = e.target.value;
    });
    document.getElementById('art-glow-toggle').addEventListener('change', (e) => {
      glowDrawMode = e.target.checked;
    });

    document.getElementById('art-clear-btn').addEventListener('click', clearArtBoard);
    document.getElementById('art-undo-btn').addEventListener('click', () => {
      if (drawHistory.length > 0) {
        drawHistory.pop(); // remove current state
        if (drawHistory.length > 0) {
          ctx.putImageData(drawHistory[drawHistory.length - 1], 0, 0);
        } else {
          clearArtBoard();
        }
      }
    });

    function clearArtBoard() {
      ctx.clearRect(0,0, drawCanvas.width, drawCanvas.height);
      ctx.fillStyle = '#0a0a12';
      ctx.fillRect(0,0, drawCanvas.width, drawCanvas.height);
    }

    // Avatar generator compilation
    document.getElementById('art-generate-avatar-btn').addEventListener('click', () => {
      clearArtBoard();
      const glowColor = vibeThemes[state.currentVibe].glow;
      
      ctx.shadowBlur = 0;
      // Draw background gradient circles
      const grad = ctx.createRadialGradient(drawCanvas.width/2, drawCanvas.height/2, 20, drawCanvas.width/2, drawCanvas.height/2, 120);
      grad.addColorStop(0, `${glowColor}30`);
      grad.addColorStop(1, '#0a0a12');
      ctx.fillStyle = grad;
      ctx.fillRect(0,0, drawCanvas.width, drawCanvas.height);

      // Face base
      ctx.fillStyle = '#1e293b';
      ctx.strokeStyle = glowColor;
      ctx.lineWidth = 3;
      ctx.shadowBlur = 15;
      ctx.shadowColor = glowColor;

      ctx.beginPath();
      ctx.arc(drawCanvas.width/2, drawCanvas.height/2 + 10, 50, 0, Math.PI * 2);
      ctx.fill();
      ctx.stroke();

      // Cyber Glasses (Procedural lines)
      ctx.strokeStyle = '#ff007f';
      ctx.shadowColor = '#ff007f';
      ctx.fillStyle = 'rgba(255,0,127,0.15)';
      ctx.lineWidth = 2.5;
      ctx.beginPath();
      // left lens
      ctx.moveTo(drawCanvas.width/2 - 35, drawCanvas.height/2);
      ctx.lineTo(drawCanvas.width/2 - 5, drawCanvas.height/2);
      ctx.lineTo(drawCanvas.width/2 - 12, drawCanvas.height/2 + 15);
      ctx.lineTo(drawCanvas.width/2 - 30, drawCanvas.height/2 + 15);
      ctx.closePath();
      ctx.fill();
      ctx.stroke();

      // right lens
      ctx.beginPath();
      ctx.moveTo(drawCanvas.width/2 + 5, drawCanvas.height/2);
      ctx.lineTo(drawCanvas.width/2 + 35, drawCanvas.height/2);
      ctx.lineTo(drawCanvas.width/2 + 30, drawCanvas.height/2 + 15);
      ctx.lineTo(drawCanvas.width/2 + 12, drawCanvas.height/2 + 15);
      ctx.closePath();
      ctx.fill();
      ctx.stroke();

      // Center link
      ctx.beginPath();
      ctx.moveTo(drawCanvas.width/2 - 5, drawCanvas.height/2 + 4);
      ctx.lineTo(drawCanvas.width/2 + 5, drawCanvas.height/2 + 4);
      ctx.stroke();

      // Cyber horn extensions
      ctx.strokeStyle = glowColor;
      ctx.shadowColor = glowColor;
      ctx.lineWidth = 4;
      ctx.beginPath();
      // left horn
      ctx.moveTo(drawCanvas.width/2 - 30, drawCanvas.height/2 - 35);
      ctx.quadraticCurveTo(drawCanvas.width/2 - 45, drawCanvas.height/2 - 65, drawCanvas.width/2 - 35, drawCanvas.height/2 - 75);
      // right horn
      ctx.moveTo(drawCanvas.width/2 + 30, drawCanvas.height/2 - 35);
      ctx.quadraticCurveTo(drawCanvas.width/2 + 45, drawCanvas.height/2 - 65, drawCanvas.width/2 + 35, drawCanvas.height/2 - 75);
      ctx.stroke();

      // Glowing details
      ctx.fillStyle = glowColor;
      ctx.beginPath();
      ctx.arc(drawCanvas.width/2, drawCanvas.height/2 - 20, 4, 0, Math.PI*2);
      ctx.fill();

      ctx.shadowBlur = 0; // reset
      drawHistory.push(ctx.getImageData(0,0, drawCanvas.width, drawCanvas.height));
      alert("Holographic Cyber Avatar Procedurally Rendered.");
    });

    // Export sketch asset PNG
    document.getElementById('art-download-btn').addEventListener('click', () => {
      const dataUrl = drawCanvas.toDataURL('image/png');
      const link = document.createElement('a');
      link.download = 'wandjy-art-render.png';
      link.href = dataUrl;
      link.click();
    });
  }


  /* ==========================================
     MODULE 7: AI Homework Tutor & Sandbox
     ========================================== */

  const tutorials = {
    var: {
      desc: 'Declare variables in Kotlin using standard val (immutable) and var (mutable) keywords.',
      code: `fun main() {
  val appName = "Wandjy Web"
  var buildLatency = 14
  
  println("Uplink: " + appName)
  println("Latency: " + buildLatency + "ms")
  
  // Re-assigning var mutable node
  buildLatency = 8
  println("Optimized Latency: " + buildLatency + "ms")
}`
    },
    loop: {
      desc: 'Iterate loops securely in Kotlin ranges and array list indicators.',
      code: `fun main() {
  val edgeNodes = listOf("US-East", "EU-West", "AP-South")
  
  println("--- Active Routing Nodes ---")
  for (node in edgeNodes) {
    println("Node Link: " + node + " (Secure SSL)")
  }
}`
    },
    compose: {
      desc: 'Create highly responsive state UI blocks using Jetpack Compose layout trees.',
      code: `@Composable
fun WandjyLogoWidget(glowColor: Color) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(12.dp)
  ) {
    Text("wandjy", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.width(4.dp))
    Box(modifier = Modifier.background(glowColor)) {
      Text("EDGE", color = Color.Black, fontSize = 10.sp)
    }
  }
}`
    }
  };

  // Preload Tutor guide buttons
  document.querySelectorAll('.tutor-tutorial-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const key = btn.dataset.key;
      const tut = tutorials[key];
      if (tut) {
        document.getElementById('tutor-code-editor').value = tut.code;
        document.getElementById('tutor-tutorial-desc').innerText = tut.desc;
        // Reset terminal
        document.getElementById('tutor-terminal-output').innerText = 'System Terminal Idle. Press "Execute Code Node".';
      }
    });
  });

  // Run mock JVM compiler action
  document.getElementById('tutor-run-btn').addEventListener('click', () => {
    const code = document.getElementById('tutor-code-editor').value;
    const outputTerminal = document.getElementById('tutor-terminal-output');
    
    outputTerminal.innerText = '[COMPILING] Initializing Kotlin Compiler Core...';
    
    // Simulate compilation delay
    setTimeout(() => {
      // Analyze code text to give intelligent dynamic compiler mock answers
      let simulatedOutput = "";
      if (code.includes('buildLatency = 8')) {
        simulatedOutput = "Uplink: Wandjy Web\nLatency: 14ms\nOptimized Latency: 8ms\n\n[SUCCESS] Build Execution Finished in 120ms.";
      } else if (code.includes('edgeNodes')) {
        simulatedOutput = "--- Active Routing Nodes ---\nNode Link: US-East (Secure SSL)\nNode Link: EU-West (Secure SSL)\nNode Link: AP-South (Secure SSL)\n\n[SUCCESS] Loop execution completed successfully.";
      } else if (code.includes('@Composable')) {
        simulatedOutput = "[JVM-UI RENDER] Jetpack Compose Frame Synthesized successfully.\nBound modifiers: padding=12.dp, size=auto\nChildren classes initialized: [Row, Text, Spacer, Box, Text]\n\n[SUCCESS] Preview Canvas generated successfully.";
      } else {
        // Fallback execution
        simulatedOutput = "Running user-compiled Kotlin instructions... \nOutput log:\nHello from Wandjy Sandbox Sandbox Terminal!\n\n[SUCCESS] Execution finished without warnings.";
      }

      outputTerminal.innerText = simulatedOutput;
    }, 800);
  });

  // Pendulum physics canvas simulation
  const physicsCanvas = document.getElementById('canvas-physics');
  if (physicsCanvas) {
    const ctx = physicsCanvas.getContext('2d');
    let angle = Math.PI/4; // 45 degrees
    let angleVelocity = 0.0;
    let angleAcceleration = 0.0;
    const length = 120; // rod length
    const gravity = 0.4; // custom gravity slider input
    const damping = 0.995; // tiny drag

    function drawPendulum() {
      if (state.activeTab !== 'study') {
        requestAnimationFrame(drawPendulum);
        return;
      }

      const dpr = window.devicePixelRatio || 1;
      const rect = physicsCanvas.getBoundingClientRect();
      physicsCanvas.width = rect.width * dpr;
      physicsCanvas.height = rect.height * dpr;
      ctx.scale(dpr, dpr);

      ctx.clearRect(0,0, rect.width, rect.height);

      const originX = rect.width / 2;
      const originY = 20;

      // Compute physical state equation
      angleAcceleration = (-1 * gravity / length) * Math.sin(angle);
      angleVelocity += angleAcceleration;
      angleVelocity *= damping;
      angle += angleVelocity;

      // Compute Cartesian coordinates for bob position
      const bobX = originX + length * Math.sin(angle);
      const bobY = originY + length * Math.cos(angle);

      // Rod line
      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.moveTo(originX, originY);
      ctx.lineTo(bobX, bobY);
      ctx.stroke();

      // Top anchor
      ctx.fillStyle = '#64748b';
      ctx.beginPath();
      ctx.arc(originX, originY, 6, 0, Math.PI*2);
      ctx.fill();

      // Glowing bob
      const themeColor = vibeThemes[state.currentVibe].glow;
      ctx.shadowBlur = 15;
      ctx.shadowColor = themeColor;
      ctx.fillStyle = themeColor;
      ctx.beginPath();
      ctx.arc(bobX, bobY, 14, 0, Math.PI*2);
      ctx.fill();
      ctx.shadowBlur = 0;

      requestAnimationFrame(drawPendulum);
    }

    drawPendulum();
  }


  /* ==========================================
     MODULE 8: Saved Answers & Starred
     ========================================== */

  function renderStarredAnswers() {
    const list = document.getElementById('starred-answers-list');
    if (!list) return;
    list.innerHTML = '';

    if (state.starredMessages.length === 0) {
      list.innerHTML = `
        <div class="p-8 text-center text-slate-500 font-mono text-xs flex flex-col gap-3 items-center">
          <i class="far fa-star text-lg opacity-40"></i>
          <span>Starred repository is empty. Star AI answers in the Chat tab to save them.</span>
        </div>
      `;
      return;
    }

    state.starredMessages.forEach(sm => {
      const card = document.createElement('div');
      card.className = 'glass-panel p-4 rounded-2xl border border-white/5 relative hover-glow';
      card.innerHTML = `
        <div class="flex items-center justify-between mb-2">
          <span class="text-[10px] text-[var(--active-glow)] font-mono font-bold tracking-wider uppercase">Saved Snippet</span>
          <button class="unstar-btn text-amber-400 hover:text-slate-500" data-id="${sm.id}">
            <i class="fas fa-star"></i>
          </button>
        </div>
        <div class="text-sm font-display leading-relaxed text-slate-200 whitespace-pre-line mb-3">${sm.text}</div>
        <div class="flex items-center justify-between text-[10px] text-slate-500 font-mono border-t border-white/5 pt-2">
          <span>Thread: ${sm.threadTitle}</span>
          <span>${new Date(sm.timestamp).toLocaleDateString()}</span>
        </div>
      `;

      card.querySelector('.unstar-btn').addEventListener('click', (e) => {
        e.stopPropagation();
        unstarMessage(sm.id);
      });

      list.appendChild(card);
    });
  }

  function unstarMessage(msgId) {
    state.starredMessages = state.starredMessages.filter(sm => sm.id !== msgId);
    saveStarred();

    // Sync in corresponding threads
    state.threads.forEach(thread => {
      const msg = thread.messages.find(m => m.id === msgId);
      if (msg) msg.starred = false;
    });
    saveThreads();

    renderStarredAnswers();
  }


  // Bootstrap System Initializer
  loadPersistedState();
  navigateTo('home');
});
