package com.rightclickwiki;

import com.rightclickwiki.RightClickWikiPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RightClickWikiPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RightClickWikiPlugin.class);
		RuneLite.main(args);
	}
}