import { DocsLayout } from 'fumadocs-ui/layouts/docs';
import type { ReactNode } from 'react';
import { source } from '@/lib/source';
import { DocsNavbar } from '@/components/DocsNavbar';
import { DocsFooter } from '@/components/DocsFooter';

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <>
      <DocsLayout
        tree={source.pageTree}
        nav={{ component: <DocsNavbar /> }}
        githubUrl="https://github.com/wkspower/wks-platform"
      >
        {children}
      </DocsLayout>
      <DocsFooter />
    </>
  );
}
