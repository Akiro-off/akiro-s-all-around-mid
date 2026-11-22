package com.fliptracker;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

/** Данные, настройки, разбор сообщений чата. */
public final class Tracker {
	public static final class Flip { public String item; public long buy, got, profit, ts; }
	public static final class Hold { public String item; public long price, ts; }
	public static final class Settings {
		public String lang = "ru";
		public int bg = 0x1B2A1F;
		public int bgAlpha = 85;
		public int fg = 0xF0D060;
		public int x = 6, y = 6;
		public boolean visible = true;
	}
	public static final class Data {
		public List<Flip> flips = new ArrayList<>();
		public List<Hold> holding = new ArrayList<>();
		public int unmatched = 0;
		public Settings settings = new Settings();
	}

	public static Data data = new Data();
	public static long sessionStart = System.currentTimeMillis();

	private static final Gson GSON = new Gson();
	// Если Hypixel поменяет текст сообщений, правь только эти два шаблона.
	static final Pattern BUY = Pattern.compile("You purchased (.+?) for ([\\d,]+) coins!");
	static final Pattern SOLD = Pattern.compile("You collected ([\\d,]+) coins from selling (.+?) to .+? in an auction!");

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
					if (data.settings == null) data.settings = new Settings();
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

	/** Принимает текст сообщения чата без цветовых кодов. */
	public static void onChat(String text) {
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
