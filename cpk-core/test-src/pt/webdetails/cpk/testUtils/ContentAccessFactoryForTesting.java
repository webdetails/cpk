package pt.webdetails.cpk.testUtils;

import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: joao Date: 11/22/13 Time: 12:33 PM To change this template use File | Settings |
 * File Templates.
 */
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
