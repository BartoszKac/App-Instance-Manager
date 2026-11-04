import type { ReactNode } from 'react';

interface ScreenHeaderProps {
  title: string;
  context?: ReactNode;
  actions?: ReactNode;
  leading?: ReactNode;
}

export const ScreenHeader = ({ title, context, actions, leading }: ScreenHeaderProps) => (
  <header className="screen-header">
    {leading}
    <div className="screen-header__text">
      <h1 className="screen-header__title" data-enter="title">{title}</h1>
      {context && <div className="screen-header__context" data-enter="meta">{context}</div>}
    </div>
    {actions && <div className="screen-header__actions" data-enter="meta">{actions}</div>}
  </header>
);
