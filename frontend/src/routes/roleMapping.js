/**
 * Centralized Role-to-Route Mapping
 * Maps backend Spring Security roles to permitted frontend dashboard routes.
 */

export const ROLE_TO_ROUTE_MAP = {
  SUPER_ADMIN: '/admin/dashboard',
  BRANCH_MANAGER: '/manager/dashboard',
  CUSTOMER: '/customer/dashboard',
  EMPLOYEE: '/employee/dashboard',
  LOAN_OFFICER: '/loans/dashboard',
  FRAUD_ANALYST: '/fraud/dashboard',
  FINANCE_MANAGER: '/finance/dashboard',
  COMPLIANCE_OFFICER: '/audit/dashboard',
  SUPPORT_AGENT: '/support/dashboard',
  ANALYTICS_HEAD: '/analytics/dashboard',
};

export const ROUTE_TO_ROLE_MAP = Object.entries(ROLE_TO_ROUTE_MAP).reduce(
  (acc, [role, route]) => ({ ...acc, [route]: role }),
  {}
);

/**
 * Returns the permitted dashboard route for a given backend role.
 */
export const getRouteForRole = (role) => {
  if (!role) return '/login';
  const normalizedRole = String(role).toUpperCase();
  return ROLE_TO_ROUTE_MAP[normalizedRole] || '/login';
};

/**
 * Validates whether a given dashboard pathname is permitted for the user's role.
 * In Phase 3, each role has access to their specific authorized dashboard.
 */
export const isRouteAllowedForRole = (pathname, role) => {
  if (!pathname || !role) return false;
  const normalizedRole = String(role).toUpperCase();
  const allowedRoute = ROLE_TO_ROUTE_MAP[normalizedRole];

  if (!allowedRoute) return false;

  // Strict check: current dashboard path must match the permitted dashboard route
  return pathname === allowedRoute || pathname === `${allowedRoute}/`;
};

