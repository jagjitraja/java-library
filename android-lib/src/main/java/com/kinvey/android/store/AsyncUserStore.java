package com.kinvey.android.store;


import android.content.Intent;
import android.net.Uri;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserDeleteCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.android.ui.MICLoginActivity;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.GetMICTempURL;
import com.kinvey.java.store.requests.user.LoginToTempURL;
import com.kinvey.java.store.requests.user.LogoutRequest;

import java.io.IOException;

/**
 * Wraps the {@link UserStore} public methods in asynchronous functionality using native Android AsyncTask.
 * <p/>
 * <p>
 * This functionality can be accessed through the static call methods of this class.
 * UserStore manages user's state.
 * <p/>
 * Methods in this API use either {@link KinveyClientCallback} for returning response status,
 * </p>
 * <p>
 * Entity Set sample:
 * <pre>
 * {@code
 * AsyncUserStore.retrieve(client, new KinveyUserCallback<User>() {
 *     @Override
 *     public void onSuccess(User result) {
 *     }
 *     @Override
 *     public void onFailure(Throwable error) {
 *     }
 * });
 * }
 * </pre>
 * </p>
 * <p/>
 * @author Prots
 */
public class AsyncUserStore {

    private static boolean clearStorage = true;
    private static KinveyUserCallback MICCallback;
    private static String MICRedirectURI;

    /**
     * Create User asynchronous
     *
     * <p>
     * Retrieving user metadata and updating the current user with the metadata.
     * Used when initializing the client.
     * </p>
     *
     * @param username username
     * @param password password
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     *
     */
    public static void signUp(String username, String password, AbstractClient client, KinveyClientCallback callback) {
        new Create(username, password, client, callback).execute();
    }

    /**
     * Login asynchronous
     *
     * <p>
     * Retrieving user metadata and updating the current user with the metadata.
     * Used when initializing the client.
     * </p>
     *
     * @param userId userId
     * @param password password
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void login(String userId, String password, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(userId, password, client, callback).execute();
    }

    /**
     * Login asynchronous using Facebook access token
     *
     * <p>
     * Login asynchronous using Facebook access token obtained through OAuth2.
     * If the user does not exist in the Kinvey service, the user will be created.
     * </p>
     *
     * @param accessToken Facebook access token obtained through OAuth2
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginFacebook(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute();
    }

    /**
     * Login asynchronous using Google access token
     *
     * <p>
     * Login asynchronous to Kinvey services using Google access token obtained through OAuth2.
     * If the user does not exist in the Kinvey service, the user will be created.
     * </p>
     *
     * @param accessToken Google access token obtained through OAuth2
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginGoogle(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute();
    }

    /**
     * Login asynchronous using Twitter-generated access token
     *
     * <p>
     * Login asynchronous to Kinvey services using Twitter-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *</p>
     *
     * @param accessToken Twitter-generated access token
     * @param accessSecret Twitter-generated access secret
     * @param consumerKey Twitter-generated consumer key
     * @param consumerSecret Twitter-generated consumer secret
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginTwitter(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.TWITTER, callback).execute();
    }

    /**
     * Login asynchronous using LinkedIn-generated access token
     *
     * <p>
     * Login asynchronous to Kinvey services using LinkedIn-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *</p>
     *
     * @param accessToken Linked In generated access token
     * @param accessSecret Linked In generated access secret
     * @param consumerKey Linked In generated consumer key
     * @param consumerSecret Linked In generated consumer secret
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginLinkedIn(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.LINKED_IN, callback).execute();
    }

    /**
     * Login asynchronous to Kinvey Services used AuthLink
     *
     * <p>
     * Login asynchronous to Kinvey Services using AuthLink-generated access token, access secret,
     * If the user does not exist in the Kinvey service, the user will be created.
     *</p>
     *
     * @param accessToken AuthLink generated accessToken
     * @param refreshToken AuthLink generated  refreshToken
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginAuthLink(String accessToken, String refreshToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, refreshToken,UserStoreRequestManager.LoginType.AUTH_LINK, client, callback).execute();
    }

    /**
     * Login asynchronous using SalesForce-generated access token
     *
     * <p>
     * Login to Kinvey services using SalesForce access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     * </p>
     *
     * @param accessToken SalesForce-generated access token
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginSalesForce(String accessToken, String client_id, String refreshToken, String id, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, client_id, refreshToken, id, client, UserStoreRequestManager.LoginType.SALESFORCE, callback).execute();
    }

    /**
     * Login to Kinvey Services using Mobile Identity Connect-generated access token
     *
     * <p>
     * Login to Kinvey Services using Mobile Identity Connect-generated access token.
     * If the user does not exist in the Kinvey service, the user will be created.
     * </p>
     *
     * @param accessToken Mobile Identity Connect-generated access token
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void loginMobileIdentity(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.MOBILE_IDENTITY, client, callback).execute();
    }

    /**
     * Login asynchronous using Kinvey credentials
     *
     * @param credential Kinvey credentials
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     * @throws IOException
     *
     */
    public static void login(Credential credential, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(credential, client, callback).execute();
    }

