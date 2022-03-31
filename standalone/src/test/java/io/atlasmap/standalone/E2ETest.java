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
package io.atlasmap.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.RemoteEndpoint.Basic;
import javax.xml.parsers.DocumentBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.websocket.WebSocketEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Json;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = { Application.class, CorsConfiguration.class, SecurityConfiguration.class, WebSocketConfiguration.class })
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
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@aria-label='Properties']")));
        WebElement atlasmapMenuBtn = driver.findElement(By.xpath("//button[@data-testid='atlasmap-menu-button']"));
        atlasmapMenuBtn.click();
        waitForLoad.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@data-testid='import-mappings-button']")));
        WebElement importBtn = driver.findElement(By.xpath("//a[@data-testid='import-mappings-button']"));
        importBtn.click();
        WebElement fileInput = driver.findElement(By.xpath("//div[@id='data-toolbar']//input[@type='file']"));
        String cwd = System.getProperty("user.dir");
        fileInput.sendKeys(cwd + "/src/test/resources/json-schema-source-to-xml-schema-target.adm");
        WebElement confirmBtn = driver.findElement(By.xpath("//button[@data-testid='confirmation-dialog-confirm-button']"));
        confirmBtn.click();
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@aria-label='JSONSchemaSource']")));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) { }

        // Check custom action param
        WebElement orderBtn = driver.findElement(By.xpath("//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order-toggle']"));
        orderBtn.click();
        By addressToggle = By.xpath(
                "//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order/address-toggle']");
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(addressToggle));
        WebElement addrBtn = driver.findElement(addressToggle);
        addrBtn.click();
        By addrDivPath = By.xpath(
                "//button[@id='sources-field-atlas:json:JSONSchemaSource:source:/order/address-toggle']/../..");
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(addrDivPath));
        WebElement addrDiv = driver.findElement(addrDivPath);
        By cityDivPath = By.xpath(".//button[@data-testid='grip-city-button']/../../../..");
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(cityDivPath));
        WebElement cityDiv = addrDiv.findElement(cityDivPath);
        Actions action = new Actions(driver);
        action.moveToElement(cityDiv).perform();
        waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//button[@data-testid='show-mapping-details-button']")));
        WebElement showDetailsBtn = cityDiv.findElement(By.xpath(".//button[@data-testid='show-mapping-details-button']"));
        showDetailsBtn.click();
        WebElement detailsCity = waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "//div[@role='dialog']//div[@aria-labelledby='mapping-field-city']")));
        assertNotNull(detailsCity);
        WebElement customActionParamInput = driver.findElement(By.id(
            "user-field-action-io.atlasmap.service.my.MyFieldActionsModel-transformation-0"));
        assertEquals("testparam", customActionParamInput.getAttribute("value"));
        // Check custom source class mapping
        WebElement customClassDoc = driver.findElement(By.xpath("//article[@aria-label='MyFieldActionsModel']"));
        WebElement paramDiv = customClassDoc.findElement(By.xpath(".//button[@data-testid='grip-param-button']/../../../.."));
        action = new Actions(driver);
        action.moveToElement(paramDiv).perform();
        waitForLoad.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[@data-testid='show-mapping-details-button']")));
        showDetailsBtn = paramDiv.findElement(By.xpath(".//button[@data-testid='show-mapping-details-button']"));
        showDetailsBtn.click();
        WebElement detailsPhotoUrl = waitForLoad.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "//div[@data-testid='column-mapping-details-area']" + "//div[@data-testid='mapping-field-photoUrl']")));
        assertNotNull(detailsPhotoUrl);

        atlasmapMenuBtn = driver.findElement(By.xpath("//button[@data-testid='atlasmap-menu-button']"));
        atlasmapMenuBtn.click();
        waitForLoad.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@data-testid='export-mappings-button']")));
        WebElement exportBtn = driver.findElement(By.xpath("//a[@data-testid='export-mappings-button']"));
        exportBtn.click();
        WebElement dialogDiv = driver.findElement(By.xpath("//div[@data-testid='export-catalog-dialog']/.."));
        WebElement exportInput = dialogDiv.findElement(By.id("filename"));
        String exportAdmFileName = UUID.randomUUID().toString() + "-exported.adm";
        exportInput.clear();
        exportInput.sendKeys(exportAdmFileName);
        confirmBtn = dialogDiv.findElement(By.xpath(".//button[@data-testid='confirmation-dialog-confirm-button']"));
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Executors.newSingleThreadExecutor().execute(() -> {
            long start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - start) < 300000) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            try {
                watcher.close();
            } catch (Exception e) {
                fail("Failed to close file watcher");
            }
        });
        Path dirPath = Paths.get(DLDIR);
        dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        confirmBtn.click();

        WatchKey key = watcher.take();
        while (key.isValid()) {
            List<WatchEvent<?>> events = key.pollEvents();
            if (events.isEmpty()) {
                Thread.sleep(1000);
                continue;
            }
            for (WatchEvent<?> event : events) {
                if (!StandardWatchEventKinds.ENTRY_CREATE.name().equals(event.kind().name())) {
                    continue;
                };
                Path eventPath = (Path)event.context();
                LOG.info("File '{}' is created", eventPath.getFileName().toString());
                if (!exportAdmFileName.equals(eventPath.getFileName().toString())) {
                    continue;
                }
                ADMArchiveHandler handler = new ADMArchiveHandler(getClass().getClassLoader());
                handler.setPersistDirectory(Paths.get(DLDIR).resolve("persist"));
                handler.setLibraryDirectory(Paths.get(DLDIR).resolve("lib"));
                handler.load(Paths.get(DLDIR).resolve(exportAdmFileName));
                handler.persist();
                assertEquals("UI.0", handler.getMappingDefinition().getName());
                DocumentKey sourceKey = new DocumentKey(DataSourceType.SOURCE, "JSONSchemaSource");
                DocumentMetadata sourceMeta = handler.getDocumentMetadata(sourceKey);
                assertEquals(DataSourceType.SOURCE, sourceMeta.getDataSourceType());
                assertEquals("JSONSchemaSource", sourceMeta.getName());
                assertEquals(DocumentType.JSON, sourceMeta.getDocumentType());
                new ObjectMapper().readTree(new FileInputStream(handler.getDocumentSpecificationFile(sourceKey)));
                DocumentKey targetKey = new DocumentKey(DataSourceType.TARGET, "XMLSchemaSource");
                DocumentMetadata targetMeta = handler.getDocumentMetadata(targetKey);
                assertEquals(DataSourceType.TARGET, targetMeta.getDataSourceType());
                assertEquals("XMLSchemaSource", targetMeta.getName());
                assertEquals(DocumentType.XML, targetMeta.getDocumentType());
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(handler.getDocumentSpecificationFile(targetKey)));
                return;
            };
        }
        fail("exported.adm was not created");
    }

    @Test
    public void testLSP() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        TestClientEndpoint clientEndpoint = new TestClientEndpoint();
        Configurator configurator = new ClientEndpointConfig.Configurator();
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().configurator(configurator).build();
        Session session = container.connectToServer(clientEndpoint, cec, new URI(String.format("ws://127.0.0.1:%s/atlas/lsp", port)));
        InitializeResult res = clientEndpoint.getClient().getRemote().initialize(new InitializeParams()).join();
        assertNotNull(res);
        session.close();
    }

    class TestClientEndpoint extends WebSocketEndpoint<LanguageServer> {
        TestLanguageClient client = new TestLanguageClient();

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            LOG.debug("LSP client: establishing a connection");
            session.setMaxTextMessageBufferSize(8192);
            session.setMaxBinaryMessageBufferSize(8192);;
            super.onOpen(session, config);
        }

        @Override
        protected void configure(Builder<LanguageServer> builder) {
            LOG.debug("LSP client: Configuring");
            builder.setLocalService(client);
            builder.setRemoteInterface(LanguageServer.class);
        }

        @Override
        protected void connect(Collection<Object> localServices, LanguageServer remoteProxy) {
            localServices.forEach(s -> ((TestLanguageClient)s).remote = remoteProxy);
        }

        public TestLanguageClient getClient() {
            return this.client;
        }
    }

    class TestLanguageClient implements LanguageClient {
        LanguageServer remote;

        @Override
        public void telemetryEvent(Object object) {
            LOG.debug("LSP Client: telemetry event: {}", object.toString());
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            LOG.debug("LSP Client: publish diagnostics: {}", diagnostics.toString());
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            LOG.debug("LSP Client: show message: {}", messageParams.toString());
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            LOG.debug("LSP Client: show message request: {}", requestParams.toString());
            return null;
        }

        @Override
        public void logMessage(MessageParams message) {
            LOG.debug("LSP Client: log message: {}", message.toString());
        }

        public LanguageServer getRemote() {
            return this.remote;
        }
    }
}
