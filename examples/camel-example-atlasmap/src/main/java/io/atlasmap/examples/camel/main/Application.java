package io.atlasmap.examples.camel.main;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class Application extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=5000")
            .setBody(simple("resource:classpath:order.json"))
            .log("--&gt; Sending: [${body}]")
            .to("atlas:atlasmap-mapping.adm")
            .log("--&lt; Received: [${body}]");
    }

    public static void main(String args[]) throws Exception {
        Main camelMain = new Main();
        camelMain.configure().addRoutesBuilder(new Application());
        camelMain.run(args);
    }
}
