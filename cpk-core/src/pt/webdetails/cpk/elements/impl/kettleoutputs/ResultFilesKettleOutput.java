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
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.vfs.KettleVFS;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;
import pt.webdetails.cpk.utils.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultFilesKettleOutput extends KettleOutput {

  public ResultFilesKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );
  }

  @Override
  public void processResult( KettleResult result ) {
    super.processResult( result );
    try {
      logger.debug( "Process Result Files" );

      List<FileObject> files = new ArrayList<FileObject>(  );
      for ( ResultFile resultFile : result.getFiles() ) {
        files.add( resultFile.getFile() );
      }

      HttpServletResponse response = this.getResponse();

      if ( files.isEmpty() ) {
        logger.warn( "Processing result files but no files found" );
        this.getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT  );
        return;
      } else if ( files.size() == 1 && files.get( 0 ).getType() == FileType.FILE ) {
        // Singe file? Just write it to the outputstream
        FileObject file = files.get( 0 );

        // Do we know the mime type?
        String mimeType = MimeTypes.getMimeType( file.getName().getExtension() );
        if ( this.getDownload() ) {
          try {
            long attachmentSize = file.getContent().getInputStream().available();
            CpkUtils.setResponseHeaders( response, mimeType, file.getName().getBaseName(),
              attachmentSize );
          } catch ( Exception e ) {
            logger.error( "Problem setting the attachment size: " + e );
          }
        } else {
          // set Mimetype only
          CpkUtils.setResponseHeaders( response, mimeType );
        }


        try {
          IOUtils.copy( KettleVFS.getInputStream( file ), response.getOutputStream() );
        } catch ( Exception ex ) {
          logger.warn( "Failed to copy file to outputstream: " + Util.getExceptionDescription( ex ) );
        }

      } else {
        // Build a zip / tar and ship it over!

        ZipUtil zip = new ZipUtil();
        zip.buildZipFromFileObjectList( files );

        CpkUtils.setResponseHeaders( response, MimeTypes.ZIP, zip.getZipNameToDownload(), zip.getZipSize() );
        try {
          IOUtils.copy( zip.getZipInputStream(), this.getOut() );
          zip.closeInputStream();
        } catch ( IOException ex ) {
          this.logger.error( "Failed to copy file to outputstream.", ex );
        }


      }
    } catch ( FileSystemException ex ) {
      Logger.getLogger( ResultFilesKettleOutput.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

}
