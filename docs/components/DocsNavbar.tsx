'use client';

/**
 * DocsNavbar — React port of wks-website StageNav + site header.
 *
 * Links mirror StageNav: Overview / Demo / Proof / Product / Pricing
 * all pointing to https://wkspower.com/*, plus a GitHub link.
 *
 * No active-state tracking here (docs live on a separate origin).
 * Visited-state sessionStorage logic omitted — not meaningful cross-site.
 */

const STAGE_LINKS = [
  { label: 'Overview', href: 'https://wkspower.com/' },
  { label: 'Demo',     href: 'https://wkspower.com/demo' },
  { label: 'Proof',    href: 'https://wkspower.com/proof' },
  { label: 'Product',  href: 'https://wkspower.com/product' },
  { label: 'Pricing',  href: 'https://wkspower.com/pricing' },
] as const;

const GITHUB_URL = 'https://github.com/wkspower/wks-platform';

export function DocsNavbar() {
  return (
    <>
      <style>{`
        .wks-navbar-chrome {
          position: sticky;
          top: 0;
          z-index: 50;
          background: color-mix(in srgb, var(--color-surface, #FFFFFF) 95%, transparent);
          backdrop-filter: saturate(180%) blur(8px);
          -webkit-backdrop-filter: saturate(180%) blur(8px);
          border-bottom: 1px solid var(--color-border, #E4E4E7);
        }

        .wks-navbar {
          display: flex;
          align-items: center;
          gap: 1rem;
          width: 100%;
          padding: 0 1rem;
          height: 3.5rem;
          min-width: 0;
        }
        @media (min-width: 640px) { .wks-navbar { padding: 0 1.5rem; } }
        @media (min-width: 1024px) { .wks-navbar { padding: 0 2rem; gap: 2rem; } }

        .wks-navbar__wordmark {
          display: inline-flex;
          align-items: center;
          gap: 0.25rem;
          font-family: var(--font-heading);
          font-weight: 700;
          font-size: 1.125rem;
          color: var(--color-foreground, #09090B);
          text-decoration: none;
          white-space: nowrap;
          flex-shrink: 0;
          letter-spacing: -0.01em;
        }
        .wks-navbar__wordmark-mark { color: var(--color-primary, #3B5BDB); }

        .wks-navbar__sep {
          width: 1px;
          height: 1.25rem;
          background: var(--color-border, #E4E4E7);
          flex-shrink: 0;
        }

        /* Stage track */
        .wks-navbar__track {
          display: none;
          list-style: none;
          margin: 0;
          padding: 0;
          align-items: center;
          gap: 0;
          flex: 1;
          min-width: 0;
        }
        @media (min-width: 768px) {
          .wks-navbar__track { display: flex; }
        }

        .wks-navbar__item {
          position: relative;
          flex: 1 1 0;
          min-width: max-content;
          display: flex;
          align-items: center;
        }

        /* Connector line between items */
        .wks-navbar__item + .wks-navbar__item::before {
          content: '';
          position: absolute;
          left: -50%;
          right: 50%;
          top: 50%;
          height: 1px;
          background: var(--color-border, #E4E4E7);
          transform: translateY(-50%);
          z-index: 0;
        }

        .wks-navbar__link {
          position: relative;
          z-index: 1;
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.25rem 0.5rem;
          margin: 0 auto;
          font-family: var(--font-mono, 'JetBrains Mono', monospace);
          font-size: 0.75rem;
          color: var(--color-muted-fg, #71717A);
          text-decoration: none;
          background: var(--color-surface, #FFFFFF);
          transition: color var(--duration-fast, 150ms) ease-out;
          white-space: nowrap;
        }
        .wks-navbar__link:hover {
          color: var(--color-foreground, #09090B);
        }

        .wks-navbar__node {
          width: 8px;
          height: 8px;
          border-radius: 999px;
          background: var(--color-border, #E4E4E7);
          flex-shrink: 0;
          transition: background-color var(--duration-fast, 150ms) ease-out;
        }
        .wks-navbar__link:hover .wks-navbar__node {
          background: var(--color-primary, #3B5BDB);
        }

        /* GitHub link */
        .wks-navbar__github {
          display: inline-flex;
          align-items: center;
          gap: 0.375rem;
          flex-shrink: 0;
          font-family: var(--font-mono, 'JetBrains Mono', monospace);
          font-size: 0.75rem;
          color: var(--color-muted-fg, #71717A);
          text-decoration: none;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          transition: color var(--duration-fast, 150ms) ease-out,
                      background-color var(--duration-fast, 150ms) ease-out;
        }
        .wks-navbar__github:hover {
          color: var(--color-foreground, #09090B);
          background: var(--color-muted, #F4F4F5);
        }

        @media (prefers-reduced-motion: reduce) {
          .wks-navbar__link,
          .wks-navbar__node,
          .wks-navbar__github { transition: none; }
        }
      `}</style>

      <header className="wks-navbar-chrome">
      <nav className="wks-navbar" aria-label="Product site navigation">
        <a
          href="https://wkspower.com/"
          className="wks-navbar__wordmark"
          aria-label="WKS Platform home"
        >
          <span className="wks-navbar__wordmark-mark">WKS</span>
          <span>Platform</span>
        </a>

        <div className="wks-navbar__sep" aria-hidden="true" />

        <ol className="wks-navbar__track" role="list" aria-label="Evaluation stages">
          {STAGE_LINKS.map((s) => (
            <li key={s.label} className="wks-navbar__item">
              <a
                href={s.href}
                className="wks-navbar__link"
                target="_blank"
                rel="noopener noreferrer"
              >
                <span className="wks-navbar__node" aria-hidden="true" />
                <span>{s.label}</span>
              </a>
            </li>
          ))}
        </ol>

        <a
          href={GITHUB_URL}
          className="wks-navbar__github"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="WKS Platform on GitHub"
        >
          {/* Inline GitHub SVG — no external dep */}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <path d="M12 0C5.374 0 0 5.373 0 12c0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0 1 12 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z" />
          </svg>
          <span>GitHub</span>
        </a>
      </nav>
      </header>
    </>
  );
}
