import countriesJson from "@/content/countries.json";
import universitiesJson from "@/content/universities.json";
import citiesJson from "@/content/cities.json";
import mediaJson from "@/content/media.json";

/** Content carried over from the old website (generated from its pages; see README.md). */
export type Country = { slug: string; name: string; legacyPath: string; universities: number[]; available: boolean };
export type University = {
  uid: number; name: string; title: string; image: string | null; paragraphs: string[]; requirements: string[];
  pageHeading: string; country: string | null; cardTitle?: string; location?: string; blurb?: string;
  cardImage?: string | null;
};
export type City = { city: string; slug: string };

export const countries = countriesJson as Country[];
export const universities = universitiesJson as University[];
export const cities = citiesJson as { ielts: City[]; pte: City[] };
export const media = mediaJson as { galleries: Record<string, string[]>; sliders: Record<string, string[]> };

export const country = (slug: string) => countries.find((c) => c.slug === slug);
export const university = (uid: number) => universities.find((u) => u.uid === uid);
export const universitiesOf = (c: Country) =>
  c.universities.map((uid) => university(uid)).filter((u): u is University => !!u);
