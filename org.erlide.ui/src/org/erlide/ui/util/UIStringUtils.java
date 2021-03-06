/*
 * Code borrowed from PyDev
 */
/*
 * @author Fabio Zadrozny
 * Created: June 2005
 * License: Common Public License v1.0
 */

package org.erlide.ui.util;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import org.erlide.core.util.StringUtils;

public class UIStringUtils {

    private UIStringUtils() {
    }

    public static final Object EMPTY = "";


    public static String removeWhitespaceColumnsToLeft(String hoverInfo) {
        StringBuilder buf = new StringBuilder();
        int firstCharPosition = Integer.MAX_VALUE;

        List<String> splitted = StringUtils.splitLines(hoverInfo);
        for (String line : splitted) {
            if (line.trim().length() > 0) {
                int found = ErlideSelection.getFirstCharPosition(line);
                firstCharPosition = Math.min(found, firstCharPosition);
            }
        }

        if (firstCharPosition != Integer.MAX_VALUE) {
            for (String line : splitted) {
                if (line.length() > firstCharPosition) {
                    buf.append(line.substring(firstCharPosition));
                }
            }
            return buf.toString();
        } else {
            return hoverInfo;// return initial
        }
    }

    /**
     * Given some html, extracts its text.
     */
    public static String extractTextFromHTML(String html) {
        try {
            EditorKit kit = new HTMLEditorKit();
            Document doc = kit.createDefaultDocument();

            // The Document class does not yet handle charset's properly.
            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

            // Create a reader on the HTML content.
            Reader rd = new StringReader(html);

            // Parse the HTML.
            kit.read(rd, doc, 0);

            // The HTML text is now stored in the document
            return doc.getText(0, doc.getLength());
        } catch (Exception e) {
        }
        return "";
    }

}
