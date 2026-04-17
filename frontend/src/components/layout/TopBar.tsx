import { t } from '@/i18n';
import { useAuthStore } from '@/stores/authStore';

export function TopBar() {
  const user = useAuthStore((s) => s.user);
  return (
    <header className="flex h-12 items-center justify-between border-b border-[var(--border)] bg-[var(--card)] px-[var(--space-4)]">
      <div className="font-heading text-sm font-semibold">{t('app.brandName')}</div>
      {user ? (
        <div className="text-xs text-[var(--muted-foreground)]" aria-label="signed-in user">
          {user.email}
        </div>
      ) : null}
    </header>
  );
}
