package com.example.appsearch;

import androidx.annotation.RestrictTo;
import androidx.appsearch.app.AppSearchSchema;
import androidx.appsearch.app.DocumentClassFactory;
import androidx.appsearch.app.DocumentClassMappingContext;
import androidx.appsearch.app.GenericDocument;
import androidx.appsearch.exceptions.AppSearchException;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.appsearch.compiler.AppSearchCompiler")
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class $$__AppSearch__Gift implements DocumentClassFactory<Gift> {
  public static final String SCHEMA_NAME = "Gift";

  @Override
  public String getSchemaName() {
    return SCHEMA_NAME;
  }

  @Override
  public AppSearchSchema getSchema() throws AppSearchException {
    return new AppSearchSchema.Builder(SCHEMA_NAME)
          .build();
  }

  @Override
  public List<Class<?>> getDependencyDocumentClasses() throws AppSearchException {
    return Collections.emptyList();
  }

  @Override
  public GenericDocument toGenericDocument(Gift document) throws AppSearchException {
    GenericDocument.Builder<?> builder =
        new GenericDocument.Builder<>(document.namespace, document.id, SCHEMA_NAME);
    return builder.build();
  }

  @Override
  public Gift fromGenericDocument(GenericDocument genericDoc,
      DocumentClassMappingContext documentClassMappingContext) throws AppSearchException {
    String namespaceConv = genericDoc.getNamespace();
    String idConv = genericDoc.getId();
    Gift document = new Gift();
    document.namespace = namespaceConv;
    document.id = idConv;
    return document;
  }
}
