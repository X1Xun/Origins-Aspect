import os
import json

# ==================== НАСТРОЙКИ МОДА ====================
MOD_ID = "origins"  # Ваш ID мода из gradle
BASE_TEXTURE = f"{MOD_ID}:item/blueprint_base"  # Общая текстура для всех схем

# СПИСОК ПРЕДМЕТОВ: Просто добавляйте сюда новые ID через запятую!
# Скрипт сам сгенерирует для них JSON-модели и добавит перевод.
BLUEPRINTS = {
    "grappling_hook": "Крюк-кошка",
    "explosive_arrow_item": "Взрывная стрела"
    # "название_предмета_в_коде": "Красивое имя на русском"
}
# ========================================================

# Базовые пути к ресурсам мода
BASE_PATH = f"src/main/resources/assets/{MOD_ID}"
MODELS_PATH = os.path.join(BASE_PATH, "models/item")
LANG_PATH = os.path.join(BASE_PATH, "lang")

# Создаем папки, если их вдруг нет
os.makedirs(MODELS_PATH, exist_ok=True)
os.makedirs(LANG_PATH, exist_ok=True)


def generate_models():
    """Создает индивидуальные JSON-модели, ссылающиеся на общую текстуру"""
    model_content = {"parent": "item/generated", "textures": {"layer0": BASE_TEXTURE}}

    for item_name in BLUEPRINTS.keys():
        file_name = f"{item_name}_blueprint.json"
        file_path = os.path.join(MODELS_PATH, file_name)

        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(model_content, f, indent=2)
        print(f"✔ Создана модель: {file_name}")


def update_translation():
    """Добавляет новые переводы в файл локализации, не затирая старые"""
    lang_file = os.path.join(LANG_PATH, "ru_ru.json")

    # Если файл уже существует, читаем его данные
    if os.path.exists(lang_file):
        try:
            with open(lang_file, "r", encoding="utf-8") as f:
                lang_data = json.load(f)
        except json.JSONDecodeError:
            lang_data = {}
    else:
        lang_data = {}

    # Добавляем новые строки перевода
    for item_name, russian_name in BLUEPRINTS.items():
        translation_key = f"item.{MOD_ID}.{item_name}_blueprint"
        lang_data[translation_key] = f"Схема: {russian_name}"

    # Сохраняем обновленный файл перевода
    with open(lang_file, "w", encoding="utf-8") as f:
        json.dump(lang_data, f, indent=2, ensure_ascii=False)
    print("✔ Файл локализации ru_ru.json успешно обновлен!")


if __name__ == "__main__":
    print("=== Запуск генерации ресурсов для схем ===")
    generate_models()
    update_translation()
    print("=== Всё готово! Перезапустите Minecraft ===")
