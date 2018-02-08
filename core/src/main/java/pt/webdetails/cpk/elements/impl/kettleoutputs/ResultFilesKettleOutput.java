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
