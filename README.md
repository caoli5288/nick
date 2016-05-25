# Nick
Nick plugin for bukkit based minecraft server.

## Require
* Bukkit based minecraft server 1.8+
* ITag or TagAPI to shown name tag

## Command
* /nick set \<nick>
    * Set owner nick to \<nick>.
    * Permission `nick.set` required.
* /nick set \<nick> \<Player>
    * Set target player's nick to \<nick>.
    * Permission `nick.admin` required.
* /nick see \<nick>
    * Find who owned the nick name.
    * No permission required.
* /nick allow \<player>
    * Give target `nick.set` permission **5** minute.
    * Permission `nick.admin` required.
* /nick set-color \<color>
    * Set owner nick color to red, blue or other.
    * Permission `nick.set.color` required.
* /nick set-color \<color> \<player>
    * Set target player's nick color.
    * Permission `nick.admin` required.