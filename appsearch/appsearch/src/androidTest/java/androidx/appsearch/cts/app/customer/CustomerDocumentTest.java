/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.appsearch.cts.app.customer;

import static com.google.common.truth.Truth.assertThat;

import androidx.appsearch.app.GenericDocument;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

/**
 * Tests that {@link GenericDocument} and {@link GenericDocument.Builder} are extendable by
 * developers.
 *
 * <p>This class is intentionally in a different package than {@link GenericDocument} to make sure
 * there are no package-private methods required for external developers to add custom types.
 */
public class CustomerDocumentTest {

    private static final byte[] BYTE_ARRAY1 = new byte[]{(byte) 1, (byte) 2, (byte) 3};
    private static final byte[] BYTE_ARRAY2 = new byte[]{(byte) 4, (byte) 5, (byte) 6};
    private static final GenericDocument DOCUMENT_PROPERTIES1 = new GenericDocument
            .Builder<>("namespace", "sDocumentProperties1", "sDocumentPropertiesSchemaType1")
            .build();
    private static final GenericDocument DOCUMENT_PROPERTIES2 = new GenericDocument
            .Builder<>("namespace", "sDocumentProperties2", "sDocumentPropertiesSchemaType2")
            .build();

    @Test
    public void testBuildCustomerDocument() {
        CustomerDocument customerDocument = new CustomerDocument.Builder("namespace", "id1")
                .setScore(1)
                .setCreationTimestampMillis(0)
                .setPropertyLong("longKey1", 1L, 2L, 3L)
                .setPropertyDouble("doubleKey1", 1.0, 2.0, 3.0)
                .setPropertyBoolean("booleanKey1", true, false, true)
                .setPropertyString("stringKey1", "test-value1", "test-value2", "test-value3")
                .setPropertyBytes("byteKey1", BYTE_ARRAY1, BYTE_ARRAY2)
                .setPropertyDocument("documentKey1", DOCUMENT_PROPERTIES1, DOCUMENT_PROPERTIES2)
                .build();

        assertThat(customerDocument.getNamespace()).isEqualTo("namespace");
        assertThat(customerDocument.getId()).isEqualTo("id1");
        assertThat(customerDocument.getSchemaType()).isEqualTo("customerDocument");
        assertThat(customerDocument.getScore()).isEqualTo(1);
        assertThat(customerDocument.getCreationTimestampMillis()).isEqualTo(0L);
        assertThat(customerDocument.getPropertyLongArray("longKey1")).asList()
                .containsExactly(1L, 2L, 3L);
        assertThat(customerDocument.getPropertyDoubleArray("doubleKey1")).usingExactEquality()
                .containsExactly(1.0, 2.0, 3.0);
        assertThat(customerDocument.getPropertyBooleanArray("booleanKey1")).asList()
                .containsExactly(true, false, true);
        assertThat(customerDocument.getPropertyStringArray("stringKey1")).asList()
                .containsExactly("test-value1", "test-value2", "test-value3");
        assertThat(customerDocument.getPropertyBytesArray("byteKey1")).asList()
                .containsExactly(BYTE_ARRAY1, BYTE_ARRAY2);
        assertThat(customerDocument.getPropertyDocumentArray("documentKey1")).asList()
                .containsExactly(DOCUMENT_PROPERTIES1, DOCUMENT_PROPERTIES2);
    }

    /**
     * An example document type for test purposes, defined outside of
     * {@link GenericDocument} (the way an external developer would define
     * it).
     */
    private static class CustomerDocument extends GenericDocument {
        private CustomerDocument(GenericDocument document) {
            super(document);
        }

        public static class Builder extends GenericDocument.Builder<CustomerDocument.Builder> {
            private Builder(@NonNull String namespace, @NonNull String id) {
                super(namespace, id, "customerDocument");
            }

            @Override
            @NonNull
            public CustomerDocument build() {
                return new CustomerDocument(super.build());
            }
        }
    }
}
