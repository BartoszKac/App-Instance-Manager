import { Notice } from '@/shared/ui/atoms';
import { UploadForm } from '../components/UploadForm';
import { UploadHistory } from '../components/UploadHistory';
import { UploadProgress } from '../components/UploadProgress';
import { useDirectUpload } from '../viewmodels/useDirectUpload';

export const DirectUploadView = ({ serverId }: { serverId: number }) => {
  const vm = useDirectUpload(serverId);
  return (
    <div className="direct-upload">
      {vm.error && <Notice tone="error">{vm.error}</Notice>}
      <UploadForm vm={vm} />
      <UploadProgress vm={vm} />
      <UploadHistory history={vm.history} />
    </div>
  );
};
