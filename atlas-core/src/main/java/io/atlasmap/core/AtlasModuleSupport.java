package io.atlasmap.core;

import java.util.ArrayList;
import java.util.List;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;

public class AtlasModuleSupport {
    
    public static List<String> listTargetPaths(AtlasMapping atlasMapping) {        
        if(atlasMapping == null || atlasMapping.getMappings() == null || atlasMapping.getMappings().getMapping() == null || atlasMapping.getMappings().getMapping().size() == 0) {
            return new ArrayList<String>();
        }
        
        return listTargetPaths(atlasMapping.getMappings().getMapping());
    }   
    
    public static List<String> listTargetPaths(List<BaseMapping> mappings) {
        List<String> targetPaths = new ArrayList<String>();
        
        if(mappings == null || mappings.size() == 0) {
            return targetPaths;
        }
        
        for(BaseMapping fm : mappings) {
            if(fm instanceof Mapping) {
                for(Field f : ((Mapping)fm).getOutputField()) {
                    targetPaths.add(f.getPath());
                }
            } else if(fm instanceof Collection) {
                if(((Collection)fm).getMappings() != null) {
                    targetPaths.addAll(listTargetPaths(((Collection)fm).getMappings().getMapping()));
                }
            }
        }
        
        return targetPaths;
    }
}
