package eu.scapeproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.purl.dc.elements._1.ElementContainer;
import org.purl.dc.elements._1.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scapeproject.model.Identifier;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.util.ScapeMarshaller;

public class ConnectorAPIMockTest {

    private static final ConnectorAPIMock MOCK = new ConnectorAPIMock(8387);
    private static final ConnectorAPIUtil UTIL = new ConnectorAPIUtil("http://localhost:8387");
    private static final HttpClient CLIENT = new DefaultHttpClient();
    private static final Logger log = LoggerFactory.getLogger(ConnectorAPIMockTest.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Thread t = new Thread(MOCK);
        t.start();
        while (!MOCK.isRunning()) {
            Thread.sleep(10);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MOCK.stop();
        MOCK.close();
        assertFalse(MOCK.isRunning());
    }

    public ElementContainer createDCElementContainer(){
        ElementContainer c = new ElementContainer();

        SimpleLiteral title = new SimpleLiteral();
        title.getContent().add("A test entity");
        c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "title"), SimpleLiteral.class, title));

        SimpleLiteral date = new SimpleLiteral();
        date.getContent().add(dateFormat.format(new Date()));
        c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "created"), SimpleLiteral.class, date));

        SimpleLiteral lang = new SimpleLiteral();
        lang.getContent().add("en");
        c.getAny().add(new JAXBElement<SimpleLiteral>(new QName("http://purl.org/dc/elements/1.1/", "created"), SimpleLiteral.class, lang));

        return c;
    }


    @Test
    public void testIngestAndRetrieveMinimalIntellectualEntity() throws Exception {
        IntellectualEntity ie = new IntellectualEntity.Builder()
                .identifier(new Identifier(UUID.randomUUID().toString()))
                .descriptive(createDCElementContainer())
                .build();
        HttpPost post = UTIL.createPostEntity(ie);
        HttpResponse resp = CLIENT.execute(post);
        post.releaseConnection();
        assertTrue(resp.getStatusLine().getStatusCode() == 201);

        HttpGet get = UTIL.createGetEntity(ie.getIdentifier().getValue());
        resp = CLIENT.execute(get);
        assertTrue(resp.getStatusLine().getStatusCode() == 200);
        IntellectualEntity fetched = ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, resp.getEntity().getContent());
        get.releaseConnection();
        assertEquals(ie.getIdentifier().getValue(), fetched.getIdentifier().getValue());
        assertEquals(ie.getDescriptive().getClass(), fetched.getDescriptive().getClass());
    }

}
