package pt.webdetails.cpk.testUtils;

import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IReadAccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: joao Date: 11/22/13 Time: 12:33 PM To change this template use File | Settings |
 * File Templates.
 */
public class ReadAccessForTesting implements IReadAccess{

  private IRepositoryAccess repAccess;

  public ReadAccessForTesting( IRepositoryAccess repAccess ) {
    this.repAccess = repAccess;
  }

  @Override public InputStream getFileInputStream( String s ) throws IOException {
    return new ByteArrayInputStream( repAccess.getSettingsResourceAsString( "cpk.xml" ).getBytes() );
  }

  @Override public boolean fileExists( String s ) {
    return true;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public long getLastModified( String s ) {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b,
                                     boolean b2 ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override public IBasicFile fetchFile( String s ) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

}
