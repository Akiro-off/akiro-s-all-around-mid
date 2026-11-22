# Flip Tracker (Fabric, Minecraft 26.2)

Мод для Hypixel SkyBlock: сам считает прибыль с флипов на аукционе и показывает панель на экране.

## Как собрать .jar (без установки Java)
1. Зарегистрируйся на github.com и создай новый репозиторий (New repository, любое имя, Public).
2. Загрузи в него все файлы этой папки: Add file > Upload files, перетащи содержимое архива.
3. Проверь, что в репозитории есть файл `.github/workflows/build.yml`.
   Если его нет (папки с точкой иногда не загружаются): Add file > Create new file,
   в имени файла напиши `.github/workflows/build.yml` и вставь содержимое из этого файла.
4. Открой вкладку Actions. Сборка запускается сама (или нажми Run workflow). Подожди 2-5 минут.
5. Открой завершённую сборку, внизу в Artifacts скачай `fliptracker-jar`, внутри будет `fliptracker-1.0.0.jar`.
6. Положи `.jar` в папку `mods` твоей сборки и запусти игру.
Если сборка упала с ошибкой, скопируй текст ошибки из Actions и пришли мне.

## Команды в игре (чат)
- `/flip lang ru` или `/flip lang en` : язык
- `/flip bg green|dark|blue|purple|red|black` или `/flip bg 1B2A1F` : цвет фона
- `/flip fg gold|white|lime|cyan|pink|red` или `/flip fg F0D060` : цвет текста
- `/flip alpha 0-100` : прозрачность фона
- `/flip pos 10 10` : поставить панель в точку экрана (в пикселях)
- `/flip move 20 -5` : сдвинуть панель относительно текущего места
- `/flip toggle` : показать или скрыть панель
- `/flip reset session|pos|all` : сбросить сессию, позицию или все данные

Данные хранятся в `config/fliptracker.json`.

## Как считает
- «You purchased X for N coins!» запоминает покупку.
- «You collected N coins from selling X ... in an auction!» находит покупку по названию, прибыль = N минус цена покупки (налог уже вычтен).
Шаблоны находятся в начале `Tracker.java` (BUY и SOLD).
