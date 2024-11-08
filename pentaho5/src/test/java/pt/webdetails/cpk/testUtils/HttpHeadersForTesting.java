/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package pt.webdetails.cpk.testUtils;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

public class HttpHeadersForTesting implements HttpHeaders {
  @Override
  public List<String> getRequestHeader( String name ) {
    return null;
  }

  @Override
  public String getHeaderString(String name) {
    return "";
  }

  @Override
  public MultivaluedMap<String, String> getRequestHeaders() {
    return null;
  }

  @Override
  public List<MediaType> getAcceptableMediaTypes() {
    return null;
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return null;
  }

  @Override
  public MediaType getMediaType() {
    return null;
  }

  @Override
  public Locale getLanguage() {
    return null;
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return null;
  }

  @Override
  public Date getDate() {
    return new Date();
  }

  @Override
  public int getLength() {
    return 0;
  }
}
