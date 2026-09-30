/**
 * Text carried over from the old site, kept in one place because several pages repeat it.
 * Obvious typos are fixed ("Sectin", "A extensive"); wording is otherwise unchanged.
 */

export type Package = {
  name: string;
  weeksLabel: string;
  description: string;
  duration: string;
  training: string;
  price?: string;
  image?: string;
};

export const IELTS_GENERAL_PACKAGES: Package[] = [
  { name: "Express Program (General)", weeksLabel: "4 weeks", description: "A crash course for boosting your IELTS band in the last minute", duration: "4 Weeks", training: "45 Hours+", image: "/images/Academic1.jpg" },
  { name: "Extensive Program (General)", weeksLabel: "6 weeks", description: "An extensive course covering all 4 Modules for higher band seekers", duration: "6 Weeks", training: "60 Hours+", image: "/images/Academic2.jpg" },
  { name: "Ultimate Program (General)", weeksLabel: "Ask us", description: "A rigorous 8 band program, loaded with private classes & extensive practice", duration: "On request", training: "On request", image: "/images/Academic3.jpg" },
];

export const IELTS_ACADEMY_PACKAGES: Package[] = [
  { name: "Express Program (Academy)", weeksLabel: "4 weeks", description: "A crash course for boosting your IELTS band in the last minute", duration: "4 Weeks", training: "45 Hours+", image: "/images/Academic1.jpg" },
  { name: "Extensive Program (Academy)", weeksLabel: "6 weeks", description: "An extensive course covering all 4 Modules for higher band seekers", duration: "6 Weeks", training: "60 Hours+", image: "/images/Academic2.jpg" },
  { name: "Ultimate Program (Academy)", weeksLabel: "Ask us", description: "A rigorous 8 band program, loaded with private classes & extensive practice", duration: "On request", training: "On request", image: "/images/Academic3.jpg" },
];

export const ONLINE_PACKAGES: Package[] = [
  { name: "Express Program (General)", weeksLabel: "2 – 3 weeks", price: "Rs. 7,990/-", description: "A crash course for boosting your IELTS band in the last minute", duration: "3 Weeks", training: "60 Hours+" },
  { name: "Extensive Program (General)", weeksLabel: "5 weeks", price: "Rs. 13,990/-", description: "An extensive course covering all 4 Modules for higher band seekers", duration: "5 Weeks", training: "90 Hours+" },
  { name: "Ultimate Program (General)", weeksLabel: "8 weeks", price: "Rs. 19,900/-", description: "A rigorous 8 band program, loaded with private classes & extensive practice", duration: "8 Weeks", training: "130 Hours+" },
];

/** Home page packages (the old home page showed "6 Weeks" badges on the last two). */
export const HOME_PACKAGES: Package[] = [
  { name: "Express Program (General)", weeksLabel: "4 weeks", description: "A crash course for boosting your IELTS band in the last minute", duration: "4 Weeks", training: "45 Hours+" },
  { name: "Extensive Program (General)", weeksLabel: "6 weeks", description: "An extensive course covering all 4 Modules for higher band seekers", duration: "6 Weeks", training: "60 Hours+" },
  { name: "Ultimate Program (General)", weeksLabel: "6 weeks", description: "A rigorous 8 band program, loaded with private classes & extensive practice", duration: "On request", training: "On request" },
];

export type TestSection = { name: string; summary: string; points: string[] };

const LISTENING: TestSection = {
  name: "Listening",
  summary: "4 sections, 40 questions, 30 minutes",
  points: [
    "Section 1: a conversation between two people",
    "Section 2: a lecture in an everyday social context",
    "Section 3: a conversation between up to four people set in an educational or training context",
    "Section 4: a lecture on an academic subject (e.g. a university lecture)",
    "Each section is heard once only",
  ],
};

const SPEAKING: TestSection = {
  name: "Speaking",
  summary: "An interview, 15 minutes",
  points: [
    "Part 1 Introduction and interview",
    "Part 2 Individual long turn (you have to talk about a topic)",
    "Part 3 Two-way discussion (The examiner asks further questions which are connected to the topic of Part 2.)",
  ],
};

const SCORES =
  "Multi-level. You get a score between 1 and 9. Half scores such as 6.5 are possible. Universities often demand an IELTS score of 6 or 7. They may also demand a minimum score in each of the 4 sections.";

export const IELTS_GENERAL_TEST = {
  title: "IELTS General",
  question: "What is the IELTS General test like?",
  intro:
    "The General version of IELTS is easier than the academic version. All candidates do the same Listening and Speaking sections. The test has four sections:",
  sections: [
    LISTENING,
    SPEAKING,
    {
      name: "Reading",
      summary: "3 sections, 40 questions, 60 minutes",
      points: [
        "Section 1: two or three short factual texts",
        "Section 2: contains two short factual texts focusing on work-related issues",
        "Section 3: one longer, more complex text.",
      ],
    },
    {
      name: "Writing",
      summary: "",
      points: [
        "Part 1: write a letter requesting information or explaining a situation.",
        "Part 2: write an essay in response to a point of view, argument or problem.",
      ],
    },
  ] as TestSection[],
  scores: SCORES,
};

