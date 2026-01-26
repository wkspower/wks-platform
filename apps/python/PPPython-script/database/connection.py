import pyodbc
import time
from config.db_config import DB_CONFIG


def get_connection(max_retries=3, retry_delay=2):
    """
    Get database connection with retry logic.
    
    Args:
        max_retries: Maximum number of connection attempts (default: 3)
        retry_delay: Delay in seconds between retries (default: 2)
    
    Returns:
        pyodbc.Connection object
    
    Raises:
        Exception if all connection attempts fail
    """
    connection_string = (
        f"DRIVER={{{DB_CONFIG['driver']}}};"
        f"SERVER={DB_CONFIG['server']};"
        f"DATABASE={DB_CONFIG['database']};"
        f"UID={DB_CONFIG['username']};"
        f"PWD={DB_CONFIG['password']};"
        f"TrustServerCertificate={DB_CONFIG['trustServerCertificate']};"
        f"Encrypt={DB_CONFIG['encrypt']};"
        f"Connection Timeout={DB_CONFIG['Connection Timeout']};"
        f"MARS_Connection=yes;"  # Enable Multiple Active Result Sets
    )
    
    last_error = None
    for attempt in range(1, max_retries + 1):
        try:
            conn = pyodbc.connect(connection_string)
            # Set connection attributes for better stability
            conn.timeout = 30
            conn.autocommit = False
            return conn
        except pyodbc.Error as e:
            last_error = e
            error_code = e.args[0] if e.args else 'Unknown'
            
            # Check if it's a transient error that can be retried
            transient_errors = ['08S01', '08001', '40001', '40197', '40501', '40613', '49918', '49919', '49920']
            is_transient = error_code in transient_errors
            
            if attempt < max_retries and is_transient:
                print(f"[DB Connection] Attempt {attempt}/{max_retries} failed with error {error_code}. Retrying in {retry_delay}s...")
                time.sleep(retry_delay)
            else:
                break
        except Exception as e:
            last_error = e
            if attempt < max_retries:
                print(f"[DB Connection] Attempt {attempt}/{max_retries} failed. Retrying in {retry_delay}s...")
                time.sleep(retry_delay)
            else:
                break
    
    # All retries failed
    raise Exception(f"Database connection failed after {max_retries} attempts with driver '{DB_CONFIG['driver']}': {str(last_error)}")