    /**
     * Login asynchronous using using a Kinvey user's _id and their valid Kinvey Auth Token
     *
     * <p>
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.
     * This method is provided to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     * </p>
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     *
     */
    public void loginKinveyAuthToken(String userId, String authToken, AbstractClient client, KinveyClientCallback callback){
        new LoginKinveyAuth(userId, authToken, client, callback).execute();
    }

    /**
     * Logs the user out of the current app asynchronous
     *
     * @param client Kinvey client instance
     */
    public static void logout(AbstractClient client) {
        if(clearStorage) {
            client.performLockDown();
        }
        new LogoutRequest(client).execute();
    }

    /**
     * Delete current Kinvey user asynchronous.
     *
     * @param isHard if true, physically deletes the user. If false, marks user as inactive.
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback} contains response's status
     *
     */
    public static void destroy(boolean isHard, AbstractClient client, KinveyUserDeleteCallback callback) {
        new Delete(isHard, client,callback).execute();
    }

    /**
     * Set a flag to allow local offline storage to persist after calls to logout.
     * <p/>
     * Only use this method if each device will have a guaranteed consistent user and there are no concerns about security
     */
    public void keepOfflineStorageOnLogout(){
        clearStorage = false;
    }

    /**
     * Send email confirmation for the current user asynchronous
     *
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void sendEmailConfirmation(AbstractClient client, KinveyUserManagementCallback callback) {
        new EmailVerification(client, callback).execute();
    }

    /**
     * Forgot username asynchronous using user's email
     *
     * @param client Kinvey client instance
     * @param email User's email
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void forgotUsername(AbstractClient client, String email, KinveyUserManagementCallback callback) {
        new ForgotUsername(client, email, callback).execute();
    }

    /**
     * Reset user's password using user's name or email asynchronous.
     *
     * @param usernameOrEmail User's name or email
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void resetPassword(String usernameOrEmail, AbstractClient client, KinveyUserManagementCallback callback) {
        new ResetPassword(usernameOrEmail, client, callback).execute();
    }

    /**
     * Check exist user using user's name asynchronous.
     *
     * @param username User's name
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void exists(String username, AbstractClient client, KinveyUserManagementCallback callback) {
        new ExistsUser(username, client, callback).execute();
    }

    /**
     * Change user's password asynchronous
     *
     * @param password New password
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void changePassword(String password, AbstractClient client, KinveyUserManagementCallback callback) {
        new ChangePassword(password, client, callback).execute();
    }

    /**
     * Get user using user's id asynchronous
     *
     * @param userId User's id
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserManagementCallback} contains result's status
     *
     */
    public static void get(String userId, AbstractClient client, KinveyUserManagementCallback callback) {
        new GetUser(userId, client, callback).execute();
    }

    /**
     * Save current user asynchronous
     *
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback<User>} containing a refreshed User instance.
     *
     */
    public void save(AbstractClient client,KinveyClientCallback<User> callback) {
        new Update(client, callback).execute();
    }

