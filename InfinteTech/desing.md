# Infinite Tech concept

Tech system in current version of game seems to be in weird state. At start you need everything at once but then run out of useful things to research fairly fast.

Goal of this concept is to keep players engaged with tech system whole run. That can be achieved by introduction of new special set of technologies — infinite tech — which can be researched repeatedly providing minor-yet-useful bonus each time. To account for growing power base of players and to push them to further invest into research each step should cost more points as well.

Due to significantly different interactions with gameplay loop I decided to split all infinite technologies into two groups: combat and core.

## Combat technologies

Combat tech are all about sword vs shield balance, each offensive technology has defensive counterpart fully negating its bonus at same tech level which effectively returns balance to original. Additionally power difference of tech should depend only on relative level difference regardless of absolute number to maintain system relevance, e.g. level 3 vs level 5 should be same as 12 vs 14. To achieve this bonus of each level should be multiplicative, +5% per level in context of combat tech in this document means that output is multiplied by `1.05^level` and -5% means `1 / 1.05^level`.

Every faction slowly advances thru this branch of technologies at same speed. Overall it can be represented by singular global combat tech level value or potentially directly derived from game date. Specific tech level may differ slightly between factions for lore reasons.

## Catch up and soft limiting

Generally it is up to player to stay ahead of curve to gain some advantage or suffer from being behind. However there should be reasonable window around global tech level where player is expected to stay to maintain gameplay quality regardless of play style. For now I chose ±3 levels as such window.

There are few things that can be done to help with it:

* Tech points cost depends on relative difference with global tech level. Multiplier will be `2^(target_level - global_level - 1)`, which means that researching one level ahead of base costs normal amount of points, while 3 levels ahead will cost x4. On the other hand if player manage to fall behind 3 levels next tech will cost only x0.125. If global tech changes midway any extra already invested tech points will carry to next level.

* Research stations, Exodus fleet and some other station and ship types can offer combat technologies for credits or prestige. Offered levels are limited to faction tech, anything ahead of global level has very low chance to appear and is extortionately expensive.

* Derelicts may contain blueprints or research notes which will provide some tech points for specific topic.

### List of combat technologies

*I am actually not sure which weapons have which damage type, consult lore specialist. Current spread makes some sense for balance between different tech.*

**Kinetic damage**: +5% damage for point defense turret, star fighter, pistol, smg, shotgun, riffle.

**Low energy weapons damage**: +5% damage for autoturret, laser pistol and riffle, stun pistol and riffle, sentry turrets, combat robots.

**High energy weapons damage**: +5% damage for energy turret, plasma riffle and clustergun.

**Explosive and incendiary damage**: +5% for rocket turret, grenade launchers and flamethrower.

**Personal protection**: -5% catch-them-all damage taken for crew, including stamina damage from stun guns. Multiplicative with armor.

**Reinforced hull**: +5% max hull stability and stabilization rate. +5% hp for shuttle, pod, star fighter and all robots.

**Advanced shielding**: +5% shield capacity and charging rate.

**Non-player tech**: Aliens and rogue bots get +5% damage and hp per global tech level. Asteroids do not trend to evolve, unsure if it makes more sense to tie them to tech or rather make them harder further into galaxy. Or maybe just leave them as is and watch your advanced point defense delete them on edge of range.

## Core technologies

Core technologies are fundamentally different from combat counterpart.

There is no race with other factions and it is fairly hard to make any visible use of them for NPCs because that would require to have alternative versions of theirs ship and station designs for different tech levels.

On the other hand cost increase per level should probably be much higher than for combat tech with exponential growth to soft limit power gain. Presumably player should end up around level 5 by end of main campaign and potentially eventually reach level 10 with further core research being way too expensive. However there may be issue in (very) late game with inconvenient research management where single core tech may delay combat tech for way too much unless manually reordered.

Early levels should be fairly easy to research, accounting for that it may be reasonable to decrease baseline effectivity of affected systems slightly.

Unlike combat tech all core bonuses are additive between levels to avoid exponential growth unless stated otherwise. *Generally that means that returns are somewhat diminishing, unsure if that is good combined with exponential costs.*

### List of core technologies

**Processing optimization**: +10% system points per level.

**Energy handling**: +10% power output for all generators, +5% power node output. *That indirectly effects energium consumption too.*

**Hyperdrive carry capacity**: +5% carry capacity for all hyperdrivers.

**Improved batteries**: +5% power capacity for everything including bots, remote control and sentries.

**Improved robot chassis**: +5% movement speed for logistic, salvage and combat robots.

**Shuttle movement speed**: +5% shuttle movement speed. *Unsure about pods because that would be logical to effect fighter speed too and that is meaningful combat characteristic.*

**Storage optimization**: +5% storage space.

**Thermal regulator effectivity**: +10% effectivity of thermal regulator.

**Advanced medical equipment**: +10% medical bed recovery speed (as in multiplier to its base bonus, e.g. +55% recover speed for advanced medical bed on level 1).

**Hypersleep chamber effectivity**: -10% energy rods consumption. Multiplicative between levels, so `0.9^level`.

**Ore processing speed**: +10% ore processing speed, +5% mining speed.

**Decompose speed**: +10% composter speed.

**Plant growth speed**: +10% growth speed, multiplicative with other bonuses (botany is still important).

**Advanced gas handling**: +10% to oxygen generator, gas scrubber and CO2 producer speed. +5% for oxygen capacity of space suits, shuttles and pods.

**Industry ∞**: +5% crew production speed.
