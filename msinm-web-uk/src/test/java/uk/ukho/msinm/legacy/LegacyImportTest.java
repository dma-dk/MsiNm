package uk.ukho.msinm.legacy;

import dk.dma.msinm.model.SeriesIdentifier;
import dk.dma.msinm.model.Type;
import dk.dma.msinm.vo.AreaVo;
import dk.dma.msinm.vo.MessageVo;
import org.junit.Test;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Test class for testing (de-)serializing messages
 */
public class LegacyImportTest {

    @Test
    public void testMarshallingMessages() throws Exception {

        Messages messages = new Messages();
        MessageVo msg = new MessageVo();
        messages.getMessages().add(msg);

        SeriesIdentifier id = new SeriesIdentifier();
        msg.setSeriesIdentifier(id);
        id.setAuthority("UKHO");
        id.setNumber(3421);
        id.setYear(2014);

        MessageVo.MessageDescVo desc = msg.createDesc("en");
        desc.setTitle("Data collection buoy");
        desc.setDescription("<p>Mariners are advised to navigate with caution in the area.</p>");

        msg.setType(Type.PRELIMINARY_NOTICE);

        AreaVo parentArea = new AreaVo();
        parentArea.createDesc("en").setName("WEST INDIES");
        AreaVo area = new AreaVo();
        area.createDesc("en").setName("Trinidad and Tobago");
        area.setParent(parentArea);
        msg.setArea(area);

        System.out.println(messages.toXml());
    }

    @Test
    public void testTransformingWeeklyXml() throws Exception {

        try (InputStream xsl = getClass().getResourceAsStream("/weekly.xsl");
             InputStream xml = getClass().getResourceAsStream("/Weekly.xml")){
            // First step is to perform an XSLT into a messages format
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xsl));
            StringWriter result = new StringWriter();
            transformer.transform(new StreamSource(xml), new StreamResult(result));

            System.out.println("RESULT:\n" + result.toString());
            System.out.println("Messages " + Messages.fromXml(new StringReader(result.toString())));
        }
    }

    @Test
    public void testUnmarshallingMessages() throws Exception {

        try (InputStream in = getClass().getResourceAsStream("/messages.xml")) {
            System.out.println(Messages.fromXml(new InputStreamReader(in)));
        }
    }
}