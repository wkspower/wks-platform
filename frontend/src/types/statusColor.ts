/**
 * Ten palette tokens matching the backend `StatusColor` enum (lowercase wire form). The mapping
 * to CSS custom properties lives in `lib/statusColor.ts` so this file stays a pure type contract.
 */
export const STATUS_COLORS = [
  'blue',
  'amber',
  'violet',
  'emerald',
  'zinc',
  'red',
  'cyan',
  'rose',
  'indigo',
  'teal',
] as const;

export type StatusColor = (typeof STATUS_COLORS)[number];
