/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2018 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.admin

import io.xh.hoist.log.LogLevel
import io.xh.hoist.RestController
import io.xh.hoist.security.Access
import org.grails.web.json.JSONObject

@Access(['HOIST_ADMIN'])
class LogLevelAdminController extends RestController {

    static restTarget = LogLevel

    protected void preprocessSubmit(JSONObject submit) {
        if (submit.level == 'None') {
            submit.level = null
        }
    }

    def lookupData() {
            def levels =  ['None'] + LogLevel.LEVELS
            renderJSON (levels: levels)
    }
}