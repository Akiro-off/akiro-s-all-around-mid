package com.fliptracker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Akiro's Allround Mod: меню настроек в стиле SkyHanni: слева категории, справа настройки.
 *
 * ЧТОБЫ ДОБАВИТЬ НОВУЮ ФУНКЦИЮ:
 *  1. Добавь поле в Tracker.Settings.
 *  2. Добавь строку в нужную категорию в cats(): toggle(...), cycle(...), step(...) или action(...).
 *  3. Для новой категории добавь new Cat(...) в cats().
 */
public class ConfigScreen extends Screen {
	private static int selected = 0;
	private static boolean confirmAll = false;

	private int px, py, pw, ph, sideW, ctrlX, ctrlW, rowsX;
	private final List<Cat> cats = cats();

	public ConfigScreen() {
		super(Component.literal("Akiro's Allround Mod"));
	}

	// ---------- описание настроек ----------

	private interface Builder { void build(Ctx c); }
	private record Ctx(Consumer<AbstractWidget> add, int x, int y, int w, Runnable rebuild) {}
	private record Opt(String ru, String en, Builder builder) {}
	private record Cat(String ru, String en, List<Opt> opts) {}

	private static Tracker.Settings s() {
		return Tracker.data.settings;
	}

	private static String tr(String ru, String en) {
		return "en".equals(s().lang) ? en : ru;
	}

	private static final String[] BG_RU = {"Зелёный", "Тёмный", "Синий", "Фиолетовый", "Красный", "Чёрный"};
	private static final String[] BG_EN = {"Green", "Dark", "Blue", "Purple", "Red", "Black"};
	private static final int[] BG_VAL = {0x1B2A1F, 0x15171A, 0x14233A, 0x2A1B3A, 0x3A1B1B, 0x000000};
	private static final String[] FG_RU = {"Золотой", "Белый", "Салатовый", "Голубой", "Розовый", "Красный"};
	private static final String[] FG_EN = {"Gold", "White", "Lime", "Cyan", "Pink", "Red"};
	private static final int[] FG_VAL = {0xF0D060, 0xFFFFFF, 0x6FE08A, 0x6FC8FF, 0xFF8FC8, 0xFF6B6B};

