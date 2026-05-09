# CisTiers Tagger

Клиентский Fabric-мод для Minecraft 1.21.1, который интегрируется с [CisTiers](https://cistiers.net).

Мод позволяет смотреть тиры игроков прямо в игре через команду, а также автоматически показывает лучший тир игрока рядом с его ником в TAB-листе.

---

## Возможности

* Команда `/cistiers <ник>`
* Вывод всех тиров игрока с CisTiers
* Отображение места в рейтинге и количества очков
* Проверка restricted-статуса
* Автоматический тег лучшего тира в TAB-листе
* Цветное форматирование тиров
* Иконки для разных китов
* Асинхронные запросы к API
* Полностью клиентский мод

---

## Поддерживаемые киты

* 🗡 Sword
* ♦ Netherite
* 🧪 DPot
* ★ OP
* 🪓 Mace
* 🛡 SMP
* ❤ UHC
* ● Vanilla

---

## Использование команды

```mcfunction
/cistiers <ник>
```

Пример:

```mcfunction
/cistiers PlayerName
```

Пример вывода:

```text
PlayerName | NOT RESTRICTED

Место: #12
Очки: 1840

Тиры:
🗡 HT1
🧪 LT2
❤ HT3
```

---

## Теги в TAB

Игроки автоматически получают тег своего лучшего тира рядом с ником в TAB-листе.

Пример:

```text
🗡 HT1 | PlayerName
🧪 LT2 | PlayerName
❤ HT3 | PlayerName
```

---

## Сборка из исходников

Обычная сборка:

```sh
./gradlew build
```

Готовый `.jar` файл появится в папке:

```text
build/libs/
```

---

## Сборка сразу в папку mods

Можно настроить Gradle так, чтобы после сборки `.jar` файл автоматически копировался в папку `mods` вашего профиля Minecraft.

Добавьте в `build.gradle`:

```gradle
def defaultModOutputDir = "/mnt/c/Users/eacherey/AppData/Roaming/AstralRinthApp/profiles/uku_s pvp modpack/mods"

tasks.register("buildToMods", Copy) {
    dependsOn remapJar

    from remapJar.archiveFile
    into providers.gradleProperty("modOutputDir")
            .map { file(it) }
            .orElse(file(defaultModOutputDir))
}

tasks.named("build") {
    finalizedBy("buildToMods")
}
```

После этого можно просто выполнить:

```sh
./gradlew build
```

После сборки мод автоматически скопируется в указанную папку `mods`.

Также можно указать другую папку без изменения `build.gradle`:

```sh
./gradlew build -PmodOutputDir="/путь/до/вашей/папки/mods"
```


---

## Если возникла проблема

* Найдите файл `latest.log` в папке логов Minecraft
* Создайте issue на GitHub
* Прикрепите `latest.log`
* Опишите, что произошло и как повторить проблему

---

## Важно

Мод использует публичное API CisTiers и не является официальной частью проекта CisTiers.

---

## Лицензия

[MIT](LICENSE)
