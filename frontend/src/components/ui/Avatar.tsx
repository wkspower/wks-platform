import { cn } from '@/lib/cn';

function initials(name: string | null | undefined): string {
  if (!name) return '?';
  const trimmed = name.trim();
  if (!trimmed) return '?';
  const parts = trimmed.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length === 0) return trimmed.charAt(0).toUpperCase();
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return (parts[0]![0]! + parts[parts.length - 1]![0]!).toUpperCase();
}

function colorFor(seed: string): string {
  const palette = ['#3b5bdb', '#22d3ee', '#10a37f', '#f59e0b', '#8b5cf6', '#ef4444', '#ec4899'];
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  return palette[h % palette.length]!;
}

export function Avatar({
  name,
  size = 'md',
  className,
}: {
  name: string | null | undefined;
  size?: 'xs' | 'sm' | 'md' | 'lg';
  className?: string;
}) {
  const sz = { xs: 'size-4 text-[8px]', sm: 'size-5 text-[9px]', md: 'size-6 text-[10px]', lg: 'size-8 text-xs' }[size];
  const c = colorFor(name ?? '?');
  return (
    <span
      className={cn(
        'inline-flex items-center justify-center rounded-full font-medium text-white shrink-0',
        sz,
        className,
      )}
      style={{ backgroundColor: c }}
      title={name ?? undefined}
    >
      {initials(name)}
    </span>
  );
}
