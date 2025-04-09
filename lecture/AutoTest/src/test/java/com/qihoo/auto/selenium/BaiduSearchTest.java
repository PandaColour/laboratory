package com.qihoo.auto.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaiduSearchTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        // 设置 Chrome 驱动
        WebDriverManager.chromedriver().setup();

        // 配置 Chrome 选项
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless"); // 无头模式，不显示浏览器窗口
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // 创建 WebDriver 实例
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
    }

    @Test
    void testBaiduSearch() {
        // 打开百度首页
        driver.get("https://www.baidu.com");

        // 等待搜索框可见
        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("kw"))
        );

        // 输入搜索关键词
        searchBox.sendKeys("AI");

        // 点击搜索按钮
        WebElement searchButton = driver.findElement(By.id("su"));
        searchButton.click();

        // 等待搜索结果加载
        wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("content_left"))
        );

        // 验证搜索结果
        WebElement resultsContainer = driver.findElement(By.id("content_left"));
        assertTrue(resultsContainer.isDisplayed(), "搜索结果应该显示");

        // 验证搜索结果数量
        int resultCount = driver.findElements(By.cssSelector("#content_left .result")).size();
        assertTrue(resultCount > 0, "应该至少有一个搜索结果");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}