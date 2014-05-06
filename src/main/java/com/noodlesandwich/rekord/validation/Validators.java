package com.noodlesandwich.rekord.validation;

import java.util.Arrays;
import com.noodlesandwich.rekord.FixedRekord;
import com.noodlesandwich.rekord.Key;
import org.pcollections.OrderedPSet;
import org.pcollections.PSet;

public final class Validators {
    private Validators() { }

    public static <T> Validator<T> when(final BooleanValidator<T> validator) {
        return new Validator<T>() {
            @Override public void test(FixedRekord<T> rekord) throws InvalidRekordException {
                if (!validator.test(rekord)) {
                    throw new UnknownRekordValidationException();
                }
            }
        };
    }

    public static <T> Validator<T> everything() {
        return new Validator<T>() {
            @Override public void test(FixedRekord<T> rekord) { }
        };
    }

    public static <T> Validator<T> nothing() {
        return new Validator<T>() {
            @Override public void test(FixedRekord<T> rekord) throws InvalidRekordException {
                throw new UnknownRekordValidationException();
            }
        };
    }

    public static <T> Validator<T> rekordsWithAllProperties() {
        return new Validator<T>() {
            @Override public void test(FixedRekord<T> rekord) throws InvalidRekordException {
                PSet<Key<? super T, ?>> expectedKeys = rekord.acceptedKeys();
                PSet<Key<? super T, ?>> actualKeys = rekord.keys();
                PSet<Key<? super T, ?>> missingKeys = expectedKeys.minusAll(actualKeys);
                if (!missingKeys.isEmpty()) {
                    throw new MissingPropertiesException(missingKeys);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Validator<T> rekordsWithProperties(final Key<? super T, ?>... keys) {
        return new Validator<T>() {
            @Override public void test(FixedRekord<T> rekord) throws InvalidRekordException {
                PSet<Key<? super T, ?>> expectedKeys = OrderedPSet.from(Arrays.asList(keys));
                PSet<Key<? super T, ?>> actualKeys = rekord.keys();
                PSet<Key<? super T, ?>> missingKeys = expectedKeys.minusAll(actualKeys);
                if (!missingKeys.isEmpty()) {
                    throw new MissingPropertiesException(missingKeys);
                }
            }
        };
    }

    public static final class UnknownRekordValidationException extends InvalidRekordException {
        public UnknownRekordValidationException() {
            super("The rekord was invalid.");
        }
    }

    public static final class MissingPropertiesException extends InvalidRekordException {
        public <T> MissingPropertiesException(PSet<Key<? super T, ?>> keys) {
            super(String.format("The rekord was missing the properties %s.", keys));
        }
    }
}