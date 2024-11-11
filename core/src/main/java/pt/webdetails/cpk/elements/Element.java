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


package pt.webdetails.cpk.elements;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Element implements IElement {

  protected Log logger = LogFactory.getLog( this.getClass() );
  private String pluginId;
  private String id;
  private String type;
  private String name;
  private String filePath;
  private boolean adminOnly;


  protected Element() {
  }

  public boolean init( final String pluginId, final String id,
                       final String type, final String filePath, boolean adminOnly ) {
    this.pluginId = pluginId;
    this.id = id;
    this.type = type;
    this.name = FilenameUtils.getBaseName( filePath );
    this.filePath = filePath;
    this.adminOnly = adminOnly;
    return true;
  }

  @Override
  public String getPluginId() {
    return this.pluginId;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLocation() {
    return filePath;
  }

  @Override
  public boolean isAdminOnly() {
    return this.adminOnly;
  }

  @Override
  public boolean isRenderable() {
    return false;
  }

  @Override
  public String toString() {
    return "{" + "pluginId=" + this.getPluginId() + ", id=" + getId() + ", type=" + getType() + ", name=" + getName()
      + ", filePath=" + getLocation() + ", adminOnly=" + isAdminOnly() + "}";
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 11 * hash + ( this.id != null ? this.id.hashCode() : 0 );
    hash = 11 * hash + ( this.type != null ? this.type.hashCode() : 0 );
    hash = 11 * hash + ( this.filePath != null ? this.filePath.hashCode() : 0 );
    return hash;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    final Element other = (Element) obj;
    if ( ( this.id == null ) ? ( other.id != null ) : !this.id.equals( other.id ) ) {
      return false;
    }
    if ( ( this.type == null ) ? ( other.type != null ) : !this.type.equals( other.type ) ) {
      return false;
    }
    if ( ( this.filePath == null ) ? ( other.filePath != null ) : !this.filePath.equals( other.filePath ) ) {
      return false;
    }
    return true;
  }
}
