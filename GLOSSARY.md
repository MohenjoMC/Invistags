# Glossary

Domain and design terms as this repository uses them. Keep entries short; add one when a
concept gains a module, a command or a test.

## Domain

**Invisible player** — a player entity with the invisibility effect, as seen by the local
client. Excludes the local player and spectators for every reveal and alert decision.

**Reveal** — forcing something the client would otherwise hide: a player's body/skin, a
nametag, a mob's body, or stripped equipment. Each reveal is a toggle and is applied in the
render path, never to the server's view of the world.

**Gear reveal** — restoring armour and held items on an invisible player from the last
equipment the client actually received. A client cannot invent gear the server never sent,
and gear swapped while the player stays invisible is invisible to this cache too.

**Toast episode** — one continuous stretch of a player being invisible. A player is announced
at most once per episode, when they first come within the alert radius. The episode ends when
the player becomes visible or leaves the world; it does *not* end by walking out of range. The
end of an episode re-arms the next one.

**Toggle catalogue** — `Toggle`: the single declaration of each user-facing setting (command
name, config key, default, labels). The `/invis` command, the config screen, persistence, the
status dump and the tests are all generated from it; adding a setting is one constant plus
translations.

## Design vocabulary

Used by the architecture review and by commit messages.

**Module** — anything with an interface and an implementation: a function, class, package, or
a slice spanning tiers. Not "component", "service" or "API".

**Interface** — everything a caller must know to use a module correctly: types, invariants,
ordering, error modes, required configuration.

**Depth** — behaviour a caller gets per unit of interface they must learn. Deep is a large
implementation behind a small interface; shallow is an interface nearly as large as what sits
behind it.

**Seam** — the place where a module's interface lives; where behaviour can change without
editing there.

**Adapter** — a concrete thing filling a slot at a seam. One adapter is a hypothetical seam;
two adapters make it real.

**Leverage** — what callers get from depth. **Locality** — what maintainers get: change, bugs
and verification concentrate in one place.
