import React, { FunctionComponent } from "react";
import { ToolbarGroup, ToolbarItem, Button } from "@patternfly/react-core";
import { css, StyleSheet } from "@patternfly/react-styles";
import {
  // ExpressionContent,
  IExpressionContentProps,
} from "./ExpressionContent";

const styles = StyleSheet.create({
  toolbarItem: { flex: 1 },
});

export const ConditionalExpressionInput: FunctionComponent<IExpressionContentProps> = () =>
  // props,
  {
    return (
      <ToolbarGroup className={css(styles.toolbarItem)} role={"form"}>
        <ToolbarItem>
          <Button variant={"plain"} tabIndex={-1}>
            <i>
              f<sub>(x)</sub>
            </i>
          </Button>
        </ToolbarItem>
        <ToolbarItem className={css(styles.toolbarItem)}>
          {/* <ExpressionContent {...props} /> */}
        </ToolbarItem>
      </ToolbarGroup>
    );
  };
