import Link from 'next/link';
import type { ReactNode } from 'react';

export interface PersonaCardProps {
  title: string;
  description: string;
  href: string;
  icon?: ReactNode;
}

export function PersonaCard({ title, description, href, icon }: PersonaCardProps) {
  return (
    <Link
      href={href}
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem',
        padding: '1.5rem',
        background: 'var(--color-surface, #FFFFFF)',
        border: '1px solid var(--color-border, #E4E4E7)',
        borderRadius: '12px',
        textDecoration: 'none',
        color: 'inherit',
        transition: 'border-color var(--duration-fast, 150ms) ease-out, box-shadow var(--duration-fast, 150ms) ease-out, transform var(--duration-fast, 150ms) ease-out',
      }}
      className="wks-persona-card"
      aria-label={`${title} — ${description}`}
    >
      {icon && (
        <span
          aria-hidden="true"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '2.5rem',
            height: '2.5rem',
            background: 'color-mix(in srgb, var(--color-primary, #3B5BDB) 10%, transparent)',
            borderRadius: '8px',
            color: 'var(--color-primary, #3B5BDB)',
            fontSize: '1.25rem',
          }}
        >
          {icon}
        </span>
      )}
      <span
        style={{
          fontFamily: 'var(--font-heading, Poppins, system-ui, sans-serif)',
          fontWeight: 600,
          fontSize: '1.0625rem',
          color: 'var(--color-foreground, #09090B)',
          lineHeight: 1.3,
        }}
      >
        {title}
      </span>
      <span
        style={{
          fontFamily: 'var(--font-body, Rubik, system-ui, sans-serif)',
          fontSize: '0.9375rem',
          color: 'var(--color-muted-fg, #71717A)',
          lineHeight: 1.5,
          flex: 1,
        }}
      >
        {description}
      </span>
      <span
        aria-hidden="true"
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.25rem',
          fontFamily: 'var(--font-body, Rubik, system-ui, sans-serif)',
          fontSize: '0.875rem',
          fontWeight: 500,
          color: 'var(--color-primary, #3B5BDB)',
          marginTop: '0.25rem',
        }}
      >
        Read{' '}
        <svg
          width="14"
          height="14"
          viewBox="0 0 14 14"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <path
            d="M2.625 7H11.375M11.375 7L7.875 3.5M11.375 7L7.875 10.5"
            stroke="currentColor"
            strokeWidth="1.4"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </span>

      <style>{`
        .wks-persona-card:hover {
          border-color: var(--color-primary, #3B5BDB);
          box-shadow: 0 4px 16px color-mix(in srgb, var(--color-primary, #3B5BDB) 12%, transparent);
          transform: translateY(-2px);
        }
        .wks-persona-card:focus-visible {
          outline: 2px solid var(--color-primary, #3B5BDB);
          outline-offset: 2px;
          border-radius: 12px;
        }
        @media (prefers-reduced-motion: reduce) {
          .wks-persona-card {
            transition: none;
          }
          .wks-persona-card:hover {
            transform: none;
          }
        }
      `}</style>
    </Link>
  );
}
