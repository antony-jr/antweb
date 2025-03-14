package org.calacademy.antweb.upload;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;

import org.apache.commons.httpclient.util.URIUtil;
import org.calacademy.antweb.*;
import org.calacademy.antweb.util.*;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

public class UploadFile {
    private static final Log s_log = LogFactory.getLog(UploadFile.class);

    String encoding;
    String userAgent;
    String base;
    String fileName;
    String root;
    boolean isBioRegion = false;
    
    // Usually this file encapsulates details of an actual file uploaded.
    boolean isReload = false;

    public boolean getIsReload() {
      return isReload;
    }
    public void setIsReload(boolean reload) { 
      isReload = reload; 
    }
        
    public static final String s_speciesListTail = "_speciesList.txt";
    public static String getSpeciesListTail() { 
        return s_speciesListTail; 
    }
    // deprecated.  In Species list home pages.  Do not remove.
    public static String getSpeciesListFileTail() { 
      return getSpeciesListTail();
    }
    
    
    public UploadFile() {
    }

    public UploadFile(String base, String fileName, String userAgent, String encoding) {

        if (false && AntwebProps.isDevMode()) {
            A.log("constructing UploadFile() 1 base:" + base + " fileName:" + fileName + " userAgent:" + userAgent);
            AntwebUtil.logShortStackTrace();
        }

        this.userAgent = userAgent;  // we don't actually use this, at all.

        this.base = base;
        this.fileName = fileName;
        String fileLoc = base + fileName;

        figureEncoding(fileLoc, encoding);
    }
    
    private void figureEncoding(String fileLoc, String encoding) {
        setEncoding("UTF-8");
    }

    /*
      //encoding = "ISO8859_1"; // No hyphen?  Not: ISO-8859-1?
      
      // These files are Jack Longino's (Utah).
      //if (fileLoc.contains("specimen2.txt")) {
          // this.encoding = "UTF-8";
      //    encoding = "MacRoman"; // Was, up until Dec 29, 2015.  
          // this.encoding = "ISO-8859-1";
      //} else {
            
      //  if (true) { //UploadFile.isValidUTF8(fileLoc)) {
          encoding = "UTF-8";
          A.log("figureEncoding() fileLoc:" + fileLoc + " isValidUTF8:" + fileLoc);
      //  }        
      //}

      //if (isWorldAnts()) encoding = "UTF-8";

      //if (correctEncoding(encoding) == false) {
      //  setEncoding("ISO8859_1");
      //}  
*/


    // To be ported towards use of UploadFile
    // This is used for Project upload, but not of Worldants

    public boolean isWorldAnts() {
        return fileName != null && fileName.contains("worldants");
    }

    public static boolean isValidUTF8(String filePath) {
      //final byte[] bytes) {
      boolean isValid = false;
      Path path = null;
      boolean isAllBytesRead = false;
      try {
      
        path = Paths.get(filePath);

        // Dying on this line in dev.  Not throwing exception.  Not returning.  Just ending.  WTF.
        byte[] bytes = Files.readAllBytes(path);

        isAllBytesRead = true;

        try {
          Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
          s_log.error("isValidUTF8() e:" + e);
          return false;
        }

        isValid = true;

      } catch (IOException e) {
        s_log.warn("isValidUTF8() iIOException:" + e + " sValid:" + isValid + " path:" + path) ;
        isValid = false;
      } finally {
        if (!isAllBytesRead) s_log.error("isValidUTF8() *** finally isAllBytesRead:" + isAllBytesRead + " filePath:" + filePath + " isValid:" + isValid);
      }

      return isValid;
    }
    
    public String getEncoding() {
      return encoding;
    }    
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    public Charset getCharset() {
        return Charset.forName(encoding);
    }

