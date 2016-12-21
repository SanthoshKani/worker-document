package com.hpe.caf.worker.document.impl;

import com.hpe.caf.api.Codec;
import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.api.worker.DataStore;
import com.hpe.caf.api.worker.WorkerException;
import com.hpe.caf.worker.document.DocumentWorkerConfiguration;
import com.hpe.caf.worker.document.model.Application;
import com.hpe.caf.worker.document.model.ServiceLocator;
import java.util.Objects;

public final class ApplicationImpl implements Application
{
    private final ServiceLocatorImpl serviceLocator;
    private final ConfigurationSource configSource;
    private final DataStore dataStore;
    private final Codec codec;
    private final DocumentWorkerConfiguration configuration;
    private final BatchSizeControllerImpl batchSizeController;

    public ApplicationImpl(final ConfigurationSource configSource, final DataStore dataStore, final Codec codec)
        throws WorkerException
    {
        this.serviceLocator = new ServiceLocatorImpl(this);
        this.configSource = Objects.requireNonNull(configSource);
        this.dataStore = Objects.requireNonNull(dataStore);
        this.codec = Objects.requireNonNull(codec);
        this.configuration = getConfiguration(configSource);
        this.batchSizeController = new BatchSizeControllerImpl(this, configuration);

        // Register services
        serviceLocator.register(DataStore.class, dataStore);
        serviceLocator.register(ConfigurationSource.class, configSource);
    }

    @Override
    public Application getApplication()
    {
        return this;
    }

    @Override
    public BatchSizeControllerImpl getBatchSizeController()
    {
        return batchSizeController;
    }

    @Override
    public <S> S getService(Class<S> service)
    {
        return serviceLocator.getService(service);
    }

    @Override
    public ServiceLocator getServiceLocator()
    {
        return serviceLocator;
    }

    public ConfigurationSource getConfigSource()
    {
        return configSource;
    }

    public DataStore getDataStore()
    {
        return dataStore;
    }

    public Codec getCodec()
    {
        return codec;
    }

    public DocumentWorkerConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * This method retrieves the DocumentWorkerConfiguration or throws an exception.
     *
     * @return the DocumentWorkerConfiguration object
     * @throws WorkerException if there is a problem creating the configuration object
     */
    private static DocumentWorkerConfiguration getConfiguration(final ConfigurationSource configSource)
        throws WorkerException
    {
        try {
            return configSource.getConfiguration(DocumentWorkerConfiguration.class);
        } catch (ConfigurationException ce) {
            throw new WorkerException("Failed to construct DocumentWorkerConfiguration object", ce);
        }
    }
}