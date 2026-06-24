/**
 * Comment Form Island Component (hydration: visible)
 * A simple comment form that hydrates only when scrolled into view.
 * Uses IntersectionObserver via the Genesis island loader.
 */
(function() {
  const island = document.querySelector('[data-src="/islands/comment-form.js"]');
  if (!island) return;

  const props = island.dataset.props ? JSON.parse(island.dataset.props) : {};
  const postId = props.postId || 'unknown';
  const comments = JSON.parse(localStorage.getItem('genesis-comments-' + postId) || '[]');

  function render() {
    island.innerHTML = `
      <div style="padding:1.5rem;background:#1e293b;border-radius:12px;border:1px solid #334155;margin-top:1rem;">
        <h3 style="color:#f8fafc;font-size:1.1rem;margin-bottom:1rem;font-weight:600;">Comments (${comments.length})</h3>
        <div id="comments-list" style="margin-bottom:1rem;">
          ${comments.map(c => `<div style="padding:0.75rem;background:#0f172a;border-radius:8px;margin-bottom:0.5rem;color:#cbd5e1;font-size:0.9rem;">${escapeHtml(c)}</div>`).join('')}
        </div>
        <div style="display:flex;gap:0.5rem;">
          <input id="comment-input" type="text" placeholder="Write a comment..." style="flex:1;padding:0.6rem 1rem;background:#0f172a;border:1px solid #475569;border-radius:8px;color:#e2e8f0;font-size:0.9rem;outline:none;" />
          <button id="comment-submit" style="padding:0.6rem 1.25rem;background:#6366f1;color:white;border:none;border-radius:8px;font-weight:600;cursor:pointer;font-size:0.85rem;">Post</button>
        </div>
      </div>
    `;
    island.querySelector('#comment-submit').onclick = () => {
      const input = island.querySelector('#comment-input');
      const text = input.value.trim();
      if (text) {
        comments.push(text);
        localStorage.setItem('genesis-comments-' + postId, JSON.stringify(comments));
        render();
      }
    };
    island.querySelector('#comment-input').onkeydown = (e) => {
      if (e.key === 'Enter') island.querySelector('#comment-submit').click();
    };
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  render();
  console.log('[Genesis Island] Comment Form hydrated (strategy: visible)');
})();
