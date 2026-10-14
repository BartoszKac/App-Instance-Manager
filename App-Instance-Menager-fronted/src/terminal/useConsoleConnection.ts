import { useCallback, useEffect, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';

const WS_URL = 'http://localhost:8888/ws-console';
const TOPIC = '/topic/output';
const SCRIPTS = [
  'https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js',
  'https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js',
];

export type LineType = 'output' | 'success' | 'error' | 'info';
export type ConnectionState = 'connecting' | 'connected' | 'disconnected';

export interface ConsoleLine {
  id: number;
  text: string;
  type: LineType;
}

const scriptLoads = new Map<string, Promise<void>>();

const loadScript = (src: string) => {
  const pending = scriptLoads.get(src) ?? new Promise<void>((resolve, reject) => {
    const s = document.createElement('script');
    s.src = src;
    s.onload = () => resolve();
    s.onerror = () => { scriptLoads.delete(src); reject(new Error(`Nie udało się załadować ${src}`)); };
    document.head.appendChild(s);
  });
  scriptLoads.set(src, pending);
  return pending;
};

const loadLibraries = async () => {
  for (const src of SCRIPTS) await loadScript(src);
  const { SockJS, Stomp } = window;
  if (!SockJS || !Stomp) throw new Error('Biblioteki WebSocket nie są dostępne');
  return { SockJS, Stomp };
};

export const useConsoleConnection = () => {
  const [lines, setLines] = useState<ConsoleLine[]>([]);
  const [state, setState] = useState<ConnectionState>('connecting');
  const clientRef = useRef<StompClient | null>(null);
  const idRef = useRef(0);

  const addLine = useCallback((text: string, type: LineType = 'output') => {
    setLines((prev) => [...prev.slice(-1999), { id: idRef.current++, text, type }]);
  }, []);

  const start = useCallback(() => {
    let cancelled = false;
    loadLibraries()
      .then(({ SockJS, Stomp }) => {
        if (cancelled) return;
        const client = Stomp.over(new SockJS(WS_URL));
        client.debug = null;
        client.connect({}, () => {
          clientRef.current = client;
          setState('connected');
          client.subscribe(TOPIC, (msg) => addLine(msg.body));
        }, () => {
          setState('disconnected');
          addLine('Brak połączenia z serwerem. Sprawdź, czy backend działa, i połącz ponownie.', 'error');
        });
      })
      .catch((err: unknown) => { setState('disconnected'); addLine(errorMessage(err), 'error'); });
    return () => { cancelled = true; };
  }, [addLine]);

  const connect = useCallback(() => {
    setState('connecting');
    start();
  }, [start]);

  const disconnect = useCallback(() => {
    clientRef.current?.disconnect();
    clientRef.current = null;
    setState('disconnected');
  }, []);

  useEffect(() => {
    const cancel = start();
    return () => { cancel(); clientRef.current?.disconnect(); clientRef.current = null; };
  }, [start]);

  return { lines, state, connect, disconnect, clear: () => setLines([]) };
};
