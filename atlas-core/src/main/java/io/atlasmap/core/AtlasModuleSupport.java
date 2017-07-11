package io.atlasmap.core;

import java.util.ArrayList;
import java.util.List;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;

public class AtlasModuleSupport {
    
    public static List<String> listTargetPaths(AtlasMapping atlasMapping) {
        List<String> targetPaths = new ArrayList<String>();
        
        if(atlasMapping == null || atlasMapping.getMappings() == null || atlasMapping.getMappings().getMapping() == null || atlasMapping.getMappings().getMapping().size() == 0) {
            return targetPaths;
        }
        
        populateTargetPathFromMapping(atlasMapping.getMappings(), targetPaths);
        return targetPaths;
    }    
    protected static void populateTargetPathFromMapping(Mappings mappings, List<String> targetPaths) {
    
        for(BaseMapping fm : mappings.getMapping()) {
            if(fm instanceof Mapping) {
                for(Field f : ((Mapping)fm).getOutputField()) {
                    targetPaths.add(f.getPath());
                }
            } else if(fm instanceof Collection) {
                populateTargetPathFromMapping(((Collection)fm).getMappings(), targetPaths);
            }
        }
    }    
}
