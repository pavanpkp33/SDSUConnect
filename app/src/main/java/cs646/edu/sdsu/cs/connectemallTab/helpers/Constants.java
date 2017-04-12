package cs646.edu.sdsu.cs.connectemallTab.helpers;

/**
 * Created by Pkp on 3/18/2017.
 */

public class Constants {
    public static String BASE_URL = "http://bismarck.sdsu.edu/hometown";
    public static String BASE_URL_USERS = "http://bismarck.sdsu.edu/hometown/users";
    public static String BASE_URL_USERS_REVERSE = "http://bismarck.sdsu.edu/hometown/users?reverse=true";
    public static String URL_FETCH_COUNTRIES = "http://bismarck.sdsu.edu/hometown/countries";
    public static String URL_ADD_USER = "http://bismarck.sdsu.edu/hometown/adduser";
    public static String URL_CHECK_NICKNAME = "http://bismarck.sdsu.edu/hometown/nicknameexists?name=";
    public static String NEXT_ID = "http://bismarck.sdsu.edu/hometown/nextid";


    public static String KEY_ID = "id";
    public static String KEY_NICKNAME = "nickname";
    public static String KEY_CITY = "city";
    public static String KEY_STATE = "state";
    public static String KEY_COUNTRY = "country";
    public static String KEY_YEAR = "year";
    public static String KEY_LONGITUDE = "longitude";
    public static String KEY_LATITUDE = "latitude";
    public static String KEY_TIMESTAMP = "time_stamp";

    public static int MIN_REC_COUNT = 100;
    public static String PREF_NEXTID = "nextid";
    public static String NEXTID_KEY = "existingid";


    public static String QUERY_ID_MAX = "SELECT COALESCE(MAX(id), 0) AS NEXTID FROM USERS";
    public static String QUERY_CHECK_RANGE = "SELECT COUNT(*) FROM USERS WHERE ID >= ? AND ID < ?";
    public static String QUERY_GET_RANGE = "SELECT * FROM USERS WHERE ID >= ? AND ID < ? ORDER BY ID DESC";
}
