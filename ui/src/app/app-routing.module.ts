import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DataMapperAppExampleHostComponent} from "./lib/atlasmap-data-mapper/components/data-mapper-example-host.component";

const routes: Routes = [
  {
    path:'mapping-id/:id',
    component: DataMapperAppExampleHostComponent
  },
  {
    path:'',
    component: DataMapperAppExampleHostComponent
  }
];

export const  routerModuleForRoot = RouterModule.forRoot(routes);
@NgModule({
  imports: [
    routerModuleForRoot
  ],
  exports: [
    RouterModule
  ],
  declarations: []
})
export class AppRoutingModule { }
