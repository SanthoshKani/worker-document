package com.hpe.caf.worker.document;

import com.hpe.caf.worker.document.extensibility.DocumentWorker;
import com.hpe.caf.api.HealthResult;
import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.TaskRejectedException;
import com.hpe.caf.api.worker.Worker;
import com.hpe.caf.api.worker.WorkerConfiguration;
import com.hpe.caf.api.worker.WorkerFactory;
import com.hpe.caf.api.worker.WorkerTaskData;
import com.hpe.caf.worker.document.impl.ApplicationImpl;
import com.hpe.caf.worker.document.impl.HealthMonitorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows implementations of the DocumentWorker class can be used with the Worker Framework.
 */
public class DocumentWorkerAdapter implements WorkerFactory
{
    /**
     * Used for logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocumentWorkerAdapter.class);

    /**
     * This object stores the global data that was initially passed to the WorkerFactoryProvider when it was called. It effectively acts
     * as a global object for the worker.
     */
    protected final ApplicationImpl application;

    /**
     * This is the actual implementation of the worker.<p>
     * This class is adapting its interface so that it can be used with the Worker Framework.
     */
    private final DocumentWorker documentWorker;

    /**
     * This object stores the configuration for the Document Worker. It is normally read from a JSON file with a name like
     * "cfg~worker-name~DocumentWorkerConfiguration".
     */
    private final DocumentWorkerConfiguration configuration;

    /**
     * Constructs the DocumentWorkerAdapter object, which adapts the DocumentWorker interface so that objects which implement it can be
     * used with the Worker Framework.
     *
     * @param application the global data for the worker
     * @param documentWorker the actual implementation of the worker
     */
    public DocumentWorkerAdapter(final ApplicationImpl application, final DocumentWorker documentWorker)
    {
        this.application = application;
        this.documentWorker = documentWorker;
        this.configuration = application.getConfiguration();
    }

    @Override
    public HealthResult healthCheck()
    {
        final HealthMonitorImpl healthMonitor = new HealthMonitorImpl(application);
        documentWorker.checkHealth(healthMonitor);

        return healthMonitor.getHealthResult();
    }

    @Override
    public Worker getWorker(WorkerTaskData workerTask)
        throws TaskRejectedException, InvalidTaskException
    {
        return new DocumentMessageProcessor(application, documentWorker, workerTask);
    }

    @Override
    public WorkerConfiguration getWorkerConfiguration()
    {
        return configuration;
    }

    @Override
    public String getInvalidTaskQueue()
    {
        return configuration.getOutputQueue();
    }

    @Override
    public int getWorkerThreads()
    {
        return configuration.getThreads();
    }

    @Override
    public void shutdown()
    {
        try {
            documentWorker.close();
        } catch (Exception ex) {
            LOG.warn("Error closing DocumentWorker during shutdown", ex);
        }
    }
}