import type { InspectedFile } from '../model/Inspection';
import { formatBytes, formatEpoch } from './format';

export const FileMeta = ({ file }: { file: InspectedFile }) => (
  <>
    <span className="file-row__cell file-row__size">{file.type === 'f' ? formatBytes(file.sizeBytes) : ''}</span>
    <span className="file-row__cell">{file.owner}{file.group && `:${file.group}`}</span>
    <span className="file-row__cell">{file.mode}</span>
    <span className="file-row__cell">{formatEpoch(file.modifiedEpochSeconds)}</span>
  </>
);

export const DeniedBadge = ({ file }: { file: InspectedFile }) =>
  file.denied ? <span className="file-row__denied">brak dostępu</span> : null;
