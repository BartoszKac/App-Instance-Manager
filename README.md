BackRunner — A web-based IDE for compiling and running Java, Python, and C++ code dynamically on the server in real time.

🛠️ Tech Stack
Backend:

Java 21
Spring Boot 4.0
Spring WebSocket (STOMP)
Maven

Frontend:

React 19
Vite
Monaco Editor (@monaco-editor/react)
SockJS + StompJS


✨ Key Features

Multi-language Execution — Supports Java, Python, and C++ out of the box. Each language has its own dedicated handler (JavaHandler, PythonHandler, CppHandler) implementing a common LanguageHandler interface, making it easy to add more languages.
Dynamic Compilation & Running — Send source code from the browser and compile/run it on the server on demand, without restarting the application.
Real-time Output Streaming — Program output (stdout/stderr) is streamed live to the browser via WebSocket (STOMP protocol), so you see results as they happen.
Multi-file Project Support — Manage multiple source files as a single project. The backend tracks related files and compiles them together, with the first file treated as the entry point.
In-browser Code Editor — Full-featured Monaco Editor (the same engine as VS Code) with syntax highlighting, autocomplete, and formatting built in.


📸 Screenshots

<img width="1854" height="918" alt="image" src="https://github.com/user-attachments/assets/ad292152-65ac-464c-a6c2-66b0e4521db9" />
