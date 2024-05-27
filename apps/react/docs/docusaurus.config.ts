import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
  title: "WKS Platform",
  tagline: "Adaptive Case Management Platform",
  url: "https://www.wkspower.com",
  baseUrl: "/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "wks-power", // Usually your GitHub org/user name.
  projectName: "wks-platform-docs", // Usually your repo name.

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          breadcrumbs: false,
          editUrl: "https://github.com/wkspower/wks-platform-website/tree/main",
        },
        blog: {
          showReadingTime: false,
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: "https://github.com/wkspower/wks-platform-website/tree/main",
        },
        theme: {
          customCss: "./src/css/custom.css",
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [],

  themeConfig: {
    // Replace with your project's social card
    image: "img/docusaurus-social-card.jpg",
    navbar: {
      title: "",
      logo: {
        alt: "WKS Platform Logo",
        src: "img/logo.svg",
      },
      items: [
        {
          href: "https://www.wkspower.com/blog",
          label: "Blog",
          position: "left",
        },
        {
          type: "doc",
          docId: "Introduction/intro",
          position: "left",
          label: "Docs",
        },
        {
          type: "doc",
          docId: "Introduction/intro-video",
          position: "left",
          label: "Intro Video",
        },
        {
          href: "https://www.wkspower.com/contact",
          label: "Contact Us",
          position: "right",
        },
        {
          href: "https://www.wkspower.com/subscribe",
          label: "Subscribe",
          position: "right",
        },
        {
          href: "https://wkspower.thinkific.com/courses/wks-platform-acm",
          label: "In-Company Training",
          position: "right",
        },
        {
          href: "https://github.com/wkspower/wks-platform",
          label: "GitHub",
          position: "right",
        },
      ],
    },
    footer: {
      // style: "dark",
      links: [
        // {
        //   title: "Docs",
        //   items: [
        //     {
        //       label: "Tutorial",
        //       to: "/docs/intro",
        //     },
        //   ],
        // },
        // {
        //   title: "Community",
        //   items: [
        //     {
        //       label: "Stack Overflow",
        //       href: "https://stackoverflow.com/questions/tagged/docusaurus",
        //     },
        //     {
        //       label: "Discord",
        //       href: "https://discordapp.com/invite/docusaurus",
        //     },
        //     {
        //       label: "Twitter",
        //       href: "https://twitter.com/docusaurus",
        //     },
        //   ],
        // },
        // {
        //   title: "More",
        //   items: [
        //     {
        //       label: "Blog",
        //       to: "/blog",
        //     },
        //     {
        //       label: "GitHub",
        //       href: "https://github.com/facebook/docusaurus",
        //     },
        //   ],
        // },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} WKS Power.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  } satisfies Preset.ThemeConfig,
};

if (process.env.PROD === "true") {
  config.plugins.push([
    "@docusaurus/plugin-google-gtag",
    {
      trackingID: process.env.GTAG_ID,
      anonymizeIP: true,
    },
  ]);
}

export default config;
