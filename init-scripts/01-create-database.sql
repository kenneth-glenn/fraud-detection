CREATE DATABASE fraud_detection;
CREATE USER fraud_api_user WITH ENCRYPTED PASSWORD 'fraud_api_password!';
GRANT ALL PRIVILEGES ON DATABASE fraud_detection TO fraud_api_user;
