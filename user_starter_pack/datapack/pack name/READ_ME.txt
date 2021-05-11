see https://minecraft.fandom.com/wiki/Data_Pack and https://minecraft.fandom.com/wiki/Resource_Pack for extra info
see https://jsonlint.com/ for a json validator. Otherwise a proper text editor should support json files.

in the assets folder, there's the client side stuff, including lang files.

in the data folder, there's most of the configuration options.
commonly used subfolders:
	loot_tables: can be used to change mob drops and chest tables.
	tags: used to add entities to the two blacklists.
		the entity name can be found in the source code of the mod (or mc wiki for vanilla), or in its NBT data.
	sh_mechanics: old config file, it's where most of the SH specific options are.
		a lot of the options are self-explanatory, and errors prevent loading, instead of resetting to default.
		the different options will be available on the SH curse forge page, if not ask on discord.


Deleting the files that are not used for a particular pack is recommended.

When playing in singleplayer, the datapack menu can be used to insert a pack into the world
For setting up a server, the datapack needs to be in a folder called "datapacks" inside the world folder
    by default the world folder is named "world", this can be changed in the server.properties file
    The other option is to use a global datapack mod, which provide a folder akin to the defaultconfigs folder but for datapacks