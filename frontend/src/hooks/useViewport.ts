import { useEffect, useState } from 'react';

export interface Viewport {
  width: number;
  height: number;
}

const SSR_DEFAULT: Viewport = { width: 1280, height: 800 };

function readViewport(): Viewport {
  if (typeof window === 'undefined') return SSR_DEFAULT;
  return { width: window.innerWidth, height: window.innerHeight };
}

export function useViewport(): Viewport {
  const [viewport, setViewport] = useState<Viewport>(readViewport);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    let frame = 0;
    function handleResize() {
      if (frame) cancelAnimationFrame(frame);
      frame = requestAnimationFrame(() => {
        setViewport({ width: window.innerWidth, height: window.innerHeight });
      });
    }
    window.addEventListener('resize', handleResize, { passive: true });
    return () => {
      if (frame) cancelAnimationFrame(frame);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return viewport;
}
