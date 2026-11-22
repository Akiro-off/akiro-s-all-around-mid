package com.fliptracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudRenderer {
	private static final Map<String, String> RU = Map.of(
		"total", "ВСЕГО", "flips", "флипов", "session", "Сессия", "hour", "ч",
		"last", "Последний", "holding", "Ждут продажи", "unm", "без пары");
	private static final Map<String, String> EN = Map.of(
		"total", "TOTAL", "flips", "flips", "session", "Session", "hour", "h",
		"last", "Last", "holding", "Waiting to sell", "unm", "unmatched");

	public static String t(String key) {
		return ("en".equals(Tracker.data.settings.lang) ? EN : RU).get(key);
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Tracker.Settings s = Tracker.data.settings;
		if (!s.visible) return;
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		long total = Tracker.total();
		long sp = Tracker.sessionProfit();
		double hours = Math.max((System.currentTimeMillis() - Tracker.sessionStart) / 3.6e6, 1.0 / 60);
		int n = Tracker.data.flips.size();

		List<String> lines = new ArrayList<>();
		lines.add(t("total") + ": " + Tracker.shortNum(total) + "  (" + n + " " + t("flips") + ")");
		if (s.showSession) {
			lines.add(t("session") + ": " + Tracker.shortNum(sp) + "  |  " + Tracker.shortNum((long) (sp / hours)) + "/" + t("hour"));
		}
		if (s.showLast && n > 0) {
			Tracker.Flip last = Tracker.data.flips.get(n - 1);
			String name = last.item.length() > 22 ? last.item.substring(0, 22) : last.item;
			lines.add(t("last") + ": " + name + " " + (last.profit >= 0 ? "+" : "") + Tracker.shortNum(last.profit));
		}
		if (s.showHolding) {
			String hold = t("holding") + ": " + Tracker.data.holding.size();
			if (Tracker.data.unmatched > 0) hold += "  (" + t("unm") + ": " + Tracker.data.unmatched + ")";
			lines.add(hold);
		}

		int w = 0;
		for (String l : lines) w = Math.max(w, font.width(l));
		int h = lines.size() * 10 + 4;
		int bg = ((s.bgAlpha * 255 / 100) << 24) | (s.bg & 0xFFFFFF);
		int fg = 0xFF000000 | (s.fg & 0xFFFFFF);

		g.fill(s.x, s.y, s.x + w + 8, s.y + h + 4, bg);
		for (int i = 0; i < lines.size(); i++) {
			g.text(font, lines.get(i), s.x + 4, s.y + 4 + i * 10, fg, s.shadow);
		}
	}
}
