
package eu.scapeproject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scapeproject.util.ScapeMarshaller;

public class MockContainer implements Container {

    private static final Logger LOG = LoggerFactory
            .getLogger(MockContainer.class);

    private final PosixStorage storage;

    private final LuceneIndex index;

    private final int asyncIngestDelay = 1000;

    private final Map<String, String> fileIdMap = new HashMap<String, String>();

    private final Map<Long, Object> asyncIngestMap =
            new HashMap<Long, Object>();

    private final AsyncIngester asyncIngester = new AsyncIngester();

    private final int port;

    private final ScapeMarshaller marshaller;

    private Thread asyncIngesterThread = new Thread();

    public MockContainer(String path, int port)
            throws JAXBException {
        this.storage = new PosixStorage(path);
        this.index = new LuceneIndex();
        this.port = port;
        this.marshaller = ScapeMarshaller.newInstance();

    }

    public void close() throws Exception {
        this.asyncIngester.stop();
        this.asyncIngesterThread.join();
        this.purgeStorage();
        this.index.close();
    }


    @Override
    public void handle(Request req, Response resp) {
        try {
            if (req.getMethod().equals("POST")) {
                handlePost(req, resp);
            } else if (req.getMethod().equals("DELETE")) {

            } else if (req.getMethod().equals("PUT")) {
                handlePut(req, resp);
            } else if (req.getMethod().equals("GET")) {
                handleGet(req, resp);
            } else {
                LOG.error("Unable to handle method of type " + req.getMethod());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGet(Request req, Response resp) throws IOException {
        String contextPath = req.getPath().getPath();
        LOG.info("-- HTTP/1.1 GET " + contextPath + " from " +
                req.getClientAddress().getAddress().getHostAddress());
        try {
            if (contextPath.startsWith("/entity/")) {
//                handleRetrieveEntity(req, resp);
            } else if (contextPath.startsWith("/metadata/")) {
//                handleRetrieveMetadata(req, resp);
            } else if (contextPath.startsWith("/representation/")) {
//                handleRetrieveRepresentation(req, resp);
            } else if (contextPath.startsWith("/entity-version-list/")) {
//                handleRetrieveVersionList(req, resp);
            } else if (contextPath.startsWith("/sru/entities")) {
//                handleEntitySRU(req, resp);
            } else if (contextPath.startsWith("/sru/representations")) {
//                handleRepresentationSRU(req, resp);
            } else if (contextPath.startsWith("/file/")) {
//                handleRetrieveFile(req, resp);
            } else if (contextPath.startsWith("/bitstream/")) {
//                handleRetrieveBitStream(req, resp);
            } else if (contextPath.startsWith("/lifecycle/")) {
//                handleRetrieveLifecycleState(req, resp);
            } else {
                resp.setCode(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resp.close();
        }
    }

    private void handlePost(Request req, Response resp) throws IOException {
        String contextPath = req.getPath().getPath();
        LOG.info("-- HTTP/1.1 POST " + contextPath + " from " +
                req.getClientAddress().getAddress().getHostAddress());
        try {
            if (contextPath.startsWith("/entity-async")) {
//                handleAsyncIngest(req, resp, 200);
            } else if (contextPath.equals("/entity")) {
//                handleIngest(req, resp, 201);
            } else if (contextPath.startsWith("/entity-list")) {
//                handleRetrieveEntityList(req, resp);
            } else {
                resp.setCode(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resp.close();
        }
    }

    private void handlePut(Request req, Response resp) throws IOException {
        String contextPath = req.getPath().getPath();
        LOG.info("-- HTTP/1.1 PUT " + contextPath + " from " +
                req.getClientAddress().getAddress().getHostAddress());
        try {
            if (contextPath.startsWith("/entity/")) {
//                handleUpdateEntity(req, resp);
            } else if (contextPath.startsWith("/representation/")) {
//                handleUpdateRepresentation(req, resp);
            } else if (contextPath.startsWith("/metadata/")) {
//                handleUpdateMetadata(req, resp);
            } else {
                resp.setCode(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resp.close();
        }

    }

    public void purgeStorage() throws Exception {
        storage.purge();
    }

    public void start() {
        this.asyncIngesterThread = new Thread(asyncIngester);
        this.asyncIngesterThread.start();
    }

    public class AsyncIngester implements Runnable {

        private boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                for (Entry<Long, Object> asyncRequest : asyncIngestMap
                        .entrySet()) {
                    if (new Date().getTime() >= asyncRequest.getKey()) {
                        try {
                            LOG.info("ingesting object at " +
                                    asyncRequest.getKey());
//                            ingestObject(asyncRequest.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            asyncIngestMap.remove(asyncRequest.getKey());
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stop = true;
                }
            }
        }

        public synchronized void stop() {
            stop = true;
        };
    }
}
