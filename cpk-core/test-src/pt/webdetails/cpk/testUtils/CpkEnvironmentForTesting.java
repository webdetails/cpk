package pt.webdetails.cpk.testUtils;

import pt.webdetails.cpf.impl.SimpleSessionUtils;
import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.ICpkEnvironment;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;

import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA. User: joao Date: 11/22/13 Time: 12:44 PM To change this template use File | Settings |
 * File Templates.
 */
public class CpkEnvironmentForTesting implements ICpkEnvironment {

  private IPluginUtils pluginUtils;
  private IRepositoryAccess repAccess;
  final IUserSession userSession = new SimpleUserSession( "userName", null, true, null );

  public CpkEnvironmentForTesting( IPluginUtils pluginUtils, IRepositoryAccess repAccess ) {
    this.pluginUtils = pluginUtils;
    this.repAccess = repAccess;
  }

  @Override
  public IPluginUtils getPluginUtils() {
    return pluginUtils;
  }

  @Override public IContentAccessFactory getContentAccessFactory() {
    return new ContentAccessFactoryForTesting( repAccess );
  }

  @Override
  public IAccessControl getAccessControl() {
    return new IAccessControl() {
      @Override
      public boolean isAllowed( IElement element ) {
        return true;
      }

      @Override
      public boolean isAdmin() {
        return true;
      }

      @Override
      public void throwAccessDenied( HttpServletResponse response ) {
        throw new UnsupportedOperationException( "Not supported yet." );
      }
    };
  }

  @Override
  public String getPluginName() {
    return pluginUtils.getPluginName();
  }

  @Override
  public ISessionUtils getSessionUtils() {
    return new SimpleSessionUtils( userSession, null, null );
  }

  @Override
  public void reload() {
  }

}
