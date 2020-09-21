/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2020 Extremely Heavy Industries Inc.
 */
package io.xh.hoist.jsonblob

import io.xh.hoist.json.JSONFormat
import io.xh.hoist.util.Utils

class JsonBlob implements JSONFormat {

    String type
    String owner
    String name
    String value
    String description
    Date dateCreated
    Date lastUpdated
    Date valueLastUpdated
    String lastUpdatedBy

    static mapping = {
        table 'xh_json_blob'
        cache true
        value type: 'text'
        description type: 'text'
    }

    static constraints = {
        type maxSize: 50, blank: false
        owner maxSize: 50, blank: false
        name maxSize: 50, blank: false
        value validator: {Utils.isJSON(it) ?: 'default.invalid.json.message'}
        description nullable: true
        lastUpdatedBy nullable: true
    }

    Map formatForJSON() {[
        id: id,
        type: type,
        owner: owner,
        name: name,
        value: value,
        description: description,
        dateCreated: dateCreated,
        lastUpdated: lastUpdated,
        valueLastUpdated: valueLastUpdated,
        lastUpdatedBy: lastUpdatedBy
    ]}
}
