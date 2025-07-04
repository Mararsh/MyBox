/**
 *
 */
package thridparty.pdfdom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mara.mybox.dev.MyBoxLog;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.mabb.fontverter.FVFont;
import org.mabb.fontverter.FontVerter;
import org.mabb.fontverter.pdf.PdfFontExtractor;

/**
 * A table for storing entries about the embedded fonts and their usage.
 *
 * @author burgetr
 *
 * Updated by Mara
 */
public class FontTable {

    private static final Pattern fontFamilyRegex = Pattern.compile("([^+^-]*)[+-]([^+]*)");

    private final List<Entry> entries = new ArrayList<>();

    public void addEntry(PDFont font) {
        try {
            FontTable.Entry entry = get(font);
            if (entry == null) {
                String fontName = font.getName();
                String family = findFontFamily(fontName);
                String usedName = nextUsedName(family);
                FontTable.Entry newEntry = new FontTable.Entry(font.getName(), usedName, font);
                if (newEntry.isEntryValid()) {
                    add(newEntry);
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public Entry get(PDFont find) {
        try {
            for (Entry entryOn : entries) {
                if (entryOn.equalToPDFont(find)) {
                    return entryOn;
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
        return null;
    }

    public List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    public String getUsedName(PDFont font) {
        FontTable.Entry entry = get(font);
        if (entry == null) {
            return null;
        } else {
            return entry.usedName;
        }
    }

    protected String nextUsedName(String fontName) {
        int i = 1;
        String usedName = fontName;
        while (isNameUsed(usedName)) {
            usedName = fontName + i;
            i++;
        }

        return usedName;
    }

    protected boolean isNameUsed(String name) {
        for (Entry entryOn : entries) {
            if (entryOn.usedName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    protected void add(Entry entry) {
        entries.add(entry);
    }

    private String findFontFamily(String fontName) {
        // pdf font family name isn't always populated so have to find ourselves from full name
        String familyName = fontName;

        Matcher familyMatcher = fontFamilyRegex.matcher(fontName);
        if (familyMatcher.find()) // currently tacking on weight/style too since we don't generate html for it yet
        // and it's helpful for debugugging
        {
            familyName = familyMatcher.group(1) + " " + familyMatcher.group(2);
        }

        // browsers will barf if + in family name
        return familyName.replaceAll("[+]", " ");
    }

    public class Entry extends HtmlResource {

        public String fontName;
        public String usedName;
        public PDFontDescriptor descriptor;

        private final PDFont baseFont;
        private byte[] cachedFontData;
        private String mimeType = "x-font-truetype";
        private String fileEnding;

        public Entry(String fontName, String usedName, PDFont font) {
            super(fontName);

            this.fontName = fontName;
            this.usedName = usedName;
            this.descriptor = font.getFontDescriptor();
            this.baseFont = font;
        }

        @Override
        public byte[] getData() {
            try {
                if (cachedFontData != null) {
                    return cachedFontData;
                }

                if (baseFont instanceof PDType1CFont || baseFont instanceof PDType1Font) {
                    cachedFontData = loadType1Font(descriptor.getFontFile3());

                } else if (descriptor.getFontFile2() != null && baseFont instanceof PDType0Font) {
                    cachedFontData = loadType0TtfDescendantFont();

                } else if (descriptor.getFontFile2() != null) {
                    cachedFontData = loadTrueTypeFont(descriptor.getFontFile2());

                } else if (descriptor.getFontFile() != null) {
                    cachedFontData = loadType1Font(descriptor.getFontFile());

                } else if (descriptor.getFontFile3() != null) {
                    // FontFile3 docs say any font type besides TTF/OTF or Type 1..
                    cachedFontData = loadOtherTypeFont(descriptor.getFontFile3());
                }
                return cachedFontData;
            } catch (Exception e) {
                MyBoxLog.console(e.toString());
                return null;
            }
        }

        public boolean isEntryValid() {
            try {
                byte[] fontData = new byte[0];
                fontData = getData();

                return fontData != null && fontData.length != 0;
            } catch (Exception e) {
                MyBoxLog.console(e.toString());
                return false;
            }
        }

        private byte[] loadTrueTypeFont(PDStream fontFile) {
//            MyBoxLog.console("Fail to convert True Type fonts.");
//            try {
//                // could convert to WOFF though for optimal html output instead.
//                FVFont font = FontVerter.readFont(fontFile.toByteArray());
//                if (font != null) {
//                    byte[] fvFontData = tryNormalizeFVFont(font);
//                    if (fvFontData != null && fvFontData.length != 0) {
//                        mimeType = "application/x-font-truetype";
//                        fileEnding = "otf";
//                        return fvFontData;
//                    }
//                }
//            } catch (Exception e) {
//                MyBoxLog.console("Unsupported FontFile found. Normalisation will be skipped.");
//                MyBoxLog.console(e.toString());
//            }
            return new byte[0];
        }

        private byte[] loadType0TtfDescendantFont() {
            mimeType = "application/x-font-truetype";
            fileEnding = "ttf";
            try {
                FVFont font = PdfFontExtractor.convertType0FontToOpenType((PDType0Font) baseFont);
                byte[] fontData = tryNormalizeFVFont(font);

                if (fontData.length != 0) {
                    return fontData;
                }
            } catch (Exception ex) {
//                MyBoxLog.console("Error loading type 0 with ttf descendant font '{}' Message: {} {}",
//                        fontName + " " + ex.getMessage() + " " + ex.getClass());

            }
            try {
                return descriptor.getFontFile2().toByteArray();
            } catch (Exception ex) {
                return new byte[0];
            }
        }

        private byte[] loadType1Font(PDStream fontFile) {
//            MyBoxLog.console("Type 1 fonts are not supported by Pdf2Dom.");
            return new byte[0];
        }

        private byte[] loadOtherTypeFont(PDStream fontFile) {
            // Likley Bare CFF which needs to be converted to a font supported by browsers, can be
            // other font types which are not yet supported.
            try {
                FVFont font = FontVerter.convertFont(fontFile.toByteArray(), FontVerter.FontFormat.WOFF1);
                mimeType = "application/x-font-woff";
                fileEnding = font.getProperties().getFileEnding();

                return font.getData();
            } catch (Exception ex) {
//                MyBoxLog.console("Issue converting Bare CFF font or the font type is not supportedby Pdf2Dom, "
//                        + "Font: {} Exception: {} {}" + "  " + fontName, ex.getMessage() + "  " + ex.getClass());

                // don't barf completley for font conversion issue, html will still be useable without.
                return new byte[0];
            }
        }

        private byte[] tryNormalizeFVFont(FVFont font) {
            try {
                // browser validation can fail for many TTF fonts from pdfs
                if (!font.isValid()) {
                    font.normalize();
                }

                return font.getData();
            } catch (Exception ex) {
//                MyBoxLog.console("Error normalizing font '{}' Message: {} {}",
//                        fontName + "  " + ex.getMessage() + "  " + ex.getClass());
            }

            return new byte[0];
        }

        public boolean equalToPDFont(PDFont compare) {
            // Appears you can have two different fonts with the same actual font name since text position font
            // references go off a seperate dict lookup name. PDFBox doesn't include the lookup name with the
            // PDFont, so might have to submit a change there to be really sure fonts are indeed the same.
            return compare.getName().equals(baseFont.getName())
                    && compare.getType().equals(baseFont.getType())
                    && compare.getSubType().equals(baseFont.getSubType());
        }

        @Override
        public int hashCode() {
            return fontName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Entry other = (Entry) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (fontName == null) {
                if (other.fontName != null) {
                    return false;
                }
            } else if (!fontName.equals(other.fontName)) {
                return false;
            }
            return true;
        }

        @Override
        public String getFileEnding() {
            return fileEnding;
        }

        private FontTable getOuterType() {
            return FontTable.this;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }
}
