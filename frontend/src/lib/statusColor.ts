import type { StatusColor } from '@/types/statusColor';

/**
 * Maps the ten lowercase wire tokens emitted by the backend `StatusColor` enum to the CSS
 * custom property names declared in `styles/tokens.css` (Story 1.3). The frontend never
 * resolves the actual hex — `--status-*` is the contract — so a future palette tweak in
 * tokens.css propagates automatically.
 */
export const STATUS_COLOR: Record<StatusColor, string> = {
  blue: 'var(--status-open)',
  amber: 'var(--status-in-progress)',
  violet: 'var(--status-review)',
  emerald: 'var(--status-resolved)',
  zinc: 'var(--status-closed)',
  red: 'var(--status-escalated)',
  cyan: 'var(--status-cyan)',
  rose: 'var(--status-rose)',
  indigo: 'var(--status-indigo)',
  teal: 'var(--status-teal)',
};

const STATUS_COLOR_KEY: Record<StatusColor, string> = {
  blue: 'open',
  amber: 'in-progress',
  violet: 'review',
  emerald: 'resolved',
  zinc: 'closed',
  red: 'escalated',
  cyan: 'cyan',
  rose: 'rose',
  indigo: 'indigo',
  teal: 'teal',
};

export function statusColorVar(color: StatusColor): string {
  return STATUS_COLOR[color];
}

export function statusSoftBgVar(color: StatusColor): string {
  return `var(--status-${STATUS_COLOR_KEY[color]}-soft)`;
}

export function statusOnFgVar(color: StatusColor): string {
  return `var(--status-${STATUS_COLOR_KEY[color]}-on)`;
}
