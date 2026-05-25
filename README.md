# WPS4PIN

> 💛 Поддержать проект: [ЮMoney](https://yoomoney.ru/to/410018488997376)

Android-приложение для расчёта WPS PIN-кода по MAC-адресу сетевого устройства (роутер, CPE, AP, gateway, ONT).

## Возможности

- 🔐 **Расчёт WPS PIN** — по алгоритму GTIN-8 с контрольной суммой
- 📋 **Любой формат MAC** — `XX:XX:XX:XX:XX:XX`, `XX-XX-XX-XX-XX-XX` или `XXXXXXXXXXXX`
- ⚡ **Реактивный пересчёт** — PIN обновляется мгновенно при вводе
- 📋 **Копирование одним нажатием** — MAC и PIN в буфер обмена
- 📜 **История расчётов** — сохраняется локально, повторный вызов одним нажатием
- 🌗 **Тёмная и светлая тема** — автоматическое переключение
- 🔒 **Полностью офлайн** — без интернета, без разрешений, без сбора данных
- ⚠️ **Дисклеймер безопасности** — при первом запуске

## Использование

1. Введите MAC-адрес устройства в любом формате
2. PIN рассчитается автоматически
3. Нажмите на результат, чтобы скопировать в буфер
4. История доступна через меню

## Скриншоты

| Светлая тема | Тёмная тема |
|--------------|-------------|
| `screenshots/screenshot_light.png` | `screenshots/screenshot_dark.png` |

## Технологии

- **Kotlin** + **Jetpack Compose** (Material 3)
- **DataStore** — локальное хранение истории
- Min SDK 26, Target SDK 34
- Android 8.0+
- APK ~1.5 МБ

## Сборка

```bash
git clone https://github.com/namotoff/Wps4PinAndroid.git
cd Wps4PinAndroid
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

Для подписи релиза создай `keystore.properties` в корне проекта.

## Установка

APK доступен в [RuStore](https://rustore.ru/) и в [GitHub Releases](../../releases).

## Политика конфиденциальности

https://namotoff.github.io/wps4pin-privacy/

## ⚠️ Дисклеймер

Приложение предназначено **исключительно для аудита безопасности собственных сетевых устройств**. Несанкционированный доступ к чужим сетям запрещён законом.

## Лицензия

Apache License 2.0 — см. [LICENSE](LICENSE).

## Контакты

Email: edazin@bk.ru
