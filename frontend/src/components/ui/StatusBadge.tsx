import type { StatusColor } from '@/types/statusColor';
import { cn } from '@/lib/cn';

const palette: Record<StatusColor, { bg: string; fg: string }> = {
  blue: { bg: '#e8eefd', fg: '#1e40af' },
  amber: { bg: '#fef3e6', fg: '#92400e' },
  violet: { bg: '#f1ecfb', fg: '#5b21b6' },
  emerald: { bg: '#e6f5f0', fg: '#065f46' },
  zinc: { bg: '#f1f1ee', fg: '#3f3f46' },
  red: { bg: '#fdecec', fg: '#991b1b' },
  cyan: { bg: '#e0f7fa', fg: '#0e7490' },
  rose: { bg: '#fdecf2', fg: '#9f1239' },
  indigo: { bg: '#eef1fc', fg: '#3730a3' },
  teal: { bg: '#e0f5f0', fg: '#115e59' },
};

export function StatusBadge({
  label,
  color = 'zinc',
  className,
}: {
  label: string;
  color?: StatusColor;
  className?: string;
}) {
  const p = palette[color] ?? palette.zinc;
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-[11px] font-medium',
        className,
      )}
      style={{ backgroundColor: p.bg, color: p.fg }}
    >
      <span className="size-1.5 rounded-full" style={{ backgroundColor: p.fg }} aria-hidden />
      {label}
    </span>
  );
}
