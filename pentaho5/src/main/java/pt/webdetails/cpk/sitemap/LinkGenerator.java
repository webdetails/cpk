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


package pt.webdetails.cpk.sitemap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import pt.webdetails.cpk.elements.IElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.elements.impl.DashboardElement;

public class LinkGenerator {

  private ArrayList<Link> dashboardLinks;
  //private ArrayList<Link> kettleLinks;
  protected Log logger = LogFactory.getLog( this.getClass() );
  private IPluginUtils pluginUtils;
  private final static String ADMIN_DIR = "admin";
  private final static String KETTLE_DIR = "kettle";

  public LinkGenerator( Map<String, IElement> elementsMap, IPluginUtils pluginUtils ) {
    this.pluginUtils = pluginUtils;
    generateLinks( elementsMap );
  }

  private Map<String, File> getTopLevelDirectories( Collection<IElement> elements ) {
    HashMap<String, File> directories = new HashMap<String, File>();

    for ( IElement element : elements ) {
      File directory = new File( FilenameUtils.getFullPath( element.getLocation() ) );
      if ( directory != null ) {
        try {
          directories.put( directory.getCanonicalPath(), directory );
        } catch ( Exception e ) {
        }
      }
    }
    return directories;
  }

  private List<File> getDirectories( File directory ) {
    List<File> directories = new ArrayList<File>();

    FileFilter dirFilter = new FileFilter() {
      @Override
      public boolean accept( File pathname ) {
        return pathname.isDirectory();
      }
    };

    File[] dirs = directory.listFiles( dirFilter );

    if ( dirs != null ) {
      directories = Arrays.asList( dirs );
    }

    return directories;
  }

  private void generateLinks( Map<String, IElement> elementsMap ) {
    dashboardLinks = new ArrayList<Link>();
    Map<String, File> directories = getTopLevelDirectories( elementsMap.values() );
    Link l = null;

    for ( File directory : directories.values() ) {
      if ( !directory.getName().equals( ADMIN_DIR ) && !directory.getName().equals( KETTLE_DIR ) ) {

        for ( File file : getFiles( directory ) ) {



          int index = file.getName().indexOf( "." );
          String filename = file.getName().substring( 0, index ).toLowerCase();

          if ( elementsMap.containsKey( filename ) ) {
            IElement element = elementsMap.get( filename );
            if ( isDashboard( element ) ) {
              l = new Link( elementsMap.get( filename ), pluginUtils );
              if ( !linkExists( dashboardLinks, l ) ) {
                dashboardLinks.add( l );
              }
            }
         }

          for ( File dir : getDirectories( directory ) ) {
            if ( !dir.getName().equals( ADMIN_DIR ) && !directories.containsValue( dir ) ) {
              l = new Link( dir, elementsMap, pluginUtils );
              if ( !linkExists( dashboardLinks, l ) ) {
                dashboardLinks.add( l );
              }
            }

          }

        }
      }
    }
  }

  private List<File> getFiles( File directory ) {
    List<File> files = null;

    if ( directory.isDirectory() ) {
      FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept( File pathname ) {
          return pathname.isFile();
        }
      };

      files = new ArrayList<File>( Arrays.asList( directory.listFiles( dirFilter ) ) );
    }

    return files;
  }

  private boolean linkExists( ArrayList<Link> lnks, Link lnk ) {
    boolean exists = false;

    for ( Link l : lnks ) {
      try {
        if ( l.getName() == null ) {
        } else if ( !l.getId().equals( "" ) && l.getId().equals( lnk.getId() ) ) {
          exists = true;
        } else if ( l.getId().equals( "" ) && l.getName().equals( lnk.getName() ) ) {
          exists = true;
        }
      } catch ( Exception e ) {
        exists = true;
      }
    }

    return exists;
  }

  public JsonNode getLinksJson() {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jnode = null;
    ArrayList<String> json = new ArrayList<String>();

    for ( Link l : dashboardLinks ) {
      json.add( l.getLinkJson() );
    }
    try {
      jnode = mapper.readTree( json.toString() );
    } catch ( IOException ex ) {
      logger.error( ex );
    }

    return jnode;
  }

  public boolean isDashboard( IElement e ) {
    return ( e instanceof DashboardElement );
  }

}