    /**
     * Asynchronous Retrieve Metadata
     *
     * <p>
     * Convenience method for retrieving user metadata and updating the current user with the metadata.
     * Used when initializing the client.
     * </p>
     *
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback<User>} containing a refreshed User instance.
     */
    public static void convenience(AbstractClient client,KinveyClientCallback<User> callback) {
        new RetrieveMetaData(client, callback).execute();
    }

    /**
     * Asynchronous Call to Retrieve (refresh) the current user
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     AsyncUserStore.retrieve(kinveyClient, new KinveyClientCallback<User>() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback<User>} containing a refreshed User instance.
     */
    public static void retrieve(AbstractClient client, KinveyClientCallback<User> callback) {
        new Retrieve(client, callback).execute();
    }

    /**
     * Asynchronous call to retrive (refresh) the current user, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     AsyncUserStore.retrieve(new String[]{"myKinveyReferencedField"}, new KinveyClientCallback<User>() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param client Kinvey client instance
     * @param callback {@link KinveyClientCallback<User>} containing a refreshed User instance.
     */
    public static void retrieve(String[] resolves, AbstractClient client, KinveyClientCallback<User> callback){
        new Retrieve(resolves, client, callback).execute();
    }

    /**
     * Asynchronous call to retrive (refresh) the users by query, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    AsyncUserStore.retrieve(Query query, new String[]{"myKinveyReferenceField"}, new KinveyUserListCallback() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     *
     *
     *
     * @param query the query to execute defining users to return
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param client Kinvey client instance
     * @param callback {@link com.kinvey.android.callback.KinveyUserListCallback} containing an array of queried users
     */
    public static void retrieve(Query query, String[] resolves, AbstractClient client, KinveyUserListCallback callback){
        new RetrieveUserList(query, resolves, client, callback).execute();
    }

    /**
     * Asynchronous Call to Retrieve users via a Query
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    AsyncUserStore.retrieve(Query query, new KinveyUserListCallback() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     * @param q the query to execute defining users to return
     * @param client Kinvey client instance
     * @param callback {@link com.kinvey.android.callback.KinveyUserListCallback} for retrieved users
     */
    public static void retrieve(Query q, AbstractClient client, KinveyListCallback callback) {
        new RetrieveUserList(q, client, callback).execute();
    }



    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
     *
     * @param redirectURI redirectURI
     * @param client Kinvey client instance
     * @param callback {@link KinveyMICCallback}
     */
    public static void loginWithAuthorizationCodeLoginPage(Client client, String redirectURI, KinveyMICCallback callback){
        //return URL for login pagef
        //https://auth.kinvey.com/oauth/auth?client_id=<your_app_id>i&redirect_uri=<redirect_uri>&response_type=code
        String appkey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String host = client.getMICHostName();
        String apiVersion = client.getMICApiVersion();
        if (apiVersion != null && apiVersion.length() > 0){
            host = client.getMICHostName() + apiVersion + "/";
        }
        String myURLToRender = host + "oauth/auth?client_id=" + appkey + "&redirect_uri=" + redirectURI + "&response_type=code";
        //keep a reference to the callback and redirect uri for later

        MICCallback = callback;
        MICRedirectURI = redirectURI;

        if (callback != null){
            callback.onReadyToRender(myURLToRender);
        }

    }