	private List<Cat> cats() {
		List<Cat> l = new ArrayList<>();

		// --- Флип-трекер ---
		List<Opt> flip = new ArrayList<>();
		flip.add(toggle("Показывать панель", "Show panel", () -> s().visible, v -> s().visible = v));
		flip.add(toggle("Показывать сессию", "Show session", () -> s().showSession, v -> s().showSession = v));
		flip.add(toggle("Показывать последний флип", "Show last flip", () -> s().showLast, v -> s().showLast = v));
		flip.add(toggle("Показывать ожидающие продажи", "Show waiting items", () -> s().showHolding, v -> s().showHolding = v));
		flip.add(action("Сброс сессии", "Reset session", () -> tr("Сбросить", "Reset"), Tracker::resetSession, false));
		flip.add(new Opt("Удалить данные флипов", "Delete flip data", c -> {
			Component label = confirmAll
				? Component.literal(tr("Нажми ещё раз", "Click again")).withStyle(ChatFormatting.RED)
				: Component.literal(tr("Удалить всё", "Delete all"));
			c.add().accept(Button.builder(label, b -> {
				if (!confirmAll) {
					confirmAll = true;
				} else {
					Tracker.data.flips.clear();
					Tracker.data.holding.clear();
					Tracker.data.unmatched = 0;
					Tracker.resetSession();
					Tracker.save();
					confirmAll = false;
				}
				c.rebuild().run();
			}).bounds(c.x(), c.y(), c.w(), 20).build());
		}));
		l.add(new Cat("Флип-трекер", "Flip Tracker", flip));

		// --- Деревья ---
		List<Opt> trees = new ArrayList<>();
		trees.add(toggle("Показывать панель деревьев", "Show trees panel", () -> s().treeVisible, v -> s().treeVisible = v));
		trees.add(toggle("Разбивка по породам", "Show by tree type", () -> s().treeByType, v -> s().treeByType = v));
		trees.add(toggle("Деревьев в час", "Trees per hour", () -> s().treePerHour, v -> s().treePerHour = v));
		trees.add(action("Добавить дерево вручную", "Add a tree manually", () -> "+1", () -> Tracker.addTree(null, 1), false));
		trees.add(action("Сбросить деревья", "Reset trees", () -> tr("Сбросить", "Reset"), Tracker::resetTrees, false));
		trees.add(toggle("Запись чата (отладка)", "Record chat (debug)", () -> s().recordChat, v -> s().recordChat = v));
		l.add(new Cat("Деревья", "Trees", trees));

		// --- Интерфейс ---
		List<Opt> gui = new ArrayList<>();
		gui.add(toggle("Фон", "Background", () -> s().showBg, v -> s().showBg = v));
		gui.add(cycle("Стиль текста", "Text style", new String[]{"Как в Minecraft", "Один цвет"}, new String[]{"Minecraft colors", "Single color"},
			new int[]{1, 0}, () -> s().mcStyle ? 1 : 0, v -> s().mcStyle = v == 1, false));
		gui.add(cycle("Язык", "Language", new String[]{"Русский", "English"}, new String[]{"Русский", "English"},
			new int[]{0, 1}, () -> "en".equals(s().lang) ? 1 : 0, v -> s().lang = v == 1 ? "en" : "ru", true));
		gui.add(cycle("Цвет фона", "Background color", BG_RU, BG_EN, BG_VAL, () -> s().bg, v -> s().bg = v, false));
		gui.add(cycle("Цвет текста (один цвет)", "Text color (single)", FG_RU, FG_EN, FG_VAL, () -> s().fg, v -> s().fg = v, false));
		gui.add(step("Прозрачность фона", "Background opacity", 0, 100, 10, "%", () -> s().bgAlpha, v -> s().bgAlpha = v));
		gui.add(toggle("Тень у текста", "Text shadow", () -> s().shadow, v -> s().shadow = v));
		l.add(new Cat("Интерфейс", "GUI", gui));

		// --- Позиция ---
		List<Opt> pos = new ArrayList<>();
		pos.add(step("Флипы: X", "Flips: X", 0, 2000, 10, "", () -> s().x, v -> s().x = v));
		pos.add(step("Флипы: Y", "Flips: Y", 0, 2000, 10, "", () -> s().y, v -> s().y = v));
		pos.add(step("Деревья: X", "Trees: X", 0, 2000, 10, "", () -> s().treeX, v -> s().treeX = v));
		pos.add(step("Деревья: Y", "Trees: Y", 0, 2000, 10, "", () -> s().treeY, v -> s().treeY = v));
		pos.add(action("Сбросить позиции", "Reset positions", () -> tr("Сбросить", "Reset"), () -> {
			s().x = 6;
			s().y = 6;
			s().treeX = 6;
			s().treeY = 80;
			Tracker.save();
		}, true));
		l.add(new Cat("Позиция", "Position", pos));

		// --- О моде (без настроек, текст рисуется в render) ---
		l.add(new Cat("О моде", "About", new ArrayList<>()));
		return l;
	}

	// ---------- фабрики элементов ----------

	private static Component onOff(boolean v) {
		return v ? Component.literal(tr("Вкл", "On")).withStyle(ChatFormatting.GREEN)
			: Component.literal(tr("Выкл", "Off")).withStyle(ChatFormatting.RED);
	}

	private static Opt toggle(String ru, String en, BooleanSupplier get, Consumer<Boolean> set) {
		return new Opt(ru, en, c -> c.add().accept(
			Button.builder(onOff(get.getAsBoolean()), b -> {
				set.accept(!get.getAsBoolean());
				Tracker.save();
				b.setMessage(onOff(get.getAsBoolean()));
			}).bounds(c.x(), c.y(), c.w(), 20).build()));
	}

	private static int indexOf(int[] vals, int v) {
		for (int i = 0; i < vals.length; i++) if (vals[i] == v) return i;
		return -1;
	}

	private static Component cycleLabel(String[] ru, String[] en, int[] vals, int cur) {
		int i = indexOf(vals, cur);
		if (i >= 0) return Component.literal(tr(ru[i], en[i]));
		return Component.literal(tr("Свой", "Custom") + " #" + String.format("%06X", cur & 0xFFFFFF));
	}

	private static Opt cycle(String ru, String en, String[] nRu, String[] nEn, int[] vals,
	                         IntSupplier get, IntConsumer set, boolean rebuild) {
		return new Opt(ru, en, c -> c.add().accept(
			Button.builder(cycleLabel(nRu, nEn, vals, get.getAsInt()), b -> {
				int i = (indexOf(vals, get.getAsInt()) + 1) % vals.length;
				set.accept(vals[i]);
				Tracker.save();
				if (rebuild) c.rebuild().run();
				else b.setMessage(cycleLabel(nRu, nEn, vals, get.getAsInt()));
			}).bounds(c.x(), c.y(), c.w(), 20).build()));
	}

