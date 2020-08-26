import React, {
  FunctionComponent,
  useContext,
  useState,
  useEffect,
} from "react";
import { ViewContext } from "../../ViewProvider";
import { IAtlasmapMapping } from "../../models";
import {
  Select,
  SelectOption,
  TextInput,
  Flex,
  FlexItem,
} from "@patternfly/react-core";
import { BoltIcon, PlusIcon } from "@patternfly/react-icons";
import { IFunction } from "src/Atlasmap";

export interface ITransformationDocumentProps {
  mapping: IAtlasmapMapping;
  availableFunctions: IFunction[];
}

export const TransformationDocument: FunctionComponent<ITransformationDocumentProps> = ({
  mapping,
  availableFunctions,
}) => {
  const context = useContext(ViewContext);
  const [functionDropDownOpen, setFunctionDropDownOpen] = useState<boolean[]>(
    [],
  );

  if (!mapping.mapping.transition.expression) {
    context!.initializeActiveMappingExpression();
    mapping.mapping.transition.lookupTableName =
      "Map(Source:any:true,Target:any)";
  }

  const functionTexts = mapping.mapping.transition.lookupTableName
    ? mapping.mapping.transition.lookupTableName?.split("|")
    : [];
  const functions: IFunction[] = functionTexts.map((text) => {
    console.log("text: ", text);
    const leftParen = text.indexOf("(");
    if (leftParen < 0) {
      return availableFunctions[0];
    }
    const prmTexts = text
      .substring(leftParen + 1, text.indexOf(")"))
      .split(",");
    const prms = prmTexts.map((pt) => {
      const colon1 = pt.indexOf(":");
      const colon2 = pt.indexOf(":", colon1 + 1);
      return {
        name: pt.substring(0, colon1),
        type: pt.substring(colon1 + 1, colon2 > 0 ? colon2 : undefined),
        canBeFunction: colon2 < 0 ? false : pt.substring(colon2 + 1) === "true",
      };
    });
    return { name: text.substring(0, leftParen), parameters: prms };
  });
  console.log("functionDropDownOpen]: ", functionDropDownOpen);

  function onSelectFunction(index: number, selection: string) {
    functions[index] = availableFunctions.find((f) => f.name === selection)!;
    const funcTexts = functions!.map((f) => {
      return (
        f.name +
        "(" +
        f.parameters
          .map((p) => p.name + ":" + p.type + ":" + p.canBeFunction)
          .join(",") +
        ")"
      );
    });
    mapping.mapping.transition.lookupTableName = funcTexts.join("|");
    functionDropDownOpen[index] = false;
    setFunctionDropDownOpen(functionDropDownOpen);
  }

  function onAddFunction() {
    mapping.mapping.transition.lookupTableName =
      "|" + mapping.mapping.transition.lookupTableName;
  }

  useEffect(() => {
    if (functionDropDownOpen.length < functions.length) {
      setFunctionDropDownOpen(functions.map(() => false));
    }
  }, [functionDropDownOpen.length, functions]);

  return (
    <div>
      {functions.map((f: IFunction, index: number) => (
        <div key={index}>
          <Flex>
            <FlexItem>
              <BoltIcon />
            </FlexItem>
            <FlexItem>
              <Select
                selections={f.name}
                isExpanded={functionDropDownOpen[index]}
                onToggle={() => {
                  functionDropDownOpen[index] = !functionDropDownOpen[index];
                  setFunctionDropDownOpen(functionDropDownOpen);
                }}
                onSelect={(_e, selection) =>
                  onSelectFunction(index, selection as string)
                }
              >
                {availableFunctions.map((func, index) => (
                  <SelectOption key={index} value={func.name} />
                ))}
              </Select>
            </FlexItem>
          </Flex>
          {f.parameters.map((p) => {
            return (
              <Flex>
                <FlexItem>{p.name}:</FlexItem>
                <FlexItem>
                  <TextInput aria-label={p.name} />
                </FlexItem>
                {p.canBeFunction && (
                  <FlexItem>
                    <PlusIcon onClick={onAddFunction} />
                  </FlexItem>
                )}
              </Flex>
            );
          })}
        </div>
      ))}
    </div>
  );
};
