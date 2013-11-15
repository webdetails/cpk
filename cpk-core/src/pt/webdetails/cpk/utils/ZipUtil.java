/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.ResultFile;


/**
 * @author Luís Paulo Silva<luis.silva@webdetails.pt>
 */
public class ZipUtil {
  private String zipName;
  private FileInputStream fis;
  private FileName topFilename;
  ArrayList<String> fileListing = new ArrayList<String>();

  protected Log logger = LogFactory.getLog( this.getClass() );


  public void buildZip( List<ResultFile> filesList ) {
    try {
      ZipOutputStream zipOut;
      topFilename = getTopFileName( filesList );
      zipName = this.topFilename.getBaseName();
      File tempZip = null;

      if ( zipName.length() < 3 ) {
        String tempPrefix = new String();
        for ( int i = 0; i < 3 - zipName.length(); i++ ) {
          tempPrefix += "_";
        }
        tempZip = File.createTempFile( tempPrefix + zipName, ".tmp" );
      } else {
        tempZip = File.createTempFile( zipName, ".tmp" );
      }

      FileOutputStream fos = new FileOutputStream( tempZip );
      zipOut = new ZipOutputStream( fos );

      logger.info( "Building '" + zipName + "'..." );

      zipOut = writeEntriesToZip( filesList, zipOut );

      zipOut.close();
      fos.close();

      setFileInputStream( tempZip );
      logger.info( "'" + zipName + "' built." + " Sending to client " + getZipSize() / 1024 + "KB of data." );


    } catch ( Exception ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  public void closeInputStream() {
    try {
      fis.close();
    } catch ( IOException ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  private ZipOutputStream writeEntriesToZip( List<ResultFile> filesList, ZipOutputStream zipOut ) {
    int i = 0;
    try {
      for ( ResultFile resFile : filesList ) {
        i++;
        logger.debug( "Files to process:" + filesList.size() );
        logger.debug( "Files processed: " + i );
        logger.debug( "Files remaining: " + ( filesList.size() - i ) );
        logger.debug( resFile.getFile().getName().getPath() );
        FileObject myFile = resFile.getFile();

        fileListing.add( removeTopFilenamePathFromString( myFile.getName().getPath() ) );

        ZipEntry zip = null;

        if ( myFile.getType() == FileType.FOLDER ) {
          zip = new ZipEntry( removeTopFilenamePathFromString( myFile.getName().getPath() + File.separator + "" ) );
          zipOut.putNextEntry( zip );
        } else {
          zip = new ZipEntry( removeTopFilenamePathFromString( myFile.getName().getPath() ) );
          zipOut.putNextEntry( zip );
          byte[] bytes = IOUtils.toByteArray( myFile.getContent().getInputStream() );
          zipOut.write( bytes );
          zipOut.closeEntry();

        }


      }


    } catch ( Exception exception ) {
      logger.error( exception );
    }
    return zipOut;
  }

  public void unzip( File zipFile, File destinationFolder ) {
    byte[] buffer = new byte[ 1024 ];
    setFileInputStream( zipFile );
    ZipInputStream zis = new ZipInputStream( fis );
    try {

      ZipEntry entry = zis.getNextEntry();
      while ( entry != null ) {
        String filename = entry.getName();
        File newFile = null;

        if ( entry.isDirectory() ) {
          newFile = new File( destinationFolder.getAbsolutePath() + File.separator + filename + File.separator );
          newFile.mkdirs();
          newFile.mkdir();
        } else {
          newFile = new File( destinationFolder.getAbsolutePath() + File.separator + filename );
          newFile.createNewFile();
          FileOutputStream fos = new FileOutputStream( newFile );
          int len = 0;
          while ( ( len = zis.read( buffer ) ) > 0 ) {
            fos.write( buffer, 0, len );
          }

          fos.close();
        }


        entry = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();

    } catch ( IOException ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }


  }

  private void setFileInputStream( File file ) {
    try {
      if ( file == null ) {
        this.fis = null;
      } else {
        this.fis = new FileInputStream( file );
      }
    } catch ( FileNotFoundException ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  public FileInputStream getZipInputStream() {
    return fis;
  }


  public String getZipNameToDownload() {
    return getZipName().replaceAll( " ", "-" )
      + ".zip"; //Firefox and Opera don't interpret blank spaces and cut the string there causing the files to be
    // interpreted as "bin".
  }

  public String getZipName() {
    return zipName;
  }

  public long getZipSize() {
    try {
      return fis.available();
    } catch ( IOException ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }
    return 0;
  }

  private FileName getTopFileName( List<ResultFile> filesList ) {
    FileName topFileName = null;
    try {
      if ( !filesList.isEmpty() ) {
        topFileName = filesList.get( 0 ).getFile().getParent().getName();
      }
      for ( ResultFile resFile : filesList ) {
        logger.debug( resFile.getFile().getParent().getName().getPath() );
        FileName myFileName = resFile.getFile().getParent().getName();
        if ( topFileName.getURI().length() > myFileName.getURI().length() ) {
          topFileName = myFileName;
        }
      }
    } catch ( Exception exception ) {
      logger.error( exception );
    }
    return topFileName;
  }

  private String removeTopFilenamePathFromString( String path ) {

    String filteredPath = null;
    int index = this.topFilename.getParent().getPath().length();
    filteredPath = path.substring( index );


    return filteredPath;
  }
}