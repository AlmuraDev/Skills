Skills
=============
RPG skills for Sponge: players level up Mining, Woodcutting, Digger, Farming and Crafting by playing, earn money as they go, and
need levels to use better tools and work rarer blocks.

This branch (`API-21-almura`) targets **SpongeAPI 21** (SpongeNeo, Minecraft 26.3) for Almura 4.0. The SpongeAPI 7 version lives on
`api-7-almura`.

## Prerequisites
* [Java] 25
* Gradle 9.7.1 (the wrapper downloads it)

## Building
Run `./gradlew build` (or `gradlew.bat build` on Windows). The plugin is `build/libs/Skills-<version>.jar`; the `-slim` jar lacks the
shaded formula library and is not meant for servers.

The jar brings its own H2 2 database driver (loaded in a class loader of its own) and exp4j (relocated), so it needs nothing besides
SpongeNeo. EconomyLite (or any economy plugin) is needed for money rewards, UltimateChat for the Discord level up relay; both are
optional.

## Configuration
Everything is in `config/skills`:

* `skills.conf`: the database file, which worlds share skills (the Nether and The End use the Overworld's by default) and the save
  interval.
* `skills/<name>.conf`: one file per skill. The file name is the skill's id (`skills:<name>`). It holds the display name (`&` color
  codes), `max-level`, `level-formula` (total experience for level `L`), `economy-formula` (money for level `L` and a rule's money
  modifier `M`), the `level-up-firework` and the rules.
* `smelting-data.conf`: written by the plugin, who loaded which furnace and unpaid smelting XP.
* `skills-h2.mv.db`: the experience database.

The default files are written when missing; the skill files only when the `skills` folder doesn't exist yet, so a skill you delete
stays deleted. Adding or removing a skill file needs a restart; `/skills reload` (or `/sponge plugins refresh`) re-reads the rules and
fireworks.

### Rules
Rules sit in `groups`. A group sets defaults for its `rules`, and every rule can override them:

```hocon
groups = [
  {
    trigger = break       # break, place, interact-block, use-tool, craft, drop or smelt
    creator = natural     # block triggers only: any (default), natural, placed, own, own-or-natural
    deny = break          # verb of the "You require <skill> level N to <verb> this." message; leave out for no message
    rules = [
      { blocks = ["minecraft:coal_ore", "minecraft:deepslate_coal_ore"], level = 15, xp = 8, money = 1 }
      { blocks = ["minecraft:wheat[age=7]"], xp = 1, money = 0.25 }
    ]
  }
]
```

* `blocks` (break, place, interact-block) or `items` (use-tool, craft, drop, smelt) list what a rule matches: an id
  (`minecraft:stone`), a block with properties (`minecraft:wheat[age=7]`, unlisted properties may be anything), a tag
  (`#minecraft:logs`) or a whole namespace (`almura:*`).
* `level`: the skill level needed. Below it the action is denied (the block doesn't break, the tool isn't used, the craft is
  cancelled, the drop is removed); for smelt it only withholds the XP.
* `xp` and `money`: what the rule pays. `money` is the `M` of the economy formula.
* Within one skill and trigger the first matching rule wins, so put specific rules (a mature crop) before general ones (any age).
* Unknown ids (e.g. of a mod that isn't installed) are left out with a note in the log; turn on debug logging to list them.

Rewards are only paid once the action really happened (no other plugin cancelled it). Placing rewards are guarded against
place/break loops: a position pays again only after a rewarded harvest of it or after 10 minutes. Smelting XP goes to the player who
put the items into the furnace, also when they are offline at the time (paid when they return).

## Commands
| Command | Permission |
| --- | --- |
| `/skills [player] [world]` | `skills.command.info` (`.other` for other players) |
| `/skills xp <player> [world] <skill> <add\|remove\|set> <xp>` | `skills.command.xp` (`.other`) |
| `/skills reset <player> [world]` | `skills.command.reset` (`.other`) |
| `/skills reload` | `skills.command.reload` |

## Notification permissions
Per skill, by its lower case name (e.g. `mining`):

* `skills.notification.xp.<skill>`: XP gained in the action bar
* `skills.notification.level.<skill>`: own level ups in chat; `skills.notification.level.<skill>.other`: other players' level ups
* `skills.notification.deny.<verb>.<skill>`: the level required messages
* `skills.effect.firework.<skill>`: a firework on level up (not while sneaking)

## API
Other plugins reach the skills through `org.inspirenxe.skills.api.Skills.service()` and can listen to `ChangeExperienceEvent`
(`Pre` is cancellable and can change the new experience; `Post.Level` fires when the level changed).

[Java]: https://adoptium.net/
