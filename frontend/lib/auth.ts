import { api, errorMessage } from "./api";
import { supabase } from "./supabase";

export const authMode: "supabase" | "dev" = supabase ? "supabase" : "dev";

/** Supabase wants E.164; assume India when a bare 10-digit number is typed. */
function toE164(phone: string) {
  const digits = phone.replace(/[^\d+]/g, "");
  return digits.startsWith("+") ? digits : digits.length === 10 ? `+91${digits}` : `+${digits}`;
}

export async function devLogin(phone: string): Promise<string> {
  const { data, error } = await api.POST("/api/v1/dev/login", { body: { phone } });
  if (error || !data) throw new Error(errorMessage(error));
  return data.accessToken!;
}

export async function sendOtp(phone: string) {
  const { error } = await supabase!.auth.signInWithOtp({ phone: toE164(phone) });
  if (error) throw error;
}

export async function verifyOtp(phone: string, code: string): Promise<string> {
  const { data, error } = await supabase!.auth.verifyOtp({ phone: toE164(phone), token: code, type: "sms" });
  if (error || !data.session) throw error ?? new Error("Verification failed");
  return data.session.access_token;
}

export async function signOut() {
  await supabase?.auth.signOut();
}
