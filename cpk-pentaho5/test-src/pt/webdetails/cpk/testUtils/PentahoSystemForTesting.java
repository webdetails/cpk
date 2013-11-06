package pt.webdetails.cpk.testUtils;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA. User: joao Date: 10/21/13 Time: 4:33 PM To change this template use File | Settings |
 * File Templates.
 */
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
      roles[ 0 ] = new GrantedAuthorityImpl( roleName );

      Authentication auth = new UsernamePasswordAuthenticationToken( name, "", roles ); //$NON-NLS-1$

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
