import api from './api.js';

/**
 * Generates a unique, cryptographically random idempotency key.
 * Used to safeguard financial operations (deposits, withdrawals) against accidental retries and double submits.
 */
export const generateIdempotencyKey = (prefix = 'FIN-IDEMP') => {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return `${prefix}-${crypto.randomUUID()}`;
  }
  const entropy = Math.random().toString(36).substring(2, 10);
  const timestamp = Date.now().toString(36);
  return `${prefix}-${timestamp}-${entropy}`;
};

/**
 * Masks a bank account number for secure customer-facing display.
 * e.g., "100000000001" -> "•••• 0001"
 */
export const maskAccountNumber = (accountNumber) => {
  if (!accountNumber) return '•••• ----';
  const str = String(accountNumber).trim();
  if (str.length <= 4) return `•••• ${str}`;
  return `•••• ${str.slice(-4)}`;
};

/**
 * Fetches all bank accounts owned by the currently authenticated customer.
 * Endpoint: GET /api/accounts/me
 */
export const getMyAccounts = async () => {
  const response = await api.get('/accounts/me');
  return response.data;
};

/**
 * Submits a deposit into the specified bank account.
 * Endpoint: POST /api/accounts/{accountId}/deposits
 *
 * @param {number|string} accountId - Bank account primary key
 * @param {Object} data - { amount, description, idempotencyKey }
 * @returns {Promise<Object>} Safe TransactionResponse DTO
 */
export const depositFunds = async (accountId, { amount, description, idempotencyKey }) => {
  const payload = {
    amount: typeof amount === 'number' ? amount.toFixed(2) : amount,
    idempotencyKey: idempotencyKey || generateIdempotencyKey('FIN-DEP'),
  };
  if (description && description.trim()) {
    payload.description = description.trim();
  }
  const response = await api.post(`/accounts/${accountId}/deposits`, payload);
  return response.data;
};

/**
 * Submits a withdrawal from the specified bank account.
 * Endpoint: POST /api/accounts/{accountId}/withdrawals
 *
 * @param {number|string} accountId - Bank account primary key
 * @param {Object} data - { amount, description, idempotencyKey }
 * @returns {Promise<Object>} Safe TransactionResponse DTO
 */
export const withdrawFunds = async (accountId, { amount, description, idempotencyKey }) => {
  const payload = {
    amount: typeof amount === 'number' ? amount.toFixed(2) : amount,
    idempotencyKey: idempotencyKey || generateIdempotencyKey('FIN-WTH'),
  };
  if (description && description.trim()) {
    payload.description = description.trim();
  }
  const response = await api.post(`/accounts/${accountId}/withdrawals`, payload);
  return response.data;
};

/**
 * Retrieves paginated, filterable transaction history for a bank account.
 * Endpoint: GET /api/accounts/{accountId}/transactions
 *
 * @param {number|string} accountId - Bank account primary key
 * @param {Object} [params] - { page, size, sortBy, direction, type, status, startDate, endDate }
 * @returns {Promise<Object>} TransactionHistoryResponse DTO
 */
export const getTransactionHistory = async (accountId, params = {}) => {
  const cleanParams = {};
  if (params.page !== undefined && params.page !== null) cleanParams.page = params.page;
  if (params.size !== undefined && params.size !== null) cleanParams.size = params.size;
  if (params.sortBy) cleanParams.sortBy = params.sortBy;
  if (params.direction) cleanParams.direction = params.direction;
  if (params.type && params.type !== 'ALL') cleanParams.type = params.type;
  if (params.status && params.status !== 'ALL') cleanParams.status = params.status;
  if (params.startDate) cleanParams.startDate = params.startDate;
  if (params.endDate) cleanParams.endDate = params.endDate;

  const response = await api.get(`/accounts/${accountId}/transactions`, { params: cleanParams });
  return response.data;
};

/**
 * Looks up a single transaction by its unique reference string.
 * Endpoint: GET /api/transactions/{transactionReference}
 *
 * @param {string} transactionReference - e.g. "TXN-20261106-ABCDEF"
 * @returns {Promise<Object>} Safe TransactionResponse DTO
 */
export const getTransactionByReference = async (transactionReference) => {
  if (!transactionReference || !transactionReference.trim()) {
    throw new Error('Transaction reference is required');
  }
  const cleanRef = encodeURIComponent(transactionReference.trim());
  const response = await api.get(`/transactions/${cleanRef}`);
  return response.data;
};

export default {
  generateIdempotencyKey,
  maskAccountNumber,
  getMyAccounts,
  depositFunds,
  withdrawFunds,
  getTransactionHistory,
  getTransactionByReference,
};
