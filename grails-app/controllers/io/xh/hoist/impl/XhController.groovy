/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2021 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.impl

import groovy.transform.CompileStatic
import io.xh.hoist.BaseController
import io.xh.hoist.config.ConfigService
import io.xh.hoist.clienterror.ClientErrorService
import io.xh.hoist.exception.NotFoundException
import io.xh.hoist.exception.SessionMismatchException
import io.xh.hoist.export.GridExportImplService
import io.xh.hoist.feedback.FeedbackService
import io.xh.hoist.json.JSONParser
import io.xh.hoist.jsonblob.JsonBlobService
import io.xh.hoist.pref.PrefService
import io.xh.hoist.security.AccessAll
import io.xh.hoist.track.TrackService
import io.xh.hoist.environment.EnvironmentService
import io.xh.hoist.util.Utils
import org.owasp.encoder.Encode

@AccessAll
@CompileStatic
class XhController extends BaseController {

    ClientErrorService clientErrorService
    ConfigService configService
    FeedbackService feedbackService
    GridExportImplService gridExportImplService
    JsonBlobService jsonBlobService
    PrefService prefService
    TrackService trackService
    EnvironmentService environmentService

    //------------------------
    // Identity / Auth
    //------------------------
    def authStatus() {
        def user = identityService.getAuthUser(request)
        renderJSON(authenticated: user != null)
    }

    def getIdentity() {
        renderJSON(identityService.clientConfig)
    }

    def login(String username, String password) {
        def success = identityService.login(username, password)
        renderJSON(success: success)
    }

    def logout() {
        def success = identityService.logout()
        renderJSON(success: success)
    }


    //------------------------
    // Admin Impersonation
    //------------------------
    def impersonationTargets() {
        def targets = identityService.impersonationTargets
        renderJSON(targets.collect{[username: it.username]})
    }

    def impersonate(String username) {
        identityService.impersonate(username)
        renderJSON(success: true)
    }

    def endImpersonate() {
        identityService.endImpersonate()
        renderJSON(success: true)
    }


    //------------------------
    // Tracking
    //------------------------
    def track(String category, String msg, String data, int elapsed, String severity) {
        ensureClientUsernameMatchesSession()

        trackService.track(
            category: safeStr(category),
            msg: safeStr(msg),
            data: data ? JSONParser.parseObjectOrArray(safeStr(data)) : null,
            elapsed: elapsed,
            severity: safeStr(severity)
        )

        renderJSON(success: true)
    }


    //------------------------
    // Config
    //------------------------
    def getConfig() {
        renderJSON(configService.clientConfig)
    }


    //------------------------
    // Preferences
    //------------------------
    def getPrefs() {
        ensureClientUsernameMatchesSession()
        renderJSON(prefService.clientConfig)
    }

    def setPrefs(String updates) {
        ensureClientUsernameMatchesSession()

        Map prefs = JSONParser.parseObject(updates)
        prefs.each {k, value ->
            String key = k.toString()
            if (value instanceof Map) {
                prefService.setMap(key, value)
            } else if (value instanceof List) {
                prefService.setList(key, value)
            } else {
                prefService.setPreference(key, value.toString())
            }
        }

        def ret = prefService.getLimitedClientConfig(prefs.keySet() as List)

        renderJSON(preferences: ret)
    }

    def clearPrefs() {
        ensureClientUsernameMatchesSession()
        prefService.clearPreferences()
        renderJSON(success: true)
    }

    //------------------------
    // Json Blobs
    //------------------------
    def getJsonBlob(String token) {
        renderJSON(jsonBlobService.get(token))
    }

    def listJsonBlobs(String type, boolean includeValue) {
        renderJSON(jsonBlobService.list(type, includeValue))
    }

    def createJsonBlob(String type, String name, String value, String meta, String description) {
        renderJSON(jsonBlobService.create(type, name, value, meta, description))
    }

    def updateJsonBlob(String token, String name, String value, String meta, String description) {
        renderJSON(jsonBlobService.update(token, name, value, meta, description))
    }

    def archiveJsonBlob(String token) {
        renderJSON(jsonBlobService.archive(token))
    }


    //------------------------
    // Export
    //------------------------
    // The 'params' is a JSON encoded string, uploaded using multipart/form-data to be treated as a file. We must read
    // its content from the inputStream, and then parse the JSON to get usable params for GridExportImplService.
    def export() {
        def inputStream = request.getPart('params').inputStream,
            data = JSONParser.parseObject(inputStream),
            ret = gridExportImplService.getBytesForRender(data)
        render(ret)
    }


    //------------------------
    // Environment
    //------------------------
    def environment() {
        renderJSON(environmentService.getEnvironment())
    }

    def version() {
        def shouldUpdate = configService.getBool('xhAppVersionCheckEnabled')
        renderJSON (
            appVersion: Utils.appVersion,
            appBuild: Utils.appBuild,
            shouldUpdate: shouldUpdate
        )
    }

    //------------------------
    // Client Errors
    //------------------------
    def submitError(String msg, String error, String appVersion, boolean userAlerted) {
        ensureClientUsernameMatchesSession()
        clientErrorService.submit(
            safeStr(msg),
            safeStr(error),
            safeStr(appVersion),
            userAlerted
        )
        renderJSON(success: true)
    }

    //------------------------
    // Feedback
    //------------------------
    def submitFeedback(String msg, String appVersion) {
        ensureClientUsernameMatchesSession()
        feedbackService.submit(
            safeStr(msg),
            safeStr(appVersion)
        )
        renderJSON(success: true)
    }


    //-----------------------
    // Misc
    //-----------------------
    /**
     * Returns the timezone offset for a given timezone ID.
     * While abbrevs (e.g. 'GMT', 'PST', 'UTC+04') are supported, fully qualified IDs (e.g.
     * 'Europe/London', 'America/New_York') are preferred, as these account for daylight savings.
     */
    def getTimeZoneOffset(String timeZoneId) {
        // Validate ID, as getTimeZone() defaults to GMT if not recognized.
        def availableIds = TimeZone.getAvailableIDs()
        if (!availableIds.contains(timeZoneId)) {
            throw new NotFoundException("Unknown timeZoneId")
        }
        def tz = TimeZone.getTimeZone(timeZoneId)
        renderJSON([offset: tz.getOffset(System.currentTimeMillis())])
    }

    def notFound() {
        throw new NotFoundException()
    }

    //------------------------
    // Implementation
    //------------------------
    /**
     * Check to validate that the client's expectation of the current user matches the active user
     * actually present within the session. Should be called prior to any operations in this
     * controller that rely on or return / modify data specific to a particular user.
     *
     * Note this is *not* a security check - access control and validation of user operations are
     * the responsibilities of the auth framework and relevant services. This is intended to avoid
     * edge-case bugs where the server-side login has changed but the app within a client browser
     * is not yet aware - e.g. calls made on page unload during the start of an impersonation session.
     */
    private void ensureClientUsernameMatchesSession() {
        def clientUsername = params.clientUsername

        if (!clientUsername) {
            throw new SessionMismatchException("Unable to confirm match between client and session user.")
        } else if (clientUsername != username) {
            throw new SessionMismatchException("The reported clientUsername does not match current session user.")
        }
    }

    /**
     * Run user-provided string input through an OWASP-provided encoder to escape tags. Note the
     * use of `forHtmlContent()` encodes only `&<>` and in particular leaves quotes un-escaped to
     * support JSON strings.
     */
    private String safeStr(String input) {
        return input ? Encode.forHtmlContent(input) : input
    }

}
