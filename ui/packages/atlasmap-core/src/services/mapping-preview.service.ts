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
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import {
  IProcessMappingRequestContainer,
  IProcessMappingResponseContainer,
  PROCESS_MAPPING_REQUEST_JSON_TYPE,
} from '../contracts/mapping-preview';
import { Subject, Subscription } from 'rxjs';
import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';
import { MappingSerializer } from '../utils/mapping-serializer';
import ky from 'ky';

/**
 * Manages Mapping Preview.
 */
export class MappingPreviewService {
  cfg!: ConfigModel;

  mappingPreviewInputSource = new Subject<MappingModel>();
  mappingPreviewInput$ = this.mappingPreviewInputSource.asObservable();
  mappingPreviewOutputSource = new Subject<MappingModel>();
  mappingPreviewOutput$ = this.mappingPreviewOutputSource.asObservable();
  mappingPreviewErrorSource = new Subject<ErrorInfo[]>();
  mappingPreviewError$ = this.mappingPreviewErrorSource.asObservable();

  private mappingPreviewInputSubscription?: Subscription;
  private mappingUpdatedSubscription?: Subscription;

  constructor(private api: typeof ky) {}

  /**
   * Enable Mapping Preview.
   */
  enableMappingPreview(): void {
    if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      // process mapping service not configured.
      return;
    }
    this.cfg.showMappingPreview = true;
    this.mappingPreviewInputSubscription =
      this.createMappingPreviewSubscription();
    this.mappingUpdatedSubscription = this.createMappingUpdatedSubscription();
  }

  private createMappingPreviewSubscription(): Subscription {
    return this.mappingPreviewInput$.subscribe((inputFieldMapping) => {
      if (!inputFieldMapping || !inputFieldMapping.isFullyMapped()) {
        return;
      }
      let hasValue = false;
      for (const sourceField of inputFieldMapping.getFields(true)) {
        if (sourceField.value) {
          hasValue = true;
          break;
        }
      }
      if (!hasValue) {
        for (const targetField of inputFieldMapping.getFields(false)) {
          if (targetField.value) {
            hasValue = true;
            break;
          }
        }
      }
      if (!hasValue) {
        return;
      }
      const payload = this.createPreviewRequestBody(inputFieldMapping);
      this.cfg.logger!.debug(
        `Process Mapping Preview Request: ${JSON.stringify(payload)}`
      );
      const url: string =
        this.cfg.initCfg.baseMappingServiceUrl + 'mapping/process';
      this.api
        .put(url, { json: payload })
        .json<IProcessMappingResponseContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Process Mapping Preview Response: ${JSON.stringify(body)}`
          );
          this.processPreviewResponse(inputFieldMapping, body);
        })
        .catch((error: any) => {
          if (
            this.cfg.mappings &&
            this.cfg.mappings.activeMapping &&
            this.cfg.mappings.activeMapping === inputFieldMapping
          ) {
            this.cfg.errorService.addError(
              new ErrorInfo({
                message: error,
                level: ErrorLevel.ERROR,
                mapping: inputFieldMapping,
                scope: ErrorScope.MAPPING,
                type: ErrorType.PREVIEW,
              })
            );
          }
          this.mappingPreviewErrorSource.next([
            new ErrorInfo({ message: error, level: ErrorLevel.ERROR }),
          ]);
        });
    });
  }

  private createPreviewRequestBody(
    inputFieldMapping: MappingModel
  ): IProcessMappingRequestContainer {
    return {
      ProcessMappingRequest: {
        jsonType: PROCESS_MAPPING_REQUEST_JSON_TYPE,
        mapping: MappingSerializer.serializeFieldMapping(
          this.cfg,
          inputFieldMapping,
          'preview',
          false
        ),
      },
    };
  }

  private processPreviewResponse(
    inputFieldMapping: MappingModel,
    body: IProcessMappingResponseContainer
  ) {
    const answer = MappingSerializer.deserializeFieldMapping(
      body.ProcessMappingResponse.mapping,
      this.cfg
    );
    for (const toWrite of inputFieldMapping.targetFields) {
      for (const toRead of answer.targetFields) {
        // TODO: check these non null operator
        if (
          toWrite.field?.docDef?.id === toRead.field?.docDef.id &&
          toWrite.field?.path === toRead.field?.path
        ) {
          // TODO let field component subscribe mappingPreviewOutputSource instead of doing this
          // TODO: check this non null operator
          toWrite.field!.value = toRead.mappingField?.value!;
          const index = answer.targetFields.indexOf(toRead);
          if (index !== -1) {
            answer.targetFields.splice(index, 1);
            break;
          }
        }
      }
    }
    this.mappingPreviewOutputSource.next(answer);
    const audits = MappingSerializer.deserializeAudits(
      body.ProcessMappingResponse.audits,
      ErrorType.PREVIEW
    );
    // TODO: check this non null operator
    if (this.cfg.mappings!.activeMapping === inputFieldMapping) {
      audits.forEach((a) => (a.mapping = inputFieldMapping));
      this.cfg.errorService.addError(...audits);
    }
    this.mappingPreviewErrorSource.next(audits);
  }

  private createMappingUpdatedSubscription(): Subscription {
    return this.cfg.mappingService.mappingUpdated$.subscribe(() => {
      if (!this.cfg || !this.cfg.mappings || !this.cfg.mappings.activeMapping) {
        return;
      }
      if (this.cfg.mappings.activeMapping.isFullyMapped()) {
        this.mappingPreviewInputSource.next(this.cfg.mappings.activeMapping);
      }
    });
  }

  /**
   * On mapping preview disable, clear any preview values and unsubscribe from
   * both the mapping-updated and mapping-preview subscriptions.
   */
  disableMappingPreview(): void {
    let mappedValueCleared = false;
    this.cfg.showMappingPreview = false;

    // Clear any preview values on mapping preview disable.
    if (this.cfg.mappings?.activeMapping?.isFullyMapped()) {
      for (const mapping of this.cfg.mappings.getAllMappings(true)) {
        for (const mappedField of mapping.getAllFields()) {
          if (mappedField.value?.length > 0 && !mappedField.isConstant()) {
            mappedField.value = '';
            mappedValueCleared = true;
          }
        }
      }
    }
    if (mappedValueCleared) {
      this.cfg.mappingService.notifyMappingUpdated();
    }
    if (this.mappingUpdatedSubscription) {
      this.mappingUpdatedSubscription.unsubscribe();
      this.mappingUpdatedSubscription = undefined;
    }
    if (this.mappingPreviewInputSubscription) {
      this.mappingPreviewInputSubscription.unsubscribe();
      this.mappingPreviewInputSubscription = undefined;
    }
  }

  /**
   * Toggle Mapping Preview.
   * @param enabled
   */
  toggleMappingPreview(enabled: boolean) {
    if (enabled) {
      this.enableMappingPreview();
    } else {
      this.disableMappingPreview();
    }
    return enabled;
  }
}
