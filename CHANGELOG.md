# Changelog

## atlasmap-1.39.4 (27/02/2019)

#### Enhancements

- [#96](https://github.com/atlasmap/atlasmap/issues/96) Provide display name for transformation parameters

#### Bug Fixes

- [#772](https://github.com/atlasmap/atlasmap/issues/772) Tooltip for Source in Mapping Table is cropped
- [#771](https://github.com/atlasmap/atlasmap/issues/771) CNFE: io.atlasmap.api.AtlasFieldAction when uploading library
- [#768](https://github.com/atlasmap/atlasmap/issues/768) Reset All is not resetting Constants
- [#767](https://github.com/atlasmap/atlasmap/issues/767) Wrong name for the constant to remove - first letter uppercased
- [#761](https://github.com/atlasmap/atlasmap/issues/761) Source panel lines don't align correctly w/ multiple ADM import
- [#759](https://github.com/atlasmap/atlasmap/issues/759) Topmost collection to collection mapping not working as expected
- [#755](https://github.com/atlasmap/atlasmap/issues/755) Support specifying ADM file path via Java system property at backend startup
- [#735](https://github.com/atlasmap/atlasmap/issues/735) Test variation of #729

---

## atlasmap-1.39.3 (13/02/2019)

#### Bug Fixes

- [#748](https://github.com/atlasmap/atlasmap/issues/748) VSCode - import of an ADM does not show mappings (will show them on restart)

---

## atlasmap-1.39.2 (06/02/2019)
*No changelog for this release.*

---

## atlasmap-1.39.1 (04/02/2019)

#### Enhancements

- [#712](https://github.com/atlasmap/atlasmap/issues/712) [camel-atlasmap] support adm file

#### Bug Fixes

- [#729](https://github.com/atlasmap/atlasmap/issues/729) [camel-atlasmap] AtlasEndpoint.populateSourceDocuments() forces body object wrapped with Map

---

## atlasmap-1.39.0 (15/01/2019)

#### Enhancements

- [#696](https://github.com/atlasmap/atlasmap/issues/696) Long processing of xsds
- [#690](https://github.com/atlasmap/atlasmap/issues/690) Support multiple source Documents via java.util.Map IN message body
- [#686](https://github.com/atlasmap/atlasmap/issues/686) Add an upper limit of field search results
- [#685](https://github.com/atlasmap/atlasmap/issues/685) Add delay on field search
- [#678](https://github.com/atlasmap/atlasmap/issues/678) Add typeahead delay to main panel search
- [#670](https://github.com/atlasmap/atlasmap/issues/670) source-side transformations depends on target-type
- [#651](https://github.com/atlasmap/atlasmap/issues/651) Save imported schemas to runtime on import
- [#644](https://github.com/atlasmap/atlasmap/issues/644) Support delete source/target document in the UI
- [#561](https://github.com/atlasmap/atlasmap/issues/561) Support importing Java classes/jars
- [#557](https://github.com/atlasmap/atlasmap/issues/557) Create all-in-one spring-boot image
- [#474](https://github.com/atlasmap/atlasmap/issues/474) Support activating mapping by clicking line
- [#451](https://github.com/atlasmap/atlasmap/issues/451) Support data preview - single mapping entry
- [#248](https://github.com/atlasmap/atlasmap/issues/248) Add support to enable field actions where appropriate in the UI
- [#29](https://github.com/atlasmap/atlasmap/issues/29) Update ClassInspection service to consider user-provided jars
- [#27](https://github.com/atlasmap/atlasmap/issues/27) [Design] AtlasGroupStrategy

#### Bug Fixes

- [#711](https://github.com/atlasmap/atlasmap/issues/711) Avoid swallowing exception in mapping-management.service.ts
- [#704](https://github.com/atlasmap/atlasmap/issues/704) Prepend/Append : NoSuchMethodException:StringComplexFieldActions.append(io.atlasmap.v2.Action, java.lang.String)
- [#681](https://github.com/atlasmap/atlasmap/issues/681) Some of object transformations should have source collection type NONE
- [#679](https://github.com/atlasmap/atlasmap/issues/679) UI Initialization Error: Could not load mapping definitions: 200 OK
- [#667](https://github.com/atlasmap/atlasmap/issues/667) 'null' when using "ItemAt" transformation from List<> -> Number 
- [#663](https://github.com/atlasmap/atlasmap/issues/663) indexOf and lastIndexOf should have INTEGER as a targetType
- [#662](https://github.com/atlasmap/atlasmap/issues/662) Validation should look at the input/output of transformations
- [#661](https://github.com/atlasmap/atlasmap/issues/661) IndexOf and LastIndexOf are possible only as target transformation

---

## atlasmap-1.38.1 (26/10/2018)

#### Enhancements

- [#85](https://github.com/atlasmap/atlasmap/issues/85) Combine mapped icons with transformation icon
- [#30](https://github.com/atlasmap/atlasmap/issues/30) Feature: Support indexed source items in mapping definition

#### Bug Fixes

- [#657](https://github.com/atlasmap/atlasmap/issues/657) Concatenate transformation shows up for non-collection source field
- [#653](https://github.com/atlasmap/atlasmap/issues/653) Skip preview execution until at least one non-empty value presents
- [#652](https://github.com/atlasmap/atlasmap/issues/652) Ignore null value for COMBINE mode
- [#647](https://github.com/atlasmap/atlasmap/issues/647) Import mapping > cancel causes infinite loading
- [#641](https://github.com/atlasmap/atlasmap/issues/641) line machine doesn't realign in complex elements after reload
- [#633](https://github.com/atlasmap/atlasmap/issues/633) Creating a constant in Data Mapper briefly shows an error message that the constant already exists
- [#632](https://github.com/atlasmap/atlasmap/issues/632) Can't change export file name
- [#631](https://github.com/atlasmap/atlasmap/issues/631) Line is not drawn for Constants in UI after reload
- [#624](https://github.com/atlasmap/atlasmap/issues/624) mapping table: consider field order in separate/combine by index rather than alphabetically 
- [#622](https://github.com/atlasmap/atlasmap/issues/622) Mappings previews displayed incorrectly in mapping table 
- [#621](https://github.com/atlasmap/atlasmap/issues/621) UI freezes after showing previews in mapping table
- [#601](https://github.com/atlasmap/atlasmap/issues/601) Padding field is shown in table view?
- [#581](https://github.com/atlasmap/atlasmap/issues/581) An array is missing in JSON instance inspection 
- [#566](https://github.com/atlasmap/atlasmap/issues/566) Unable to add Transformation on source when source type is collection 
- [#565](https://github.com/atlasmap/atlasmap/issues/565) Line machine is broken with data preview enabled
- [#504](https://github.com/atlasmap/atlasmap/issues/504) Drag and drop doesn't work in Firefox 
- [#385](https://github.com/atlasmap/atlasmap/issues/385) DayOf{Month|Week|Year} transformation doesn't show up for Date type source field

---

## atlasmap-1.38.0 (12/10/2018)

#### Enhancements

- [#609](https://github.com/atlasmap/atlasmap/issues/609) Apply transformation for each items in collection if it doesn't consume collection
- [#556](https://github.com/atlasmap/atlasmap/issues/556) Add `itemAt` Transformation
- [#550](https://github.com/atlasmap/atlasmap/issues/550) Handle FieldGroup contributed via mapping definition
- [#292](https://github.com/atlasmap/atlasmap/issues/292) [UI] Support uploading document schema
- [#77](https://github.com/atlasmap/atlasmap/issues/77) when mapped/unmapped fields are hidden show user a warning when none show

#### Bug Fixes

- [#623](https://github.com/atlasmap/atlasmap/issues/623) Mapping preview is empty for gaps in mapping table
- [#612](https://github.com/atlasmap/atlasmap/issues/612) CircleCI build fails with VM terminated error
- [#607](https://github.com/atlasmap/atlasmap/issues/607) Add Transformation link doesn't work if target field is not selected
- [#606](https://github.com/atlasmap/atlasmap/issues/606) Enable source side transformations before a target is set
- [#600](https://github.com/atlasmap/atlasmap/issues/600) Increase heap size for DM runtime and extend http timeout
- [#588](https://github.com/atlasmap/atlasmap/issues/588) Class inspection can't inspect nested class

---

## atlasmap-1.37.0 (02/10/2018)

#### Bug Fixes

- [#592](https://github.com/atlasmap/atlasmap/issues/592) Add Transformation doesn't work when index:1 is padding field
- [#582](https://github.com/atlasmap/atlasmap/issues/582) Build error in demo app

---

## atlasmap-1.35.8 (21/09/2018)

#### Enhancements

- [#558](https://github.com/atlasmap/atlasmap/issues/558) Support importing Document definition in standalone UI
- [#549](https://github.com/atlasmap/atlasmap/issues/549) Add combine and separate FieldAction
- [#543](https://github.com/atlasmap/atlasmap/issues/543) Create initial design for standalone only part in UI
- [#377](https://github.com/atlasmap/atlasmap/issues/377) Support exporting mapping definition file in standalone UI
- [#338](https://github.com/atlasmap/atlasmap/issues/338) Allow field actions to receive whole collection

#### Bug Fixes

- [#573](https://github.com/atlasmap/atlasmap/issues/573) StackOverflowError when processing FHIR xsd
- [#572](https://github.com/atlasmap/atlasmap/issues/572) atlasmap window become inactive after add constant/property
- [#563](https://github.com/atlasmap/atlasmap/issues/563) UI freezes with data preview enabled in table view

---

## atlasmap-1.36.0 (30/08/2018)

#### Enhancements

- [#483](https://github.com/atlasmap/atlasmap/issues/483) Support receiving FieldActions metadata from outside
- [#482](https://github.com/atlasmap/atlasmap/issues/482) Create GenerateFieldActionsMojo

#### Bug Fixes

- [#544](https://github.com/atlasmap/atlasmap/issues/544) Constants value is not saved into mapping definition
- [#537](https://github.com/atlasmap/atlasmap/issues/537) Disable CustomAction parameter

---

## atlasmap-1.35.7 (01/08/2018)

#### Enhancements

- [#485](https://github.com/atlasmap/atlasmap/issues/485) Apply UXD outcome for data preview - single entry
- [#375](https://github.com/atlasmap/atlasmap/issues/375) Improve error diagnostic if a user attempts multiple mappings (map mode) to the same target element

#### Bug Fixes

- [#521](https://github.com/atlasmap/atlasmap/issues/521) Drag & drop of padding field can confuse index
- [#515](https://github.com/atlasmap/atlasmap/issues/515) Prepend FieldAction doesn't show up for STRING target field
- [#491](https://github.com/atlasmap/atlasmap/issues/491) Error notification is not  automatically hidden

---

## atlasmap-1.35.6 (24/07/2018)

#### Bug Fixes

- [#505](https://github.com/atlasmap/atlasmap/issues/505) change 'Use ctrl-M1 to select multiple elements for 'Combine' or 'Separate' actions..' when running on MacOS
- [#499](https://github.com/atlasmap/atlasmap/issues/499) Reset the "Mapping Detail" panel, when a user delete a mapping in look up mode

---

## atlasmap-1.35.5 (19/07/2018)

#### Enhancements

- [#455](https://github.com/atlasmap/atlasmap/issues/455) Create UX design for data preview - single entry
- [#454](https://github.com/atlasmap/atlasmap/issues/454) [ui] Support data preview for single mapping entry
- [#278](https://github.com/atlasmap/atlasmap/issues/278) Enable source side transformation
- [#46](https://github.com/atlasmap/atlasmap/issues/46) Add TemplateCombineStrategy

#### Bug Fixes

- [#513](https://github.com/atlasmap/atlasmap/issues/513) Remove transformation button help causes page flickering
- [#500](https://github.com/atlasmap/atlasmap/issues/500) Add "Drag and drop" action to change the index in separate mode or the tooltip need to be removed
- [#320](https://github.com/atlasmap/atlasmap/issues/320) TypeConverter/FieldAction method resolver should fallback with respecting Java inheritance

---

## atlasmap-1.35.4 (28/06/2018)
*No changelog for this release.*

---

## atlasmap-1.35.3 (28/06/2018)
*No changelog for this release.*

---

## atlasmap-1.35.2 (28/06/2018)

#### Bug Fixes

- [#479](https://github.com/atlasmap/atlasmap/issues/479) Check source field value type in processPreview()

---

## atlasmap-1.35.1 (28/06/2018)

#### Enhancements

- [#476](https://github.com/atlasmap/atlasmap/issues/476) Enable mapping preview also in table view
- [#460](https://github.com/atlasmap/atlasmap/issues/460) Need version attribute to be stored in mapping files
- [#459](https://github.com/atlasmap/atlasmap/issues/459) Add trash link to the new source/target card as well
- [#453](https://github.com/atlasmap/atlasmap/issues/453) [runtime] Support process single mapping in preview mode
- [#449](https://github.com/atlasmap/atlasmap/issues/449) Account for gaps when mapping components in separate or combine modes.

#### Bug Fixes

- [#472](https://github.com/atlasmap/atlasmap/issues/472) [ui][separate/combine] Weird '[object Object]' list will appear when '1' or '2' is inserted into index input  
- [#463](https://github.com/atlasmap/atlasmap/issues/463) NPE from DocumentJavaFieldReader.read() is thrown up to client
- [#462](https://github.com/atlasmap/atlasmap/issues/462) Can't add transformation for collection mapping
- [#457](https://github.com/atlasmap/atlasmap/issues/457) Scope CSS selectors

---

## atlasmap-1.35.0 (31/05/2018)

#### Enhancements

- [#444](https://github.com/atlasmap/atlasmap/issues/444) Support interim results of transformations
- [#441](https://github.com/atlasmap/atlasmap/issues/441) Support data preview in UI
- [#440](https://github.com/atlasmap/atlasmap/issues/440) Multiple source selection should cause mapping to be a combine
- [#436](https://github.com/atlasmap/atlasmap/issues/436) Provide a button to trigger an auto-mapping of fields
- [#423](https://github.com/atlasmap/atlasmap/issues/423) Add auto action mode change to drag&drop support with combine/ separate modes.
- [#83](https://github.com/atlasmap/atlasmap/issues/83) Would it be better to have explicit mapping buttons? 

#### Bug Fixes

- [#445](https://github.com/atlasmap/atlasmap/issues/445) CCE on collection mapping between different Document format
- [#433](https://github.com/atlasmap/atlasmap/issues/433) [1.34.x] fromUnit toUnit isn't saved to mapping.xml when using ConvertVolumeUnits
- [#431](https://github.com/atlasmap/atlasmap/issues/431) UI needs to store collectionClassName somewhere in the mapping definition
- [#430](https://github.com/atlasmap/atlasmap/issues/430) CollectionType.LIST handling is hardcoded to use LinkedList
- [#422](https://github.com/atlasmap/atlasmap/issues/422) On OSX control-click means right click
- [#395](https://github.com/atlasmap/atlasmap/issues/395) Implement XSRF header handling with spring-boot adapter
- [#235](https://github.com/atlasmap/atlasmap/issues/235) "Collection<T>" field is interpreted as Object instead of List in UI 
- [#111](https://github.com/atlasmap/atlasmap/issues/111) Subscription to the fetchMappings operation is left open

---

## atlasmap-1.34.4 (04/05/2018)

#### Bug Fixes

- [#415](https://github.com/atlasmap/atlasmap/issues/415) "null" value during mapping to JSON number type 

---

## atlasmap-1.34.3 (02/05/2018)

#### Bug Fixes

- [#408](https://github.com/atlasmap/atlasmap/issues/408) String > int/long converter fails with decimal expression
- [#398](https://github.com/atlasmap/atlasmap/issues/398) Clean up unit types for mass/ distance/ area/ volume.
- [#386](https://github.com/atlasmap/atlasmap/issues/386) Consider using dropdown's for specifying units in ConvertUnits transformation 

---

## atlasmap-1.34.2 (20/04/2018)

#### Enhancements

- [#376](https://github.com/atlasmap/atlasmap/issues/376) Support uploading Document specification in standalone UI

#### Bug Fixes

- [#394](https://github.com/atlasmap/atlasmap/issues/394) [UI] Introduce http interceptor with an ability to send out XSRF token
- [#393](https://github.com/atlasmap/atlasmap/issues/393) Incorrect Transformation options can be selected in UI for some mappings
- [#382](https://github.com/atlasmap/atlasmap/issues/382) [regression] Index of last target field is lost after separator change 
- [#381](https://github.com/atlasmap/atlasmap/issues/381) Transformations with "ANY_DATE" aren't working 
- [#374](https://github.com/atlasmap/atlasmap/issues/374) Correct auto-index sequencing when in combine or separate mode.
- [#368](https://github.com/atlasmap/atlasmap/issues/368) [NumberFieldActions] java.lang.NoSuchMethodException during Collection<Number> -> Number mapping field actions
- [#364](https://github.com/atlasmap/atlasmap/issues/364) "Add Transformation" button is missing when Date -> Date mapping is created
- [#140](https://github.com/atlasmap/atlasmap/issues/140) Separate doesn't allow adding more target fields by clicking
- [#95](https://github.com/atlasmap/atlasmap/issues/95) Combine - Clicked source field should end up being in current active mapping

---

## atlasmap-1.34.1 (05/04/2018)

#### Bug Fixes

- [#348](https://github.com/atlasmap/atlasmap/issues/348) Allow selecting root element for XML Schema document

---

## atlasmap-1.34.0 (29/03/2018)

#### Bug Fixes

- [#357](https://github.com/atlasmap/atlasmap/issues/357) Add ctrl-M1 click support
- [#354](https://github.com/atlasmap/atlasmap/issues/354) Constants are not preserved across atlasmap invocations
- [#344](https://github.com/atlasmap/atlasmap/issues/344) UI should not allow duplicate mapping against same target field
- [#265](https://github.com/atlasmap/atlasmap/issues/265) XmlModule should check docId when retrieve namespaces from DataSource
- [#205](https://github.com/atlasmap/atlasmap/issues/205) Fix date/time related runtime code so fields show up correctly in UI
- [#104](https://github.com/atlasmap/atlasmap/issues/104) [Datamapper] Adding property with existing name seems allowed but nothing happens
- [#91](https://github.com/atlasmap/atlasmap/issues/91) The red cross on the right side jumps around randomly when dismissing the error message

---

## atlasmap-1.33.6 (12/03/2018)

#### Enhancements

- [#321](https://github.com/atlasmap/atlasmap/issues/321) Add missing date/time <-> primitives converters
- [#319](https://github.com/atlasmap/atlasmap/issues/319) Add BigInteger/BigDecimal type converters
- [#114](https://github.com/atlasmap/atlasmap/issues/114) Implement OOTB TypeConverters for NUMBER fieldType
- [#76](https://github.com/atlasmap/atlasmap/issues/76) Add support for runtime properties as a source. 
- [#74](https://github.com/atlasmap/atlasmap/issues/74) limit new constant/property/field value types

#### Bug Fixes

- [#335](https://github.com/atlasmap/atlasmap/issues/335) Regression: Class '[JAVA_PRIMITIVE]' for field is not found on the classpath
- [#332](https://github.com/atlasmap/atlasmap/issues/332) Disable "Add Field" on JSON/XML doc
- [#329](https://github.com/atlasmap/atlasmap/issues/329) Disable "Add Template" icon on toolbar
- [#324](https://github.com/atlasmap/atlasmap/issues/324) Cannot read property 'nativeElement' of undefined
- [#314](https://github.com/atlasmap/atlasmap/issues/314) Bad error message when source document is null
- [#312](https://github.com/atlasmap/atlasmap/issues/312) Use java.util.Date as a DATE_TIME representative class
- [#311](https://github.com/atlasmap/atlasmap/issues/311) DateConverter should not use FieldType.COMPLEX
- [#304](https://github.com/atlasmap/atlasmap/issues/304) ExpressionChangedAfterItHasBeenCheckedError appears on startup
- [#279](https://github.com/atlasmap/atlasmap/issues/279) unable to create repeating mapping with "Add new mapping" 
- [#273](https://github.com/atlasmap/atlasmap/issues/273) Null value in target during repeating mappings
- [#271](https://github.com/atlasmap/atlasmap/issues/271) Ignore null source document
- [#268](https://github.com/atlasmap/atlasmap/issues/268) Constant value must respect field type
- [#263](https://github.com/atlasmap/atlasmap/issues/263) Unable to convert various date types (Date -> ZonedDateTime,java.sql.Date etc.)
- [#234](https://github.com/atlasmap/atlasmap/issues/234) Change separator from UI during combine/separate doesn't have any effect 
- [#233](https://github.com/atlasmap/atlasmap/issues/233) Converter not found for sourceType: INTEGER targetType: NUMBER during Number-related transformation
- [#210](https://github.com/atlasmap/atlasmap/issues/210) Do not prohibit collection when non-collection is already clicked on the other side
- [#51](https://github.com/atlasmap/atlasmap/issues/51) Make boolean<->number conversion consistent

---

## atlasmap-1.33.5 (22/02/2018)

#### Enhancements

- [#291](https://github.com/atlasmap/atlasmap/issues/291) Set the Content-Type message header

#### Bug Fixes

- [#297](https://github.com/atlasmap/atlasmap/issues/297) Better initialization error handling
- [#290](https://github.com/atlasmap/atlasmap/issues/290) Qualified namespace handling
- [#88](https://github.com/atlasmap/atlasmap/issues/88) JSON schema source/target is shown as XML

---

## atlasmap-1.33.4 (16/02/2018)

#### Bug Fixes

- [#269](https://github.com/atlasmap/atlasmap/issues/269) WARN  i.a.c.DefaultAtlasConversionService - Converter ... exists.

---

## atlasmap-1.33.3 (14/02/2018)
*No changelog for this release.*

---

## atlasmap-1.33.2 (13/02/2018)
*No changelog for this release.*

---

## atlasmap-1.33.1 (08/02/2018)

#### Enhancements

- [#266](https://github.com/atlasmap/atlasmap/issues/266) Support multiple namespaces on a XML Document

#### Bug Fixes

- [#108](https://github.com/atlasmap/atlasmap/issues/108) Invalid FieldType 'LIST<STRING>' causes save failure in design runtime
- [#6](https://github.com/atlasmap/atlasmap/issues/6) TypeConverter other than for primitives are not supported yet

---

## atlasmap-1.33.0 (30/01/2018)

#### Enhancements

- [#242](https://github.com/atlasmap/atlasmap/issues/242) Update atlasmap config object to accept step IDs, labels, etc
- [#241](https://github.com/atlasmap/atlasmap/issues/241) Support multiple documents in the Message map held as an Exchange property
- [#69](https://github.com/atlasmap/atlasmap/issues/69) Add atlasmap-ui to Jenkins build
- [#64](https://github.com/atlasmap/atlasmap/issues/64) Support multiple source/target Document

#### Bug Fixes

- [#229](https://github.com/atlasmap/atlasmap/issues/229) Default value of input fields is set to '0' in all Transformation attributes  
- [#226](https://github.com/atlasmap/atlasmap/issues/226) Start/End Indexes translated as negative values when using substring before/after transformation
- [#225](https://github.com/atlasmap/atlasmap/issues/225) No field actions for Boolean, Short, Date 
- [#141](https://github.com/atlasmap/atlasmap/issues/141) XmlFieldReader test fails with the method really in use
- [#110](https://github.com/atlasmap/atlasmap/issues/110) Values for Transformation Replace parameters are ignored
- [#107](https://github.com/atlasmap/atlasmap/issues/107) NUMBER > NUMBER mapping doesn't even show an "Add Transformation" button
- [#106](https://github.com/atlasmap/atlasmap/issues/106) Some entries cannot be selected and have an "expandable" arrow which has no effect
- [#103](https://github.com/atlasmap/atlasmap/issues/103) Table view shows wrong icon for the validation error
- [#99](https://github.com/atlasmap/atlasmap/issues/99) No focus on text field when search is selected
- [#93](https://github.com/atlasmap/atlasmap/issues/93) Don't set '0' as default value for "Replace" transformation
- [#81](https://github.com/atlasmap/atlasmap/issues/81) Search and Autocomplete are broken
- [#14](https://github.com/atlasmap/atlasmap/issues/14) AtlasService.converterCheck() contains incomplete code
- [#12](https://github.com/atlasmap/atlasmap/issues/12) Update Java, Xml and Json modules to not create validations for core field types

---

## atlasmap-1.32.2 (02/01/2018)

#### Enhancements

- [#175](https://github.com/atlasmap/atlasmap/issues/175) Implement Date-related p0 field actions
- [#55](https://github.com/atlasmap/atlasmap/issues/55) Implement field actions (p0)
- [#52](https://github.com/atlasmap/atlasmap/issues/52) Support append and prepend of strings
- [#32](https://github.com/atlasmap/atlasmap/issues/32) [Performance] JSON reader / writer should cache the JSON document (and maybe XML, too)

#### Bug Fixes

- [#207](https://github.com/atlasmap/atlasmap/issues/207) AtlasEndpoint : There's no source document with docId...
- [#197](https://github.com/atlasmap/atlasmap/issues/197) UX Review - "Add new mapping" workflow
- [#97](https://github.com/atlasmap/atlasmap/issues/97) datamapper buttons are links
- [#94](https://github.com/atlasmap/atlasmap/issues/94) Make [None] disappear on focus
- [#87](https://github.com/atlasmap/atlasmap/issues/87) Error fetching validation data when removing last mapping

---

## 1.32.1 (14/12/2017)

#### Enhancements

- [#157](https://github.com/atlasmap/atlasmap/issues/157) Implement Object-related p0 field actions
- [#153](https://github.com/atlasmap/atlasmap/issues/153) Implement String-related p0 field actions
- [#151](https://github.com/atlasmap/atlasmap/issues/151) Implement Number-related p0 field actions
- [#134](https://github.com/atlasmap/atlasmap/issues/134) Support multiple source/target Document in camel-atlasmap
- [#133](https://github.com/atlasmap/atlasmap/issues/133) Support multiple target Document in UI
- [#132](https://github.com/atlasmap/atlasmap/issues/132) Support multiple source/target Document

#### Bug Fixes

- [#176](https://github.com/atlasmap/atlasmap/issues/176) ClassInspectionService is returning NOT_FOUND status for the COMPLEX fields of twitter4j.Status
- [#172](https://github.com/atlasmap/atlasmap/issues/172) atlasmap-maven-plugin: Can't inspect COMPLEX fields
- [#165](https://github.com/atlasmap/atlasmap/issues/165) UI puts invalid uri for Java DataSource
- [#129](https://github.com/atlasmap/atlasmap/issues/129) build.sh --skip-image-builds doesn't work
- [#44](https://github.com/atlasmap/atlasmap/issues/44) DefaultAtlasContext.process() should invoke common validation
- [#8](https://github.com/atlasmap/atlasmap/issues/8) Enable output field action or remove completely
- [#7](https://github.com/atlasmap/atlasmap/issues/7) Update BYTE conversion behavior
