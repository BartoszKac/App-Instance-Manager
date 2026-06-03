// src/editor/components/Topbar.tsx
import React from 'react';

interface TopbarProps {
  left?: React.ReactNode;
  right?: React.ReactNode;
}

export const Topbar: React.FC<TopbarProps> = ({ left, right }) => {
  return (
    <header className="topbar">
      <div className="topbar-left">{left}</div>
      <div className="topbar-right">{right}</div>
    </header>
  );
};
