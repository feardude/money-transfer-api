[![Build Status](https://travis-ci.com/feardude/money-transfer-api.svg?branch=master)](https://travis-ci.com/feardude/money-transfer-api)
[![QualityGate Status](https://sonarcloud.io/api/project_badges/measure?project=feardude_money-transfer-api&metric=alert_status)](https://sonarcloud.io/dashboard?id=feardude_money-transfer-api)

### Money transfer API 
#### Trial for Revolut
The application represents API for processing accounts' money. 
No authentication, so anyone can see any account's balance. 
Real application should grant balance access only to account's owner.

---
### Architecture
- **SparkJava** web framework.
- **Google Guice** DI framework.
- **Sql2o** for data access layer.
- **HSQLDB** in-memory database.

---
### Infrastructure
- **Continuous integration**: Travis-CI
- **Continuous delivery**: Heroku (github integration). App is deployed from master branch after successful CI build.
- **Code quality**: SonarCloud (used inside CI pipeline)

Consider dockerizing of CI/CD pipeline.

---
### API
##### GET /
Redirects to /api
<br><br>

##### GET /api
Returns all implemented API methods
<br><br>

##### GET /api/accounts
Returns info for all accounts (id, real id, amount).
<br><br>

##### GET /api/accounts/:id
Returns info for account with specific id.
<br><br>

##### POST /api/accounts/:id
Body payload:
{
    "action": ${action},
    "accountId": ${id},
    "amount": ${amount}
}
- _action_: String (WITHDRAW, DEPOSIT)
- _id_: long, id of existing account
- _amount_: amount of money with dot as scale delimiter

Returns account info after processing. Returns _InsufficientFundsException_ if account has not enough money.
<br><br>

##### POST /api/transfer
Body payload:
{
    "fromAccountId": ${fromAccountId},
    "toAccountId": ${toAccountId},
    "amount": ${amount}
}<br>
- _fromAccountId_: long, id of existing account to withdraw money from
- _toAccountId_: long, id of existing account to deposit money to
- _amount_: amount of money with dot as scale delimiter

Returns empty string in case of success. Returns _InsufficientFundsException_ if from-account has not enough money.

---
### Known issues
- Poor in-memory DB initializing. Scripts (creating schema and tables, populating tables) should be moved to specific config for more flexibility and testability. Consider removing duplication in integration test and main config.
- No tests for exception handling in Spark controller. Consider using another spark-test library or writing own test infrastructure.
- SQL scripts are defined inside Dao object. Consider moving scripts to external config (resource file or Config class).
- No proofs of proper concurrent logic. Need to add concurrent tests.
- No stress tests/benchmarks.
