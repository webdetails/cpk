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

import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;


public class ContentAccessFactoryForTesting implements IContentAccessFactory {

  private IRepositoryAccess repAccess;

  public ContentAccessFactoryForTesting( IRepositoryAccess repAccess ) {
    this.repAccess = repAccess;
  }

  @Override public IUserContentAccess getUserContentAccess( String s ) {
    return new UserContentAccessForTesting( repAccess );
  }

  @Override public IReadAccess getPluginSystemReader( String s ) {
    return new ReadAccessForTesting( repAccess );
  }

  @Override public IReadAccess getPluginRepositoryReader( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public IRWAccess getPluginRepositoryWriter( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public IRWAccess getPluginSystemWriter( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public IReadAccess getOtherPluginSystemReader( String s, String s2 ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public IRWAccess getOtherPluginSystemWriter( String s, String s2 ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