    /**
     * Used by the MIC login flow, this method should be called after a successful login in the onNewItent Method of your activity.  See the MIC guide for more information.
     *
     * @param intent The intent provided to the application from the redirect
     * @param client Kinvey client instance
     */
    public static void onOAuthCallbackRecieved(Intent intent, AbstractClient client){
        if (intent == null || intent.getData() == null){
            return;
        }
        final Uri uri = intent.getData();
        String accessToken = uri.getQueryParameter("code");
        if (accessToken == null){
            return;
        }
        getMICAccessToken(accessToken, client);
    }

    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
     *
     * @param username Kinvey user's name
     * @param password Kinvey user's password
     * @param redirectURI url for redirect after success login
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserCallback}
     */
    public static void loginWithAuthorizationCodeAPI(AbstractClient client, String username, String password, String redirectURI, KinveyUserCallback<User> callback){
        MICCallback = callback;

        new PostForTempURL(client, redirectURI, username, password, callback).execute();
    }

    /**
     * Posts for a MIC login Access token
     *
     * @param token the access code returned from the MIC Auth service
     * @param client Kinvey client instance
     */
    private static void getMICAccessToken(String token, AbstractClient client){
        new PostForAccessToken(client, MICRedirectURI, token, (KinveyClientCallback) MICCallback).execute();
    }

    /***
     * Initiate the MIC login flow with an Activity containing a Webview
     *
     * @param redirectURI url for redirect after success login
     * @param client Kinvey client instance
     * @param callback {@link KinveyUserCallback}
     */
    public static void presentMICLoginActivity(final Client client, String redirectURI, final KinveyUserCallback<User> callback){

        loginWithAuthorizationCodeLoginPage(client, redirectURI, new KinveyMICCallback() {
            @Override
            public void onReadyToRender(String myURLToRender) {
                Intent i = new Intent(client.getContext(), MICLoginActivity.class);
                i.putExtra(MICLoginActivity.KEY_LOGIN_URL, myURLToRender);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                client.getContext().startActivity(i);
            }

            @Override
            public void onSuccess(User result) {
                if(callback != null){
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onSuccess(Object result) {
//                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable error) {
                if(callback != null){
                    callback.onFailure(error);
                }
            }
        });
    }


    /**
     * Create asynchronously Login request using synchronous UserStore's method
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback} for returning User
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      Login login = new Login(credential, client, callback);
     *      login.execute()
     * }
     * </pre>
     * </p>
     */
    private static class Login extends AsyncClientRequest<User> {

        String username;
        String password;
        String accessToken;
        String refreshToken;
        String accessSecret;
        String consumerKey;
        String consumerSecret;
        Credential credential;
        UserStoreRequestManager.LoginType type;
        AbstractClient client;

        //Salesforce...
        String id;
        String client_id;

        /**
         * Constructor to instantiate the Login request.
         *
         * @param client Kinvey client instance
         * @param client {@link KinveyClientCallback}
         */
        private Login(AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.IMPLICIT;
        }

        /**
         * Constructor to instantiate the Login request using Kinvey's credentials.
         *
         * @param username User's name
         * @param password User's password
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(String username, String password, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.username = username;
            this.password = password;
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.KINVEY;
        }

        /**
         * Constructor to instantiate the Login request using Facebook access token.
         *
         * @param accessToken Facebook access token obtained through OAuth2
         * @param type {@link com.kinvey.java.store.UserStoreRequestManager.LoginType} Enum for identify which login type is using for authentication
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(String accessToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.type = type;
            
            this.client = client;
        }

        /**
         * Constructor to instantiate the Login request using AuthLink access token.
         *
         * @param accessToken AuthLink generated accessToken
         * @param refreshToken AuthLink generated  refreshToken
         * @param type {@link com.kinvey.java.store.UserStoreRequestManager.LoginType} Enum for identify which login type is using for authentication
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(String accessToken, String refreshToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.type = type;
            this.client = client;
        }

        /**
         * Constructor to instantiate the Login request using LinkedIn access token.
         *
         * @param accessToken LinkedIn generated access token
         * @param accessSecret LinkedIn generated access secret
         * @param consumerKey LinkedIn generated consumer key
         * @param consumerSecret LinkedIn generated consumer secret
         * @param type {@link com.kinvey.java.store.UserStoreRequestManager.LoginType} Enum for identify which login type is using for authentication
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client,
                      UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
            this.client = client;
        }

        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        /**
         * Method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
         *
         * @param accessToken access token
         * @param clientID Salesforce clientID
         * @param refresh refresh
         * @param id Salesforce id
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(String accessToken, String clientID, String refresh, String id, AbstractClient client, KinveyClientCallback callback){
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refresh;
            this.client_id = clientID;
            this.id = id;
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.SALESFORCE;
        }

        /**
         * Constructor to instantiate the Login request using Kinvey user's credential.
         *
         * @param credential Kinvey user's credential
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback}
         */
        private Login(Credential credential, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.credential = credential;
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE;
        }

        /**
         * Async execute UserStore's synchronous Login request
         */
        @Override
        protected User executeAsync() throws IOException {
            switch(this.type) {
                case IMPLICIT:
                    return UserStore.login(client);
                case KINVEY:
                    return UserStore.login(username, password, client);
                case FACEBOOK:
                    return UserStore.loginFacebook(accessToken, client);
                case GOOGLE:
                    return UserStore.loginGoogle(accessToken, client);
                case TWITTER:
                    return UserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client);
                case LINKED_IN:
                    return UserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client);
                case AUTH_LINK:
                    return UserStore.loginAuthLink(accessToken, refreshToken, client);
                case SALESFORCE:
                    return UserStore.loginSalesForce(accessToken, client_id, refreshToken, id, client);
                case MOBILE_IDENTITY:
                    return UserStore.loginMobileIdentity(accessToken, client);
                case CREDENTIALSTORE:
                    return UserStore.login(credential, client);
            }
            return null;
        }
    }

    /**
     * Create asynchronously Create user request using synchronous UserStore's method UserStore.signUp
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<User>} for returning User
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new Create(username, password, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class Create extends AsyncClientRequest<User> {
        String username;
        String password;
        private final AbstractClient client;

        /**
         * Constructor to instantiate the Create request using user's name and password.
         *
         * @param username user's name for creating new user
         * @param password user's password for creating new user
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User>}
         */
        private Create(String username, String password, AbstractClient client, KinveyClientCallback<User> callback) {
            super(callback);
            this.username=username;
            this.password=password;
            this.client = client;
            
        }

        /**
         * Async execute UserStore's synchronous Create request
         */
        @Override
        protected User executeAsync() throws IOException {
            return UserStore.signUp(username, password, client);
        }
    }

    /**
     * Create asynchronously Delete user request using synchronous UserStore's method  UserStore.destroy
     * <p/>
     * Methods in this API use either {@link KinveyUserDeleteCallback} for returning response's status
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new Delete(isHard, client,callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class Delete extends AsyncClientRequest<Void> {
        boolean hardDelete;
        private final AbstractClient client;

        /**
         * Constructor to instantiate the Delete request for deleting current user.
         *
         * @param hardDelete if true, physically deletes the user. If false, marks user as inactive.
         * @param client Kinvey client instance
         * @param callback {@link KinveyUserDeleteCallback}
         */
        private Delete(boolean hardDelete,  AbstractClient client, KinveyUserDeleteCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
            this.client = client;
        }

        /**
         * Async execute UserStore's synchronous Delete request
         */
        @Override
        protected Void executeAsync() throws IOException {
            UserStore.destroy(hardDelete, client);
            return null;
        }
    }

    /**
     * Create asynchronously PostAccessToken request for Login using synchronous UserStore's method  UserStore.loginMobileIdentity.
     * Used by the MIC login flow.
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<User>} for returning User
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new PostForAccessToken(client, MICRedirectURI, token, (KinveyClientCallback) MICCallback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class PostForAccessToken extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final String redirectURI;
        private String token;

        /**
         * Constructor to instantiate the PostAccessToken request for getting User
         *
         * @param redirectURI url for redirect after success login
         * @param token the access code returned from the MIC Auth service
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User>}
         */
        public PostForAccessToken(AbstractClient client, String redirectURI, String token, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
            this.redirectURI = redirectURI;
            this.token = token;
        }

        /**
         * Async execute UserStore's synchronous UserStore.loginMobileIdentity method
         */
        @Override
        protected User executeAsync() throws IOException {
            UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GenericJson result = requestManager.getMICToken(token).execute();

            User ret =  UserStore.loginMobileIdentity(result.get("access_token").toString(), client);

            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(result.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);

            return ret;
        }
    }

    /**
     * Create asynchronously PostForTempURL request for Login with the MIC service, using the oauth flow.
     * This request provides direct login, without rending a login page.
     * <p/>
     * Methods in this API use either {@link KinveyUserCallback<User>} for returning User
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new PostForTempURL(client, redirectURI, username, password, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class PostForTempURL extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final String redirectURI;
        String username;
        String password;

        /**
         * Constructor to instantiate the PostForTempURL request for Login with the MIC service
         *
         * @param client Kinvey client instance
         * @param redirectURI url for redirect after success login
         * @param username user's name for login
         * @param password user's password for login
         * @param callback {@link KinveyUserCallback<User>}
         */
        public PostForTempURL(AbstractClient client, String redirectURI, String username, String password, KinveyUserCallback<User> callback) {
            super(callback);
            this.client = client;
            this.redirectURI = redirectURI;
            this.username=username;
            this.password=password;
        }

        /**
         * Async execute PostForTempURL request
         */
        @Override
        protected User executeAsync() throws IOException {
            UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GetMICTempURL micTempURL = requestManager.getMICTempURL();
            GenericJson tempResult = micTempURL.execute();
            String tempURL = tempResult.get("temp_login_uri").toString();
            LoginToTempURL loginToTempURL = requestManager.MICLoginToTempURL(username, password, tempURL);
            GenericJson accessResult = loginToTempURL.execute();
            User user = UserStore.loginMobileIdentity(accessResult.get("access_token").toString(), client);
            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(accessResult.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);
            return user;
        }
    }

    /**
     * Asynchronous request to refresh current user's data via the Kinvey back-end.
     * <p/>
     * Methods in this API use either {@link KinveyUserCallback<User>} for returning User
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new Retrieve(client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class Retrieve extends AsyncClientRequest<User> {

        private String[] resolves = null;
        private final AbstractClient client;

        /**
         * Constructor to instantiate the PostForTempURL request for returning User's data
         *
         * @param client Kinvey client instance
         * @param callback {@link KinveyUserCallback<User>}
         */
        private Retrieve(AbstractClient client,KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
        }

        /**
         * Constructor to instantiate the PostForTempURL request for returning User's data
         *
         * @param resolves an array of json keys maintaining KinveyReferences to be resolved
         * @param client Kinvey client instance
         * @param callback {@link KinveyUserCallback<User>}
         */
        private Retrieve(String[] resolves, AbstractClient client, KinveyClientCallback<User> callback){
            super(callback);
            this.resolves = resolves;
            this.client = client;
        }

        /**
         * Async execute Retrieve request
         */
        @Override
        public User executeAsync() throws IOException {
            if (resolves == null){
                return UserStore.retrieve(client);
            }else{
                return UserStore.retrieve(resolves, client);
            }
        }
    }

    /**
     * Asynchronous request to retrieve User objects via a Query.
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<User[]>} contains an array of queried users
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new RetrieveUserList(q, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class RetrieveUserList extends AsyncClientRequest<User[]> {

        private Query query = null;
        private String[] resolves = null;
        private final AbstractClient client;

        /**
         * Constructor to instantiate the RetrieveUserList request
         *
         * @param query the query to execute defining users to return
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User[]>} contains an array of queried users
         */
        private RetrieveUserList(Query query, AbstractClient client,KinveyClientCallback<User[]> callback){
            super(callback);
            this.query = query;
            this.client = client;
        }

        /**
         * Constructor to instantiate the RetrieveUserList request
         *
         * @param query the query to execute defining users to return
         * @param resolves an array of json keys maintaining KinveyReferences to be resolved
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User[]>} contains an array of queried users
         */
        private RetrieveUserList(Query query, String[] resolves, AbstractClient client, KinveyClientCallback<User[]> callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
            this.client = client;
        }

        /**
         * Async execute RetrieveUserList request
         */
        @Override
        public User[] executeAsync() throws IOException {
            if (resolves == null){
                return UserStore.retrieve(query, client);
            }else{
                return UserStore.retrieve(query, resolves, client);
            }
        }
    }

    /**
     * Asynchronous request for retrieving user metadata and updating the current user with the metadata
     * using UserStore.convenience
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<User>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new RetrieveMetaData(client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class RetrieveMetaData extends AsyncClientRequest<User> {

        private final AbstractClient client;

        /**
         * Constructor to instantiate the RetrieveMetaData request
         *
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User>}
         */
        private RetrieveMetaData(AbstractClient client, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
        }

        /**
         * Async execute RetrieveMetaData request
         */
        @Override
        protected User executeAsync() throws IOException {
            return UserStore.convenience(client);
        }
    }

    /**
     * Asynchronous request for saving/updating user metadata
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<User>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new Update(client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class Update extends AsyncClientRequest<User> {

        AbstractClient client = null;

        /**
         * Constructor to instantiate the Update request
         *
         * @param client Kinvey client instance
         * @param callback {@link KinveyClientCallback<User>}
         */
        private Update(AbstractClient client, KinveyClientCallback<User> callback){
            super(callback);
            this.client = client;
        }

        /**
         * Async execute Update request
         */
        @Override
        protected User executeAsync() throws IOException {
            return UserStore.save(client);
        }
    }

    /**
     * Asynchronous request changing user's password
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<Void>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new ChangePassword(password, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class ChangePassword extends AsyncClientRequest<Void> {

        private final String password;
        AbstractClient client = null;
        

        private ChangePassword(String password, AbstractClient client, KinveyClientCallback<Void> callback){
            super(callback);
            this.password = password;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.changePassword(password, client);
            return null;
        }
    }

    /**
     * Asynchronous request reset user's password using user's name or email.
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<Void>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new ResetPassword(usernameOrEmail, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class ResetPassword extends AsyncClientRequest<Void> {

        String usernameOrEmail;
        private final AbstractClient client;
        

        private ResetPassword(String usernameOrEmail, AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.usernameOrEmail = usernameOrEmail;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.resetPassword(usernameOrEmail, client);
            return null;
        }
    }

    /**
     * Asynchronous request for check existing user using user's name .
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<Void>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new ExistsUser(username, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class ExistsUser extends AsyncClientRequest<Void> {

        String username;
        private final AbstractClient client;
        
        private ExistsUser(String username, AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.username = username;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.exists(username, client);
            return null;
        }
    }

    /**
     * Asynchronous request for getting user using user's id.
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new GetUser(userId, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class GetUser extends AsyncClientRequest {

        String userId;
        private final AbstractClient client;
        

        private GetUser(String userId, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.userId = userId;
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            UserStore.get(userId, client);
            return null;
        }
    }

    /**
     * Asynchronous request for sending email confirmation for the current user.
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<Void>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new EmailVerification(client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class EmailVerification extends AsyncClientRequest<Void> {

        private final AbstractClient client;
        

        private EmailVerification(AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.sendEmailConfirmation(client);
            return null;
        }
    }

    /**
     * Asynchronous request for forgetting username using user's email
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback<Void>} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new ForgotUsername(client, email, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private static class ForgotUsername extends AsyncClientRequest<Void> {

        private final AbstractClient client;
        
        private final String email;

        private ForgotUsername(AbstractClient client, String email, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;
            
            this.email = email;
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.forgotUsername(client, email);
            return null;
        }
    }

    /**
     * Asynchronous request for Login using using a Kinvey user's _id and their valid Kinvey Auth Token
     * <p/>
     * Methods in this API use either {@link KinveyClientCallback} for returning User data
     * </p>
     * <p>
     * Entity Set sample:
     * <pre>
     * {@code
     *      new LoginKinveyAuth(userId, authToken, client, callback).execute();
     * }
     * </pre>
     * </p>
     */
    private class LoginKinveyAuth extends AsyncClientRequest<User> {

        private String authToken;
        private final AbstractClient client;
        
        private String userID;

        private LoginKinveyAuth(String userId, String authToken, AbstractClient client, KinveyClientCallback callback){
            super(callback);
            this.userID = userId;
            this.authToken = authToken;
            this.client = client;
        }

        @Override
        protected User executeAsync() throws IOException {
            return UserStore.loginKinveyAuthToken(userID, authToken, client);

        }
    }

    /**
     * Create Builder for UserStoreRequestManager
     *
     * @param client Kinvey client instance
     * @return KinveyAuthRequest.Builder
     */
    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}
