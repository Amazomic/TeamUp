# TeamUp

**Небольшое Android-приложение для быстрого случайного распределения игроков по командам.**

[English](README.md) · [Последний релиз](https://github.com/Amazomic/TeamUp/releases/latest)

## Возможности

- Выбор от 2 до 30 игроков и от 2 до 8 команд.
- Имена игроков и цветное отображение назначенных команд.
- Перемешивание команд без пересоздания списка игроков.
- История жеребьёвок с возможностью удалить отдельные записи или очистить историю.
- Локальное сохранение имён, настроек и текущих назначений.
- Не нужны аккаунт и подключение к интернету; нет рекламы и аналитики.

## Скачать

Исходный код и заметки к версиям доступны в [GitHub Releases](https://github.com/Amazomic/TeamUp/releases). При публикации каждого релиза GitHub Actions соберёт APK с понятным именем `TeamUp-<версия>-Android-debug.apk`. Это тестовая сборка с отладочной подписью: перед установкой следующего APK удалите предыдущий.

## Сборка из исходного кода

Требуются Android Studio с JDK 17 или новее и Android SDK Platform 36.

```powershell
.\gradlew.bat assembleDebug
```

APK появится в `app/build/outputs/apk/debug/TeamUp-<версия>-Android-debug.apk`.

Для создания App Bundle Google Play настройте собственный ключ, скопировав [`signing.properties.example`](signing.properties.example) в `signing.properties`, затем выполните:

```powershell
.\gradlew.bat bundleRelease
```

Храните keystore и пароли в безопасном месте и не добавляйте их в Git. AAB появится в `app/build/outputs/bundle/release/app-release.aab`.

## Конфиденциальность

Имена игроков, настройки команд и история сохраняются локально на устройстве и не отправляются на сервер. См. [черновик политики конфиденциальности](play-store/privacy-policy-draft-ru.md).

## Сведения о проекте

- Application ID: `ru.teampicker.app`
- Версия: `1.2.0` (version code `3`)
- Минимальная версия Android: Android 6.0 (API 23)
- Целевая версия Android: Android 16 (API 36)

История релизов находится в [CHANGELOG.md](CHANGELOG.md). TeamUp распространяется на условиях [GNU GPL v3](LICENSE).
