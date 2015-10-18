# keepassj-cli
Command line interface for the keepassj library by pfn
# Usage
```
java -jar KeepassjCli.jar -f dbfile [options]
 -f <arg>   keepass db file to load
 -h         this help
 -i         interactive mode
 -k <arg>   keepass key file to load
 -l         list entries and exit
 -p <arg>   keepass db password (Prompting is more secure!)
 -s <arg>   search entries for string
 -v         verbose output
```
# Example
```
java -jar keepassj-cli.jar -f dbfile.kdbx -i
Enter password for dbfile.kdbx
SuperSecretPassword
Description:all my codes and phrases to remember
175 entries
Entering interactive mode.
Enter something to search for (or \q to quit):git
------------------------------------------------------------------------------
1)      Title:github
        Username:trarman
        URL:https://github.com/
------------------------------------------------------------------------------
2)      Title:work git
        Username:work.user
        URL:https://gitforwork.com/
------------------------------------------------------------------------------
Would you like to view a specific entry? y/n
y
Enter the title number:1
Password:ImpossibleToMemorizePassword
Title:github
URL:https://github.com/
UserName:trarman

Enter something to search for (or \q to quit):\q
```
