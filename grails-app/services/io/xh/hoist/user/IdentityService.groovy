/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2018 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.user

import groovy.transform.CompileStatic
import io.xh.hoist.BaseService
import io.xh.hoist.auth.UserIdentity

/**
 * Primary service for retrieving the logged-in HoistUser (aka the application user).
 * This service powers the getUser() and getUsername() methods in Hoist's
 * BaseService and BaseController classes.
 *
 * This service is *not* intended for override or customization at the application level.
 */
@CompileStatic
class IdentityService extends BaseService {

    BaseUserService userService

    /**
     * Return the current active user. Note that this is the 'apparent' user, used for most
     * application level purposes. In the case of an active impersonation session this will be
     * different from the authenticated user.
     */
    HoistUser getUser() {
        getApparentUser()
    }

    /**
     *  The 'apparent' user, used for most application level purposes.
     */
    HoistUser getApparentUser() {
        UserIdentity identity = UserIdentity.get()
        return findUser(identity?.apparentUsername)
    }

    /**
     *  The 'authorized' user as verified by AuthenticationService.
     */
    HoistUser getAuthUser() {
        UserIdentity identity = UserIdentity.get()
        return findUser(identity?.username)
    }

    /**
     * Is the authorized user currently impersonating someone else?
     */
    boolean isImpersonating() {
        UserIdentity identity = UserIdentity.get()
        return identity.apparentUsername != identity.username
    }

    /**
     * Return minimal identify information for confirmed authenticated users.
     * Used by client-side web app for identity management.
     */
    Map getClientConfig() {
        def authUser = this.getAuthUser(),
            apparentUser = this.getApparentUser()

        if (!authUser || !apparentUser) return null

        return (authUser != apparentUser) ?
            [
                apparentUser: apparentUser,
                apparentUserRoles: apparentUser.roles,
                authUser: authUser,
                authUserRoles: authUser.roles,
            ] :
            [
                user: authUser,
                roles: authUser.roles
            ]
    }

    //----------------------
    // Implementation
    //----------------------
    private HoistUser findUser(String username) {
        return username ? userService.find(username) : null
    }
}