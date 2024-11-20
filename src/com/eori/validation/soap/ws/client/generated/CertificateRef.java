
package com.eori.validation.soap.ws.client.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CertificateRef complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CertificateRef">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="country" type="{http://eori.ws.eos.dds.s/}Country"/>
 *         &lt;element name="aeo.certificate.type" type="{http://eori.ws.eos.dds.s/}CertificateType"/>
 *         &lt;element name="national.number" type="{http://eori.ws.eos.dds.s/}SingleLineString29"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CertificateRef", propOrder = {
    "country",
    "aeoCertificateType",
    "nationalNumber"
})
public class CertificateRef {

    @XmlElement(required = true)
    protected Country country;
    @XmlElement(name = "aeo.certificate.type", required = true)
    protected CertificateType aeoCertificateType;
    @XmlElement(name = "national.number", required = true)
    protected String nationalNumber;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link Country }
     *     
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link Country }
     *     
     */
    public void setCountry(Country value) {
        this.country = value;
    }

    /**
     * Gets the value of the aeoCertificateType property.
     * 
     * @return
     *     possible object is
     *     {@link CertificateType }
     *     
     */
    public CertificateType getAeoCertificateType() {
        return aeoCertificateType;
    }

    /**
     * Sets the value of the aeoCertificateType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CertificateType }
     *     
     */
    public void setAeoCertificateType(CertificateType value) {
        this.aeoCertificateType = value;
    }

    /**
     * Gets the value of the nationalNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNationalNumber() {
        return nationalNumber;
    }

    /**
     * Sets the value of the nationalNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationalNumber(String value) {
        this.nationalNumber = value;
    }

}
