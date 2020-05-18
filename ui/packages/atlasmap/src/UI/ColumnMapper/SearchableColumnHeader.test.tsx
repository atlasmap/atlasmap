import React from "react";

import { render, fireEvent, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { SearchableColumnHeader } from "./SearchableColumnHeader";
import { example } from "./SearchableColumnHeader.stories";

describe("SearchableColumnHeader tests", () => {
  test("should render", async () => {
    const { getByText } = render(example());
    getByText("Source");
  });

  test("change events are propagated when typing", async () => {
    const searchButtonLabel = "Toggle search input";
    const searchInputLabel = "Search fields";
    const onSearchSpy = jest.fn();
    const { getByLabelText, queryByLabelText, findByLabelText } = render(
      <SearchableColumnHeader title={"Source"} onSearch={onSearchSpy} />,
    );
    expect(queryByLabelText(searchInputLabel)).toBeNull();

    act(() => {
      fireEvent.click(getByLabelText(searchButtonLabel));
    });

    const inputField = await findByLabelText(searchInputLabel);

    await userEvent.type(inputField, "ABC");

    // the onChange event should be triggered for every keystoke
    expect(onSearchSpy).toHaveBeenCalledTimes(3);
  });
});
