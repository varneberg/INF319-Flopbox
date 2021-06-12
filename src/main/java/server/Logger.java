package server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.LogRecord;

public class Logger {
    private File logfile;
    private String logName="flopbox.log";
    private static String logpath = "./src/main/resources/Log";

    public void initLog(){
        createLogFile();
    }

    public void createLogFile(){
        logfile = new File(logpath+logName);
        if(!logfile.exists()){
            try {
                logfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setLogfile(logfile);
    }

    public void appendLog(int logLevel, String logMessage){
        logfile = getLogfile();
        try {
            FileWriter writer = new FileWriter(logfile);
            String logString = "\n"+getLogLevelMessage(logLevel) + logMessage;
            writer.write(logString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendError(int logLevel, String clientAddress, String function, String logMessage){
        logfile = getLogfile();
        try {
            FileWriter writer = new FileWriter(logfile);
            String logString = "\n"
                    + getLogLevelMessage(logLevel)
                    + "\t" + clientAddress
                    + "\t"+ function
                    + "\t"+ logMessage;
            writer.write(logString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogLevelMessage(int logLevel){
        switch (logLevel){
            case 2:
                return "[Warning]:\t";
            case 3:
                return "[Critical]:\t";
            default:
                return "[Info]:\t";
        }
    }

    public void setLogfile(File logfile) {
        this.logfile = logfile;
    }

    public File getLogfile() {
        return logfile;
    }
}
