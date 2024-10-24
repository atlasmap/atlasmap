package io.atlasmap.autoMap;

import io.atlasmap.builder.DefaultAtlasMappingBuilder;
import io.atlasmap.core.DefaultAtlasSession;
import io.atlasmap.customcode.ObjectAutoMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

import java.util.List;

/**
 * @author vagrant
 * @version $ 10/8/24
 */
public class ObjectAutoMapper extends DefaultAtlasMappingBuilder {

    @Override
    public void processMapping() throws Exception {
        //TODO("Not yet implemented")
        DefaultAtlasSession atlasSession = (DefaultAtlasSession) getAtlasSession();
        System.out.println(atlasSession) ;
        ObjectAutoMapping customMapping = (ObjectAutoMapping) atlasSession.head().getCustomMapping();
        List<Field> sourceFields = customMapping.getInputField();
        List<Field> targetFields = customMapping.getOutputField();
        //FIXME : iterate using i loop so that index 1 in input field corresponds to index 1 in target field
        //FIXME: enhance validation of custom auto mapper by checking that source field and target field should be of size 1 only.
        for (Field sourceField : sourceFields) {
            String sourceDocId = sourceField.getDocId();
            String sourcePath = sourceField.getPath();
            for (Field targetField : targetFields) {
                String targetDocId = targetField.getDocId();
                String targetPath = targetField.getPath();
                read(sourceDocId,sourcePath).write(targetDocId , targetPath);
            }
        }

        FieldGroup sourceFieldGroup = customMapping.getInputFieldGroup();
        System.out.println(sourceFieldGroup + "sourceFieldGorup");
        for (Field sourceField : sourceFieldGroup.getField()) {
            String sourceDocId = sourceField.getDocId();
            String sourcePath = sourceField.getPath();
            for (Field targetField : targetFields) {
                String targetDocId = targetField.getDocId();
                String targetPath = targetField.getPath();
                read(sourceDocId,sourcePath).write(targetDocId , targetPath);
            }
        }
    }
}
