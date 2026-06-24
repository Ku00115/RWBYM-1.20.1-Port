# RWBYM 1.20.1 Port Notes

This workspace is a Forge 1.20.1 port scaffold for `Blaez-Dev/RWBYM`.

Completed:
- Updated the Forge workspace metadata to `rwbym`.
- Added a 1.20.1 `@Mod` entry point.
- Added `mods.toml`.
- Migrated the original asset tree.
- Converted the English `.lang` file to `en_us.json`.
- Ported the original recipe JSON files from `assets/rwbym/recipes` to `data/rwbym/recipes`.
- Converted old `rwbym:smelting` recipe types to vanilla `minecraft:smelting`.
- Converted old metadata dye ingredients to 1.20 dye items.
- Added simple block loot tables and a pickaxe mineable tag.
- Normalized migrated resource ids to lowercase for 1.20's strict `ResourceLocation` validation.
- Removed invalid duplicate recipe file names with spaces/parentheses.
- Fixed un-namespaced recipe item ids.
- Migrated model texture references to 1.20-style `textures/block` and `textures/item` paths.
- Added compatibility copies for legacy `textures/blocks` and `textures/items` assets.
- Added generated placeholder textures for migrated models that referenced missing original PNGs.
- Replaced a few old oversized item models with lightweight generated-item models until their custom renderers are ported.
- Rebuilt the hard-light fence blockstate/model files with vanilla 1.20 fence parents.
- Rewrote legacy blockstate variants for placeholder blocks to 1.20-compatible empty variants and moved the misplaced `crush` model into `models/block`.
- Fixed remaining old vanilla block texture references in ore models.
- Added first-pass item behavior for placeholder registrations: armor-slot items, ammo/magazine stack sizes, single-stack charms/scrolls/cameras, and weapon durability.
- Registered migrated weapon sound events with Forge's 1.20 deferred registry.
- Added transitional weapon item classes: guns now play migrated shot sounds on right-click, and melee weapons expose basic attack attributes.
- Added transitional gun gameplay: server-side hitscan damage, generic ammo consumption, creative-mode bypass, and dry-fire sound when no ammo is available.
- Ported the first Aura capability core for 1.20.1: player attachment, NBT persistence, death/dimension clone copy, passive recharge, and `/rwbym aura` debug commands.
- Updated dust ore loot tables to drop matching dust rocks and added `needs_iron_tool` harvest tags.
- Added Forge 1.20 biome modifiers plus configured/placed features for the migrated dust ores.
- Removed invalid animation metadata from the generated `doom` placeholder texture.
- Added first-pass food/drink behavior and a ported Aura Regen mob effect.
- Added first-pass charm inventory effects as a transitional replacement until the original accessory systems are fully ported.
- Replaced vanilla armor-material placeholders with a RWBYM `huntsman` armor material.
- Added player-model armor rendering for migrated RWBYM armor sets, using the original `textures/models/armor/*_default.png` and `*_slim.png` character textures instead of a single generic armor layer.
- Fixed migrated RWBYM armor rendering to use a 1.20 `PlayerModel`-based armor model with the original skin UV layout and outer clothing parts, matching the 1.12 `ModelArmor` behavior for head/chest/legs and avoiding boots rendering over the full leg texture.
- Added dedicated 1.20 player armor model layers with the original 0.02 armor expansion so RWBYM skin-layout armor no longer renders flat on the player skin.
- Restored Kore Kosmou as special chest armor instead of a plain item, including its vanilla armor-layer textures, sneak-right-click form cycling between off/fire/ice/wind, and the original worn-form weapon creation for `kkfire`, `kkice`, and `kkwind`.
- Added first-pass migrated cosmetic wearables: hoods/masks/glasses/hats now equip into the head slot without being treated as vanilla armor, and RWBY limb cosmetics (`Ears`, `Tail`, `Body`, `Head`, arms, legs) can be applied/cleared and rendered on player model parts using the migrated item JSON models.
- Matched original `RWBYLimbItem` usage timing for ears/tails/horns/limbs: cosmetics now use the bow-style 32 tick action and apply/consume on release while still syncing appearance to tracking clients.
- Wired the original `RWBYHood` wearables into the armor perk system, including head-slot attribute modifiers, passive effects, AntiMagic clearing, combat perk lookup, and hood/headgear morph pairs for Ruby, Summer, and Taylor.
- Restored first-pass semblance coin behavior for the currently migrated coin items: using a coin sets the matching 1.20 generic semblance or raises its level if already active.
- Restored original-style durability and single-stack behavior for the migrated crusher tools: `chisel` and the current `crush` block item now take 255 uses when used by the crusher.
- Added a small Forge network channel for RWBY cosmetic appearance sync, so applied ears/tails/limbs are sent to the owning client and tracking players instead of staying server-only.
- Restored a dedicated RWBY cosmetic/mutation creative tab so ears, horns, tails, fins, Grimm body parts, and clear-slot tools no longer fall into the generic materials tab.
- Ported the original armor perk bitmask table for 163 migrated armor/charm entries and wired first-pass 1.20 behavior for movement, defense, vitality, attack, footing, rush, night vision, jump boost, fire resistance, and Aura Regen.
- Wired more original armor/charm perk behavior into 1.20 combat: `KINGSPAWN`/`KINGSGAMBIT` party damage, `JAVELIN` thrown-weapon damage, `HandofBullets` hitscan gun damage, dagger critical strikes, rapier/winter puncture, scythe reach, hammer/fist daze chance, and sword gladiator knockback. Charm inventory items now participate in the same perk checks as armor.
- Generated 237 migrated RWBY weapon profiles from the original 1.12 `RWBYGun` registrations, including damage, weapon type flags, ammo lists, recoil, elemental mode data, and 172 morph targets.
- Added weapon profiles for the two original `NPCRWBYSword` entries, `armasword` and `armaswordsummon`, so Armorgeist swords use the shared 1.20 weapon behavior instead of the basic fallback item.
- Replaced first-pass weapon placeholders with a shared 1.20 weapon item implementation: sneak-right-click morphing, profile-aware ammo checks, hitscan shooting, recoil, elemental hit effects, melee sweep/rapier/hammer/dagger behavior, and Aura/Grimm kill linkage.
- Expanded the shared weapon implementation with more original RWBYGun behavior: element-form switching, Aura-draining/channelled weapons, flight and wall-climb movement while using weapons, shield-style blocking use animations, shield passives, rage shield wither burst, and axe/pickaxe-style mining behavior.
- Added a generic rendered weapon projectile entity and routed thrown, boomerang, rocket, and bow-style weapons through visible projectiles instead of plain hitscan. Projectiles carry weapon display items, damage, elemental effects, simple rocket/grenade explosions, and boomerang return behavior.
- Expanded weapon profiles with original charge/shield/blocking flags, added charged release shooting for bow/rocket/Sanrei/Letz Stil-style weapons, and improved weapon tooltips with type, morph target, and ammo hints.
- Tightened weapon ammo handling so profile-specific ammo is preferred, ammo is checked before any stack is consumed, old missing placeholder ammo falls back to the generic ammo tag, and migrated weapons can be repaired with `rwbym:scrap`.
- Added more 1.12 RWBYGun parity to the shared weapon implementation: placeholder ammo ids are ignored during matching, Sanrei/Letz Stil/Aura weapons require and spend Aura for shots, Grimm/whip/staff special effects were expanded, and returning projectile weapons no longer duplicate their display item on return.
- Registered missing profile-only ammo items and lightweight generated models for migrated weapon ammo ids, then added them to the `rwbym:ammo` tag.
- Restored legacy item model predicates on the client for migrated weapon JSON overrides (`pull`, `pulling`, `blocking`, `offhand`, and `mainhand` variants), so charge/block/dual-wield visual states can resolve in 1.20.1.
- Finished the current weapon data pass: all 242 migrated weapon profiles have registered items, models, morph targets, and registered RWBYM ammo references. Projectile weapons now carry the firing weapon stack for modifier enchantment effects, and P90/Hecate magazine handling supports chamber cycling plus ejecting partially loaded magazines.
- Restored visible projectile and camera recoil behavior for guns: ranged RWBYM weapon profiles now spawn item-rendered projectile entities using the fired ammo model/texture instead of invisible hitscan, fast gun rounds use short-lived high-speed projectiles, and server-side recoil is mirrored to the client camera through a dedicated packet.
- Restored P90/Hecate special gun controls for 1.20.1: client key mappings now send explicit shoot, ADS, slide/bolt cycle, remove-round, hammer, magazine release, magazine insert, and fire-select packets to the server, and the shared weapon item tracks chambered rounds, magazines, hammer state, slide/bolt state, ADS, trigger hold, and fire mode for item predicates.
- Added a 1.20 resource-generation compatibility path for the original special gun Blockbench models. `tools/generate_special_gun_models.mjs` converts the original `.bbmodel` files, generates P90/Hecate ADS/mag-out/slide-back/bolt-back model variants, writes item override predicates, and is wired into Gradle before `processResources`.
- Added RWBYM creative-mode category tabs for weapons, armor, charms, materials, blocks, entities/spawn eggs, and the all-items tab.
- Expanded the `rwbym:ammo` tag from weapon profile ammo data.
- Added a `rwbym:ammo` item tag and moved transitional gun ammo checks to prefer the tag.
- Fixed the impure ore loot target and normalized additional small/non-power-of-two placeholder textures to remove mip warnings.
- Added the first migrated Grimm entity scaffold: `beowolf` entity type, attributes, spawn placement, overworld biome spawns, renderer registration, spawn egg, model, and localization.
- Added first-pass `ursa`, `boarbatusk`, and `creep` Grimm entity registrations with attributes, spawn placements, biome spawns, temporary renderers, spawn eggs, models, and localization.
- Restored registry coverage for all 44 original `RWBYEntities` ids in 1.20.1, including legacy projectile aliases, winter/summoned Grimm, bosses, flying Grimm, Arachne/Arachne clone, Hollow, Atlas Knight, Blake/Ren/Ragora/Zwei summons, and store/weapon/black-market/armor/crowbar NPCs.
- Added first-pass attributes, spawn placements, texture-backed renderers, spawn eggs, localization, and standard Grimm loot tables for the restored entity set. Dedicated original AI, merchant trading, and per-entity model ports remain follow-up work.
- Added first-pass Grimm entity loot tables for remnants and lien drops.
- Linked transitional weapons, Grimm, and Aura: killing basic Grimm with guns or melee weapons restores a small amount of Aura.
- Replaced the temporary all-zombie Grimm client renderers with transitional textured Grimm renderers that use the migrated entity textures and per-Grimm scaling.
- Expanded `BasicGrimmEntity` with first-pass Grimm behavior differences: sunlight immunity, Apathy weakening aura, Lancer aerial movement, Deathstalker poison, charging knockback for Boarbatusk/Sabyr, and heavy Grimm slowing hits.
- Added first-pass 1.20 interactions for migrated utility blocks: Grimm bait summons random Grimm, toolkit repairs a held damaged item for 5 levels and consumes itself, and crusher/crush blocks process dust rocks/crystals into dust/cut crystals without the old GUI yet.
- Registered placeholder 1.20.1 content for the migrated assets:
  - 28 block registry entries from `assets/rwbym/blockstates`.
  - 817 item registry entries from `assets/rwbym/models/item` plus the missing `hbangle` recipe item.
- Verified `gradlew build` succeeds.
- Verified `gradlew runClient` reaches normal client resource/audio initialization after the weapon predicate and ammo updates.

Important limitations:
- The original RWBYM codebase targets Forge 1.12.2. Its gameplay systems cannot be compiled directly on 1.20.1.
- Weapons, armor abilities, entities, world generation, GUI screens, networking, capabilities, tile entities, fluids, recipes, and custom renderers still need to be ported module by module.
- Current registered items and blocks are functional placeholders so the project can build and load while those systems are migrated.

Suggested next modules:
1. Port item properties, armor materials, and creative tab grouping.
2. Port projectile and weapon behavior.
3. Port Grimm entities and renderers.
4. Port capabilities, commands, networking, and GUI screens.
5. Replace placeholder block classes with proper machines, fluids, and light-emitting/custom-shape blocks.
