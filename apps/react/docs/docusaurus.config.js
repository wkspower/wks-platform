// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require("prism-react-renderer/themes/github");
const darkCodeTheme = require("prism-react-renderer/themes/dracula");

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "WKS Platform",
  tagline: "Adaptive Case Management Platform",
  url: "https://docs.wkspower.com",
  baseUrl: "/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "wks-power", // Usually your GitHub org/user name.
  projectName: "wks-platform-docs", // Usually your repo name.

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  scripts: [],

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
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
          customCss: [require.resolve("./src/css/custom.css")],
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: "light",
        disableSwitch: true,
        respectPrefersColorScheme: false,
      },
      docs: {
        sidebar: {
          autoCollapseCategories: true,
        },
      },
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
        // style: 'dark',
        links: [
          // {
          //   title: 'Docs',
          //   items: [
          //     {
          //       label: 'Solution Architecture',
          //       to: '/docs/architecture',
          //     },
          //   ],
          // },
          // {
          //   title: 'Community',
          //   items: [
          //     {
          //       label: 'CI/CD',
          //       href: 'https://stackoverflow.com/questions/tagged/docusaurus',
          //     },
          //     {
          //       label: 'Sonar',
          //       href: 'https://stackoverflow.com/questions/tagged/docusaurus',
          //     }
          //   ],
          // },
          // {
          //   title: 'More',
          //   items: [
          //     {
          //       label: 'GitHub',
          //       href: 'https://github.com/wkspower/wks-platform',
          //     },
          //   ],
          // },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} WKS Power.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

if (process.env.PROD === "true") {
  config.scripts.push({
    src: "https://www.googletagmanager.com/gtag/js?id=G-DD0FWZXED7",
    async: true,
  });
  config.scripts.push({
    src: "/js/gtag.js",
    async: false,
  });
}

module.exports = config;
