"use client";

import Image from "next/image";
import { useEffect, useState } from "react";

/** Cross-fading background photos (the old site's home slider). */
export default function Slider({ images, alts = [] }: { images: string[]; alts?: string[] }) {
  const [i, setI] = useState(0);
  useEffect(() => {
    if (images.length < 2) return;
    const t = setInterval(() => setI((x) => (x + 1) % images.length), 5000);
    return () => clearInterval(t);
  }, [images.length]);

  return (
    <div className="absolute inset-0">
      {images.map((src, n) => (
        <Image key={src} src={src} alt={alts[n] ?? ""} fill priority={n === 0} sizes="100vw"
               className={`object-cover transition-opacity duration-1000 ${n === i ? "opacity-100" : "opacity-0"}`} />
      ))}
      <div className="absolute bottom-6 left-1/2 z-10 flex -translate-x-1/2 gap-2">
        {images.map((src, n) => (
          <button key={src} onClick={() => setI(n)} aria-label={`Slide ${n + 1}`}
                  className={`h-1.5 rounded-full transition-all ${n === i ? "w-8 bg-white" : "w-3 bg-white/50"}`} />
        ))}
      </div>
    </div>
  );
}
