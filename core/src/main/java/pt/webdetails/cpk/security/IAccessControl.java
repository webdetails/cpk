/*!
* Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

package pt.webdetails.cpk.security;

import pt.webdetails.cpk.elements.IElement;

import jakarta.servlet.http.HttpServletResponse;

public interface IAccessControl {
  public boolean isAllowed( IElement element );

  public boolean isAdmin();


  //Bloody stupid name
  public void throwAccessDenied( HttpServletResponse response );

}
