import { Component, Input, OnInit } from '@angular/core';
import { inflate, gzip } from 'pako';
import { Subscription } from 'rxjs';

import { DocumentType, InspectionType } from './lib/atlasmap-data-mapper/common/config.types';
import { DataMapperUtil } from './lib/atlasmap-data-mapper/common/data-mapper-util';
import { ConfigModel } from './lib/atlasmap-data-mapper/models/config.model';
import { DocumentDefinition } from './lib/atlasmap-data-mapper/models/document-definition.model';
import { InitializationService } from './lib/atlasmap-data-mapper/services/initialization.service';
import { DocumentManagementService } from './lib/atlasmap-data-mapper/services/document-management.service';
import { MappingManagementService } from './lib/atlasmap-data-mapper/services/mapping-management.service';

@Component({
  selector: 'atlasmap-navbar',
  templateUrl: './atlasmap-navbar.component.html',
  styleUrls: ['./lib/atlasmap-data-mapper/components/data-mapper-app.component.css']
})

export class AtlasmapNavbarComponent implements OnInit {
  private SCHEMA_FILE_JAVA = 'userjavaschemafile';
  private SCHEMA_FILE_JSON = 'userjsonschemafile';
  private SCHEMA_FILE_XML = 'userxmlschemafile';
  private INSTANCE_SOURCES_PANEL = 'instance-sources-panel';
  private INSTANCE_TARGETS_PANEL = 'instance-targets-panel';
  private SCHEMA_SOURCES_PANEL = 'schema-sources-panel';
  private SCHEMA_TARGETS_PANEL = 'schema-targets-panel';

  cfg = ConfigModel.getConfig();

  private docSplitPrefix = '\x1e' + 'RH:';
  private fileData: any;
  private instanceDoc: boolean;
  private isSource: boolean;
  private mappingsFileName: string;
  private reader: FileReader;
  private schemaDoc: boolean;
  private saveMappingSubscription: Subscription;

  ngOnInit(): void {
    this.fileData = null;
    this.instanceDoc = false;
    this.isSource = false;
    this.mappingsFileName = '';
    this.reader = new FileReader();
    this.schemaDoc = false;
  }

  get instanceSourcesPanel(): string {
    return this.INSTANCE_SOURCES_PANEL;
  }

  get instanceTargetsPanel(): string {
    return this.INSTANCE_SOURCES_PANEL;
  }

  get schemaSourcesPanel(): string {
    return this.SCHEMA_SOURCES_PANEL;
  }

  get schemaTargetsPanel(): string {
    return this.SCHEMA_TARGETS_PANEL;
  }

  get getInstanceDoc(): boolean {
    return this.instanceDoc;
  }

  get getSchemaDoc(): boolean {
    return this.schemaDoc;
  }

  private clearPanelCheckbox(boxName: string) {
    const fields: any = document.getElementsByName(boxName);
    if (fields != null && fields.length > 0) {
      fields[0].checked = false;
    }
  }

  /**
   * Perform an asynchronous read of a local file, returning a byte array for the file content.
   *
   * @param fileContent
   */
  async readFile(fileContent: Blob): Promise<Uint8Array> {
    return new Promise<Uint8Array>((resolve, reject) => {
      this.reader.onload = (event: any) => {
        const arrayBuffer = this.reader.result;
        const bytes = new Uint8Array(arrayBuffer);
        resolve(bytes);
      };
      this.reader.readAsArrayBuffer(fileContent);
    });
  }

  /**
   * Schema import button click.  Disable initialization to trigger the loading icon.
   */
  processClick(loadingStatus: string) {
    this.cfg.initCfg.initialized = false;
    this.cfg.initializationService.updateLoadingStatus(loadingStatus);
  }

  /**
   * The user has specified (or we have defaulted) an AtlasMap mappings catalog file name into which the current
   * live mappings and support documents will be exported.
   *
   * @param event
   */
  handleMappingsInstanceName(event) {
    let filename = event.target.value;
    if (filename !== null || filename.length > 0) {

      // Tack on a .adm suffix if one wasn't already specified.
      if (filename.split('.').pop() !== 'adm') {
        filename = filename.concat('.adm');
      }
      this.mappingsFileName = filename;
    }
  }

  /**
   * The user has requested their current mappings be exported.  Use the mapping management
   * service to establish the file content and to push it down to the server.
   *
   * @param event
   */
  handleExportMapping(event) {
    this.cfg.mappingService.exportMappingsCatalog(this.mappingsFileName);
  }

