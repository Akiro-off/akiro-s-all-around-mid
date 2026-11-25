package com.fliptracker;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

/** Данные, настройки, разбор сообщений чата (Akiro's Allround Mod). */
public final class Tracker {
	public static final class Flip { public String item; public long buy, got, profit, ts; }
	public static final class Hold { public String item; public long price, ts; }
	public static final class Settings {
		public String lang = "ru";
		// общий стиль панелей
		public boolean showBg = true;
		public boolean mcStyle = true;      // true: цветной текст как в Minecraft, false: один цвет
		public int bg = 0x1B2A1F;
		public int bgAlpha = 85;
		public int fg = 0xF0D060;
		public boolean shadow = true;
		// панель флипов
		public int x = 6, y = 6;
		public boolean visible = true;
		public boolean showSession = true, showLast = true, showHolding = true;
		// панель деревьев
		public boolean treeVisible = true, treeByType = true, treePerHour = true;
		public int treeX = 6, treeY = 80;
		public String treePattern = "";
		public boolean recordChat = false;
	}
	public static final class Data {
		public List<Flip> flips = new ArrayList<>();
		public List<Hold> holding = new ArrayList<>();
		public int unmatched = 0;
		public int trees = 0;
		public Map<String, Integer> treeTypes = new LinkedHashMap<>();
		public Settings settings = new Settings();
	}

	public static Data data = new Data();
	public static long sessionStart = System.currentTimeMillis();
	public static int sessionTrees = 0;

	private static final Gson GSON = new Gson();
	// Если Hypixel поменяет текст сообщений, правь только эти шаблоны.
	static final Pattern BUY = Pattern.compile("You purchased (.+?) for ([\\d,]+) coins!");
	static final Pattern SOLD = Pattern.compile("You collected ([\\d,]+) coins from selling (.+?) to .+? in an auction!");
	// Своё срубленное дерево целиком: "TIMBER! You felled the entire Oak Tree!" (чужие: "TIMBER! <ник> felled ...")
	static final Pattern TREE_OWN = Pattern.compile("You felled the entire (?:(.+?) )?Tree!");

	private static Pattern customCache = null;
	private static String customCacheSrc = "";
	private static int recorded = 0;

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("fliptracker.json");
	}

	public static void load() {
		try {
			Path f = file();
			if (Files.exists(f)) {
				Data d = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), Data.class);
				if (d != null) {
					data = d;
					if (data.flips == null) data.flips = new ArrayList<>();
					if (data.holding == null) data.holding = new ArrayList<>();
					if (data.treeTypes == null) data.treeTypes = new LinkedHashMap<>();
					if (data.settings == null) data.settings = new Settings();
					if (data.settings.treePattern == null) data.settings.treePattern = "";
				}
			}
		} catch (Exception ignored) {
		}
	}

	public static void save() {
		try {
			Files.writeString(file(), GSON.toJson(data), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	private static long num(String s) {
		return Long.parseLong(s.replace(",", ""));
	}

	// ---------- деревья ----------

	/** Свой шаблон для подсчёта деревьев. null, если не задан или неверный. */
	private static Pattern customPattern() {
		String src = data.settings.treePattern;
		if (src == null || src.isEmpty()) return null;
		if (!src.equals(customCacheSrc)) {
			customCacheSrc = src;
			try {
				customCache = Pattern.compile(src);
			} catch (PatternSyntaxException e) {
				customCache = null;
			}
		}
		return customCache;
	}

	/** Возвращает false, если регулярное выражение неверное. Пустая строка отключает свой шаблон. */
	public static boolean setTreePattern(String regex) {
		if (regex.isEmpty()) {
			data.settings.treePattern = "";
			save();
			return true;
		}
		try {
			Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			return false;
		}
		data.settings.treePattern = regex;
		save();
		return true;
	}

	public static void addTree(String type, int n) {
		data.trees = Math.max(0, data.trees + n);
		sessionTrees = Math.max(0, sessionTrees + n);
		if (type != null && !type.isBlank() && n > 0) {
			data.treeTypes.merge(type.trim(), n, Integer::sum);
		}
		save();
	}

	public static void resetTrees() {
		data.trees = 0;
		data.treeTypes.clear();
		sessionTrees = 0;
		save();
	}

	public static void resetSession() {
		sessionStart = System.currentTimeMillis();
		sessionTrees = 0;
	}

	private static void record(String text) {
		if (recorded >= 500) return;
		String low = text.toLowerCase();
		if (!(low.contains("tree") || low.contains("gift") || low.contains("felled"))) return;
		recorded++;
		try {
			Path f = FabricLoader.getInstance().getConfigDir().resolve("fliptracker-chat.txt");
			Files.writeString(f, text + System.lineSeparator(), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ignored) {
		}
	}

	// ---------- чат ----------

	/** Принимает текст сообщения чата без цветовых кодов. */
	public static void onChat(String text) {
		if (data.settings.recordChat) record(text);

		// Деревья: если задан свой шаблон, считаем только по нему (чтобы не считать дважды).
		Pattern cp = customPattern();
		if (cp != null) {
			Matcher cm = cp.matcher(text);
			if (cm.find()) {
				String type = null;
				try {
					if (cm.groupCount() >= 1) type = cm.group(1);
				} catch (Exception ignored) {
				}
				addTree(type, 1);
				return;
			}
		} else {
			Matcher tm = TREE_OWN.matcher(text);
			if (tm.find()) {
				addTree(tm.group(1), 1);
				return;
			}
		}

		Matcher m = BUY.matcher(text);
		if (m.find()) {
			Hold h = new Hold();
			h.item = m.group(1).trim();
			h.price = num(m.group(2));
			h.ts = System.currentTimeMillis();
			data.holding.add(h);
			save();
			return;
		}
		m = SOLD.matcher(text);
		if (m.find()) {
			long got = num(m.group(1));
			String item = m.group(2).trim();
			Hold match = null;
			for (Hold h : data.holding) {
				if (h.item.equals(item)) { match = h; break; }
			}
			if (match == null) {
				data.unmatched++;
			} else {
				data.holding.remove(match);
				Flip f = new Flip();
				f.item = item;
				f.buy = match.price;
				f.got = got;
				f.profit = got - match.price;
				f.ts = System.currentTimeMillis();
				data.flips.add(f);
			}
			save();
		}
	}

	public static long total() {
		long t = 0;
		for (Flip f : data.flips) t += f.profit;
		return t;
	}

	public static long sessionProfit() {
		long t = 0;
		for (Flip f : data.flips) if (f.ts >= sessionStart) t += f.profit;
		return t;
	}

	public static String shortNum(long n) {
		double a = Math.abs((double) n);
		if (a >= 1e9) return String.format("%.2fb", n / 1e9);
		if (a >= 1e6) return String.format("%.2fm", n / 1e6);
		if (a >= 1e3) return String.format("%.2fk", n / 1e3);
		return Long.toString(n);
	}
}
