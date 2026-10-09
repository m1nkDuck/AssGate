# Ashen Belfry animation assets

Generated with the built-in `image_gen` tool, then extracted and packed for MIDP with nearest-neighbour scaling and binary alpha. Original generated sheets remain here for editing; the game loads the packed atlases in `res`.

| Character | Selected source sheets | Game atlas | Poses |
| --- | --- | --- | --- |
| Belfry guard | [guard.png](guard.png), two repaired passing poses from [guard_swords_fixed.png](guard_swords_fixed.png) | [belfry-guard-atlas.png](../../res/belfry-guard-atlas.png) | 32: four directions, eight poses per direction |
| Elite spearman | [spearman_walk_final.png](spearman_walk_final.png), [spearman_actions.png](spearman_actions.png) | [belfry-spearman-atlas.png](../../res/belfry-spearman-atlas.png) | 32: four directions, eight poses per direction |
| Bellbound | [bellbound_body.png](bellbound_body.png) | [bellbound-atlas.png](../../res/bellbound-atlas.png) | 40: four directions, ten poses per direction |

Both soldiers use idle, four alternating walking poses, windup, strike and fallen poses. Their hit and collapse effects are rendered in the game. Bellbound uses idle, four walking poses, windup, sweep, slam, toll/phase brace and fallen poses; its chain and bell remain native MIDP drawing so they follow the actual attack geometry.

The selected source sheets and their hashes, source bounds, directions, anchors and scales are recorded in [guard-frames.json](guard-frames.json), [spearman-frames.json](spearman-frames.json) and [bellbound-frames.json](bellbound-frames.json).

Full saved prompt set:

- [Initial character and action prompts](prompts.txt)
- [Spearman walk and action repair prompts](spearman-fix-prompts.txt)
- [Alternating leg repair prompt](spearman-walk-fix-prompt.txt)
- [Final guard weapon and spearman walk prompts](final-repair-prompts.txt)

Repack with `python tools/pack_belfry_sprites.py` and `python tools/pack_bellbound_sprites.py` from the project root. The spearman walk packer follows transparent gutters between touching source rows; it retains source pixels rather than cutting along a straight horizontal grid.

The earlier bonfire resting sheet is [knight_rest.png](../knight-sheets/knight_rest.png), with its saved [prompt](../knight-sheets/rest-prompt.txt), [metadata](../knight-sheets/rest-frames.json) and [game atlas](../../res/hero-rest-atlas.png).