export const IELTS_ACADEMIC_TEST = {
  title: "IELTS Academic",
  question: "What is the IELTS Academic test like?",
  intro:
    "The Academic version of IELTS is harder than the general version. All candidates do the same Listening and Speaking sections. The test has four sections:",
  sections: [
    LISTENING,
    SPEAKING,
    {
      name: "Reading",
      summary: "3 long reading passages, 40 questions, 60 minutes",
      points: ["Each section contains one long text. Texts are from books, journals, magazines and newspapers."],
    },
    {
      name: "Writing",
      summary: "",
      points: [
        "Part 1: describe, summarise or explain the information in a graph, table, chart or diagram",
        "Part 2: write an essay in response to a point of view, argument or problem",
      ],
    },
  ] as TestSection[],
  scores: SCORES,
};

export const IELTS_GENERAL_BLURB =
  "The IELTS General Training test is for those who aim to settle abroad in an English speaking country. IELTS test counts for 60% of the total eligibility criteria required to get permanent residency in such countries.";
export const IELTS_ACADEMIC_BLURB =
  "IELTS developed the Academic test if you wish to study at university or college as an undergraduate or postgraduate student, or, if you want to join or gain entry into a professional institution.";

/** Copy for the ~79 "IELTS classes in <city>" pages (identical on the old site apart from the city). */
export const ieltsCityCopy = (city: string) => ({
  intro: [
    `In countries like Canada, United Kingdom, USA, Australia, and New Zealand, IELTS is required for those seeking admission to universities. You can prove your proficiency in English by passing the IELTS test. Academic IELTS and General IELTS are the two types of IELTS available. Students planning to study overseas should apply for IELTS Academic while people seeking employment or permanent residency abroad should apply for IELTS General. We at Western World Visa Services ${city} offer IELTS preparation coaching, study-abroad guidance, career counselling, and profile-building classes.`,
    "There are four modules in IELTS. Speaking, listening, reading, and writing. You will have 60 minutes to complete each module on test day. Speaking will take place on other days; reading, writing, and listening will be conducted on the same day. Afterward, you get a band out of nine. Your proficiency in the English language can be determined by these IELTS bands.",
    `Western World Visa Services is one of the leading consultancies for study abroad and IELTS in ${city}, India. Through our indefatigable ability to provide excellent services in the fields of Career counselling, IELTS coaching, Study Abroad admission guidance, and visa assisting as well as SOP writing, we have gained an immense reputation in ${city}, India. You will receive all the abroad education assistance from IELTS preparation to landing overseas under one roof. You will be guided all the way to abroad education by our team of highly skilled and experienced coaches. Allow us to take care of everything while you focus on getting ready for your study overseas journey. To begin your study abroad education, you need to prepare for the IELTS exam. Our team at the office will help you prepare well for your IELTS exams.`,
  ],
  features: [
    ["Skilled and experienced staff", `Our IELTS instructors in ${city}, India have extensive experience and are qualified to maximize your chances of success. During this course, you will learn unique techniques for cracking the IELTS test and advanced strategies for doing well. Furthermore, these courses will help you to improve your English skills.`],
    ["Quality Education", `IELTS Institute in ${city}, India focuses on an activity-based, interactive curriculum for preparing students for the IELTS test by providing highly interactive and small-group classes that upgrade you to receive in-depth knowledge and increase your self-confidence.`],
    ["Exclusive Study Material", `At ${city} institution, we offer precise, specific and detailed, and intense IELTS study material that is intent on the pace of learning along with a wider range of IELTS practice test sheets to improvise your skill and lead you to score a high band in your IELTS test. Additionally, we also provide exclusive practice papers for vocabulary and grammar progressions.`],
    ["Mode of Instruction", `Western World Visa Services IELTS coaching in ${city} offers both online and offline IELTS classes. The modules of the course are customized according to the knowledge and time requirements of the individual student.`],
    ["The pace of learning", `For an in-depth understanding of the English language, Western World Visa Services, ${city}, offers progressive courses primarily focused on Skilled English learning and assist you in getting immense success on your IELTS examination. To enhance your English grammar skills, you can also enroll in separate grammar sessions during the ongoing courses.`],
    ["Detailed Practice IELTS Test Papers", "Students are provided with a variety of full-length IELTS mock tests to enhance their skills and develop the competencies to crack the IELTS exam with extreme success."],
    ["Personalized Oral IELTS Mock Assessments", `We at ${city}, India provides detailed speaking IELTS practice test to enhance your pronunciation abilities, intelligence, confidence, and abbreviation that expertise you to rise your verbal wisdom and score high in IELTS Speaking Module.`],
    ["Cost-effective", "We at North Delhi, provide a complete range of budget-friendly and economical IELTS Learning programs. That includes correspondence as well as full-length IELTS preparation courses. It focuses on the entire IELTS modules as well as specific IELTS modules where you require extra attention. You can also take grammar-focused online courses, which provide many academic advantages at a low cost. Spend only INR 5000/ on all the study materials, live sessions, and two full-length mock tests for IELTS preparation now. (Limited offer)."],
    ["Intensive classes", `These classes are specially designed to clear up all of your doubts and facilitate group discussions. At Western World Visa Services ${city}, our apprehension classes are specially designed to resolve your doubts and give you the clarity to rectify your problem. As a result, your IELTS score will also be enhanced.`],
  ] as [string, string][],
  closing: `Visit Western World Visa Services, ${city}, for more information about IELTS Coaching, Abroad Education, Profile building, Overseas Admissions, Scholarships, and securing high bands on IELTS. You can also book an appointment by calling the following numbers:`,
});

