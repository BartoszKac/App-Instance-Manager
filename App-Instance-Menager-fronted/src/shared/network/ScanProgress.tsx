import { STAGE_LABEL } from './model';
import type { ScanJob } from './model';
import { Spinner } from '@/shared/ui/atoms';

const counters = (job: ScanJob) => {
  const parts = [`Adresy: ${job.addressesScanned} z ${job.addressesTotal}`, `Urządzenia: ${job.deviceCount}`];
  if (job.stage !== 'SWEEP') parts.push(`Porty: ${job.portsScanned} z ${job.deviceCount}`);
  if (job.stage === 'DETAILS') parts.push(`Szczegóły: ${job.detailsDone} z ${job.detailsTotal}`);
  return parts.join(' · ');
};

export const ScanProgress = ({ job }: { job: ScanJob }) => {
  if (job.state !== 'RUNNING') return null;
  return (
    <div className="scan-progress" aria-live="polite">
      <div className="scan-progress__head">
        <span className="scan-progress__stage"><Spinner />{STAGE_LABEL[job.stage]}</span>
        <strong className="scan-progress__percent">{job.percent}%</strong>
      </div>
      <div className="scan-progress__track" role="progressbar" aria-label="Postęp skanu" aria-valuemin={0} aria-valuemax={100} aria-valuenow={job.percent}>
        <span className="scan-progress__fill" style={{ width: `${job.percent}%` }} />
      </div>
      <p className="scan-progress__counters">{counters(job)}</p>
    </div>
  );
};
