"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ErrorText, Field } from "@/components/common";
import { errorMessage } from "@/lib/api";
import { platformApi, platformSession } from "@/lib/platform";

/** Sign-in for SoftZenith platform admins (username + password). Tenant staff sign in at /login with their phone. */
export default function PlatformLoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    const { data, error } = await platformApi.POST("/api/platform/auth/login", { body: { username, password } });
    setBusy(false);
    if (!data) {
      setPassword("");
      setError(errorMessage(error));
      return;
    }
    platformSession.set(data.accessToken!, data.username!);
    router.replace("/platform");
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-sidebar p-6">
      <form onSubmit={submit} className="w-full max-w-sm space-y-5 rounded-2xl bg-card p-8 shadow-2xl">
        <div className="space-y-1">
          <p className="flex items-center gap-2 text-sm font-semibold text-primary"><ShieldCheck className="size-4" /> SoftZenith Platform</p>
          <h1 className="text-2xl font-semibold tracking-tight">Administrator sign in</h1>
          <p className="text-sm text-muted-foreground">For onboarding and configuring businesses. Not for tenant staff.</p>
        </div>
        <Field label="Username">
          <Input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" required autoFocus className="h-11" />
        </Field>
        <Field label="Password">
          <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" required className="h-11" />
        </Field>
        <ErrorText>{error}</ErrorText>
        <Button type="submit" disabled={busy} className="h-11 w-full">{busy ? "Signing in…" : "Sign in"}</Button>
      </form>
    </main>
  );
}
