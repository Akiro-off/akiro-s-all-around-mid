package com.fliptracker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FlipTrackerClient implements ClientModInitializer {
	private static final Map<String, Integer> BG = Map.of(
		"green", 0x1B2A1F, "dark", 0x15171A, "blue", 0x14233A, "purple", 0x2A1B3A, "red", 0x3A1B1B, "black", 0x000000);
	private static final Map<String, Integer> FG = Map.of(
		"gold", 0xF0D060, "white", 0xFFFFFF, "lime", 0x6FE08A, "cyan", 0x6FC8FF, "pink", 0xFF8FC8, "red", 0xFF6B6B);

	@Override
	public void onInitializeClient() {
		Tracker.load();
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!overlay) Tracker.onChat(message.getString());
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("fliptracker", "hud"), HudRenderer::render);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> register(dispatcher));
	}

	private static boolean en() {
		return "en".equals(Tracker.data.settings.lang);
	}

	private static int say(CommandContext<FabricClientCommandSource> c, String ru, String en) {
		c.getSource().sendFeedback(Component.literal(en() ? en : ru));
		return 1;
	}

	private static int help(CommandContext<FabricClientCommandSource> c) {
		return say(c,
			"/flip lang ru|en | bg <цвет|HEX> | fg <цвет|HEX> | alpha 0-100 | pos x y | move dx dy | toggle | reset session|all|pos",
			"/flip lang ru|en | bg <name|HEX> | fg <name|HEX> | alpha 0-100 | pos x y | move dx dy | toggle | reset session|all|pos");
	}

	private static Integer color(String s, Map<String, Integer> names) {
		Integer v = names.get(s.toLowerCase());
		if (v != null) return v;
		try {
			return Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static void register(CommandDispatcher<FabricClientCommandSource> d) {
		Tracker.Settings s = Tracker.data.settings;
		d.register(ClientCommands.literal("flip")
			.executes(FlipTrackerClient::help)
			.then(ClientCommands.literal("lang")
				.then(ClientCommands.argument("l", StringArgumentType.word()).executes(c -> {
					String l = StringArgumentType.getString(c, "l").toLowerCase();
					if (!l.equals("ru") && !l.equals("en")) return say(c, "Используй ru или en", "Use ru or en");
					s.lang = l;
					Tracker.save();
					return say(c, "Язык: русский", "Language: English");
				})))
			.then(ClientCommands.literal("bg")
				.then(ClientCommands.argument("c", StringArgumentType.word()).executes(c -> {
					Integer v = color(StringArgumentType.getString(c, "c"), BG);
					if (v == null) return say(c, "Цвета: green dark blue purple red black или HEX (1B2A1F)", "Colors: green dark blue purple red black or HEX (1B2A1F)");
					s.bg = v;
					Tracker.save();
					return say(c, "Цвет фона изменён", "Background color changed");
				})))
			.then(ClientCommands.literal("fg")
				.then(ClientCommands.argument("c", StringArgumentType.word()).executes(c -> {
					Integer v = color(StringArgumentType.getString(c, "c"), FG);
					if (v == null) return say(c, "Цвета: gold white lime cyan pink red или HEX (F0D060)", "Colors: gold white lime cyan pink red or HEX (F0D060)");
					s.fg = v;
					Tracker.save();
					return say(c, "Цвет текста изменён", "Text color changed");
				})))
			.then(ClientCommands.literal("alpha")
				.then(ClientCommands.argument("a", IntegerArgumentType.integer(0, 100)).executes(c -> {
					s.bgAlpha = IntegerArgumentType.getInteger(c, "a");
					Tracker.save();
					return say(c, "Прозрачность фона: " + s.bgAlpha + "%", "Background opacity: " + s.bgAlpha + "%");
				})))
			.then(ClientCommands.literal("pos")
				.then(ClientCommands.argument("x", IntegerArgumentType.integer(0, 4000))
					.then(ClientCommands.argument("y", IntegerArgumentType.integer(0, 4000)).executes(c -> {
						s.x = IntegerArgumentType.getInteger(c, "x");
						s.y = IntegerArgumentType.getInteger(c, "y");
						Tracker.save();
						return say(c, "Позиция: " + s.x + ", " + s.y, "Position: " + s.x + ", " + s.y);
					}))))
			.then(ClientCommands.literal("move")
				.then(ClientCommands.argument("dx", IntegerArgumentType.integer(-4000, 4000))
					.then(ClientCommands.argument("dy", IntegerArgumentType.integer(-4000, 4000)).executes(c -> {
						s.x = Math.max(0, s.x + IntegerArgumentType.getInteger(c, "dx"));
						s.y = Math.max(0, s.y + IntegerArgumentType.getInteger(c, "dy"));
						Tracker.save();
						return say(c, "Позиция: " + s.x + ", " + s.y, "Position: " + s.x + ", " + s.y);
					}))))
			.then(ClientCommands.literal("toggle").executes(c -> {
				s.visible = !s.visible;
				Tracker.save();
				return say(c, s.visible ? "Панель включена" : "Панель скрыта", s.visible ? "HUD shown" : "HUD hidden");
			}))
			.then(ClientCommands.literal("reset")
				.then(ClientCommands.literal("session").executes(c -> {
					Tracker.sessionStart = System.currentTimeMillis();
					return say(c, "Сессия сброшена", "Session reset");
				}))
				.then(ClientCommands.literal("pos").executes(c -> {
					s.x = 6;
					s.y = 6;
					Tracker.save();
					return say(c, "Позиция сброшена", "Position reset");
				}))
				.then(ClientCommands.literal("all").executes(c -> {
					Tracker.data.flips.clear();
					Tracker.data.holding.clear();
					Tracker.data.unmatched = 0;
					Tracker.sessionStart = System.currentTimeMillis();
					Tracker.save();
					return say(c, "Все данные удалены", "All data deleted");
				}))));
	}
}
