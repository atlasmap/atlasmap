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
package io.atlasmap.examples.camel.main;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class Application extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=5000")
            .setBody(simple("resource:classpath:order.json"))
            .log("--&gt; Sending: [${body}]")
            .to("atlasmap:atlasmap-mapping.adm")
            .log("--&lt; Received: [${body}]");
    }

    public static void main(String args[]) throws Exception {
        Main camelMain = new Main();
        camelMain.configure().addRoutesBuilder(new Application());
        camelMain.run(args);
    }
}
