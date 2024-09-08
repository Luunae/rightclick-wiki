package com.rightclickwiki;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import static net.runelite.api.MenuAction.*;

@Slf4j
@PluginDescriptor(
	name = "Right Click Wiki"
)
public class RightClickWikiPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RightClickWikiConfig config;

	@Override
	protected void startUp()
	{
		log.info("Right-Click Wiki Search started!");
	}

	@Override
	protected void shutDown()
	{
		log.info("Right-Click Wiki Search stopped!");
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.isMenuOpen())
		{
			return;
		}

		String target = event.getTarget();
		if (target.isEmpty())
		{
			return;
		}

		// String cleanTarget = Text.removeTags(target).trim();  // This line is vestigial.
		boolean shouldAdd = false;

		MenuAction type = MenuAction.of(event.getType());

		if (type == MenuAction.EXAMINE_NPC && config.enableNpcs())
		{
			shouldAdd = true;
		}
		else if (type == MenuAction.EXAMINE_OBJECT && config.enableObjects())
		{
			shouldAdd = true;
		}
		else if ((type == MenuAction.EXAMINE_ITEM_GROUND
				  || type == MenuAction.ITEM_USE_ON_GAME_OBJECT
				  || type == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
				 && config.enableItems())
		{
			shouldAdd = true;
		}

		if (shouldAdd)
		{
			client.createMenuEntry(-1)
				.setOption(config.menuOption())
				.setTarget(target)
				.setType(MenuAction.RUNELITE)
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setIdentifier(event.getIdentifier());
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() == MenuAction.RUNELITE && event.getMenuOption().equals(config.menuOption()))
		{
			event.consume();
			MenuEntry entry = event.getMenuEntry();
			String entityName = Text.removeTags(entry.getTarget()).trim();
			String type = getEntityType(entry);
			int id = getEntityId(entry);

			String wikiUrl = String.format("https://oldschool.runescape.wiki/w/Special:Lookup?type=%s&id=%d&name=%s&utm_source=runelite",
				type, id, URLEncoder.encode(entityName, StandardCharsets.UTF_8));
			
			try
			{
				Desktop.getDesktop().browse(new URI(wikiUrl));
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Opening wiki page for " + entityName + ".", null);
			}
			catch (Exception e)
			{
				log.warn("Error opening wiki page", e);
			}
		}
	}

	private String getEntityType(MenuEntry entry)
	{
		switch (entry.getType())
		{
			case EXAMINE_NPC:
				return "npc";
			case EXAMINE_OBJECT:
				return "object";
			case EXAMINE_ITEM_GROUND:
			case ITEM_USE_ON_GAME_OBJECT:
			case WIDGET_TARGET_ON_GAME_OBJECT:
				return "item";
			default:
				return "item"; // Default to item if type is unknown
		}
	}

	private int getEntityId(MenuEntry entry)
	{
		switch (entry.getType())
		{
			case EXAMINE_NPC:
				NPC npc = client.getCachedNPCs()[entry.getIdentifier()];
				return npc != null ? npc.getId() : -1;
			case EXAMINE_OBJECT:
				WorldPoint worldPoint = WorldPoint.fromScene(client, entry.getParam0(), entry.getParam1(), client.getPlane());
				LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
				if (localPoint == null) return -1;
				Tile tile = client.getScene().getTiles()[worldPoint.getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];
				TileObject object = tile.getGameObjects()[0];
				return object != null ? object.getId() : -1;
			case EXAMINE_ITEM:
			case EXAMINE_ITEM_GROUND:
			case CC_OP:
			case ITEM_USE:
				return entry.getIdentifier();
			default:
				return -1; // Return -1 if we can't determine the ID
		}
	}

	@Provides
	RightClickWikiConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RightClickWikiConfig.class);
	}
}
