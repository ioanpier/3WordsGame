package grioanpier.auth.users.movies;

/**
 * Created by Ioannis on 10/4/2015.
 */
public class Utility {

    public static String getStackTraceString (StackTraceElement[] stackTraceElements){
        StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement element : stackTraceElements)
            stackTrace.append(element.toString() + '\n');
        return  stackTrace.toString();
    }
}
