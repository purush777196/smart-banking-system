/**
 * Currency, date, and number formatters for Finova
 */

export const formatCurrency = (amount, currency = 'USD', compact = false) => {
  if (amount === undefined || amount === null || isNaN(amount)) return '$0.00';
  
  if (compact && Math.abs(amount) >= 1_000_000_000) {
    return `$${(amount / 1_000_000_000).toFixed(1)}B`;
  }
  if (compact && Math.abs(amount) >= 1_000_000) {
    return `$${(amount / 1_000_000).toFixed(1)}M`;
  }
  if (compact && Math.abs(amount) >= 1_000) {
    return `$${(amount / 1_000).toFixed(1)}K`;
  }

  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

export const formatNumber = (num, compact = false) => {
  if (num === undefined || num === null || isNaN(num)) return '0';
  
  if (compact && Math.abs(num) >= 1_000_000) {
    return `${(num / 1_000_000).toFixed(1)}M`;
  }
  if (compact && Math.abs(num) >= 1_000) {
    return `${(num / 1_000).toFixed(1)}K`;
  }

  return new Intl.NumberFormat('en-US').format(num);
};

export const formatDate = (dateString, options = {}) => {
  if (!dateString) return '—';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  
  const defaultOptions = {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    ...options
  };

  return new Intl.DateTimeFormat('en-US', defaultOptions).format(date);
};

export const formatDateTime = (dateString) => {
  if (!dateString) return '—';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(date);
};

export const maskAccountNumber = (accNumber) => {
  if (!accNumber) return '••••';
  const str = String(accNumber);
  if (str.length <= 4) return str;
  return `•••• ${str.slice(-4)}`;
};

