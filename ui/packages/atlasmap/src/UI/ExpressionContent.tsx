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
  Button,
  Form,
  FormGroup,
  InputGroup,
  Tooltip,
} from '@patternfly/react-core';
import { EnumValue, useToggle } from '../impl/utils';
import {
  Environment,
  IKeyboardEvent,
  KeyCode,
  KeyMod,
  Position,
  Range,
  Token,
  editor,
  languages,
} from 'monaco-editor';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';
import { ExpressionEnumSelect } from './ExpressionEnumSelect';
import { ExpressionFieldSearch } from './ExpressionFieldSearch';
import { atlasmapLanguageID } from '@atlasmap/core';
import { css } from '@patternfly/react-styles';
import monacoEditorStyle from './ExpressionContent.module.css';

// Establish a Monaco environment.
declare global {
  interface Window {
    MonacoEnvironment: Environment;
  }
}
window.MonacoEnvironment = {
  // TODO: Need to provide a worker function to avoid console warning:
  // You must define a function MonacoEnvironment.getWorkerUrl or MonacoEnvironment.getWorker
  getWorker: function (_workerId: string, _label: string): Worker {
    return null as unknown as Worker;
  },
};

let insertPosition: Position | null = null;
let enumCandidates: EnumValue[] = [];
let mappedFieldCandidates: string[][] = [];
let searchFilter = '';
let searchMode = false;
let selectedNodeId: string = '';
let mappingExprInit: () => void;

export interface IExpressionContentProps {
  executeFieldSearch: (searchFilter: string, isSource: boolean) => string[][];
  getFieldEnums: (nodeId: string) => EnumValue[];
  mappingExpressionAddField: (
    selectedDocId: string,
    selectedField: string,
    position: Position | null,
  ) => void;
  isMappingExpressionEmpty: boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (str: string) => void;
  mappingExpressionRemoveField: (idPosition?: Position) => void;
  mappingExpression?: string;
  disabled: boolean;
  onToggle: () => void;
  setSelectedEnumValue: (
    selectedEnum: string,
    selectedEnumValueIndex: number,
  ) => void;
  getAtlasmapLanguage: () =>
    | (languages.ILanguageExtensionPoint & {
        [key: string]: any;
      })
    | undefined;
}

