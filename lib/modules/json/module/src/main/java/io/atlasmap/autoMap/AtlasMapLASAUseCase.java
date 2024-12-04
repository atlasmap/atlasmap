package io.atlasmap.autoMap;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.json.inspect.JsonInspectionException;
import io.atlasmap.json.inspect.JsonSchemaInspector;
import io.atlasmap.json.v2.JsonDocument;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtlasMapLASAUseCase {

    public static void main(String[] args) throws AtlasException, URISyntaxException, IOException {

       /* WeakHashMap<Employee, String> weakHashMap = testWeakHashMapMethod();
        System.gc();
        System.out.println("weakhashmap entries from method: " + weakHashMap);
        System.gc();
        System.out.println("weakhashmap entries after gc: " + weakHashMap);

        HashMap<Employee, String> hashMap = testHashMapMethod();
        System.out.println("hashMap entries from method: " + hashMap);
        System.gc();
        System.out.println("hashMap entries after gc: " + hashMap);*/

        /*String schemaJSON = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/processRuleOneOfSchema.json" ).toURI())));


        try {
            JsonDocument jsonDocument =JsonSchemaInspector.instance().inspect(schemaJSON);
            System.out.println("---- jsonDocument ----" + new ObjectMapper().writeValueAsString(jsonDocument));
        } catch (JsonInspectionException e) {
            throw new RuntimeException(e);
        }*/


       //transformJSON("mappings/autoMappingWithStdMapping.json","data/ProcessRule.json" );
       //transformJSON("mappings/jsoncomplexType.json","data/ProcessRule.json" );

        //transformJSON("mappings/newautoMapUseCase.json","data/ProcessRule.json" );
         //transformJSON("mappings/arrayAutoMapping.json","data/ProcessRule.json" );
        //transformJSON("mappings/objectAutoMapping.json","data/ProcessRule.json" );
        //transformJSON("mappings/arrayAutoMapping.json","data/processRuleSchema.json" );
        //transformJSON("mappings/serializationMapping.json","data/serialization.json" );

       testAllUseCases();

    }

    private static HashMap<Employee, String> testHashMapMethod() {
        HashMap<Employee, String> map = new HashMap<>();
        map.put(new Employee(1, "one"), "one");
        map.put(new Employee(2, "two"), "two");
        map.put(new Employee(3, "three"), "three");
        System.out.println("hashmap entries insidemethod : " + map);
        return map;
    }

    private static WeakHashMap testWeakHashMapMethod() {
        WeakHashMap<Employee, String> map = new WeakHashMap<>();
        map.put(new Employee(1, "one"), "one");
        map.put(new Employee(2, "two"), "two");
        map.put(new Employee(3, "three"), "three");
        System.gc();
        System.out.println("weakhashmap entries : " + map);
        return map;
    }

    static class Employee {

        private int id;
        private String name;

        public Employee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        //constructors, getters and setters

        public String toString() {
            return "[Employee{id=" + id + " ,name=" + name + "}]";
        }
    }
    private static void testAllUseCases() throws AtlasException, URISyntaxException, IOException {
        List<String> scenarios = Stream.of(
            // Serialization
            "SER1",
            "SER2",
            "SER3",
            "SER4",
            "SER5",
            "SER6",
            "SER7",
            "SER8",
            "SER9",
            "SER10",
            "SER11",
            "SER12",
            // Deserialization
            "DES1",
            "DES2",
            "DES3",
            "DES4",
            "DES5",
            "DES6",
            "DES7",
            "DES8",
            "DES9",
            "DES10",
            "DES11",
            "NonDirectional/151Fields",
            "NonDirectional/DeepNested",
            "NonDirectional/OneToMany",
            "NonDirectional/ManyToOne"
        ).collect(Collectors.toList());

        for (String scenario : scenarios) {

            String mappingFilePath = "MappingScenarios/" + scenario + "/transform.json";
            String inputFilePath = "MappingScenarios/" + scenario + "/input.json";
            String actualOutput = transformJSON(mappingFilePath, inputFilePath);

            // Then expected transformed output matches actual output

            String outputfilePath = "/home/vagrant/atlasmap/lib/modules/json/module/src/main/resources/MappingScenarios/" + scenario + "/output.json";
            StringBuilder builder = new StringBuilder();
            try (BufferedReader buffer = new BufferedReader(new FileReader(outputfilePath))) {
                String str;
                while ((str = buffer.readLine()) != null) {
                    builder.append(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String expectedOutput = builder.toString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedJSON = mapper.readTree(expectedOutput);
            System.out.println("---- expectedJSON ----" + expectedJSON);
            JsonNode outputJSON = mapper.readTree(actualOutput);
            System.out.println( "transformation success ---> " + expectedJSON.equals(outputJSON));
        }
    }

    private static String transformJSON(String mappingFilePath, String inputFilePath) throws AtlasException, URISyntaxException, IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(mappingFilePath);
        AtlasContextFactory atlasContextFactory = DefaultAtlasContextFactory.getInstance();
        AtlasContext context = atlasContextFactory.createContext(/*AtlasContextFactory.Format.JSON,*/ url.toURI());
        AtlasSession session = context.createSession();
        String inputJSON = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource(inputFilePath).toURI())));
        session.setSourceDocument("inputJSON", inputJSON);
        //System.out.println("---- inputJSON ---- " + inputJSON);
        context.process(session);
        String targetDocument = (String) session.getDefaultTargetDocument();
        System.out.println("---- targetDocument ----" + targetDocument);
        return targetDocument;
    }
}
