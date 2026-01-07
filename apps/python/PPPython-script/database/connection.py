import pyodbc
from config.db_config import DB_CONFIG

def get_connection():
    try:
        connection_string = (
            f"DRIVER={{{DB_CONFIG['driver']}}};"
            f"SERVER={DB_CONFIG['server']};"
            f"DATABASE={DB_CONFIG['database']};"
            f"UID={DB_CONFIG['username']};"
            f"PWD={DB_CONFIG['password']};"
            f"TrustServerCertificate={DB_CONFIG['trustServerCertificate']};"
            f"Encrypt={DB_CONFIG['encrypt']};"
            f"Connection Timeout={DB_CONFIG['Connection Timeout']};"
        )
        conn = pyodbc.connect(connection_string)
        return conn
    except Exception as e:
        raise Exception(f"Database connection failed with driver '{DB_CONFIG['driver']}': {str(e)}")