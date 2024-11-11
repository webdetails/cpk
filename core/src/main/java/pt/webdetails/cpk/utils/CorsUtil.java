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

import pt.webdetails.cpf.utils.AbstractCorsUtil;
import pt.webdetails.cpf.utils.CsvUtil;
import pt.webdetails.cpk.CpkEngine;

import java.util.Collection;

/**
 * CPK CorsUtil implementation
 */
public class CorsUtil extends AbstractCorsUtil {

  private static CorsUtil instance;

  public static CorsUtil getInstance() {
    if ( instance == null ) {
      instance = new CorsUtil();
    }
    return instance;
  }

  /**
   * Retrieves a flag value from a plugin settings.xml
   * @return true if the flag is present and CORS is allowed, otherwise returns false
   */
  @Override protected boolean isCorsAllowed() {
    return "true".equalsIgnoreCase( CpkEngine.getInstance().getCorsAllowed() );
  }

  /**
   *
   * @return
   */
  @Override protected Collection<String> getDomainWhitelist() {
    return CsvUtil.parseCsvString( CpkEngine.getInstance().getDomainWhitelist() );
  }
}
