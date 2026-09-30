import { createClient } from "@supabase/supabase-js";

// Supabase phone OTP when configured; otherwise the backend's development login (phone only, no OTP).
const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
const anonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;

/** Keeps the Supabase session (and refreshes its access token before it expires). Null in dev-login mode. */
export const supabase = url && anonKey ? createClient(url, anonKey) : null;
