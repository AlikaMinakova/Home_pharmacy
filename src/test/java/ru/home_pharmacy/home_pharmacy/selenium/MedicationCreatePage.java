package ru.home_pharmacy.home_pharmacy.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MedicationCreatePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "name")
    private WebElement nameInput;

    @FindBy(id = "description")
    private WebElement descriptionInput;

    @FindBy(id = "imageInput")
    private WebElement fileInput;

    @FindBy(xpath = "//button[text()='Создать']")
    private WebElement submitButton;

    public MedicationCreatePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void fillName(String name) {
        nameInput.sendKeys(name);
    }

    public void fillDescription(String description) {
        descriptionInput.sendKeys(description);
    }

    public void uploadImage(String filePath) {
        fileInput.sendKeys(filePath);
    }

    public void selectDiseaseById(String diseaseId) {
        WebElement chk = driver.findElement(By.xpath("//input[@name='diseases' and @value='" + diseaseId + "']"));
        if (!chk.isSelected()) chk.click();
    }

    public void selectIngredientById(String ingredientId) {
        WebElement chk = driver.findElement(By.xpath("//input[@name='activeIngredients' and @value='" + ingredientId + "']"));
        if (!chk.isSelected()) chk.click();
    }

    public void submit() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        wait.until(ExpectedConditions.urlContains("/medication"));
    }
}