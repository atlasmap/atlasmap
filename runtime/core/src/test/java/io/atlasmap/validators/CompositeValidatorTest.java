package io.atlasmap.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.ValidationScope;

public class CompositeValidatorTest {

    @Test
    public void testSupports() {
        NotEmptyValidator notEmptyValidator = new NotEmptyValidator(ValidationScope.ALL, "violationMessage");
        assertTrue(notEmptyValidator.supports(new HashSet<String>()));
        assertTrue(notEmptyValidator.supports(new ArrayList<String>()));
        assertTrue(notEmptyValidator.supports(new HashMap<String, String>()));
        assertFalse(notEmptyValidator.supports(new String[1]));
        assertTrue(notEmptyValidator.supports(new ArrayDeque<String>()));

        List<AtlasValidator> validators = new ArrayList<>();
        validators.add(notEmptyValidator);
        CompositeValidator compositeValidator = new CompositeValidator(validators);
        assertFalse(compositeValidator.supports(NotEmptyValidator.class));
        assertTrue(compositeValidator.supports(List.class));
    }

}
