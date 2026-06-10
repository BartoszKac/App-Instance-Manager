import { useState, useEffect, useRef, useCallback } from "react";

const WS_URL = "http://localhost:8888/ws-console";
const SUBSCRIBE_TOPIC = "/topic/output";

function loadSockJS(cb) {
  if (window.SockJS) return cb();
  const s = document.createElement("script");
  s.src = "https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js";
  s.onload = cb;
  document.head.appendChild(s);
}

function loadStomp(cb) {
  if (window.Stomp) return cb();
  const s = document.createElement("script");
  s.src = "https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js";
  s.onload = cb;
  document.head.appendChild(s);
}

const MIN_HEIGHT = 32;  // zwinięty — tylko titlebar
const MAX_HEIGHT = 600;
const DEFAULT_HEIGHT = 200;

export default function PackageTerminal() {
  const [lines, setLines] = useState([
    { id: 0, text: "Package Terminal — oczekiwanie na dane z serwera…", type: "info" },
  ]);
  const [connected, setConnected]   = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [height, setHeight]         = useState(DEFAULT_HEIGHT);
  const [collapsed, setCollapsed]   = useState(false);

  const stompRef    = useRef(null);
  const bottomRef   = useRef(null);
  const idRef       = useRef(1);
  const dragRef     = useRef({ active: false, startY: 0, startH: 0 });

  const addLine = useCallback((text, type = "output") => {
    setLines((prev) => [...prev, { id: idRef.current++, text, type }]);
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [lines]);

  // ── drag logic ──────────────────────────────────────────────
  const onMouseDown = useCallback((e) => {
    if (collapsed) return;
    e.preventDefault();
    dragRef.current = { active: true, startY: e.clientY, startH: height };

    const onMove = (ev) => {
      if (!dragRef.current.active) return;
      const delta = dragRef.current.startY - ev.clientY;
      const next  = Math.min(MAX_HEIGHT, Math.max(MIN_HEIGHT, dragRef.current.startH + delta));
      setHeight(next);
    };

    const onUp = () => {
      dragRef.current.active = false;
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup", onUp);
    };

    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup", onUp);
  }, [height, collapsed]);

  // ── websocket ────────────────────────────────────────────────
  const connect = useCallback(() => {
    setConnecting(true);
    loadSockJS(() =>
      loadStomp(() => {
        const sock   = new window.SockJS(WS_URL);
        const client = window.Stomp.over(sock);
        client.debug  = null;

        client.connect({}, () => {
          stompRef.current = client;
          setConnected(true);
          setConnecting(false);
          addLine("✓ Połączono z serwerem", "success");
          client.subscribe(SUBSCRIBE_TOPIC, (msg) => addLine(msg.body, "output"));
        }, (err) => {
          setConnecting(false);
          setConnected(false);
          addLine(`✗ Błąd połączenia: ${err}`, "error");
        });
      })
    );
  }, [addLine]);

  const disconnect = useCallback(() => {
    stompRef.current?.disconnect();
    stompRef.current = null;
    setConnected(false);
    addLine("Rozłączono.", "info");
  }, [addLine]);

  const lineColor = (type) => {
    switch (type) {
      case "success": return "#7ee787";
      case "error":   return "#ff7b72";
      case "info":    return "#8b949e";
      default:        return "#e6edf3";
    }
  };

  const toggleCollapse = () => {
    setCollapsed((v) => !v);
  };

  return (
    <div
      style={{
        fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', monospace",
        background: "#0d1117",
        borderTop: "1px solid #30363d",
        display: "flex",
        flexDirection: "column",
        height: collapsed ? `${MIN_HEIGHT + 28}px` : `${height}px`,
        minHeight: collapsed ? `${MIN_HEIGHT + 28}px` : `${MIN_HEIGHT}px`,
        width: "100%",
        boxSizing: "border-box",
        flexShrink: 0,
        transition: collapsed ? "height 0.2s ease" : "none",
        position: "relative",
      }}
    >
      {/* Drag handle */}
      {!collapsed && (
        <div
          onMouseDown={onMouseDown}
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            height: "5px",
            cursor: "ns-resize",
            zIndex: 10,
            background: "transparent",
          }}
        />
      )}

      {/* Title bar */}
      <div
        style={{
          background: "#161b22",
          borderBottom: "1px solid #30363d",
          padding: "4px 14px",
          display: "flex",
          alignItems: "center",
          gap: "8px",
          flexShrink: 0,
          userSelect: "none",
        }}
      >
        {/* Collapse toggle */}
        <button
          onClick={toggleCollapse}
          title={collapsed ? "Rozwiń terminal" : "Zwiń terminal"}
          style={{
            background: "transparent",
            border: "none",
            color: "#8b949e",
            cursor: "pointer",
            fontSize: 11,
            padding: "0 4px",
            lineHeight: 1,
            transform: collapsed ? "rotate(180deg)" : "rotate(0deg)",
            transition: "transform 0.2s",
          }}
        >
          ▼
        </button>

        <span style={{ fontSize: 11, color: "#8b949e", letterSpacing: "0.05em" }}>OUTPUT</span>

        <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8 }}>
          <span
            style={{
              width: 7, height: 7, borderRadius: "50%",
              background: connected ? "#28c840" : connecting ? "#febc2e" : "#ff5f57",
              display: "inline-block",
              transition: "background 0.3s",
            }}
          />
          <span style={{ fontSize: 11, color: "#8b949e" }}>
            {connected ? "połączono" : connecting ? "łączenie…" : "rozłączono"}
          </span>
          {!connected ? (
            <button
              onClick={connect}
              disabled={connecting}
              style={{
                fontSize: 11, padding: "2px 8px",
                background: "transparent", border: "1px solid #388bfd",
                color: "#58a6ff", borderRadius: 4,
                cursor: connecting ? "not-allowed" : "pointer",
                opacity: connecting ? 0.6 : 1,
              }}
            >
              Połącz
            </button>
          ) : (
            <button
              onClick={disconnect}
              style={{
                fontSize: 11, padding: "2px 8px",
                background: "transparent", border: "1px solid #30363d",
                color: "#8b949e", borderRadius: 4, cursor: "pointer",
              }}
            >
              Rozłącz
            </button>
          )}
          <button
            onClick={() => setLines([])}
            style={{
              fontSize: 11, padding: "2px 8px",
              background: "transparent", border: "1px solid #30363d",
              color: "#8b949e", borderRadius: 4, cursor: "pointer",
            }}
          >
            Wyczyść
          </button>
        </div>
      </div>

      {/* Output area */}
      {!collapsed && (
        <div
          style={{
            flex: 1, overflowY: "auto",
            padding: "8px 16px",
            display: "flex", flexDirection: "column", gap: 1,
          }}
        >
          {lines.map((l) => (
            <div
              key={l.id}
              style={{
                fontSize: 12, lineHeight: 1.6,
                color: lineColor(l.type),
                whiteSpace: "pre-wrap", wordBreak: "break-all",
                userSelect: "text",
              }}
            >
              {l.text}
            </div>
          ))}
          <div ref={bottomRef} />
        </div>
      )}
    </div>
  );
}