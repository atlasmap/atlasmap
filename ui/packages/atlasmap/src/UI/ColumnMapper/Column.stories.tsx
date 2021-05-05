import { Column, ColumnBody, SearchableColumnHeader } from ".";
import { boolean, number } from "@storybook/addon-knobs";

import React from "react";
import { action } from "@storybook/addon-actions";

export default {
  title: "ColumnMapper",
  component: Column,
  includeStories: [], // or don't load this file at all
};

export const example = () => (
  <Column
    totalColumns={number("Total number of columns", 1)}
    visible={boolean("Is visible", true)}
  >
    <SearchableColumnHeader title={"Header"} onSearch={action("onSearch")} />
    <ColumnBody>
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
    </ColumnBody>
  </Column>
);
