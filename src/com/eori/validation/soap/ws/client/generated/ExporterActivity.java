
package com.eori.validation.soap.ws.client.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExporterActivity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExporterActivity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activity.name" type="{http://eori.ws.eos.dds.s/}MainActivityContentType"/>
 *         &lt;element name="activity.flag" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExporterActivity", propOrder = {
    "activityName",
    "activityFlag"
})
public class ExporterActivity {

    @XmlElement(name = "activity.name", required = true)
    protected String activityName;
    @XmlElement(name = "activity.flag")
    protected boolean activityFlag;

    /**
     * Gets the value of the activityName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * Sets the value of the activityName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityName(String value) {
        this.activityName = value;
    }

    /**
     * Gets the value of the activityFlag property.
     * 
     */
    public boolean isActivityFlag() {
        return activityFlag;
    }

    /**
     * Sets the value of the activityFlag property.
     * 
     */
    public void setActivityFlag(boolean value) {
        this.activityFlag = value;
    }

}
