import os
import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

@pytest.fixture(scope="function")
def driver():
    """
    Фикстура: создание драйвера
    """
    APK_PATH = r"C:\Users\Elizaveta\lb7_test\app\build\outputs\apk\debug\app-debug.apk"
    assert os.path.exists(APK_PATH), f"APK не найден: {APK_PATH}"

    options = UiAutomator2Options()
    options.device_name = "emulator-5554"
    options.app = APK_PATH
    options.automation_name = "UiAutomator2"
    options.set_capability("unicodeKeyboard", True)
    options.set_capability("resetKeyboard", True)

    driver = webdriver.Remote("http://127.0.0.1:4723", options=options)
    yield driver
    driver.quit()

def test_create_note(driver):
    """
    Тестирование создания заметки
    """
    wait = WebDriverWait(driver, 10)

    # Находим поле ввода и кнопку добавления
    note_input = wait.until(EC.presence_of_element_located((By.ID, "com.example.lb7_test:id/inputNote")))
    add_button = driver.find_element(By.ID, "com.example.lb7_test:id/btnSave")

    # Ввод текста и добавление заметки
    note_input.send_keys("Сделать ЛБ7!")
    add_button.click()

    # Проверяем, что заметка появилась в списке
    first_note = wait.until(EC.presence_of_element_located((By.XPATH, "//android.widget.ListView/*[1]")))
    assert "Сделать ЛБ7!" in first_note.text, "Заметка не отображается в списке"
