-- Neovim wiring for scalino-lsp (scalino's JVM-free Scala LSP -- see
-- ../README.md and ../docs/findings.md "JVM-free language server (LSP)").
--
-- Unlike the vscode-extension/ and zed-extension/ directories, this isn't a
-- packaged plugin -- Neovim's own LSP client (vim.lsp, 0.11+) is generic
-- enough that no extension code is needed, just a client config. Source
-- this file from your init.lua, or copy it into your config and adjust.
--
-- No auto-download of `scalino-lsp` itself, same as the other two: it isn't
-- published anywhere generic, it's this repo's own dist/ build output
-- (build/08-build-scalino-lsp.sh, or a release tarball).

local M = {}

local function is_executable(path)
  return vim.fn.executable(path) == 1
end

-- Mirrors vscode-extension/src/extension.ts's resolveServerPath: an explicit
-- path wins, otherwise fall back to a PATH lookup.
local function resolve_cmd(opts)
  opts = opts or {}
  if opts.path and opts.path ~= "" then
    if not is_executable(opts.path) then
      vim.notify(
        "scalino-lsp: configured path is not executable: " .. opts.path,
        vim.log.levels.ERROR
      )
    end
    return opts.path
  end

  if is_executable("scalino-lsp") then
    return "scalino-lsp"
  end

  vim.notify(
    "scalino-lsp not found on PATH -- build it (build/08-build-scalino-lsp.sh, "
      .. "or use a release tarball's dist/scalino-lsp) and either add dist/ to "
      .. "PATH or pass { path = \"/absolute/path/to/scalino-lsp\" } to setup()",
    vim.log.levels.ERROR
  )
  return "scalino-lsp"
end

--- @param opts table|nil { path?: string, args?: string[], env?: table }
function M.setup(opts)
  opts = opts or {}
  local cmd = { resolve_cmd(opts), unpack(opts.args or { "-stdio" }) }

  vim.lsp.config("scalino_lsp", {
    cmd = cmd,
    filetypes = { "scala" },
    root_markers = { ".scalino-build", "build.sbt", ".git" },
    cmd_env = opts.env,
  })
  vim.lsp.enable("scalino_lsp")
end

return M
