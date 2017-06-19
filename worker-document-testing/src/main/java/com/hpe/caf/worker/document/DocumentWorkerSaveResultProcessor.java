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

import com.hpe.caf.api.worker.DataStoreException;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.worker.testing.*;
import com.hpe.caf.worker.testing.data.ContentComparisonType;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DocumentWorkerSaveResultProcessor<TTestInput>
    extends OutputToFileProcessor<DocumentWorkerResult, TTestInput, DocumentWorkerTestExpectation>
{
    private TestConfiguration configuration;
    private final WorkerServices workerServices;

    public DocumentWorkerSaveResultProcessor(TestConfiguration<DocumentWorkerTask, DocumentWorkerResult, TTestInput, DocumentWorkerTestExpectation> configuration,
                                             WorkerServices workerServices)
    {
        super(workerServices.getCodec(), configuration.getWorkerResultClass(), configuration.getTestDataFolder());
        this.configuration = configuration;
        this.workerServices = workerServices;
    }

    @Override
    protected byte[] getOutputContent(DocumentWorkerResult workerResult,
                                      TaskMessage message,
                                      TestItem<TTestInput, DocumentWorkerTestExpectation> testItem) throws Exception
    {
        testItem.getExpectedOutputData().setResult(convert(workerResult, testItem));
        return getSerializedTestItem(testItem, configuration);
    }

    private DocumentWorkerResultExpectation convert(final DocumentWorkerResult result,
                                                    TestItem<TTestInput, DocumentWorkerTestExpectation> testItem)
    {
        final DocumentWorkerResultExpectation expectation = new DocumentWorkerResultExpectation();
        if (result.fieldChanges != null) {
            expectation.fieldChanges = convert(result.fieldChanges, testItem);
        }
        if (result.failures != null) {
            expectation.failures = result.failures;
        }
        return expectation;
    }

    private Map<String, DocumentWorkerFieldChangesExpectation> convert(final Map<String, DocumentWorkerFieldChanges> fieldChanges,
                                                                       final TestItem<TTestInput, DocumentWorkerTestExpectation> testItem)
    {
        return fieldChanges.entrySet().stream().collect(
            Collectors.toMap(entry -> entry.getKey(),
                             entry -> convert(entry.getValue(), entry.getKey(), testItem)));
    }

    private DocumentWorkerFieldChangesExpectation convert(final DocumentWorkerFieldChanges fieldChange,
                                                          final String fieldName,
                                                          final TestItem<TTestInput, DocumentWorkerTestExpectation> testItem)
    {
        final DocumentWorkerFieldChangesExpectation expectation = new DocumentWorkerFieldChangesExpectation();
        expectation.action = fieldChange.action;
        expectation.values = fieldChange.values.stream().map(value -> convert(value, fieldName, testItem)).collect(Collectors.toList());
        return expectation;
    }

    private DocumentWorkerFieldValueExpectation convert(final DocumentWorkerFieldValue fieldValue,
                                                        final String fieldName,
                                                        final TestItem<TTestInput, DocumentWorkerTestExpectation> testItem)
    {
        final DocumentWorkerFieldValueExpectation expectation = new DocumentWorkerFieldValueExpectation();
        expectation.encoding = fieldValue.encoding;
        if (fieldValue.encoding == DocumentWorkerFieldEncoding.storage_ref) {
            expectation.content = getContentExpectation(fieldValue.data, fieldName, testItem);
            expectation.data = null;
        } else {
            expectation.content = null;
            expectation.data = fieldValue.data;
        }
        return expectation;
    }

    private ContentFileTestExpectation getContentExpectation(final String storageRef,
                                                             final String fieldName,
                                                             final TestItem<TTestInput, DocumentWorkerTestExpectation> testItem)
    {
        final ContentFileTestExpectation expectation = new ContentFileTestExpectation();
        try (InputStream dataStream = workerServices.getDataStore().retrieve(storageRef)) {
            final Path contentFile = saveContentFile(testItem, testItem.getTag(), fieldName, dataStream);
            expectation.setExpectedContentFile(contentFile.toString());
            expectation.setComparisonType(ContentComparisonType.BINARY);
            expectation.setExpectedSimilarityPercentage(100);
        } catch (DataStoreException | IOException e) {
            System.out.print("Failed To get data stream :" + e.toString());
        }
        return expectation;
    }

    protected Path saveContentFile(final TestItem<TTestInput, DocumentWorkerTestExpectation> testItem,
                                   String baseFileName,
                                   final String extension,
                                   final InputStream dataStream) throws IOException
    {
        String outputFolder = getOutputFolder();
        if (configuration.isStoreTestCaseWithInput()) {
            final Path path = Paths.get(testItem.getTag()).getParent();
            outputFolder = Paths.get(configuration.getTestDataFolder(), path == null ? "" : path.toString()).toString();
        }
        String normalizedBaseFileName = Paths.get(FilenameUtils.normalize(baseFileName)).getFileName().toString();
        final Path contentFile = Paths.get(outputFolder, normalizedBaseFileName + "." + extension + ".content");
        Files.deleteIfExists(contentFile);
        Files.copy(dataStream, contentFile, REPLACE_EXISTING);
        return getRelativeLocation(contentFile);
    }

    protected Path getRelativeLocation(final Path contentFile)
    {
        final Path relative = Paths.get(configuration.getTestDataFolder()).relativize(contentFile);
        return relative;
    }
}
