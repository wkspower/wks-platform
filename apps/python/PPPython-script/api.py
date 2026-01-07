"""
Flask REST API for Power Plant Budgeting System
This API exposes the Python budget calculation as HTTP endpoints
that can be called from Java or any other service.
"""

import os
import glob
from flask import Flask, request, jsonify, send_file, send_from_directory
from flask_cors import CORS

from services.budget_service import (
    calculate_budget, 
    calculate_budget_with_iteration,
)
from run_full_year import run_full_financial_year, get_available_cpp_plants, get_default_cpp_plant

app = Flask(__name__)
CORS(app)  # Enable CORS for cross-origin requests from Java

# Log folder configuration
LOG_FOLDER = os.environ.get("LOG_FOLDER", "/app/logs")


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    return jsonify({
        "status": "healthy",
        "service": "pp-budget-calculator",
        "log_folder": LOG_FOLDER
    })


@app.route('/api/budget/calculate', methods=['POST'])
def calculate_budget_api():
    """
    Calculate budget without USD iteration.
    
    Request Body (JSON):
    {
        "month": 1,
        "year": 2024,
        "lp_process": 30043.15,
        "lp_fixed": 5169.51,
        "mp_process": 14030.65,
        "mp_fixed": 518.00,
        "hp_process": 4971.91,
        "hp_fixed": 0.00,
        "shp_process": 20975.34,
        "shp_fixed": 0.00,
        "bfw_ufu": 0.00,
        "air_process": 6095102.0,
        "cw1_process": 15194.0,
        "cw2_process": 9016.0,
        "dm_process": 54779.0
    }
    """
    try:
        data = request.get_json()
        
        # Validate required fields
        required_fields = ['month', 'year']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    "success": False,
                    "error": f"Missing required field: {field}"
                }), 400
        
        # Extract parameters with defaults
        result = calculate_budget(
            month=int(data['month']),
            year=int(data['year']),
            lp_process=float(data.get('lp_process', 30043.15)),
            lp_fixed=float(data.get('lp_fixed', 5169.51)),
            mp_process=float(data.get('mp_process', 14030.65)),
            mp_fixed=float(data.get('mp_fixed', 518.00)),
            hp_process=float(data.get('hp_process', 4971.91)),
            hp_fixed=float(data.get('hp_fixed', 0.00)),
            shp_process=float(data.get('shp_process', 20975.34)),
            shp_fixed=float(data.get('shp_fixed', 0.00)),
            bfw_ufu=float(data.get('bfw_ufu', 0.00)),
            air_process=float(data.get('air_process', 6095102.0)),
            cw1_process=float(data.get('cw1_process', 15194.0)),
            cw2_process=float(data.get('cw2_process', 9016.0)),
            dm_process=float(data.get('dm_process', 54779.0)),
        )
        
        return jsonify({
            "success": True,
            "data": result
        })
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/budget/calculate-with-iteration', methods=['POST'])
def calculate_budget_with_iteration_api():
    """
    Calculate budget with USD iteration (Power-Steam balancing).
    
    Request Body (JSON):
    {
        "month": 1,
        "year": 2024,
        "lp_process": 30043.15,
        "lp_fixed": 5169.51,
        "mp_process": 14030.65,
        "mp_fixed": 518.00,
        "hp_process": 4971.91,
        "hp_fixed": 0.00,
        "shp_process": 20975.34,
        "shp_fixed": 0.00,
        "bfw_ufu": 0.00,
        "export_available": false,
        "air_process": 6095102.0,
        "cw1_process": 15194.0,
        "cw2_process": 9016.0,
        "dm_process": 54779.0,
        "save_to_db": false
    }
    """
    try:
        data = request.get_json()
        
        # Validate required fields
        required_fields = ['month', 'year']
        for field in required_fields:
            if field not in data:
                return jsonify({
                    "success": False,
                    "error": f"Missing required field: {field}"
                }), 400
        
        # Extract parameters with defaults
        result = calculate_budget_with_iteration(
            month=int(data['month']),
            year=int(data['year']),
            lp_process=float(data.get('lp_process', 30043.15)),
            lp_fixed=float(data.get('lp_fixed', 5169.51)),
            mp_process=float(data.get('mp_process', 14030.65)),
            mp_fixed=float(data.get('mp_fixed', 518.00)),
            hp_process=float(data.get('hp_process', 4971.91)),
            hp_fixed=float(data.get('hp_fixed', 0.00)),
            shp_process=float(data.get('shp_process', 20975.34)),
            shp_fixed=float(data.get('shp_fixed', 0.00)),
            bfw_ufu=float(data.get('bfw_ufu', 0.00)),
            export_available=bool(data.get('export_available', False)),
            air_process=float(data.get('air_process', 6095102.0)),
            cw1_process=float(data.get('cw1_process', 15194.0)),
            cw2_process=float(data.get('cw2_process', 9016.0)),
            dm_process=float(data.get('dm_process', 54779.0)),
            save_to_db=bool(data.get('save_to_db', False)),
        )
        
        return jsonify({
            "success": True,
            "data": result
        })
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/budget/run-full-year', methods=['POST'])
def run_full_year_api():
    """
    Run budget calculation for a full financial year (April to March).
    
    Request Body (JSON):
    {
        "financial_year": 2025,      # Required: FY 2025-26 (April 2025 to March 2026)
        "cpp_plant_id": "...",       # Optional: CPP Plant GUID, uses default if not provided
        "save_to_db": true,          # Optional: Save results to database (default: true)
        "save_logs": true            # Optional: Save log files (default: true)
    }
    """
    try:
        data = request.get_json()
        
        # Validate required fields
        if 'financial_year' not in data:
            return jsonify({
                "success": False,
                "error": "Missing required field: financial_year"
            }), 400
        
        financial_year = int(data['financial_year'])
        cpp_plant_id = data.get('cpp_plant_id')
        save_to_db = bool(data.get('save_to_db', True))
        save_logs = bool(data.get('save_logs', True))
        
        # Run full year calculation
        results = run_full_financial_year(
            financial_year=financial_year,
            cpp_plant_id=cpp_plant_id,
            process_demands=None,  # Uses defaults
            save_to_db=save_to_db,
            save_logs=save_logs
        )
        
        # Prepare response
        response = {
            "success": True,
            "financial_year": f"{financial_year}-{str(financial_year + 1)[-2:]}",
            "cpp_plant_id": cpp_plant_id or get_default_cpp_plant(),
            "run_timestamp": results.get("run_timestamp"),
            "summary": results.get("summary", {}),
            "months": {}
        }
        
        # Add month details
        for key, month_data in results.get("months", {}).items():
            response["months"][key] = {
                "month": month_data.get("month"),
                "year": month_data.get("year"),
                "month_name": month_data.get("month_name"),
                "success": month_data.get("success", False),
                "converged": month_data.get("converged", False),
                "iterations": month_data.get("iterations", 0),
                "total_power_kwh": month_data.get("total_power_kwh", 0),
                "total_shp_mt": month_data.get("total_shp_mt", 0),
                "records_saved": month_data.get("records_saved", 0),
                "log_file": month_data.get("log_file", "")
            }
        
        return jsonify(response)
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/cpp-plants', methods=['GET'])
def get_cpp_plants_api():
    """Get list of available CPP plants."""
    try:
        plants = get_available_cpp_plants()
        default_plant = get_default_cpp_plant()
        return jsonify({
            "success": True,
            "plants": plants,
            "default_plant": default_plant
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/logs', methods=['GET'])
def list_logs_api():
    """List all available log folders/runs."""
    try:
        if not os.path.exists(LOG_FOLDER):
            return jsonify({
                "success": True,
                "runs": [],
                "log_folder": LOG_FOLDER
            })
        
        # List all run folders
        runs = []
        for item in os.listdir(LOG_FOLDER):
            item_path = os.path.join(LOG_FOLDER, item)
            if os.path.isdir(item_path) and item.startswith("run_"):
                # Get summary if exists
                summary_path = os.path.join(item_path, "summary.txt")
                has_summary = os.path.exists(summary_path)
                
                # Count log files
                log_files = glob.glob(os.path.join(item_path, "month_*.txt"))
                
                runs.append({
                    "run_id": item,
                    "path": item_path,
                    "has_summary": has_summary,
                    "log_count": len(log_files)
                })
        
        # Sort by run_id (timestamp) descending
        runs.sort(key=lambda x: x["run_id"], reverse=True)
        
        return jsonify({
            "success": True,
            "runs": runs,
            "log_folder": LOG_FOLDER
        })
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/logs/<run_id>', methods=['GET'])
def get_run_logs_api(run_id):
    """Get list of log files for a specific run."""
    try:
        run_path = os.path.join(LOG_FOLDER, run_id)
        
        if not os.path.exists(run_path):
            return jsonify({
                "success": False,
                "error": f"Run not found: {run_id}"
            }), 404
        
        # List all files in the run folder
        files = []
        for item in os.listdir(run_path):
            item_path = os.path.join(run_path, item)
            if os.path.isfile(item_path):
                files.append({
                    "filename": item,
                    "size": os.path.getsize(item_path)
                })
        
        return jsonify({
            "success": True,
            "run_id": run_id,
            "files": files
        })
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/logs/<run_id>/<filename>', methods=['GET'])
def download_log_file_api(run_id, filename):
    """Download a specific log file."""
    try:
        run_path = os.path.join(LOG_FOLDER, run_id)
        file_path = os.path.join(run_path, filename)
        
        if not os.path.exists(file_path):
            return jsonify({
                "success": False,
                "error": f"File not found: {filename}"
            }), 404
        
        return send_from_directory(run_path, filename, as_attachment=True)
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/logs/<run_id>/<filename>/content', methods=['GET'])
def get_log_content_api(run_id, filename):
    """Get content of a specific log file."""
    try:
        run_path = os.path.join(LOG_FOLDER, run_id)
        file_path = os.path.join(run_path, filename)
        
        if not os.path.exists(file_path):
            return jsonify({
                "success": False,
                "error": f"File not found: {filename}"
            }), 404
        
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        return jsonify({
            "success": True,
            "run_id": run_id,
            "filename": filename,
            "content": content
        })
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


if __name__ == '__main__':
    # Create log folder if it doesn't exist
    os.makedirs(LOG_FOLDER, exist_ok=True)
    
    # Get port from environment variable or default to 5000
    port = int(os.environ.get('PORT', 5000))
    host = os.environ.get('HOST', '0.0.0.0')
    debug = os.environ.get('DEBUG', 'false').lower() == 'true'
    
    print(f"Starting PP Budget Calculator API on {host}:{port}")
    print(f"Log folder: {LOG_FOLDER}")
    app.run(host=host, port=port, debug=debug)
