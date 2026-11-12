# Repository Guidelines

## Codebase Research & Exploration
- **Never use `git grep`, `grep`, `Select-String`, or shell find commands.**
- **Always use `codegraph_explore`** exclusively for locating code, researching symbols, references, functions, call paths, blast radius, and usages in this codebase.
- Query `codegraph_explore` with symbol names, function names, or natural-language relationship queries (e.g., `"caller of ExportStatementPDFToWriter"`).
