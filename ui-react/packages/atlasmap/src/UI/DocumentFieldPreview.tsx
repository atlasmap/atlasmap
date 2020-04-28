import { Form, FormGroup, TextInput } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";
import { css, StyleSheet } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  form: {
    padding: "1rem",
    marginTop: "0.5rem",
  },
});

export interface IDocumentFieldPreviewProps {
  id: string;
  value: string;
  onChange: (value: string) => void;
}

export const DocumentFieldPreview: FunctionComponent<IDocumentFieldPreviewProps> = ({
  id,
  value,
  onChange,
}) => {
  return (
    <Form
      className={css(styles.form)}
      onClick={(event) => event.stopPropagation()}
    >
      <FormGroup label="Mapping preview" fieldId={id}>
        <TextInput
          id={id}
          type="text"
          onChange={onChange}
          value={value}
          aria-label="Type sample data here"
          placeholder="Type sample data here"
        />
      </FormGroup>
    </Form>
  );
};
