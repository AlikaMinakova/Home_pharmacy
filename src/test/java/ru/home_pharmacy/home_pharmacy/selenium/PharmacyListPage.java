package ru.home_pharmacy.home_pharmacy.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class PharmacyListPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By searchInput = By.name("keyword");
    private final By searchButton = By.cssSelector("button[type='submit']");
    private final By sortSelect = By.name("sort");
    private final By cardSelector = By.cssSelector(".card"); // каждая карточка остатка
    private final By medicationNameInCard = By.cssSelector(".card h5, .card h6"); // название лекарства в карточке

    public PharmacyListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    public void open(String baseUrl) {
        driver.get(baseUrl + "/pharmacies");
    }

    public void searchByKeyword(String keyword) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys(keyword);
        driver.findElement(searchButton).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card, .alert-info")));
    }

    public void selectSortOption(String value) {
        WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(sortSelect));
        Select select = new Select(selectElement);
        select.selectByValue(value);
        wait.until(ExpectedConditions.presenceOfElementLocated(cardSelector));
    }

    public List<String> getMedicationNames() {
        List<WebElement> cards = driver.findElements(cardSelector);
        return cards.stream()
                .map(card -> card.findElement(medicationNameInCard).getText())
                .collect(Collectors.toList());
    }

    public boolean isMedicationPresent(String name) {
        List<WebElement> cards = driver.findElements(cardSelector);
        return cards.stream()
                .anyMatch(card -> {
                    try {
                        String cardName = card.findElement(medicationNameInCard).getText();
                        return cardName.equals(name);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }
    public boolean hasData() {
        return !driver.findElements(cardSelector).isEmpty();
    }
}