/*
 * Central Repository
 *
 * Copyright 2015-2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.centralrepository.datamodel;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coordinationservice.CoordinationService;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;

/**
 * Contains business logic for saving and validating settings for central repo
 */
public class CentralRepoDbManager {

    private static final Logger logger = Logger.getLogger(CentralRepoDbManager.class.getName());

    private static final String CENTRAL_REPO_DB_NAME = "central_repository";



    private static CentralRepoDbChoice SAVED_CHOICE = null;

    /**
     * Save the selected platform to the config file.
     */
    public static CentralRepoDbChoice saveDbChoice(CentralRepoDbChoice choice) {
        choice = (choice == null) ? CentralRepoDbChoice.DISABLED : choice;
        SAVED_CHOICE = choice;
        ModuleSettings.setConfigSetting("CentralRepository", "db.selectedPlatform", choice.getSettingKey());
        return choice;
    }

    /**
     * Load the selectedPlatform boolean from the config file, if it is set.
     */
    public static CentralRepoDbChoice getSavedDbChoice() {
        if (SAVED_CHOICE == null) {
            String selectedPlatformString = ModuleSettings.getConfigSetting("CentralRepository", "db.selectedPlatform"); // NON-NLS
            SAVED_CHOICE = fromKey(selectedPlatformString);    
        }

        return SAVED_CHOICE;
    }


    private static CentralRepoDbChoice fromKey(String keyName) {
        for (CentralRepoDbChoice dbChoice : CentralRepoDbChoice.CHOICES) {
            if (dbChoice.getSettingKey().equalsIgnoreCase(keyName)) {
                return dbChoice;
            }
        }

        return CentralRepoDbChoice.DISABLED;
    }


    
    /**
     * obtains the database connectivity for central repository
     *
     * @return the CentralRepository object to connect to
     * @throws CentralRepoException
     */
    private static CentralRepository obtainCentralRepository() throws CentralRepoException {
        //get connection
        try {
            return CentralRepository.getInstance();
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, "Error updating central repository, unable to make connection", ex);
            onUpgradeError("Error updating central repository, unable to make connection",
                    Bundle.EamDbUtil_centralRepoConnectionFailed_message() + Bundle.EamDbUtil_centralRepoDisabled_message(), ex);
        }

