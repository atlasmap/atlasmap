package io.atlasmap.dfdl.module;

import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.xml.module.XmlValidationService;

public class DfdlValidationService extends XmlValidationService {

    private AtlasModuleDetail moduleDetail = DfdlModule.class.getAnnotation(AtlasModuleDetail.class);

    public DfdlValidationService(AtlasConversionService conversionService, AtlasFieldActionService fieldActionService) {
        super(conversionService, fieldActionService);
    }

    @Override
    protected AtlasModuleDetail getModuleDetail() {
        return moduleDetail;
    }

}
