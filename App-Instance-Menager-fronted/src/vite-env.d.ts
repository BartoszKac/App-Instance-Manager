/// <reference types="vite/client" />

declare module "*.css";
interface StompMessage {
  body: string;
}

interface StompClient {
  debug: ((message: string) => void) | null;
  connect(headers: Record<string, string>, onConnect: () => void, onError: (error: unknown) => void): void;
  subscribe(destination: string, callback: (message: StompMessage) => void): void;
  disconnect(): void;
}

interface Window {
  SockJS?: new (url: string) => unknown;
  Stomp?: { over(socket: unknown): StompClient };
}
