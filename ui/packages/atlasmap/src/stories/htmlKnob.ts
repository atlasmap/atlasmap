import { text } from "@storybook/addon-knobs";
import he from "he";

export function html(name: string, value: string, groupId?: string) {
  return he.decode(text(name, value, groupId));
}
