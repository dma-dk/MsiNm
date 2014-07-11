package uk.ukho.msinm.legacy;


import dk.dma.msinm.vo.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The JAX-B implementation of the time-result½ format
 */
@XmlRootElement(name="messages")
@XmlAccessorType(XmlAccessType.FIELD)
public class Messages {

    @XmlElement(name="message")
    private List<MessageVo> messages = new ArrayList<>();

    /**
     * Creates a new JAXB context
     * @return a new JAXB context
     */
    public static JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(
                Messages.class, MessageVo.class, MessageVo.MessageDescVo.class,
                AreaVo.class, AreaVo.AreaDescVo.class, LocationVo.class, LocationVo.LocationDescVo.class,
                PointVo.class, PointVo.PointDescVo.class, CategoryVo.class, CategoryVo.CategoryDescVo.class
        );
    }

    /**
     * Returns an XML representation of this model
     * @return an XML representation of this model
     */
    public String toXml() throws JAXBException {

        JAXBContext jc = getJAXBContext();
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter out = new StringWriter();
        marshaller.marshal(this, out);
        return out.toString();
    }

    /**
     * Unmarshals an xml input stream
     * @param reader the input stream
     * @return the unmarshalled Messages
     */
    public static Messages fromXml(Reader reader) throws JAXBException {
        JAXBContext jc = getJAXBContext();
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Messages) unmarshaller.unmarshal(reader);
    }

    public List<MessageVo> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageVo> messages) {
        this.messages = messages;
    }
}
