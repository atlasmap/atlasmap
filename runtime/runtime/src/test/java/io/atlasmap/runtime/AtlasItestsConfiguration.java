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
package io.atlasmap.runtime;

import java.net.InetAddress;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.undertow.Undertow.Builder;

@Configuration
public class AtlasItestsConfiguration {
    private Properties properties;

    public AtlasItestsConfiguration() throws Exception {
        properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("atlas-itests.properties"));
    }

    @Bean
    public UndertowEmbeddedServletContainerFactory create() throws Exception {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();

        factory.setPort(Integer.parseInt(properties.getProperty("http.port", "8585")));
        factory.setAddress(InetAddress.getByName(properties.getProperty("http.host", "localhost")));
        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
            public void customize(Builder builder) {
                try {
                    builder.addHttpsListener(
                            Integer.parseInt(properties.getProperty("https.port", "8443")),
                            properties.getProperty("https.host", "localhost"),
                            createSslContext());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return factory;
    }

    private SSLContext createSslContext() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        String keyStoreFilename = properties.getProperty("https.keystore", "ssl.keystore");
        char[] keyStorePassword = properties.getProperty("https.keystore.password", "atlasmap").toCharArray();
        keyStore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFilename), keyStorePassword);
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keyStore, keyStorePassword);
        context.init(kmfactory.getKeyManagers(), new TrustManager[] {new DummyTrustManager()}, null);
        return context;
    }

    private class DummyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // no-op
        }
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // no-op
        }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }
}
