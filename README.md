# syjservicestn-expft
Responsible for communicating with Toll.no for Express Fortolling 

## Mission
This component retrieve data from tables realising input similiar to Toll.no UI and transform it into appropriate REST-calls.
It also include the JWT generation base on a certificate.


## Overall
1. First make call to OIDC, using customer enterprise certifikat for signing. Use the retrieved accessToken in API call. See DifiJwtCreator and CertManager.
2. Do the API call. Se ApiServices


## Dependencies
- A valid certificate for signing need to be located in /espedsg2/certificates/expft
- Mockito. Mockito is tool for enhanced tests. Download Jar from https://mvnrepository.com/artifact/org.mockito/mockito-all/2.0.2-beta
	also see __https://www.baeldung.com/injecting-mocks-in-spring__
