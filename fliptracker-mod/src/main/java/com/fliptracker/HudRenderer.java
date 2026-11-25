package com.fliptracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudRenderer {
	private static final String SEC = "\u00a7"; // знак § для цветовых кодов Minecraft

	private static final Map<String, String> RU = Map.of(
		"total", "ВСЕГО", "flips", "флипов", "session", "Сессия", "hour", "ч",
		"last", "Последний", "holding", "Ждут продажи", "unm", "без пары",
		"trees", "Срублено деревьев", "tcount", "Всего");
	private static final Map<String, String> EN = Map.of(
		"total", "TOTAL", "flips", "flips", "session", "Session", "hour", "h",
		"last", "Last", "holding", "Waiting to sell", "unm", "unmatched",
		"trees", "Trees Felled", "tcount", "Total");

	public static String t(String key) {
		return ("en".equals(Tracker.data.settings.lang) ? EN : RU).get(key);
	}

	private static String c(String code) {
		return SEC + code;
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Tracker.Settings s = Tracker.data.settings;
		Font font = Minecraft.getInstance().font;
		if (s.visible) box(g, font, s, flipLines(s), s.x, s.y);
		if (s.treeVisible) box(g, font, s, treeLines(s), s.treeX, s.treeY);
	}

	private static double hours() {
		return Math.max((System.currentTimeMillis() - Tracker.sessionStart) / 3.6e6, 1.0 / 60);
	}

	private static List<String> flipLines(Tracker.Settings s) {
		List<String> l = new ArrayList<>();
		long total = Tracker.total();
		long sp = Tracker.sessionProfit();
		int n = Tracker.data.flips.size();

		l.add(c("a") + c("l") + "Flip Tracker");
		l.add(c("7") + t("total") + ": " + c("6") + Tracker.shortNum(total) + c("7") + "  (" + n + " " + t("flips") + ")");
		if (s.showSession) {
			l.add(c("7") + t("session") + ": " + c("6") + Tracker.shortNum(sp) + c("7") + "  |  "
				+ c("6") + Tracker.shortNum((long) (sp / hours())) + c("7") + "/" + t("hour"));
		}
		if (s.showLast && n > 0) {
			Tracker.Flip last = Tracker.data.flips.get(n - 1);
			String name = last.item.length() > 22 ? last.item.substring(0, 22) : last.item;
			l.add(c("7") + t("last") + ": " + c("f") + name + " "
				+ (last.profit >= 0 ? c("a") + "+" : c("c")) + Tracker.shortNum(last.profit));
		}
		if (s.showHolding) {
			String hold = c("7") + t("holding") + ": " + c("f") + Tracker.data.holding.size();
			if (Tracker.data.unmatched > 0) hold += c("7") + "  (" + t("unm") + ": " + Tracker.data.unmatched + ")";
			l.add(hold);
		}
		return l;
	}

	private static List<String> treeLines(Tracker.Settings s) {
		List<String> l = new ArrayList<>();
		l.add(c("a") + c("l") + t("trees"));
		l.add(c("7") + t("tcount") + ": " + c("6") + Tracker.data.trees);
		String sess = c("7") + t("session") + ": " + c("6") + Tracker.sessionTrees;
		if (s.treePerHour) {
			sess += c("7") + "  |  " + c("6") + Math.round(Tracker.sessionTrees / hours()) + c("7") + "/" + t("hour");
		}
		l.add(sess);
		if (s.treeByType && !Tracker.data.treeTypes.isEmpty()) {
			List<Map.Entry<String, Integer>> es = new ArrayList<>(Tracker.data.treeTypes.entrySet());
			es.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
			for (int i = 0; i < Math.min(5, es.size()); i++) {
				l.add(c("7") + "  " + es.get(i).getKey() + ": " + c("f") + es.get(i).getValue());
			}
		}
		return l;
	}

	/** Рисует блок текста. Первая строка это заголовок: в режиме «один цвет» она пропускается. */
	private static void box(GuiGraphicsExtractor g, Font font, Tracker.Settings s, List<String> lines, int x, int y) {
		List<String> out = new ArrayList<>();
		if (s.mcStyle) {
			out.addAll(lines);
		} else {
			for (int i = 1; i < lines.size(); i++) out.add(lines.get(i).replaceAll(SEC + ".", ""));
		}
		int w = 0;
		for (String l : out) w = Math.max(w, font.width(l));
		int pad = s.showBg ? 4 : 0;
		int h = out.size() * 10;
		if (s.showBg) {
			int bg = ((s.bgAlpha * 255 / 100) << 24) | (s.bg & 0xFFFFFF);
			g.fill(x, y, x + w + pad * 2, y + h + pad * 2, bg);
		}
		int color = s.mcStyle ? 0xFFFFFFFF : (0xFF000000 | (s.fg & 0xFFFFFF));
		for (int i = 0; i < out.size(); i++) {
			g.text(font, out.get(i), x + pad, y + pad + i * 10, color, s.shadow);
		}
	}
}
