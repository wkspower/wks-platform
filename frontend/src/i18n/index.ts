import en from './en.json';

type Bundle = Record<string, string>;

const SUPPORTED = new Set(['en']);
const FALLBACK_LOCALE = 'en';

function pickLocale(): string {
  const requested = (import.meta.env.VITE_WKS_LOCALE as string | undefined) ?? FALLBACK_LOCALE;
  if (!SUPPORTED.has(requested)) {
    // eslint-disable-next-line no-console
    console.warn(`[i18n] Unsupported locale '${requested}', falling back to '${FALLBACK_LOCALE}'`);
    return FALLBACK_LOCALE;
  }
  return requested;
}

export const locale = pickLocale();

const bundles: Record<string, Bundle> = { en };
const activeBundle: Bundle = bundles[locale] ?? en;

const reportedMissing = new Set<string>();

/**
 * Look up an i18n key. Substitutes `{name}` placeholders. Missing keys
 * return the key itself and log a single WARN per key (so dev catches
 * regressions without flooding the console).
 */
export function t(key: string, params?: Record<string, string>): string {
  const template = activeBundle[key];
  if (template === undefined) {
    if (!reportedMissing.has(key)) {
      reportedMissing.add(key);
      // eslint-disable-next-line no-console
      console.warn(`[i18n] missing key '${key}' for locale '${locale}'`);
    }
    return key;
  }
  if (!params) return template;
  return template.replace(/\{(\w+)\}/g, (_, name: string) =>
    params[name] !== undefined ? params[name] : `{${name}}`,
  );
}
