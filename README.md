https://www.curseforge.com/minecraft/mc-mods/create-gravity but it's not MCreator

# Configuration
Biomes can be configured as low oxygen and / or low gravity biome
An example entry: 
- `minecraft:plains;75;-0.3` 
- or `#forge:is_hot;;-0.8` (uses the default oxygen factor of `100`) 
- or `terralith:alpha_islands;100;` (uses the default gravity factor of `-0.8`)

The Oxygen factor has to be above or equal to `0` (a value of `0` will disable the logic for said biome)

The Gravity factor has to be between `-1` (will reverse gravity) or `0` (disables gravity logic for said biome)

---

In addition, the amount of damage and the rate at which the players does get damaged can be configured as well

## Datapack
### Item tags
- `create_gravity:anti_low_gravity_boots`: Will disable the gravity effect of said biome (when worn in the boots equipment slot)
- `create_gravity:diving_helmets`: Will decrease the rate at which oxygen drains (when worn in the helmet slot)
- `create_gravity:backtanks`: The logic will look for an NBT entry of `Air` (when worn in the chest slot)
  - If present and the value is above `1` it will be decreased by `1`, supply oxygen and the oxygen damage ticks will reset
  - If no backtank is equipped the oxygen damage ticks will increase and once they reach the configured value (default is `60`) then the player will take damage
  - By default, the `Create` backtanks will have this tag though you can add other items and handle the addition of this tag yourself