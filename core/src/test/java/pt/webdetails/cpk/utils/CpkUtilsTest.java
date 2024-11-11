/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
