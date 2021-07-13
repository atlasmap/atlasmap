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
  Alert,
  AlertActionCloseButton,
  AlertProps,
  // Progress,
} from '@patternfly/react-core';
import React, { FunctionComponent, useEffect, useState } from 'react';

import { useToggle } from '../impl/utils';

export interface ITimedToastProps {
  variant: AlertProps['variant'];
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
      actionClose={
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
