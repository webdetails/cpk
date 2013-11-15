/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.testUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import pt.webdetails.cpf.impl.DefaultRepositoryFile;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryFile;

/**
 * @author joao
 */
public class PentahoRepositoryAccessForTesting extends PentahoRepositoryAccess {

  private static Log logger = LogFactory.getLog( PentahoRepositoryAccessForTesting.class );

  @Override
  public IRepositoryFile getSettingsFile( String fileName, FileAccess fa ) {
    //Get plugin dir
    URL resourceUrl = null;
    try {
      resourceUrl =
        new URL( "file://" + System.getProperty( "user.dir" ) + "/test-resources/repository/system/cpkSol/cpk.xml" );
    } catch ( MalformedURLException ex ) {
      Logger.getLogger( PentahoRepositoryAccess.class.getName() ).log( Level.SEVERE, null, ex );
    }
    File f;
    try {
      f = new File( resourceUrl.toURI() );
    } catch ( URISyntaxException ex ) {
      logger.error( "Error while opening settings file with url " + resourceUrl );
      return null;
    }
    return new DefaultRepositoryFile( f );
    //*/
    //return super.getSettingsFile(fileName, fa);
  }

  private static IPentahoSession getAdminSession() {
    IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
    UserSession session = new UserSession( "admin", null, false, null );
    GrantedAuthority[] auths = userDetailsRoleListService.getUserRoleListService().getAllAuthorities();
    Authentication auth = new AnonymousAuthenticationToken( "admin", SecurityHelper.SESSION_PRINCIPAL, auths );
    session.setAttribute( SecurityHelper.SESSION_PRINCIPAL, auth );
    session.doStartupActions( null );
    return session;
  }
}
