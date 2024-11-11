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


package pt.webdetails.cpk.elements.impl.kettleoutputs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.vfs.KettleVFS;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.utils.CpkUtils;
import pt.webdetails.cpk.utils.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResultFilesKettleOutput extends KettleOutput {

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

    String defaultAttachmentName = this.getConfiguration().getAttachmentName();
    try {
      if ( files.size() == 1 && files.get( 0 ).getType() == FileType.FILE ) {
        // Singe file
        FileObject file = files.get( 0 );
        InputStream fileInputStream = KettleVFS.getInputStream( file );
        FileName fileName = file.getName();
        String defaultMimeType = this.getConfiguration().getMimeType();
        String mimeType = defaultMimeType != null ? defaultMimeType : MimeTypes.getMimeType( fileName.getBaseName() );
        String attachmentName = defaultAttachmentName != null ? defaultAttachmentName : fileName.getBaseName();

        CpkUtils.send( this.getResponse(), fileInputStream, mimeType,
                       attachmentName, this.getConfiguration().getSendResultAsAttachment() );

      } else {
        // More than one file, or folder
        // Build a zip / tar and ship it over!
        ZipUtil zip = new ZipUtil();
        zip.buildZipFromFileObjectList( files );

        String attachmentName = defaultAttachmentName != null ? defaultAttachmentName : zip.getZipNameToDownload();
        CpkUtils.send( this.getResponse(), zip.getZipInputStream(), MimeTypes.ZIP, attachmentName, true );
      }
    } catch ( FileSystemException ex ) {
      logger.error( "Failed sending files from kettle result.", ex );
    }
  }

}
