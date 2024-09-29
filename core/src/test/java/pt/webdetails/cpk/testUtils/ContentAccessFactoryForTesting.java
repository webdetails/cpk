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
