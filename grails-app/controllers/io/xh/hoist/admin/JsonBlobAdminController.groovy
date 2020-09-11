/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2020 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.admin

import io.xh.hoist.jsonblob.JsonBlob
import io.xh.hoist.RestController
import io.xh.hoist.security.Access

@Access(['HOIST_ADMIN'])
class JsonBlobAdminController extends RestController {
    static restTarget = JsonBlob
    static trackChanges = true

    def lookupData() {
        renderJSON(
            types: JsonBlob.createCriteria().list{
                projections { distinct('type') }
            }.sort()
        )
    }

    protected void preprocessSubmit(Map submit) {
        submit.lastUpdatedBy = username
        if (submit.value) submit.valueLastUpdated = new Date()
    }
}
