/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;


import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpk.security.IAccessControl;


public interface ICpkEnvironment {

  public IPluginUtils getPluginUtils();

  public IContentAccessFactory getContentAccessFactory();

  public IAccessControl getAccessControl();

  public String getPluginName();

  public ISessionUtils getSessionUtils();

  public void reload();
}
