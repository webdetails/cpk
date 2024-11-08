/*!
 * Copyright 2019-2024 Webdetails, a Hitachi Vantara company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
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
