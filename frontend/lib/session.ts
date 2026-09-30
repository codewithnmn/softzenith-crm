import { supabase } from "./supabase";

// Signed-in marker / dev-login token and the selected tenant, kept in the browser.
const TOKEN = "crm.token";
const TENANT = "crm.tenant";

const read = (key: string) => (typeof window === "undefined" ? null : localStorage.getItem(key));

export const session = {
  token: () => read(TOKEN),
  tenant: () => read(TENANT),
  setToken: (token: string) => localStorage.setItem(TOKEN, token),
  setTenant: (tenantId: string) => localStorage.setItem(TENANT, tenantId),
  clear: () => {
    localStorage.removeItem(TOKEN);
    localStorage.removeItem(TENANT);
  },
  /**
   * The token to send. With Supabase it comes from the Supabase client, which refreshes it before it expires
   * (access tokens live about an hour); a stored copy would sign everyone out hourly.
   */
  accessToken: async (): Promise<string | null> => {
    if (!supabase) return read(TOKEN);
    const { data } = await supabase.auth.getSession();
    return data.session?.access_token ?? null;
  },
};
