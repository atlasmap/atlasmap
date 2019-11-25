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
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = { Application.class, CorsConfiguration.class, SecurityConfiguration.class })
public class E2ETest {

    @LocalServerPort
    int port;

    ChromeDriver driver;

    @Before
    public void before() {
        String driverPath = System.getProperty("webdriver.chrome.driver");
        assumeTrue(driverPath != null && !driverPath.isEmpty());

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-setuid-sandbox");
        driver = new ChromeDriver(options);
    }

    @After
    public void after() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testEmpty() throws IOException {
        driver.get("http://127.0.0.1:" + port);
        WebDriverWait waitForLoad = new WebDriverWait(driver, 5);
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.id("Properties")));
        WebElement container = driver.findElement(By.tagName("data-mapper-error"));
        List<WebElement> errors = container.findElements(By.className("alert-danger"));
        assertEquals("Some error message was found: " + container.getAttribute("innerHTML"), 0, errors.size());
    }

}