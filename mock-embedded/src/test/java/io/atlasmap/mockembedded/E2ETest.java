/*
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
package io.atlasmap.mockembedded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = { Application.class, CorsConfiguration.class, SecurityConfiguration.class })
public class E2ETest {

    private static final Logger LOG = LoggerFactory.getLogger(E2ETest.class);
    private static final String DLDIR = System.getProperty("user.dir") + File.separator + "target";

    @LocalServerPort
    int port;

    ChromeDriver driver;

    @BeforeEach
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

    @AfterEach
    public void after() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() throws Exception {
        driver.get("http://127.0.0.1:" + port);
        WebDriverWait waitForLoad = new WebDriverWait(driver, Duration.ofSeconds(30));
        /** TODO possibly a bug related to https://github.com/atlasmap/atlasmap/issues/3994
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@aria-label='JSONSchemaSource']")));

        // Check custom action param
        WebElement orderBtn = driver.findElement(By.xpath("//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order-toggle']"));
        orderBtn.click();
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-testid='document-orderId-field']")));
         */
    }

}
