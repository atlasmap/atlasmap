import { Form, FormGroup, TextInput } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";
import { css } from "@patternfly/react-styles";

import styles from "./DocumentFieldPreviewResults.css";

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
          value={value === undefined ? "" : value}
          aria-label="Mapping results preview"
          placeholder="Results will be displayed here"
          isDisabled={true}
          data-testid={`results-document-mapping-preview-${id}-field`}
        />
      </FormGroup>
    </Form>
  );
};
