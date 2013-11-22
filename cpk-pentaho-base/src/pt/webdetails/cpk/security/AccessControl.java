/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/


package pt.webdetails.cpk.security;

import javax.servlet.http.HttpServletResponse;

import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.session.PentahoSessionUtils;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpf.utils.IPluginUtils;

public class AccessControl implements pt.webdetails.cpk.security.IAccessControl {
    private IUserSession session;
    private PluginUtils pluginUtils;

    public AccessControl(IPluginUtils pluginUtils) {
        this.session = new PentahoSessionUtils().getCurrentSession();
        this.pluginUtils = (PluginUtils) pluginUtils;
    }


    @Override
    public boolean isAllowed(IElement element) {
        boolean is = false;

        if (element.isAdminOnly() && isAdmin()) {
            is = true;
        } else if (!element.isAdminOnly()) {
            is = true;
        }

        return is;
    }

    @Override
    public boolean isAdmin() {
        boolean is = false;
        is = session.isAdministrator();


        return is;
    }

    @Override
    public void throwAccessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

}
