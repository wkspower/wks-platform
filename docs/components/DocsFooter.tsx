'use client';

/**
 * DocsFooter — React port of wks-website AuditStrip.astro
 *
 * Visitor session audit log. Demonstrates auditability inline.
 * Privacy: no network calls, no PII, no cookies. sessionStorage only. Capped at 12 events.
 * Dismissible per session.
 */

import { useEffect, useRef } from 'react';

const AUDIT_KEY = 'wks:audit-events';
const DISMISS_KEY = 'wks:audit-dismissed';
const CAP = 12;

type AuditEvent = { t: number; verb: string; obj: string };

function load(): AuditEvent[] {
  if (typeof window === 'undefined') return [];
  try { return JSON.parse(sessionStorage.getItem(AUDIT_KEY) || '[]'); } catch { return []; }
}
function save(events: AuditEvent[]) {
  try { sessionStorage.setItem(AUDIT_KEY, JSON.stringify(events.slice(-CAP))); } catch {}
}
function emit(verb: string, obj: string) {
  const events = load();
  const last = events[events.length - 1];
  if (last && last.verb === verb && last.obj === obj) return;
  events.push({ t: Date.now(), verb, obj });
  save(events);
}
function fmtTime(t: number) {
  return new Date(t).toTimeString().slice(0, 8);
}

