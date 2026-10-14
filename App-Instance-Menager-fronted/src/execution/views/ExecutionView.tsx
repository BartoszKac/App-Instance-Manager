import { useEffect, useRef } from 'react';
import { useExecutionViewModel } from '@/execution/viewmodels/useExecutionViewModel';
import type { TerminalLine } from '@/execution/model/Executionmodel';
import { InstanceCard } from '@/execution/components/InstanceCard';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { EmptyState, Notice, Spinner } from '@/shared/ui/atoms';

const Output = ({ lines, onClear }: { lines: TerminalLine[]; onClear: () => void }) => {
  const endRef = useRef<HTMLLIElement>(null);
  useEffect(() => { endRef.current?.scrollIntoView({ block: 'end' }); }, [lines]);
  return (
    <section className="run-output" aria-label="Wynik komend">
      <div className="run-output__head">
        <h2>Wynik</h2>
        <button className="btn btn--ghost btn--sm" onClick={onClear}><Icon name="erase" size={15} />Wyczyść</button>
      </div>
      <ol className="run-output__lines" aria-live="polite">
        {lines.map((l) => <li key={l.id} className={`run-output__line run-output__line--${l.type}`}>{l.text}</li>)}
        <li ref={endRef} aria-hidden="true" />
      </ol>
    </section>
  );
};

export const ExecutionView = () => {
  const vm = useExecutionViewModel();
  const empty = !vm.loading && vm.instances.length === 0;

  return (
    <>
      <ScreenHeader
        title="Uruchom"
        context="Wysłane programy na serwerach. Wpisz komendę, a wynik pojawi się obok."
        actions={<button className="btn btn--quiet" onClick={vm.fetchInstances} disabled={vm.loading}>{vm.loading ? <Spinner /> : <Icon name="refresh" />}Odśwież</button>}
      />
      <div className="run" data-enter="body">
        <div className="run__list">
          {vm.error && <Notice tone="error">Nie udało się wczytać programów: {vm.error}</Notice>}
          {vm.loading && vm.instances.length === 0 && <p className="deploy-panel__loading"><Spinner />Wczytywanie programów…</p>}
          {empty && !vm.error && <EmptyState title="Nic jeszcze nie wysłano" hint="Wyślij projekt na serwer w kroku Wysyłka. Tutaj pojawi się jako program, na którym możesz uruchamiać komendy." />}
          <ul className="instances">
            {vm.instances.map((inst) => <InstanceCard key={inst.id} instance={inst} onRun={vm.executeCommand} />)}
          </ul>
        </div>
        <Output lines={vm.terminalLines} onClear={vm.clearTerminal} />
      </div>
    </>
  );
};
