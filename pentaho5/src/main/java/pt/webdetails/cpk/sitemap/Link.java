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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import pt.webdetails.cpk.elements.IElement;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import pt.webdetails.cpk.CpkEngine;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.utils.XmlParserFactoryProducer;

public class Link {

  private String name, id, link;
  private List<Link> subLinks;
  private Map<String, IElement> elements;
  private IPluginUtils pluginUtils;
  protected Log logger = LogFactory.getLog( this.getClass() );

  public Link( File directory, Map<String, IElement> elementsMap, IPluginUtils pluginUtils ) {
    this.pluginUtils = pluginUtils;
    elements = elementsMap;
    subLinks = new ArrayList<Link>();
    buildLink( directory );
    this.name = directory.getName();
    this.id = "";
    this.link = "";

  }


  public Link( IElement element, IPluginUtils plug ) {
    this.pluginUtils = plug;
    this.name = getTextFromWcdf( element.getLocation(), "title" );
    if ( this.name == null ) {
      this.name = getTextFromWcdf( element.getLocation(), "description" );
    }
    this.link = "/pentaho/plugin/" + pluginUtils.getPluginName() + "/api/" + element.getId().toLowerCase();
    this.id = element.getLocation().split( "/" )[ element.getLocation().split( "/" ).length - 1 ];
    subLinks = new ArrayList<Link>();
  }

  public void buildLink( File directory ) {
    List<File> directories = null;
    List<File> files = null;
    Link link = null;

    if ( !getTopLevelDirectories( elements ).containsValue( directory ) ) {

      if ( hasSubfolders( directory ) ) {
        directories = getDirectories( directory );
      }

      if ( hasFiles( directory ) ) {
        files = getFiles( directory );
      }

      if ( directories != null ) {
        for ( File dir : directories ) {
          link = new Link( dir, elements, pluginUtils );
          subLinks.add( link );
        }
      }

      if ( files != null ) {
        for ( File file : files ) {
          int index = file.getName().indexOf( "." );
          String filename = file.getName().substring( 0, index ).toLowerCase();
          if ( elements.containsKey( filename ) ) {
            IElement myElement = elements.get( filename );
            link = new Link( myElement, pluginUtils );

            if ( !subLinkExists( (ArrayList) subLinks, link ) ) {
              subLinks.add( link );
            }
          }
        }
      }
    }
  }

  private boolean hasSubfolders( File directory ) {
    boolean has = false;
    if ( directory.isDirectory() ) {
      FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept( File pathname ) {
          return pathname.isDirectory();
        }
      };

      if ( directory.listFiles( dirFilter ).length > 0 ) {
        has = true;
      }
    }

    return has;
  }

  public String getName() {
    String n;
    if ( this.name == null ) {
      n = "null";
    } else {
      n = this.name;
    }
    return n;
  }

  public String getId() {
    String i;
    if ( this.id == null ) {
      i = "null";
    } else {
      i = this.id;
    }
    return i;
  }

  public List<Link> getSublinks() {
    return subLinks;
  }

  public String getLink() {
    String l;
    if ( this.link == null ) {
      l = "null";
    } else {
      l = this.link;
    }
    return l;
  }

  private String getTextFromWcdf( String path, String text ) {
    File xml = new File( path );
    SAXReader reader = XmlParserFactoryProducer.getSAXReader( null );
    Document doc = null;
    try {
      doc = reader.read( xml );
    } catch ( DocumentException documentException ) {
      logger.error( "Problem reading properties from " + path );
    }

    org.dom4j.Element root = doc.getRootElement();

    return root.elementText( text );
  }

  private void createSublinks() {
  }

  public boolean isDashboard( IElement e ) {
    boolean is = false;

    if ( e.getType().equalsIgnoreCase( "dashboard" ) ) {
      is = true;
    }

    return is;
  }

  public boolean isKettle( IElement e ) {
    boolean is = false;

    if ( e.getType().equalsIgnoreCase( "kettle" ) ) {
      is = true;
    }

    return is;
  }

  @JsonIgnore
  public String getLinkJson() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString( this );
    } catch ( IOException ex ) {
      Logger.getLogger( CpkEngine.class.getName() ).log( Level.SEVERE, null, ex );
    }
    return null;
  }

  private boolean hasFiles( File directory ) {
    boolean has = false;
    if ( directory.isDirectory() ) {
      FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept( File pathname ) {
          return pathname.isFile();
        }
      };

      if ( directory.listFiles( dirFilter ).length > 0 ) {
        has = true;
      }
    }

    return has;
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

  private List<File> getDirectories( File directory ) {
    List<File> dirs = null;

    if ( directory.isDirectory() ) {
      FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept( File pathname ) {
          return pathname.isDirectory();
        }
      };

      dirs = new ArrayList<File>( Arrays.asList( directory.listFiles( dirFilter ) ) );
    }

    return dirs;
  }

  private Map<String, File> getTopLevelDirectories( Map<String, IElement> elementsMap ) {
    HashMap<String, File> directories = new HashMap<String, File>();

    for ( IElement element : elementsMap.values() ) {
      File directory = new File( FilenameUtils.getPath( element.getLocation() ) );
      if ( directory != null ) {
        try {
          directories.put( directory.getCanonicalPath(), directory );
        } catch ( Exception e ) {
        }
      }
    }
    return directories;
  }

  private boolean subLinkExists( ArrayList<Link> lnks, Link lnk ) {
    boolean exists = false;

    for ( Link l : lnks ) {
      try {
        if ( l.getName() == null ) {
        } else if ( l.getId().equals( lnk.getId() ) ) {
          exists = true;
        }
      } catch ( Exception e ) {
        exists = true;
      }
    }

    return exists;
  }
}
