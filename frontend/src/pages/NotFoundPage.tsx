import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-background p-6 text-center">
      <div>
        <p className="text-[11px] uppercase tracking-widest text-foreground-subtle">404</p>
        <h1 className="font-heading text-3xl font-semibold mt-2">Page not found</h1>
        <p className="text-foreground-muted mt-1 text-[13px]">The page you're looking for doesn't exist.</p>
        <Link to="/" className="inline-block mt-5 text-[var(--primary)] hover:underline text-[13px]">
          Back to home
        </Link>
      </div>
    </main>
  );
}
