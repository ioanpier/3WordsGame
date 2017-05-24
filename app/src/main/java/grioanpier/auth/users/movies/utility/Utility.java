package grioanpier.auth.users.movies.utility;
/*
Copyright {2016} {Ioannis Pierros (ioanpier@gmail.com)}

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
