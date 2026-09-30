"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useSyncExternalStore, type ReactNode } from "react";
import { LogOut, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { platformSession } from "@/lib/platform";

const noSubscription = () => () => {};

/** Frame of the SoftZenith platform console: signed-in check (except on its login page) and a slim top bar. */
export default function PlatformShell({ children }: { children: ReactNode }) {
  const router = useRouter();
  const path = usePathname();
  const onLogin = path.startsWith("/platform/login");
  // The signed-in admin lives in localStorage: unknown while rendering on the server, read once in the browser.
  const user = useSyncExternalStore(noSubscription, () => (platformSession.token() ? platformSession.username() : null), () => undefined);

  useEffect(() => {
    if (!onLogin && user === null) router.replace("/platform/login");
  }, [onLogin, user, router]);

  if (onLogin) return <>{children}</>;
  if (!user) return <div className="flex min-h-screen items-center justify-center text-sm text-muted-foreground">Loading…</div>;

  const signOut = () => {
    platformSession.clear();
    router.replace("/platform/login");
  };

  return (
    <div className="min-h-screen bg-muted/30">
      <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b bg-sidebar px-4 text-sidebar-foreground lg:px-8">
        <Link href="/platform" className="flex items-center gap-2 font-semibold">
          <ShieldCheck className="size-5 text-sky-300" /> SoftZenith Platform
        </Link>
        <span className="ml-auto text-sm text-sidebar-foreground/70">{user}</span>
        <Button variant="ghost" size="sm" onClick={signOut} className="text-sidebar-foreground hover:bg-sidebar-accent">
          <LogOut /> Sign out
        </Button>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 lg:px-8">{children}</main>
    </div>
  );
}
