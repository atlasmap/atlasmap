import React from "react";

import { boolean, number } from "@storybook/addon-knobs";
import { Column } from ".";

export default {
  title: "ColumnMapper",
  component: Column,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <div style={{ height: 300, display: "flex", overflow: "auto" }}>
    <Column
      totalColumns={number("Total number of columns", 1)}
      visible={boolean("Is visible", true)}
    >
      <p>I can scroll.</p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Animi at
        deserunt dolor eos est impedit ipsa, laboriosam laborum nisi officia
        officiis quis reiciendis repellendus reprehenderit sapiente sint sunt
        totam vitae!
      </p>
    </Column>
  </div>
);
