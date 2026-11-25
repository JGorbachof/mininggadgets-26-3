# Mining Gadgets unofficial 26.3 port

Upstream: Direwolf20-MC/MiningGadgets, release v1.19.3, revision e36293a76d26f2bf5bf535e8483b8e505a890f59. MIT source license retained. Original credits in README.md include ErrorMikey, ItsTheBdoge (3D laser model), Soaryn and Rorax (original laser texture). Asset-specific redistribution rights still require review before publication.
Target: Minecraft 26.3 / NeoForge 26.3.0.118 / Java 25 / ModDevGradle 2.0.148.

Changes:
- Keep production source and registry IDs. Remove active upstream publishing and old upstream update notification; retain optional JEI compile-time API, do not load old JEI at runtime. JEI 26.3 integration unverified.
- Migrate removed IItemHandler filter storage to vanilla Container and ghost Slot; block ghost pickup/click mutation, clamp ghost entries to one, validate packet index and menu type.
- Migrate server configuration to SYNCED, piston reaction and inventory drop prediction.
- Migrate recipe/loot generator to registry bootstrap; update tag provider and all 27 recipe-unlock advancement JSON conditions to plural recipes. Preserve recipe and advancement IDs.
- Migrate GUI and hand rendering, beam and block overlay geometry to render-state extraction and custom geometry submission. Restore previously commented-out modification table upgrade-card overlay with ItemStackRenderState. Client rendering not executed.
- Original energy capability, mining code and upgrades retained, except compatibility changes above.

Evidence: ../mininggadgets-build-pass1.log (82 initial errors), pass2 (1 error), pass3 and pass4 (BUILD SUCCESSFUL). Initial server run failed loading old advancement format. Rebuilt JAR passed ../mininggadgets-qc/qc-pass2.log (2/2 required tests, clean shutdown); specific assertions on energy insert/extract, mining admission, upgrade persistence, filter clamping, rejection and clearing. No actual block-breaking or modification table end-to-end interaction verified yet. Full datagen, client rendering, network sync, multiplayer and external charger integration remain untested. No publication.

## Expanded mechanics validation (2026-11-25)
Build pass 5 fixes durability synchronization: deduplicate updates by position within each server dimension, send only to players with negotiated payload channels, and skip stale client positions instead of aborting the batch. Earlier tests exposed an obstructed test ray (passes 3/4), then a genuine unnegotiated-payload crash after otherwise successful mining (pass 5). Final packaged JAR passes qc-pass6.log and qc-pass7.log with clean shutdown. Real server-side gadget use destroys one stone block and a full 3x3 stone face, debiting exact energy for one and nine blocks respectively. Modification table installation, duplicate rejection, removal, returned card, reset range and all 27 recipe registrations also pass. Tests drive the real onUseTick using an embedded mock server player, not an actual connected client; multiplayer channel delivery and client visuals remain unverified.
Asset provenance: upstream root MIT license and README credits are present; model history includes Bdogs15, texture history includes Direwolf20/Soaryn and contributors, and all five sound files were introduced in Direwolf20 commit adba41b. No separate asset license or explicit restriction found in the tracked repository. This is repository-level evidence, not independent confirmation of all third-party asset rights. Preserve original credits and notices; review remaining uncertainty before public distribution.