export const ExpressionContent: FunctionComponent<IExpressionContentProps> = ({
  executeFieldSearch,
  getFieldEnums,
  mappingExpressionAddField,
  isMappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionRemoveField,
  mappingExpression,
  disabled,
  onToggle,
  setSelectedEnumValue,
  getAtlasmapLanguage,
}) => {
  const [editorInitPhase, setEditorInitPhase] = useState(false);
  const [insertField, setInsertField] = useState<boolean>();
  const [insertedField, setInsertedField] = useState<boolean>(false);

  let addFieldToExpression: (
    selectedDocId: string,
    selectedField: string,
    position: Position | null,
  ) => void;
  let getEnums: (enumFieldName: string) => EnumValue[];
  let setSelEnumValue: (
    selectedEnumNodeId: string,
    selectedEnumValueIndex: number,
  ) => void;

  const {
    state: showEnumSelect,
    toggleOn: toggleEnumSelOn,
    toggleOff: toggleEnumSelOff,
  } = useToggle(false);

  let [condExprEditor, setCondExprEditor] = useState<
    editor.IStandaloneCodeEditor | undefined
  >();

  getEnums = getFieldEnums;
  setSelEnumValue = setSelectedEnumValue;

  /**
   * An enumeration value has been selected for the specified selected field node ID.
   *
   * @param selectedEnumNodeId - selected field node ID
   * @param selectedEnumValueIndex - selected enumeration index
   */
  function onEnumSelect(
    selectedEnumNodeId: string,
    selectedEnumValueIndex: number,
  ) {
    setSelEnumValue(selectedEnumNodeId, selectedEnumValueIndex);
    clearEnumSelect();
  }

  /**
   * The user has selected a field from the search select menu.  Extract
   * the field name and the scope if it is present.
   *
   * @param selectedDocId
   * @param selectedField
   * @returns
   */
  function insertSelectedField(
    selectedDocId: string,
    selectedField: string,
  ): void {
    addFieldToExpression(selectedDocId, selectedField, insertPosition);
    clearSearchMode();
    condExprEditor!.focus();
    setInsertedField(true);
  }

  /**
   * Clear elements associated with mapped-field searching.
   */
  function clearSearchMode(): void {
    insertPosition = null;
    searchMode = false;
    searchFilter = '';
    mappedFieldCandidates = [];
  }

  function clearEnumSelect() {
    selectedNodeId = '';
    enumCandidates = [];
    toggleEnumSelOff();
  }

  /**
   * Establish mapped field candidates for the field search menu.
   *
   * @returns
   */
  function insertFieldReference(): boolean {
    searchMode = true;
    insertPosition = condExprEditor?.getPosition()!;
    searchFilter = '';
    mappedFieldCandidates = executeFieldSearch(searchFilter, true);
    setInsertField(false);
    return true;
  }

  async function insertFieldCb() {
    setInsertField(true);
    setInsertedField(false);
  }

  /**
   * The monaco editor cannot be established without a monaco-container HTML element
   * (conditional editor DOM).  This element only exists if the conditional expression
   * box is enabled (either through the f(x) button or from a previous state - ref
   * 'disabled' property).
   */
  const establishEditor = useCallback((): void => {
    const atlasmapLanguage = getAtlasmapLanguage();
    languages.register({ id: atlasmapLanguageID });

    languages.onLanguage(atlasmapLanguage!.id, () => {
      atlasmapLanguage!.loader().then((module: any) => {
        // Register a tokens provider for the Atlasmap language.
        languages.setMonarchTokensProvider(
          atlasmapLanguage!.id,
          module.language,
        );

        // Register a language configuration for the Atlasmap language.
        languages.setLanguageConfiguration(
          atlasmapLanguage!.id,
          module.languageConfiguration,
        );
      });
    });

    // Establish a unique Atlasmap theme.
    editor.defineTheme('atlasmapTheme', {
      base: 'vs',
      inherit: true,
      rules: [
        { token: 'identifier', foreground: '0b3c0b', fontStyle: 'bold' },
        { token: 'action', foreground: '641564', fontStyle: 'italic' },
        { token: 'number', foreground: '0f2386', fontStyle: 'none' },
        { token: 'number.float', foreground: '0f2386', fontStyle: 'none' },
      ],
      colors: {
        'editor.foreground': '#000000',
        'editor.background': '#EDF9FA',
        'editorCursor.foreground': '#8B0000',
        'editor.lineHighlightBackground': '#0000FF20',
        'editor.selectionBackground': '#88000030',
        'editor.inactiveSelectionBackground': '#88000015',
      },
    });

    const condEditorDOM = document.getElementById('monaco-container');
    const condExpressionEditor = editor.create(condEditorDOM!, {
      ariaLabel: 'atlasmapEditor',
      automaticLayout: true,
      cursorWidth: 2,
      find: {
        addExtraSpaceOnTop: false,
        autoFindInSelection: 'never',
        seedSearchStringFromSelection: 'never',
      },
      folding: false,
      glyphMargin: false,
      hideCursorInOverviewRuler: true,
      language: atlasmapLanguageID,
      lineDecorationsWidth: 0,
      lineNumbers: 'off',
      lineNumbersMinChars: 0,
      minimap: { enabled: false },
      overviewRulerLanes: 0,
      overviewRulerBorder: false,
      value: mappingExpression,
      scrollBeyondLastColumn: 0,
      scrollbar: {
        horizontal: 'hidden',
        horizontalHasArrows: false,
        vertical: 'auto',
        verticalHasArrows: false,
      },
      tabCompletion: 'off',
      theme: 'atlasmapTheme',
      wordWrap: 'on',
    });
    /*
      Custom command override:
      condExpressionEditor.addCommand(
        monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_Z,
        function () {
          console.log('Custom undo');
          condExprEditor?.trigger('keyboard', 'undo', null);
        },
      );
    */
    const addFieldRef: editor.IActionDescriptor = {
      id: 'insert-field-reference',
      label: 'Insert AtlasMap Field Reference',
      contextMenuOrder: 0, // choose the order
      contextMenuGroupId: 'operation', // create a new grouping
      keybindings: [
        // eslint-disable-next-line no-bitwise
        KeyMod.CtrlCmd | KeyCode.Enter,
      ],
      run: insertFieldCb,
    };

    condExpressionEditor.addAction(addFieldRef);
    setCondExprEditor(condExpressionEditor);
  }, [getAtlasmapLanguage, mappingExpression]);

  const initializeMappingExpression = useCallback(() => {
    mappingExprInit();
  }, []);

  /**
   * A mouse click has occurred within the expression box.
   *
   * @param event - mouse event
   */
  const onExprClick = useCallback(
    (_event: editor.ICursorPositionChangedEvent) => {
      // Check for clicking on an enumeration field node.
      enumCandidates = getEnums(selectedNodeId);
      if (enumCandidates.length > 0) {
        toggleEnumSelOn();
      }
    },
    [getEnums, toggleEnumSelOn],
  );

  /**
   * Monaco editor model content change callback.
   */
  const onChange = useCallback(
    (_event: editor.IModelContentChangedEvent) => {
      if (isMappingExpressionEmpty) {
        initializeMappingExpression();
        return;
      }
      mappingExpressionInsertText(condExprEditor!.getValue());
    },
    [
      condExprEditor,
      initializeMappingExpression,
      isMappingExpressionEmpty,
      mappingExpressionInsertText,
    ],
  );

  const onPaste = useCallback(
    (event: editor.IPasteEvent) => {
      if (!condExprEditor || !event) {
        return;
      }
      mappingExpressionInsertText(condExprEditor.getValue());
      condExprEditor!.focus();
    },
    [condExprEditor, mappingExpressionInsertText],
  );

  const updateSearchMode = useCallback(() => {
    if (searchFilter.length === 0) {
      mappedFieldCandidates = [];
      searchMode = false;
    } else {
      searchFilter = searchFilter.substr(0, searchFilter.length - 1);
      mappedFieldCandidates = executeFieldSearch(searchFilter, true);
    }
  }, [executeFieldSearch]);

  /**
   * Lexically tokenize the specified expression text source line.
   */
  const lexExpression = useCallback(
    (editBuffer: string): Token[] | undefined => {
      const tokens = editor.tokenize(editBuffer, atlasmapLanguageID);
      return tokens.shift();
    },
    [],
  );

  /**
   * Remove the token at the current cursor position or remove the entire
   * identifier string if the cursor points anywhere within it.
   *
   * @param before - delete before or after the current position (bs/del)
   * @returns
   */
  const removeTokenAtCaretPosition = useCallback(
    (before: boolean) => {
      let textLine = condExprEditor!.getValue();
      const currentPos = condExprEditor!.getPosition();
      if (!currentPos) {
        return;
      }
      const adjustedColumn = before ? currentPos.column - 1 : currentPos.column;
      const lines = textLine.split('\n');
      const lineText = lines[currentPos.lineNumber - 1];
      const expTokens = lexExpression(lineText);
      let targetToken = null;
      let deletionRange = null;
      const keyword = condExprEditor?.getModel()!.getWordAtPosition(currentPos);
      let targetColumn = 1;

      for (let tokenIndex = 0; tokenIndex < expTokens!.length; tokenIndex++) {
        let t: Token = expTokens![tokenIndex];
        if (t.type === 'identifier') {
          if (
            adjustedColumn >= t.offset + 1 &&
            adjustedColumn <= t.offset + keyword?.word.length!
          ) {
            targetColumn = before ? t.offset + 2 : t.offset + 1;
            deletionRange = new Range(
              currentPos.lineNumber,
              targetColumn,
              currentPos.lineNumber,
              targetColumn + keyword?.word.length! - 1,
            );
            targetToken = t;
            break;
          }
        }
      }

      // Remove the identifier.
      if (targetToken) {
        const op = {
          identifier: 'expression-text-1',
          range: deletionRange!,
          text: '',
          forceMoveMarkers: true,
        };
        condExprEditor!.executeEdits('expression', [op]);
        mappingExpressionRemoveField(
          new Position(
            currentPos.lineNumber,
            before ? targetColumn - 1 : targetColumn,
          ),
        );
      }
    },
    [condExprEditor, lexExpression, mappingExpressionRemoveField],
  );

  /**
   * Handle key down events.
   *
   * @param event - expression keyboard event
   */
  const onKeyDown = useCallback(
    (event: IKeyboardEvent) => {
      if (
        event.metaKey ||
        event.altKey ||
        event.ctrlKey ||
        event.browserEvent.key === 'Enter'
      ) {
        return;
      }
      if (isMappingExpressionEmpty) {
        initializeMappingExpression();
      }
      if (searchMode) {
        if (event.browserEvent.key.match(/[a-z0-9]/i)) {
          searchFilter += event.browserEvent.key;
          mappedFieldCandidates = executeFieldSearch(searchFilter, true);
        }
      }

      if ('Backspace' === event.browserEvent.key) {
        removeTokenAtCaretPosition(true);
        if (searchMode) {
          updateSearchMode();
        }
      } else if ('Delete' === event.browserEvent.key) {
        removeTokenAtCaretPosition(false);
        if (searchMode) {
          updateSearchMode();
        }
      }
    },
    [
      executeFieldSearch,
      initializeMappingExpression,
      isMappingExpressionEmpty,
      removeTokenAtCaretPosition,
      updateSearchMode,
    ],
  );

  /**
   * Toggle the conditional expression monaco editor and expression JSON.
   */
  const toggleExpression = useCallback(() => {
    if (condExprEditor) {
      setCondExprEditor(undefined);
      setEditorInitPhase(false);
    } else {
      setEditorInitPhase(true);
      initializeMappingExpression();
    }
    onToggle();
  }, [condExprEditor, initializeMappingExpression, onToggle]);

  addFieldToExpression = mappingExpressionAddField;
  mappingExprInit = mappingExpressionInit;

  if (insertField) {
    insertFieldReference();
    condExprEditor!.setValue(mappingExpression!);
  }

  useEffect(() => {
    if (disabled) {
      return;
    }

    // The editor initialization phase is required to allow for the async creation
    // of the monaco editor container DOM.
    if (editorInitPhase) {
      if (!condExprEditor) {
        establishEditor();
      } else {
        const eventListener = document.getElementById('monaco-container');
        if (eventListener) {
          eventListener.addEventListener('cut', (_event) => {
            mappingExpressionInsertText(condExprEditor!.getValue());
          });
          condExprEditor.onDidChangeCursorPosition(onExprClick);
          condExprEditor.onDidChangeModelContent(onChange);
          condExprEditor.onDidPaste(onPaste);
          condExprEditor.onKeyDown(onKeyDown);
        }
        setEditorInitPhase(false); // Monaco editor initialization phase complete.
      }
    } else if (mappingExpression !== undefined && !condExprEditor) {
      setEditorInitPhase(true); // Initiate Monaco editor initialization phase.
    } else if (insertedField) {
      condExprEditor!.setValue(mappingExpression!);
      setInsertedField(false);
    }
    return;
  }, [
    condExprEditor,
    disabled,
    editorInitPhase,
    establishEditor,
    executeFieldSearch,
    lexExpression,
    initializeMappingExpression,
    insertedField,
    isMappingExpressionEmpty,
    onChange,
    onExprClick,
    onKeyDown,
    onPaste,
    mappingExpression,
    mappingExpressionInsertText,
    mappingExpressionRemoveField,
  ]);
  return (
    <>
      <Form>
        <FormGroup fieldId="expressionContent">
          <InputGroup>
            <Tooltip
              content={'Enable/ Disable conditional mapping expression.'}
              enableFlip={true}
              entryDelay={750}
              exitDelay={100}
              position={'left'}
            >
              <Button
                variant={'control'}
                aria-label="Enable/ Disable conditional mapping expression"
                tabIndex={-1}
                onClick={toggleExpression}
                data-testid={
                  'enable-disable-conditional-mapping-expression-button'
                }
                isDisabled={disabled}
              >
                <i>
                  f
                  <small style={{ position: 'relative', bottom: -3 }}>
                    (x)
                  </small>
                </i>
              </Button>
            </Tooltip>
            {mappingExpression !== undefined && !disabled && (
              <Tooltip
                content={
                  'Enter text or enter Ctrl+Enter/right-click for a source fields menu.'
                }
                enableFlip={true}
                entryDelay={750}
                exitDelay={100}
                position={'left'}
              >
                <div
                  id="monaco-container"
                  key="monaco-container-div"
                  aria-label="Expression Content"
                  className={css(monacoEditorStyle)}
                  style={{
                    display: 'flex',
                    overflow: 'hidden',
                    paddingLeft: 8,
                    width: '100%',
                  }}
                />
              </Tooltip>
            )}
          </InputGroup>
        </FormGroup>
      </Form>
      <div>
        {mappedFieldCandidates.length > 0 && (
          <span>
            <ExpressionFieldSearch
              clearSearchMode={clearSearchMode}
              insertSelectedField={insertSelectedField}
              mappedFieldCandidates={mappedFieldCandidates}
            />
          </span>
        )}
        {showEnumSelect && (
          <span>
            <ExpressionEnumSelect
              selectedNodeId={selectedNodeId}
              enumCandidates={enumCandidates!}
              clearEnumSelect={clearEnumSelect}
              onEnumSelect={onEnumSelect}
            />
          </span>
        )}
      </div>
    </>
  );
};
