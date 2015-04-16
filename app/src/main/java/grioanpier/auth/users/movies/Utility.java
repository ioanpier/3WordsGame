package grioanpier.auth.users.movies;

import java.util.UUID;

/**
 * Created by Ioannis on 10/4/2015.
 */
public class Utility {

    public static String getStackTraceString (StackTraceElement[] stackTraceElements){
        StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement element : stackTraceElements)
            stackTrace.append(element.toString()).append('\n');
        return  stackTrace.toString();
    }

    public static UUID[] getUUIDsFromStrings(String[] uuidStrings){
        UUID[] uuids = new UUID[uuidStrings.length];
        for (int i=0; i< uuidStrings.length; i++)
            uuids[i] = UUID.fromString(uuidStrings[i]);
        return uuids;
    }
}
