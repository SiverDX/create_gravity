![Create : Gravity logo](https://i.ibb.co/VYhDchm/image-2024-02-10-145637766.png)

# **Create : Gravity is a simple Create addon, turning the End Dimension into something more Moon-themed with 2 simple tweaks**

<div align="center">
  <a href="https://www.youtube.com/watch?v=i5oLgZKQnck"><img src="https://img.youtube.com/vi/i5oLgZKQnck/0.jpg" alt="Showcase"></a>
</div>

# Functionality
This mod allows you to make certain biomes (and therefor dimensions):
1. Not have oxygen (if you run out of air, you will take damage)
2. Have low gravity

By default these two functionalities will be applied to biomes of the end dimension

# Configuration
Biomes can be configured as low oxygen and / or low gravity biome, for example:
- `minecraft:plains;75;-0.3`
- or `#forge:is_hot;;-0.8` (uses the default oxygen factor of `100`)
- or `terralith:alpha_islands;100;` (uses the default gravity factor of `-0.8`)

The Oxygen factor has to be above or equal to `0` (a value of `0` will disable the logic for said biome)
- Higher values will cause the oxygen to deplete at a lower rate

The Gravity factor has to be between `-1` (will disable gravity entirely) or `0` (disables gravity logic for said biome)

In addition, the following things can be configured as well:
- Amount of damage and the rate at which the player does get damaged
- Whether wearing a diving helmet is required to make the backtank functional (i.e. use the air supply)
- The rate at which the player loses oxygen (this will always be slower if a player is wearing a diving helmet)
- The rate at which the backtank air supply gets depleted

## Datapack
### Item tags
- `create_gravity:anti_low_gravity_boots`: Will disable the gravity effect of said biome (when worn in the boots equipment slot)
  - Contains `create:copper_diving_boots` and `create:netherite_diving_boots` by default
- `create_gravity:diving_helmets`: Will decrease the rate at which oxygen drains (when worn in the helmet slot)
  - Contains `create:copper_diving_helmet` and `create:netherite_diving_helmet` by default
- `create_gravity:backtanks`: The logic will look for an NBT entry of `Air` (when worn in the chest slot)
  - Contains `create:copper_backtank` and `create:netherite_backtank` by default
  - If present and the value is above `1` it will be decreased by `1`, supply oxygen and the oxygen damage ticks will reset
  - If no backtank is equipped the oxygen damage ticks will increase and once they reach the configured value (default is `60`) then the player will take damage
  - By default, the `Create` backtanks will have this tag - though you can add other items and handle the addition of this tag yourself

### Entity Type tags
- `create_gravity:low_gravity_blacklist`: Will cause the entity to not be affected by the gravity change (contains `#forge:bosses` by default)

# Compatibility
The mod should be compatible with some other space suits mods (Futurepack and Space Dimensions suits seems to be working)

No explicit compatibility has been made so far (might be the case in the future)

# Social
[![Join the Unusual Squad discord ](https://media.forgecdn.net/attachments/description/813116/description_8e31fce8-e7ef-4280-8793-4ee4a74152af.png)](https://discord.gg/k3fGJwJqzu "Click to join our modding discord server")