	private static Opt step(String ru, String en, int min, int max, int step, String suffix,
	                        IntSupplier get, IntConsumer set) {
		return new Opt(ru, en, c -> {
			Button val = Button.builder(Component.literal(get.getAsInt() + suffix), b -> {})
				.bounds(c.x() + 22, c.y(), c.w() - 44, 20).build();
			val.active = false;
			Button minus = Button.builder(Component.literal("-"), b -> {
				set.accept(Math.max(min, get.getAsInt() - step));
				Tracker.save();
				val.setMessage(Component.literal(get.getAsInt() + suffix));
			}).bounds(c.x(), c.y(), 20, 20).build();
			Button plus = Button.builder(Component.literal("+"), b -> {
				set.accept(Math.min(max, get.getAsInt() + step));
				Tracker.save();
				val.setMessage(Component.literal(get.getAsInt() + suffix));
			}).bounds(c.x() + c.w() - 20, c.y(), 20, 20).build();
			c.add().accept(minus);
			c.add().accept(val);
			c.add().accept(plus);
		});
	}

	private static Opt action(String ru, String en, java.util.function.Supplier<String> label, Runnable run, boolean rebuild) {
		return new Opt(ru, en, c -> c.add().accept(
			Button.builder(Component.literal(label.get()), b -> {
				run.run();
				if (rebuild) c.rebuild().run();
			}).bounds(c.x(), c.y(), c.w(), 20).build()));
	}

	// ---------- экран ----------

	@Override
	protected void init() {
		pw = Math.min(this.width - 16, 420);
		ph = Math.min(this.height - 16, 250);
		px = (this.width - pw) / 2;
		py = (this.height - ph) / 2;
		sideW = 100;
		rowsX = px + sideW + 12;
		ctrlW = 120;
		ctrlX = px + pw - ctrlW - 10;

		if (selected >= cats.size()) selected = 0;
		for (int i = 0; i < cats.size(); i++) {
			final int idx = i;
			Cat cat = cats.get(i);
			Button b = Button.builder(Component.literal(tr(cat.ru(), cat.en())), btn -> {
				selected = idx;
				confirmAll = false;
				this.rebuildWidgets();
			}).bounds(px + 6, py + 34 + i * 22, sideW - 12, 20).build();
			b.active = (i != selected);
			this.addRenderableWidget(b);
		}

		List<Opt> opts = cats.get(selected).opts();
		for (int i = 0; i < opts.size(); i++) {
			Ctx c = new Ctx(w -> this.addRenderableWidget(w), ctrlX, py + 34 + i * 24, ctrlW, this::rebuildWidgets);
			opts.get(i).builder().build(c);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);

		int line = 0xFF4A6B54;
		g.fill(px, py, px + pw, py + 1, line);
		g.fill(px, py + ph - 1, px + pw, py + ph, line);
		g.fill(px, py, px + 1, py + ph, line);
		g.fill(px + pw - 1, py, px + pw, py + ph, line);
		g.fill(px + sideW, py + 28, px + sideW + 1, py + ph - 6, line);
		g.fill(px + 6, py + 26, px + pw - 6, py + 27, line);

		g.text(this.font, "Akiro's Allround Mod", px + 8, py + 10, 0xFFF0D060, true);
		String sub = tr("Настройки", "Settings");
		g.text(this.font, sub, px + pw - 8 - this.font.width(sub), py + 10, 0xFF93A899, true);

		List<Opt> opts = cats.get(selected).opts();
		for (int i = 0; i < opts.size(); i++) {
			Opt o = opts.get(i);
			g.text(this.font, tr(o.ru(), o.en()), rowsX, py + 34 + i * 24 + 6, 0xFFE6EFE8, true);
		}

		if (selected == cats.size() - 1) {
			String[] about = {
				"Akiro's Allround Mod",
				tr("Флип-трекер: прибыль с аукционных флипов.", "Flip Tracker: auction flip profit."),
				tr("Деревья: счётчик срубленных деревьев.", "Trees: felled tree counter."),
				tr("Данные: config/fliptracker.json", "Data: config/fliptracker.json"),
				tr("Команды: /flip help (или /akiro help)", "Commands: /flip help (or /akiro help)"),
			};
			for (int i = 0; i < about.length; i++) {
				g.text(this.font, about[i], rowsX, py + 40 + i * 14, 0xFFE6EFE8, true);
			}
		}
	}
}
