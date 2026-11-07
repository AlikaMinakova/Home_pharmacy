package ru.home_pharmacy.home_pharmacy.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DiseaseListPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public DiseaseListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/diseases?size=100");
    }

    public boolean isDiseasePresent(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'card')]//h5[text()='" + name + "']")
            )) != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void deleteDisease(String name) {
        WebElement card = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class,'card')]//h5[text()='" + name + "']/ancestor::div[contains(@class,'card')]")
        ));

        WebElement deleteButton = card.findElement(By.xpath(".//button[contains(text(),'Удалить')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card')]//h5[text()='" + name + "']")
        ));
    }
}