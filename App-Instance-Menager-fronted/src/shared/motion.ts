import { useLayoutEffect } from 'react';
import type { RefObject } from 'react';
import { gsap } from 'gsap';

export const prefersReducedMotion = (): boolean =>
  typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

export const useScreenEnter = (ref: RefObject<HTMLElement | null>, key: string) => {
  useLayoutEffect(() => {
    const root = ref.current;
    if (!root || prefersReducedMotion()) return;
    const ctx = gsap.context(() => {
      gsap.timeline({ defaults: { ease: 'power3.out' } })
        .from('[data-enter="title"]', { y: 14, opacity: 0, duration: 0.42 })
        .from('[data-enter="meta"]', { y: 8, opacity: 0, duration: 0.32 }, '-=0.26')
        .from('[data-enter="body"]', { y: 10, opacity: 0, duration: 0.38, clearProps: 'transform' }, '-=0.2');
    }, root);
    return () => ctx.revert();
  }, [ref, key]);
};
