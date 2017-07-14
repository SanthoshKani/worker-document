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
package com.hpe.caf.worker.document;

import com.hpe.caf.api.worker.WorkerTask;
import com.hpe.caf.worker.document.tasks.AbstractTask;

public final class BulkDocument
{
    private final WorkerTask workerTask;
    private final AbstractTask documentWorkerTask;

    public BulkDocument(final WorkerTask workerTask, final AbstractTask documentWorkerTask)
    {
        this.workerTask = workerTask;
        this.documentWorkerTask = documentWorkerTask;
    }

    public WorkerTask getWorkerTask()
    {
        return workerTask;
    }

    public AbstractTask getDocumentWorkerTask()
    {
        return documentWorkerTask;
    }
}
