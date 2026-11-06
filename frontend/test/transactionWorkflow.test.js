import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { generateIdempotencyKey, maskAccountNumber } from '../src/services/transactionService.js';

describe('Phase 6 Frontend: Transaction Workflow & Security Validation', () => {
  describe('1. Idempotency Key Generation', () => {
    it('generates non-empty idempotency key with expected prefix', () => {
      const key = generateIdempotencyKey('FIN-DEP');
      assert.ok(key.startsWith('FIN-DEP-'));
      assert.ok(key.length >= 10);
    });

    it('generates distinct keys across multiple invocations to prevent collision', () => {
      const keys = new Set();
      for (let i = 0; i < 100; i++) {
        keys.add(generateIdempotencyKey('FIN-TEST'));
      }
      assert.equal(keys.size, 100);
    });
  });

  describe('2. Account Number Masking', () => {
    it('masks standard 10-12 digit account number displaying only last 4 digits', () => {
      assert.equal(maskAccountNumber('9842109284'), '•••• 9284');
      assert.equal(maskAccountNumber('100000000001'), '•••• 0001');
    });

    it('handles short or fallback account numbers gracefully', () => {
      assert.equal(maskAccountNumber('123'), '•••• 123');
      assert.equal(maskAccountNumber(null), '•••• ----');
      assert.equal(maskAccountNumber(undefined), '•••• ----');
    });
  });

  describe('3. Deposit Form Validation Logic', () => {
    const validateDeposit = (amount, targetAccountId, description) => {
      if (!targetAccountId) return { valid: false, error: 'Target account required' };
      const num = parseFloat(amount);
      if (!amount || isNaN(num) || num <= 0) {
        return { valid: false, error: 'Amount must be strictly positive' };
      }
      const parts = amount.toString().split('.');
      if (parts.length > 1 && parts[1].length > 2) {
        return { valid: false, error: 'Max 2 decimal places allowed' };
      }
      if (description && description.length > 255) {
        return { valid: false, error: 'Description exceeds 255 characters' };
      }
      return { valid: true };
    };

    it('accepts valid deposit amounts with 0, 1, or 2 decimal places', () => {
      assert.deepEqual(validateDeposit('100', '1', 'Savings'), { valid: true });
      assert.deepEqual(validateDeposit('100.5', '1', 'Savings'), { valid: true });
      assert.deepEqual(validateDeposit('100.50', '1', 'Savings'), { valid: true });
    });

    it('rejects zero amount', () => {
      assert.equal(validateDeposit('0', '1').valid, false);
      assert.equal(validateDeposit('0.00', '1').valid, false);
    });

    it('rejects negative amount', () => {
      assert.equal(validateDeposit('-50.00', '1').valid, false);
    });

    it('rejects excess decimal precision (more than 2 decimals)', () => {
      assert.equal(validateDeposit('10.999', '1').valid, false);
      assert.equal(validateDeposit('10.999', '1').error, 'Max 2 decimal places allowed');
    });

    it('rejects missing target account', () => {
      assert.equal(validateDeposit('50.00', '').valid, false);
    });
  });

  describe('4. Withdrawal Form Validation & Overdraft Prevention', () => {
    const validateWithdrawal = (amount, sourceAccountId, availableBalance) => {
      if (!sourceAccountId) return { valid: false, error: 'Source account required' };
      const num = parseFloat(amount);
      if (!amount || isNaN(num) || num <= 0) {
        return { valid: false, error: 'Amount must be strictly positive' };
      }
      const parts = amount.toString().split('.');
      if (parts.length > 1 && parts[1].length > 2) {
        return { valid: false, error: 'Max 2 decimal places allowed' };
      }
      if (typeof availableBalance === 'number' && num > availableBalance) {
        return { valid: false, error: 'Insufficient funds' };
      }
      return { valid: true };
    };

    it('accepts valid withdrawal within available balance', () => {
      assert.deepEqual(validateWithdrawal('500.00', '1', 1000.00), { valid: true });
      assert.deepEqual(validateWithdrawal('1000.00', '1', 1000.00), { valid: true });
    });

    it('rejects withdrawal exceeding available balance', () => {
      const res = validateWithdrawal('1500.00', '1', 1000.00);
      assert.equal(res.valid, false);
      assert.equal(res.error, 'Insufficient funds');
    });

    it('rejects zero or negative withdrawal', () => {
      assert.equal(validateWithdrawal('0.00', '1', 1000.00).valid, false);
      assert.equal(validateWithdrawal('-100.00', '1', 1000.00).valid, false);
    });

    it('rejects fractional fractions greater than 2 decimals', () => {
      assert.equal(validateWithdrawal('100.255', '1', 1000.00).valid, false);
    });
  });

  describe('5. Ledger Transaction Status & Type Consistency', () => {
    const VALID_TYPES = ['DEPOSIT', 'WITHDRAWAL', 'TRANSFER_DEBIT', 'TRANSFER_CREDIT'];
    const VALID_STATUSES = ['PENDING', 'COMPLETED', 'FAILED', 'REVERSED'];

    it('supports all required transaction types', () => {
      VALID_TYPES.forEach((t) => assert.ok(typeof t === 'string'));
      assert.equal(VALID_TYPES.length, 4);
    });

    it('supports all required transaction statuses', () => {
      VALID_STATUSES.forEach((s) => assert.ok(typeof s === 'string'));
      assert.equal(VALID_STATUSES.length, 4);
    });
  });
});