  /**
   * Cancel button - Export > Current Mapping
   */
  handleExportMappingCancel(): void {
    this.mappingsFileName = '';
  }

  /* A user has selected a compressed mappings catalog file to be imported into the canvas.
   *
   * @param event
   */
  async processMappingsCatalog(event) {

    // Wait for the async read of the selected mappings doc to be completed.
    try {
      this.fileData = await this.readFile(new Blob([event.target.files[0]]));
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to import the specified data mappings file: ' +
        event.target.files[0].name + '\n' + error.message, error);
      return;
    }

    // Inflate the buffer and push it to the server.
    try {
      this.cfg.initializationService.processMappingsCatalog(this.fileData);
      window.location.reload(true);
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to decompress the aggregate mappings file: \n' + event.target.files[0].name +
       '\n' + error.message, error);
      return;
    }
  }

  /**
   * The user has selected a panel for inclusion of their instance or schema document.  Un-check the opposite
   * panel check box.
   *
   * @param selectedPanel
   */
  handlePanelClick(selectedPanel: string): void {
    if (selectedPanel === null) {
      return;
    }
    const selectedPanelName = selectedPanel.split('-');
    const importType = selectedPanelName[0];

    if (selectedPanelName[1] === 'sources') {
      this.clearPanelCheckbox(importType + '-targets-panel');
    } else {
      this.clearPanelCheckbox(importType + '-sources-panel');
    }

    if (selectedPanelName[0] === 'schema') {
      this.clearPanelCheckbox(this.INSTANCE_SOURCES_PANEL);
      this.clearPanelCheckbox(this.INSTANCE_TARGETS_PANEL);
      this.instanceDoc = false;
    } else {
      this.clearPanelCheckbox(this.SCHEMA_SOURCES_PANEL);
      this.clearPanelCheckbox(this.SCHEMA_TARGETS_PANEL);
      this.schemaDoc = false;
    }
  }

  /**
   * Read the selected instance file and call the initialization service to update the sources/ targets
   * in both the runtime and the UI.  The runtime will parse/ validate the file.
   *
   * @param event
   */
  async processInstanceDoc(event) {
    const selectedFile = event.target.files[0];
    this.cfg.documentService.processDocument(selectedFile, InspectionType.INSTANCE, this.isSource);
    this.instanceDoc = false;
  }

  /**
   * Process the instance sources/targets check box.
   *
   * @param event
   */
  handleInstanceDoc(event): void {
    this.instanceDoc = true;
    this.isSource = (event !== null && event.target.name === this.INSTANCE_SOURCES_PANEL);
  }

  /**
   * Cancel button - 'Instance Document' pull-down.
   */
  handleInstanceCancel() {
    this.instanceDoc = false;
    this.clearCheckboxes();
  }

  /**
   * Process the schema sources/targets check box.
   *
   * @param event
   */
  handleSchemaDoc(event): void {
    this.schemaDoc = true;
    this.isSource = (event !== null && event.target.name === this.SCHEMA_SOURCES_PANEL);
  }

  /**
   * Start out with clean instance/ schema check boxes.
   */
  clearCheckboxes(): void {
    let fields: any = document.getElementsByName(this.INSTANCE_SOURCES_PANEL);
    fields[0].checked = false;
    fields = document.getElementsByName(this.INSTANCE_TARGETS_PANEL);
    fields[0].checked = false;
    fields = document.getElementsByName(this.SCHEMA_SOURCES_PANEL);
    fields[0].checked = false;
    fields = document.getElementsByName(this.SCHEMA_TARGETS_PANEL);
    fields[0].checked = false;

    this.instanceDoc = false;
    this.schemaDoc = false;
  }

  /**
   * Cancel button - 'Schema Document' pull-down.
   */
  handleSchemaCancel() {
    this.schemaDoc = false;
    this.clearCheckboxes();
  }

  /**
   * Read the selected schema file from the specified selection event and call the document service
   * to update the sources/ targets in both the runtime and the UI.  The runtime will parse/ validate the
   * file.
   *
   * @param event
   */
  async processSchemaDoc(event) {
    const selectedFile = event.target.files[0];
    this.cfg.documentService.processDocument(selectedFile, InspectionType.SCHEMA, this.isSource);
    this.schemaDoc = false;
  }

  /**
   * Return a string representing valid suffixes for importable schema and schema-instance documents.
   */
  getFileSuffix() {
    return '.json,.xml,.xsd';
  }
}
