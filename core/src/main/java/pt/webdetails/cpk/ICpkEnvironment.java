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

package pt.webdetails.cpk;

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpk.security.IAccessControl;

import java.util.Set;

public interface ICpkEnvironment {

  IPluginUtils getPluginUtils();

  IContentAccessFactory getContentAccessFactory();

  IAccessControl getAccessControl();

  String getPluginName();

  Set<String> getReservedWords();

  ISessionUtils getSessionUtils();

  String getWebAppDir();

  void reload();
}
