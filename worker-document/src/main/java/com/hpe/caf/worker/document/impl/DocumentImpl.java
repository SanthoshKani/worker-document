package com.hpe.caf.worker.document.impl;

import com.hpe.caf.api.worker.TaskStatus;
import com.hpe.caf.api.worker.WorkerResponse;
import com.hpe.caf.api.worker.WorkerTaskData;
import com.hpe.caf.worker.document.DocumentWorkerConstants;
import com.hpe.caf.worker.document.DocumentWorkerFailure;
import com.hpe.caf.worker.document.DocumentWorkerResult;
import com.hpe.caf.worker.document.DocumentWorkerResultFunctions;
import com.hpe.caf.worker.document.DocumentWorkerTask;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.Field;
import com.hpe.caf.worker.document.model.Fields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DocumentImpl extends DocumentWorkerObjectImpl implements Document
{
    private final DocumentWorkerTask documentWorkerTask;

    private final FieldsImpl fields;

    private List<DocumentWorkerFailure> failures;

    public DocumentImpl(
        final ApplicationImpl application,
        final WorkerTaskData workerTask,
        final DocumentWorkerTask documentWorkerTask
    )
    {
        super(application);
        Objects.requireNonNull(workerTask);
        this.documentWorkerTask = Objects.requireNonNull(documentWorkerTask);
        this.fields = new FieldsImpl(application, this);
        this.failures = null;
    }

    @Override
    public Fields getFields()
    {
        return fields;
    }

    @Override
    public Field getField(String fieldName)
    {
        return fields.get(fieldName);
    }

    @Override
    public String getCustomData(String dataKey)
    {
        final Map<String, String> customMap = documentWorkerTask.customData;
        if (customMap == null) {
            return null;
        }

        return customMap.get(dataKey);
    }

    @Override
    public void addFailure(String failureId, String failureMessage)
    {
        if (failureId == null && failureMessage == null) {
            return;
        }

        final DocumentWorkerFailure failure = new DocumentWorkerFailure();
        failure.failureId = failureId;
        failure.failureMessage = failureMessage;

        if (failures == null) {
            failures = new ArrayList<>(1);
        }

        failures.add(failure);
    }

    public DocumentWorkerTask getDocumentWorkerTask()
    {
        return documentWorkerTask;
    }

    public WorkerResponse createWorkerResponse()
    {
        // Construct the DocumentWorkerResult object
        final DocumentWorkerResult documentWorkerResult = new DocumentWorkerResult();
        documentWorkerResult.fieldChanges = fields.getChanges();
        documentWorkerResult.failures = failures;

        // Serialise the result object
        final byte[] data = DocumentWorkerResultFunctions.serialise(documentWorkerResult, application.getCodec());

        // Create the WorkerResponse object
        return new WorkerResponse(application.getConfiguration().getOutputQueue(),
                                  TaskStatus.RESULT_SUCCESS,
                                  data,
                                  DocumentWorkerConstants.WORKER_NAME,
                                  DocumentWorkerConstants.WORKER_API_VER,
                                  null);
    }
}