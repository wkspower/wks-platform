import { useNavigate } from 'react-router-dom';

import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/Card';
import { t } from '@/i18n';

export function ForbiddenScreen() {
  const navigate = useNavigate();
  return (
    <div className="flex min-h-full items-start justify-center p-[var(--space-8)]">
      <Card className="w-full max-w-lg">
        <CardHeader>
          <h1 className="font-heading text-xl font-semibold">{t('page.forbidden.title')}</h1>
        </CardHeader>
        <CardContent>
          <p className="text-[var(--muted-foreground)]">{t('page.forbidden.body')}</p>
        </CardContent>
        <CardFooter>
          <Button variant="outline" onClick={() => navigate(-1)}>
            {t('page.forbidden.back')}
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}
