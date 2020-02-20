import { FunctionComponent, useState, useEffect, ReactElement } from "react";

export interface IDelayedBooleanProps {
  value: boolean;
  delay?: number;
  children: (value: boolean) => ReactElement | null;
}

export const DelayedBoolean: FunctionComponent<IDelayedBooleanProps> = ({
  value,
  delay = 1000,
  children,
}) => {
  const [delayedValue, setDelayedValue] = useState(value);
  useEffect(() => {
    const updateValue = () => setDelayedValue(value);
    const timer = setTimeout(updateValue, delay);
    return () => clearTimeout(timer);
  }, [delay, value]);
  return children(delayedValue);
};
