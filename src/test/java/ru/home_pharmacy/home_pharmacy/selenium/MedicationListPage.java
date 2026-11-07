package ru.home_pharmacy.home_pharmacy.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MedicationListPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MedicationListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/medication?size=100");
    }

    public boolean isMedicationPresent(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'card')]//h6[text()='" + name + "']")
            )) != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void editMedication(String medicationName) {
        WebElement card = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class,'card')]//h6[text()='" + medicationName + "']/ancestor::div[contains(@class,'card-body')]")
        ));
        WebElement editButton = card.findElement(By.cssSelector("a.btn-edit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", editButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);
    }

    public void deleteMedication(String medicationName) {
        WebElement card = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class,'card')]//h6[text()='" + medicationName + "']/ancestor::div[contains(@class,'card-body')]")
        ));
        WebElement deleteButton = card.findElement(By.cssSelector("button.btn-delete"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);
        wait.until(ExpectedConditions.urlContains("/medication"));
    }

    public void searchByName(String name) {
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        searchInput.clear();
        searchInput.sendKeys(name);

        WebElement searchForm = driver.findElement(By.cssSelector("form[action='/medication']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", searchForm);

        wait.until(ExpectedConditions.urlContains("name="));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card, .alert-info")));
        wait.until(d -> isMedicationPresent(name));
    }

    public void resetSearch() {
        WebElement resetLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/medication']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", resetLink);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resetLink);
        wait.until(ExpectedConditions.urlContains("/medication"));
    }
}