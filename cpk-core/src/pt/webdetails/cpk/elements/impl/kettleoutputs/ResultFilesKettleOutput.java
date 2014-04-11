/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cpk.elements.impl.kettleoutputs;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.vfs.KettleVFS;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;
import pt.webdetails.cpk.utils.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResultFilesKettleOutput extends KettleOutput {

  private String defaultMimeType;

  public ResultFilesKettleOutput( HttpServletResponse response, boolean download, String defaultMimeType ) {
    super( response, download );

    this.defaultMimeType = defaultMimeType;
  }

  @Override
  public void processResult( KettleResult result ) {
    logger.debug( "Process Result Files" );

    List<FileObject> files = new ArrayList<FileObject>(  );
    for ( ResultFile resultFile : result.getFiles() ) {
      files.add( resultFile.getFile() );
    }

    if ( files.isEmpty() ) {
      logger.warn( "Processing result files but no files found" );
      this.getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT  );
      return;
    }

    try {
      if ( files.size() == 1 && files.get( 0 ).getType() == FileType.FILE ) {
        // Singe file
        FileObject file = files.get( 0 );
        InputStream fileInputStream = KettleVFS.getInputStream( file );
        FileName fileName = file.getName();
        String mimeType = this.defaultMimeType != null ? this.defaultMimeType
          : MimeTypes.getMimeType( fileName.getBaseName() );

        if ( this.getDownload() ) {
          try {
            long attachmentSize = fileInputStream.available();
            this.sendAttached( KettleVFS.getInputStream( file ), mimeType, fileName.getBaseName(),
              attachmentSize );
          } catch ( IOException e ) {
            logger.error( "Failed setting attachment size.", e );
          }
        } else {
          this.sendDirectly( fileInputStream, mimeType );
        }

      } else {
        // More than one file, or folder
        // Build a zip / tar and ship it over!
        ZipUtil zip = new ZipUtil();
        zip.buildZipFromFileObjectList( files );

        this.sendAttached( zip.getZipInputStream(), MimeTypes.ZIP, zip.getZipNameToDownload(), zip.getZipSize() );
      }
    } catch ( FileSystemException ex ) {
      logger.error( "Failed sending files from kettle result.", ex );
    }
  }

  private void sendDirectly( InputStream file, String mimeTypes ) {
    CpkUtils.setResponseHeaders( this.getResponse(), mimeTypes );

    try {
      IOUtils.copy( file, this.getOut() );
      this.getOut().flush();
      file.close();
    } catch ( Exception ex ) {
      logger.error( "Failed to copy file to outputstream: " + ex );
    }
  }

  private void sendAttached( InputStream file, String mimeTypes, String fileName, long fileSize ) {
    CpkUtils.setResponseHeaders( this.getResponse(), mimeTypes, fileName, fileSize );

    try {
      IOUtils.copy( file, this.getOut() );
      this.getOut().flush();
      file.close();
    } catch ( IOException ex ) {
      this.logger.error( "Failed to copy file to outputstream.", ex );
    }
  }

}
