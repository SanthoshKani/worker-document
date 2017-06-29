/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.hpe.caf.worker.document.impl;

import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.worker.document.DocumentWorkerAction;
import com.hpe.caf.worker.document.DocumentWorkerFieldChanges;
import com.hpe.caf.worker.document.DocumentWorkerFieldEncoding;
import com.hpe.caf.worker.document.DocumentWorkerFieldValue;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Field;
import com.hpe.caf.worker.document.model.FieldValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FieldImpl extends DocumentWorkerObjectImpl implements Field
{
    private static final Logger LOG = LoggerFactory.getLogger(FieldImpl.class);
    private final DocumentImpl document;

    private final String fieldName;

    private final List<DocumentWorkerFieldValue> initialFieldValues;

    private final DocumentWorkerFieldChanges fieldChanges;

    public FieldImpl(final ApplicationImpl application, final DocumentImpl document, final String fieldName)
    {
        super(application);
        this.document = Objects.requireNonNull(document);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.initialFieldValues = getInitialFieldValues(document, fieldName);
        this.fieldChanges = createFieldChanges();
    }

    @Override
    public void add(String data)
    {
        if (data.length() > 100) {
            try {
                String dataRef = application.getDataStore().store(data.getBytes(), null);
                addReference(dataRef);
                return;
            }
            catch (DataStoreException e) {
                LOG.warn("Could not store data over the threshold in data store.", e);
            }
        }
        final DocumentWorkerFieldValue fieldValue = new DocumentWorkerFieldValue();
        fieldValue.data = data;
        fieldChanges.values.add(fieldValue);
    }

    @Override
    public void add(byte[] data)
    {
        if (data.length > 100) {
            try {
                String dataRef = application.getDataStore().store(data, null);
                addReference(dataRef);
                return;
            }
            catch (DataStoreException e) {
                LOG.warn("Could not store data over the threshold in data store.", e);
            }
        }

        final DocumentWorkerFieldValue fieldValue = new DocumentWorkerFieldValue();
        fieldValue.data = Base64.encodeBase64String(data);
        fieldValue.encoding = DocumentWorkerFieldEncoding.base64;

        fieldChanges.values.add(fieldValue);
    }

    @Override
    public void addReference(String dataRef)
    {
        final DocumentWorkerFieldValue fieldValue = new DocumentWorkerFieldValue();
        fieldValue.data = dataRef;
        fieldValue.encoding = DocumentWorkerFieldEncoding.storage_ref;

        fieldChanges.values.add(fieldValue);
    }

    @Override
    public void clear()
    {
        fieldChanges.action = DocumentWorkerAction.replace;
        fieldChanges.values.clear();
    }

    @Override
    public Document getDocument()
    {
        return document;
    }

    @Override
    public String getName()
    {
        return fieldName;
    }

    @Override
    public List<String> getStringValues()
    {
        final List<String> stringValueList = getValues()
            .stream()
            .filter(fieldValue -> (!fieldValue.isReference()) && fieldValue.isStringValue())
            .map(fieldValue -> fieldValue.getStringValue())
            .collect(Collectors.toList());

        return Collections.unmodifiableList(stringValueList);
    }

    @Override
    public FieldValues getValues()
    {
        final List<DocumentWorkerFieldValue> currentFieldValues = new ArrayList<>();

        if (fieldChanges.action == DocumentWorkerAction.add) {
            currentFieldValues.addAll(initialFieldValues);
        }

        currentFieldValues.addAll(fieldChanges.values);

        return new FieldValuesImpl(application, this, currentFieldValues);
    }

    @Override
    public boolean hasChanges()
    {
        return (fieldChanges.action == DocumentWorkerAction.replace)
            || (!fieldChanges.values.isEmpty());
    }

    @Override
    public boolean hasValues()
    {
        // Return true if there are new values being added OR (the action is 'add' AND there are initial values)
        return (!fieldChanges.values.isEmpty())
            || ((fieldChanges.action == DocumentWorkerAction.add) && (!initialFieldValues.isEmpty()));
    }

    @Override
    public void reset()
    {
        fieldChanges.action = DocumentWorkerAction.add;
        fieldChanges.values.clear();
    }

    /**
     * Returns the changes made to the field, or null if no changes have been made.
     *
     * @return the changes made to the field, or null if no changes have been made
     */
    public DocumentWorkerFieldChanges getChanges()
    {
        return hasChanges()
            ? fieldChanges
            : null;
    }

    private static List<DocumentWorkerFieldValue> getInitialFieldValues(final DocumentImpl document, final String fieldName)
    {
        final Map<String, List<DocumentWorkerFieldValue>> fields = document.getDocumentWorkerTask().fields;

        if (fields == null) {
            return Collections.emptyList();
        }

        final List<DocumentWorkerFieldValue> fieldValues = fields.get(fieldName);

        return (fieldValues != null) ? fieldValues : Collections.emptyList();
    }

    private static DocumentWorkerFieldChanges createFieldChanges()
    {
        final DocumentWorkerFieldChanges fieldChanges = new DocumentWorkerFieldChanges();
        fieldChanges.action = DocumentWorkerAction.add;
        fieldChanges.values = new ArrayList<>();

        return fieldChanges;
    }
}
