package com.hpe.caf.worker.document.extensibility;

import com.hpe.caf.worker.document.exceptions.DocumentWorkerTransientException;
import com.hpe.caf.worker.document.model.Document;
import com.hpe.caf.worker.document.model.HealthMonitor;

/**
 * This is the main interface that must be implemented by a Document Worker.
 * <p>
 * When integrated into the Data Processing pipeline the Document Worker implementation is passed a defined subset of the fields that the
 * document has. It is able to add, remove, or update the document's fields.
 * <p>
 * The fields that are passed to the Document Worker are defined in the Data Processing Action that is added to the workflow. It is
 * possible to pass all of the document's fields to the worker.
 * <p>
 * The Document Worker may implement the {@link BulkDocumentWorker} interface instead of implementing this interface if there would be
 * efficiency gains to be made by processing multiple documents together. The BulkDocumentWorker interface extends this interface
 * so the methods of this interface must still be implemented.
 */
public interface DocumentWorker extends AutoCloseable
{
    /**
     * This method provides an opportunity for the worker to report if it has any problems which would prevent it processing documents
     * correctly. If the worker is healthy then it should simply return without calling the health monitor.
     *
     * @param healthMonitor used to report the health of the application
     */
    void checkHealth(HealthMonitor healthMonitor);

    /**
     * Processes a single document. Fields can be added or removed from the document.
     *
     * @param document the document to be processed
     * @throws InterruptedException if any thread has interrupted the current thread
     * @throws DocumentWorkerTransientException if the document could not be processed
     */
    void processDocument(Document document) throws InterruptedException, DocumentWorkerTransientException;

    /**
     * This method will be called when the worker is shutting down.<p>
     * It should be overridden by workers which hold resources that need to be released.
     *
     * @throws Exception if the worker's resources cannot be closed
     */
    @Override
    default void close() throws Exception
    {
    }
}