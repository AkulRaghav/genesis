/**
 * Newsletter Signup Island Component (hydration: client:only)
 * Never server-renders — only renders on the client after JS loads.
 * Proves CLIENT_ONLY strategy: view-source shows empty div, form appears after hydration.
 */
(function() {
  const island = document.querySelector('[data-src="/islands/newsletter.js"]');
  if (!island) return;

  let submitted = false;

  function render() {
    if (submitted) {
      island.innerHTML = `
        <div style="padding:1.25rem;background:#064e3b;border:1px solid #10b981;border-radius:12px;text-align:center;">
          <span style="font-size:1.5rem;">✓</span>
          <p style="color:#a7f3d0;margin:0.5rem 0 0;font-weight:500;">You're subscribed! Check your inbox.</p>
        </div>
      `;
      return;
    }

    island.innerHTML = `
      <div style="padding:1.5rem;background:linear-gradient(135deg,#1e1b4b,#312e81);border:1px solid #4338ca;border-radius:12px;">
        <h4 style="color:#e0e7ff;margin-bottom:0.25rem;font-size:1rem;font-weight:600;">📬 Stay updated</h4>
        <p style="color:#a5b4fc;font-size:0.85rem;margin-bottom:1rem;">Get notified about new posts. No spam, unsubscribe anytime.</p>
        <div style="display:flex;gap:0.5rem;">
          <input id="newsletter-email" type="email" placeholder="you@example.com" style="flex:1;padding:0.6rem 1rem;background:#0f0a2e;border:1px solid #4338ca;border-radius:8px;color:#e2e8f0;font-size:0.9rem;outline:none;" />
          <button id="newsletter-submit" style="padding:0.6rem 1.25rem;background:#6366f1;color:white;border:none;border-radius:8px;font-weight:600;cursor:pointer;font-size:0.85rem;white-space:nowrap;">Subscribe</button>
        </div>
      </div>
    `;
    island.querySelector('#newsletter-submit').onclick = () => {
      const email = island.querySelector('#newsletter-email').value.trim();
      if (email && email.includes('@')) {
        submitted = true;
        render();
      }
    };
    island.querySelector('#newsletter-email').onkeydown = (e) => {
      if (e.key === 'Enter') island.querySelector('#newsletter-submit').click();
    };
  }

  render();
  console.log('[Genesis Island] Newsletter Signup hydrated (strategy: client:only)');
})();
