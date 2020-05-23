/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.atlasmap.core.ADMArchiveHandler;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = { Application.class, CorsConfiguration.class, SecurityConfiguration.class })
public class E2ETest {

    private static final String DLDIR = System.getProperty("user.dir") + File.separator + "target";

    @LocalServerPort
    int port;

    ChromeDriver driver;

    @Before
    public void before() {
        String driverPath = System.getProperty("webdriver.chrome.driver");
        assumeTrue(driverPath != null && !driverPath.isEmpty());

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-setuid-sandbox");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DLDIR);
        options.setExperimentalOption("prefs", prefs);
        driver = new ChromeDriver(options);
    }

    @After
    public void after() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() throws Exception {
        driver.get("http://127.0.0.1:" + port);
        WebDriverWait waitForLoad = new WebDriverWait(driver, 5);
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@aria-label='Properties']")));
        WebElement importBtn = driver.findElement(By.xpath("//button[@data-testid='import-mappings-button']"));
        importBtn.click();
        WebElement fileInput = driver.findElement(By.xpath("//div[@id='data-toolbar']//input[@type='file']"));
        String cwd = System.getProperty("user.dir");
        fileInput.sendKeys(cwd + "/src/test/resources/json-schema-source-to-xml-schema-target.adm");
        WebElement confirmBtn = driver.findElement(By.xpath("//button[@data-testid='confirmation-dialog-confirm-button']"));
        confirmBtn.click();
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@aria-label='JSONSchemaSource']")));
        WebElement orderBtn = driver.findElement(By.xpath("//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order-toggle']"));
        orderBtn.click();
        WebElement addrBtn = driver.findElement(By.xpath(
            "//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order/address-toggle']"));
        addrBtn.click();
        WebElement addrDiv = driver.findElement(By.xpath(
            "//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order/address-toggle']/../.."));
        WebElement cityDiv = addrDiv.findElement(By.xpath(".//button[@data-testid='grip-city-button']/../../../.."));
        Actions action = new Actions(driver);
        action.moveToElement(cityDiv).perform();
        WebElement showDetailsBtn = cityDiv.findElement(By.xpath(".//button[@data-testid='show-mapping-details-button']"));
        showDetailsBtn.click();
        WebElement detailsCity = waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "//div[@role='dialog']//div[@aria-labelledby='mapping-field-city']")));
        assertNotNull(detailsCity);

        WebElement exportBtn = driver.findElement(By.xpath("//button[@data-testid='export-mappings-button']"));
        exportBtn.click();
        WebElement dialogDiv = driver.findElement(By.xpath("//div[@role='dialog' and @aria-label='Export Mappings and Documents.']"));
        WebElement exportInput = dialogDiv.findElement(By.id("filename"));
        String exportAdmFileName = UUID.randomUUID().toString() + "-exported.adm";
        exportInput.clear();
        exportInput.sendKeys(exportAdmFileName);
        confirmBtn = dialogDiv.findElement(By.xpath(".//button[@data-testid='confirmation-dialog-confirm-button']"));
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dirPath = Paths.get(DLDIR);
        dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        confirmBtn.click();
        WatchKey key = watcher.poll(5, TimeUnit.SECONDS);
        for (WatchEvent<?> event : key.pollEvents()) {
            if (!StandardWatchEventKinds.ENTRY_CREATE.name().equals(event.kind().name())) {
                continue;
            };
            Path eventPath = (Path)event.context();
            if (!exportAdmFileName.equals(eventPath.getFileName().toString())) {
                continue;
            }
            ADMArchiveHandler handler = new ADMArchiveHandler(getClass().getClassLoader());
            handler.load(Paths.get(DLDIR + File.separator + exportAdmFileName));
            assertEquals("UI.0", handler.getMappingDefinition().getName());
            return;
        };
        fail("exported.adm was not created");
    }

}