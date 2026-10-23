**MoreRefugeeActions** is a mod for for game [Space Haven](https://bugbyte.fi/spacehaven/).

# Features

## Limited schedule management

One of 3 schedule shits (offset by 8 hours) can be assigned to refugees and prisoners.

New refugees get automatically split between shifts. Player can cycle between shifts for character with new button in selection card.

## Recruitment

Refugees can be directly recruited to player crew if mood requirements are met. Social ties with existing crew can soften thresholds (see below).

## Eden drop off

Any refugees can be droped off on Eden after completion of Exodus quest line for minor credits reward. Current formula is `(100 + longtem_mood) * (1 + sum_of_skill_level/40)` credits.

## AI reaction to refugees

AI will not actively seek own faction refugees if *ALL* of them are fine with staying on players fleet (see below).

If any of them are not happy enough or if player manually enters refugee-related communication subdialog vanilla logic applies. Unfortunately comms are very inconvenient to mod for more gradual control.


# Refugee stances

Refugee can be either discontent (actively wants to return to home faction), neutral (fine with staying on players fleet for now but will not accept hire yet) or hireable.

Stance depends on combination of relationships with player crew, personal longterm mood and average crew mood. For relationships only best pair is considered, if no social contact with crew was made, "acquaintance" is taken as default level.

| Relationships | stay own | stay crew | hire own | hire crew |
| ---           | ---      | ---       | ---      | ---       |
| Enemies       | 70       | 70        | 75       | 75        |
| Dislike       | 65       | 65        | 70       | 70        |
| Acquaintance  | 60       | 60        | 65       | 65        |
| Friends       | 50       | 50        | 55       | 55        |
| Good Friends  | 40       | 40        | 40       | 40        |
| Best Friends  | 30       | 0         | 30       | 30        |
| Lovers        | 30       | 0         | 30       | 30        |

Current stance is indicated with icon in character selection card.
