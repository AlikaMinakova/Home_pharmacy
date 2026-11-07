package ru.home_pharmacy.home_pharmacy.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MedicationSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private MedicationListPage listPage;
    private MedicationCreatePage createPage;
    private MedicationUpdatePage updatePage;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void init() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        listPage = new MedicationListPage(driver);
        createPage = new MedicationCreatePage(driver);
        updatePage = new MedicationUpdatePage(driver);
        listPage.open("http://localhost:" + port);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    private void selectFirstCheckbox(String fieldName) {
        String xpath = "//input[@name='" + fieldName + "']";
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        List<WebElement> checkboxes = driver.findElements(By.xpath(xpath));
        if (!checkboxes.isEmpty()) {
            WebElement first = checkboxes.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", first);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", first);
        } else {
            fail("Не найден чекбокс с именем " + fieldName);
        }
    }

    private void safeUploadImage(MedicationCreatePage page, String resourcePath) {
        try {
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                String imagePath = imageUrl.getPath();
                if (imagePath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    imagePath = imagePath.substring(1);
                }
                page.uploadImage(imagePath);
            } else {
                System.out.println("Файл " + resourcePath + " не найден, пропускаем загрузку");
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки изображения: " + e.getMessage());
        }
    }

    private void safeUploadImage(MedicationUpdatePage page, String resourcePath) {
        try {
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl != null) {
                String imagePath = imageUrl.getPath();
                if (imagePath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    imagePath = imagePath.substring(1);
                }
                page.uploadImage(imagePath);
            } else {
                System.out.println("Файл " + resourcePath + " не найден, пропускаем загрузку");
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки изображения: " + e.getMessage());
        }
    }

    private boolean ensureMedicationExists(String name, String description) {
        if (listPage.isMedicationPresent(name)) {
            System.out.println("Лекарство '" + name + "' уже существует, пропускаем создание");
            return true;
        }
        driver.get("http://localhost:" + port + "/medication/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        createPage.fillName(name);
        if (description != null) createPage.fillDescription(description);
        selectFirstCheckbox("diseases"); // обязательно!
        safeUploadImage(createPage, "/test.jpg");
        createPage.submit();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/medication"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-danger"))
        ));
        if (driver.getCurrentUrl().contains("/new")) {
            String errorMsg = driver.findElement(By.cssSelector(".text-danger")).getText();
            fail("Создание не удалось: " + errorMsg);
        }
        wait.until(d -> listPage.isMedicationPresent(name));
        return true;
    }


    @Test
    void testAllPagesLoadSuccessfully() {
        List<String> urls = Arrays.asList(
                "/medication",
                "/diseases",
                "/symptoms",
                "/ingredients",
                "/pharmacies"
        );

        for (String url : urls) {
            String fullUrl = "http://localhost:" + port + url;
            driver.get(fullUrl);

            assertFalse(driver.getCurrentUrl().contains("error"),
                    "Страница " + fullUrl + " перенаправила на error");

            String pageSource = driver.getPageSource();
            assertFalse(pageSource.contains("Whitelabel Error Page"),
                    "Страница " + fullUrl + " содержит сообщение об ошибке");

            assertTrue(driver.findElements(By.tagName("body")).size() > 0,
                    "Страница " + fullUrl + " не загрузилась (пустое body)");
        }
    }

    @Test
    void testEditMedication() {
        String originalName = "Тест-Парацетамол";
        String updatedName = "Тест-Парацетамол (обновлён)";

        ensureMedicationExists(originalName, "Оригинальное описание");

        listPage.editMedication(originalName);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        updatePage.updateName(updatedName);
        safeUploadImage(updatePage, "/paracetamol.jpg");
        updatePage.submitUpdate();

        wait.until(d -> listPage.isMedicationPresent(updatedName));
        assertTrue(listPage.isMedicationPresent(updatedName), "Обновлённое лекарство не появилось");
        assertFalse(listPage.isMedicationPresent(originalName), "Старое лекарство ещё отображается");

        listPage.deleteMedication(updatedName);
        wait.until(d -> !listPage.isMedicationPresent(updatedName));
        assertFalse(listPage.isMedicationPresent(updatedName), "Лекарство не удалилось");
    }

    @Test
    void testCreateAndDeleteMedication() {
        String medicineName = "Тест-Аспирин";

        if (listPage.isMedicationPresent(medicineName)) {
            listPage.deleteMedication(medicineName);
            wait.until(d -> !listPage.isMedicationPresent(medicineName));
        }

        driver.get("http://localhost:" + port + "/medication/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        createPage.fillName(medicineName);
        createPage.fillDescription("Тестовое описание");
        selectFirstCheckbox("diseases");
        safeUploadImage(createPage, "/test.jpg");
        createPage.submit();

        wait.until(ExpectedConditions.urlContains("/medication"));
        assertFalse(driver.getCurrentUrl().contains("/new"), "Остались на странице создания – ошибка валидации");

        wait.until(d -> listPage.isMedicationPresent(medicineName));
        assertTrue(listPage.isMedicationPresent(medicineName), "Лекарство не создалось");

        listPage.deleteMedication(medicineName);
        wait.until(d -> !listPage.isMedicationPresent(medicineName));
        assertFalse(listPage.isMedicationPresent(medicineName), "Лекарство не удалилось");
    }

    @Test
    void testCreateAndDeleteDisease() {
        String diseaseName = "Тест-Грипп";
        String description = "Тестовое описание гриппа";
        String baseUrl = "http://localhost:" + port;

        DiseaseCreatePage diseaseCreatePage = new DiseaseCreatePage(driver);
        DiseaseListPage diseaseListPage = new DiseaseListPage(driver);

        diseaseListPage.open(baseUrl);

        if (diseaseListPage.isDiseasePresent(diseaseName)) {
            diseaseListPage.deleteDisease(diseaseName);

            wait.until(d -> !diseaseListPage.isDiseasePresent(diseaseName));
        }

        diseaseCreatePage.open(baseUrl);
        diseaseCreatePage.fillName(diseaseName);
        diseaseCreatePage.fillDescription(description);
        diseaseCreatePage.selectFirstSymptom();
        diseaseCreatePage.submit();

        wait.until(ExpectedConditions.urlContains("/diseases"));

        diseaseListPage.open(baseUrl);
        wait.until(d -> diseaseListPage.isDiseasePresent(diseaseName));
        assertTrue(diseaseListPage.isDiseasePresent(diseaseName), "Болезнь не создалась");

        diseaseListPage.deleteDisease(diseaseName);
        wait.until(d -> !diseaseListPage.isDiseasePresent(diseaseName));
        assertFalse(diseaseListPage.isDiseasePresent(diseaseName), "Болезнь не удалилась");
    }

    @Test
    void testSortPharmacies() {
        PharmacyListPage pharmacyPage = new PharmacyListPage(driver);
        pharmacyPage.open("http://localhost:" + port);

        if (!pharmacyPage.hasData()) {
            System.out.println("Нет данных об остатках в аптеках, тест сортировки пропущен.");
            return;
        }

        pharmacyPage.selectSortOption("purchaseDate");
        List<String> namesByPurchase = pharmacyPage.getMedicationNames();

        pharmacyPage.selectSortOption("expirationDate");
        List<String> namesByExpiration = pharmacyPage.getMedicationNames();

        assertNotEquals(namesByPurchase, namesByExpiration,
                "Порядок сортировки не изменился");
    }

    @Test
    void testSearchTwoMedications() {
        String med1 = "Парацетамол";
        String med2 = "Нурофен";

        assumeTrue(listPage.isMedicationPresent(med1), "Препарат '" + med1 + "' отсутствует в БД");
        assumeTrue(listPage.isMedicationPresent(med2), "Препарат '" + med2 + "' отсутствует в БД");

        listPage.searchByName(med1);
        assertTrue(listPage.isMedicationPresent(med1), med1 + " не найден после поиска");
        assertFalse(listPage.isMedicationPresent(med2), med2 + " не должен отображаться при поиске " + med1);

        listPage.resetSearch();
        wait.until(d -> listPage.isMedicationPresent(med1));

        listPage.searchByName(med2);
        assertTrue(listPage.isMedicationPresent(med2), med2 + " не найден после поиска");
        assertFalse(listPage.isMedicationPresent(med1), med1 + " не должен отображаться при поиске " + med2);
    }

    @Test
    void testCreateDiseaseWithoutSymptom() {
        String diseaseName = "Тест-БезСимптома";
        String baseUrl = "http://localhost:" + port;

        DiseaseCreatePage createPage = new DiseaseCreatePage(driver);
        DiseaseListPage listPage = new DiseaseListPage(driver);

        listPage.open(baseUrl);
        if (listPage.isDiseasePresent(diseaseName)) {
            listPage.deleteDisease(diseaseName);
        }

        createPage.open(baseUrl);
        createPage.fillName(diseaseName);
        createPage.fillDescription("Описание без симптома");
        createPage.submit();

        wait.until(ExpectedConditions.urlContains("/diseases"));
        listPage.open(baseUrl);
        assertFalse(listPage.isDiseasePresent(diseaseName),
                "Болезнь не должна была создаться без симптома, но она появилась в списке");
    }
}