import { useSessionExpiry } from '@/hooks/useSessionExpiry';

import { LicenseBanner } from './LicenseBanner';
import { SessionExpiryBannerView } from './SessionExpiryBanner';

/**
 * Priority-queue for top-of-shell banners. SessionExpiry wins over License so
 * the user never sees stacked yellow strips competing for attention. Once
 * the user reauths or dismisses, the License banner has a chance to surface
 * on the next render. The session hook is owned here (not by the banner) so
 * we can read `expired` before deciding which child to mount.
 */
export function BannerStack() {
  const { expired, dismiss, triggerLogin } = useSessionExpiry();
  if (expired) {
    return <SessionExpiryBannerView dismiss={dismiss} triggerLogin={triggerLogin} />;
  }
  return <LicenseBanner />;
}
