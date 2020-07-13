/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2020 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.admin

import grails.gorm.transactions.ReadOnly
import io.xh.hoist.BaseController
import io.xh.hoist.clienterror.ClientError
import io.xh.hoist.security.Access

@Access(['HOIST_ADMIN'])
class ClientErrorAdminController extends BaseController {

    @ReadOnly
    def index() {
        def startDate = parseDate(params.startDate),
            endDate = parseDate(params.endDate)

        def results = ClientError.findAll(max: 5000, sort: 'dateCreated', order: 'desc') {
            if (startDate)          dateCreated >= startDate
            if (endDate)            dateCreated < endDate+1
            if (params.username)    username =~ "%$params.username%"
            if (params.error)       error =~ "%$params.error%"
        }

        renderJSON(results)
    }

    def lookups() {
        renderJSON([
            usernames: distinctVals('username')
        ])
    }


    //------------------------
    // Implementation
    //------------------------
    private Date parseDate(String dateStr) {
        return dateStr ? Date.parse('yyyyMMdd', dateStr) : null
    }

    private List distinctVals(String property) {
        return ClientError.createCriteria().list {
            projections { distinct(property) }
        }.sort()
    }

}