        // will never be reached
        return null;
    }

    /**
     * obtains central repository lock
     *
     * @param db the database connection
     * @return the lock if acquired
     * @throws CentralRepoException
     */
    private static CoordinationService.Lock obtainCentralRepoLock(CentralRepository db) throws CentralRepoException {
        try {
            // This may return null if locking isn't supported, which is fine. It will
            // throw an exception if locking is supported but we can't get the lock
            // (meaning the database is in use by another user)
            return db.getExclusiveMultiUserDbLock();
            //perform upgrade
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, "Error updating central repository, unable to acquire exclusive lock", ex);
            onUpgradeError("Error updating central repository, unable to acquire exclusive lock",
                    Bundle.EamDbUtil_exclusiveLockAquisitionFailure_message() + Bundle.EamDbUtil_centralRepoDisabled_message(), ex);
        }

        // will never be reached
        return null;
    }

    /**
     * updates central repository schema if necessary
     *
     * @param db the database connectivity
     * @param lock the acquired lock
     * @throws CentralRepoException
     */
    private static void updatedDbSchema(CentralRepository db, CoordinationService.Lock lock) throws CentralRepoException {
        try {
            db.upgradeSchema();
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, "Error updating central repository", ex);
            onUpgradeError("Error updating central repository", ex.getUserMessage() + Bundle.EamDbUtil_centralRepoDisabled_message(), ex);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating central repository", ex);
            onUpgradeError("Error updating central repository",
                    Bundle.EamDbUtil_centralRepoUpgradeFailed_message() + Bundle.EamDbUtil_centralRepoDisabled_message(), ex);
        } catch (IncompatibleCentralRepoException ex) {
            logger.log(Level.SEVERE, "Error updating central repository", ex);
            onUpgradeError("Error updating central repository",
                    ex.getMessage() + "\n\n" + Bundle.EamDbUtil_centralRepoUpgradeFailed_message() + Bundle.EamDbUtil_centralRepoDisabled_message(), ex);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (CoordinationService.CoordinationServiceException ex) {
                    logger.log(Level.SEVERE, "Error releasing database lock", ex);
                }
            }
        }
    }

    /**
     * Upgrade the current Central Reposity schema to the newest version. If the
     * upgrade fails, the Central Repository will be disabled and the current
     * settings will be cleared.
     */
    @NbBundle.Messages(value = {"EamDbUtil.centralRepoDisabled.message= The Central Repository has been disabled.", "EamDbUtil.centralRepoUpgradeFailed.message=Failed to upgrade Central Repository.", "EamDbUtil.centralRepoConnectionFailed.message=Unable to connect to Central Repository.", "EamDbUtil.exclusiveLockAquisitionFailure.message=Unable to acquire exclusive lock for Central Repository."})
    public static void upgradeDatabase() throws CentralRepoException {
        if (!CentralRepository.isEnabled()) {
            return;
        }

        CentralRepository db = obtainCentralRepository();

        //get lock necessary for upgrade
        if (db != null) {
            CoordinationService.Lock lock = obtainCentralRepoLock(db);
            updatedDbSchema(db, lock);
        } else {
            onUpgradeError("Unable to connect to database",
                    Bundle.EamDbUtil_centralRepoConnectionFailed_message() + Bundle.EamDbUtil_centralRepoDisabled_message(), null);
        }
    }

    private static void onUpgradeError(String message, String desc, Exception innerException) throws CentralRepoException {
        // Disable the central repo and clear the current settings.
        try {
            if (null != CentralRepository.getInstance()) {
                CentralRepository.getInstance().shutdownConnections();
            }
        } catch (CentralRepoException ex2) {
            logger.log(Level.SEVERE, "Error shutting down central repo connection pool", ex2);
        }
        saveDbChoice(CentralRepoDbChoice.DISABLED);
        if (innerException == null) {
            throw new CentralRepoException(message, desc);
        } else {
            throw new CentralRepoException(message, desc, innerException);
        }
    }

    private DatabaseTestResult testingStatus;
    private CentralRepoDbChoice selectedDbChoice;

    private final PostgresCentralRepoSettings dbSettingsPostgres;
    private final SqliteCentralRepoSettings dbSettingsSqlite;

    private boolean configurationChanged = false;

    public CentralRepoDbManager() {
        dbSettingsPostgres = new PostgresCentralRepoSettings();
        dbSettingsSqlite = new SqliteCentralRepoSettings();
        selectedDbChoice = getSavedDbChoice();
    }

    public PostgresCentralRepoSettings getDbSettingsPostgres() {
        return dbSettingsPostgres;
    }

    public SqliteCentralRepoSettings getDbSettingsSqlite() {
        return dbSettingsSqlite;
    }

    /**
     * setup sqlite db with default settings
     * @throws CentralRepoException     if unable to successfully set up database
     */
    public void setupDefaultSqliteDb() throws CentralRepoException {
        // change in-memory settings to default sqlite
        selectedDbChoice = CentralRepoDbChoice.SQLITE;
        dbSettingsSqlite.setupDefaultSettings();

        // if db is not present, attempt to create it
        DatabaseTestResult curStatus = testStatus();
        if (curStatus == DatabaseTestResult.DB_DOES_NOT_EXIST) {
            createDb();
            curStatus = testStatus();
        }
        
        // the only successful setup status is tested ok
        if (curStatus != DatabaseTestResult.TESTEDOK) {
            throw new CentralRepoException("Unable to successfully create sqlite database");
        }
        
        // if successfully got here, then save the settings
        CentralRepoDbUtil.setUseCentralRepo(true);
        saveNewCentralRepo();
    }

    /**
     * Returns if changes to the central repository configuration were
     * successfully applied
     *
     * @return true if the database configuration was successfully changed false
     * if it was not
     */
    public boolean wasConfigurationChanged() {
        return configurationChanged;
    }

    private CentralRepoDbConnectivityManager getSelectedSettings() throws CentralRepoException {
        if (selectedDbChoice == CentralRepoDbChoice.POSTGRESQL_CUSTOM)
            return dbSettingsPostgres;
        if (selectedDbChoice == CentralRepoDbChoice.SQLITE)
            return dbSettingsSqlite;
        if (selectedDbChoice == CentralRepoDbChoice.DISABLED)
            return null;
        
        throw new CentralRepoException("Unknown database type: " + selectedDbChoice);
    }

    private RdbmsCentralRepoFactory getDbFactory() throws CentralRepoException {
        if (selectedDbChoice == CentralRepoDbChoice.POSTGRESQL_CUSTOM)
            return new RdbmsCentralRepoFactory(CentralRepoPlatforms.POSTGRESQL, dbSettingsPostgres);
        if (selectedDbChoice == CentralRepoDbChoice.SQLITE)
            return new RdbmsCentralRepoFactory(CentralRepoPlatforms.SQLITE, dbSettingsSqlite);
        if (selectedDbChoice == CentralRepoDbChoice.DISABLED)
            return null;
        
        throw new CentralRepoException("Unknown database type: " + selectedDbChoice);
    }

    public boolean createDb() throws CentralRepoException {
        boolean result = false;
        boolean dbCreated = true;

        CentralRepoDbConnectivityManager selectedDbSettings = getSelectedSettings();

        if (!selectedDbSettings.verifyDatabaseExists()) {
            dbCreated = selectedDbSettings.createDatabase();
        }
        if (dbCreated) {
            try {
                RdbmsCentralRepoFactory centralRepoSchemaFactory = getDbFactory();

                result = centralRepoSchemaFactory.initializeDatabaseSchema()
                        && centralRepoSchemaFactory.insertDefaultDatabaseContent();
            } catch (CentralRepoException ex) {
                logger.log(Level.SEVERE, "Unable to create database for central repository with settings " + selectedDbSettings, ex);
                throw ex;
            }
        }
        if (!result) {
            // Remove the incomplete database
            if (dbCreated) {
                // RAMAN TBD: migrate  deleteDatabase() to RdbmsCentralRepoFactory
                selectedDbSettings.deleteDatabase();
            }

            String schemaError = "Unable to initialize database schema or insert contents into central repository.";
            logger.severe(schemaError);
            throw new CentralRepoException(schemaError);
        }

        testingStatus = DatabaseTestResult.TESTEDOK;
        return true;
    }

    /**
     * saves a new central repository based on current settings
     */
    @NbBundle.Messages({"CentralRepoDbManager.connectionErrorMsg.text=Failed to connect to central repository database."})
    public void saveNewCentralRepo() throws CentralRepoException {
        /**
         * We have to shutdown the previous platform's connection pool first;
         * assuming it wasn't DISABLED. This will close any existing idle
         * connections.
         *
         * The next use of an EamDb API method will start a new connection pool
         * using those new settings.
         */
        try {
            CentralRepository previousDbManager = CentralRepository.getInstance();
            if (null != previousDbManager) {
                // NOTE: do not set/save the seleted platform before calling this.
                CentralRepository.getInstance().shutdownConnections();
            }
        } catch (CentralRepoException ex) {
            logger.log(Level.SEVERE, "Failed to close database connections in previously selected platform.", ex); // NON-NLS
            throw ex;
        }

        // Even if we fail to close the existing connections, make sure that we
        // save the new connection settings, so an Autopsy restart will correctly
        // start with the new settings.
        saveDbChoice(selectedDbChoice);

        CentralRepoDbConnectivityManager selectedDbSettings = getSelectedSettings();

        // save the new settings
        selectedDbSettings.saveSettings();
        // Load those newly saved settings into the postgres db manager instance
        //  in case we are still using the same instance.
        if (selectedDbChoice != null && selectedDbSettings != CentralRepoDbChoice.DISABLED) {
            try {
                logger.info("Creating central repo db with settings: " + selectedDbSettings);
                CentralRepository.getInstance().updateSettings();
                configurationChanged = true;
            } catch (CentralRepoException ex) {
                logger.log(Level.SEVERE, Bundle.CentralRepoDbManager_connectionErrorMsg_text(), ex); //NON-NLS
                return;
            }
        }
    }

    public DatabaseTestResult getStatus() {
        return testingStatus;
    }

    public CentralRepoDbChoice getSelectedDbChoice() {
        return selectedDbChoice;
    }

    public void clearStatus() {
        testingStatus = DatabaseTestResult.UNTESTED;
    }

    public void setSelctedDbChoice(CentralRepoDbChoice newSelected) {
        selectedDbChoice = newSelected;
        testingStatus = DatabaseTestResult.UNTESTED;
    }

    /**
     * Tests whether or not the database settings are valid.
     *
     * @return True or false.
     */
    public boolean testDatabaseSettingsAreValid(
            String tbDbHostname, String tbDbPort, String tbDbUsername, String tfDatabasePath, String jpDbPassword) throws CentralRepoException, NumberFormatException {

        if (selectedDbChoice == CentralRepoDbChoice.POSTGRESQL_CUSTOM) {
            dbSettingsPostgres.setHost(tbDbHostname);
            dbSettingsPostgres.setPort(Integer.parseInt(tbDbPort));
            dbSettingsPostgres.setDbName(CENTRAL_REPO_DB_NAME);
            dbSettingsPostgres.setUserName(tbDbUsername);
            dbSettingsPostgres.setPassword(jpDbPassword);
        }
        else if (selectedDbChoice == CentralRepoDbChoice.SQLITE) {
            File databasePath = new File(tfDatabasePath);
            dbSettingsSqlite.setDbName(SqliteCentralRepoSettings.DEFAULT_DBNAME);
            dbSettingsSqlite.setDbDirectory(databasePath.getPath());
        }
        else if (selectedDbChoice != CentralRepoDbChoice.POSTGRESQL_MULTIUSER) {
            throw new IllegalStateException("Central Repo has an unknown selected platform: " + selectedDbChoice);
        }

        return true;
    }

    public DatabaseTestResult testStatus() {
        if (selectedDbChoice == CentralRepoDbChoice.POSTGRESQL_CUSTOM) {
            if (dbSettingsPostgres.verifyConnection()) {
                if (dbSettingsPostgres.verifyDatabaseExists()) {
                    if (dbSettingsPostgres.verifyDatabaseSchema()) {
                        testingStatus = DatabaseTestResult.TESTEDOK;
                    } else {
                        testingStatus = DatabaseTestResult.SCHEMA_INVALID;
                    }
                } else {
                    testingStatus = DatabaseTestResult.DB_DOES_NOT_EXIST;
                }
            } else {
                testingStatus = DatabaseTestResult.CONNECTION_FAILED;
            }
        } else if (selectedDbChoice == CentralRepoDbChoice.SQLITE) {
            if (dbSettingsSqlite.dbFileExists()) {
                if (dbSettingsSqlite.verifyConnection()) {
                    if (dbSettingsSqlite.verifyDatabaseSchema()) {
                        testingStatus = DatabaseTestResult.TESTEDOK;
                    } else {
                        testingStatus = DatabaseTestResult.SCHEMA_INVALID;
                    }
                } else {
                    testingStatus = DatabaseTestResult.SCHEMA_INVALID;
                }
            } else {
                testingStatus = DatabaseTestResult.DB_DOES_NOT_EXIST;
            }
        }

        return testingStatus;
    }
}
