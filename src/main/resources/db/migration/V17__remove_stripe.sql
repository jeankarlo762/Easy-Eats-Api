ALTER TABLE tbempresa
    DROP COLUMN stripe_account_id,
    DROP COLUMN stripe_charges_enabled,
    DROP COLUMN stripe_onboarding_status,
    DROP COLUMN stripe_payouts_enabled;
