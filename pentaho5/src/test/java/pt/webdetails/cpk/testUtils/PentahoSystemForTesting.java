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


package pt.webdetails.cpk.testUtils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.Arrays;
import java.util.concurrent.Callable;

public class PentahoSystemForTesting extends PentahoSystem {

  public static <T> T runAsSystem( final Callable<T> callable ) throws Exception {
    final String name = "system session"; //$NON-NLS-1$
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      // create pentaho session
      StandaloneSession session = new StandaloneSession( name );
      session.setAuthenticated( name );
      // create authentication

      GrantedAuthority[] roles;

      ISystemSettings settings = PentahoSystem.getSystemSettings();
      String roleName = ( settings != null ) ? settings.getSystemSetting( "acl-voter/admin-role", "Admin" ) : "Admin";

      roles = new GrantedAuthority[ 1 ];
      roles[ 0 ] = new SimpleGrantedAuthority( roleName );

      Authentication auth = new UsernamePasswordAuthenticationToken( name, "", Arrays.asList( roles ) ); //$NON-NLS-1$

      // set holders
      PentahoSessionHolder.setSession( session );
      SecurityContextHolder.getContext().setAuthentication( auth );
      return callable.call();
    } finally {
      IPentahoSession sessionToDestroy = PentahoSessionHolder.getSession();
      if ( sessionToDestroy != null ) {
        try {
          sessionToDestroy.destroy();
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
      PentahoSessionHolder.setSession( origSession );
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }
}
