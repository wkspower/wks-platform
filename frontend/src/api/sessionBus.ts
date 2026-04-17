/**
 * Session expiry event bus. The API client emits a SessionExpiredEvent
 * on the bus when an authenticated request returns 401; the AppShell
 * subscribes via useSessionExpiry and surfaces an inline banner. The
 * bus deliberately lives outside React state — both producers (api/client.ts)
 * and consumers (hooks/useSessionExpiry.ts) want a single global channel.
 */
export interface SessionExpiredDetail {
  /** Path of the request that triggered the expiry (e.g. "/api/cases"). */
  requestPath: string;
}

export const SESSION_EXPIRED = 'session-expired';

class SessionBus extends EventTarget {
  emit(detail: SessionExpiredDetail): void {
    this.dispatchEvent(new CustomEvent<SessionExpiredDetail>(SESSION_EXPIRED, { detail }));
  }
}

export const sessionBus = new SessionBus();

export type SessionExpiredEvent = CustomEvent<SessionExpiredDetail>;
