import os

DB_CONFIG = {
    "server": os.environ.get("DB_SERVER", "216.48.180.83"),
    "database": os.environ.get("DB_DATABASE", "RIL.AOP"),
    "username": os.environ.get("DB_USERNAME", "sa"),
    "password": os.environ.get("DB_PASSWORD", "#Qwer123"),
    "driver": os.environ.get("DB_DRIVER", "ODBC Driver 17 for SQL Server"),
    "trustServerCertificate": os.environ.get("DB_TRUST_CERT", "yes")
}