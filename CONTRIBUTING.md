# Contributing

Thanks for wanting to help. This is a small project, so the rules are short.

## Reporting a bug

[Open an issue](../../issues) and include:

1. What you expected and what happened instead.
2. Your versions: Minecraft, Fabric Loader, Fabric API and the mod.
3. The relevant part of `logs/latest.log` if the game crashed or the mod did not load.
4. If the panel does not react to a buy or sale: the exact chat lines Hypixel showed you.

## Suggesting something

Open an issue describing what you would like and why. The mod focuses on coins and the Auction House, so ideas in that area fit best.

## Sending a pull request

- Keep it small and focused: one change per pull request.
- Chat message patterns live at the top of `fliptracker-mod/src/main/java/com/fliptracker/Tracker.java`. If Hypixel changes a message, that is usually the only thing to fix.
- New settings go into `Tracker.Settings` and into the list in `ConfigScreen.java`.
- Do not add anything that automates gameplay (auto buying, auto clicking and similar).
- Say in the description whether you tested the change in the game.
- If AI wrote a substantial part of your contribution, please say so.

## Building

```
cd fliptracker-mod
gradle build
```

Requires Java 25.
