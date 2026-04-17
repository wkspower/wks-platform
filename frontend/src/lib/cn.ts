import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Combine class names with conflict resolution. Use everywhere instead
 * of raw template strings so Tailwind utility conflicts collapse to
 * the last-wins behaviour the components rely on.
 */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}
