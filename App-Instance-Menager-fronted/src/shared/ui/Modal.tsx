import { useEffect, useId, useRef } from 'react';
import type { ReactNode } from 'react';
import { gsap } from 'gsap';
import { prefersReducedMotion } from '@/shared/motion';

interface ModalProps {
  title: string;
  description?: string;
  onClose: () => void;
  footer: ReactNode;
  children: ReactNode;
}

const useEscape = (onClose: () => void) => {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);
};

const useOpenAnimation = (ref: React.RefObject<HTMLDivElement | null>) => {
  useEffect(() => {
    if (!ref.current || prefersReducedMotion()) return;
    const tween = gsap.from(ref.current, { y: 12, scale: 0.98, opacity: 0, duration: 0.28, ease: 'power3.out' });
    return () => { tween.kill(); };
  }, [ref]);
};

export const Modal = ({ title, description, onClose, footer, children }: ModalProps) => {
  const titleId = useId();
  const panelRef = useRef<HTMLDivElement>(null);
  useEscape(onClose);
  useOpenAnimation(panelRef);

  return (
    <div className="modal-backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div ref={panelRef} className="modal" role="dialog" aria-modal="true" aria-labelledby={titleId}>
        <h2 id={titleId} className="modal__title">{title}</h2>
        {description && <p className="modal__description">{description}</p>}
        <div className="modal__body">{children}</div>
        <div className="modal__footer">{footer}</div>
      </div>
    </div>
  );
};
