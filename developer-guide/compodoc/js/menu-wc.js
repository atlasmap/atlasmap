'use strict';


customElements.define('compodoc-menu', class extends HTMLElement {
    constructor() {
        super();
        this.isNormalMode = this.getAttribute('mode') === 'normal';
    }

    connectedCallback() {
        this.render(this.isNormalMode);
    }

    render(isNormalMode) {
        let tp = lithtml.html(`
        <nav>
            <ul class="list">
                <li class="title">
                    <a href="index.html" data-type="index-link">@atlasmap/atlasmap-data-mapper documentation</a>
                </li>

                <li class="divider"></li>
                ${ isNormalMode ? `<div id="book-search-input" role="search"><input type="text" placeholder="Type to search"></div>` : '' }
                <li class="chapter">
                    <a data-type="chapter-link" href="index.html"><span class="icon ion-ios-home"></span>Getting started</a>
                    <ul class="links">
                        <li class="link">
                            <a href="overview.html" data-type="chapter-link">
                                <span class="icon ion-ios-keypad"></span>Overview
                            </a>
                        </li>
                        <li class="link">
                            <a href="index.html" data-type="chapter-link">
                                <span class="icon ion-ios-paper"></span>README
                            </a>
                        </li>
                        <li class="link">
                            <a href="license.html"  data-type="chapter-link">
                                <span class="icon ion-ios-paper"></span>LICENSE
                            </a>
                        </li>
                                <li class="link">
                                    <a href="dependencies.html" data-type="chapter-link">
                                        <span class="icon ion-ios-list"></span>Dependencies
                                    </a>
                                </li>
                    </ul>
                </li>
                    <li class="chapter modules">
                        <a data-type="chapter-link" href="modules.html">
                            <div class="menu-toggler linked" data-toggle="collapse" ${ isNormalMode ?
                                'data-target="#modules-links"' : 'data-target="#xs-modules-links"' }>
                                <span class="icon ion-ios-archive"></span>
                                <span class="link-name">Modules</span>
                                <span class="icon ion-ios-arrow-down"></span>
                            </div>
                        </a>
                        <ul class="links collapse " ${ isNormalMode ? 'id="modules-links"' : 'id="xs-modules-links"' }>
                            <li class="link">
                                <a href="modules/DataMapperModule.html" data-type="entity-link">DataMapperModule</a>
                                    <li class="chapter inner">
                                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ?
                                            'data-target="#components-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' : 'data-target="#xs-components-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                            <span class="icon ion-md-cog"></span>
                                            <span>Components</span>
                                            <span class="icon ion-ios-arrow-down"></span>
                                        </div>
                                        <ul class="links collapse" ${ isNormalMode ? 'id="components-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' :
                                            'id="xs-components-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                            <li class="link">
                                                <a href="components/ClassNameComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ClassNameComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/CollapsableHeaderComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">CollapsableHeaderComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ConstantFieldEditComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ConstantFieldEditComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/DataMapperAppComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">DataMapperAppComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/DataMapperAppExampleHostComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">DataMapperAppExampleHostComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/DataMapperErrorComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">DataMapperErrorComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/DocumentDefinitionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">DocumentDefinitionComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/DocumentFieldDetailComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">DocumentFieldDetailComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/EmptyModalBodyComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">EmptyModalBodyComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ExpressionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ExpressionComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/FieldEditComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">FieldEditComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/LineMachineComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">LineMachineComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/LookupTableComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">LookupTableComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingDetailComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingDetailComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingFieldActionArgumentComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingFieldActionArgumentComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingFieldActionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingFieldActionComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingFieldContainerComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingFieldContainerComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingFieldDetailComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingFieldDetailComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingListComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingListComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingListFieldComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingListFieldComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingSelectionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingSelectionComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/MappingSelectionSectionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">MappingSelectionSectionComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ModalErrorDetailComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ModalErrorDetailComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ModalErrorWindowComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ModalErrorWindowComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ModalWindowComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ModalWindowComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/NamespaceEditComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">NamespaceEditComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/NamespaceListComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">NamespaceListComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/PropertyFieldEditComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">PropertyFieldEditComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/TemplateEditComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">TemplateEditComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/ToolbarComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ToolbarComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/TransitionSelectionComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">TransitionSelectionComponent</a>
                                            </li>
                                        </ul>
                                    </li>
                                <li class="chapter inner">
                                    <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ?
                                        'data-target="#directives-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' : 'data-target="#xs-directives-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                        <span class="icon ion-md-code-working"></span>
                                        <span>Directives</span>
                                        <span class="icon ion-ios-arrow-down"></span>
                                    </div>
                                    <ul class="links collapse" ${ isNormalMode ? 'id="directives-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' :
                                        'id="xs-directives-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                        <li class="link">
                                            <a href="directives/FocusDirective.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules">FocusDirective</a>
                                        </li>
                                    </ul>
                                </li>
                                <li class="chapter inner">
                                    <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ?
                                        'data-target="#injectables-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' : 'data-target="#xs-injectables-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                        <span class="icon ion-md-arrow-round-down"></span>
                                        <span>Injectables</span>
                                        <span class="icon ion-ios-arrow-down"></span>
                                    </div>
                                    <ul class="links collapse" ${ isNormalMode ? 'id="injectables-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' :
                                        'id="xs-injectables-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                        <li class="link">
                                            <a href="injectables/DocumentManagementService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>DocumentManagementService</a>
                                        </li>
                                        <li class="link">
                                            <a href="injectables/ErrorHandlerService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>ErrorHandlerService</a>
                                        </li>
                                        <li class="link">
                                            <a href="injectables/FieldActionService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>FieldActionService</a>
                                        </li>
                                        <li class="link">
                                            <a href="injectables/FileManagementService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>FileManagementService</a>
                                        </li>
                                        <li class="link">
                                            <a href="injectables/InitializationService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>InitializationService</a>
                                        </li>
                                        <li class="link">
                                            <a href="injectables/MappingManagementService.html"
                                                data-type="entity-link" data-context="sub-entity" data-context-id="modules" }>MappingManagementService</a>
                                        </li>
                                    </ul>
                                </li>
                                    <li class="chapter inner">
                                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ?
                                            'data-target="#pipes-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' : 'data-target="#xs-pipes-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                            <span class="icon ion-md-add"></span>
                                            <span>Pipes</span>
                                            <span class="icon ion-ios-arrow-down"></span>
                                        </div>
                                        <ul class="links collapse" ${ isNormalMode ? 'id="pipes-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' :
                                            'id="xs-pipes-links-module-DataMapperModule-808218972a6169a6e1484dba53d2e962"' }>
                                            <li class="link">
                                                <a href="pipes/ToErrorIconClassPipe.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">ToErrorIconClassPipe</a>
                                            </li>
                                        </ul>
                                    </li>
                            </li>
                            <li class="link">
                                <a href="modules/ExampleAppModule.html" data-type="entity-link">ExampleAppModule</a>
                                    <li class="chapter inner">
                                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ?
                                            'data-target="#components-links-module-ExampleAppModule-51693419d2e2e694844f4b37b7fc3144"' : 'data-target="#xs-components-links-module-ExampleAppModule-51693419d2e2e694844f4b37b7fc3144"' }>
                                            <span class="icon ion-md-cog"></span>
                                            <span>Components</span>
                                            <span class="icon ion-ios-arrow-down"></span>
                                        </div>
                                        <ul class="links collapse" ${ isNormalMode ? 'id="components-links-module-ExampleAppModule-51693419d2e2e694844f4b37b7fc3144"' :
                                            'id="xs-components-links-module-ExampleAppModule-51693419d2e2e694844f4b37b7fc3144"' }>
                                            <li class="link">
                                                <a href="components/AppComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">AppComponent</a>
                                            </li>
                                            <li class="link">
                                                <a href="components/AtlasmapNavbarComponent.html"
                                                    data-type="entity-link" data-context="sub-entity" data-context-id="modules">AtlasmapNavbarComponent</a>
                                            </li>
                                        </ul>
                                    </li>
                            </li>
                </ul>
                </li>
                    <li class="chapter">
                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ? 'data-target="#classes-links"' :
                            'data-target="#xs-classes-links"' }>
                            <span class="icon ion-ios-paper"></span>
                            <span>Classes</span>
                            <span class="icon ion-ios-arrow-down"></span>
                        </div>
                        <ul class="links collapse " ${ isNormalMode ? 'id="classes-links"' : 'id="xs-classes-links"' }>
                            <li class="link">
                                <a href="classes/AdmRedrawMappingLinesEvent.html" data-type="entity-link">AdmRedrawMappingLinesEvent</a>
                            </li>
                            <li class="link">
                                <a href="classes/ConfigModel.html" data-type="entity-link">ConfigModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/DataMapperInitializationModel.html" data-type="entity-link">DataMapperInitializationModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/DataMapperUtil.html" data-type="entity-link">DataMapperUtil</a>
                            </li>
                            <li class="link">
                                <a href="classes/DocumentDefinition.html" data-type="entity-link">DocumentDefinition</a>
                            </li>
                            <li class="link">
                                <a href="classes/DocumentInitializationModel.html" data-type="entity-link">DocumentInitializationModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/EnumValue.html" data-type="entity-link">EnumValue</a>
                            </li>
                            <li class="link">
                                <a href="classes/ErrorInfo.html" data-type="entity-link">ErrorInfo</a>
                            </li>
                            <li class="link">
                                <a href="classes/Examples.html" data-type="entity-link">Examples</a>
                            </li>
                            <li class="link">
                                <a href="classes/ExpressionModel.html" data-type="entity-link">ExpressionModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/ExpressionNode.html" data-type="entity-link">ExpressionNode</a>
                            </li>
                            <li class="link">
                                <a href="classes/ExpressionUpdatedEvent.html" data-type="entity-link">ExpressionUpdatedEvent</a>
                            </li>
                            <li class="link">
                                <a href="classes/Field.html" data-type="entity-link">Field</a>
                            </li>
                            <li class="link">
                                <a href="classes/FieldAction.html" data-type="entity-link">FieldAction</a>
                            </li>
                            <li class="link">
                                <a href="classes/FieldActionArgument.html" data-type="entity-link">FieldActionArgument</a>
                            </li>
                            <li class="link">
                                <a href="classes/FieldActionArgumentValue.html" data-type="entity-link">FieldActionArgumentValue</a>
                            </li>
                            <li class="link">
                                <a href="classes/FieldActionDefinition.html" data-type="entity-link">FieldActionDefinition</a>
                            </li>
                            <li class="link">
                                <a href="classes/FieldNode.html" data-type="entity-link">FieldNode</a>
                            </li>
                            <li class="link">
                                <a href="classes/LineModel.html" data-type="entity-link">LineModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/LookupTable.html" data-type="entity-link">LookupTable</a>
                            </li>
                            <li class="link">
                                <a href="classes/LookupTableData.html" data-type="entity-link">LookupTableData</a>
                            </li>
                            <li class="link">
                                <a href="classes/LookupTableEntry.html" data-type="entity-link">LookupTableEntry</a>
                            </li>
                            <li class="link">
                                <a href="classes/LookupTableUtil.html" data-type="entity-link">LookupTableUtil</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappedField.html" data-type="entity-link">MappedField</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappedFieldParsingData.html" data-type="entity-link">MappedFieldParsingData</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappingDefinition.html" data-type="entity-link">MappingDefinition</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappingModel.html" data-type="entity-link">MappingModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappingSerializer.html" data-type="entity-link">MappingSerializer</a>
                            </li>
                            <li class="link">
                                <a href="classes/MappingUtil.html" data-type="entity-link">MappingUtil</a>
                            </li>
                            <li class="link">
                                <a href="classes/NamespaceModel.html" data-type="entity-link">NamespaceModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/PaddingField.html" data-type="entity-link">PaddingField</a>
                            </li>
                            <li class="link">
                                <a href="classes/TextNode.html" data-type="entity-link">TextNode</a>
                            </li>
                            <li class="link">
                                <a href="classes/TransitionDelimiterModel.html" data-type="entity-link">TransitionDelimiterModel</a>
                            </li>
                            <li class="link">
                                <a href="classes/TransitionModel.html" data-type="entity-link">TransitionModel</a>
                            </li>
                        </ul>
                    </li>
                        <li class="chapter">
                            <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ? 'data-target="#injectables-links"' :
                                'data-target="#xs-injectables-links"' }>
                                <span class="icon ion-md-arrow-round-down"></span>
                                <span>Injectables</span>
                                <span class="icon ion-ios-arrow-down"></span>
                            </div>
                            <ul class="links collapse " ${ isNormalMode ? 'id="injectables-links"' : 'id="xs-injectables-links"' }>
                                <li class="link">
                                    <a href="injectables/ApiHttpXsrfTokenExtractor.html" data-type="entity-link">ApiHttpXsrfTokenExtractor</a>
                                </li>
                            </ul>
                        </li>
                    <li class="chapter">
                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ? 'data-target="#interceptors-links"' :
                            'data-target="#xs-interceptors-links"' }>
                            <span class="icon ion-ios-swap"></span>
                            <span>Interceptors</span>
                            <span class="icon ion-ios-arrow-down"></span>
                        </div>
                        <ul class="links collapse " ${ isNormalMode ? 'id="interceptors-links"' : 'id="xs-interceptors-links"' }>
                            <li class="link">
                                <a href="interceptors/ApiXsrfInterceptor.html" data-type="entity-link">ApiXsrfInterceptor</a>
                            </li>
                        </ul>
                    </li>
                    <li class="chapter">
                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ? 'data-target="#interfaces-links"' :
                            'data-target="#xs-interfaces-links"' }>
                            <span class="icon ion-md-information-circle-outline"></span>
                            <span>Interfaces</span>
                            <span class="icon ion-ios-arrow-down"></span>
                        </div>
                        <ul class="links collapse " ${ isNormalMode ? ' id="interfaces-links"' : 'id="xs-interfaces-links"' }>
                            <li class="link">
                                <a href="interfaces/ModalErrorWindowValidator.html" data-type="entity-link">ModalErrorWindowValidator</a>
                            </li>
                            <li class="link">
                                <a href="interfaces/ModalWindowValidator.html" data-type="entity-link">ModalWindowValidator</a>
                            </li>
                        </ul>
                    </li>
                    <li class="chapter">
                        <div class="simple menu-toggler" data-toggle="collapse" ${ isNormalMode ? 'data-target="#miscellaneous-links"'
                            : 'data-target="#xs-miscellaneous-links"' }>
                            <span class="icon ion-ios-cube"></span>
                            <span>Miscellaneous</span>
                            <span class="icon ion-ios-arrow-down"></span>
                        </div>
                        <ul class="links collapse " ${ isNormalMode ? 'id="miscellaneous-links"' : 'id="xs-miscellaneous-links"' }>
                            <li class="link">
                                <a href="miscellaneous/enumerations.html" data-type="entity-link">Enums</a>
                            </li>
                            <li class="link">
                                <a href="miscellaneous/variables.html" data-type="entity-link">Variables</a>
                            </li>
                        </ul>
                    </li>
                    <li class="chapter">
                        <a data-type="chapter-link" href="coverage.html"><span class="icon ion-ios-stats"></span>Documentation coverage</a>
                    </li>
                    <li class="divider"></li>
                    <li class="copyright">
                        Documentation generated using <a href="https://compodoc.app/" target="_blank">
                            <img data-src="images/compodoc-vectorise.png" class="img-responsive" data-type="compodoc-logo">
                        </a>
                    </li>
            </ul>
        </nav>
        `);
        this.innerHTML = tp.strings;
    }
});