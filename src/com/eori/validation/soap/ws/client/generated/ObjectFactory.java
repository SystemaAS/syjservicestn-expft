
package com.eori.validation.soap.ws.client.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.eori.validation.soap.ws.client.generated package. 
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

    private final static QName _ValidateEORI_QNAME = new QName("http://eori.ws.eos.dds.s/", "validateEORI");
    private final static QName _ValidateEORIResponse_QNAME = new QName("http://eori.ws.eos.dds.s/", "validateEORIResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.eori.validation.soap.ws.client.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ValidateEORIResponse }
     * 
     */
    public ValidateEORIResponse createValidateEORIResponse() {
        return new ValidateEORIResponse();
    }

    /**
     * Create an instance of {@link ValidateEORI }
     * 
     */
    public ValidateEORI createValidateEORI() {
        return new ValidateEORI();
    }

    /**
     * Create an instance of {@link EoriValidationResult }
     * 
     */
    public EoriValidationResult createEoriValidationResult() {
        return new EoriValidationResult();
    }

    /**
     * Create an instance of {@link GoodsData }
     * 
     */
    public GoodsData createGoodsData() {
        return new GoodsData();
    }

    /**
     * Create an instance of {@link Period }
     * 
     */
    public Period createPeriod() {
        return new Period();
    }

    /**
     * Create an instance of {@link CertificateType }
     * 
     */
    public CertificateType createCertificateType() {
        return new CertificateType();
    }

    /**
     * Create an instance of {@link Country }
     * 
     */
    public Country createCountry() {
        return new Country();
    }

    /**
     * Create an instance of {@link CertificateRef }
     * 
     */
    public CertificateRef createCertificateRef() {
        return new CertificateRef();
    }

    /**
     * Create an instance of {@link AuthorityRef }
     * 
     */
    public AuthorityRef createAuthorityRef() {
        return new AuthorityRef();
    }

    /**
     * Create an instance of {@link EoriResponse }
     * 
     */
    public EoriResponse createEoriResponse() {
        return new EoriResponse();
    }

    /**
     * Create an instance of {@link ExporterActivity }
     * 
     */
    public ExporterActivity createExporterActivity() {
        return new ExporterActivity();
    }

    /**
     * Create an instance of {@link CdValidityType }
     * 
     */
    public CdValidityType createCdValidityType() {
        return new CdValidityType();
    }

    /**
     * Create an instance of {@link AddressType }
     * 
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidateEORI }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eori.ws.eos.dds.s/", name = "validateEORI")
    public JAXBElement<ValidateEORI> createValidateEORI(ValidateEORI value) {
        return new JAXBElement<ValidateEORI>(_ValidateEORI_QNAME, ValidateEORI.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidateEORIResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://eori.ws.eos.dds.s/", name = "validateEORIResponse")
    public JAXBElement<ValidateEORIResponse> createValidateEORIResponse(ValidateEORIResponse value) {
        return new JAXBElement<ValidateEORIResponse>(_ValidateEORIResponse_QNAME, ValidateEORIResponse.class, null, value);
    }

}
