/**
 * Counter Island Component (hydration: load)
 * A simple interactive counter that increments on click.
 * Hydrates immediately on page load.
 */
(function() {
  const island = document.querySelector('[data-src="/islands/counter.js"]');
  if (!island) return;

  const props = island.dataset.props ? JSON.parse(island.dataset.props) : {};
  let count = props.initialCount || 0;

  function render() {
    island.innerHTML = `
      <div style="display:inline-flex;align-items:center;gap:0.75rem;padding:0.75rem 1.25rem;background:#1e293b;border-radius:12px;border:1px solid #334155;">
        <button id="counter-dec" style="width:32px;height:32px;border-radius:8px;border:1px solid #475569;background:#0f172a;color:#e2e8f0;font-size:1.1rem;cursor:pointer;display:flex;align-items:center;justify-content:center;">−</button>
        <span style="font-size:1.5rem;font-weight:700;color:#f8fafc;min-width:3ch;text-align:center;">${count}</span>
        <button id="counter-inc" style="width:32px;height:32px;border-radius:8px;border:none;background:#6366f1;color:white;font-size:1.1rem;cursor:pointer;display:flex;align-items:center;justify-content:center;">+</button>
      </div>
    `;
    island.querySelector('#counter-dec').onclick = () => { count--; render(); };
    island.querySelector('#counter-inc').onclick = () => { count++; render(); };
  }

  render();
  console.log('[Genesis Island] Counter hydrated (strategy: load)');
})();
