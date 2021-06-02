/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { ADMDigest } from '../contracts/adm-digest';
import { CommonUtil } from './common-util';
import { ConfigModel } from '../models/config.model';

export class MappingDigestUtil {
  static generateMappingDigest(cfg: ConfigModel, mappingJson: any): ADMDigest {
    let mappingDigest: ADMDigest = {
      exportMappings: { value: '' },
      exportMeta: [],
      exportBlockData: [],
    };

    // Retrieve the JSON mappings buffer from the server.
    const jsonBuffer = JSON.stringify(mappingJson);
    if (jsonBuffer) {
      mappingDigest.exportMappings.value = CommonUtil.sanitizeJSON(jsonBuffer);
    }

    for (const doc of cfg.getAllDocs()) {
      if (!doc.isPropertyOrConstant) {
        mappingDigest.exportMeta.push({
          name: doc.name,
          dataSourceType: doc.type,
          id: doc.id,
          inspectionType: doc.inspectionType,
          inspectionParameters: doc.inspectionParameters,
          isSource: doc.isSource,
        });
        mappingDigest.exportBlockData.push({ value: doc.inspectionSource });
      }
    }
    return mappingDigest;
  }
}
