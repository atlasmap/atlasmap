/* SystemJS module definition */
declare var module: NodeModule;
interface NodeModule {
  id: string;
}
/*
interface JQuery<TElement = HTMLElement> extends Iterable<TElement> {
   combobox( option: any ) : any;
}
*/
interface JQuery {
  combobox( option: any ) : any;
}