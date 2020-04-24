/*
 * This file belongs to Hoist, an application development toolkit
 * developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
 *
 * Copyright © 2020 Extremely Heavy Industries Inc.
 */

package io.xh.hoist.log

import ch.qos.logback.classic.Level
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.slf4j.Logger

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.ERROR
import static io.xh.hoist.util.Utils.exceptionRenderer
import static java.lang.System.currentTimeMillis


@Slf4j
@CompileStatic
trait LogSupport {

    /**
     * Expose the conventional logger associated with the class of the concrete
     * instance of this object.  This is useful for code in super classes or auxillary classes
     * that want to produce log statements in the loggers of particular concrete instances.
     */
    @CompileDynamic
    Logger getInstanceLog() {
        log
    }

    //------------------------------------------------------------------------------------
    // Main variants.  Will use closest inherited logger starting from the *class*
    // where the logging statement is written.
    // -----------------------------------------------------------------------------------
    Object withDebug(Object msgs, Closure c)        {withDebug(log, msgs, c)}
    Object withShortDebug(Object msgs, Closure c)   {withShortDebug(log, msgs, c)}
    Object withInfo(Object msgs, Closure c)         {withInfo(log, msgs, c)}
    Object withShortInfo(Object msgs, Closure c)    {withShortInfo(log, msgs, c)}

    Object logErrorCompact(Object msg, Throwable e) {logErrorCompact(log, msg, e)}
    Object logDebugCompact(Object msg, Throwable e) {logDebugCompact(log, msg, e)}


    //---------------------------------------------------------------------------
    // Variants for logging to a specific logger.
    // Typically used with getInstanceLog(), or when calling from a static method
    //---------------------------------------------------------------------------
    static Object withDebug(Object log, Object msgs, Closure c) {
        loggedDo(log, DEBUG, true, msgs, c)
    }

    static Object withShortDebug(Object log, Object msgs, Closure c) {
        loggedDo(log, DEBUG, false, msgs, c)
    }

    static Object withInfo(Object log, Object msgs, Closure c) {
        loggedDo(log, INFO, true, msgs, c)
    }

    static Object withShortInfo(Object log, Object msgs, Closure c) {
        loggedDo(log, INFO, false, msgs, c)
    }

    @CompileDynamic
    static Object logErrorCompact(Object log, Object msg, Throwable t) {
        String message = exceptionRenderer.summaryTextForThrowable(t)
        if (msg) message = msg.toString() + ' | ' + message

        if (log.debugEnabled) {
            log.error(message, t)
        } else {
            log.error(message + ' [log on debug for more...]')
        }
    }

    @CompileDynamic
    static Object logDebugCompact(Object log, Object msg, Throwable t) {
        String message = exceptionRenderer.summaryTextForThrowable(t)
        if (msg) message = msg.toString() + ' | ' + message

        if (log.traceEnabled) {
            log.debug(message, t)
        } else {
            log.debug(message + ' [log on trace for more...]')
        }
    }

    //------------------------
    // Implementation
    //------------------------
    static private Object loggedDo(Object log, Level level, boolean full, Object msgs, Closure c) {
        long start = currentTimeMillis()
        String msg = msgs instanceof Collection ? msgs.collect {it.toString()}.join(' | ') : msgs.toString()
        def ret

        if (full) logAtLevel(log, level, "$msg | started")

        try {
            ret = c.call()
        } catch (Exception e) {
            long elapsed = currentTimeMillis() - start
            def exceptionSummary = exceptionRenderer.summaryTextForThrowable((Throwable) e)
            logAtLevel(log, level, "$msg | failed - $exceptionSummary | $elapsed")
            throw e
        }

        long elapsed = currentTimeMillis() - start
        logAtLevel(log, level, "$msg | completed | $elapsed")

        return ret
    }

    @CompileDynamic
    static private void logAtLevel(Object log, Level level, GString msg) {
        switch (level) {
            case DEBUG: log.debug (msg); break
            case INFO:  log.info (msg); break
            case WARN:  log.warn (msg); break
            case ERROR: log.error (msg); break
        }
    }
}
