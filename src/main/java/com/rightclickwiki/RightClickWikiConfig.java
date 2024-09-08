package com.rightclickwiki;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("rightclickwiki")
public interface RightClickWikiConfig extends Config
{
	@ConfigItem(
		keyName = "menuOption",
		name = "Menu Option",
		description = "The text to show in the right-click menu"
	)
	default String menuOption()
	{
		return "Wiki";
	}

	@ConfigItem(
		keyName = "enableNpcs",
		name = "Enable for NPCs",
		description = "Add wiki lookup for NPCs"
	)
	default boolean enableNpcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableItems",
		name = "Enable for Items",
		description = "Add wiki lookup for Items"
	)
	default boolean enableItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableObjects",
		name = "Enable for Objects",
		description = "Add wiki lookup for Objects"
	)
	default boolean enableObjects()
	{
		return true;
	}
}
