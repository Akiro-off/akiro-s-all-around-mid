package com.fliptracker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FlipTrackerClient implements ClientModInitializer {
	// Экран нельзя открывать прямо из команды: чат закроется и закроет его. Открываем на следующем тике.
	private static boolean pendingOpen = false;

	private static final Map<String, Integer> BG = Map.of(
		"green", 0x1B2A1F, "dark", 0x15171A, "blue", 0x14233A, "purple", 0x2A1B3A, "red", 0x3A1B1B, "black", 0x000000);
	private static final Map<String, Integer> FG = Map.of(
		"gold", 0xF0D060, "white", 0xFFFFFF, "lime", 0x6FE08A, "cyan", 0x6FC8FF, "pink", 0xFF8FC8, "red", 0xFF6B6B);

	@Override
	public void onInitializeClient() {
		Tracker.load();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (pendingOpen) {
				pendingOpen = false;
				client.gui.setScreen(new ConfigScreen());
			}
		});
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
		say(c,
			"/flip (меню) | lang ru|en | bg <цвет|HEX> | fg <цвет|HEX> | alpha 0-100 | pos x y | move dx dy | toggle | reset session|all|pos",
			"/flip (menu) | lang ru|en | bg <name|HEX> | fg <name|HEX> | alpha 0-100 | pos x y | move dx dy | toggle | reset session|all|pos");
		return say(c,
			"Деревья: /flip trees toggle | pos x y | add N | reset | pattern <regex>|off | record on|off",
			"Trees: /flip trees toggle | pos x y | add N | reset | pattern <regex>|off | record on|off");
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
		d.register(root("flip"));
		d.register(root("akiro"));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> root(String name) {
		Tracker.Settings s = Tracker.data.settings;
		return ClientCommands.literal(name)
			.executes(ctx -> {
				pendingOpen = true;
				return 1;
			})
			.then(ClientCommands.literal("gui").executes(ctx -> {
				pendingOpen = true;
				return 1;
			}))
			.then(ClientCommands.literal("help").executes(FlipTrackerClient::help))
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
				return say(c, s.visible ? "Панель флипов включена" : "Панель флипов скрыта", s.visible ? "Flip panel shown" : "Flip panel hidden");
			}))
			.then(ClientCommands.literal("trees")
				.then(ClientCommands.literal("toggle").executes(c -> {
					s.treeVisible = !s.treeVisible;
					Tracker.save();
					return say(c, s.treeVisible ? "Панель деревьев включена" : "Панель деревьев скрыта", s.treeVisible ? "Trees panel shown" : "Trees panel hidden");
				}))
				.then(ClientCommands.literal("pos")
					.then(ClientCommands.argument("x", IntegerArgumentType.integer(0, 4000))
						.then(ClientCommands.argument("y", IntegerArgumentType.integer(0, 4000)).executes(c -> {
							s.treeX = IntegerArgumentType.getInteger(c, "x");
							s.treeY = IntegerArgumentType.getInteger(c, "y");
							Tracker.save();
							return say(c, "Позиция деревьев: " + s.treeX + ", " + s.treeY, "Trees position: " + s.treeX + ", " + s.treeY);
						}))))
				.then(ClientCommands.literal("add")
					.then(ClientCommands.argument("n", IntegerArgumentType.integer(-100000, 100000)).executes(c -> {
						Tracker.addTree(null, IntegerArgumentType.getInteger(c, "n"));
						return say(c, "Деревьев всего: " + Tracker.data.trees, "Trees total: " + Tracker.data.trees);
					})))
				.then(ClientCommands.literal("reset").executes(c -> {
					Tracker.resetTrees();
					return say(c, "Счётчик деревьев сброшен", "Tree counter reset");
				}))
				.then(ClientCommands.literal("pattern")
					.then(ClientCommands.literal("off").executes(c -> {
						Tracker.setTreePattern("");
						return say(c, "Свой шаблон выключен, считаются сообщения «You felled the entire ... Tree!»",
							"Custom pattern off, counting «You felled the entire ... Tree!» messages");
					}))
					.then(ClientCommands.argument("rx", StringArgumentType.greedyString()).executes(c -> {
						String rx = StringArgumentType.getString(c, "rx");
						if (!Tracker.setTreePattern(rx)) return say(c, "Неверное регулярное выражение", "Invalid regular expression");
						return say(c, "Шаблон деревьев установлен: " + rx, "Tree pattern set: " + rx);
					})))
				.then(ClientCommands.literal("record")
					.then(ClientCommands.literal("on").executes(c -> {
						s.recordChat = true;
						Tracker.save();
						return say(c, "Запись чата включена: файл config/fliptracker-chat.txt", "Chat recording on: file config/fliptracker-chat.txt");
					}))
					.then(ClientCommands.literal("off").executes(c -> {
						s.recordChat = false;
						Tracker.save();
						return say(c, "Запись чата выключена", "Chat recording off");
					}))))
			.then(ClientCommands.literal("reset")
				.then(ClientCommands.literal("session").executes(c -> {
					Tracker.resetSession();
					return say(c, "Сессия сброшена", "Session reset");
				}))
				.then(ClientCommands.literal("pos").executes(c -> {
					s.x = 6;
					s.y = 6;
					s.treeX = 6;
					s.treeY = 80;
					Tracker.save();
					return say(c, "Позиции сброшены", "Positions reset");
				}))
				.then(ClientCommands.literal("all").executes(c -> {
					Tracker.data.flips.clear();
					Tracker.data.holding.clear();
					Tracker.data.unmatched = 0;
					Tracker.resetSession();
					Tracker.save();
					return say(c, "Данные флипов удалены", "Flip data deleted");
				})));
	}
}
