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
