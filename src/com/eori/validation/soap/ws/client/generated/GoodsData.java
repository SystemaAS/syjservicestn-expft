
package com.eori.validation.soap.ws.client.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GoodsData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GoodsData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="taric.code" type="{http://eori.ws.eos.dds.s/}NotificationTARICCodeContentType"/>
 *         &lt;element name="description" type="{http://eori.ws.eos.dds.s/}LongTextContentType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GoodsData", propOrder = {
    "taricCode",
    "description"
})
public class GoodsData {

    @XmlElement(name = "taric.code", required = true)
    protected String taricCode;
    @XmlElement(required = true)
    protected String description;

    /**
     * Gets the value of the taricCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaricCode() {
        return taricCode;
    }

    /**
     * Sets the value of the taricCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaricCode(String value) {
        this.taricCode = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

}
