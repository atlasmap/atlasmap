package io.atlasmap.autoMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtlasMapLASAUseCase {

    public static void main(String[] args) throws AtlasException, URISyntaxException, IOException {
       // transformJSON("mappings/arrayAutoMapping.json","data/ProcessRule.json" );
        //transformJSON("mappings/objectAutoMapping.json","data/ProcessRule.json" );
        transformJSON("mappings/serializationMapping.json","data/serialization.json" );

       // testAllUseCases();

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
