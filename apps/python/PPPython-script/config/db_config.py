import os
import pyodbc

# Try to detect available ODBC drivers
def get_available_driver():
    """Detect available SQL Server ODBC drivers."""
    try:
        drivers = [d for d in pyodbc.drivers() if 'SQL Server' in d]
        
        # Prefer newer drivers
        for driver_name in ['ODBC Driver 18 for SQL Server', 'ODBC Driver 17 for SQL Server', 'ODBC Driver 13 for SQL Server']:
            if driver_name in drivers:
                print(f"[DB Config] Using ODBC driver: {driver_name}")
                return driver_name
        
        # If no driver found, log available drivers and use default
        if drivers:
            print(f"[DB Config] Available drivers: {drivers}")
        print(f"[DB Config] No SQL Server ODBC driver found, attempting with default")
    except Exception as e:
        print(f"[DB Config] Error detecting drivers: {e}")
    
    # Fallback to environment variable or default
    return os.environ.get("DB_DRIVER", "ODBC Driver 18 for SQL Server")

DB_CONFIG = {
    "server": os.environ.get("DB_SERVER", "216.48.180.83"),
    "database": os.environ.get("DB_DATABASE", "RIL.AOP"),
    "username": os.environ.get("DB_USERNAME", "sa"),
    "password": os.environ.get("DB_PASSWORD", "#Qwer123"),
    "driver": get_available_driver(),
    "trustServerCertificate": os.environ.get("DB_TRUST_CERT", "yes"),
    "encrypt": "yes",
    "Connection Timeout": 30
}