/** Business details shown across the site. */
export const SITE = {
  name: "Western World Visa Services",
  shortName: "WWVS",
  legalName: "Western World Visa Services Pvt. Ltd.",
  url: "https://www.westernworldvisaservices.com",
  address: "1st Floor, POWER HOUSE CHOWK, above CANARA Bank, Model Town, Rohtak, Haryana 124001",
  phones: ["+91-7027022554", "+91-9812727721"],
  email: "info@westernworldvisaservices.com",
  whatsapp: "https://api.whatsapp.com/send?phone=917027022554",
  webmail: "https://webmail.westernworldvisaservices.com/",
  social: {
    facebook: "https://www.facebook.com/westernworldvisaservices/",
    instagram: "https://www.instagram.com/westernworldvisaservices/?hl=en",
    linkedin: "https://in.linkedin.com/in/western-world-visa-services-288826235?trk=public_profile_browsemap",
  },
  footerAbout:
    "Western World Visa Services is an overseas educational consultancy dedicated towards providing services to Indian students in making educational avenues in countries...",
};

export const tel = (phone: string) => `tel:${phone.replace(/[^+\d]/g, "")}`;

export type NavItem = { label: string; href: string; children?: NavItem[] };

/** Same menu as the old site (Singapore added: it had a page but no menu entry). */
export const NAV: NavItem[] = [
  { label: "Home", href: "/" },
  { label: "About Us", href: "/about-us" },
  {
    label: "Global Education",
    href: "/study-in/canada",
    children: [
      { label: "Study in Canada", href: "/study-in/canada" },
      { label: "Study in New Zealand", href: "/study-in/new-zealand" },
      { label: "Study in USA", href: "/study-in/usa" },
      { label: "Study in UK", href: "/study-in/uk" },
      { label: "Study in Ireland", href: "/study-in/ireland" },
      { label: "Study in France", href: "/study-in/france" },
      { label: "Study in Germany", href: "/study-in/germany" },
      { label: "Study in Singapore", href: "/study-in/singapore" },
    ],
  },
  {
    label: "Services",
    href: "/ielts-general",
    children: [
      { label: "IELTS General", href: "/ielts-general" },
      { label: "IELTS Academy", href: "/ielts-academy" },
      { label: "PTE", href: "/pte" },
      { label: "TOEFL", href: "/toefl" },
      { label: "UKVI-IELTS", href: "/ukvi-ielts" },
      { label: "CELPIP", href: "/ielts-academy" },
      { label: "OET", href: "/ielts-academy" },
      { label: "DUOLINGO", href: "/ielts-academy" },
      { label: "GRE", href: "/ielts-academy" },
      { label: "GMAT", href: "/ielts-academy" },
      { label: "Online Courses", href: "/online-courses" },
      { label: "Visa Assistance", href: "/visa-assistance" },
    ],
  },
  {
    label: "IELTS Academic",
    href: "/classroom-courses",
    children: [
      { label: "Classroom Courses", href: "/classroom-courses" },
      { label: "Online Courses", href: "/online-courses" },
    ],
  },
  { label: "Our Blog", href: "/blog" },
  { label: "Contact Us", href: "/contact-us" },
];

export const FOOTER_LINKS: NavItem[] = [
  { label: "Home", href: "/" },
  { label: "About Us", href: "/about-us" },
  { label: "IELTS Classes", href: "/ielts-classes-india" },
  { label: "PTE Classes", href: "/pte-classes-india" },
  { label: "Contact", href: "/contact-us" },
];

/** Link to the contact form with the course/interest filled in. */
export const enquire = (course?: string, country?: string) => {
  const q = new URLSearchParams();
  if (course) q.set("course", course);
  if (country) q.set("country", country);
  const s = q.toString();
  return `/contact-us${s ? `?${s}` : ""}#enquiry`;
};
