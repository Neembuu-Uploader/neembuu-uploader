package neembuu.uploader.uploaders.api._4shared;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.pmstation.shared.soap.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ApiException_QNAME = new QName("http://api.soap.shared.pmstation.com/", "ApiException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.pmstation.shared.soap.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FaultBean }
     * 
     */
    public FaultBean createFaultBean() {
        return new FaultBean();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FaultBean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://api.soap.shared.pmstation.com/", name = "ApiException")
    public JAXBElement<FaultBean> createApiException(FaultBean value) {
        return new JAXBElement<FaultBean>(_ApiException_QNAME, FaultBean.class, null, value);
    }
}
