package ru.home_pharmacy.home_pharmacy.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class DiseaseCreatePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public DiseaseCreatePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/diseases/new");
    }

    public void fillName(String name) {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameInput.clear();
        nameInput.sendKeys(name);
    }

    public void fillDescription(String description) {
        WebElement descInput = driver.findElement(By.id("description"));
        descInput.clear();
        descInput.sendKeys(description);
    }

    public void selectFirstSymptom() {
        List<WebElement> checkboxes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//input[@name='symptomIds']")
        ));
        if (checkboxes.isEmpty()) {
            throw new RuntimeException("Нет симптомов для выбора");
        }
        WebElement first = checkboxes.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", first);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", first);
    }

    public void submit() {
        WebElement submitButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[text()='Создать']")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
    }
    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".text-danger")
            )).getText();
        } catch (TimeoutException e) {
            return null;
        }
    }
}