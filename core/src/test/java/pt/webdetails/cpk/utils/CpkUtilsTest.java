/*!
 * Copyright 2019 Webdetails, a Hitachi Vantara company.  All rights reserved.
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
package pt.webdetails.cpk.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class CpkUtilsTest {

  @Test
  public void testStatusCodeEquivalents() {
    OutputStream out = new ByteArrayOutputStream();
    HttpServletResponse httpResponse = new HttpServletResponseForTesting( out );

    httpResponse.setStatus( HttpServletResponse.SC_OK );
    Response.Status respStatus = CpkUtils.getEquivalentStatusFromHttpServletResponse( httpResponse );
    assertEquals( Response.Status.OK, respStatus );

    httpResponse.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    respStatus = CpkUtils.getEquivalentStatusFromHttpServletResponse( httpResponse );
    assertEquals( Response.Status.INTERNAL_SERVER_ERROR, respStatus );

    httpResponse.setStatus( HttpServletResponse.SC_NO_CONTENT );
    respStatus = CpkUtils.getEquivalentStatusFromHttpServletResponse( httpResponse );
    assertEquals( Response.Status.NO_CONTENT, respStatus );

    httpResponse.setStatus( HttpServletResponse.SC_FORBIDDEN );
    respStatus = CpkUtils.getEquivalentStatusFromHttpServletResponse( httpResponse );
    assertEquals( Response.Status.FORBIDDEN, respStatus );
  }
}
