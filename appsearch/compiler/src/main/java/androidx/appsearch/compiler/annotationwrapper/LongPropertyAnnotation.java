/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.appsearch.compiler.annotationwrapper;

import static androidx.appsearch.compiler.IntrospectionHelper.APPSEARCH_SCHEMA_CLASS;
import static androidx.appsearch.compiler.IntrospectionHelper.DOCUMENT_ANNOTATION_CLASS;

import static com.google.auto.common.MoreTypes.asElement;

import androidx.appsearch.compiler.IntrospectionHelper;
import androidx.appsearch.compiler.ProcessingException;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * An instance of the {@code @Document.LongProperty} annotation.
 */
@AutoValue
public abstract class LongPropertyAnnotation extends DataPropertyAnnotation {
    public static final ClassName CLASS_NAME =
            DOCUMENT_ANNOTATION_CLASS.nestedClass("LongProperty");

    public static final ClassName CONFIG_CLASS =
            APPSEARCH_SCHEMA_CLASS.nestedClass("LongPropertyConfig");

    private static final ClassName DEFAULT_SERIALIZER_CLASS =
            CLASS_NAME.nestedClass("DefaultSerializer");

    public LongPropertyAnnotation() {
        super(
                CLASS_NAME,
                CONFIG_CLASS,
                /* genericDocGetterName= */"getPropertyLong",
                /* genericDocArrayGetterName= */"getPropertyLongArray",
                /* genericDocSetterName= */"setPropertyLong");
    }

    /**
     * @param defaultName The name to use for the annotated property in case the annotation
     *                    params do not mention an explicit name.
     * @throws ProcessingException If the annotation points to an Illegal serializer class.
     */
    static @NonNull LongPropertyAnnotation parse(
            @NonNull Map<String, Object> annotationParams,
            @NonNull String defaultName) throws ProcessingException {
        String name = (String) annotationParams.get("name");
        SerializerClass customSerializer = null;
        TypeMirror serializerInAnnotation = (TypeMirror) annotationParams.get("serializer");
        String typeName = TypeName.get(serializerInAnnotation).toString();
        if (!typeName.equals(DEFAULT_SERIALIZER_CLASS.canonicalName())) {
            customSerializer = SerializerClass.create(
                    (TypeElement) asElement(serializerInAnnotation),
                    SerializerClass.Kind.LONG_SERIALIZER);
        }
        return new AutoValue_LongPropertyAnnotation(
                name.isEmpty() ? defaultName : name,
                (boolean) annotationParams.get("required"),
                (int) annotationParams.get("indexingType"),
                customSerializer);
    }

    /**
     * Specifies how a property should be indexed.
     */
    public abstract int getIndexingType();

    /**
     * An optional {@link androidx.appsearch.app.LongSerializer}.
     *
     * <p>This is specified in the annotation when the annotated getter/field is of some custom
     * type that should boil down to a long in the database.
     *
     * @see androidx.appsearch.annotation.Document.LongProperty#serializer()
     */
    public abstract @Nullable SerializerClass getCustomSerializer();

    @Override
    public final @NonNull Kind getDataPropertyKind() {
        return Kind.LONG_PROPERTY;
    }

    @Override
    public @NonNull TypeMirror getUnderlyingTypeWithinGenericDoc(
            @NonNull IntrospectionHelper helper) {
        return helper.mLongPrimitiveType;
    }
}
