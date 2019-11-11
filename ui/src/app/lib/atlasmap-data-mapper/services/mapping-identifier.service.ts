import {Injectable} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';

@Injectable()
export class MappingIdentifierService {

  constructor(private route: ActivatedRoute) {
  }

  getCurrentMappingId(): number {
    var urlMappingId = this.route.snapshot.paramMap.get('id');
    if (urlMappingId == null){
      return 0;
    }

    let mappingId: number = +urlMappingId;
    if (isNaN(mappingId) || mappingId == null) {
      // Default mapping id
      return 0;
    }
    return mappingId;
  }
}
