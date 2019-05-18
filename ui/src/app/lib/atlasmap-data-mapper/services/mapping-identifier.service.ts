import { Injectable } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class MappingIdentifierService {

  constructor(private route: ActivatedRoute) { }

  getCurrentMappingId():string{
    var mappingid = this.route.snapshot.paramMap.get('id');
    //If not set, return default
    return mappingid;
}
}
