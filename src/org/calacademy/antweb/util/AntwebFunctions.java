package org.calacademy.antweb.util;

import java.io.*;
import java.util.*;

import java.sql.*;

import org.calacademy.antweb.*;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;


public abstract class AntwebFunctions {

  private static final Log s_log = LogFactory.getLog(AntwebFunctions.class);
    
    public static void genRecentDescEdits(Connection connection)
      throws SQLException, IOException {

      // This method generates the file here: http://localhost/antweb/recentDescEdits.jsp
      // This will be included by the homepage template.

        int MAX_DESC_EDIT_COUNT = 5;

        String docBase = AntwebProps.getDocRoot();

        String genIncDir = docBase + "/web/genInc/";

        FileUtil.makeDir(genIncDir);

        File outputFile = new File(genIncDir + "recentDescEdits.jsp");
        FileWriter outFile = new FileWriter(outputFile);

        Statement stmt = null;
        ResultSet rset = null;
        try {
          stmt = connection.createStatement();
          String query = "select taxon_name, code from description_edit" 
            + " where taxon_name in (select taxon_name from taxon)"
            + " and is_manual_entry = 1"
            + " order by created desc limit 100";

          //s_log.warn("getRecentDescEdits() query:" + query);
          rset = stmt.executeQuery(query);

          int count = 0; 
          Vector<String> taxonList = new Vector<>();  // for distinctness.
          String href;
          while (rset.next()) {
            String taxonName = rset.getString("taxon_name");
            String code = rset.getString("code");
            if (taxonList.contains(taxonName) && code == null) {
              // do nothing.  Effectively a distinct.
            } else {   
              if (code == null) {
                taxonList.add(taxonName);
                href = Taxon.getTaxonUrl(connection, taxonName);
              } else {
                href = Specimen.makeLink(code);
              }
              //A.log("getRecentDescEdits() taxonName:" + taxonName + " code:" + code + " href:" + href);
              outFile.write(href + "<br/>\n");
              count++;
              if (count > MAX_DESC_EDIT_COUNT) {
                break;
              }
            }
          }
          outFile.write("<span class=\"right\"><a href=\"" + AntwebProps.getDomainApp() + "/recentDescEdits.do?action=recentDescEdits\">More &#187</a></span>");
          outFile.close();

        } finally {  
          DBUtil.close(stmt, rset, "AntwebFunctions", "genRecentDescEdits");
        }
    }

    
    public static void imageCheck() {
      String imgDir = AntwebProps.getDocRoot() + "images/";
      String pyLoc = "/Users/mark/dev/calacademy/antweb/src/py/";
      String pyInstall = "";
      if (!AntwebProps.isDevMode()) {
        pyLoc = AntwebProps.getAntwebDir() + "/src/py/";
        //pyInstall = "/usr/local/bin/";
      }
      String logDir = AntwebProps.getDocRoot() + "web/log/imageCheck/";
      String logFile = logDir + DateUtil.getFormatDateStr() + ".log";
      String command = pyInstall + "python3.6 " + pyLoc + "imageCheck.py > " + logFile;
      s_log.error("imageCheck() location needs to be corrected. Not:" + command);
      s_log.warn("imageCheck() command:" + command);
      new AntwebSystem().launchProcess(command, true);
    }


/*
    For this to be run, it needs to be run as root. There must be in the /etc/sudoers file the following:
antweb	ALL=(ALL)	NOPASSWD: /antweb/deploy/bin/admin.sh   
    You will need to "C-x C-q" in order to edit /etc/sudoers even as root.

    Either by Scheduler, or by calling: /schedule.do?action=run&num=14
    A request is fired off to /utilData.do?action=adminTasks&param=allow
    This calls AntwebFunctions.adminTasks()
    Which calls AntwebSystem.exec() with the command below
    
    Now handled by cron
    public static String adminTasks() {
      String command = "sudo " + antwebDir + "/bin/admin.sh";
      //s_log.warn("adminTasks() command:" + command);
      AntwebSystem system = new AntwebSystem();
      //system.launchProcess(command, true);
      //system.exec(command);

      try {
        system.exec("sudo " + antwebDir + "/bin/admin.sh");      
      } catch (Exception e) {
        A.log("adminTasks() e:" + e);
      }

      String message = command;

      // Verified that files are written as antweb:antweb.		
      AntwebUtil.writeDataFile("t.txt", "test");				
      
      return message;
    }

    public static String updateExif() {
      String imgDir = AntwebProps.getDocRoot() + "images/";
      String command = "exiftool -Copyright='California Academy of Sciences 2000-2019' /antweb/images/casent0005904/CASENT0005904_D.tif";  
      A.log("updateExif() command:" + command);
//      (new AntwebSystem()).launchProcess(command); //, true);
      (new AntwebSystem()).launchBuilder(command); //, true);
      String message = command;
      return message;
    }
*/

    

    public static String moveImages(String code) {
      if (code == null || code.length() <= 0) return "Enter code to move";
      code = code.toLowerCase();
      String imgDir = AntwebProps.getDocRoot() + "images/";
      Utility.makeDirTree(imgDir + "bak/");
      String command = "mv " + imgDir + code + " " + imgDir + "bak/" + code;
      s_log.warn("moveImages() command:" + command);
      new AntwebSystem().launchProcess(command, true);
      String message = command;
      return message;
    }

    public static String changeOwner(String code) {
      if (code == null || code.length() <= 0) return "Enter code to change owner of";
      code = code.toLowerCase();
      String imgDir = AntwebProps.getDocRoot() + "images/";
      String command = "chown -R apache:apache " + imgDir + code;
      s_log.warn("changeOwner() command:" + command);
      new AntwebSystem().launchProcess(command, true);
      String link = " See specimen:<a href='" + AntwebProps.getDomainApp() + "/specimen.do?code=" + code + "'>" + code + "</a>";
      String message = command + "<br>" + link;
      return message;
    }

    public static String changeOwnerAndPerms(String code) {
      if (code == null || code.length() <= 0) return "Enter code to change owner and permissions of";
      code = code.toLowerCase();
      String imgDir = AntwebProps.getDocRoot() + "images/";

      String chownCommand = "chown -R antweb:antweb " + imgDir + code;
      s_log.warn("changeOwnerAndPerms() command:" + chownCommand);
      new AntwebSystem().launchProcess(chownCommand, true);

      String permsCommand = "chmod 775 " + imgDir + code;
      s_log.warn("changeOwnerAndPerms() command:" + permsCommand);
      new AntwebSystem().launchProcess(permsCommand, true);

      String link = " See specimen:<a href='" + AntwebProps.getDomainApp() + "/specimen.do?code=" + code + "'>" + code + "</a>";
      String message = chownCommand + "<br>" + permsCommand + "<br>" + link;
      return message;
    }
    
}

