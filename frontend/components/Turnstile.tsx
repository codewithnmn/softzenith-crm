"use client";

import Script from "next/script";
import { useEffect, useRef, useState } from "react";

declare global {
  interface Window {
    turnstile?: { render: (el: HTMLElement, options: { sitekey: string }) => string; remove: (id: string) => void };
  }
}

const SITE_KEY = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY;

/**
 * Cloudflare Turnstile captcha for public forms. Adds a hidden "cf-turnstile-response" field to the surrounding form,
 * which the form sends as captchaToken. Renders nothing when no site key is configured (local development).
 * Tokens are single-use: remount it (change its key) after a failed submit.
 */
export function Turnstile({ className }: { className?: string }) {
  const ref = useRef<HTMLDivElement>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!SITE_KEY || !ready || !ref.current || !window.turnstile) return;
    const id = window.turnstile.render(ref.current, { sitekey: SITE_KEY });
    return () => window.turnstile?.remove(id);
  }, [ready]);

  if (!SITE_KEY) return null;
  return (
    <>
      <Script src="https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit" onReady={() => setReady(true)} />
      <div ref={ref} className={className} />
    </>
  );
}
