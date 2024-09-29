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


package pt.webdetails.cpk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.ResultFile;

public class ZipUtil {
  private String zipName;
  private FileInputStream fis;
  private FileName topFilename;
  ArrayList<String> fileListing = new ArrayList<String>();

  protected Log logger = LogFactory.getLog( this.getClass() );


  public void buildZip( List<ResultFile> filesList ) {
    List<FileObject> files = new ArrayList<FileObject>( );
    for ( ResultFile resultFile : filesList ) {
      files.add( resultFile.getFile() );
    }
    this.buildZipFromFileObjectList( files );
  }

  public void buildZipFromFileObjectList( List<FileObject> files ) {
    try {
      ZipOutputStream zipOut;
      topFilename = getTopFileName( files );
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

      zipOut = writeEntriesToZip( files, zipOut );

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

  private ZipOutputStream writeEntriesToZip( Collection<FileObject> files, ZipOutputStream zipOut ) {
    int i = 0;
    try {
      for ( FileObject file : files ) {
        i++;
        logger.debug( "Files to process:" + files.size() );
        logger.debug( "Files processed: " + i );
        logger.debug( "Files remaining: " + ( files.size() - i ) );
        logger.debug( file.getName().getPath() );

        fileListing.add( removeTopFilenamePathFromString( file.getName().getPath() ) );

        ZipEntry zip = null;

        if ( file.getType() == FileType.FOLDER ) {
          zip = new ZipEntry( removeTopFilenamePathFromString( file.getName().getPath() + File.separator + "" ) );
          zipOut.putNextEntry( zip );
        } else {
          zip = new ZipEntry( removeTopFilenamePathFromString( file.getName().getPath() ) );
          zipOut.putNextEntry( zip );
          byte[] bytes = IOUtils.toByteArray( file.getContent().getInputStream() );
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

  public int getZipSize() {
    try {
      return fis.available();
    } catch ( IOException ex ) {
      Logger.getLogger( ZipUtil.class.getName() ).log( Level.SEVERE, null, ex );
    }
    return 0;
  }



  private FileName getTopFileName( List<FileObject> files ) {
    FileName topFileName = null;
    try {
      if ( !files.isEmpty() ) {
        topFileName = files.get( 0 ).getParent().getName();
      }
      for ( FileObject file : files ) {
        logger.debug( file.getParent().getName().getPath() );
        FileName myFileName = file.getParent().getName();
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