# ADR-0001: Share mapping-independent policy across version branches

- **Status:** Accepted
- **Date:** 2026-11-11

## Context

Invistags ships four branches of the same mod — `main` (1.21.11), `26.1`, `26`, `26.3` — because
the rendering API differs substantially between the lines (`PlayerEntityRenderer` vs
`AvatarRenderer`, `OrderedRenderCommandQueue` vs `SubmitNodeCollector`, different render-state
field names). The README keeps a twelve-row table of those differences.

Most of the mod is not rendering API. The toggle catalogue, the JSON config schema, the rule that
decides whether an invisible entity's label is forced, the toast episode state machine, and the
"which players count" filter only use `java.util` and, at most, `FabricLoader`. They were living in
the client source set and being ported by hand four times. The 1.2.0 toast rewrite
(`VanishDetector`/`VanishedPlayers` → `ToastEpisodes`) landed on `main` only; the other branches
still carried the old modules, and nothing failed.

## Decision

Mapping-independent policy lives in `src/main/java/org/codersoft/cleaspfabric/`:

- `Toggle`, `ModConfig` — settings and persistence
- `NametagPolicy` — force a label? add its through-block copy?
- `ToastEpisodes` — one toast per invisibility episode

`src/client` keeps only adapters that name a Minecraft client type: the entrypoint, the config
screen, the notifier, the gear cache, `PlayerRoster` (the self/spectator filter, whose rule is a
static predicate the tests cover) and the mixins. A version branch copies `src/main` unchanged and
rewrites `src/client`.

## Consequences

- A behaviour change lands once, and the unit tests for it run on every branch.
- A new branch cannot silently skip a policy module: the adapter fails to compile without it.
- `src/main` is compiled into the artifact on both sides. Nothing there touches a client-only
  class, so this is inert on a dedicated server.
- The render adapters still diverge per branch by design; this ADR does not try to abstract the
  render API itself.
