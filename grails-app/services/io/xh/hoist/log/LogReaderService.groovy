package io.xh.hoist.log

import io.xh.hoist.BaseService
import org.apache.commons.io.input.ReversedLinesFileReader

class LogReaderService extends BaseService {

    /**
     * Fetch the (selected) contents of a log file for viewing in the admin console.
     * @param filename - (required) Filename to be read
     * @param startLine - (optional) line number of file to start at; if null or zero or negative, will return tail of file
     * @param maxLines - (optional) number of lines to return
     * @param pattern - (optional) only lines matching pattern will be returned
     * @return - List of elements of the form [linenumber, text] for the requested lines
     */
    List readFile(String filename, Integer startLine, Integer maxLines = 10000, String pattern) {
        def tail = !startLine || startLine <= 0,
            ret = new ArrayList(maxLines),
            file = new File(LogUtils.logRootPath, filename)

        if (!file.exists()) throw new FileNotFoundException()

        Closeable closeable
        try {

            if (tail) {
                ReversedLinesFileReader reader = closeable = new ReversedLinesFileReader(file)

                long lineNumber = getFileLength(file)
                for (String line = reader.readLine(); line != null && ret.size() < maxLines; line = reader.readLine()) {
                    if (!pattern || line.toLowerCase() =~ pattern.toLowerCase()) {
                        ret << [lineNumber, line]
                        lineNumber--
                    }
                }

            } else {
                BufferedReader reader = closeable = new BufferedReader(new FileReader(file))

                // Skip lines as needed
                for (def i = 1; i < startLine; i++) {
                    def throwAway = reader.readLine();
                    if (throwAway == null) return []
                }

                long lineNumber = startLine
                for (String line = reader.readLine(); line != null && ret.size() < maxLines; line = reader.readLine()) {
                    if (!pattern || line.toLowerCase() =~ pattern.toLowerCase()) {
                        ret << [lineNumber, line]
                        lineNumber++
                    }
                }
            }

            return ret

        } finally {
            if (closeable) closeable.close()
        }
    }

    long getFileLength(File file) {
        BufferedReader reader
        try {
            reader = new BufferedReader(new FileReader(file))
            long ret = 0;
            while (reader.readLine() != null) {
                ret++;
            }
            return ret
        } finally {
            if (reader) reader.close()
        }
    }
}