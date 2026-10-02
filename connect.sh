#!/usr/bin/env bash
# Plain socket REPL client. Use rlwrap for line editing/history if you have it.
if command -v rlwrap >/dev/null; then exec rlwrap nc 127.0.0.1 5555; else exec nc 127.0.0.1 5555; fi
