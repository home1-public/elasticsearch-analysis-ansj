package org.ansj.elasticsearch;

import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.rest.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.ansj.AnsjUtils.linesWithoutBlankAndComments;
import static org.ansj.elasticsearch.AnsjAnalysisPluginContext.PLUGIN_CONTEXT;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.*;

/**
 * by zhl on 15/7/21.
 */
public class AnsjRestHandler extends BaseRestHandler {

    @Inject
    public AnsjRestHandler(
            final Settings settings,
            final RestController restController,
            final Client client //save the client for searching later
    ) {
        super(settings, restController, client);
        logger.info("restHandler client: {}", client);
        restController.registerHandler(GET, "/_ansj/info", this);
        restController.registerHandler(POST, "/_ansj/upload", this);
        restController.registerHandler(POST, "/_ansj/commit", this);
    }

    @Override
    protected void handleRequest(final RestRequest request, final RestChannel channel, final Client client) throws Exception {
        this.triggerAnalysisInitialize(client);

        logger.info("ansj path: {}", request.path());
        final AnsjAnalysisPluginContext pluginContext = PLUGIN_CONTEXT();
        logger.info("ansj pluginContext: {}", pluginContext);

        switch (request.method()) {
            case GET:
                switch (request.path()) {
                    case "/_ansj/info":
                        channel.sendResponse(new BytesRestResponse(OK, pluginContext.toString()));
                        break;
                    default:
                        unsupportedOperation(request, channel);
                        break;
                }
                break;
            case POST:
                switch (request.path()) {
                    case "/_ansj/upload": {
                        final String hash = getHash(request);
                        logger.info("upload hash: {}", hash);
                        if (isNotBlank(hash)) {
                            final File outFile = dumpFile(pluginContext, hash, request.content().streamInput());
                            if (outFile != null) {
                                channel.sendResponse(new BytesRestResponse(CREATED, outFile.getName()));
                            } else {
                                unsupportedOperation(request, channel);
                            }
                        } else {
                            unsupportedOperation(request, channel);
                        }
                    }
                    break;
                    case "/_ansj/commit": {
                        final String hash = getHash(request);
                        logger.info("commit hash: {}", hash);
                        if (isNotBlank(hash)) {
                            if (commitAndClear(pluginContext, hash, request.content().streamInput())) {
                                channel.sendResponse(new BytesRestResponse(ACCEPTED, ""));
                            } else {
                                unsupportedOperation(request, channel);
                            }
                        } else {
                            unsupportedOperation(request, channel);
                        }
                    }
                    break;
                    default:
                        unsupportedOperation(request, channel);
                        break;
                }
                break;
            case PUT:
                unsupportedOperation(request, channel);
                break;
            case DELETE:
                unsupportedOperation(request, channel);
                break;
            default:
                unsupportedOperation(request, channel);
                break;
        }
    }

    @SneakyThrows
    File dumpFile(final AnsjAnalysisPluginContext pluginContext, final String hash, final InputStream inputStream) {
        if (isBlank(hash)) {
            return null;
        }
        final File parent = uploadLocation(pluginContext);
        logger.info("dumpFile hash: {}", hash);
        logger.info("dumpFile parent: {}", parent);
        if (!parent.exists() && !parent.mkdirs()) {
            return null;
        }
        if (!parent.canWrite()) {
            return null;
        }
        final byte[] data = getData(hash, inputStream);
        logger.info("dumpFile data.length: {}", data != null ? data.length : -1);
        if (data == null) {
            return null;
        }
        // TODO 预解析 检查词典内容是否合法
        final File outFile = new File(parent, hash.toUpperCase());
        logger.info("dumpFile write {} bytes into {}", data.length, outFile);
        write(data, new FileOutputStream(outFile));
        try {
            setFilePremissions(outFile);
            return outFile;
        } catch (final Exception e) {
            deleteQuietly(outFile);
            return null;
        }
    }

    boolean commitAndClear(final AnsjAnalysisPluginContext pluginContext, final String hash, final InputStream inputStream) {
        final boolean result = commit(pluginContext, hash, inputStream);
        deleteQuietly(uploadLocation(pluginContext));
        if (result) {
            pluginContext.refresh();
        }
        return result;
    }

    boolean commit(final AnsjAnalysisPluginContext pluginContext, final String hash, final InputStream inputStream) {
        final byte[] data = getData(hash, inputStream);
        logger.info("commit data.length{}", data != null ? data.length : -1);
        if (data == null) {
            return false;
        }
        final File uploadLocation = uploadLocation(pluginContext);
        final File dicRoot = dicRoot(pluginContext);
        final Map<File, File> commit = newLinkedHashMap();
        for (final String line : linesWithoutBlankAndComments(new ByteArrayInputStream(data), l -> l)) {
            final String[] hashAndTarget = line.split("\\s+");
            if (hashAndTarget.length != 2) {
                return false;
            }
            final File source = new File(uploadLocation, hashAndTarget[0].toUpperCase());
            final File target = new File(dicRoot, hashAndTarget[1]);
            logger.info("commit prepare source: {}, target: {}", source, target);
            if (!source.exists() || !source.canRead()) {
                return false;
            }
            commit.put(source, target);
        }
        if (commit.size() == 0) {
            return false;
        }
        for (final Map.Entry<File, File> entry : commit.entrySet()) {
            final File from = entry.getKey();
            final File to = entry.getValue();
            try {
                logger.info("commit move from: {}, to: {}", from, to);
                Files.move(from.toPath(), to.toPath(), REPLACE_EXISTING);
                setFilePremissions(to);
            } catch (final Exception e) {
                logger.warn("error move file.", e);
                deleteQuietly(to);
                return false;
            }
        }
        return true;
    }

    @SneakyThrows
    byte[] getData(final String hash, final InputStream inputStream) {
        final byte[] data = toByteArray(inputStream);
        final String sha1 = sha1Hex(data);
        if (sha1.toUpperCase().equals(hash.toUpperCase())) {
            return data;
        } else {
            return null;
        }
    }

    @SneakyThrows
    void setFilePremissions(final File file) {
        Files.setPosixFilePermissions(file.toPath(), newHashSet(OWNER_READ, OWNER_WRITE));
    }

    File uploadLocation(final AnsjAnalysisPluginContext pluginContext) {
        return new File(pluginContext.userLibraryLocation, "upload");
    }

    File dicRoot(final AnsjAnalysisPluginContext pluginContext) {
        return new File(pluginContext.userLibraryLocation).getParentFile();
    }

    String getHash(final RestRequest request) {
        return isNotBlank(request.header("hash")) ? request.header("hash") : request.param("hash");
    }

    void unsupportedOperation(final RestRequest request, final RestChannel channel) {
        channel.sendResponse(new BytesRestResponse(BAD_REQUEST, "unsupportedOperation"));
    }

    void triggerAnalysisInitialize(final Client client) {
        try {
            final CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate("warmup").execute().actionGet();
            logger.info("createIndex: {}", createIndexResponse);
        } catch (final IndexAlreadyExistsException e) {
            logger.info("index 'warmup' already exists.");
        }
    }
}
