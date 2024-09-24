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
