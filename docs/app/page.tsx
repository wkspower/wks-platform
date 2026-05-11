import { PersonaCard } from './components/PersonaCard';
import { DocsFooter } from '../components/DocsFooter';

const personas = [
  {
    title: 'New to WKS',
    description:
      'Stand up a local instance, load a seed case type, and have a working case-management surface in under five minutes.',
    href: '/docs/start/run-wks-in-5-minutes',
    icon: '🚀',
  },
  {
    title: 'Coming from Camunda',
    description:
      'See how case types, stages, and statuses map to BPMN concepts — and where the mental model is deliberately different.',
    href: '/docs/start/coming-from-camunda',
    icon: '🔁',
  },
  {
    title: 'Going to production',
    description:
      'Docker Compose with Postgres and MinIO, required environment variables, and a smoke-check sequence before your first client handover.',
    href: '/docs/operations/deploy-to-production',
    icon: '⚙️',
  },
  {
    title: 'API & schema reference',
    description:
      'Every top-level YAML key with type, required/optional, validator citation, and a complete worked example.',
    href: '/docs/reference/yaml-schema',
    icon: '📖',
  },
] as const;

export default function LandingPage() {
  return (
    <>
      <style>{`
        .wks-landing {
          min-height: 100vh;
          background: var(--color-background, #FAFAFA);
          display: flex;
          flex-direction: column;
        }
        .wks-landing__hero {
          padding: 5rem 1.5rem 3rem;
          text-align: center;
          max-width: 52rem;
          margin: 0 auto;
          width: 100%;
        }
        .wks-landing__eyebrow {
          display: inline-block;
          font-family: var(--font-body, Rubik, system-ui, sans-serif);
          font-size: 0.8125rem;
          font-weight: 500;
          text-transform: uppercase;
          letter-spacing: 0.1em;
          color: var(--color-primary, #3B5BDB);
          margin-bottom: 1.25rem;
        }
        .wks-landing__h1 {
          font-family: var(--font-heading, Poppins, system-ui, sans-serif);
          font-size: clamp(1.875rem, 5vw, 3rem);
          font-weight: 700;
          line-height: 1.2;
          color: var(--color-brand-navy, #0B1437);
          margin: 0 0 1.25rem;
        }
        .wks-landing__subhead {
          font-family: var(--font-body, Rubik, system-ui, sans-serif);
          font-size: 1.125rem;
          color: var(--color-muted-fg, #71717A);
          line-height: 1.6;
          margin: 0;
          max-width: 38rem;
          margin-left: auto;
          margin-right: auto;
        }
        .wks-landing__grid-wrapper {
          flex: 1;
          padding: 2rem 1.5rem 4rem;
          max-width: 56rem;
          margin: 0 auto;
          width: 100%;
          box-sizing: border-box;
        }
        .wks-landing__grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 1.25rem;
        }
        @media (max-width: 640px) {
          .wks-landing__hero {
            padding: 3rem 1rem 2rem;
          }
          .wks-landing__grid {
            grid-template-columns: 1fr;
          }
          .wks-landing__grid-wrapper {
            padding: 1.5rem 1rem 3rem;
          }
        }
      `}</style>

      <div className="wks-landing">
        <header className="wks-landing__hero">
          <span className="wks-landing__eyebrow">Documentation</span>
          <h1 className="wks-landing__h1">
            From zero to client demo in 30 minutes
          </h1>
          <p className="wks-landing__subhead">
            Declare your domain in YAML, attach a process engine when you need one, and ship to production with confidence. Choose a starting point below.
          </p>
        </header>

        <main className="wks-landing__grid-wrapper">
          <div className="wks-landing__grid">
            {personas.map((p) => (
              <PersonaCard
                key={p.href}
                title={p.title}
                description={p.description}
                href={p.href}
                icon={p.icon}
              />
            ))}
          </div>
        </main>

        <DocsFooter />
      </div>
    </>
  );
}
