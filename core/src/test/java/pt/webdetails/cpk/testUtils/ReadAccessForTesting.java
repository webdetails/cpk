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
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.impl.FileBasedResourceAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReadAccessForTesting implements IReadAccess {

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

    if ( s == null ) {
      s = repAccess.getRepositoryFile( "", IRepositoryAccess.FileAccess.READ ).getFullPath();
    }

    final File file = new File ( s );

    return new IBasicFile () {

      public InputStream getContents() throws IOException{
        return new FileInputStream(file);
      }

      public String getName() {
        return file.getName();
      }

      public String getFullPath() {
        return file.getAbsolutePath();//TODO: . ..
      }

      public String getPath() {
        return file.getPath();
      }

      public String getExtension() {
        return RepositoryHelper.getExtension( getName() );
      }

      public boolean isDirectory() {
        return file.isDirectory();
      }

    };
  }

}
