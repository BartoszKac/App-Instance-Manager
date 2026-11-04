import { Icon as IconifyIcon } from '@iconify/react';
import type { IconifyIcon as IconData } from '@iconify/react';
import code from '@iconify-icons/solar/code-square-linear';
import upload from '@iconify-icons/solar/upload-minimalistic-linear';
import ship from '@iconify-icons/solar/plain-2-linear';
import run from '@iconify-icons/solar/play-circle-linear';
import add from '@iconify-icons/solar/add-circle-linear';
import search from '@iconify-icons/solar/magnifer-linear';
import close from '@iconify-icons/solar/close-circle-linear';
import trash from '@iconify-icons/solar/trash-bin-minimalistic-linear';
import server from '@iconify-icons/solar/server-square-linear';
import file from '@iconify-icons/solar/document-text-linear';
import refresh from '@iconify-icons/solar/refresh-linear';
import check from '@iconify-icons/solar/check-circle-linear';
import warning from '@iconify-icons/solar/danger-triangle-linear';
import play from '@iconify-icons/solar/play-bold';
import build from '@iconify-icons/solar/settings-linear';
import save from '@iconify-icons/solar/diskette-linear';
import erase from '@iconify-icons/solar/eraser-linear';
import chevronDown from '@iconify-icons/solar/alt-arrow-down-linear';
import chevronRight from '@iconify-icons/solar/alt-arrow-right-linear';
import folder from '@iconify-icons/solar/folder-with-files-linear';
import transfer from '@iconify-icons/solar/transfer-horizontal-linear';
import back from '@iconify-icons/solar/arrow-left-linear';
import command from '@iconify-icons/solar/command-linear';
import terminal from '@iconify-icons/solar/programming-linear';
import cpu from '@iconify-icons/solar/cpu-bolt-linear';

const ICONS = {
  code, upload, ship, run, add, search, close, trash, server, file, refresh, check, warning,
  play, build, save, erase, chevronDown, chevronRight, folder, transfer, back, command,
  terminal, cpu,
} satisfies Record<string, IconData>;

export type IconName = keyof typeof ICONS;

interface IconProps {
  name: IconName;
  size?: number;
  className?: string;
}

export const Icon = ({ name, size = 18, className }: IconProps) => (
  <IconifyIcon icon={ICONS[name]} width={size} height={size} className={className} aria-hidden="true" />
);
