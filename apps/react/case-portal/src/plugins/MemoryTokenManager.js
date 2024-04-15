var _TokenInMemory_ = null;

class MemoryTokenManager {
  static getToken() {
    return _TokenInMemory_;
  }

  static setToken(token) {
    _TokenInMemory_ = token;
    return true;
  }
}

export default MemoryTokenManager;
