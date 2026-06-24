/**
 * Theme Switcher Island Component (hydration: idle)
 * Toggles light/dark theme, persists to localStorage.
 * Hydrates on browser idle via requestIdleCallback.
 */
(function() {
  const island = document.querySelector('[data-src="/islands/theme-switcher.js"]');
  if (!island) return;

  const saved = localStorage.getItem('genesis-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);

  function render() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const isDark = current === 'dark';
    island.innerHTML = `
      <button id="theme-toggle" style="display:inline-flex;align-items:center;gap:0.5rem;padding:0.5rem 1rem;background:${isDark ? '#1e293b' : '#f1f5f9'};border:1px solid ${isDark ? '#334155' : '#cbd5e1'};border-radius:9999px;cursor:pointer;color:${isDark ? '#e2e8f0' : '#1e293b'};font-size:0.85rem;font-weight:500;transition:all 0.2s;">
        <span style="font-size:1.1rem;">${isDark ? '🌙' : '☀️'}</span>
        ${isDark ? 'Dark' : 'Light'}
      </button>
    `;
    island.querySelector('#theme-toggle').onclick = () => {
      const next = isDark ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('genesis-theme', next);
      render();
    };
  }

  render();
  console.log('[Genesis Island] Theme Switcher hydrated (strategy: idle)');
})();