/** PTE copy; `city` is undefined on the main PTE classes page (which says "5 years" and "Rohtak"). */
export const pteCopy = (city?: string) => {
  const where = city ?? "Rohtak";
  return {
    intro: city
      ? `Excel in PTE with Western World Visa Services: Your Pathway to Study Abroad and High Bands. If you're searching for the top PTE classes near you, look no further than Western World Visa Services, the trusted name in PTE Academic coaching in ${city}. Strategically located in Rohtak, we serve students from Rohtak, Bahadurgarh, Gurgaon, and ${city} nearby areas, offering PTE affordable classes with both online and offline options. Achieving high bands on the PTE is your gateway to success, and we're here to make that happen.`
      : "Excel in PTE with Western World Visa Services: Your Pathway to Study Abroad and High Bands. If you're searching for the top PTE classes near you, look no further than Western World Visa Services, the trusted name in PTE Academic coaching in Rohtak. Strategically located in Rohtak, we serve students from Rohtak, Bahadurgarh, Gurgaon, and other nearby areas, offering PTE affordable classes with both online and offline options. Achieving high bands on the PTE is your gateway to success, and we're here to make that happen.",
    whyTitle: `Why Western World Visa Services is the Best Choice for PTE in ${where}`,
    whyIntro:
      "Whether you're aspiring to study abroad in Australia or any other global destination, scoring well on the PTE Academic test is crucial. Here's how Western World Visa Services ensures your success:",
    reasons: [
      ["Top PTE Classes Near You", city
        ? `Conveniently located in ${city}, we are the go-to destination for students from Rohtak, Bahadurgarh, Gurgaon and ${city} areas in India.`
        : "Conveniently located in Rohtak, we are the go-to destination for students from Rohtak, Bahadurgarh, Gurgaon and nearby areas in India."],
      ["Experienced Faculty", `Our trainers have over ${city ? "10" : "5"} years of expertise in PTE coaching, providing you with personalized tips and strategies to boost your confidence.`],
      ["Latest Study Material", "Access comprehensive resources, including updated materials and techniques to score PTE high bands."],
      ["Affordable PTE Classes", "Quality coaching at a price that fits your budget. We believe education should be accessible to all."],
      ["Tailored Coaching for PTE Academic", "From Speaking to Writing, Reading, and Listening, we cover every aspect of the exam in detail."],
      ["Online & Offline Options", "Whether you prefer learning in person at our Delhi PTE classes or online from the comfort of your home, we’ve got you covered."],
      ["Free 3-Day Trial Classes", "Experience our teaching quality before enrolling. Register now for your free trial here."],
    ] as [string, string][],
    modulesIntro: "Modules We Cover: Our approach focuses on excelling across all PTE sections:",
    modules: [
      ["Speaking Module", "Develop fluency, clarity, and confidence with focused practice and guidance."],
      ["Writing Module", "Master essay and summary writing with expert tips on grammar, coherence, and time management."],
      ["Reading Module", "Enhance your reading comprehension skills with targeted techniques."],
      ["Listening Module", "Improve your ability to grasp accents and nuances through structured exercises."],
    ] as [string, string][],
    gatewayTitle: "Why PTE is Your Gateway to Australia and Beyond",
    gateway:
      "The PTE Academic is a trusted test for those aiming to study abroad in countries like Australia. With the right coaching, you can achieve high bands and open doors to endless opportunities. At Western World Visa Services, we equip you with the skills needed to excel in the test and beyond.",
    benefitsTitle: "Benefits of Joining Western World Visa Services PTE Classes",
    benefits: [
      "One-on-one attention with customized feedback.",
      "Mock tests to simulate the real exam environment.",
      "Accessible and flexible scheduling for PTE classes near you.",
      "A proven track record of student success stories.",
    ],
    enrol: city ? { title: "Enroll with Delhi’s Best PTE Classes", text: `Searching for PTE ${city}? Your journey to success starts here! Contact Western World Visa Services today at` } : null,
    closing: city
      ? `Make your dreams of studying abroad a reality with Western World Visa Services — India's trusted partner in PTE coaching ${city}. Join us today and let us guide you on your journey as students' gateway to Australia and beyond!`
      : "Make your dreams of studying abroad a reality with Western World Visa Services — India's trusted partner in PTE coaching. Join us today and let us guide you on your journey as students' gateway to Australia and beyond!",
  };
};