    boolean correctEncoding(String encoding) {
      boolean aLineFound = false;
      String theLine = null;
      Date startTime = new Date();
      String fileLoc = this.base + this.fileName;
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileLoc), encoding));

        theLine = in.readLine();
        while (theLine != null) {
          if (theLine.contains("0160810")) {
            aLineFound = true;
            int startLocCodePos = theLine.indexOf("Mont Copolia") - 5;
            int endLocCodePos = startLocCodePos + 21;
            String localityCode = theLine.substring(startLocCodePos, endLocCodePos); // was (211, 232);
            String localityEncoded = URIUtil.encodePath(localityCode, "ISO-8859-1");
            s_log.debug("correctEncoding(): 0160810 localityCode:" + localityCode + " substring(" + startLocCodePos + ", " + endLocCodePos + ") encoded:" + localityEncoded);
            //s_log.warn("correctEncoding(): substring:" + theLine.substring(277, 298);
            if (!localityEncoded.equals("Mah%E9%20Mont%20Copolia%20520")) {   // MahÈ Island, Mont Copolia
              s_log.error("correctEncoding() found false in " + AntwebUtil.secsSince(startTime) + " seconds.  Should be encoded: Mah%E9%20Mont%20Copolia%20520");
              s_log.error("Line:" + theLine);
              return false;
            }
          }

          if (theLine.contains("0625035")) {  // This is one of Jack's
            if (encoding == "MacRoman") return true;
            aLineFound = true;
            int offset = 36;
            String localityMatch = "Regi%C3%B3n%20Aut%C3%B3noma%20del%20Atl%C3%A1ntico%20Sur";   // not UTF-8
            if (encoding.equals("UTF-8")) {
              offset = 33;
              localityMatch = "Regi%F3n%20Aut%F3noma%20del%20Atl%E1ntico%20Sur"; 
            } else if (encoding.equals("MacRoman")) {
              offset = 33;
              localityMatch = "Regi%F3n%20Aut%F3noma%20del%20Atl%E1ntico%20Sur";
            }
            int startLocCodePos = theLine.indexOf("Reg");
            int endLocCodePos = startLocCodePos + 33;  // 33 if UTF.  36 in ...
            String localityCode = theLine.substring(startLocCodePos, endLocCodePos); // was (211, 232);
            String localityEncoded = URIUtil.encodePath(localityCode, "ISO-8859-1");
            s_log.debug("correctEncoding(): 0625035 encoding:" + encoding + " localityCode:" + localityCode + " substring(" + startLocCodePos + ", " + endLocCodePos + ") encoded:" + localityEncoded
    + " theLine:" + theLine);

            //if (AntwebProps.isDevMode()) AntwebUtil.logStackTrace();
              // Code is called twice.  Once from constructor (called from UploadAction:720) and once from UploadAction:741
    
            //s_log.warn("correctEncoding(): substring:" + theLine.substring(277, 298);
            //if (!localityEncoded.equals("Regi%C3%B3n%20Aut%C3%B3noma%20del%20Atl%C3%A1ntico%20Sur")) {  
                                      // Regi%C3%B3n%20Aut%C3%B3noma%20del%20Atl%C3%A1ntico%20
            if (!localityEncoded.equals(localityMatch)) { //   .  That's the UTF-8 encoding                                      
              // Región Autónoma del Atlántico Sur    urlEncodes as:Regi%C3%B3n%20Aut%C3%B3noma%20del%20Atl%C3%A1ntico%20Sur
              s_log.error("correctEncoding:" + encoding + " found false in " + AntwebUtil.secsSince(startTime) + " seconds.");
              // This would happen if uploaded as Cal Academy
              //if (AntwebProps.isDevMode()) {
              //  s_log.warn("Is DevMode returning true despite failure.  FIX.");
              //  return true;
              //}
              return false;
            }
          }
          
          theLine = in.readLine();
        }
      } catch (Exception e) {
        //s_log.error("correctEncoding(" + encoding + ") fileLoc:" + fileLoc + " e:" + e);
          // Expected, for instance from Jack's upload.
        // return false; // Not in 6.8.1
        /* This was added in, reasonably. In tricky release 6.9 (that included a wider rollout of the encoding selector,
           it caused uploads to be invalid. */
      }
      s_log.info("correctEncoding(" + encoding + ") found true in " + AntwebUtil.secsSince(startTime) + " seconds.  LineFound:" + aLineFound);

      return true;
    }


    // From: usr/local/antweb/web/upload/20220627-17:55:08-specimen31.txt   to: specimen31.txt
    private String m_fileNameBase = null;
    public String getFileNameBase() {
        if (m_fileNameBase == null) {
            String base = getFileLoc();
            if (base.contains("specimen")) base = base.substring(base.indexOf("specimen"));
            if (base.contains("worldants")) base = base.substring(base.indexOf("worldants"));

            //A.log("getFileNameBase() fileLoc:" + getFileLoc() + " base:" + base);
            m_fileNameBase = base;
        }
        return m_fileNameBase;
    }


    // Used for the Taxon source during specimen upload. Something like specimen1.txt
    // From: usr/local/antweb/web/upload/20220627-17:55:08-specimen31.txt   to: 20220627-17:55:08-specimen31.txt
    private String m_shortFileName = null;
    public String getShortFileName() {
      if (m_shortFileName == null) {
          String shortFileName = getFileLoc();
          while (shortFileName.contains("/")) {
              shortFileName = shortFileName.substring(shortFileName.indexOf("/") + 1);
          }

          //if (!shortFileName.equals(getFileName())) s_log.error("getShortFileName() shortFileName:" + shortFileName + " does not equal fileName:" + getFileName());
          // And it should.  When they are proven equal, we may get rid of this method.

          A.log("getShortFileName() fileLoc:" + getFileLoc() + " shortFileName:" + shortFileName);
          m_shortFileName = shortFileName;
      }
      return m_shortFileName;
    }

    public String getFileLoc() {
      return this.base + this.fileName;
    }
    
    public String getFileName() {
      return this.fileName;
    }
    
    public boolean isUTF8() {
      return encoding.equals("UTF-8");
    }
    public boolean isMacRoman() {
      return encoding.equals("MacRoman");
    }
    public boolean isIso() {
      return encoding.equals("ISO8859_1");
    }

    public String getRoot() {
      return this.root;
    }
    public void setRoot(String root) {
      this.root = root;
    }
    
    public boolean getIsBioRegion() {
        return isWorldAnts() || this.isBioRegion;
    }
    public void setIsBioRegion(boolean isBioRegion) {
      this.isBioRegion = isBioRegion;
    }
 
    public boolean exists() {
      File f = new File(getFileLoc());    
      return f.exists();
    }
    
    public void backup() {
      File file = new File(getFileLoc());
      s_log.info("backup() exists:" + exists() + " fileLoc:" + getFileLoc() + " exists:" + file.exists());

      if (!exists()) {
        s_log.warn("backup() fileLoc does not exist:" + getFileLoc());
      } else {
          String fullWebDir = Utility.getDocRoot() + "web";
          String fullWebUploadDir = fullWebDir + "/upload";
          //String backupWorkingDir = util.getInputFileHome() + "/backup";
          Utility.makeDirTree(fullWebUploadDir);
          s_log.info("backup() makeDirTree:" + fullWebUploadDir);

          String tempBackupDirFile = fullWebUploadDir + "/" + getBackupFileName();
          A.log("backup() date:" + Utility.getDateForFileName() + " base:" + getFileNameBase());   // + " short:" + getShortFileName()
          try {
            s_log.info("backup() " + getFileLoc() + " to " + tempBackupDirFile);
            Utility.copyFile(getFileLoc(), tempBackupDirFile);
          } catch (IOException e) {
            s_log.error("backup() e:" + e);
          }
      }
    }

    String backupFileName = null;
    public String getBackupFileName() {
      if (backupFileName == null) {
          backupFileName = Utility.getDateForFileName() + "-" + getFileNameBase();  //getShortFileName();
      }
      return backupFileName;
    }

    // This is what gets persisted in the specimen table.
    public String getBackupDirFile() {
        return "upload/" + getBackupFileName();
    }


}
