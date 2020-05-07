import React, { FunctionComponent, useEffect, useState } from "react";
import {
  Alert,
  AlertActionCloseButton,
  AlertProps,
  // Progress,
} from "@patternfly/react-core";
import { useToggle } from "./useToggle";

export interface ITimedToastProps {
  variant: AlertProps["variant"];
  title: string;
  onTimeout: () => void;
  onClose: () => void;
}

export const TimedToast: FunctionComponent<ITimedToastProps> = ({
  variant,
  title,
  children,
  onTimeout,
  onClose,
}) => {
  const showTime = 8000;
  const intervalTime = 100;
  const [elapsed, setElapsed] = useState(showTime);
  const {
    state: hovering,
    toggleOn: hoveringOn,
    toggleOff: hoveringOff,
  } = useToggle(false);
  useEffect(() => {
    const tick = () => {
      setElapsed((elapsed) => elapsed - intervalTime);
    };
    let timer: NodeJS.Timer | undefined = undefined;
    if (elapsed <= 0) {
      onTimeout();
    } else if (!hovering && !timer) {
      timer = setInterval(tick, intervalTime);
    } else {
      setElapsed(showTime);
    }
    return () => timer && clearTimeout(timer);
  }, [elapsed, hovering, onTimeout]);
  return (
    <Alert
      isLiveRegion
      variant={variant}
      title={title}
      action={
        <AlertActionCloseButton
          title={title}
          variantLabel={`${variant} alert`}
          onClose={onClose}
        />
      }
      onMouseEnter={hoveringOn}
      onMouseLeave={hoveringOff}
    >
      {children}
      {/* {!hovering && (
        <Progress
          size={"sm"}
          measureLocation={"none"}
          value={elapsed}
          max={8000}
        />
      )} */}
    </Alert>
  );
};
