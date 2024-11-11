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


package pt.webdetails.cpk.security;

import pt.webdetails.cpk.elements.IElement;

import javax.servlet.http.HttpServletResponse;

public interface IAccessControl {
  public boolean isAllowed( IElement element );

  public boolean isAdmin();


  //Bloody stupid name
  public void throwAccessDenied( HttpServletResponse response );

}
