package no.systema.jservices.tvinn.expressfortolling;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DifiTokenClient {

  private static final String BODY_GRANT_TYPE =
      "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer";
  private static final String BODY_ASSERTION = "assertion=";

  private final String difiTokenRequestUrl;

  public DifiTokenClient(final String difiTokenRequestUrl) {
    this.difiTokenRequestUrl = difiTokenRequestUrl;
  }

  public String performTokenRequest(final String accessRequestJwt) throws IOException {
    HttpURLConnection connection = null;
    try {
      connection = prepareQuery();
      final String body = BODY_GRANT_TYPE + "&" + BODY_ASSERTION + accessRequestJwt;
      return preformQuery(connection, body);

    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private HttpURLConnection prepareQuery() throws IOException {
    final URL url = new URL(difiTokenRequestUrl);
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("User-Agent", "Toll testklient");
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    return connection;
  }

  private String preformQuery(final HttpURLConnection con, final String body) throws IOException {
    try (final DataOutputStream outputStream = new DataOutputStream(con.getOutputStream())) {
      outputStream.write(body.getBytes());
    }

    final StringBuilder response = new StringBuilder();

    try (final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

      String line;
      while ((line = in.readLine()) != null) {
        response.append(line);
        response.append(System.lineSeparator());
      }
    }
    return response.toString();
  }
}
