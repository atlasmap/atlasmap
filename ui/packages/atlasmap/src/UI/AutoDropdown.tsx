import { Dropdown, DropdownProps } from "@patternfly/react-core";
import React, { FunctionComponent, ReactElement } from "react";

import { useToggle } from "../Atlasmap/utils";

export interface IAutoDropdown
  extends Omit<Omit<DropdownProps, "css">, "toggle"> {
  toggle: (props: { isOpen: boolean; toggleOpen: () => void }) => ReactElement;
}

export const AutoDropdown: FunctionComponent<IAutoDropdown> = ({
  toggle,
  ...props
}) => {
  const { state: isOpen, toggle: toggleOpen } = useToggle(false);

  return (
    <Dropdown
      {...props}
      isOpen={isOpen}
      onSelect={toggleOpen}
      toggle={toggle({ isOpen, toggleOpen })}
    />
  );
};
