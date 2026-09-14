// VS Code extension for scalino-lsp (scalino's JVM-free Scala LSP -- see
// /README.md and /docs/findings.md "JVM-free language server (LSP)").
// Attaches to `.scala` files by document selector, independent of whatever
// grammar/language contribution owns the "scala" language id -- unlike Zed
// (see ../zed-extension/README.md), VS Code merges multiple extensions'
// `languages` contributions for the same id instead of picking one, so
// there's no collision to dodge if Metals is also installed. If both are
// installed and both attach, expect duplicate diagnostics/hover -- disable
// one for the workspace.
//
// No DAP support and no auto-download of `scalino-lsp` itself, same as the
// Zed extension: it isn't published anywhere generic, it's this repo's own
// dist/ build output (build/08-build-scalino-lsp.sh, or a release tarball).
// Resolved via `scalino-lsp.path` setting, falling back to a PATH lookup.

import * as fs from "fs";
import * as path from "path";
import * as vscode from "vscode";
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
} from "vscode-languageclient/node";

const SERVER_ID = "scalino-lsp";

let client: LanguageClient | undefined;

function isExecutable(file: string): boolean {
  try {
    fs.accessSync(file, fs.constants.X_OK);
    return true;
  } catch {
    return false;
  }
}

function findOnPath(name: string): string | undefined {
  const pathEnv = process.env.PATH ?? "";
  const exts = process.platform === "win32" ? [".exe", ".cmd", ".bat", ""] : [""];
  for (const dir of pathEnv.split(path.delimiter)) {
    if (!dir) continue;
    for (const ext of exts) {
      const candidate = path.join(dir, name + ext);
      if (isExecutable(candidate)) return candidate;
    }
  }
  return undefined;
}

function resolveServerPath(config: vscode.WorkspaceConfiguration): string {
  const configured = config.get<string>("path", "").trim();
  if (configured) return configured;

  const onPath = findOnPath(SERVER_ID);
  if (onPath) return onPath;

  throw new Error(
    `${SERVER_ID} not found on PATH -- build it (build/08-build-scalino-lsp.sh, ` +
      `or use a release tarball's dist/${SERVER_ID}) and either add dist/ to PATH ` +
      `or set "scalino-lsp.path" in settings.json`,
  );
}

export function activate(_context: vscode.ExtensionContext): void {
  const config = vscode.workspace.getConfiguration(SERVER_ID);

  let command: string;
  try {
    command = resolveServerPath(config);
  } catch (err) {
    vscode.window.showErrorMessage((err as Error).message);
    return;
  }

  const args = config.get<string[]>("args", ["-stdio"]);
  const extraEnv = config.get<Record<string, string>>("env", {});

  const serverOptions: ServerOptions = {
    command,
    args,
    options: { env: { ...process.env, ...extraEnv } },
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: "file", pattern: "**/*.scala" }],
    initializationOptions: config.get("initializationOptions"),
    synchronize: {
      fileEvents: vscode.workspace.createFileSystemWatcher("**/*.scala"),
    },
  };

  client = new LanguageClient(SERVER_ID, "Scalino LSP", serverOptions, clientOptions);
  client.start();
}

export function deactivate(): Thenable<void> | undefined {
  return client?.stop();
}
