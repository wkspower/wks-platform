import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/Card';
import { t } from '@/i18n';

export function AppErrorFallback() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-[var(--background)] p-[var(--space-8)]">
      <Card className="w-full max-w-lg">
        <CardHeader>
          <h1 className="font-heading text-xl font-semibold">{t('error.title')}</h1>
        </CardHeader>
        <CardContent>
          <p className="text-[var(--muted-foreground)]">{t('error.savedServerSide')}</p>
        </CardContent>
        <CardFooter>
          <Button onClick={() => window.location.reload()}>{t('error.reload')}</Button>
        </CardFooter>
      </Card>
    </div>
  );
}
