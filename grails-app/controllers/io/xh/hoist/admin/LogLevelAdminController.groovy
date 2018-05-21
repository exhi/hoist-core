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
    def logLevelService

    def lookupData() {
            def levels = LogLevel.LEVELS
            renderJSON (levels: levels)
    }

    protected void doCreate(Object obj, Object data) {
        super.doCreate(obj, data)
        logLevelService.calculateAdjustments()
    }

    protected void doUpdate(Object obj, Object data) {
        super.doUpdate(obj, data)
        logLevelService.calculateAdjustments()
    }

    protected void doDelete(Object obj) {
        super.doDelete(obj)
        logLevelService.calculateAdjustments()
    }

}