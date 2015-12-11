/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keepassj.cli;

import com.hanhuy.keepassj.CompositeKey;
import com.hanhuy.keepassj.IOConnectionInfo;
import com.hanhuy.keepassj.InvalidCompositeKeyException;
import com.hanhuy.keepassj.KcpKeyFile;
import com.hanhuy.keepassj.KcpPassword;
import com.hanhuy.keepassj.PwDatabase;
import com.hanhuy.keepassj.PwDefs;
import com.hanhuy.keepassj.PwEntry;
import com.hanhuy.keepassj.PwGroup;
import com.hanhuy.keepassj.PwObjectList;
import com.hanhuy.keepassj.SearchParameters;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author cternent
 */
public class KeepassjCli {

    private final PwDatabase db;
    
    public KeepassjCli(String dbfilename, String password, String keyfile) throws IOException {
        db = new PwDatabase();
        CompositeKey key = new CompositeKey();
	key.AddUserKey(new KcpPassword(password));
	if (keyfile!=null && ! keyfile.isEmpty()) {
            key.AddUserKey(new KcpKeyFile(keyfile));
	}
        db.Open(IOConnectionInfo.FromPath(dbfilename), key, null);
    }
    /**
     * @param args the command line arguments
     * @throws org.apache.commons.cli.ParseException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        Options options = KeepassjCli.getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (! KeepassjCli.validateOptions(cmd)) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("Usage: java -jar KeepassjCli -f dbfile [options]", options);
        } else {
            String password;
            if (cmd.hasOption('p')) {
                password = cmd.getOptionValue('p');
            } else {
                Console console = System.console();
                char[] hiddenString = console.readPassword("Enter password for %s\n", cmd.getOptionValue('f'));
                password = String.valueOf(hiddenString);
            }
            KeepassjCli instance = new KeepassjCli(cmd.getOptionValue('f'), password, cmd.getOptionValue('k'));
            System.out.println("Description:"+instance.db.getDescription());
            PwGroup rootGroup = instance.db.getRootGroup();
            System.out.println(String.valueOf(rootGroup.GetEntriesCount(true))+" entries");
            if (cmd.hasOption('l')) {
                instance.printEntries(rootGroup.GetEntries(true), false);
            } else if (cmd.hasOption('s')) {
                PwObjectList<PwEntry> results = instance.search(cmd.getOptionValue('s'));
                System.out.println("Found "+results.getUCount()+" results for:"+cmd.getOptionValue('s'));
                instance.printEntries(results, false);
            } else if (cmd.hasOption('i')) {
                System.out.println("Entering interactive mode.");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                String input = null;
                PwObjectList<PwEntry> results = null;
                while (! "\\q".equals(input)) {
                    if (results!=null) {
                        System.out.println("Would you like to view a specific entry? y/n");
                        input = bufferedReader.readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.out.print("Enter the title number:");
                            input = bufferedReader.readLine();
                            instance.printCompleteEntry(results.GetAt(Integer.parseInt(input)-1)); // Since humans start counting at 1
                        }
                        results = null;
                        System.out.println();
                    } else {
                        System.out.print("Enter something to search for (or \\q to quit):");
                        input = bufferedReader.readLine();
                        if (! "\\q".equalsIgnoreCase(input)) {
                            results = instance.search(input);
                            instance.printEntries(results, true);
                        }
                    }
                }
            }
            
            // Close before exit
            instance.db.Close();
        }
    }
    
    private static Options getOptions() {
        Options options = new Options();
        options.addOption("f", true, "keepass db file to load");
        options.addOption("h", false, "this help");
        options.addOption("i", false, "interactive mode");
        options.addOption("k", true, "keepass key file to load");
        options.addOption("l",false, "list entries and exit");
        options.addOption("p", true, "keepass db password (Prompting is more secure!)");
        options.addOption("s", true, "search entries for string");
        options.addOption("v", false, "verbose output");
        return options;
    }
    
    private static boolean validateOptions(CommandLine cmd) {
        boolean okay = true;
        if (cmd.hasOption('h') || cmd.hasOption('?')) {
            okay = false;
        } else if (!cmd.hasOption('f')) {
            System.err.println("ERROR: Missing db filename");
            okay = false;
        }
        return okay;
    }

    private PwObjectList<PwEntry> search(String s) {
        SearchParameters sp = new SearchParameters();
        sp.setSearchString(s);
        PwObjectList<PwEntry> results = new PwObjectList<>();
        db.getRootGroup().SearchEntries(sp, results);
        return results;
    }    

    private void printEntries(PwObjectList<PwEntry> entryList, boolean numberEntries) {
        if (entryList!=null) {
            int count=1;
            for (PwEntry entry:entryList) {
                System.out.println("------------------------------------------------------------------------------");
                System.out.println(String.valueOf(count++)+")\tTitle:"+entry.getStrings().Get(PwDefs.TitleField).ReadString());
                System.out.println("\tUsername:"+entry.getStrings().Get(PwDefs.UserNameField).ReadString());
                System.out.println("\tURL:"+entry.getStrings().Get(PwDefs.UrlField).ReadString());
            }
            System.out.println("------------------------------------------------------------------------------");
        }
    }
    
    private void printCompleteEntry(PwEntry entry) {
        if (entry!=null) {
            for (String key:entry.getStrings().GetKeys()) {
                System.out.println(key+":"+entry.getStrings().Get(key).ReadString());
            }
        }
    }
}
