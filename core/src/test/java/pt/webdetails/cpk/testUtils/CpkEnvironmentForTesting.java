/*!
* Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

import pt.webdetails.cpf.impl.SimpleSessionUtils;
import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.ICpkEnvironment;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CpkEnvironmentForTesting implements ICpkEnvironment {

  private IPluginUtils pluginUtils;
  private IRepositoryAccess repAccess;
  final IUserSession userSession = new SimpleUserSession( "userName", new String[]{"administrator", "authenticated"}, true, null );


  private static final String[] reserverdWords = { "default", "refresh", "status", "reload", "getElementsList",
    "getSitemapJson", "version", "getPluginMetadata" };
  private HashSet<String> reservedWords;


  public CpkEnvironmentForTesting( IPluginUtils pluginUtils, IRepositoryAccess repAccess ) {
    this.pluginUtils = pluginUtils;
    this.repAccess = repAccess;
    this.reservedWords = new HashSet<String>( Arrays.asList( CpkEnvironmentForTesting.reserverdWords ) );
  }

  @Override
  public IPluginUtils getPluginUtils() {
    return pluginUtils;
  }

  @Override public IContentAccessFactory getContentAccessFactory() {
    return new ContentAccessFactoryForTesting( repAccess );
  }

  @Override
  public IAccessControl getAccessControl() {
    return new IAccessControl() {
      @Override
      public boolean isAllowed( IElement element ) {
        return true;
      }

      @Override
      public boolean isAdmin() {
        return true;
      }

      @Override
      public void throwAccessDenied( HttpServletResponse response ) {
        throw new UnsupportedOperationException( "Not supported yet." );
      }
    };
  }

  @Override
  public String getPluginName() {
    return pluginUtils.getPluginName();
  }

  @Override
  public Set<String> getReservedWords() {
    return this.reservedWords;
  }

  @Override
  public ISessionUtils getSessionUtils() {
    return new SimpleSessionUtils( userSession, null, null );
  }

  @Override public String getWebAppDir() {
    // TODO
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void reload() {
  }

}
