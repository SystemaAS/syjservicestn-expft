
package com.eori.validation.soap.ws.client.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CertificateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CertificateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="certificate.type.code" type="{http://eori.ws.eos.dds.s/}CertificateTypeCode"/>
 *         &lt;element name="certificate.type.name" type="{http://eori.ws.eos.dds.s/}M_SingleLineString150"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CertificateType", propOrder = {
    "certificateTypeCode",
    "certificateTypeName"
})
public class CertificateType {

    @XmlElement(name = "certificate.type.code", required = true)
    protected String certificateTypeCode;
    @XmlElement(name = "certificate.type.name", required = true)
    protected String certificateTypeName;

    /**
     * Gets the value of the certificateTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateTypeCode() {
        return certificateTypeCode;
    }

    /**
     * Sets the value of the certificateTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateTypeCode(String value) {
        this.certificateTypeCode = value;
    }

    /**
     * Gets the value of the certificateTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificateTypeName() {
        return certificateTypeName;
    }

    /**
     * Sets the value of the certificateTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificateTypeName(String value) {
        this.certificateTypeName = value;
    }

}
