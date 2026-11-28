<p align="center">
  <img src="banner.png" alt="Akiro's Allround Mod" width="720">
</p>

<h1 align="center">Akiro’s Allround Mod</h1>

<hr>

<p align="center">
  <b>Every flip deserves a receipt.</b><br>
  A client-side Fabric mod for Hypixel SkyBlock · Minecraft 26.2
</p>

<hr>

You bought low. You sold high. Hypixel told you about it in chat, once, somewhere between a party invite and a stranger selling a Hyperion.

**Akiro’s Allround Mod remembers.**

Every time you buy an item from the Auction House, the mod writes down what you paid. When you collect the coins for it, it matches the sale to the purchase and does the maths. The result sits in a small panel on your screen: how much you have made in total, how much this session, how much per hour, and what you flipped last.

It is a profit tracker. It does not flip for you, click for you or touch the Auction House in any way. It reads the chat and keeps count.

## What it does

- **Tracks every auction flip automatically.** Buy, sell, collect. Nothing to type.
- **Shows a live panel in the game.** Total profit, session profit, coins per hour, last flip, and items still waiting to sell.
- **Looks like Minecraft.** Coloured text with a shadow, like the rest of the HUD. Turn the background off if you want only text.
- **Has a settings menu.** Type `/flip`. Language (English or Russian), colours, opacity, text shadow, what the panel shows, and where it sits on the screen.
- **Keeps your history.** Your numbers are saved to a file and are there next time you start the game.

## How it works

| Step | What Hypixel says | What the mod does |
|---|---|---|
| 1. Buy | `You purchased <item> for <N> coins!` | Remembers the item and the price |
| 2. Collect | `You collected <N> coins from selling <item> to <player> in an auction!` | Finds the purchase by item name and records the flip |
| 3. Profit | | `coins received − buy price`. The auction tax is already taken off by Hypixel, so it is already in the number |

For example, you buy for **1,500,000** and collect **2,100,000**: the panel shows **+600.00k**.

Profit is counted when you **collect** the coins in *Manage Auctions*, not when the item sells. If a sale has no matching purchase (you bought it before installing the mod), it goes into the *unmatched* counter and does not change your profit.

## Install

You need:

- Minecraft **26.2**
- [Fabric Loader](https://fabricmc.net/use/) 0.19.3 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2
- Java 25 (the launcher normally sets this up for you)

Then:

1. Download the latest `.jar` from the [Releases](../../releases) page.
2. Put it in your `mods` folder.
3. Start the game, join Hypixel SkyBlock and type `/flip`.

## Using it

`/flip` (or `/akiro`) opens the settings menu. Everything is in there:

| Category | What you can change |
|---|---|
| **Flip Tracker** | Show or hide the panel and each of its lines, reset the session, delete all flip data |
| **GUI** | Background on or off, Minecraft-style colours or a single colour, language, colours, opacity, text shadow |
| **Position** | Move the panel on the screen, reset the position |
| **About** | Version and file locations |

Your data and settings are stored in `config/fliptracker.json` in your game folder.

## Limits

Honest list, so nothing surprises you:

- **Auction House only.** The Bazaar is not tracked.
- **It depends on Hypixel’s chat text.** If Hypixel changes the wording of those two messages, the mod stops recognising them until it is updated. The patterns are at the top of [`Tracker.java`](fliptracker-mod/src/main/java/com/fliptracker/Tracker.java).
- **Profit appears when you collect the coins**, not when the item sells.
- **Items with identical names** are matched in the order you bought them.
- **It was written for a very new Minecraft version** and has had limited testing. If something breaks, please [open an issue](../../issues).

## Is it safe to use on Hypixel?

The mod only reads chat messages and draws a panel. It does not automate anything, send any packets of its own or change what the game does. It is **not made or approved by Hypixel**, so use it at your own risk and follow Hypixel’s rules.

## Build from source

You need Java 25 and Gradle.

```
cd fliptracker-mod
gradle build
```

The jar appears in `fliptracker-mod/build/libs/`. Every push to this repository also builds the jar with GitHub Actions: open the **Actions** tab, pick the latest run and download the `fliptracker-jar` artifact.

## About this project

This mod was written with the help of AI ([Claude](https://claude.ai)). The code is open, so you are welcome to read it, question it and improve it. It is not affiliated with Hypixel.

## Contributing and license

Bug reports, ideas and pull requests are welcome, see [CONTRIBUTING](CONTRIBUTING.md). The project is released under the [MIT License](LICENSE).

<details>
<summary><b>Русский</b></summary>

### Akiro’s Allround Mod: у каждого флипа есть чек

Клиентский мод Fabric для Hypixel SkyBlock (Minecraft 26.2). Он сам считает прибыль с аукционных флипов и показывает её панелью прямо в игре.

- Покупаешь предмет на аукционе, и мод запоминает цену.
- Забираешь монеты за проданный предмет в *Manage Auctions*, и мод находит покупку по названию и считает прибыль. Налог аукциона уже вычтен самим Hypixel.
- На панели видно общую прибыль, прибыль за сессию, монеты в час, последний флип и предметы, которые ещё ждут продажи.
- Команда `/flip` (или `/akiro`) открывает меню настроек: язык, цвета, фон, прозрачность, тень текста, что показывать и где стоит панель.

**Установка:** Minecraft 26.2, Fabric Loader 0.19.3+, Fabric API, Java 25. Скачай `.jar` на странице [Releases](../../releases), положи в папку `mods`, зайди на Hypixel SkyBlock и напиши `/flip`.

**Ограничения:** учитывается только аукцион (не базар), прибыль засчитывается в момент получения монет, а если Hypixel изменит текст сообщений, мод придётся обновить. Мод не автоматизирует ничего, но он не одобрен Hypixel, поэтому пользуйся на свой риск.

Мод создан с помощью ИИ (Claude). Код открыт.

</details>
