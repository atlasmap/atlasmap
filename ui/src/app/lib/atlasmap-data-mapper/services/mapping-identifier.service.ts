import {Injectable} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';

@Injectable()
export class MappingIdentifierService {

  constructor(private route: ActivatedRoute) {
  }

  getCurrentMappingDefinitionId(): number {
    const urlMappingId = this.route.snapshot.paramMap.get('id');
    if (urlMappingId == null) {
      return 0;
    }

    const mappingDefinitionId: number = +urlMappingId;
    if (isNaN(mappingDefinitionId) || mappingDefinitionId == null) {
      // Default mapping id
      return 0;
    }
    return mappingDefinitionId;
  }
}
