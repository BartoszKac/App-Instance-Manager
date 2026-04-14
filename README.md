BackRunner — A web-based IDE for compiling and running Java code dynamically on the server in real time.

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

Dynamic Java Compilation — Send Java source code from the browser and compile it on the server using the Java Compiler API (javax.tools), without restarting the application.
Real-time Output Streaming — Program output (stdout/stderr) is streamed live to the browser via WebSocket (STOMP protocol), so you see results as they happen.
Multi-file Project Support — Manage multiple .java files as a single project. The backend tracks related files and compiles them together, with the first file treated as the entry point.
In-browser Code Editor — Full-featured Monaco Editor (the same engine as VS Code) with Java syntax highlighting, autocomplete, and formatting built in.


📸 Screenshots

Add screenshots by uploading .png files to the repository and replacing the placeholders below:

![IDE View](screenshots/ide-view.png)
![Terminal Output](screenshots/terminal-output.png)
Tip: Run the app locally, take a screenshot of the editor + terminal panel, and save it as screenshots/ide-view.png in the repo.