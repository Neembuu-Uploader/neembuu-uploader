
package neembuu.uploader.uploaders.api._4shared;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for faultBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faultBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="details" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exceptionClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faultBean", propOrder = {
    "details",
    "exceptionClass"
})
public class FaultBean {

    protected String details;
    protected String exceptionClass;

    /**
     * Gets the value of the details property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the value of the details property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetails(String value) {
        this.details = value;
    }

    /**
     * Gets the value of the exceptionClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExceptionClass() {
        return exceptionClass;
    }

    /**
     * Sets the value of the exceptionClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExceptionClass(String value) {
        this.exceptionClass = value;
    }

}
