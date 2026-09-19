package Farme_rich.Security;

import java.util.List;
import java.util.Map;

public class RolePaths {
    private RolePaths(){}


    public static final Map<String,List<String>> PATH_ROLES = Map.of(

            "/api/seller/Save",
            List.of("PAID"),


            "/api/seller/ServiceInventory",
            List.of("PAID"),


            "/api/seller/UserBehaviour",
            List.of("PAID","LOGGEDUSER")

    );



    public static List<String> getRoles(String path){

        return PATH_ROLES.getOrDefault(
                path,
                List.of()
        );

    }


    public static boolean requiresRole(String path){

        return PATH_ROLES.containsKey(path);

    }
}
