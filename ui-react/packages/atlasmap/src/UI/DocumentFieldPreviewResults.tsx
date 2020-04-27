import { Form, FormGroup, TextInput } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  form: {
    padding: "1rem",
    marginTop: "0.5rem",
  },
});

export interface IDocumentFieldPreviewResultsProps {
  id: string;
  value: string;
}

export const DocumentFieldPreviewResults: FunctionComponent<IDocumentFieldPreviewResultsProps> = ({
  id,
  value,
}) => {
  return (
    <Form className={css(styles.form)}>
      <FormGroup label="Preview results" fieldId={id}>
        <TextInput
          id={id}
          type="text"
          value={value}
          aria-label="Mapping results preview"
          placeholder="Results will be displayed here"
          isDisabled={true}
        />
      </FormGroup>
    </Form>
  );
};
