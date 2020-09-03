package io.atlasmap.examples.camel.main;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.support.DefaultMessage;

public class Application extends RouteBuilder {

    private static final String SOURCE_MAP_NAME = "atlasSourceMap";

    @Override
    public void configure() throws Exception {
        from("timer:main?period=5000")
            .to("direct:order-producer")
            .to("direct:contact-producer")
            .to(String.format("atlas:atlasmap-mapping.adm?sourceMapName=%s", SOURCE_MAP_NAME))
            .to("direct:outcome-consumer");

        from("direct:order-producer")
            .process(new MessageCaptureProcessor().setSource("order-schema", "data/order.json"))
            .log("--> Order: [${body}]");
        
        from("direct:contact-producer")
            .process(new MessageCaptureProcessor().setSource("contact-schema", "data/contact.xml"))
            .log("--> Contact: [${body}]");
        
        from("direct:outcome-consumer")
            .log("--< Outcome: body:[")
            .log("${body}")
            .log("], Headers:[")
            .log("Order Document ID:${header.order-id}, Order path:${header.order-path}")
            .log("Contact Document ID:${header.contact-id}, Contact path:${header.contact-path}")
            .log("]");
    }

    public static void main(String args[]) throws Exception {
        Main camelMain = new Main();
        camelMain.configure().addRoutesBuilder(new Application());
        camelMain.run(args);
    }

    public class MessageCaptureProcessor implements Processor {
        private String id;
        private String path;

        public MessageCaptureProcessor setSource(String id, String path) {
            this.id = id;
            this.path = path;
            return this;
        }

        public void process(Exchange ex) throws Exception {
            Message msg = new DefaultMessage(ex);
            msg.setHeader("id", id);
            msg.setHeader("path", path);
            URL url = ex.getContext().getClassResolver().loadResourceAsURL(path);
            String doc = new String(Files.readAllBytes(Paths.get(url.toURI())));
            msg.setBody(doc);

            Map sourceMap = (Map) ex.getProperty(SOURCE_MAP_NAME);
            if (sourceMap == null) {
                sourceMap = new HashMap<String, Object>();
                ex.setProperty(SOURCE_MAP_NAME, sourceMap);
            }
            sourceMap.put(id, msg);
            ex.setMessage(msg);
        }
    }

}