export function DocsFooter() {
  const rootRef = useRef<HTMLElement>(null);
  const countRef = useRef<HTMLSpanElement>(null);
  const latestRef = useRef<HTMLSpanElement>(null);
  const listRef = useRef<HTMLOListElement>(null);

  function render() {
    const root = rootRef.current;
    const countEl = countRef.current;
    const latestEl = latestRef.current;
    const listEl = listRef.current;
    if (!root || !countEl || !latestEl || !listEl) return;

    const events = load();
    countEl.textContent = `${events.length} event${events.length === 1 ? '' : 's'}`;
    const latest = events[events.length - 1];
    latestEl.textContent = latest ? `· ${latest.verb} ${latest.obj}` : '';
    listEl.innerHTML = events
      .slice()
      .reverse()
      .map(
        (e) =>
          `<li><span class="wks-audit__ts">${fmtTime(e.t)}</span><span class="wks-audit__verb">${e.verb}</span><span class="wks-audit__obj">${e.obj}</span></li>`
      )
      .join('');

    if (sessionStorage.getItem(DISMISS_KEY) !== '1') {
      root.hidden = false;
    }
  }

  useEffect(() => {
    // Emit initial page-view event
    const route =
      location.pathname === '/'
        ? 'docs'
        : location.pathname.replace(/^\//, '').replace(/\/$/, '');
    emit('opened', route);
    render();

    // Wire data-audit click triggers
    function handleClick(e: MouseEvent) {
      const el = (e.target as HTMLElement).closest<HTMLElement>('[data-audit]');
      if (!el) return;
      const raw = el.getAttribute('data-audit') || '';
      const [verb, ...rest] = raw.split(':');
      const obj = rest.join(':') || el.textContent?.trim() || '';
      if (verb && obj) { emit(verb, obj); render(); }
    }
    document.addEventListener('click', handleClick);

    // Section-view tracking via IntersectionObserver
    let io: IntersectionObserver | undefined;
    if ('IntersectionObserver' in window) {
      const seen = new Set<string>();
      io = new IntersectionObserver(
        (entries) => {
          entries.forEach((en) => {
            if (!en.isIntersecting) return;
            const target = en.target as HTMLElement;
            const name = target.getAttribute('data-audit-view') || '';
            if (!name || seen.has(name)) return;
            seen.add(name);
            emit('viewed', name);
            render();
            io?.unobserve(target);
          });
        },
        { threshold: 0.5 }
      );
      document.querySelectorAll<HTMLElement>('[data-audit-view]').forEach((el) => io!.observe(el));
    }

    return () => {
      document.removeEventListener('click', handleClick);
      io?.disconnect();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function toggleExpanded() {
    const root = rootRef.current;
    if (!root) return;
    const expanded = root.getAttribute('data-state') === 'expanded';
    root.setAttribute('data-state', expanded ? 'collapsed' : 'expanded');
  }

  function dismiss(e: React.MouseEvent) {
    e.stopPropagation();
    sessionStorage.setItem(DISMISS_KEY, '1');
    if (rootRef.current) rootRef.current.hidden = true;
  }

  return (
    <>
      <style>{`
        .wks-audit {
          position: fixed;
          right: 1rem;
          bottom: 1rem;
          z-index: 60;
          width: min(20rem, calc(100vw - 2rem));
          background: var(--color-surface, #FFFFFF);
          border: 1px solid var(--color-border, #E4E4E7);
          border-radius: 10px;
          box-shadow: 0 6px 20px rgba(9, 9, 11, 0.06);
          font-family: var(--font-mono, 'JetBrains Mono', monospace);
          font-size: 0.6875rem;
          color: var(--color-foreground, #09090B);
          overflow: hidden;
        }

        .wks-audit__head {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          width: 100%;
          padding: 0.5rem 2rem 0.5rem 0.75rem;
          background: transparent;
          border: 0;
          text-align: left;
          cursor: pointer;
          color: inherit;
          font: inherit;
        }
        .wks-audit__head:hover { background: var(--color-muted, #F4F4F5); }

        .wks-audit__dot {
          width: 6px; height: 6px; border-radius: 999px;
          background: var(--color-success, #059669);
          flex-shrink: 0;
        }
        .wks-audit__key {
          text-transform: uppercase;
          letter-spacing: 0.12em;
          color: var(--color-muted-fg, #71717A);
          font-weight: 600;
        }
        .wks-audit__count {
          color: var(--color-muted-fg, #71717A);
          flex-shrink: 0;
        }
        .wks-audit__latest {
          flex: 1;
          min-width: 0;
          color: var(--color-foreground, #09090B);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          opacity: 0.85;
        }
        .wks-audit__chevron {
          color: var(--color-muted-fg, #71717A);
          transition: transform var(--duration-fast, 150ms) ease-out;
          transform: rotate(180deg);
        }
        .wks-audit[data-state="expanded"] .wks-audit__chevron { transform: rotate(0deg); }

        .wks-audit__list {
          list-style: none;
          margin: 0;
          padding: 0;
          max-height: 0;
          overflow: hidden;
          transition: max-height var(--duration-normal, 300ms) ease-out;
          border-top: 1px solid transparent;
        }
        .wks-audit[data-state="expanded"] .wks-audit__list {
          max-height: 14rem;
          overflow-y: auto;
          border-top-color: var(--color-border, #E4E4E7);
        }
        .wks-audit__list li {
          display: grid;
          grid-template-columns: 4.25rem auto 1fr;
          gap: 0.5rem;
          padding: 0.4rem 0.75rem;
          border-bottom: 1px solid var(--color-border, #E4E4E7);
        }
        .wks-audit__list li:last-child { border-bottom: 0; }
        .wks-audit__ts   { color: var(--color-muted-fg, #71717A); opacity: 0.7; }
        .wks-audit__verb { color: var(--color-primary, #3B5BDB); font-weight: 600; }
        .wks-audit__obj  {
          color: var(--color-foreground, #09090B);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .wks-audit__close {
          position: absolute;
          top: 0.25rem;
          right: 0.25rem;
          width: 1.25rem;
          height: 1.25rem;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          background: transparent;
          border: 0;
          border-radius: 4px;
          color: var(--color-muted-fg, #71717A);
          font-size: 0.875rem;
          line-height: 1;
          cursor: pointer;
        }
        .wks-audit__close:hover {
          background: var(--color-muted, #F4F4F5);
          color: var(--color-foreground, #09090B);
        }

        @media (prefers-reduced-motion: reduce) {
          .wks-audit__list,
          .wks-audit__chevron { transition: none; }
        }

        @media (max-width: 480px) {
          .wks-audit { right: 0.5rem; bottom: 0.5rem; width: calc(100vw - 1rem); }
        }
      `}</style>

      <aside
        ref={rootRef}
        id="wks-audit-strip"
        className="wks-audit"
        aria-label="Session audit log"
        data-state="collapsed"
        hidden
      >
        <button
          className="wks-audit__head"
          type="button"
          aria-expanded="false"
          aria-controls="wks-audit-list"
          onClick={toggleExpanded}
        >
          <span className="wks-audit__dot" aria-hidden="true" />
          <span className="wks-audit__key">Audit</span>
          <span ref={countRef} className="wks-audit__count">0 events</span>
          <span ref={latestRef} className="wks-audit__latest" />
          <span className="wks-audit__chevron" aria-hidden="true">&#9652;</span>
        </button>
        <ol ref={listRef} className="wks-audit__list" id="wks-audit-list" role="list" />
        <button
          className="wks-audit__close"
          type="button"
          aria-label="Dismiss audit log"
          onClick={dismiss}
        >
          &times;
        </button>
      </aside>
    </>
  );
}
